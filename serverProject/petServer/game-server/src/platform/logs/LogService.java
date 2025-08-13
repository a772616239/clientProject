package platform.logs;

import common.*;
import common.GameConst.RankingName;
import common.entity.RankingUpdateRequest;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import common.tick.Tickable;
import java.io.File;
import java.util.*;
import model.drawCard.DrawCardManager;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.entity.OnLinePlayerNumLog;
import platform.logs.entity.OnLineTimeLog;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

public class LogService implements Tickable {

    private static LogService instance = new LogService();

    public static LogService getInstance() {
        if (instance == null) {
            synchronized (DrawCardManager.class) {
                if (instance == null) {
                    instance = new LogService();
                }
            }
        }
        return instance;
    }

    private LogService() {
    }


    public String URL = ServerConfig.getInstance().getPlatformLogBaseDir();

    private Map<Class<? extends AbstractServerLog>, List<String>> KEYS = new HashMap<>();

    private List<SaveLogsThread> threads = new ArrayList<>();

    public Map<Class<? extends AbstractServerLog>, List<String>> getKEYS() {
        return KEYS;
    }

    private Map<Class<? extends AbstractServerLog>, Object> LOCKS = new HashMap<>();

    private volatile long nextSaveTime;
    private volatile long nextLogOnLinePlayerNumTime;

//    public static final String STATISTICS_LOG_TIME_ZONE = "GMT+8";

    private boolean opened = false;

    /**
     * 每次更新统计排行榜的玩家数
     **/
    private static final int UPDATE_STATISTIC_RANKING_EACH_PLAYER_SIZE = 500;

    public boolean init() throws Exception {
        if (URL.equals("")) {
            LogUtil.error("LogServer base dir is null");
            return false;
        }
        Set<Class<AbstractServerLog>> ret = LogBeanUtil.getSubClasses("platform.logs.entity", AbstractServerLog.class);
        for (Class<AbstractServerLog> logClass : ret) {
            AbstractServerLog log = logClass.newInstance();
            log.init();
            KEYS.put(log.getClass(), new ArrayList<>());
            LOCKS.put(log.getClass(), new Object());
            threads.add(new SaveLogsThread(log.getClass()));
            if (!checkDirectory(logClass)) {
                return false;
            }
        }
        opened = true;
        return GlobalTick.getInstance().addTick(this);
    }

    /**
     * 检查日志对应目录是否存在
     */
    private boolean checkDirectory(Class<? extends AbstractServerLog> clazz) {
        if (null == clazz) {
            LogUtil.error("checkDirectory, params is null");
            return false;
        }

        File path = new File(LogService.getInstance().URL + "/" + clazz.getSimpleName());
        if (!path.exists()) {
            if (!path.mkdirs()) {
                LogUtil.error("LogServer create path failed, path = " + path.getPath());
                return false;
            }
        }
        return true;
    }

    public void close() {
        saveLogs();
        opened = false;
        LogUtil.debug("logServer close");
    }

    public void submit(AbstractServerLog log) {
        if (!opened) {
            LogUtil.info("log server is closed, can not submit new log");
            return;
        }
        Object lock = LOCKS.get(log.getClass());
        synchronized (lock) {
            List<String> logs = KEYS.get(log.getClass());
            logs.add(log.toJson());
        }
    }

    public Object getLock(Class<? extends AbstractServerLog> classes) {
        return LOCKS.get(classes);
    }

    public void saveLogs() {
        for (SaveLogsThread th : threads) {
            GlobalThread.getInstance().execute(th);
        }
    }

