package common;

import common.load.ServerConfig;
import model.room.cache.RoomCache;
import util.LogUtil;

import java.util.concurrent.atomic.AtomicBoolean;

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
    private final AtomicBoolean run = new AtomicBoolean(true);


    public void start() {
        startTimeTick();
    }

    public long getCurrentTime() {
        if (currentTime == 0) {
            currentTime = System.currentTimeMillis();
        }
        return currentTime;
    }

    private long getNewTime() {
        return System.currentTimeMillis();
    }

    @Override
    public void run() {
        LogUtil.info("GlobalTick start time tick, curTime = " + getNewTime());
        Thread.currentThread().setName("timeTick");
        long lastShowSystemInfo = 0;
        while (run.get()) {
            try {
                this.currentTime = getNewTime();
                if (getCurrentTime() - lastShowSystemInfo > ServerConfig.getInstance().getPrintSvrInfoCycle()) {
                    showSystemInfo();
                    lastShowSystemInfo = getCurrentTime();
                }

                Thread.sleep(ServerConfig.getInstance().getTimeTickCycle());
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
        LogUtil.info("BattleRoom count = " + RoomCache.getInstance().size());
        LogUtil.info("========================SYSTEM INFO================================");

    }

    private void startTimeTick() {
        GlobalThread.getInstance().getExecutor().execute(this);
    }
}
