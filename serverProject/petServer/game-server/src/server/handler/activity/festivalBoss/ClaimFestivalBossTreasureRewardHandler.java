package server.handler.activity.festivalBoss;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Activity.CS_ClaimFestivalBossTreasureReward;
import protocol.Activity.SC_ClaimFestivalBossTreasureReward;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;


@MsgId(msgId = MsgIdEnum.CS_ClaimFestivalBossTreasureReward_VALUE)
public class ClaimFestivalBossTreasureRewardHandler extends AbstractBaseHandler<CS_ClaimFestivalBossTreasureReward> {
    @Override
    protected CS_ClaimFestivalBossTreasureReward parse(byte[] bytes) throws Exception {
        return CS_ClaimFestivalBossTreasureReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimFestivalBossTreasureReward req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        SC_ClaimFestivalBossTreasureReward.Builder msg = SC_ClaimFestivalBossTreasureReward.newBuilder();

        RetCodeEnum codeEnum = claimTreasureReward(playerIdx, req.getActivityId(), req.getClaimId());

        msg.setRetCode(GameUtil.buildRetCode(codeEnum));

        gsChn.send(MsgIdEnum.SC_ClaimFestivalBossTreasureReward_VALUE, msg);
    }

    private RetCodeEnum claimTreasureReward(String playerIdx, long activityId, int claimId) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        Server.ServerActivity activity = ActivityManager.getInstance().getActivityCfgById(activityId);
        if (!ActivityUtil.activityInOpen(activity)) {
            return RetCodeEnum.RCE_Activity_NotOpen;
        }
        return SyncExecuteFunction.executeFunction(entity, cache -> {
            TargetSystemDB.DB_FestivalBoss dbFestivalBoss = entity.getDb_Builder().getFestivalBossInfoMap().getOrDefault(activityId, TargetSystemDB.DB_FestivalBoss.getDefaultInstance());
            if (dbFestivalBoss.getClaimedTreasureIdsList().contains(claimId)) {
                return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
            }
            Activity.FestivalBossTreasure treasureCfg = activity.getFestivalBoss().getTreasuresList().stream().filter(e -> e.getId() == claimId).findAny().orElse(null);

            if (treasureCfg == null) {
                return RetCodeEnum.RCE_Target_MissionCfgIdNotExist;
            }

            if (treasureCfg.getTarget() > dbFestivalBoss.getPresentTimes()) {
                return RetCodeEnum.RCE_Activity_DissatisfyAddition;

            }

            entity.getDb_Builder().putFestivalBossInfo(activityId, dbFestivalBoss.toBuilder().addClaimedTreasureIds(claimId).build());

            ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FestivalBoss, "节日boss宝箱奖励");
            RewardManager.getInstance().doRewardByList(playerIdx, treasureCfg.getRewardList(), reason, true);
            entity.sendFestivalBossInfoUpdate(playerIdx, activityId);
            LogUtil.info("player:{} success claim festivalBoss TreasureReward, activityid:{} claimId:{}", playerIdx, activityId, claimId);
            return RetCodeEnum.RCE_Success;
        });

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.FestivalBoss;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimFestivalBossTreasureReward_VALUE, SC_ClaimFestivalBossTreasureReward.newBuilder().setRetCode(retCode));
    }
}
