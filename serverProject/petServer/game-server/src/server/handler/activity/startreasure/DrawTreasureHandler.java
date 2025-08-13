package server.handler.activity.startreasure;

import common.AbstractBaseHandler;
import common.GameConst.RedisKey;
import common.GameConst.StarTreasureConstant;
import common.JedisUtil;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import lombok.Data;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.consume.ConsumeManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_DarwStarTreasure;
import protocol.Activity.ENUMStarTreasureItemStatus;
import protocol.Activity.ENUMStarTreasureRewardPoolColor;
import protocol.Activity.SC_DarwStarTreasure;
import protocol.Activity.StarTreasureChooseItem;
import protocol.Activity.StarTreasureCostInfo;
import protocol.Activity.StarTreasureRecord;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RandomReward;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.Server.ServerPlatformStarTreasure;
import protocol.Server.ServerStarTreasureRewardPool;
import protocol.TargetSystemDB.DB_StarTreasureActivity;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 星星宝藏-占星
 */
@MsgId(msgId = MsgIdEnum.CS_DarwStarTreasure_VALUE)
public class DrawTreasureHandler extends AbstractBaseHandler<CS_DarwStarTreasure> {

    @Override
    protected CS_DarwStarTreasure parse(byte[] bytes) throws Exception {
        return CS_DarwStarTreasure.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_DarwStarTreasure req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        SC_DarwStarTreasure.Builder resultBuilder = SC_DarwStarTreasure.newBuilder();
        if (!ActivityUtil.activityInOpen(activityCfg)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_DarwStarTreasure_VALUE, resultBuilder);
            return;
        }

        if (!this.isRightCfg(playerIdx, req.getActivityId(), activityCfg)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
            gsChn.send(MsgIdEnum.SC_DarwStarTreasure_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DarwStarTreasure_VALUE, resultBuilder);
            return;
        }

