package server.handler.activity.zeroCostPurchase;

import cfg.TimeLimitActivity;
import cfg.TimeLimitActivityObject;
import cfg.ZeroCostPurchaseCfg;
import cfg.ZeroCostPurchaseCfgObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityUtil;
import model.player.RewardRecordHelper;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Activity.SC_ClaimZeroCostPurchaseReward;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

/**
 * 零元购购买以及激活返利任务
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimZeroCostPurchaseReward_VALUE)
public class ClaimZeroCostPurchaseRewardHandler extends AbstractBaseHandler<Activity.CS_ClaimZeroCostPurchaseReward> {
    @Override
    protected Activity.CS_ClaimZeroCostPurchaseReward parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimZeroCostPurchaseReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimZeroCostPurchaseReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Activity.SC_ClaimZeroCostPurchaseReward.Builder result = Activity.SC_ClaimZeroCostPurchaseReward.newBuilder();

        playerEntity player = playerCache.getByIdx(playerIdx);
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null || player == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimZeroCostPurchaseReward_VALUE, result);
            return;
        }
        int purchaseId = req.getCfgId();
        TimeLimitActivityObject activityConfig = TimeLimitActivity.getById(ActivityUtil.LocalActivityId.ZeroCostPurchase);
        ZeroCostPurchaseCfgObject itemConfig = ZeroCostPurchaseCfg.getById(purchaseId);
        if (itemConfig == null || activityConfig == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_ConfigNotExist));
            gsChn.send(MsgIdEnum.SC_ClaimZeroCostPurchaseReward_VALUE, result);
            return;
        }
        Activity.ZeroCostPurchaseItem costPurchaseItem = target.getDb_Builder().getSpecialInfo().getZeroCostPurchase().getZeroCostPurchaseMap().get(purchaseId);
        int playerRewardRecordIndex = queryRewardRecordByPurchaseItem(purchaseId, costPurchaseItem);

        if (player.alreadyClaimed(ActivityUtil.LocalActivityId.ZeroCostPurchase, playerRewardRecordIndex)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_RewardAlreadyClaim));
            gsChn.send(MsgIdEnum.SC_ClaimZeroCostPurchaseReward_VALUE, result);
            return;
        }

        LogUtil.info("player:{} ClaimZeroCostPurchaseReward,purchaseId:{}", playerIdx, purchaseId);
        RetCodeEnum codeEnum = SyncExecuteFunction.executeFunction(target, entity -> {
            TargetSystemDB.DB_ZeroCostPurchase.Builder zeroCostPurchase = target.getDb_Builder().getSpecialInfoBuilder().getZeroCostPurchaseBuilder();
            Activity.ZeroCostPurchaseItem purchaseItem = zeroCostPurchase.getZeroCostPurchaseMap().get(purchaseId);

            LogUtil.info("player:{} ClaimZeroCostPurchaseReward purchaseItem:{} in Db", playerIdx, purchaseItem);
            if (!canClaimReward(purchaseItem)) {
                return RetCodeEnum.RCE_Activity_DissatisfyAddition;
            }

            int[][] delayReward = itemConfig.getDelayreward();
            int claimIndex = purchaseItem.getClaimIndex();
            if (completeClaim(purchaseItem, claimIndex, delayReward)) {
                return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
            }

            Activity.ZeroCostPurchaseItem.Builder newPurchaseItem = getNewPurchaseItem(purchaseItem, claimIndex, delayReward);

            LogUtil.info("player:{} ClaimZeroCostPurchaseReward putZeroCostPurchase purchaseItem:{} in Db", playerIdx, newPurchaseItem);

            zeroCostPurchase.putZeroCostPurchase(req.getCfgId(), newPurchaseItem.build());

            doReward(playerIdx, delayReward[claimIndex]);

            target.sendZeroCostPurchaseUpdate(purchaseId);
            return RetCodeEnum.RCE_Success;
        });

        if (codeEnum == RetCodeEnum.RCE_Success) {
            SyncExecuteFunction.executeConsumer(player, entity -> {
                player.increasePlayerRewardRecord(ActivityUtil.LocalActivityId.ZeroCostPurchase, playerRewardRecordIndex);
            });
        }

        result.setRetCode(GameUtil.buildRetCode(codeEnum));
        gsChn.send(MsgIdEnum.SC_ClaimZeroCostPurchaseReward_VALUE, result);
    }

    private boolean recordInPlayerClaimed(String playerIdx, int rewardRecordIndex) {
        return RewardRecordHelper.onceRewardClaimed(playerIdx, rewardRecordIndex);
    }

    private int queryRewardRecordByPurchaseItem(int purchaseId, Activity.ZeroCostPurchaseItem costPurchaseItem) {
        return purchaseId * 1000 + costPurchaseItem.getClaimIndex();
    }

    private void doReward(String playerIdx, int[] params) {
        Common.Reward reward = RewardUtil.parseReward(params);
        RewardManager.getInstance().doReward(playerIdx, reward, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ZeroCostPurchase), true);
    }

    private boolean completeClaim(Activity.ZeroCostPurchaseItem purchaseItem, int claimIndex, int[][] delayReward) {
        return purchaseItem.getClaimStatus() == Activity.ActivityClaimStatusEnum.ACS_Complete || claimIndex >= delayReward.length;
    }

    private Activity.ZeroCostPurchaseItem.Builder getNewPurchaseItem(Activity.ZeroCostPurchaseItem purchaseItem, int claimIndex, int[][] delayReward) {
        Activity.ZeroCostPurchaseItem.Builder builder = purchaseItem.toBuilder();
        boolean allClaim = purchaseItem.getClaimIndex() == delayReward.length - 1;
        if (allClaim) {
            builder.setClaimStatus(Activity.ActivityClaimStatusEnum.ACS_Complete);
        } else {
            builder.setNextClaimTime(TimeUtil.getNextDayResetTime(GlobalTick.getInstance().getCurrentTime()));
        }
        builder.setClaimIndex(claimIndex + 1);
        return builder;
    }

    private boolean canClaimReward(Activity.ZeroCostPurchaseItem purchaseItem) {
        return purchaseItem == null || purchaseItem.getClaimStatus() != Activity.ActivityClaimStatusEnum.ACS_Claiming
                || purchaseItem.getNextClaimTime() < GlobalTick.getInstance().getCurrentTime();
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ZeroCostPurchase;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimZeroCostPurchaseReward_VALUE, SC_ClaimZeroCostPurchaseReward.newBuilder().setRetCode(retCode));
    }
}
