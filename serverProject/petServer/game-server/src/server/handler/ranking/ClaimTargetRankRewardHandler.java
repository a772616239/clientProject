package server.handler.ranking;

import cfg.RankRewardTargetConfig;
import cfg.RankRewardTargetConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.util.PlayerUtil;
import model.ranking.ranking.RankingTargetManager;
import model.redpoint.RedPointManager;
import model.redpoint.RedPointOptionEnum;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.*;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

import static protocol.RedPointIdEnum.RedPointId.RP_HONORWALL_VALUE;

@MsgId(msgId = protocol.MessageId.MsgIdEnum.CS_ClaimRankingTargetReward_VALUE)
public class ClaimTargetRankRewardHandler extends AbstractBaseHandler<Activity.CS_ClaimRankingTargetReward> {
    @Override
    protected Activity.CS_ClaimRankingTargetReward parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimRankingTargetReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimRankingTargetReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Activity.SC_ClaimRankingTargetReward.Builder msg = Activity.SC_ClaimRankingTargetReward.newBuilder();
        if (!PlayerUtil.queryFunctionUnlock(playerIdx, Common.EnumFunction.EF_RankingEntrance)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_LvNotEnough));
            gsChn.send(protocol.MessageId.MsgIdEnum.SC_ClaimRankingTargetReward_VALUE, msg);
            return;
        }
        LogUtil.info("playerIdx:{} ClaimTargetRankReward begin,req:{}", playerIdx, req);
        int targetRewardId = req.getTargetRewardId();
        RankRewardTargetConfigObject config = RankRewardTargetConfig.getById(targetRewardId);


        if (config == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            gsChn.send(protocol.MessageId.MsgIdEnum.SC_ClaimRankingTargetReward_VALUE, msg);
            return;
        }

        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            gsChn.send(protocol.MessageId.MsgIdEnum.SC_ClaimRankingTargetReward_VALUE, msg);
            return;
        }
        RetCodeId.RetCodeEnum retCodeEnum = SyncExecuteFunction.executeFunction(target, entity -> {

            RetCodeId.RetCodeEnum codeEnum = canClaimReward(target, targetRewardId);
            msg.setRetCode(GameUtil.buildRetCode(codeEnum));

            if (codeEnum != RetCodeId.RetCodeEnum.RCE_Success) {
                gsChn.send(MessageId.MsgIdEnum.SC_ClaimRankingTargetReward_VALUE, msg);
                return codeEnum;
            }

            updateCache(target, targetRewardId);

            doReward(playerIdx, config);

            gsChn.send(MessageId.MsgIdEnum.SC_ClaimRankingTargetReward_VALUE, msg);

            LogUtil.info("playerIdx:{} ClaimTargetRankReward finished ,req:{},claimedIds:{}", playerIdx, req, target.getDb_Builder().getClaimedRankTargetRewardList());
            return codeEnum;
        });
        if (RetCodeId.RetCodeEnum.RCE_Success==retCodeEnum){
            checkAndClearRedPoint(playerIdx);
        }

    }

    private void checkAndClearRedPoint(String playerIdx) {
        boolean canClaimReward = RankingTargetManager.getInstance().canPlayerClaimRankTargetReward(playerIdx);
        if (!canClaimReward) {
            RedPointManager.getInstance().redPointReadClear(playerIdx, RP_HONORWALL_VALUE);
            RedPointManager.getInstance().sendRedPoint(playerIdx, RP_HONORWALL_VALUE, RedPointOptionEnum.REMOVE);
        }
    }

    private void updateCache(targetsystemEntity target, int targetRewardId) {
        target.getDb_Builder().addClaimedRankTargetReward(targetRewardId);
    }

    private void doReward(String playerIdx, RankRewardTargetConfigObject config) {
        List<Common.Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(config.getReward());
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_TargetRank), true);
        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_HonorWall_CumuClaimReward, 1, 0);
    }

    private RetCodeId.RetCodeEnum canClaimReward(targetsystemEntity target, int targetRewardId) {
        List<Integer> claimedList = target.getDb_Builder().getClaimedRankTargetRewardList();
        //已领取
        if (claimedList.contains(targetRewardId)) {
            return RetCodeId.RetCodeEnum.RCE_Target_MissionAlreadyClaim;
        }
        //未解锁
        if (!RankingTargetManager.getInstance().targetRewardUnLock(targetRewardId)) {
            return RetCodeId.RetCodeEnum.RCE_Target_MissionUnfinished;
        }
        LogUtil.info("player:{} ClaimTargetRankReward canClaimReward,now claimedList:{}", target.getLinkplayeridx(), claimedList);

        return RetCodeId.RetCodeEnum.RCE_Success;
    }


    @Override
    public protocol.Common.EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }
}