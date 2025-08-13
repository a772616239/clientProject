package server.handler.activity.demonDescends;

import cfg.DemonDescendsConfig;
import cfg.DemonDescendsConfigObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_BuyDemonDescendsItem;
import protocol.Activity.SC_BuyDemonDescendsItem;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB.DB_DemonDescendsActivityInfo;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.10.08
 */
@MsgId(msgId = MsgIdEnum.CS_BuyDemonDescendsItem_VALUE)
public class BuyDemonDescendsItemHandler extends AbstractBaseHandler<CS_BuyDemonDescendsItem> {
    @Override
    protected CS_BuyDemonDescendsItem parse(byte[] bytes) throws Exception {
        return CS_BuyDemonDescendsItem.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyDemonDescendsItem req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_BuyDemonDescendsItem.Builder resultBuilder = SC_BuyDemonDescendsItem.newBuilder();
        RetCodeEnum retCode = doFunction(playerIdx, req);
        resultBuilder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.SC_BuyDemonDescendsItem_VALUE, resultBuilder);
    }

    private RetCodeEnum doFunction(String playerIdx, CS_BuyDemonDescendsItem req) {
        if (playerIdx == null || req == null || req.getBuyCount() <= 0) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        ServerActivity activity = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        if (activity == null || activity.getType() != ActivityTypeEnum.ATE_DemonDescends
                || GameUtil.outOfScope(activity.getBeginTime(), activity.getEndTime(), GlobalTick.getInstance().getCurrentTime())) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        DemonDescendsConfigObject descendsConfig = DemonDescendsConfig.getById(GameConst.CONFIG_ID);

        int canBuyCount = SyncExecuteFunction.executeFunction(entity, e -> {
            DB_DemonDescendsActivityInfo.Builder infoBuilder = e.getDemonDescendsInfoBuilder(req.getActivityId());
            return descendsConfig.getBuyupperlimit() - infoBuilder.getAlreadyBugCount();
        });

        if (req.getBuyCount() > canBuyCount) {
            return RetCodeEnum.RCE_DemonDescends_ItemBuyLimit;
        }

        Consume consume = ConsumeUtil.parseAndMulti(descendsConfig.getPrice(), req.getBuyCount());
        Reward.Builder rewardBuilder = RewardUtil.parseRewardBuilder(descendsConfig.getDrawuseitem());
        if (consume == null || rewardBuilder == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DemonDescends_Buy);
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
            return RetCodeEnum.RCE_MatieralNotEnough;
        }

        RewardManager.getInstance().doReward(playerIdx, rewardBuilder.setCount(req.getBuyCount()).build(), reason, true);

        //修改购买记录
        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_DemonDescendsActivityInfo.Builder infoBuilder = e.getDemonDescendsInfoBuilder(req.getActivityId());
            infoBuilder.setAlreadyBugCount(infoBuilder.getAlreadyBugCount() + req.getBuyCount());
            entity.putDemonDescendsInfoBuilder(infoBuilder);
        });

        entity.refreshDemonDescendsActivityInfo(req.getActivityId());
        return RetCodeEnum.RCE_Success;
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
