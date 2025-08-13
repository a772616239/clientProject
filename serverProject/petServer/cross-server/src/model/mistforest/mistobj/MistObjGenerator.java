package model.mistforest.mistobj;

import cfg.MistActivityBossConfig;
import cfg.MistActivityBossConfigObject;
import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import cfg.MistDailyObjConfig;
import cfg.MistDailyObjConfigObject;
import cfg.MistMapObjConfig;
import cfg.MistMapObjConfigObject;
import cfg.MistNewObjConfig;
import cfg.MistNewObjConfigObject;
import cfg.MistRebornChangeProp;
import cfg.MistTimeLimitActivity;
import cfg.MistTimeLimitActivityObject;
import com.sun.org.apache.regexp.internal.RE;
import common.GlobalTick;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.mistforest.MistConst;
import model.mistforest.mistobj.activityboss.MistActivityBoss;
import model.mistforest.mistobj.activityboss.MistBornPosController;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.UnitMetadata;
import protocol.TransServerCommon.MistBornObjInfo;
import protocol.TransServerCommon.MistBornObjMetaData;
import protocol.TransServerCommon.MistBornPosInfo;
import util.LogUtil;
import util.TimeUtil;

public class MistObjGenerator {
    private MistRoom room;
    private List<MistBornObjInfo> initObjList; // 初始对象

    private List<UnitMetadata> initMetaData;
    private Set<Long> overallObjData;

    private List<Long> dailyMonsterList;

    private List<MistBornPosInfo> safeRegionBornPosList; // 玩家安全区出生点
    private Map<Integer, List<MistBornPosInfo>> outdoorBornPosList; // 野外各类型未被占用的出生点
    private Map<Integer, Map<Integer, MistBornPosInfo>> usedBornPosList; // 野外各类型已被占用的出生点

    private Map<Integer, List<Integer>> complxBornPosInfo; // 复杂出生点各类型未被占用的出生点
    private Map<Integer, List<Integer>> usedComplxBornPosInfo; // 复杂出生点各类型已被占用的出生点

    // 每日额外刷新对象列表<CfgId, TimeStamp>
    private Map<Integer, Long> dailyObjMap;
    // 每日额外刷新对象类型已存在对象数量<objType, objCount>
    private Map<Integer, Integer> dailyObjCountMap;

    private long updateBossActivityTime;

    private int bossObjCfgId;
    private int bossActivityId;
    private long bossObjId;

    public MistObjGenerator(MistRoom room) {
        this.room = room;
        initObjList = new LinkedList<>();
        initMetaData = new LinkedList<>();
        overallObjData = new HashSet<>();
        safeRegionBornPosList = new ArrayList<>();
        outdoorBornPosList = new HashMap<>();
        usedBornPosList = new HashMap<>();
        dailyObjMap = new HashMap<>();
        dailyObjCountMap = new HashMap<>();
        dailyMonsterList = new ArrayList<>();
        complxBornPosInfo = new HashMap<>();
        usedComplxBornPosInfo = new HashMap<>();
    }

    public List<UnitMetadata> getInitMetaData() {
        return initMetaData;
    }

    public List<UnitMetadata> getOverallObjMetaData(MistFighter fighter) {
        if (overallObjData.isEmpty()) {
            return Collections.emptyList();
        }
        MistObject obj;
        List<UnitMetadata> metadataList = new ArrayList<>();
        for (Long id : overallObjData) {
            obj = room.getObjManager().getMistObj(id);
            if (null == obj) {
                continue;
            }
            metadataList.add(obj.getMetaData(fighter));
        }
        return metadataList;
    }

    public int getBossObjCfgId() {
        return bossObjCfgId;
    }

    public void setBossObjCfgId(int bossObjCfgId) {
        this.bossObjCfgId = bossObjCfgId;
    }

    public void init() {
        initRoomBornPos();
        initRoomComplxBornPos();
        initRoomObj();
        initDailyObj();
        initBossActivityData();
    }

