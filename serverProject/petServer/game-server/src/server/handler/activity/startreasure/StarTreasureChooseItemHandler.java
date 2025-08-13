package server.handler.activity.startreasure;

import common.AbstractBaseHandler;
import common.GameConst.StarTreasureConstant;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import lombok.Data;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_ChooseStarTreasureItem;
import protocol.Activity.ENUMStarTreasureItemStatus;
import protocol.Activity.ENUMStarTreasureRewardPoolColor;
import protocol.Activity.SC_ChooseStarTreasureItem;
import protocol.Activity.StarTreasureChooseItem;
import protocol.Common.EnumFunction;
import protocol.Common.RandomReward;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.Server.ServerStarTreasureRewardPool;
import protocol.TargetSystemDB;
import protocol.TargetSystemDB.DB_StarTreasureActivity;
import util.GameUtil;
import util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选择星星宝藏自定义奖池
 */
@MsgId(msgId = MsgIdEnum.CS_ChooseStarTreasureItem_VALUE)
public class StarTreasureChooseItemHandler extends AbstractBaseHandler<CS_ChooseStarTreasureItem> {

    @Override
    protected CS_ChooseStarTreasureItem parse(byte[] bytes) throws Exception {
        return CS_ChooseStarTreasureItem.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ChooseStarTreasureItem req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        SC_ChooseStarTreasureItem.Builder resultBuilder = SC_ChooseStarTreasureItem.newBuilder();
        if (!ActivityUtil.activityInOpen(activityCfg)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_ChooseStarTreasureItem_VALUE, resultBuilder);
            return;
        }

        if(!this.isRightCfg(playerIdx,req.getActivityId(),activityCfg)){
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
            gsChn.send(MsgIdEnum.SC_ChooseStarTreasureItem_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ChooseStarTreasureItem_VALUE, resultBuilder);
            return;
        }