    @Override
    public synchronized void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        saveTick(currentTime);
        onlinePlayerNumTick(currentTime);
    }

    private void saveTick(long currentTime) {
        if (nextSaveTime == 0) {
            nextSaveTime = currentTime + ServerConfig.getInstance().getPlatformLogSaveInterval();
        }

        if (currentTime > nextSaveTime) {
            saveLogs();
            nextSaveTime = currentTime + ServerConfig.getInstance().getPlatformLogSaveInterval();
        }
    }

    /**
     * 每日十二点更新玩家在线时长
     */
    private void logPlayerOnlineTime() {
        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
        if (GameUtil.collectionIsEmpty(allOnlinePlayerIdx)) {
            return;
        }

        for (String onlinePlayerIdx : allOnlinePlayerIdx) {
            playerEntity player = playerCache.getByIdx(onlinePlayerIdx);
            if (player != null) {
                submit(new OnLineTimeLog(player));
            }
        }
    }

    public void updateDailyData() {
        updateStatisticsRanking();
        logPlayerOnlineTime();
    }

    /**
     * 每日统计一次
     */
    private void updateStatisticsRanking() {
        List<String> allPlayerIdx = playerCache.getInstance().getAllPlayerIdx();
        if (GameUtil.collectionIsEmpty(allPlayerIdx)) {
            LogUtil.info("LogService.updateStatisticsRanking all PlayerIdxList is empty, skip update");
            return;
        }

        List<List<String>> result = GameUtil.splitList(allPlayerIdx, UPDATE_STATISTIC_RANKING_EACH_PLAYER_SIZE);
        if (GameUtil.collectionIsEmpty(result)) {
            LogUtil.error("LogService.updateStatisticsRanking, split all player list failed");
            return;
        }

        for (List<String> strings : result) {
            updateStatisticRanking(strings);
        }
    }

    /**
     * 通过一个列表更新玩家排行榜数据
     *
     * @param playerList
     */
    private void updateStatisticRanking(List<String> playerList) {
        if (GameUtil.collectionIsEmpty(playerList)) {
            return;
        }

        Map<String, RankingUpdateRequest> updateRanking = new HashMap<>(10);
        for (String playerIdx : playerList) {
            playerEntity player = playerCache.getByIdx(playerIdx);
            if (player == null) {
                continue;
            }

            //战力
            updateRanking.computeIfAbsent(RankingName.RN_Statistics_Ability,
                    t -> new RankingUpdateRequest(RankingName.RN_Statistics_Ability)).
                    addScore(playerIdx, petCache.getInstance().totalAbility(playerIdx));
            //钻石
            updateRanking.computeIfAbsent(RankingName.RN_Statistics_Diamond,
                    t -> new RankingUpdateRequest(RankingName.RN_Statistics_Diamond)).
                    addScore(playerIdx, player.getDiamond());

            //金币
            updateRanking.computeIfAbsent(RankingName.RN_Statistics_Gold,
                    t -> new RankingUpdateRequest(RankingName.RN_Statistics_Gold)).
                    addScore(playerIdx, player.getGold());

            //点券
            updateRanking.computeIfAbsent(RankingName.RN_Statistics_Coupon,
                    t -> new RankingUpdateRequest(RankingName.RN_Statistics_Coupon)).
                    addScore(playerIdx, player.getCoupon());

            itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
            if (itemBag != null) {
                //流浪印记
                updateRanking.computeIfAbsent(RankingName.RN_Statistics_StrayMarks,
                        t -> new RankingUpdateRequest(RankingName.RN_Statistics_StrayMarks)).
                        addScore(playerIdx, itemBag.getItemCount(GameConst.ITEM_ID_STRAY_MARKS));

                //高级召唤卷
                updateRanking.computeIfAbsent(RankingName.RN_Statistics_HighDrawCard,
                        t -> new RankingUpdateRequest(RankingName.RN_Statistics_HighDrawCard)).
                        addScore(playerIdx, itemBag.getItemCount(GameConst.ITEM_ID_HIGH_DRAW_CARD));

                //远古召唤卷
                updateRanking.computeIfAbsent(RankingName.RN_Statistics_Ancient,
                        t -> new RankingUpdateRequest(RankingName.RN_Statistics_Ancient)).
                        addScore(playerIdx, itemBag.getItemCount(GameConst.ITEM_ID_HIGH_ANCIENT));
            }
        }

        for (RankingUpdateRequest value : updateRanking.values()) {
            try {
                HttpRequestUtil.asyncUpdateRanking(value);
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
    }

    private void onlinePlayerNumTick(long curTime) {
        if (curTime > nextLogOnLinePlayerNumTime) {
            submit(new OnLinePlayerNumLog());
            nextLogOnLinePlayerNumTime = curTime + TimeUtil.MS_IN_A_MIN;
        }
    }
}
