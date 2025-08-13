package server.handler.activity.novice;

import cfg.NoviceCredit;
import cfg.NoviceCreditObject;
import cfg.NoviceTask;
import cfg.NoviceTaskObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;

import java.util.ArrayList;
import java.util.List;

import model.activity.ActivityUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity.CS_ClaimNoviceReward;
import protocol.Activity.SC_ClaimNoviceReward;
import protocol.Activity.SC_RefreshNovicePro;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetMission;
import protocol.TargetSystemDB.DB_NoviceCredit;
import protocol.TargetSystemDB.DB_TargetSystem.Builder;
import util.GameUtil;
import util.LogUtil;

import static protocol.RetCodeId.RetCodeEnum.*;

@MsgId(msgId = MsgIdEnum.CS_ClaimNoviceReward_VALUE)
public class ClaimNoviceRewardHandler extends AbstractBaseHandler<CS_ClaimNoviceReward> {
    @Override
    protected CS_ClaimNoviceReward parse(byte[] bytes) throws Exception {
        return CS_ClaimNoviceReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimNoviceReward req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        SC_ClaimNoviceReward.Builder resultBuilder = SC_ClaimNoviceReward.newBuilder();
        //type = 1,普通任务. type = 2,积分任务
        int type = req.getType();
        int id = req.getId();
        if ((type != 1 && type != 2)
                || (type == 1 && NoviceTask.getById(id) == null)
                || (type == 2 && NoviceCredit.getByPoints(id) == null)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimNoviceReward_VALUE, resultBuilder);
            return;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);

        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null || player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimNoviceReward_VALUE, resultBuilder);
            return;
        }
        if (player.alreadyClaimed(ActivityUtil.LocalActivityId.SevenDayTarget, getIndexInPlayer(req.getType(), id))) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_RewardAlreadyClaim));
            gsChn.send(MsgIdEnum.SC_ClaimNoviceReward_VALUE, resultBuilder);
            return;
        }

        SC_RefreshNovicePro.Builder refresh = SC_RefreshNovicePro.newBuilder();
        List<Reward> rewards = new ArrayList<>();
        RetCodeEnum codeEnum = SyncExecuteFunction.executeFunction(target, t -> {
            Builder db_builder = target.getDb_Builder();
            if (db_builder == null) {
                return RCE_UnknownError;
            }

            String reasonStr;
            DB_NoviceCredit.Builder novice = db_builder.getSpecialInfoBuilder().getNoviceBuilder();
            if (type == 1) {
                NoviceTaskObject byId = NoviceTask.getById(id);
                if (byId == null) {
                    return RCE_UnknownError;
                }

                if (!targetsystemEntity.timeInScope(novice.getStartTime(), byId.getOpenday(), byId.getEnddisplay())) {
                    LogUtil.info("player:{} claim novice reward time not match ,startTime:{} ,openDay:{},displayDay:{}",playerIdx,novice.getStartTime(),byId.getOpenday(),byId.getEnddisplay());
                    return RCE_Activity_MissionOutOfTime;
                }

                TargetMission missionPro = novice.getMissionProMap().get(id);
                TargetMission.Builder builder;
                if (missionPro == null) {
                    builder = TargetMission.newBuilder().setCfgId(id);
                } else {
                    builder = missionPro.toBuilder();
                }

                if (builder.getStatus() != MissionStatusEnum.MSE_Finished) {
                    return RCE_Activity_MissionCanNotClaim;
                }
                builder.setStatus(MissionStatusEnum.MSE_FinishedAndClaim);
                novice.putMissionPro(id, builder.build());

                List<Reward> rewards1 = RewardUtil.parseRewardIntArrayToRewardList(byId.getFinishreward());
                if (rewards1 != null) {
                    rewards.addAll(rewards1);
                }
                novice.setCurPoint(novice.getCurPoint() + byId.getPointreward());

                refresh.addNewPro(builder);

                reasonStr = "日:" + byId.getOpenday();
            } else {
                NoviceCreditObject byPoints = NoviceCredit.getByPoints(id);
                if (byPoints == null) {
                    return RCE_ErrorParam;
                }

                if (novice.getClaimRewardList().contains(id)) {
                    return RCE_Activity_RewardAlreadyClaim;
                }

                if (novice.getCurPoint() < byPoints.getPoints()) {
                    return RCE_Activity_MissionCanNotClaim;
                }

                Reward reward = RewardUtil.parseReward(byPoints.getAward());
                if (reward != null) {
                    rewards.add(reward);
                }
                novice.addClaimReward(byPoints.getPoints());

                reasonStr = "积分:" + byPoints.getPoints();
            }

            if (!rewards.isEmpty()) {
                RewardManager.getInstance().doRewardByList(target.getLinkplayeridx(), rewards,
                        ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Novice, reasonStr), true);
            }


            refresh.addAllClaimedReward(novice.getClaimRewardList());
            refresh.setCurPoint(novice.getCurPoint());
            gsChn.send(MsgIdEnum.SC_RefreshNovicePro_VALUE, refresh);
            return RCE_Success;
        });
        if (codeEnum == RCE_Success) {
            player.increasePlayerRewardRecord(ActivityUtil.LocalActivityId.SevenDayTarget, getIndexInPlayer(req.getType(), id));
        }
        resultBuilder.setRetCode(GameUtil.buildRetCode(codeEnum));
        gsChn.send(MsgIdEnum.SC_ClaimNoviceReward_VALUE, resultBuilder);
    }

    private int getIndexInPlayer(int type, int id) {
        return type * 100 + id;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Novice;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimNoviceReward_VALUE, SC_ClaimNoviceReward.newBuilder().setRetCode(retCode));
    }
}
