package http.entity;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/12/2
 */
public class PlatformDropInfo {
    /**
     * 掉落道具id
     */
    private int dropId;
    /**
     * 掉落概率(千分比)
     */
    private int dropOdds;
    /**
     * 每日掉落上限(-1无上限)
     */
    private int dropDailyLimit;
    /**
     * 掉落来源
     */
    private List<Integer> dropSource;

    public int getDropId() {
        return dropId;
    }

    public void setDropId(int dropId) {
        this.dropId = dropId;
    }

    public int getDropOdds() {
        return dropOdds;
    }

    public void setDropOdds(int dropOdds) {
        this.dropOdds = dropOdds;
    }

    public int getDropDailyLimit() {
        return dropDailyLimit;
    }

    public void setDropDailyLimit(int dropDailyLimit) {
        this.dropDailyLimit = dropDailyLimit;
    }

    public List<Integer> getDropSource() {
        return dropSource;
    }

    public void setDropSource(List<Integer> dropSource) {
        this.dropSource = dropSource;
    }
}
