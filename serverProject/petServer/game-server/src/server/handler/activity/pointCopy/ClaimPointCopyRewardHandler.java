package server.handler.activity.pointCopy;

import cfg.PointCopyCfgObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.activity.PointCopyManager;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity.CS_ClaimPointCopyReward;
import protocol.Activity.SC_ClaimPointCopyReward;
import protocol.Activity.SC_ClaimPointCopyReward.Builder;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB.DB_PointCopy;
import protocol.TargetSystemDB.DB_TargetSystem;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.03.06
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimPointCopyReward_VALUE)
public class ClaimPointCopyRewardHandler extends AbstractBaseHandler<CS_ClaimPointCopyReward> {
    @Override
    protected CS_ClaimPointCopyReward parse(byte[] bytes) throws Exception {
        return CS_ClaimPointCopyReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimPointCopyReward req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);

        Builder resultBuilder = SC_ClaimPointCopyReward.newBuilder();
        PointCopyCfgObject mission = PointCopyManager.getInstance().getRewardMissionById(req.getIndex());
        if (target == null || target.getDb_Builder() == null || mission == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimPointCopyReward_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(target, t -> {
            DB_TargetSystem.Builder db_builder = target.getDb_Builder();
            DB_PointCopy.Builder pointCopyBuilder = db_builder.getSpecialInfoBuilder().getPointCopyBuilder();
            if (pointCopyBuilder.getClaimRewardMissionIdList().contains(req.getIndex())) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_RewardAlreadyClaim));
                gsChn.send(MsgIdEnum.SC_ClaimPointCopyReward_VALUE, resultBuilder);
                return;
            }

            if (pointCopyBuilder.getCurPoint() < mission.getPointtarget()) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_PointNotEnough));
                gsChn.send(MsgIdEnum.SC_ClaimPointCopyReward_VALUE, resultBuilder);
                return;
            }

            pointCopyBuilder.addClaimRewardMissionId(req.getIndex());

            List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(mission.getReward());
            RewardManager.getInstance().doRewardByList(playerIdx, rewards,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PointInstance), true);

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimPointCopyReward_VALUE, resultBuilder);
        });

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Novice;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimPointCopyReward_VALUE, SC_ClaimPointCopyReward.newBuilder().setRetCode(retCode));
    }
}
