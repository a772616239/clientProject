package model.activityboss.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ActivityBossRewardUnit {
    private long damage;
    private int totalWeight;
    private Map<Integer, Integer> weightReward; // 权重及奖励id

    public void addWeightReward(int rewardCfgId, int weight) {
        if (weightReward == null) {
            weightReward = new HashMap<>();
        }
        weightReward.put(rewardCfgId, weight);
    }
}
