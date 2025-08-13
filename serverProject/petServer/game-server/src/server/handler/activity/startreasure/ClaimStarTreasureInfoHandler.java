package server.handler.activity.startreasure;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_ClaimStarTreasure;
import protocol.Activity.ClientStarTreasureInfo;
import protocol.Activity.ClientStarTreasureRewardPool;
import protocol.Activity.SC_ClaimStarTreasure;
import protocol.Common.EnumFunction;
import protocol.Common.RandomReward;
import protocol.Common.Reward;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.Server.ServerPlatformStarTreasure;
import protocol.Server.ServerStarTreasureRewardPool;
import protocol.TargetSystemDB;
import protocol.TargetSystemDB.DB_StarTreasureActivity;
import util.GameUtil;
import util.LogUtil;

import java.util.List;

/**
 * 获取星星宝藏活动信息
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimStarTreasure_VALUE)
public class ClaimStarTreasureInfoHandler extends AbstractBaseHandler<CS_ClaimStarTreasure> {
    @Override
    protected CS_ClaimStarTreasure parse(byte[] bytes) throws Exception {
        return CS_ClaimStarTreasure.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimStarTreasure req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        SC_ClaimStarTreasure.Builder resultBuilder = SC_ClaimStarTreasure.newBuilder();
        if (!ActivityUtil.activityNeedDis(activityCfg)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_ClaimStarTreasure_VALUE, resultBuilder);
            return;
        }

        if (activityCfg.getType() != ActivityTypeEnum.ATE_StarTreasure) {
            LogUtil.error("pid:{} activityId:{} cfgType={}.activity type error.",playerIdx,req.getActivityId(),activityCfg.getType());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimStarTreasure_VALUE, resultBuilder);
            return;
        }

        if (!activityCfg.hasStarTreasure()) {
            LogUtil.error("pid:{} activityId:{}.have activity but have detail info.",playerIdx,req.getActivityId());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimStarTreasure_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimStarTreasure_VALUE, resultBuilder);
            return;
        }

        this.buildClientMsg(resultBuilder,entity,activityCfg);
        gsChn.send(MsgIdEnum.SC_ClaimStarTreasure_VALUE, resultBuilder);
    }


    private void buildClientMsg(SC_ClaimStarTreasure.Builder resultBuilder, targetsystemEntity entity, ServerActivity activityCfg) {
        TargetSystemDB.DB_SpecialActivity specialActivity = entity.getDb_Builder().getSpecialInfo();
        if(specialActivity.hasStarTreasureActivity()){
            DB_StarTreasureActivity dbStarTreasureActivity = specialActivity.getStarTreasureActivity();
            if(dbStarTreasureActivity.getChooseItemsCount() > 0){
                resultBuilder.addAllChooseItems(dbStarTreasureActivity.getChooseItemsList());
            }
        }

        ServerPlatformStarTreasure platformStarTreasure = activityCfg.getStarTreasure();
        ClientStarTreasureInfo reStarTreasureInfo = this.buildClientInfoFromServerInfo(platformStarTreasure);
        resultBuilder.setStarTreasureInfo(reStarTreasureInfo);

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
    }


    private ClientStarTreasureInfo buildClientInfoFromServerInfo(ServerPlatformStarTreasure serverStarTreasure){
        if(serverStarTreasure == null){
            return null;
        }

        ClientStarTreasureInfo.Builder clientInfo = ClientStarTreasureInfo.newBuilder();
        clientInfo.setCostItem(serverStarTreasure.getCostItem());
        clientInfo.addAllCostInfo(serverStarTreasure.getCostInfoList());

        List<ServerStarTreasureRewardPool> serverPoolList = serverStarTreasure.getRewardPoolList();
        for (ServerStarTreasureRewardPool serverPool: serverPoolList) {
            clientInfo.addRewardPool(this.serverPoolToClientPool(serverPool));
        }
        return clientInfo.build();
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StarTreasure;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

    private ClientStarTreasureRewardPool serverPoolToClientPool(ServerStarTreasureRewardPool serverPool){
        ClientStarTreasureRewardPool.Builder rePool = ClientStarTreasureRewardPool.newBuilder();
        rePool.setColor(serverPool.getColor());
        rePool.setName(serverPool.getName());
        rePool.setChooseLimit(serverPool.getChooseLimit());

        for (RandomReward randomReward : serverPool.getItemsList()) {
            rePool.addItems(this.randomRewardToReward(randomReward));
        }
        return rePool.build();
    }

    private Reward randomRewardToReward(RandomReward randomReward){
        Reward.Builder rewardBuilder = Reward.newBuilder();
        rewardBuilder.setId(randomReward.getId());
        rewardBuilder.setRewardType(randomReward.getRewardType());
        rewardBuilder.setCount(randomReward.getCount());

        return rewardBuilder.build();
    }
}
