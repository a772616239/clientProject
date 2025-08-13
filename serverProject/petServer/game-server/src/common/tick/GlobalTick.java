package common.tick;

import common.GlobalData;
import common.GlobalThread;
import common.load.ServerConfig;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import model.activityboss.ActivityBossManager;
import model.cacheprocess.baseUapteCacheL;
import model.comment.dbCache.commentCache;
import model.farmmine.FarmMineManager;
import model.mistforest.MistForestManager;
import model.pet.StrongestPetManager;
import model.player.dbCache.playerCache;
import model.ranking.RankingManager;
import model.training.dbCache.trainingCache;
import model.warpServer.battleServer.BattleServerManager;
import model.warpServer.crossServer.CrossServerManager;
import util.ClassUtil;
import util.GameUtil;
import util.LogUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author huhan
 * 全局tick函数
 */
public class GlobalTick implements Runnable {
    private static GlobalTick instance = new GlobalTick();

    public static GlobalTick getInstance() {
        if (instance == null) {
            synchronized (GlobalTick.class) {
                if (instance == null) {
                    instance = new GlobalTick();
                }
            }
        }
        return instance;
    }

    private GlobalTick() {
    }

    @Getter
    private volatile long currentTime;
    private volatile long currentNanoTime;
    private final Set<Tickable> tickSet = new ConcurrentSet<>();
    private final AtomicBoolean run = new AtomicBoolean(true);


    public void start() {
        this.currentTime = getNewTime();
        startTimeTick();
        initTickSet();
        startLogicTick();
        playerTick();
    }

    private void playerTick() {
        GlobalThread.getInstance().execute(() -> {
            LogUtil.info("playerTick start tick, curTime = " + GlobalTick.getInstance().getCurrentTime());
            Thread.currentThread().setName("PlayerTick");
            while (run.get()) {
                try {

                    playerCache.getInstance().onTick();
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                } finally {
                    GameUtil.sleep(ServerConfig.getInstance().getLogicTickCycle());
                }
            }
            LogUtil.warn("playerTick is over");
        });
    }

    private void initTickSet() {
        tickSet.add(MistForestManager.getInstance());
        tickSet.add(BattleServerManager.getInstance());
        tickSet.add(CrossServerManager.getInstance());
        tickSet.add(commentCache.getInstance());
        tickSet.add(RankingManager.getInstance());
        tickSet.add(ActivityBossManager.getInstance());
        tickSet.add(StrongestPetManager.getInstance());
        tickSet.add(trainingCache.getInstance());
        tickSet.add(FarmMineManager.getInstance());
    }

    public boolean addTick(Tickable tickable) {
        if (tickable == null || tickSet.contains(tickable)) {
            LogUtil.error("common.tick.GlobalTick.addTick, params is null or new tick is already exist in tick set:" +
                    " tick name:" + (tickable == null ? "" : tickable.getClass().getSimpleName()));
            return false;
        }
        return tickSet.add(tickable);
    }

    private long getNewTime() {
        return System.currentTimeMillis();
    }

    public void closeTick() {
        run.set(false);
    }

    /**
     * logicTick
     */
    private void onTick() {
        for (Tickable next : tickSet) {
            if (next != null) {
//                long start = Instant.now().toEpochMilli();

                next.onTick();

//                long useTime = Instant.now().toEpochMilli() - start;
//                String className = next.getClass().getSimpleName();
//                if (useTime >= 30) {
//                    LogUtil.info("");
//                }
            }
        }
    }

    @Override
    public void run() {
        LogUtil.info("GlobalTick start tick, curTime = " + getNewTime());
        Thread.currentThread().setName("GlobalTick");
        long lastShowSystemInfo = 0;
        while (run.get()) {
            try {
                this.currentTime = getNewTime();
                if (getCurrentTime() - lastShowSystemInfo > ServerConfig.getInstance().getSystemInfoPrint()) {
                    showSystemInfo();
                    lastShowSystemInfo = getCurrentTime();
                }
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            } finally {
                GameUtil.sleep(ServerConfig.getInstance().getTickCycle());
            }
        }
        LogUtil.warn("timeTick is over");
    }

    private void showSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        LogUtil.info("========================SYSTEM INFO================================");
        LogUtil.info("CPU core count = " + runtime.availableProcessors());
        LogUtil.info("free memory = " + runtime.freeMemory() / (1024 * 1024 * 8) + "MB");
        LogUtil.info("max memory = " + runtime.maxMemory() / (1024 * 1024 * 8) + "MB");
        LogUtil.info("used memory = " + (runtime.maxMemory() - runtime.freeMemory()) / (1024 * 1024 * 8) + "MB");
//        LogUtil.info("total usable memory = " + runtime.totalMemory() / (1024 * 1024)  + "MB");
        LogUtil.info("online player count = " + GlobalData.getInstance().getOnlinePlayerNum());
        LogUtil.info("========================SYSTEM INFO================================");

