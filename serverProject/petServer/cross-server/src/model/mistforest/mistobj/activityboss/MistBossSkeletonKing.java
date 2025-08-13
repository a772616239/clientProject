package model.mistforest.mistobj.activityboss;

import cfg.CrossConstConfig;
import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import common.GameConst;
import model.mistforest.MistConst.MistActivityBossStage;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import util.TimeUtil;

public class MistBossSkeletonKing extends MistActivityBoss {
    protected long createMonsterTime;

    protected MistBornPosController posController = new MistBornPosController();

    public MistBossSkeletonKing(MistRoom room, int objType) {
        super(room, objType);
    }

    public MistBornPosController getPosController() {
        return posController;
    }

    @Override
    public void clear() {
        super.clear();
        posController.clear();
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        int complxBornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        MistComboBornPosConfigObject cfg = MistComboBornPosConfig.getById(complxBornPosId);
        if (cfg != null) {
            posController.init(cfg.getSlaveobjposlist());
        }
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (getAttribute(MistUnitPropTypeEnum.MUPT_ActivityBossStage_VALUE) == MistActivityBossStage.furyStage) {
            return;
        }
        super.beTouch(fighter);
    }

    protected void generateSkeletonMonster(boolean canReborn) {
        int maxCount = Math.min(posController.getEmptyPosCount() + posController.getUsePosCount(), CrossConstConfig.getById(GameConst.ConfigId).getMaxskeletonmonstercount());
        int count = Math.min(maxCount, CrossConstConfig.getById(GameConst.ConfigId).getCreateskeletonmonstercount());
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
            MistSkeletonMonster monster = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_SkeletonMonster_VALUE);
            monster.initByMaster(this);
            monster.afterInit(new int[]{pos.getX(), pos.getY()}, null);
            if (!canReborn) {
                monster.setRebornTime(0);
            }

            addSlaveObj(monster.getId());
            getRoom().getWorldMap().objFirstEnter(monster);
        }
    }

    protected void changeSlaveToFuryStage() {
        if (CollectionUtils.isEmpty(slaveObjList)) {
            return;
        }
        MistObject obj;
        for (long  slaveId : slaveObjList) {
            obj = getRoom().getObjManager().getMistObj(slaveId);
            if (null == obj || !obj.isAlive()) {
                continue;
            }
            long rebornTime = obj.getAttribute(MistUnitPropTypeEnum.MUPT_RebornTime_VALUE);
            if (obj.getRebornTime() <= 0) {
                obj.setRebornTime((int) rebornTime);

            }

        }
    }

    protected void changeSlaveToWeakStage() {
        if (CollectionUtils.isEmpty(slaveObjList)) {
            return;
        }
        MistObject obj;
        for (long  slaveId : slaveObjList) {
            obj = getRoom().getObjManager().getMistObj(slaveId);
            if (null == obj) {
                continue;
            }
            obj.setRebornTime(0);
            if (obj.getAttribute(MistUnitPropTypeEnum.MUPT_NotRemoveWhenDead_VALUE) > 0) {
                obj.removeObjFromMap();
            }
        }
    }

    @Override
    public void changeToStage(int stage) {
        super.changeToStage(stage);
        if (stage == MistActivityBossStage.furyStage) {
            changeSlaveToFuryStage();
            generateSkeletonMonster(true);
        } else if (stage ==MistActivityBossStage.weakStage) {
            changeSlaveToWeakStage();
        }
    }

    @Override
    protected void onBossInitStage(long curTime) {
        super.onBossInitStage(curTime);
        if (createMonsterTime < curTime) {
            generateSkeletonMonster(false);
            createMonsterTime = curTime + CrossConstConfig.getById(GameConst.ConfigId).getCreateskeletonmonsterinterval() * TimeUtil.MS_IN_A_S;
        }
    }

    @Override
    protected void onBossFuryStage(long curTime) {
        super.onBossFuryStage(curTime);
        if (createMonsterTime < curTime) {
            generateSkeletonMonster(true);
            createMonsterTime = curTime + TimeUtil.MS_IN_A_MIN;
        }
        boolean allDead = true;
        if (!CollectionUtils.isEmpty(slaveObjList)) {
            MistObject obj;
            for (Long objId : slaveObjList) {
                obj = getRoom().getObjManager().getMistObj(objId);
                if (null != obj && obj.isAlive()) {
                    allDead = false;
                    break;
                }
            }
        }
        if (allDead) {
            clearSlaveObj();
            changeToStage(MistActivityBossStage.weakStage);
            posController.resetEmptyPos();
        }
    }

}
