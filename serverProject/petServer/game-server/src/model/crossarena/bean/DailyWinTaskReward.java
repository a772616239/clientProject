package model.crossarena.bean;

import protocol.Common;

import java.util.Collections;
import java.util.List;

public class DailyWinTaskReward {
    private List<Integer> taskIds = Collections.emptyList();
    private List<Common.Reward> rewards = Collections.emptyList();

    public List<Integer> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<Integer> taskIds) {
        this.taskIds = taskIds;
    }

    public List<Common.Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Common.Reward> rewards) {
        this.rewards = rewards;
    }
}
