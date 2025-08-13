package util.timerule;

/**
 * 时间计时类,多用于定时
 *
 * @author Autumn
 */
public class SafeTimer {
    private long startTime;
    private long interval;// 间隔毫秒
    boolean isfirst = true;

    public SafeTimer() {
    }

    /**
     * 构造一个带间隔时间的类
     *
     * @param dur
     */
    public SafeTimer(long dur) {
        start(dur);
    }

    /**
     * 计时开始
     *
     * @param dur
     */
    public void start(long dur) {
        this.interval = dur;
        startTime = System.currentTimeMillis();
        isfirst = true;
    }

    /**
     * 重新启动
     *
     * @param dur    间隔时间
     * @param elapse 时间偏差
     * @param now    当前时间
     */
    private void restart(long dur, long elapse, long now) {
        this.interval = dur;
        if (elapse < 0) {
            startTime = now;
        } else {
            startTime = now;
        }
    }

    /**
     * 是否已经到了计时时间
     *
     * @return
     */
    public boolean isOK() {
        long curTime = System.currentTimeMillis();
        if (curTime - startTime >= interval)
            return true;
        return false;
    }

    /**
     * 是否已经到了计时时间
     *
     * @param now
     * @return
     */
    public boolean isOK(long now) {
        if (now - startTime >= interval)
            return true;
        return false;
    }

    /**
     * 距离间隔还差多少
     *
     * @return
     */
    public long remainTimeToIntvl() {
        long curTime = System.currentTimeMillis();
        if (!isOK())
            return interval - (curTime - startTime);
        return 0;
    }

    /**
     * 距离间隔还差多少秒
     *
     * @return
     */
    public int remainSecondsToIntvl() {
        return remainTimeToIntvl() / 1000 <= 0 ? 0 : (int) (remainTimeToIntvl() / 1000);
    }

    /**
     * 恢复初始化
     *
     * @return
     */
    public void reset() {
        startTime = 0;
        interval = 0;
        isfirst = true;
    }

    /**
     * 第一次到时
     *
     * @param now
     * @return
     */
    public boolean isFirstOK(long now) {
        if (isfirst && now - startTime >= interval) {
            isfirst = false;
            return true;
        }
        return false;
    }

    /**
     * 是否到了固定的间隔
     *
     * @param now
     * @return
     */
    public boolean isIntervalOK(long now) {
        if (now - startTime >= interval) {
            restart(interval, now - startTime - interval, now);
            return true;
        }
        return false;
    }

    /**
     * 修改间隔时间(毫秒)
     *
     * @param interval
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * 设置启动时间
     *
     * @param startTime
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * 获取启动时间
     *
     * @return
     */
    public long getStartTime() {
        return startTime;
    }
}
