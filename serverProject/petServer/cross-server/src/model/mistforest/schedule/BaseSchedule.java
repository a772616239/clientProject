package model.mistforest.schedule;

import cfg.MistScheduleConfigObject;
import cfg.MistScheduleObjConfig;
import cfg.MistScheduleObjConfigObject;
import common.GlobalTick;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import model.mistforest.mistobj.MistObject;
import model.mistforest.mistobj.activityboss.MistBornPosController;
import model.mistforest.room.entity.MistRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistScheduleData;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.SC_UpdateScheduleInfo;
import util.LogUtil;
import util.TimeUtil;

public class BaseSchedule {
    protected MistRoom room;

    protected int scheduleType;
    protected MistScheduleConfigObject cfg;
    protected int scheduleState;

    protected long scheduleStateChangeTime;
    protected long updateTime;

    protected Set<Long> removeWhenEndObjs;
    // <objCfgId, TimeStamp>
    protected Map<Integer, Long> refreshObjData;
    // <objCfgId, objCount>
    protected Map<Integer, Integer> aliveObjCountData;

    protected Map<Integer, MistBornPosController> posControllerMap;

    public BaseSchedule(MistRoom room, MistScheduleConfigObject cfg) {
        this.room = room;
        this.cfg = cfg;
        init();
    }

    public void init() {
        if (cfg == null) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        long todayTimeStamp = TimeUtil.getTodayStamp(curTime);
        long nextDayTimeStamp = todayTimeStamp + TimeUtil.MS_IN_A_DAY;
        long scheduleStartTime = todayTimeStamp + cfg.getDailystarttime() * TimeUtil.MS_IN_A_MIN; // 第一次开始时间
        long scheduleDuration = cfg.getDuration() * TimeUtil.MS_IN_A_MIN;
        long scheduleInterval = cfg.getInterval() * TimeUtil.MS_IN_A_MIN;
        this.scheduleType = cfg.getScheduletype();
        this.posControllerMap = new HashMap<>();
        if (curTime < scheduleStartTime) {
            scheduleStateChangeTime = scheduleStartTime;
            scheduleState = 0;
        } else {
            if (scheduleInterval <= 0) {
                if (curTime > scheduleStartTime + scheduleDuration) {
                    scheduleStateChangeTime = nextDayTimeStamp + scheduleStartTime;
                    scheduleState = 0;
                } else {
                    onScheduleStart(curTime, curTime - scheduleStartTime);
                }
            } else {
                long tmpTime1 = curTime - scheduleStartTime; // 每日第一次开始时间到现在经过的时间
                long tmpTime2 = scheduleDuration + scheduleInterval; // 每日开启时间和间隔时间的和
                long curScheduleTime = tmpTime1 % tmpTime2; // 当前轮次经过的时间(持续时间+间隔时间为1轮)
                if (curScheduleTime > scheduleDuration) {
                    scheduleStateChangeTime = scheduleStartTime + ((tmpTime1 / tmpTime2) + 1) * tmpTime2;
                    if (scheduleStateChangeTime > nextDayTimeStamp) {
                        scheduleStateChangeTime = scheduleStartTime + TimeUtil.MS_IN_A_DAY;
                    }
                    scheduleState = 0;
                } else {
                    onScheduleStart(curTime, curScheduleTime);
                }
            }
        }
    }

    public void clear() {
        if (removeWhenEndObjs != null) {
            removeWhenEndObjs.clear();
        }
        if (refreshObjData != null) {
            refreshObjData.clear();
        }
        if (aliveObjCountData != null) {
            aliveObjCountData.clear();
        }
        if (posControllerMap != null) {
            posControllerMap.clear();
        }
        cfg = null;
    }

    public int getCfgId() {
        return cfg != null ? cfg.getId() : 0;
    }

    public int getScheduleType() {
        return scheduleType;
    }

    public long getScheduleStateChangeTime() {
        return scheduleStateChangeTime;
    }

    public void removeAliveObj(int objCfgId) {
        if (aliveObjCountData == null) {
            return;
        }
        Integer countObj = aliveObjCountData.get(objCfgId);
        if (countObj != null) {
            if (countObj > 1) {
                aliveObjCountData.put(objCfgId, --countObj);
            } else {
                aliveObjCountData.remove(objCfgId);
            }
        }
    }

    public void removeRemoveWhenEndObj(long objId) {
        if (removeWhenEndObjs == null) {
            return;
        }
        if (!removeWhenEndObjs.contains(objId)) {
            return;
        }
        removeWhenEndObjs.remove(objId);
    }

    public int[] getPosFromPosController(int objCfgId) {
		if (posControllerMap == null) {
			return null;
		}
        MistBornPosController posController = posControllerMap.get(objCfgId);
        if (posController == null) {
            return null;
        }
        ProtoVector pos = posController.getAndUseEmptyPos();
        if (pos == null) {
            return null;
        }
        return new int[]{pos.getX(), pos.getY()};
    }

