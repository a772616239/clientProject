package model.mistforest;

import cfg.GameConfig;
import cfg.Mission;
import cfg.MistCommonConfig;
import cfg.MistTimeLimitMission;
import cfg.MistTimeLimitMissionObject;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GameConst.EventType;
import common.tick.GlobalTick;
import common.tick.Tickable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import model.gameplay.GamePlayerUpdate;
import model.gameplay.dbCache.gameplayCache;
import model.gameplay.entity.gameplayEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.GameplayDB.DB_MistTimeLimitMissionCfg;
import protocol.GameplayDB.DB_MistTimeLimitMissionTimeRecord;
import protocol.GameplayDB.GameplayTypeEnum;
import util.ArrayUtil;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2020/08/06
 */
public class MistTimeLimitMissionManager implements Tickable, GamePlayerUpdate {
    private static MistTimeLimitMissionManager instance;

    public static MistTimeLimitMissionManager getInstance() {
        if (instance == null) {
            synchronized (MistTimeLimitMissionManager.class) {
                if (instance == null) {
                    instance = new MistTimeLimitMissionManager();
                }
            }
        }
        return instance;
    }

    private MistTimeLimitMissionManager() {
    }

    private DB_MistTimeLimitMissionTimeRecord.Builder globalCfg;

    private static final int MISSION_START_TIME_INDEX = 0;
    private static final int MISSION_END_TIME_INDEX = 1;
    private static final int MISSION_START_INDEX = 2;
    private static final int CONFIG_LENGTH = 4;

    /**
     * 当前活动是否开启
     */
    private volatile boolean open;

    public boolean init() {
        return checkCfg()
                && loadFormDb()
                && GlobalTick.getInstance().addTick(this)
                && gameplayCache.getInstance().addToUpdateSet(this);
    }

    private boolean loadFormDb() {
        gameplayEntity entity = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.GTE_MistTimeLimitMission);
        if (entity != null && entity.getGameplayinfo() != null) {
            try {
                globalCfg = DB_MistTimeLimitMissionTimeRecord.parseFrom(entity.getGameplayinfo()).toBuilder();
            } catch (InvalidProtocolBufferException e) {
                LogUtil.printStackTrace(e);
            }
        }

        if (globalCfg == null) {
            globalCfg = DB_MistTimeLimitMissionTimeRecord.newBuilder();
        }

