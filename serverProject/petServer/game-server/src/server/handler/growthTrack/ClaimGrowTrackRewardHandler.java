package server.handler.growthTrack;

import cfg.FunctionOpenLvConfig;
import cfg.GrowthTrack;
import cfg.GrowthTrackObject;
import cfg.Mission;
import cfg.MissionObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_ClaimGrowTrackReward;
import protocol.TargetSystem.SC_ClaimGrowTrackReward;
import protocol.TargetSystem.SC_ClaimGrowTrackReward.Builder;
import protocol.TargetSystem.TargetMission;
import protocol.TargetSystemDB.DB_GrowthTrack;
import util.GameUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2020/06/30
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimGrowTrackReward_VALUE)
public class ClaimGrowTrackRewardHandler extends AbstractBaseHandler<CS_ClaimGrowTrackReward> {
    @Override
    protected CS_ClaimGrowTrackReward parse(byte[] bytes) throws Exception {
        return CS_ClaimGrowTrackReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimGrowTrackReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Builder resultBuilder = SC_ClaimGrowTrackReward.newBuilder();
        if (!PlayerUtil.queryFunctionUnlock(playerIdx, EnumFunction.GrowthTrack)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            gsChn.send(MsgIdEnum.SC_ClaimGrowTrackReward_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimGrowTrackReward_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_GrowthTrack.Builder trackBuilder = entity.getGrowthTrackBuilder();

            GrowthTrackObject trackConfig = GrowthTrack.getByMissionId(req.getMissionCfgId());
            if (trackConfig == null
                    || !trackBuilder.getCurMissionGroupIdsList().contains(trackConfig.getId())) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionUnfinished));
                gsChn.send(MsgIdEnum.SC_ClaimGrowTrackReward_VALUE, resultBuilder);
                return;
            }

            TargetMission targetMission = trackBuilder.getMissionsMap().get(req.getMissionCfgId());
            if (targetMission == null || targetMission.getStatus() == MissionStatusEnum.MSE_UnFinished) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionUnfinished));
                gsChn.send(MsgIdEnum.SC_ClaimGrowTrackReward_VALUE, resultBuilder);
                return;
            }

            if (targetMission.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_RewardAlreadyClaim));
                gsChn.send(MsgIdEnum.SC_ClaimGrowTrackReward_VALUE, resultBuilder);
                return;
            }

            MissionObject missionCfg = Mission.getById(req.getMissionCfgId());
            if (missionCfg == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
                gsChn.send(MsgIdEnum.SC_ClaimGrowTrackReward_VALUE, resultBuilder);
                return;
            }

            //修改状态
            TargetMission newBuild = targetMission.toBuilder()
                    .setStatus(MissionStatusEnum.MSE_FinishedAndClaim)
                    .build();

            trackBuilder.putMissions(newBuild.getCfgId(), newBuild);

            //发放奖励
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GrowthFund);
            RewardManager.getInstance().doRewardByList(playerIdx,
                    RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward()), reason, true);

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimGrowTrackReward_VALUE, resultBuilder);

            //判断是否解锁新的任务列
            TargetMission findUnclaimedMission = Arrays.stream(trackConfig.getMissionlist())
                    .mapToObj(ele -> trackBuilder.getMissionsMap().get(ele))
                    .filter(ele -> ele.getStatus() == MissionStatusEnum.MSE_UnFinished
                            || ele.getStatus() == MissionStatusEnum.MSE_Finished)
                    .findAny()
                    .orElse(null);
            if (findUnclaimedMission == null) {
                Set<Integer> newGroupIds = new HashSet<>(trackBuilder.getCurMissionGroupIdsList());
                newGroupIds.remove(trackConfig.getId());
                for (int nextGroupId : trackConfig.getNextmissiongroup()) {
                    newGroupIds.add(nextGroupId);
                }

                trackBuilder.clearCurMissionGroupIds();
                trackBuilder.addAllCurMissionGroupIds(newGroupIds);

                if (GrowthTrack.containLoginMission(newGroupIds)) {
                    trackBuilder.setNextCanUpdateCumuLoginTime(TimeUtil.getNextDayResetTime(GlobalTick.getInstance().getCurrentTime()));
                }
            }

            //刷新进度
            entity.sendRefreshGrowthTrackProgressMsg(Collections.singletonList(newBuild));
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.GrowthTrack;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimGrowTrackReward_VALUE, SC_ClaimGrowTrackReward.newBuilder().setRetCode(retCode));
    }
}