    public void clear() {
        initObjList.clear();
        initMetaData.clear();
        overallObjData.clear();
        safeRegionBornPosList.clear();
        outdoorBornPosList.clear();
        usedBornPosList.clear();
        dailyObjMap.clear();
        dailyObjCountMap.clear();
        dailyMonsterList.clear();
        usedComplxBornPosInfo.clear();
        updateBossActivityTime = 0;
        bossObjId = 0;
        bossObjCfgId = 0;
        bossActivityId = 0;
    }

    public void initRoomBornPos() {
        List<MistMapObjConfigObject> objList = MistMapObjConfig.getInstance().getBornPosByMaplevel(room.getMistRule(), room.getLevel());
        if (objList == null) {
            LogUtil.error("Init mist room bornPos error,cfg is null,level=" + room.getLevel());
            return;
        }
        int id = 1;
        for (MistMapObjConfigObject objCfg : objList) {
            if (objCfg.getObjtype() != MistUnitTypeEnum.MUT_PosObj_VALUE || objCfg.getInitprop() == null) {
                continue;
            }
            Long bornObjType = objCfg.getInitprop().get(MistUnitPropTypeEnum.MUPT_TypeOfBornPos_VALUE);
            if (bornObjType == null || bornObjType < 0) {
                continue;
            }
            int[] initPos = objCfg.getInitpos();
            if (initPos == null || initPos.length < 2) {
                continue;
            }
            if (!room.getWorldMap().isPosValid(initPos[0], initPos[1])) {
                continue;
            }
            int bornForObjType = (int) ((long) bornObjType);
            MistBornPosInfo.Builder builder = MistBornPosInfo.newBuilder();
            builder.setId(++id);
            builder.getPosBuilder().setX(initPos[0]).setY(initPos[1]);
            if (bornForObjType == MistUnitTypeEnum.MUT_Player_VALUE && room.getWorldMap().isInSafeRegion(initPos[0], initPos[1])) {
                safeRegionBornPosList.add(builder.build());
            } else {
                List<MistBornPosInfo> posList = outdoorBornPosList.get(bornForObjType);
                if (posList == null) {
                    posList = new LinkedList<>();
                }
                posList.add(builder.build());
                outdoorBornPosList.put(bornForObjType, posList);
            }

            Long isOverallObj = objCfg.getInitprop().get(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE);
            if (isOverallObj != null && isOverallObj > 0) {
                MistObject newObj = room.getObjManager().createObj(objCfg.getObjtype());
                if (initPos != null && initPos.length > 1) {
                    newObj.setInitPos(initPos[0], initPos[1]);
                    newObj.setPos(initPos[0], initPos[1]);
                }
                newObj.getAttributes().putAll(objCfg.getInitprop());
                addOverallObjId(newObj.getId());
            }
        }
    }

    public void initRoomComplxBornPos() {
        List<MistComboBornPosConfigObject> cfgList = MistComboBornPosConfig.getInstance().getCfgListByLevel(room.getLevel());
        if (CollectionUtils.isEmpty(cfgList)) {
            return;
        }
        for (MistComboBornPosConfigObject cfg : cfgList) {
            List<Integer> posList = complxBornPosInfo.get(cfg.getObjtype());
            if (null == posList) {
                posList = new ArrayList<>();
                complxBornPosInfo.put(cfg.getObjtype(), posList);
            }
            posList.add(cfg.getId());
        }
    }

