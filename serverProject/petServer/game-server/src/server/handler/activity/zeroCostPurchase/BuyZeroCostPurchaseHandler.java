package server.handler.activity.zeroCostPurchase;

import cfg.TimeLimitActivity;
import cfg.TimeLimitActivityObject;
import cfg.ZeroCostPurchaseCfg;
import cfg.ZeroCostPurchaseCfgObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityUtil;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Activity.SC_BuyZeroCostPurchase;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;

/**
 * 零元购购买以及激活返利任务
 */
@MsgId(msgId = MsgIdEnum.CS_BuyZeroCostPurchase_VALUE)
public class BuyZeroCostPurchaseHandler extends AbstractBaseHandler<Activity.CS_BuyZeroCostPurchase> {
    @Override
    protected Activity.CS_BuyZeroCostPurchase parse(byte[] bytes) throws Exception {
        return Activity.CS_BuyZeroCostPurchase.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_BuyZeroCostPurchase req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Activity.SC_BuyZeroCostPurchase.Builder result = Activity.SC_BuyZeroCostPurchase.newBuilder();
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        LogUtil.info("receive player:{} buy zeroCostPurchase,req:{}");
        if (target == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BuyZeroCostPurchase_VALUE, result);
            return;
        }
        int purchaseId = req.getCfgId();
        TimeLimitActivityObject activityConfig = TimeLimitActivity.getById(ActivityUtil.LocalActivityId.ZeroCostPurchase);
        ZeroCostPurchaseCfgObject itemConfig = ZeroCostPurchaseCfg.getById(purchaseId);
        if (itemConfig == null || activityConfig == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_ConfigNotExist));
            gsChn.send(MsgIdEnum.SC_BuyZeroCostPurchase_VALUE, result);
            return;
        }
        if (!playerReachLv(playerIdx, activityConfig)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_BuyZeroCostPurchase_VALUE, result);
            return;
        }
        if (alreadyPurchase(purchaseId, target)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_RepeatedBuy));
            gsChn.send(MsgIdEnum.SC_BuyZeroCostPurchase_VALUE, result);
            return;
        }

        Common.Consume consume = ConsumeUtil.parseConsume(itemConfig.getConsume());
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_ZeroCostPurchase))) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
            gsChn.send(MsgIdEnum.SC_BuyZeroCostPurchase_VALUE, result);
            return;
        }
        SyncExecuteFunction.executeConsumer(target, entity -> addPurchaseRecord(purchaseId, target));

        LogUtil.info("player:{} buy zeroCostPurchase success,purchaseId:{}", playerIdx, playerIdx);

        target.sendZeroCostPurchaseUpdate(purchaseId);

        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_BuyZeroCostPurchase_VALUE, result);

        doBuyInstantReward(playerIdx, itemConfig);

    }

    private boolean playerReachLv(String playerIdx, TimeLimitActivityObject activityConfig) {
        return PlayerUtil.queryPlayerLv(playerIdx) >= activityConfig.getOpenlv();
    }

    private void doBuyInstantReward(String playerIdx, ZeroCostPurchaseCfgObject itemConfig) {
        Common.Reward reward = RewardUtil.parseReward(itemConfig.getInstantreward());
        RewardManager.getInstance().doReward(playerIdx, reward, ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_ZeroCostPurchase), true);
    }


    private void addPurchaseRecord(int purchaseId, targetsystemEntity target) {
        TargetSystemDB.DB_SpecialActivity.Builder specialInfoBuilder = target.getDb_Builder().getSpecialInfoBuilder();
        Activity.ZeroCostPurchaseItem purchaseRecord = Activity.ZeroCostPurchaseItem.newBuilder().setClaimStatus(Activity.ActivityClaimStatusEnum.ACS_Claiming).setPurchaseCfg(purchaseId).build();
        specialInfoBuilder.getZeroCostPurchaseBuilder().putZeroCostPurchase(purchaseId, purchaseRecord);
    }

    private boolean alreadyPurchase(int purchaseId, targetsystemEntity target) {
        return target.getDb_Builder().getSpecialInfo().getZeroCostPurchase().getZeroCostPurchaseMap().containsKey(purchaseId);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ZeroCostPurchase;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyZeroCostPurchase_VALUE, SC_BuyZeroCostPurchase.newBuilder().setRetCode(retCode));
    }
}
