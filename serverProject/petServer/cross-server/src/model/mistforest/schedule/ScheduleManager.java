package model.mistforest.schedule;

import cfg.MistScheduleConfig;
import cfg.MistScheduleConfigObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.MistScheduleTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.SC_UpdateScheduleInfo;

public class ScheduleManager {
    protected MistRoom room;
    protected List<BaseSchedule> scheduleList;

    public ScheduleManager(MistRoom room) {
        this.room = room;
        initSchedule(room);
    }

    protected void initSchedule(MistRoom room) {
        if (room.getMistRule() != EnumMistRuleKind.EMRK_Common_VALUE) {
            return;
        }
        scheduleList = new ArrayList<>();
        for (Entry<Integer, MistScheduleConfigObject> entry : MistScheduleConfig._ix_id.entrySet()) {
            if (entry.getValue().getMistlevel() != room.getLevel()) {
                continue;
            }

            BaseSchedule schedule = createByCfg(entry.getValue());
            scheduleList.add(schedule);
        }
    }

    protected BaseSchedule createByCfg(MistScheduleConfigObject cfg) {
        switch (cfg.getScheduletype()) {
            case MistScheduleTypeEnum.MSTE_HotDispute_VALUE: {
                return new HotDisputeSchedule(room, cfg);
            }
            case MistScheduleTypeEnum.MSTE_FateDoor_VALUE: {
                return new FateDoorSchedule(room, cfg);
            }
            default:
                return new BaseSchedule(room, cfg);
        }
    }

    public void clear() {
        for (BaseSchedule schedule : scheduleList) {
            schedule.clear();
        }
        scheduleList.clear();
    }

    public void removeAliveObj(long objId, int objCfgId) {
        for (BaseSchedule schedule : scheduleList) {
            schedule.removeAliveObj(objCfgId);
            schedule.removeRemoveWhenEndObj(objId);
        }
    }

    public int[] getPosFromPosController(int cfgId, int cfgObjId) {
        for (BaseSchedule schedule : scheduleList) {
            if (schedule.getCfgId() == cfgId) {
                return schedule.getPosFromPosController(cfgObjId);
            }
        }
        return null;
    }

    public void returnPosToPosController(int cfgId, int objCfgId, ProtoVector pos) {
        for (BaseSchedule schedule : scheduleList) {
            if (schedule.getCfgId() == cfgId) {
                schedule.returnPosToPosController(objCfgId, pos);
            }
        }
    }

    public boolean isScheduleTypeOpen(int scheduleType) {
        for (BaseSchedule schedule : scheduleList) {
            if (schedule.getScheduleType() == scheduleType) {
                return schedule.isScheduleOpen();
            }
        }
        return false;
    }

    public <T extends BaseSchedule> T getScheduleByType(int scheduleType) {
        for (BaseSchedule schedule : scheduleList) {
            if (schedule.getScheduleType() == scheduleType) {
                return (T) schedule;
            }
        }
        return null;
    }

    public boolean isFighterInClosedSection(int scheduleType, int posX, int posY) {
        for (BaseSchedule schedule : scheduleList) {
            if (schedule.getScheduleType() == scheduleType) {
                return schedule.isInClosedSection(posX, posY);
            }
        }
        return false;
    }

    public void updateAllScheduleData(MistPlayer player) {
        if (CollectionUtils.isEmpty(scheduleList)) {
            return;
        }
        SC_UpdateScheduleInfo.Builder builder = SC_UpdateScheduleInfo.newBuilder();
        for (BaseSchedule schedule : scheduleList) {
            builder.addScheduleData(schedule.buildScheduleData());
        }
        player.sendMsgToServer(MsgIdEnum.SC_UpdateScheduleInfo_VALUE, builder);
    }

    public void onTick(long curTime) {
        if (scheduleList == null) {
            return;
        }
        for (BaseSchedule schedule : scheduleList) {
            schedule.onTick(curTime);
        }
    }
}
