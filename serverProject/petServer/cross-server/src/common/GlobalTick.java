package common;

import common.load.ServerConfig;
import io.netty.util.internal.ConcurrentSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import model.mistforest.room.cache.MistRoomCache;
import model.mistplayer.cache.MistPlayerCache;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warroom.dbCache.WarRoomCache;
import util.LogUtil;

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

    private final Set<Tickable> tickSet = new ConcurrentSet<>();

    public boolean addTick(Tickable tickable) {
        if (tickable == null || tickSet.contains(tickable)) {
            return false;
        }
        return tickSet.add(tickable);
    }

    public void start() {
        startTimeTick();
        startLogicTick();
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

    private void startLogicTick() {
        GlobalThread.getInstance().execute(() -> {
            LogUtil.info("GlobalTick start logic tick, curTime = " + getNewTime());
            Thread.currentThread().setName("logicTick");
            while (run.get()) {
                try {
                    for (Tickable tickable : tickSet) {
                        tickable.onTick();
                    }

                    Thread.sleep(ServerConfig.getInstance().getTimeTickCycle());
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                }
            }
            LogUtil.warn("logicTick is over");
        });
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
        LogUtil.info("MistRoom count = " + MistRoomCache.getInstance().getMistRoomCount());
        LogUtil.info("MistPlayer count = " + MistPlayerCache.getInstance().getObjCount());
        LogUtil.info("WarRoom count = " + WarRoomCache.getInstance().getObjCount());
        LogUtil.info("WarPlayer count = " + WarPlayerCache.getInstance().getObjCount());
        LogUtil.info("========================SYSTEM INFO================================");
    }

    private void startTimeTick() {
        GlobalThread.getInstance().getExecutor().execute(this);
    }
}