        List<StarTreasureChooseItem> chooseItemList = entity.getDb_Builder().getSpecialInfo().getStarTreasureActivity().getChooseItemsList();
        if (chooseItemList.isEmpty()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.REC_StarTreasure_NotChoose));
            gsChn.send(MsgIdEnum.SC_DarwStarTreasure_VALUE, resultBuilder);
            return;
        }

        int drawTimes = this.getDrawedTimes(chooseItemList);
        if (drawTimes >= StarTreasureConstant.MAX_ITEM_COUNT) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.REC_StarTreasure_GetAll));
            gsChn.send(MsgIdEnum.SC_DarwStarTreasure_VALUE, resultBuilder);
            return;
        }

        int needCostCount = this.drawCostByTimes(++drawTimes, activityCfg.getStarTreasure().getCostInfoList());
        if (needCostCount <= 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
            gsChn.send(MsgIdEnum.SC_DarwStarTreasure_VALUE, resultBuilder);
            return;
        }

        long haveStarCount = ConsumeManager.getInstance().getConsumItemCount(playerIdx, activityCfg.getStarTreasure().getCostItem());
        if (haveStarCount < needCostCount) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.REC_Star_NotEnough));
            gsChn.send(MsgIdEnum.SC_DarwStarTreasure_VALUE, resultBuilder);
            return;
        }

        List<StarTreasureChooseItem> noGetItemList = this.getNoGetItem(chooseItemList);
        if (noGetItemList.isEmpty()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DarwStarTreasure_VALUE, resultBuilder);
            return;
        }

        StarTreasureReward rewardItem;
        try {
            rewardItem = this.drawStarTreasure(playerIdx,noGetItemList,activityCfg.getStarTreasure().getRewardPoolList());
        } catch (Exception e) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DarwStarTreasure_VALUE, resultBuilder);
            return;
        }

        boolean consumeResult = this.consumeStar(playerIdx,needCostCount,activityCfg.getStarTreasure().getCostItem());
        if(!consumeResult){
            LogUtil.error("StarTreasure consume error.pid:{} activityId:{} needCount={} needItem:\n{}..",
                    playerIdx,req.getActivityId(),needCostCount,activityCfg.getStarTreasure().getCostItem().toString());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DarwStarTreasure_VALUE, resultBuilder);
            return;
        }

        boolean rewardResult = this.addReward(playerIdx,rewardItem.getReward());
        if(!rewardResult){
            LogUtil.error("StarTreasure addReward error.pid:{} activityId:{} consumeCount={} needItem:\n{}..",
                    playerIdx,req.getActivityId(),needCostCount,activityCfg.getStarTreasure().getCostItem().toString());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DarwStarTreasure_VALUE, resultBuilder);
            return;
        }

        this.saveRecord(playerIdx,rewardItem,activityCfg.getStarTreasure());
        StarTreasureChooseItem chooseItem = this.updateChooseItemStatus(entity,rewardItem,activityCfg.getStarTreasure());
        if(chooseItem == null){
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
        }else{
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setReward(chooseItem);
        }
        gsChn.send(MsgIdEnum.SC_DarwStarTreasure_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StarTreasure;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

    private int getDrawedTimes(List<StarTreasureChooseItem> chooseItemList) {
        int drawTimes = 0;
        for (StarTreasureChooseItem item : chooseItemList) {
            if (item != null && item.getStatusValue() == ENUMStarTreasureItemStatus.STT_status_get_VALUE) {
                ++drawTimes;
            }
        }
        return drawTimes;
    }

    private int drawCostByTimes(int times, List<StarTreasureCostInfo> costInfos) {
        for (StarTreasureCostInfo costInfo : costInfos) {
            if (costInfo != null && costInfo.getTimes() == times) {
                return costInfo.getCount();
            }
        }
        return 0;
    }

    private boolean consumeStar(String playerIdx, int useCount, Consume consume) {
        Consume.Builder consumeBuilder = consume.toBuilder();
        consumeBuilder.setCount(useCount);

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_StarTreasure);
        return ConsumeManager.getInstance().consumeMaterial(playerIdx, consumeBuilder.build(), reason);
    }

    private boolean addReward(String playerIdx, Reward rewardItem) {
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_StarTreasure);
        return RewardManager.getInstance().doReward(playerIdx,rewardItem,reason,true);
    }

    private List<StarTreasureChooseItem> getNoGetItem(List<StarTreasureChooseItem> chooseItemList) {
        List<StarTreasureChooseItem> noGetList = new ArrayList<>();
        for (StarTreasureChooseItem item : chooseItemList) {
            LogUtil.info("StarTreasureChooseItem:{}",item.toString());
            if (item != null && item.getStatusValue() == ENUMStarTreasureItemStatus.STT_status_choose_VALUE) {
                noGetList.add(item);
            }
        }
        return noGetList;
    }

    private StarTreasureReward drawStarTreasure(String playerIdx,List<StarTreasureChooseItem> allCanGetItemList, List<ServerStarTreasureRewardPool> poolList) throws Exception {
        Map<Integer,RandomPool> randPoolMap = this.buildRandomPoolAndItem(playerIdx,allCanGetItemList,poolList);
        List<RandomPool> randomList = new ArrayList<>(randPoolMap.values());
        List<RandomReward> poolRandomReward = this.buildRandPoolList(randomList);

        Reward rewardPool = RewardUtil.drawMustRandomReward(poolRandomReward);
        if(rewardPool == null ){
            LogUtil.error("StarTreasure random pool error.list:\n{}",randomList.toString());
            throw new Exception("StarTreasure random pool error");
        }
        List<RandomReward> poolRandomItemList = randPoolMap.get(rewardPool.getId()).getRandomItemList();
        Reward rewardItem = RewardUtil.drawMustRandomReward(poolRandomItemList);
        if(rewardItem == null){
            LogUtil.error("StarTreasure random item error.list:\n{}",randomList.toString());
            throw new Exception("StarTreasure random item error");
        }

        return new StarTreasureReward(rewardPool.getId(),rewardItem);
    }

    private List<RandomReward> buildRandPoolList(List<RandomPool> randomList){
        List<RandomReward> randomRewardList = new ArrayList<>(randomList.size());
        for (RandomPool pool : randomList) {
            RandomReward.Builder randomRewardBuilder = RandomReward.newBuilder();
            randomRewardBuilder.setRandomOdds(pool.getPoolOdds());
            randomRewardBuilder.setId(pool.getColor().getNumber());
            randomRewardList.add(randomRewardBuilder.build());
        }
        return randomRewardList;
    }

    private Map<Integer,RandomPool> buildRandomPoolAndItem(String playerIdx,List<StarTreasureChooseItem> allCanGetItemList, List<ServerStarTreasureRewardPool> poolList) throws Exception {
        Map<Integer,RandomPool> randomPoolMap = new HashMap<>();
        for (StarTreasureChooseItem canGetItem:allCanGetItemList) {
            ServerStarTreasureRewardPool pool = this.getRewardPoolByColor(canGetItem.getPoolColor(),poolList);
            if(pool == null){
                LogUtil.error("StarTreasure poolColor:{} not found reward pool.",canGetItem.getPoolColorValue());
                throw new Exception("StarTreasure poolColor="+canGetItem.getPoolColorValue()+" not found reward pool.");
            }
            int itemInPoolIndex = canGetItem.getPoolIndex();
            if(itemInPoolIndex >= pool.getItemsCount()){
                LogUtil.error("StarTreasure  playerId{} choose index error.poolColor:{},index:{},itemCount{}",
                                    playerIdx,pool.getColorValue(),itemInPoolIndex,pool.getItemsCount());
                throw new Exception("StarTreasure  playerId="+playerIdx+" choose index error.poolColor="+pool.getColorValue()
                                                +",index="+itemInPoolIndex+",itemCount="+pool.getItemsCount());
            }
            RandomReward randItem = pool.getItems(itemInPoolIndex);
            RandomPool randPool = randomPoolMap.get(pool.getColorValue());
            if(randPool == null){
                randPool = new RandomPool(pool.getColor(),pool.getWeight());
            }
            randPool.addRandItem(randItem);
            randomPoolMap.put(pool.getColorValue(),randPool);
        }

        return randomPoolMap;
    }

    private ServerStarTreasureRewardPool getRewardPoolByColor(ENUMStarTreasureRewardPoolColor color,List<ServerStarTreasureRewardPool> poolList){
        for (ServerStarTreasureRewardPool pool: poolList) {
            if(pool != null && pool.getColorValue() == color.getNumber()){
                return pool;
            }
        }
        return null;
    }

    private void saveRecord(String playerIdx,StarTreasureReward reward, ServerPlatformStarTreasure starTreasure){
        if(reward.getColor() < starTreasure.getNoticeColorValue()){
            return;
        }

        playerEntity pEntity = playerCache.getByIdx(playerIdx);
        if(pEntity == null){
            LogUtil.error("StarTreasure saveRecord ,player is null by playerId[{}]", playerIdx);
            return;
        }
        StarTreasureRecord.Builder record = StarTreasureRecord.newBuilder();
        record.setPlayerName(pEntity.getName());
        record.setItem(reward.getReward());

        JedisUtil.jedis.rpush(RedisKey.getStarTreasureRecordKey(),record.build().toByteArray());
        JedisUtil.jedis.ltrim(RedisKey.getStarTreasureRecordKey(),-10,-1);
    }

    private boolean isRightCfg(String playerIdx, long activityId, ServerActivity activityCfg) {
        ActivityTypeEnum type = activityCfg.getType();
        if (type != ActivityTypeEnum.ATE_StarTreasure) {
            LogUtil.error("pid:{} activityId:{} cfgType={}.activity type error.", playerIdx, activityId, type);
            return false;
        }
        if (!activityCfg.hasStarTreasure()) {
            LogUtil.error("pid:{} activityId:{}.have activity but have detail info.", playerIdx, activityId);
            return false;
        }

        if (activityCfg.getStarTreasure().getRewardPoolCount() <= 0) {
            LogUtil.error("pid:{} activityId:{}.StarTreasure activity no have reward pool.", playerIdx, activityId);
            return false;
        }

        if (activityCfg.getStarTreasure().getCostItem().getId() <= 0) {
            LogUtil.error("pid:{} activityId:{} costItemId {}.StarTreasure cost item error.",
                    playerIdx, activityId,activityCfg.getStarTreasure().getCostItem().getId());
            return false;
        }

        List<ServerStarTreasureRewardPool> poolList = activityCfg.getStarTreasure().getRewardPoolList();
        for (ServerStarTreasureRewardPool pool : poolList) {
            if (pool.getItemsCount() <= 0) {
                LogUtil.error("pid:{} activityId:{} color {}.StarTreasure pool no have item.", playerIdx, activityId, pool.getColorValue());
                return false;
            }
        }


        return true;
    }

    private StarTreasureChooseItem updateChooseItemStatus(targetsystemEntity entity,StarTreasureReward gainItem,ServerPlatformStarTreasure starTreasureActivity){
        StarTreasureChooseItem gainChooseItem = null;
        ServerStarTreasureRewardPool gainPool = null;
        int gainIndex = -1;

        int getItemId = gainItem.getReward().getId();
        int getItemCount = gainItem.getReward().getCount();

        int gainColor = gainItem.getColor();

        List<ServerStarTreasureRewardPool> poolList = starTreasureActivity.getRewardPoolList();
        for (ServerStarTreasureRewardPool pool :poolList) {
            if(pool != null && pool.getColorValue() == gainColor){
                gainPool = pool;
                break;
            }
        }

        List<RandomReward> itemList = gainPool.getItemsList();
        for (int i = 0; i < itemList.size(); i++) {
            RandomReward cfgItem = itemList.get(i);
            if(cfgItem.getId() == getItemId && cfgItem.getCount() == getItemCount){
                gainIndex = i;
                break;
            }
        }
        LogUtil.info("StarTreasure chooseColor:{},index:{},item:\n{}",gainColor,gainIndex,gainItem.toString());
        List<StarTreasureChooseItem> chooseItemList = entity.getDb_Builder().getSpecialInfo().getStarTreasureActivity().getChooseItemsList();
        List<StarTreasureChooseItem> newChooseItemList = new ArrayList<>(chooseItemList.size());
        for (StarTreasureChooseItem chooseItem : chooseItemList) {
            if(chooseItem.getPoolColorValue() == gainColor && chooseItem.getPoolIndex() == gainIndex){
                StarTreasureChooseItem.Builder newItem = chooseItem.toBuilder();
                newItem.setStatus(ENUMStarTreasureItemStatus.STT_status_get);
                gainChooseItem = newItem.build();
                newChooseItemList.add(gainChooseItem);
            }else{
                newChooseItemList.add(chooseItem);
            }
        }

        SyncExecuteFunction.executeFunction(entity,e -> {
            DB_StarTreasureActivity.Builder starTreasureActivityBuilder = entity.getDb_Builder().getSpecialInfoBuilder().getStarTreasureActivityBuilder();
            starTreasureActivityBuilder.clearChooseItems();
            starTreasureActivityBuilder.addAllChooseItems(newChooseItemList);
            return RetCodeEnum.RCE_Success;
        });
        return gainChooseItem;
    }


    /////////////////////////////
    @Data
    private static class RandomPool {
        ENUMStarTreasureRewardPoolColor color;
        int poolOdds;
        List<RandomReward> randomItemList;

        protected RandomPool(ENUMStarTreasureRewardPoolColor color, int poolOdds) {
            this.color = color;
            this.poolOdds = poolOdds;
            this.randomItemList = new ArrayList<>();
        }

        public void addRandItem(RandomReward randItem){
            randomItemList.add(randItem);
        }
    }

    @Data
    private static class StarTreasureReward {
        int color;
        Reward reward;

        protected StarTreasureReward(int color, Reward reward) {
            this.color = color;
            this.reward = reward;
        }
    }
}
