package model.mistforest.mistobj.activityboss;

import cfg.MistActivityBossConfig;
import cfg.MistActivityBossConfigObject;
import java.util.HashMap;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistActivityBossStage;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.mistobj.rewardobj.MistCrystalBox;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.ServerTransfer.EnumMistPveBattleType;
import util.LogUtil;
import util.TimeUtil;

public class MistActivityBoss extends MistObject {
    public MistActivityBoss(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        addCreateBuffEffect();
    }

    protected void addCreateBuffEffect() {
        long createBuffId = getAttribute(MistUnitPropTypeEnum.MUPT_CreateBuffId_VALUE);
        if (createBuffId > 0) {
            getBufMachine().addBuff((int) createBuffId,this, null);
        }
    }

    protected void addDeadBuffEffect() {
        long preDeadBuffId = getAttribute(MistUnitPropTypeEnum.MUPT_PreDeadBuffId_VALUE);
        if (preDeadBuffId > 0) {
            HashMap<Integer, Long> params = new HashMap<>();
            params.put(MistTriggerParamType.RemoveObjId, getId());
            getBufMachine().addBuff((int) preDeadBuffId, this, params);
        }
    }

    protected void addDisappearBuffEffect() {
        long escapeBuffId = getAttribute(MistUnitPropTypeEnum.MUPT_BossEscapeBuffId_VALUE);
        if (escapeBuffId > 0) {
            HashMap<Integer, Long> params = new HashMap<>();
            params.put(MistTriggerParamType.RemoveObjId, getId());
            getBufMachine().addBuff((int) escapeBuffId, this, params);
        }
    }

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
            preDead();
            generateRewardObj();
        }
        Long posData = MistConst.buildComboRebornPos((int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE));
        fighter.changeFighterPos(posData);
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (null == player) {
            return;
        }
        getRoom().updateBossActivityRank(player, damage);
    }

    public void changeToStage(int stage) {
        setAttribute(MistUnitPropTypeEnum.MUPT_ActivityBossStage_VALUE, stage);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_ActivityBossStage_VALUE, stage);
    }

    public void beTouch(MistFighter fighter) {
        if (fighter.isBattling()) {
            return;
        }
        if (!isAlive()) {
            return;
        }
        fighter.enterPveBattle(EnumMistPveBattleType.EMPBT_EliteMonsterBattle_VALUE, this);
    }

    public void preDead() {
        setAttribute(MistUnitPropTypeEnum.MUPT_TouchMask_VALUE, -1);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_TouchMask_VALUE, -1);

        setRebornTime(0);
        setDeadTimeStamp(0);
        addDeadBuffEffect();
        clearSlaveObj();
    }

    public void disappear() {
        setAttribute(MistUnitPropTypeEnum.MUPT_PreDeadState_VALUE, 1);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_PreDeadState_VALUE, 1);

        setRebornTime(0);
        setDeadTimeStamp(0);

        clearSlaveObj();
        addDisappearBuffEffect();
    }

    protected void generateRewardObj() {
        MistActivityBossConfigObject config = MistActivityBossConfig.getById(getRoom().getObjGenerator().getBossObjCfgId());
        if (config == null) {
            return;
        }
        int[][] rewardBoxData = config.getDropboxlist();
        if (null == rewardBoxData || rewardBoxData.length <= 0) {
            return;
        }
        int index = RandomUtils.nextInt(rewardBoxData.length);
        if (rewardBoxData[index].length < 3) {
            return;
        }
        MistCrystalBox box = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_CrystalBox_VALUE);
        box.setAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE, rewardBoxData[index][0]);
        box.setAttribute(MistUnitPropTypeEnum.MUPT_LifeTime_VALUE, rewardBoxData[index][1]);
        box.setAttribute(MistUnitPropTypeEnum.MUPT_ImageId_VALUE, rewardBoxData[index][2]);

        box.setAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE, 1);
        box.afterInit(new int[]{getPos().getX(), getPos().getY()}, new int[]{getToward().getX(), getToward().getY()});
        box.setRebornTime(0);

        getRoom().getObjGenerator().addOverallObjId(box.getId());
        box.addCreateObjCmd();
    }

    protected void onBossInitStage(long curTime) {

    }

    protected void onBossFuryStage(long curTime) {
        if (CollectionUtils.isEmpty(getSlaveObjList())) {
            changeToStage(MistActivityBossStage.weakStage);
        }
    }

    protected void onBossWeakStage(long curTime) {

    }

    public void updateBossStage(long curTime) {
        int stage = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ActivityBossStage_VALUE);
        switch (stage) {
            case MistActivityBossStage.initStage:{
                onBossInitStage(curTime);
                break;
            }
            case MistActivityBossStage.furyStage:{
                onBossFuryStage(curTime);
                break;
            }
            case MistActivityBossStage.weakStage:{
                onBossWeakStage(curTime);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onTick(long curTime) {
        updateBossStage(curTime);

        bufMachine.onTick(curTime);
        updateBattleCmd();
        battleCmdList.clear();
        if (!isAlive()) {
            long deadTimeStamp = getDeadTimeStamp();
            if (rebornTime <= 0) {
                if (deadTimeStamp == 0 || curTime - deadTimeStamp > TimeUtil.MS_IN_A_S * MistConst.MistDelayRemoveTime - 100) {
                    LogUtil.debug("remove bossObj id = " + getId());
                    removeObjFromMap();
                    room.getObjManager().removeObj(getId());
                    clear();
                }
            } else if (curTime - deadTimeStamp >= rebornTime * TimeUtil.MS_IN_A_S) {
                reborn();
            }
        }
    }
}
