package server.handler.activity;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Activity.CS_ShareActivity;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import util.GameUtil;

import java.util.List;

import static protocol.MessageId.MsgIdEnum.SC_ShareActivity_VALUE;


@MsgId(msgId = MsgIdEnum.CS_ShareActivity_VALUE)
public class ShareActivityHandler extends AbstractBaseHandler<CS_ShareActivity> {
    @Override
    protected CS_ShareActivity parse(byte[] bytes) throws Exception {
        return CS_ShareActivity.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ShareActivity req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        long activityId = req.getActivityId();
        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(activityId);

        RetCodeEnum codeEnum = playerShareActivity(activityCfg, playerIdx);
        if (codeEnum == RetCodeEnum.RCE_Success) {
            doShareReward(playerIdx, activityCfg);
        }
        Activity.SC_ShareActivity.Builder msg = Activity.SC_ShareActivity.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(codeEnum));
        gsChn.send(SC_ShareActivity_VALUE, msg);
    }

    private void doShareReward(String playerIdx, ServerActivity activityCfg) {
        if (activityCfg.getType() != Activity.ActivityTypeEnum.ATE_FestivalBoss) {
            return;
        }
        List<Common.Reward> shareRewardList = activityCfg.getFestivalBoss().getShareRewardList();
        RewardManager.getInstance().doRewardByList(playerIdx, shareRewardList, ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FestivalBoss, "分享"), true);
    }

    private RetCodeEnum playerShareActivity(ServerActivity activityCfg, String playerIdx) {
        if (!ActivityUtil.activityInOpen(activityCfg)) {
            return RetCodeEnum.RCE_Activity_NotOpen;
        }
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        //当前做的活动都只能分享一次
        if (target.getDb_Builder().getActivityShareTimesMap().get(activityCfg.getActivityId()) != null) {
            return RetCodeEnum.RCE_Activity_AlreadyShare;
        }

        SyncExecuteFunction.executeConsumer(target, cache -> {
            target.getDb_Builder().putActivityShareTimes(activityCfg.getActivityId(), target.getDb_Builder().getActivityShareTimesMap().getOrDefault(activityCfg.getActivityId(), 0) + 1);
        });
        return RetCodeEnum.RCE_Success;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.FestivalBoss;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_ShareActivity_VALUE, Activity.SC_ShareActivity.newBuilder().setRetCode(retCode));
    }
}
