package model.mistforest.mistobj.activityboss;

import model.mistforest.MistConst;
import model.mistforest.mistobj.MistMonster;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;

public class MistSkeletonMonster extends MistMonster {
    public MistSkeletonMonster(MistRoom room, int objType) {
        super(room, objType);
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
    public void dead() {
        super.dead();
        if (getRebornTime() <= 0) {
            MistBossSkeletonKing skeletonKing = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
            if (null != skeletonKing) {
                skeletonKing.getPosController().returnUsedPos(getInitPos().build());
            }
        }
    }

    @Override
    public void removeFromMaster() {
        if (getRebornTime() <= 0) {
            MistBossSkeletonKing obj = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
            if (null != obj) {
                obj.removeSlaveId(getId());
            }
        }
    }
}