        if(this.isChooseItem(entity)){
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.REC_StarTreasure_choosen));
            gsChn.send(MsgIdEnum.SC_ChooseStarTreasureItem_VALUE, resultBuilder);
            return;
        }

        RetCodeEnum chooseRetCode = this.isRightChoose(playerIdx,req.getChooseItemsList(),activityCfg.getStarTreasure().getRewardPoolList());
        if(chooseRetCode.getNumber() != RetCodeEnum.RCE_Success_VALUE){
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ChooseStarTreasureItem_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> addChooseItemToEntity(entity,req.getChooseItemsList()));

        resultBuilder.setRetCode(GameUtil.buildRetCode(chooseRetCode));
        gsChn.send(MsgIdEnum.SC_ChooseStarTreasureItem_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StarTreasure;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }


    private boolean isChooseItem(targetsystemEntity entity){
        TargetSystemDB.DB_SpecialActivity specialActivity = entity.getDb_Builder().getSpecialInfo();
        if(!specialActivity.hasStarTreasureActivity()){
            return false;
        }
        DB_StarTreasureActivity dbStarTreasure = specialActivity.getStarTreasureActivity();
        return dbStarTreasure.getChooseItemsCount() > 0;
    }

    private void addChooseItemToEntity(targetsystemEntity entity,List<StarTreasureChooseItem> chooseItemList){
        for (StarTreasureChooseItem item: chooseItemList) {
            item.toBuilder().setStatus(ENUMStarTreasureItemStatus.STT_status_choose);
        }

        DB_StarTreasureActivity.Builder dbStarTreasureBuilder = entity.getDb_Builder().getSpecialInfoBuilder().getStarTreasureActivityBuilder();
        dbStarTreasureBuilder.clearChooseItems();
        dbStarTreasureBuilder.addAllChooseItems(chooseItemList);
    }

    private RetCodeEnum isRightChoose(String playerIdx,List<StarTreasureChooseItem> chooseItemList, List<ServerStarTreasureRewardPool> rewardPoolList){
        if(chooseItemList == null){
            LogUtil.info("playerIdx:{} choose item list is null.",playerIdx);
            return RetCodeEnum.RCE_ErrorParam;
        }

        if(chooseItemList.size() != StarTreasureConstant.MAX_ITEM_COUNT){
            LogUtil.info("playerIdx:{} count:{},choose item size error.",playerIdx,chooseItemList.size());
            return RetCodeEnum.RCE_ErrorParam;
        }

        Map<Integer, RewardPoolShortInfo> cfgPoolShortInfoMap = this.cfgPoolItemListToMap(rewardPoolList);
        if(cfgPoolShortInfoMap == null){
            return RetCodeEnum.RCE_ConfigError;
        }

        Map<Integer,Integer> poolChooseCountMap = new HashMap<>();
        for (StarTreasureChooseItem item: chooseItemList) {
            if(item == null){
                return RetCodeEnum.RCE_ErrorParam;
            }

            RewardPoolShortInfo poolShortInfo = cfgPoolShortInfoMap.get(item.getPoolColorValue());
            if(poolShortInfo == null){
                LogUtil.info("playerIdx:{} chooseColor:{},not find reward pool.",playerIdx,item.getPoolColor());
                return RetCodeEnum.RCE_ErrorParam;
            }

            Integer poolChooseItemCount = poolChooseCountMap.get(item.getPoolColorValue());

            if(poolChooseItemCount != null && poolChooseItemCount >= poolShortInfo.getChooseLimit()){
                LogUtil.info("playerIdx:{} chooseColor:{},count out limit.", playerIdx,item.getPoolColor());
                return RetCodeEnum.RCE_ErrorParam;
            }
            if(poolChooseItemCount == null){
                poolChooseItemCount = 0;
            }


            if(item.getPoolIndex() >= poolShortInfo.getItemCount()){
                LogUtil.info("playerIdx:{} chooseColor:{} index:{},cfgCount:{},index out size.",
                        playerIdx,item.getPoolColor(),item.getPoolIndex(),poolShortInfo.getItemCount());
                return RetCodeEnum.RCE_ErrorParam;
            }
            poolChooseCountMap.put(item.getPoolColorValue(), ++poolChooseItemCount);
        }

        return RetCodeEnum.RCE_Success;
    }

    private Map<Integer, RewardPoolShortInfo> cfgPoolItemListToMap(List<ServerStarTreasureRewardPool> rewardPoolList){
        Map<Integer, RewardPoolShortInfo> poolShortInfoMap  = new HashMap<>();
        for (ServerStarTreasureRewardPool pool:rewardPoolList) {
            if(pool == null){
                LogUtil.error("StarTreasure have null reward pool");
                return null;
            }
            if(pool.getItemsCount() <= 0){
                LogUtil.error("StarTreasure color:{}  pool have null reward item list",pool.getColorValue());
                return null;
            }
            List<RandomReward> rewardList = pool.getItemsList();
            int itemCount = 0;
            for (RandomReward item : rewardList) {
                if(item == null || item.getId() <= 0){
                    LogUtil.error("StarTreasure color:{}  pool have null reward item",pool.getColor().getNumber());
                    return null;
                }
                ++itemCount;
            }
            poolShortInfoMap.put(pool.getColorValue(),new RewardPoolShortInfo(pool.getColor(),pool.getChooseLimit(),itemCount));
        }
        return poolShortInfoMap;
    }



    private boolean isRightCfg(String playerIdx,long activityId,ServerActivity activityCfg){
        if (activityCfg.getType() != ActivityTypeEnum.ATE_StarTreasure) {
            LogUtil.error("pid:{} activityId:{} cfgType={}.activity type error.",playerIdx,activityId,activityCfg.getType());
            return false;
        }
        if(!activityCfg.hasStarTreasure()){
            LogUtil.error("pid:{} activityId:{}.have activity but have detail info.",playerIdx,activityId);
            return false;
        }

        if(activityCfg.getStarTreasure().getRewardPoolCount() <= 0){
            LogUtil.error("pid:{} activityId:{}.StarTreasure activity no have reward pool.",playerIdx,activityId);
            return false;
        }

        return true;
    }


    /////////////////////////////
    @Data
    private static class RewardPoolShortInfo{
        ENUMStarTreasureRewardPoolColor color;
        int chooseLimit;
        int itemCount;

        public RewardPoolShortInfo(ENUMStarTreasureRewardPoolColor color, int chooseLimit, int itemCount) {
            this.color = color;
            this.chooseLimit = chooseLimit;
            this.itemCount = itemCount;
        }
    }
}
