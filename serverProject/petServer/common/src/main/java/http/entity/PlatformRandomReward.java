package http.entity;

/**
 * @author xiao_FL
 * @date 2019/11/28
 */
public class PlatformRandomReward {
    private int rewardType;
    private int id;
    private int count;
    private int randomOdds;

    public int getRewardType() {
        return rewardType;
    }

    public void setRewardType(int rewardType) {
        this.rewardType = rewardType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getRandomOdds() {
        return randomOdds;
    }

    public void setRandomOdds(int randomOdds) {
        this.randomOdds = randomOdds;
    }
}