        StringBuilder sb = new StringBuilder("Global tickSet size=" + tickSet.size() + ",detail:");
        for (Tickable next : tickSet) {
            sb.append(next.getClass().getSimpleName() + ",");
        }
        LogUtil.debug(sb.toString());
    }

    private void startTimeTick() {
        GlobalThread.getInstance().execute(this);
    }

    private void startLogicTick() {
        GlobalThread.getInstance().execute(() -> {
            LogUtil.info("LogicTick start tick, curTime = " + GlobalTick.getInstance().getCurrentTime());
            Thread.currentThread().setName("LogicTick");
            while (run.get()) {
                try {
                    onTick();

                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                } finally {
                    GameUtil.sleep(ServerConfig.getInstance().getLogicTickCycle());
                }
            }
            LogUtil.warn("LogicTick is over");
        });
    }

    private final Map<baseUapteCacheL, Long> updateMap = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private void initUpdateMap() {
        List<Class<baseUapteCacheL>> subClass = ClassUtil.getSubClass("model", baseUapteCacheL.class);
        for (Class<baseUapteCacheL> aClass : subClass) {
            try {
                Method method = aClass.getMethod("getInstance");
                baseUapteCacheL instance = (baseUapteCacheL) method.invoke(aClass);
                updateMap.put(instance, getNextUpdateTime());
                LogUtil.info("success add to updateMap, name:" + aClass.getSimpleName());
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }


//        updateMap.put(playerUpdateCacheL.getInstance(), getNextUpdateTime());
//
//        updateMap.put(gameplayUpdateCacheL.getInstance(), getNextUpdateTime());
//        updateMap.put(targetsystemUpdateCacheL.getInstance(), getNextUpdateTime());
//        updateMap.put(teamUpdateCacheL.getInstance(), getNextUpdateTime());
//
//        updateMap.put(itembagUpdateCacheL.getInstance(), getNextUpdateTime());
//        updateMap.put(mailboxUpdateCacheL.getInstance(), getNextUpdateTime());
//        updateMap.put(mainlineUpdateCacheL.getInstance(), getNextUpdateTime());
//
//        updateMap.put(bravechallengeUpdateCacheL.getInstance(), getNextUpdateTime());
//        updateMap.put(patrolUpdateCacheL.getInstance(), getNextUpdateTime());
//        updateMap.put(exchangehistoryUpdateCacheL.getInstance(), getNextUpdateTime());
//
//        updateMap.put(petUpdateCacheL.getInstance(), getNextUpdateTime());
//        updateMap.put(petfragmentUpdateCacheL.getInstance(), getNextUpdateTime());
//        updateMap.put(petmissionUpdateCacheL.getInstance(), getNextUpdateTime());
//        updateMap.put(petruneUpdateCacheL.getInstance(), getNextUpdateTime());
//
//        updateMap.put(timerUpdateCacheL.getInstance(), getNextUpdateTime());
//
//        updateMap.put(mainlinerecentpassUpdateCacheL.getInstance(), getNextUpdateTime());
    }

    private long getNextUpdateTime() {
        return getCurrentTime() + random.nextInt(20) * ServerConfig.getInstance().getUpdateTickCycle();
    }

    public void startUpdateTick() {
        initUpdateMap();
        LogUtil.info("UpdateTick start tick, curTime = " + GlobalTick.getInstance().getCurrentTime());
        Thread.currentThread().setName("UpdateTick");
        while (run.get()) {
            try {
                for (Entry<baseUapteCacheL, Long> entry : updateMap.entrySet()) {
                    if (GlobalTick.getInstance().getCurrentTime() > entry.getValue()) {
                        long start = System.currentTimeMillis();
                        entry.getKey().dealUpdateCache();
                        long useTime = System.currentTimeMillis() - start;
                        if (useTime > 100) {
                            LogUtil.info("update " + entry.getKey().getClass().getSimpleName() + " use time = " + useTime);
                        }

                        entry.setValue(getNextUpdateTime());
                    }
                }
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            } finally {
                GameUtil.sleep(ServerConfig.getInstance().getUpdateTickCycle());
            }
        }
        LogUtil.warn("UpdateTick is over");
    }
}