    public void initRoomObj() {
        List<MistMapObjConfigObject> objList = MistMapObjConfig.getInstance().getMapObjByMaplevel(room.getMistRule(), room.getLevel());
        if (objList == null) {
            LogUtil.error("Init mist room error,cfg is null,level=" + room.getLevel() + ",rule=" + room.getMistRule());
            return;
        }
        for (MistMapObjConfigObject objCfg : objList) {
            if (objCfg.getObjtype() == MistUnitTypeEnum.MUT_PosObj_VALUE) {
                continue;
            }
            int[] initPos = objCfg.getInitpos();
            int[] initToward = objCfg.getInittoward();
            if (objCfg.getDelayborntime() > 0) {
                MistBornObjInfo.Builder builder = MistBornObjInfo.newBuilder();
                builder.setDelayBornTime(objCfg.getDelayborntime());
                MistBornObjMetaData.Builder builder1 = MistBornObjMetaData.newBuilder();
                builder1.setTypeValue(objCfg.getObjtype());
                if (initPos != null && initPos.length > 1) {
                    builder1.setPos(ProtoVector.newBuilder().setX(initPos[0]).setY(initPos[1]));
                }
                if (initToward != null && initToward.length > 1) {
                    builder1.setToward(ProtoVector.newBuilder().setX(initToward[0]).setY(initToward[1]));
                }
                builder1.putAllPropMap(objCfg.getInitprop());
                if (objCfg.getRebornpropchangeinfo() != null) {
                    for (int prop : objCfg.getRebornpropchangeinfo()) {
                        builder1.addRebornChangeProps(prop);
                    }
                }
                builder.addBornObjs(builder1);
                initObjList.add(builder.build());
            } else {
                MistObject newObj = room.getObjManager().createObj(objCfg.getObjtype());
                newObj.getAttributes().putAll(objCfg.getInitprop());
                if (objCfg.getRebornpropchangeinfo() != null) {
                    newObj.addAllRebornChangeProps(objCfg.getRebornpropchangeinfo());
                }
                newObj.afterInit(initPos, initToward);
                if (newObj.getType() == MistUnitTypeEnum.MUT_Building_VALUE) {
                    initMetaData.add(newObj.getMetaData(null));
                } else if (newObj.getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0) {
                    addOverallObjId(newObj.getId());
                } else {
                    room.getWorldMap().objFirstEnter(newObj);
                }
            }
        }
        if (initObjList != null && !initObjList.isEmpty()) {
            initObjList.sort(Comparator.comparingInt(MistBornObjInfo::getDelayBornTime));
        }
    }

