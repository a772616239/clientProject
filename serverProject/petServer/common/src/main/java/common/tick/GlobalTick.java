package common.tick;

import common.GlobalThread;
import io.netty.util.internal.ConcurrentSet;
import model.cacheprocess.baseUapteCacheL;
import util.LogUtil;
import util.ServerConfig;

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

    private volatile long currentTime;
    private final Set<Tickable> tickSet = new ConcurrentSet<>();
    private final AtomicBoolean run = new AtomicBoolean(true);


    public void start() {
        startTimeTick();
        initTick();
        startLogicTick();
    }

    private void initTick() {
    }

    public boolean addTick(Tickable tickable) {
        if (tickable == null || tickSet.contains(tickable)) {
            return false;
        }
        return tickSet.add(tickable);
    }

    public long getCurrentTime() {
        return currentTime;
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
                next.onTick();
            }
        }
    }

    @Override
    public void run() {
        LogUtil.info("GlobalTick start tick, curTime = " + getNewTime());
        Thread.currentThread().setName("timeTick");
        long lastShowSystemInfo = 0;
        while (run.get()) {
            try {
                this.currentTime = getNewTime();
                if (getCurrentTime() - lastShowSystemInfo > ServerConfig.getInstance().getSystemInfoPrint()) {
                    showSystemInfo();
                    lastShowSystemInfo = getCurrentTime();
                }

                Thread.sleep(ServerConfig.getInstance().getTickCycle());
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
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
//        LogUtil.info("online player count = " + GlobalData.getInstance().getOnlinePlayerNum());
        LogUtil.info("========================SYSTEM INFO================================");
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

                    Thread.sleep(ServerConfig.getInstance().getLogicTickCycle());
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                }
            }
            LogUtil.warn("LogicTick is over");
        });
    }

    private final Map<baseUapteCacheL, Long> updateMap = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private void initUpdateMap() {
    }

    private long getNextUpdateTime() {
        return getCurrentTime() + random.nextInt(10) * ServerConfig.getInstance().getUpdateTickCycle();
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
                        if (useTime > 0) {
                            LogUtil.info("update " + entry.getKey().getClass().getSimpleName() + " use time = " + useTime);
                        }

                        entry.setValue(getNextUpdateTime());
                    }
                }
                Thread.sleep(ServerConfig.getInstance().getUpdateTickCycle());
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        LogUtil.warn("UpdateTick is over");
    }
}
