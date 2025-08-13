package model.stoneRift.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DbStoneRiftEvent implements Serializable {

    private static final long serialVersionUID = 9066394701339781530L;

    private int event;

    private long expireTime;

    private boolean alreadyTrigger;

    private long nextCanTriggerTime;

    private long startTime;

    public int getRewardId() {
        return rewardId;
    }

    public void setRewardId(int rewardId) {
        this.rewardId = rewardId;
    }

    private int rewardId;

    private Map<Integer, Integer> eventEffect = new HashMap<>(1);


    public Map<Integer, Integer> getEventEffect() {
        return eventEffect;
    }

    public void setEventEffect(Map<Integer, Integer> eventEffect) {
        this.eventEffect = eventEffect;
    }

    public long getNextCanTriggerTime() {
        return nextCanTriggerTime;
    }

    public void setNextCanTriggerTime(long nextCanTriggerTime) {
        this.nextCanTriggerTime = nextCanTriggerTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isAlreadyTrigger() {
        return alreadyTrigger;
    }

    public void setAlreadyTrigger(boolean alreadyTrigger) {
        this.alreadyTrigger = alreadyTrigger;
    }
}
