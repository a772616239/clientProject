package server.http.entity;

import lombok.Data;
import protocol.Activity.ENUMStarTreasureRewardPoolColor;
import protocol.Activity.StarTreasureCostInfo;
import protocol.Common.RandomReward;
import protocol.Common.RewardTypeEnum;
import protocol.Server.ServerPlatformStarTreasure;
import protocol.Server.ServerStarTreasureRewardPool;
import util.LogUtil;

import java.util.List;

@Data
public class PlatformStarTreasure {
    private int noticeColor;
    private int limitGainStarCount;
    private int perTenGainStar;
    private List<CostInfo> costList;
    private PlatformConsume costItem;
    private List<RewardPool> rewardPoolList;

    public ServerPlatformStarTreasure.Builder toBuilder(){
        ServerPlatformStarTreasure.Builder builder = ServerPlatformStarTreasure.newBuilder();

        ENUMStarTreasureRewardPoolColor color = ENUMStarTreasureRewardPoolColor.forNumber(this.getNoticeColor());
        builder.setNoticeColor(color);
        builder.setLimitGainStarCount(this.getLimitGainStarCount());
        builder.setPerTenGainStar(this.getPerTenGainStar());
        builder.setCostItem(this.getCostItem().toConsume());

        this.setCostBuiler(builder);
        this.setPoolBuiler(builder);

        return builder;
    }

    private void setCostBuiler(ServerPlatformStarTreasure.Builder builder){
        if(this.costList == null || this.costList.isEmpty()){
            LogUtil.error("plat StarTreasure cost list is null.");
            return;
        }

        for (CostInfo cost:costList) {
            if(cost == null){
                continue;
            }
            builder.addCostInfo(cost.toBuilder());
        }
    }

    private void setPoolBuiler(ServerPlatformStarTreasure.Builder builder){
        if(this.rewardPoolList == null && this.rewardPoolList.isEmpty()){
            LogUtil.error("plat StarTreasure rewardPoolList is null.");
            return;
        }

        for (RewardPool pool:rewardPoolList) {
            if(pool == null){
                continue;
            }
            builder.addRewardPool(pool.toBuilder());
        }
    }


}

@Data
class CostInfo{
    private int times;
    private int count;

    protected StarTreasureCostInfo.Builder toBuilder(){
        StarTreasureCostInfo.Builder costBuilder = StarTreasureCostInfo.newBuilder();
        costBuilder.setTimes(this.getTimes());
        costBuilder.setCount(this.getCount());
        return costBuilder;
    }
}

@Data
class RewardPool{
    private int color;
    private String name;
    private String weight;         //权重
    private int chooseLimit;
    private List<PlatformRandomReward> itemList;

    protected ServerStarTreasureRewardPool.Builder toBuilder(){
        ServerStarTreasureRewardPool.Builder poolBuilder = ServerStarTreasureRewardPool.newBuilder();

        ENUMStarTreasureRewardPoolColor color = ENUMStarTreasureRewardPoolColor.forNumber(this.getColor());
        poolBuilder.setColor(color);

        poolBuilder.setName(this.getName());
        try{
            poolBuilder.setWeight(Integer.parseInt(weight));
        }catch (Exception e){
            LogUtil.error("plat StarTreasure pool weight error.weight={}",this.getWeight());
        }

        poolBuilder.setChooseLimit(this.getChooseLimit());
        this.setRewardList(poolBuilder);

        return poolBuilder;
    }

    protected void setRewardList(ServerStarTreasureRewardPool.Builder poolBuilder){
        if(this.itemList == null && this.itemList.isEmpty()){
            return;
        }
        for (PlatformRandomReward item:itemList) {
            if(item == null){
                LogUtil.error("plat StarTreasure pool reward null item.poolColor={}",this.getColor());
                continue;
            }
            RewardTypeEnum type = RewardTypeEnum.forNumber(item.getRewardType());
            if(type == null){
                LogUtil.error("plat StarTreasure pool reward item type error.poolColor={}",this.getColor());
                continue;
            }

            RandomReward.Builder itemBuilder = RandomReward.newBuilder();
            itemBuilder.setRewardType(type);

            itemBuilder.setId(item.getId());
            itemBuilder.setCount(item.getCount());
            itemBuilder.setRandomOdds(item.getRandomOdds());
            poolBuilder.addItems(itemBuilder);
        }
    }


}
