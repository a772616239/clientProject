package http.entity;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/11/28
 */
public class PlatformServerSubMission {
    private int index;

    private int subType;

    private int addition;

    private int target;

    private JSONObject name;

    private JSONObject desc;

    private List<PlatformReward> reward;

    private List<PlatformRandomReward> randoms;

    private int randomTimes;

    private long endTimestamp;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public int getAddition() {
        return addition;
    }

    public void setAddition(int addition) {
        this.addition = addition;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public JSONObject getName() {
        return name;
    }

    public void setName(JSONObject name) {
        this.name = name;
    }

    public JSONObject getDesc() {
        return desc;
    }

    public void setDesc(JSONObject desc) {
        this.desc = desc;
    }

    public List<PlatformReward> getReward() {
        return reward;
    }

    public void setReward(List<PlatformReward> reward) {
        this.reward = reward;
    }

    public List<PlatformRandomReward> getRandoms() {
        return randoms;
    }

    public void setRandoms(List<PlatformRandomReward> randoms) {
        this.randoms = randoms;
    }

    public int getRandomTimes() {
        return randomTimes;
    }

    public void setRandomTimes(int randomTimes) {
        this.randomTimes = randomTimes;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }
}