        //检查任务
        if (!checkMissionList() || openTimeChanged()) {
            clearAllPlayerMistTimeLimitMissionProgress();
            initNext();
        }
        return true;
    }

    /**
     * 判断配置表时间是否变化
     *
     * @return
     */
    private boolean openTimeChanged() {
        long dbTimeStamp = TimeUtil.getTodayStamp(this.globalCfg.getCurStartTime());
        long dbStartMin = (this.globalCfg.getCurStartTime() - dbTimeStamp) / TimeUtil.MS_IN_A_MIN;
        long dbEndMin = (this.globalCfg.getCurEndTime() - dbTimeStamp) / TimeUtil.MS_IN_A_MIN;

        for (int[] ints : GameConfig.getById(GameConst.CONFIG_ID).getMistforcepvpstarttime()) {
            if (ints.length < CONFIG_LENGTH) {
                continue;
            }
            if (ints[0] != dbStartMin || ints[1] != dbEndMin) {
                return true;
            }
        }

        return false;
    }

    private boolean checkCfg() {
        Map<Integer, Integer> timeScope = new HashMap<>();
        int[][] timeList = GameConfig.getById(GameConst.CONFIG_ID).getMistforcepvpstarttime();
        for (int[] ints : timeList) {
            if (ints.length < CONFIG_LENGTH
                    || ints[MISSION_START_TIME_INDEX] < 0
                    || ints[MISSION_END_TIME_INDEX] > TimeUtil.MIN_IN_A_DAY
                    || ints[MISSION_START_TIME_INDEX] >= ints[MISSION_END_TIME_INDEX]) {
                LogUtil.error("model.mistforest.MistTimeLimitMissionManager.checkCfg, open time cfg error");
                return false;
            }

            //TODO 时间段不能重合
            for (Entry<Integer, Integer> entry : timeScope.entrySet()) {
                if (GameUtil.inScope(entry.getKey(), entry.getValue(), ints[MISSION_START_TIME_INDEX])
                        || GameUtil.inScope(entry.getKey(), entry.getValue(), ints[MISSION_END_TIME_INDEX])) {
                    LogUtil.error("model.mistforest.MistTimeLimitMissionManager.checkCfg, open time has duplicate scope");
                    return false;
                }
            }
            timeScope.put(ints[MISSION_START_TIME_INDEX], ints[MISSION_END_TIME_INDEX]);

            for (int j = MISSION_START_INDEX; j < CONFIG_LENGTH; j++) {
                MistTimeLimitMissionObject mistCfg = MistTimeLimitMission.getById(ints[j]);
                if (mistCfg == null || !checkMissionList(mistCfg.getMissionlist())) {
                    LogUtil.error("model.mistforest.MistTimeLimitMissionManager.checkCfg, mission list cfg error,  id:" + j);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkMissionList() {
        for (DB_MistTimeLimitMissionCfg missionCfg : globalCfg.getMissionCfgList()) {
            if (!checkMissionList(missionCfg.getMissionsList())) {
                return false;
            }
        }
        return true;
    }

    private boolean checkMissionList(int[] missionList) {
        return checkMissionList(ArrayUtil.intArrayToList(missionList));
    }

    private boolean checkMissionList(List<Integer> missionList) {
        if (CollectionUtils.isEmpty(missionList)) {
            LogUtil.error("model.mistforest.MistTimeLimitMissionManager.checkMissionList, mission list is empty");
            return false;
        }

        Set<Integer> checked = new HashSet<>();
        for (int missionId : missionList) {
            if (checked.contains(missionId) || Mission.getById(missionId) == null) {
                LogUtil.error("model.mistforest.MistTimeLimitMissionManager.checkMissionList, mission is not exist, id:" + missionId);
                return false;
            }
            checked.add(missionId);
        }
        return true;
    }


    @Override
    public synchronized void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (currentTime >= this.globalCfg.getCurEndTime()) {
            LogUtil.info("model.mistforest.MistTimeLimitMissionManager.onTick, init next mission");
            initNext();
            clearAllPlayerMistTimeLimitMissionProgress();
        }

        this.open = GameUtil.inScope(this.globalCfg.getCurStartTime(), this.globalCfg.getCurEndTime(), currentTime);
    }

    /**
     * 清空所有玩家的限时任务进度
     */
    private void clearAllPlayerMistTimeLimitMissionProgress() {
        EventUtil.unlockObjEvent(EventType.ET_MIST_CLEAR_ALL_TIME_LIMIT_PROGRESS);
    }

    /**
     * 初始化下一次活动
     */
    private synchronized void initNext() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        long todayStamp = TimeUtil.getTodayStamp(currentTime);
        long curTodayMs = currentTime - todayStamp;

        int[] tmpRet = null;
        int[][] timeList = GameConfig.getById(GameConst.CONFIG_ID).getMistforcepvpstarttime();
        for (int[] ints : timeList) {
            if (ints.length < CONFIG_LENGTH) {
                continue;
            }

            long todayStartTime = ints[MISSION_START_TIME_INDEX] * TimeUtil.MS_IN_A_MIN;
            long todayEndTime = ints[MISSION_END_TIME_INDEX] * TimeUtil.MS_IN_A_MIN;

            if (todayStartTime > curTodayMs
                    || GameUtil.inScope(todayStartTime, todayEndTime, curTodayMs)) {
                tmpRet = ints;
                break;
            }
        }

        //获得时间点最靠前的配置,初始化为第二天
        if (tmpRet == null) {
            tmpRet = getFirstCfg();
            todayStamp = todayStamp + TimeUtil.MS_IN_A_DAY;
        }

        if (tmpRet != null) {
            this.globalCfg.setCurStartTime(todayStamp + TimeUtil.MS_IN_A_MIN * tmpRet[MISSION_START_TIME_INDEX]);
            this.globalCfg.setCurEndTime(todayStamp + TimeUtil.MS_IN_A_MIN * tmpRet[MISSION_END_TIME_INDEX]);
            this.globalCfg.clearMissionCfg();
            for (int i = MISSION_START_INDEX; i < tmpRet.length; i++) {
                MistTimeLimitMissionObject findCfg = MistTimeLimitMission.getById(tmpRet[i]);
                DB_MistTimeLimitMissionCfg.Builder newCfg = DB_MistTimeLimitMissionCfg.newBuilder()
                        .addAllMissions(ArrayUtil.intArrayToList(findCfg.getMissionlist()))
                        .addAllMissionsLimitLv(ArrayUtil.intArrayToList(findCfg.getOpenlevel()));
                this.globalCfg.addMissionCfg(newCfg);
            }
        }
    }

    /**
     * 获取所有配置中时间最靠前的配置
     *
     * @return
     */
    private int[] getFirstCfg() {
        int[] tmpRet = null;
        int[][] timeList = GameConfig.getById(GameConst.CONFIG_ID).getMistforcepvpstarttime();
        for (int[] ints : timeList) {
            if (ints.length < CONFIG_LENGTH) {
                continue;
            }
            if (tmpRet == null
                    || tmpRet[MISSION_START_TIME_INDEX] > ints[MISSION_START_TIME_INDEX]) {
                tmpRet = ints;
            }
        }
        return tmpRet;
    }

    /**
     * 获取下一个mission
     *
     * @param curMissionId
     * @return -1 未找到,  == curMissionId 没有更多任务
     */
    public int getNextMissionId(String playerIdx, int curMissionId) {
        if (StringUtils.isEmpty(playerIdx)) {
            LogUtil.error("model.mistforest.MistTimeLimitMissionManager.getNextMissionId, mission list is empty");
            return -1;
        }

        int mistLv = MistConst.getPlayerBelongMistLv(playerIdx);
        DB_MistTimeLimitMissionCfg targetCfg = null;
        for (DB_MistTimeLimitMissionCfg missionCfg : globalCfg.getMissionCfgList()) {
            if (missionCfg.getMissionsLimitLvList().contains(mistLv)) {
                targetCfg = missionCfg;
                break;
            }
        }

        if (targetCfg == null) {
            return -1;
        }

        if (curMissionId == 0) {
            return targetCfg.getMissions(0);
        }

        for (int i = 0; i < targetCfg.getMissionsCount(); i++) {
            if (Objects.equals(targetCfg.getMissions(i), curMissionId)) {
                int nextIndex = i + 1;
                if (targetCfg.getMissionsCount() > nextIndex) {
                    return targetCfg.getMissions(nextIndex);
                }
            }
        }

        return curMissionId;
    }

    /**
     * 迷雾深林活动是否开启
     *
     * @return
     */
    public boolean isOpen() {
        return this.open;
    }

    /**
     * 更新到数据库
     */
    @Override
    public void update() {
        gameplayEntity entity = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.GTE_MistTimeLimitMission);
        entity.setGameplayinfo(this.globalCfg.build().toByteArray());
        gameplayCache.put(entity);
    }

    /**
     * 判断玩家是否在相同的楼层任务内
     *
     * @param beforeMistLv
     * @param afterMistLv
     * @return
     */
    public boolean inSameTimeLimitActivity(int beforeMistLv, int afterMistLv) {
        if (beforeMistLv == afterMistLv) {
            return true;
        }

        for (DB_MistTimeLimitMissionCfg missionCfg : this.globalCfg.getMissionCfgList()) {
            if (missionCfg.getMissionsLimitLvList().contains(beforeMistLv)
                    && missionCfg.getMissionsLimitLvList().contains(afterMistLv)) {
                return true;
            }
        }
        return false;
    }

}