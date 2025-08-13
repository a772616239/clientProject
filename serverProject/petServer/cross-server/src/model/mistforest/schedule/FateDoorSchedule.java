package model.mistforest.schedule;

import cfg.MistScheduleConfigObject;
import java.util.List;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.TransServerCommon.EnumMistSelfOffPropData;

public class FateDoorSchedule extends BaseSchedule {
    public FateDoorSchedule(MistRoom room, MistScheduleConfigObject cfg) {
        super(room, cfg);
    }

    protected void transPlayerBackToSafeRegion() {
        List<MistFighter> fighterList = room.getObjManager().getAllFighters();
        if (fighterList == null) {
            return;
        }
        for (MistFighter fighter : fighterList) {
            if (!isInClosedSection(fighter.getPos().getX(), fighter.getPos().getY())) {
                continue;
            }
            fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.ScheduleOpen, fighter, null);
        }
    }

    protected void removePlayerDoorIndexData() {
        List<MistFighter> fighterList = room.getObjManager().getAllFighters();
        if (fighterList == null) {
            return;
        }
        MistPlayer player;
        for (MistFighter fighter : fighterList) {
            fighter.setAttribute(MistUnitPropTypeEnum.MUPT_FateDoorIndex_VALUE, 0);
            fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_FateDoorIndex_VALUE, 0);

            player = fighter.getOwnerPlayerInSameRoom();
            if (player == null) {
                continue;
            }

            player.removeOfflineData(MistUnitPropTypeEnum.MUPT_FateDoorIndex, EnumMistSelfOffPropData.EMSOPD_FateDoorExpireTime);
        }
    }

    @Override
    public void onScheduleStart(long curTime, long deltaTime) {
        transPlayerBackToSafeRegion();
        super.onScheduleStart(curTime, deltaTime);
    }

    @Override
    public void onScheduleEnd(long curTime) {
        super.onScheduleEnd(curTime);
        removePlayerDoorIndexData();
    }
}
