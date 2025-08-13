package model.mistforest.mistobj;

import java.util.HashMap;
import java.util.Map;
import model.mistforest.mistobj.rewardobj.MistDecipheringBox;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.UnitMetadata;

public class MistDecipheringColumn extends MistObject {
    protected Map<Long, Boolean> fighterDecipherMap;

    public MistDecipheringColumn(MistRoom room, int objType) {
        super(room, objType);
        fighterDecipherMap = new HashMap<>();
    }

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_DecipheringColumnState_VALUE
                || propType == MistUnitPropTypeEnum.MUPT_DecipheringColumnSwitch_VALUE;
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata.Builder metaData = UnitMetadata.newBuilder();
        metaData.mergeFrom(super.getMetaData(fighter));
        if (fighter != null) {
            MistDecipheringBox masterBox = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
            if (masterBox == null || !masterBox.canListColumn(fighter)) {
                metaData.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_DecipheringColumnState_VALUE)
                        .addValues(1l); //关
            } else {
                metaData.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_DecipheringColumnState_VALUE)
                        .addValues(0l); //开
            }
            long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
            Boolean fighterRet = fighterDecipherMap.get(playerId);
            if (fighterRet != null) {
                metaData.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_DecipheringColumnSwitch_VALUE)
                        .addValues(fighterRet ? 1l : 0l);
                return metaData.build();
            }
        }
        metaData.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_DecipheringColumnSwitch_VALUE)
                .addValues(getAttribute(MistUnitPropTypeEnum.MUPT_DecipheringColumnSwitch_VALUE));
        return metaData.build();
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        MistDecipheringBox masterBox = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
        if (masterBox == null) {
            return;
        }
        if (!masterBox.canListColumn(fighter)) {
            return;
        }
        lightTheColumn(fighter);
        masterBox.lightAroundColumn(fighter, this);
    }

//    public RetCodeEnum lightDecipherColumn(MistFighter fighter) {
//        if (!isAlive()) {
//            return RetCodeEnum.RCE_MistForest_CanNotTouch;
//        }
//        MistDecipheringBox masterBox = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
//        if (masterBox == null) {
//            return RetCodeEnum.RCE_MistForest_NotFoundSealBox; // 宝箱状态错误
//        }
//        if (!masterBox.canListColumn(fighter)) {
//            return RetCodeEnum.RCE_MistForest_NotFoundSealBox; // 宝箱状态错误
//        }
//        lightTheColumn(fighter);
//        masterBox.lightAroundColumn(fighter, this);
//        return RetCodeEnum.RCE_Success;
//    }

    public void lightTheColumn(MistFighter fighter) {
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        boolean lightingState = getLightingState(fighter);
        fighterDecipherMap.put(playerId, !lightingState);
        addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_DecipheringColumnSwitch_VALUE, lightingState ? 0l : 1l);
    }

    public boolean getLightingState(MistFighter fighter) {
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        Boolean lightingState = fighterDecipherMap.get(playerId);
        if (lightingState == null) {
            lightingState = getAttribute(MistUnitPropTypeEnum.MUPT_DecipheringColumnSwitch_VALUE) > 0 ? true : false;
        }
        return lightingState;
    }
}
