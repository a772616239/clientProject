package server.handler.resRecycle;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.consume.ConsumeManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.ResourceCopy.SC_BuyTimes;
import protocol.ResourceRecycle;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimResourceRecycleReward_VALUE)
public class ClaimResourceCycleRewardHandler extends AbstractBaseHandler<ResourceRecycle.CS_ClaimResourceRecycleReward> {
    @Override
    protected ResourceRecycle.CS_ClaimResourceRecycleReward parse(byte[] bytes) throws Exception {
        return ResourceRecycle.CS_ClaimResourceRecycleReward.parseFrom(bytes);
    }

    private static final int baseClaim = 0;
    private static final int advancedClaim = 1;

    @Override
    protected void execute(GameServerTcpChannel gsChn, ResourceRecycle.CS_ClaimResourceRecycleReward req, int i) {
        ResourceRecycle.SC_ClaimResourceRecycleReward.Builder msg = ResourceRecycle.SC_ClaimResourceRecycleReward.newBuilder();
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        LogUtil.info("receive player:{} claim resource recycle rewards req:{}", playerIdx, req);
        if (player == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimResourceRecycleReward_VALUE, msg);
            return;
        }
        if (!checkParams(req)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimResourceRecycleReward_VALUE, msg);
            return;
        }

        int claimType = req.getClaimType();

        Common.Consume consume = player.getResourceCycleConsume(claimType);
        if (consume == null || consume.getCount() <= 0) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ResourceRecycle_NoRewardsCanClaim));
            gsChn.send(MsgIdEnum.SC_ClaimResourceRecycleReward_VALUE, msg);
            return;
        }

        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, ReasonManager.getInstance().borrowReason())) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
            gsChn.send(MsgIdEnum.SC_ClaimResourceRecycleReward_VALUE, msg);
            return;
        }
        LogUtil.info(" player:{} start claim resource recycle rewards ", playerIdx);
        SyncExecuteFunction.executeConsumer(player, entity -> {

            List<Common.Reward> rewards = getReward(player, claimType);

            player.getDb_data().getResourceRecycleBuilder().clear();

            ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_ResourceRecycle);

            RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);

            LogUtil.info(" player:{} claim resource recycle success rewards", playerIdx, rewards);
        });


        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimResourceRecycleReward_VALUE, msg);
    }

    private List<Common.Reward> getReward(playerEntity player, int claimType) {
        List<Common.Reward> fullRewards = player.queryResourceRecycleFullRewards();
        if (claimType == advancedClaim) {
            return fullRewards;
        }
        return RewardUtil.multiRewardsByPerThousand(fullRewards, GameConfig.getById(GameConst.CONFIG_ID).getResrecyclebaserewardrate());
    }


    private boolean checkParams(ResourceRecycle.CS_ClaimResourceRecycleReward req) {
        if (req.getClaimType() != 1 && req.getClaimType() != 0) {
            return false;
        }
        return true;

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ResCopy;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, SC_BuyTimes.newBuilder().setRetCode(retCode));
    }
}
