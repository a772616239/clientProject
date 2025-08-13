package model.activity;

/**
 * 活动接口
 */
public interface IActivity {
    /**
     * 活动开始
     */
    public boolean start();

    /**
     * 活动更新帧频
     */
    public void update(long now);

    /**
     * 活动结束
     */
    public boolean end();

    /**
     * 开始前N分钟触发
     */
    public void onStartBeforNMinute(int minute);

    /**
     * 开始后N分钟触发
     */
    public void onStartAfterNMinute(int minute);

    /**
     * 结束前N分钟触发
     */
    public void onEndBeforNMinute(int minute);

    /**
     * 结束后N分钟触发
     */
    public void onEndAfterNMinute(int minute);

}
