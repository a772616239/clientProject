package entity;

import java.util.List;
import protocol.Common.Reward;

/**
 * @author xiao_FL
 * @date 2019/12/9
 */
public class RewardResult extends CommonResult {
    /**
     * 奖励内容
     */
    private List<Reward> rewardList;

    public List<Reward> getRewardList() {
        return rewardList;
    }

    public void setRewardList(List<Reward> rewardList) {
        this.rewardList = rewardList;
    }
}
