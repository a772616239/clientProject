package model.pet.entity;

import entity.CommonResult;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/9/6
 */
public class PetCollectionResult extends CommonResult {
    /**
     * 玩家收集宠物
     */
    private List<Integer> cfgIdList;

    /**
     * 领取奖励表CollectionRewardId
     */
    private int rewardId;

    public List<Integer> getCfgIdList() {
        return cfgIdList;
    }

    public void setCfgIdList(List<Integer> cfgIdList) {
        this.cfgIdList = cfgIdList;
    }

    public int getRewardId() {
        return rewardId;
    }

    public void setRewardId(int rewardId) {
        this.rewardId = rewardId;
    }
}
