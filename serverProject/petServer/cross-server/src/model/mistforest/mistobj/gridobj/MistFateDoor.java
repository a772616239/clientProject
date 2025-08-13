package model.mistforest.mistobj.gridobj;

import cfg.MistFateDoorConfig;
import cfg.MistFateDoorConfigObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.room.entity.MistRoom;
import model.mistforest.schedule.FateDoorSchedule;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistScheduleTypeEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.SC_BattleCmd;
import protocol.TransServerCommon.EnumMistSelfOffPropData;

public class MistFateDoor extends MistGridObj {

    public MistFateDoor(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public boolean isGridBlock(MistFighter fighter) {
        if (!isAlive()) {
            return false;
        }
        if (fighter == null) {
            return false;
        }
        long fighterIndex = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_FateDoorIndex_VALUE);
        long doorIndex = getAttribute(MistUnitPropTypeEnum.MUPT_FateDoorIndex_VALUE);
        return doorIndex != fighterIndex;
    }

    public void openFateDoor(MistFighter fighter) {
        if (!fighter.canBeTouch()) {
            return;
        }
        if (room.getScheduleManager() == null) {
            return;
        }
        FateDoorSchedule schedule = room.getScheduleManager().getScheduleByType(MistScheduleTypeEnum.MSTE_FateDoor_VALUE);
        if (schedule == null || !schedule.isScheduleOpen()) {
            return;
        }
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        long fighterDoorIndex = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_FateDoorIndex_VALUE);
        if (fighterDoorIndex > 0) {
            return;
        }
        long doorIndex = getAttribute(MistUnitPropTypeEnum.MUPT_FateDoorIndex_VALUE);
        if (doorIndex <= 0) {
            return;
        }
        fighter.setAttribute(MistUnitPropTypeEnum.MUPT_FateDoorIndex_VALUE, doorIndex);
        fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_FateDoorIndex_VALUE, doorIndex);

        Map<Integer, Long> offlineProp = new HashMap<>();
        offlineProp.put(MistUnitPropTypeEnum.MUPT_FateDoorIndex_VALUE, doorIndex);

        Map<Integer, Long> selfOffData = new HashMap<>();
        selfOffData.put(EnumMistSelfOffPropData.EMSOPD_FateDoorExpireTime_VALUE, schedule.getScheduleStateChangeTime());
        player.updateOfflineData(offlineProp, selfOffData);
        generateRewardObj(fighter);
    }

    protected void generateRewardObj(MistFighter fighter) {
        MistFateDoorConfigObject cfg = MistFateDoorConfig.getById((int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE));
        if (cfg == null || cfg.getRewardobjlist() == null) {
            return;
        }
        Map<Integer, Long> extProp = new HashMap<>();
        extProp.put(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE, fighter.getId());
        List<MistObject> objList = room.getObjGenerator().generateNewObjByIntArrays(cfg.getRewardobjlist(), null, extProp);
        if (objList == null) {
            return;
        }
        SC_BattleCmd.Builder builder = null;
        for (MistObject mistObj : objList) {
            if (mistObj.getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0) {
                room.getObjGenerator().addOverallObjId(mistObj.getId());
                if (builder == null) {
                    builder = SC_BattleCmd.newBuilder();
                }
                builder.addCMDList(mistObj.buildCreateObjCmd());
            } else if (mistObj.getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE) > 0) {
                mistObj.addCreateObjCmd();
            } else {
                room.getWorldMap().objFirstEnter(mistObj);
            }
        }
        if (builder != null) {
            room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, builder, true);
        }
    }
}
