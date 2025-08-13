package model.mistforest.mistobj.activityboss;

import cfg.CrossConstConfig;
import cfg.MistActivityBossConfig;
import cfg.MistActivityBossConfigObject;
import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import cfg.MistRebornChangeProp;
import cfg.MistRebornChangePropObject;
import common.GameConst;
import java.util.List;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistActivityBossStage;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import util.TimeUtil;

public class MistBossManEater extends MistActivityBoss {
    protected long phantomObjId;

    protected long createMonsterTime;

    protected int monsterRebornChangeCfgId; // 临时属性配置，后续优化

    protected MistBornPosController posController = new MistBornPosController();

    public MistBossManEater(MistRoom room, int objType) {
        super(room, objType);
    }

    public long getPhantomObjId() {
        return phantomObjId;
    }

    public void setPhantomObjId(long phantomObjId) {
        this.phantomObjId = phantomObjId;
    }

    public MistBornPosController getPosController() {
        return posController;
    }

    @Override
    public void clear() {
        super.clear();
        posController.clear();
        phantomObjId = 0;
        monsterRebornChangeCfgId = 0;
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        int complxBornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        MistComboBornPosConfigObject cfg = MistComboBornPosConfig.getById(complxBornPosId);
        if (cfg != null && cfg.getSlaveobjposlist().length > 0) {
            posController.init(cfg.getSlaveobjposlist());
            if (cfg.getSlaveobjposlist()[0] != null && cfg.getSlaveobjposlist()[0].length >= 3) {
                monsterRebornChangeCfgId = cfg.getSlaveobjposlist()[0][2];
            }
        }

        List<ProtoVector> posList = posController.getEmptyPosMap();
        if (!CollectionUtils.isEmpty(posList)) {
            posList.remove(0); // 第一个位置留给幻象
        }
    }

    @Override
    public void initRebornTime() {
        int rebornTime = (int) getAttribute(MistUnitPropTypeEnum.MUPT_RebornTime_VALUE);
        if (rebornTime > 0) {
            rebornTime = Math.max(MistConst.MistDelayRemoveTime, rebornTime);
        }
        setRebornTime(rebornTime);
    }

    @Override
    public void recoverHp(boolean init) {
        long maxHp = getAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE);
        if (maxHp > 0) {
            if (init) {
                setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, maxHp);
            } else {
                long rate = CrossConstConfig.getById(GameConst.ConfigId).getManeaterrebornhprate();
                long curHp = maxHp * rate / 1000;
                setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
                addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
            }
        }
    }

    @Override
    public void settleDamage(MistFighter fighter, long damage) {
        if (!isAlive()) {
            return;
        }
        long curHp = getAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE);
        if (curHp <= 0) {
            return;
        }
        int changeRate = 500; // 暂写死 变身比例50%
        MistActivityBossConfigObject config = MistActivityBossConfig.getById(getRoom().getObjGenerator().getBossObjCfgId());
        if (null != config) {
            changeRate = config.getChangestagehprate();
        }
        long maxHp = getAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE);
//        damage = maxHp / 2 + 1;
        curHp = Math.max(curHp - damage, 0);
        setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
        if (curHp > 0) {
            int stage = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ActivityBossStage_VALUE);
            if (MistActivityBossStage.initStage == stage && (curHp * 1000 / maxHp) < changeRate) {
                changeToStage(MistActivityBossStage.furyStage);
            }
        } else {
            MistManEaterPhantom phantom = getRoom().getObjManager().getMistObj(getPhantomObjId());
            if (null != phantom && phantom.isAlive()) {
                dead();
            } else {
                preDead();
                generateRewardObj();
            }
        }
        Long posData = MistConst.buildComboRebornPos((int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE));
        fighter.changeFighterPos(posData);
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (null == player) {
            return;
        }
        getRoom().updateBossActivityRank(player, damage);
    }

    @Override
    public void dead() {
        if (getAttribute(MistUnitPropTypeEnum.MUPT_ActivityBossStage_VALUE) != MistActivityBossStage.furyStage) {
            clearSlaveObj();
        }
        super.dead();

    }

    @Override
    public void changeToStage(int stage) {
        super.changeToStage(stage);
        if (stage == MistActivityBossStage.furyStage) {
            generateManEaterPhantom();
            createMonsterTime = 0;
        }
    }

    @Override
    protected void onBossInitStage(long curTime) {
        super.onBossInitStage(curTime);

        if (createMonsterTime < curTime) {
            generateManEaterMonster();
            createMonsterTime = curTime + CrossConstConfig.getById(GameConst.ConfigId).getCreatemaneatermonsterinterval() * TimeUtil.MS_IN_A_S;
        }
    }

    @Override
    public void clearSlaveObj() {
        super.clearSlaveObj();

        posController.resetEmptyPos();
        if (getPhantomObjId() == 0) {
            return;
        }
        MistObject obj = getRoom().getObjManager().getMistObj(getPhantomObjId());
        if (obj == null) {
            return;
        }
        obj.setRebornTime(0);
        if (obj.isAlive()) {
            obj.dead();
        } else if (obj.getAttribute(MistUnitPropTypeEnum.MUPT_NotRemoveWhenDead_VALUE) > 0) {
            obj.removeObjFromMap();
        }
        setPhantomObjId(0);
    }

    protected void generateManEaterMonster() {
        int maxCount = Math.min(posController.getEmptyPosCount() + posController.getUsePosCount(), CrossConstConfig.getById(GameConst.ConfigId).getMaxmaneatermonstercount());
        int count = Math.min(maxCount, CrossConstConfig.getById(GameConst.ConfigId).getCreatemaneatermonstercount());
        if (count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            if (null != slaveObjList && slaveObjList.size() >= maxCount) {
                return;
            }
            ProtoVector pos = posController.getAndUseEmptyPos();
            if (null == pos) {
                continue;
            }
            MistManEaterMonster monster = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_ManEaterMonster_VALUE);
            monster.initByMaster(this);

            MistRebornChangePropObject rebornChangePropCfg = MistRebornChangeProp.getById(monsterRebornChangeCfgId);
            monster.updateRandomProp(rebornChangePropCfg, false);
            monster.afterInit(new int[]{pos.getX(), pos.getY()}, null);

            addSlaveObj(monster.getId());
            getRoom().getWorldMap().objFirstEnter(monster);
        }
    }

    protected void generateManEaterPhantom() {
        int complxBornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        MistComboBornPosConfigObject posCfg = MistComboBornPosConfig.getById(complxBornPosId);
        if (null == posCfg) {
            return;
        }
        if (null == posCfg.getSlaveobjposlist()) {
            return;
        }
        int length = posCfg.getSlaveobjposlist().length;
        if (length <= 0) {
            return;
        }
        int[] posData = posCfg.getSlaveobjposlist()[0];
        if (posData.length < 2) {
            return;
        }
        MistManEaterPhantom monster = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_ManEaterPhantom_VALUE);
        monster.initByMaster(this);
        long maxHp = getAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE);
        monster.setAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE,maxHp);
        monster.afterInit(posData, null);

        setPhantomObjId(monster.getId());
        boolean isOverallObj = monster.getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0;
        if (isOverallObj) {
            getRoom().getObjGenerator().addOverallObjId(monster.getId());
            monster.addCreateObjCmd();
        } else {
            getRoom().getWorldMap().objFirstEnter(monster);
        }
    }
}