    public void delayBornObj(long curTime) {
        if (initObjList == null || initObjList.isEmpty()) {
            return;
        }
        MistBornObjInfo objInfo = initObjList.get(0);
        if (objInfo == null || objInfo.getDelayBornTime() <= 0 || objInfo.getBornObjsCount() <= 0) {
            initObjList.remove(0);
            return;
        }
        if (curTime - room.getCreateTime() > objInfo.getDelayBornTime() * TimeUtil.MS_IN_A_S) {
            SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
            for (MistBornObjMetaData metaData : objInfo.getBornObjsList()) {
                MistObject newObj = room.getObjManager().createObj(metaData.getTypeValue());
                newObj.getAttributes().putAll(metaData.getPropMapMap());
                newObj.addAllRebornChangeProps(metaData.getRebornChangePropsList());
                newObj.afterInit(new int[]{metaData.getPos().getX(), metaData.getPos().getY()}, new int[]{metaData.getToward().getX(), metaData.getToward().getY()});
                LogUtil.debug("room delay create obj,id=" + newObj.getId() + ",type=" + newObj.getType() + ",bornTime=" + objInfo.getDelayBornTime());
                if (newObj.getType() == MistUnitTypeEnum.MUT_Building_VALUE) {
                    initMetaData.add(newObj.getMetaData(null));
                    builder.addCMDList(newObj.buildCreateObjCmd());
                } else if (newObj.getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0) {
                    addOverallObjId(newObj.getId());
                    builder.addCMDList(newObj.buildCreateObjCmd());
                } else {
                    room.getWorldMap().objFirstEnter(newObj);
                }
            }
            if (builder.getCMDListCount() > 0) {
                room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, builder, true);
            }
            initObjList.remove(0);
        }
    }

    public void initDailyObj() {
        if (room.getLevel() >= 1000) { // 排除新手引导地图
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        List<MistDailyObjConfigObject> cfgList = MistDailyObjConfig.getAllConfig(room.getLevel());
        if (cfgList == null) {
            return;
        }
        for (MistDailyObjConfigObject cfg : cfgList) {
            if (cfg.getCreatetimedata().isEmpty()) {
                continue;
            }
            if (cfg.getMaprule() != room.getMistRule()) {
                continue;
            }
            long nextCreateTime = cfg.generateNextCreateTime(curTime);
            if (nextCreateTime <= 0) {
                continue;
            }
            dailyObjMap.put(cfg.getId(), nextCreateTime);
        }
    }

    protected void generateBossObj(MistActivityBossConfigObject cfg, long deadTimeStamp) {
        MistActivityBoss activityBoss = room.getObjManager().createObj(cfg.getBossunittype());
        if (null == activityBoss) {
            return;
        }
        if (null != cfg.getActivtiybossindividualprops() && cfg.getActivtiybossindividualprops().length > 0) {
            for (int i = 0; i < cfg.getActivtiybossindividualprops().length; i++) {
                activityBoss.setAttribute(cfg.getActivtiybossindividualprops()[i][0],cfg.getActivtiybossindividualprops()[i][1]);
            }
        }
        activityBoss.setDeadTimeStamp(deadTimeStamp);
        activityBoss.afterInit(null, null);
        bossObjId = activityBoss.getId();
        if (activityBoss.getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0) {
            SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
            addOverallObjId(activityBoss.getId());
            builder.addCMDList(activityBoss.buildCreateObjCmd());
            room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, builder, true);
        } else {
            room.getWorldMap().objFirstEnter(activityBoss);
        }
    }

    protected void initBossActivityData() {
        if (room.getLevel() >= 1000) { // 排除新手引导地图
            return;
        }
        MistTimeLimitActivityObject activityCfg = MistConst.getNextMistOpenActivityCfg();
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (null == activityCfg) {
            return;
        }
        bossActivityId = activityCfg.getId();
        if (activityCfg.getStarttime() <= curTime && activityCfg.getEndtime() > curTime) {
            bossObjCfgId = MistConst.getMistActivityBossCfgId(activityCfg, room.getLevel());
            MistActivityBossConfigObject cfg = MistActivityBossConfig.getById(bossObjCfgId);
            if (null == cfg) {
                return;
            }
            generateBossObj(cfg, activityCfg.getEndtime());
        }
    }

    public void activityBossSettleWhenRoomClear() {
        MistTimeLimitActivityObject activityCfg = MistTimeLimitActivity.getById(bossActivityId);
        if (null == activityCfg) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (activityCfg.getStarttime() <= curTime && activityCfg.getEndtime() > curTime) {
            room.settleActivityRankData();
        }
    }

    protected void updateBossActivity(long curTime) {
        if (updateBossActivityTime > curTime) {
            return;
        }
        MistTimeLimitActivityObject activityCfg = MistTimeLimitActivity.getById(bossActivityId);
        if (null == activityCfg) {
            activityCfg = MistConst.getNextMistOpenActivityCfg();
            if (null != activityCfg) {
                bossObjCfgId = activityCfg.getId();
            } else {
                updateBossActivityTime = curTime + TimeUtil.MS_IN_A_MIN;
            }
        } else {
            if (activityCfg.getStarttime() > curTime) {
                updateBossActivityTime = activityCfg.getStarttime();
            } else if (activityCfg.getEndtime() > curTime) {
                if (bossObjCfgId <= 0) {
                    bossObjCfgId = MistConst.getMistActivityBossCfgId(activityCfg, room.getLevel());
                    MistActivityBossConfigObject cfg = MistActivityBossConfig.getById(bossObjCfgId);
                    if (null == cfg) {
                        updateBossActivityTime = curTime + TimeUtil.MS_IN_A_S;
                        return;
                    }
                    generateBossObj(cfg, activityCfg.getEndtime());
                    updateBossActivityTime = activityCfg.getEndtime();
                }
            } else {
                MistActivityBoss activityBoss = room.getObjManager().getMistObj(bossObjId);
                if (null != activityBoss) {
                    activityBoss.disappear();
                }
                bossObjId = 0;
                bossObjCfgId = 0;
                room.settleActivityRankData();
                activityCfg = MistConst.getNextMistOpenActivityCfg();
                if (null == activityCfg) {
                    bossActivityId = 0;
                } else {
                    bossActivityId = activityCfg.getId();
                    updateBossActivityTime = activityCfg.getStarttime();
                }
            }
        }
    }

    public void dailyBornObj(long curTime) {
        HashMap<Integer, Long> tmpMap = null;
        for (Map.Entry<Integer, Long> entry : dailyObjMap.entrySet()) {
            MistDailyObjConfigObject cfg = MistDailyObjConfig.getById(entry.getKey());
            if (cfg == null) {
                continue;
            }
            if (entry.getValue() > curTime) {
                continue;
            }
            if (tmpMap == null) {
                tmpMap = new HashMap<>();
            }
            long nextCreateTime = cfg.generateNextCreateTime(curTime);
            if (nextCreateTime > 0) {
                tmpMap.put(entry.getKey(), nextCreateTime);
            }
            int maxDailyObjCount = cfg.getCurMaxDailyObjCount(curTime);
            int objCount = dailyObjCountMap.containsKey(cfg.getObjtype()) ? dailyObjCountMap.get(cfg.getObjtype()) : 0;
            MistObject mistObj;
            if (objCount < maxDailyObjCount) {
                SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
                while (objCount < maxDailyObjCount) {
                    mistObj = room.getObjManager().createObj(cfg.getObjtype());
                    mistObj.addAttributes(cfg.getInitprop());
                    mistObj.setDailyObj(true);
                    initNewObjRandProp(mistObj, cfg.getInitRandProp());
                    mistObj.afterInit(null, null);
                    LogUtil.info("room[" + room.getIdx() + "] daily create obj,id=" + mistObj.getId() + ",type="
                            + mistObj.getType() + ",curDailyObjCount=" + objCount);

                    if (mistObj.getType() == MistUnitTypeEnum.MUT_Building_VALUE) {
                        initMetaData.add(mistObj.getMetaData(null));
                        builder.addCMDList(mistObj.buildCreateObjCmd());
                    } else if (mistObj.getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0) {
                        addOverallObjId(mistObj.getId());
                        builder.addCMDList(mistObj.buildCreateObjCmd());
                    } else {
                        room.getWorldMap().objFirstEnter(mistObj);
                    }
                    ++objCount;
                    if (mistObj.getType() == MistUnitTypeEnum.MUT_Monster_VALUE) {
                        dailyMonsterList.add(mistObj.getId());
                    }
                }
                if (builder.getCMDListCount() > 0) {
                    room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, builder, true);
                }
                dailyObjCountMap.put(cfg.getObjtype(), objCount);
            } else if (cfg.getObjtype() == MistUnitTypeEnum.MUT_Monster_VALUE && dailyMonsterList.size() > maxDailyObjCount) {
                int count = dailyMonsterList.size() - maxDailyObjCount;
                MistMonster monster;
                List<Long> removeList = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    long objId = dailyMonsterList.get(i);
                    monster = room.getObjManager().getMistObj(objId);
                    if (monster == null || !monster.isAlive() || monster.getAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE) > 0) {
                        continue;
                    }
                    monster.forceDead();
                    removeList.add(objId);
                }
                for (Long id : removeList) {
                    dailyMonsterList.remove(id);
                }
            }
        }
        if (tmpMap != null) {
            dailyObjMap.putAll(tmpMap);
        }
    }

    public void addDailyObjCount(int objType) {
        dailyObjCountMap.merge(objType, 1, (oldVal, newVal) -> oldVal + newVal);
    }

    public void decreaseDailyObjCount(int objType) {
        Integer objCount = dailyObjCountMap.get(objType);
        if (objCount == null) {
            return;
        }
        dailyObjCountMap.put(objType, Math.max(0, --objCount));
    }

    public void decreaseDailyMonster(long objId) {
        dailyMonsterList.remove(objId);
    }

    public void initNewObjRandProp(MistObject mistObj, int[] randPropList) {
        if (mistObj == null || randPropList == null || randPropList.length <= 0) {
            return;
        }
        for (Integer cfgId : randPropList) {
            mistObj.updateRandomProp(MistRebornChangeProp.getById(cfgId), false);
        }
    }

    public void removeInitMeta(long id) {
        for (UnitMetadata metaData : initMetaData) {
            if (metaData.getSnapShotData().getUnitId() == id) {
                initMetaData.remove(metaData);
                break;
            }
        }
    }

    public void addOverallObjId(long id) {
        overallObjData.add(id);
    }

    public void removeOverallObjId(long id) {
        if (!overallObjData.contains(id)) {
            return;
        }
        overallObjData.remove(id);
    }

    public MistBornPosInfo getRandomBornPosObj(int objType, boolean toSafeRegion, boolean isPrivateObj) {
        if (objType != MistUnitTypeEnum.MUT_Player_VALUE || !toSafeRegion) {
            return getOutDoorRandomBornPosObj(objType, isPrivateObj);
        }
        if (safeRegionBornPosList.isEmpty()) {
            return null;
        }
        return safeRegionBornPosList.get(RandomUtils.nextInt(safeRegionBornPosList.size()));
    }

    public MistBornPosInfo getOutDoorRandomBornPosObj(int objType, boolean isPrivateObj) {
        if (outdoorBornPosList.isEmpty()) {
            return null;
        }
        List<MistBornPosInfo> posList = outdoorBornPosList.get(objType);
        if (posList == null || posList.isEmpty()) {
            return null;
        }
        MistBornPosInfo bornPos = posList.get(RandomUtils.nextInt(posList.size()));
        if (bornPos != null && !isPrivateObj) {
            Map<Integer, MistBornPosInfo> usedBornMap = usedBornPosList.get(objType);
            if (usedBornMap == null) {
                usedBornMap = new HashMap<>();
                usedBornPosList.put(objType, usedBornMap);
            }
            MistBornPosInfo.Builder usedPos = MistBornPosInfo.newBuilder();
            usedBornMap.put(bornPos.getId(), usedPos.mergeFrom(bornPos).build());
            posList.remove(bornPos);
        }
        return bornPos;
    }

    public void resetUsedOutDoorBornPos(int objType, int bornPosId) {
        if (bornPosId == 0) {
            return;
        }
        Map<Integer, MistBornPosInfo> usedMap = usedBornPosList.get(objType);
        if (usedMap == null) {
            return;
        }
        MistBornPosInfo usedPos = usedMap.get(bornPosId);
        if (usedPos == null) {
            return;
        }
        usedMap.remove(bornPosId);
        List<MistBornPosInfo> emptyList = outdoorBornPosList.get(objType);
        if (emptyList == null) {
            emptyList = new LinkedList<>();
            outdoorBornPosList.put(objType, emptyList);
        }
        emptyList.add(usedPos);
    }

    public MistComboBornPosConfigObject getComplxBornPosData(int objType) {
        if (complxBornPosInfo.isEmpty()) {
            return null;
        }
        List<Integer> posList = complxBornPosInfo.get(objType);
        if (posList == null || posList.isEmpty()) {
            return null;
        }
        int bornPosId = posList.get(RandomUtils.nextInt(posList.size()));
        MistComboBornPosConfigObject cfg = MistComboBornPosConfig.getById(bornPosId);
        if (null != cfg) {
            List<Integer> usedList = usedComplxBornPosInfo.get(objType);
            if (null == usedList) {
                usedList = new ArrayList<>();
                usedComplxBornPosInfo.put(objType, usedList);
            }
            usedList.add(bornPosId);
            posList.removeIf(e -> e.equals(bornPosId));
        }
        return cfg;
    }

    public void resetComplxUsedPosIdData(int objType, int bornPosId) {
        if (bornPosId == 0) {
            return;
        }
        List<Integer> usedPosData = usedComplxBornPosInfo.get(objType);
        if (null == usedPosData) {
            return;
        }
        usedPosData.removeIf(e->e.equals(bornPosId));
        List<Integer> emptyList = complxBornPosInfo.get(objType);
        if (emptyList == null) {
            emptyList = new LinkedList<>();
            complxBornPosInfo.put(objType, emptyList);
        }
        emptyList.add(bornPosId);
    }

    public int getRemainCreateKeyTime() {
        long nextCreateKeyTime = 0;
        for (Map.Entry<Integer, Long> entry : dailyObjMap.entrySet()) {
            MistDailyObjConfigObject objCfg = MistDailyObjConfig.getById(entry.getKey());
            if (objCfg == null) {
                continue;
            }
            if (objCfg.getObjtype() == MistUnitTypeEnum.MUT_Key_VALUE) {
                nextCreateKeyTime = entry.getValue();
                break;
            }
        }
        return Integer.max(0, (int) (nextCreateKeyTime - GlobalTick.getInstance().getCurrentTime()));
    }

    public MistObject generateObjByNewObjCfgId(int cfgId, MistBornPosController posController, Map<Integer, Long> extProp) {
        MistNewObjConfigObject objConfig = MistNewObjConfig.getById(cfgId);
        if (objConfig == null) {
            return null;
        }
        MistObject mistObj = room.getObjManager().createObj(objConfig.getObjtype());
        if (mistObj == null) {
            return null;
        }
        mistObj.addAttributes(objConfig.getInitprop());
        if (objConfig.getInitrand()) {
            initNewObjRandProp(mistObj, objConfig.getRandprop());
        }
        int[] initPos = null;
        if (objConfig.getRandposdata() != null && objConfig.getRandposdata().length > 0) {
            if (posController != null) {
                ProtoVector pos = posController.getAndUseEmptyPos();
                initPos = new int[]{pos.getX(), pos.getY()};
            } else {
                int rand = RandomUtils.nextInt(objConfig.getRandposdata().length);
                if (objConfig.getRandposdata()[rand] != null && objConfig.getRandposdata()[rand].length > 2) {
                    initPos = objConfig.getRandposdata()[rand];
                }
            }
        } else if (objConfig.getInitpos() != null && objConfig.getInitpos().length >= 2) {
            initPos = objConfig.getInitpos();
        }
        if (extProp != null) {
            mistObj.addAttributes(extProp);
        }
        mistObj.afterInit(initPos, null);
        return mistObj;
    }

    public List<MistObject> generateNewObjByInts(int[] objList, MistBornPosController posController, Map<Integer, Long> extProp) {
        if (objList == null) {
            return null;
        }
        List<MistObject> objectList = null;
        for (int i = 0; i < objList.length; i++) {
            MistObject mistObject = generateObjByNewObjCfgId(objList[i], posController, extProp);
            if (mistObject == null) {
                continue;
            }
            if (objectList==null) {
                objectList = new ArrayList<>();
            }
            objectList.add(mistObject);
        }
        return objectList;
    }

    public List<MistObject> generateNewObjByIntArrays(int[][] objList, MistBornPosController posController, Map<Integer, Long> extProp) {
        if (objList == null) {
            return null;
        }
        List<MistObject> objectList = null;
        for (int i = 0; i < objList.length; i++) {
            if (objList[i] == null) {
                continue;
            }
            if (objList[i].length <= 0 || objList[i].length % 2 != 0) {
                continue;
            }
            for (int j = 0; j < objList[i][1]; j++) {
                MistObject mistObject = generateObjByNewObjCfgId(objList[i][0], posController, extProp);
                if (objectList == null) {
                    objectList = new ArrayList<>();
                }
                objectList.add(mistObject);
            }
        }
        return objectList;
    }

    public void onTick(long curTime) {
        delayBornObj(curTime);
        dailyBornObj(curTime);
        if (room.getMistRule() == EnumMistRuleKind.EMRK_Common_VALUE) {
            updateBossActivity(curTime);
        }
    }
}
