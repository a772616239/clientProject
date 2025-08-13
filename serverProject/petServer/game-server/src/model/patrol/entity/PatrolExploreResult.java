package model.patrol.entity;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/8/5
 */
public class PatrolExploreResult extends PatrolMoveResult {
    private int greed;

    private int status;

    /**
     * 合计状态
     */
    private List<Integer> allStatus;

    public int getGreed() {
        return greed;
    }

    public void setGreed(int greed) {
        this.greed = greed;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Integer> getAllStatus() {
        return allStatus;
    }

    public void setAllStatus(List<Integer> allStatus) {
        this.allStatus = allStatus;
    }
}