    public void returnPosToPosController(int objCfgId, ProtoVector pos) {
		if (posControllerMap == null) {
			return;
		}
        MistBornPosController posController = posControllerMap.get(objCfgId);
        if (posController == null) {
            return;
        }
        posController.returnUsedPos(pos);
    }

    public boolean isScheduleOpen() {
        return scheduleState == 1;
    }

    public boolean isInClosedSection(int posX, int posY) {
        if (cfg == null) {
            return false;
        }
        if (!isScheduleOpen()) {
            return false;
        }
        if (cfg.getClosedsection() == null || cfg.getClosedsection().length < 2) {
            return false;
        }
        if (cfg.getClosedsection()[0] == null || cfg.getClosedsection()[0].length < 2) {
            return false;
        }
        if (cfg.getClosedsection()[1] == null || cfg.getClosedsection()[1].length < 2) {
            return false;
        }
        if (cfg.getClosedsection()[0][0] > posX || cfg.getClosedsection()[0][1] > posY) {
            return false;
        }
        if (cfg.getClosedsection()[1][0] < posX || cfg.getClosedsection()[1][1] < posY) {
            return false;
        }
        return true;
    }

    public MistScheduleData.Builder buildScheduleData() {
        MistScheduleData.Builder scheduleData = MistScheduleData.newBuilder();
        scheduleData.setCfgId(cfg.getId());
        scheduleData.setIsOpen(isScheduleOpen());
        scheduleData.setNextUpdateTime(getScheduleStateChangeTime());
        return scheduleData;
    }

    public void updateScheduleTime() {
        if (cfg == null) {
            return;
        }
        SC_UpdateScheduleInfo.Builder builder = SC_UpdateScheduleInfo.newBuilder();
        builder.addScheduleData(buildScheduleData());
        room.broadcastMsg(MsgIdEnum.SC_UpdateScheduleInfo_VALUE, builder, true);
    }

    public void onScheduleStart(long curTime, long deltaTime) {
        if (cfg == null) {
            return;
        }
        if (cfg.getInitobjdata() != null) {
            if (posControllerMap == null) {
                posControllerMap = new HashMap<>();
            }
            SC_BattleCmd.Builder builder = null;
            for (int i = 0; i < cfg.getInitobjdata().length; i++) {
                MistScheduleObjConfigObject objConfig = MistScheduleObjConfig.getById(cfg.getInitobjdata()[i]);
                if (objConfig == null) {
                    continue;
                }
                if (objConfig.getRefreshinterval() > 0) {
                    if (refreshObjData == null) {
                        refreshObjData = new HashMap<>();
                    }
                    refreshObjData.put(objConfig.getId(), curTime + objConfig.getRefreshinterval() * TimeUtil.MS_IN_A_S);
                }
                if (objConfig.getInitcount() <= 0 || objConfig.getMaxcount() <= 0) {
                    continue;
                }
                Integer objCountInteger = aliveObjCountData != null ? aliveObjCountData.get(objConfig.getId()) : 0;
                int objCount = objCountInteger != null ? objCountInteger : 0;
                if (objCount > objConfig.getInitcount()) {
                    continue;
                }
                MistBornPosController posController = new MistBornPosController();
                posController.init(objConfig.getRandposdata());
                for (int j = 0; j < objConfig.getInitcount() - objCount; j++) {
                    MistObject mistObj = room.getObjManager().createObj(objConfig.getObjtype());
                    mistObj.addAttributes(objConfig.getInitprop());
                    room.getObjGenerator().initNewObjRandProp(mistObj, objConfig.getRandprop());
                    ProtoVector pos = posController.getAndUseEmptyPos();
                    int[] posArray = pos != null ? new int[]{pos.getX(), pos.getY()} : null;
                    mistObj.afterInit(posArray, null);
                    mistObj.setScheduleCfgId(getCfgId());
                    mistObj.setScheduleObjCfgId(objConfig.getId());

                    if (mistObj.getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0) {
                        room.getObjGenerator().addOverallObjId(mistObj.getId());
                        if (builder == null) {
                            builder = SC_BattleCmd.newBuilder();
                        }
                        builder.addCMDList(mistObj.buildCreateObjCmd());
                    } else {
                        room.getWorldMap().objFirstEnter(mistObj);
                    }
                    if (aliveObjCountData == null) {
                        aliveObjCountData = new HashMap<>();
                    }
                    aliveObjCountData.merge(objConfig.getId(), 1, (oldVal, newVal) -> oldVal + newVal);

                    if (objConfig.getRemovewhenscheduleend()) {
                        if (removeWhenEndObjs == null) {
                            removeWhenEndObjs = new HashSet<>();
                        }
                        removeWhenEndObjs.add(mistObj.getId());
                    }
                }
                posControllerMap.put(objConfig.getId(), posController);
            }
            if (builder != null) {
                room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, builder, true);
            }
        }
        scheduleState = 1;
        long nextDayTimeStamp = TimeUtil.getNextDayStamp(curTime);
        long nextScheduleTime = curTime + cfg.getDuration() * TimeUtil.MS_IN_A_MIN - deltaTime;
        scheduleStateChangeTime = Math.min(nextDayTimeStamp, nextScheduleTime);

