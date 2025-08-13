package model.mistforest.mistobj.rewardobj;

import common.GlobalTick;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.LifeStateEnum;
import protocol.MistForest.MistUnitPropTypeEnum;

public class MistTreasureBag extends MistRewardObj {
    public MistTreasureBag(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void dead() {
        setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);
        super.dead();
    }

    public void beAbsorbed() {
        setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);
        setAttribute(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Dead_VALUE);
        AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
        if (aoiNode != null) {
            aoiNode.onTreasureBagDead(this);
        }
        int bornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_BornPosId_VALUE);
        room.getObjGenerator().resetUsedOutDoorBornPos(getType(), bornPosId);
        setAttribute(MistUnitPropTypeEnum.MUPT_BornPosId_VALUE, 0);
        setDeadTimeStamp(GlobalTick.getInstance().getCurrentTime());
    }
}
