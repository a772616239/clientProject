package server.http.entity;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/12/2
 */
public class PlatformServerExMission {
    /**
     * 活动内序号不允许重复
     */
    private int index;

    /**
     * 任务名
     */
    private JSONObject name;

    /**
     * 描述
     */
    private JSONObject desc;

    /**
     * 可兑换次数(-1表示无限制)
     */
    private int exchangeLimit;

    /**
     * 兑换物条件,以兑换条件index为键
     */
    private List<PlatformExchangeSlot> exSlots;

    /**
     * 兑换奖励
     */
    private List<PlatformReward> rewards;

    /**
     * 过期时间戳(-1表示永不过期)
     */
    private long endTimestamp;

    /**
     * 约定的一些客户端视觉表现(待定)
     */
    private int visualFlag;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public int getExchangeLimit() {
        return exchangeLimit;
    }

    public void setExchangeLimit(int exchangeLimit) {
        this.exchangeLimit = exchangeLimit;
    }

    public List<PlatformExchangeSlot> getExSlots() {
        return exSlots;
    }

    public void setExSlots(List<PlatformExchangeSlot> exSlots) {
        this.exSlots = exSlots;
    }

    public List<PlatformReward> getRewards() {
        return rewards;
    }

    public void setRewards(List<PlatformReward> rewards) {
        this.rewards = rewards;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public int getVisualFlag() {
        return visualFlag;
    }

    public void setVisualFlag(int visualFlag) {
        this.visualFlag = visualFlag;
    }
}