        // 更新放最后
        updateScheduleTime();
    }

    public void onScheduleEnd(long curTime) {
        if (cfg == null) {
            return;
        }
        if (removeWhenEndObjs != null) {
            MistObject mistObj;
            for (Long objId : removeWhenEndObjs) {
                mistObj = room.getObjManager().getMistObj(objId);
                if (mistObj == null || !mistObj.isAlive()) {
                    continue;
                }
                if (mistObj.isAlive()) {
                    mistObj.dead();
                }
                mistObj.setRebornTime(0);
            }
            removeWhenEndObjs.clear();
        }
        scheduleState = 0;
        long nextDayTimeStamp = TimeUtil.getNextDayStamp(curTime);
        long nextScheduleTime = curTime + cfg.getInterval() * TimeUtil.MS_IN_A_MIN;
        if (nextDayTimeStamp <= nextScheduleTime) {
            scheduleStateChangeTime = nextDayTimeStamp + cfg.getDailystarttime() * TimeUtil.MS_IN_A_MIN;
        } else {
            scheduleStateChangeTime = nextScheduleTime;
        }

        // 更新放最后
        updateScheduleTime();
    }

    public void refreshObj(long curTime) {
        if (cfg == null) {
            return;
        }
        if (refreshObjData == null || refreshObjData.isEmpty()) {
            return;
        }
        MistScheduleObjConfigObject objConfig;
        SC_BattleCmd.Builder builder = null;
        for (Entry<Integer, Long> entry : refreshObjData.entrySet()) {
            objConfig = MistScheduleObjConfig.getById(entry.getKey());
            if (objConfig == null || objConfig.getRefreshinterval() <= 0) {
                continue;
            }
            if (entry.getValue() < curTime) {
                continue;
            }
            refreshObjData.put(objConfig.getId(), curTime + objConfig.getRefreshinterval() * TimeUtil.MS_IN_A_S);
            Integer objCountInteger = aliveObjCountData.get(objConfig.getId());
            int objCount = objCountInteger != null ? objCountInteger : 0;
            if (objConfig.getMaxcount() <= objCount) {
                continue;
            }
            MistBornPosController posController = posControllerMap.get(objConfig.getId());
            ProtoVector pos = null;
            if (posController != null) {
                pos = posController.getAndUseEmptyPos();
            }
            for (int i = 0; i < objConfig.getMaxcount() - objCount; i++) {
                MistObject mistObj = room.getObjManager().createObj(objConfig.getObjtype());
                mistObj.addAttributes(objConfig.getInitprop());

                room.getObjGenerator().initNewObjRandProp(mistObj, objConfig.getRandprop());
                int[] initPos = pos != null ? new int[]{pos.getX(), pos.getY()} : null;
                mistObj.afterInit(initPos, null);
                mistObj.setScheduleCfgId(getCfgId());
                mistObj.setScheduleObjCfgId(objConfig.getId());

                if (mistObj.getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0) {
                    room.getObjGenerator().addOverallObjId(mistObj.getId());
                    if (builder == null) {
                        builder = SC_BattleCmd.newBuilder();
                    }
                    builder.addCMDList(mistObj.buildCreateObjCmd());
                } else {
                    room.getWorldMap().objFirstEnter(mistObj);
                }
                aliveObjCountData.merge(objConfig.getId(), 1, (oldVal, newVal) -> oldVal + newVal);
                if (objConfig.getRemovewhenscheduleend()) {
                    if (removeWhenEndObjs == null) {
                        removeWhenEndObjs = new HashSet<>();
                    }
                    removeWhenEndObjs.add(mistObj.getId());
                }
            }
        }
        if (builder != null) {
            room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, builder, true);
        }
    }

    public void onTick(long curTime) {
        if (updateTime > curTime) {
            return;
        }
        updateTime = curTime + TimeUtil.MS_IN_A_S;
        if (scheduleState == 1) {
            if (scheduleStateChangeTime > 0 && scheduleStateChangeTime <= curTime) {
                onScheduleEnd(curTime);
            } else {
                refreshObj(curTime);
            }
        } else {
            if (scheduleStateChangeTime > 0 && scheduleStateChangeTime <= curTime) {
                onScheduleStart(curTime, 0);
            }
        }
    }
}
