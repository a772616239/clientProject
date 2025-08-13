package entity;

/**
 * 此接口用于标记 需要调用每日更新的cache对象
 * @author huhan
 * @date 2020.10.30
 */
public interface UpdateDailyData {

    /**
     * 每日更新时调用
     */
    void updateDailyData();
}
