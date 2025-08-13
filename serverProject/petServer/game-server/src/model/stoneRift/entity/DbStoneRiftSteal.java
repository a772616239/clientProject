package model.stoneRift.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DbStoneRiftSteal implements Serializable {
    private static final long serialVersionUID = 2838936502319930718L;
    private int factoryId;
    private int stealCount;
    private int stealRewardCount;

    private int level;

    public boolean isCanSteal() {
        return canSteal;
    }

    public void setCanSteal(boolean canSteal) {
        this.canSteal = canSteal;
    }

    private boolean canSteal;


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(int factoryId) {
        this.factoryId = factoryId;
    }

    public int getStealCount() {
        return stealCount;
    }

    public void setStealCount(int stealCount) {
        this.stealCount = stealCount;
    }

    public int getStealRewardCount() {
        return stealRewardCount;
    }

    public void setStealRewardCount(int stealRewardCount) {
        this.stealRewardCount = stealRewardCount;
    }
}
