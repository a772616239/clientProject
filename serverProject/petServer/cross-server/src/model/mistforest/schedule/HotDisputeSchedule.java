package model.mistforest.schedule;

import cfg.MistScheduleConfigObject;
import java.util.List;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MistForest.MistUnitPropTypeEnum;

public class HotDisputeSchedule extends BaseSchedule {
    public HotDisputeSchedule(MistRoom room, MistScheduleConfigObject cfg) {
        super(room, cfg);
    }

    @Override
    public void onScheduleEnd(long curTime) {
        super.onScheduleEnd(curTime);
        List<MistFighter> fighterList = room.getObjManager().getAllFighters();
        if (CollectionUtils.isEmpty(fighterList)) {
            return;
        }
        for (MistFighter fighter : fighterList) {
            if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_LavaBadgeCount_VALUE) > 0) {
                fighter.setAttribute(MistUnitPropTypeEnum.MUPT_LavaBadgeCount_VALUE, 0);
                fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_LavaBadgeCount_VALUE, 0);
            }
        }
    }
}
