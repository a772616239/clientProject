package petrobot.tick;

import petrobot.util.LogUtil;

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


    public long getCurrentTime() {
        return currentTime;
    }

    private long getNewTime() {
        return System.currentTimeMillis();
    }

    @Override
    public void run() {
        LogUtil.info("GlobalTick start tick, curTime = " + getNewTime());
        Thread.currentThread().setName("timeTick");
        while (true) {
            try {
                this.currentTime = getNewTime();
                Thread.sleep(200);
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
    }
}