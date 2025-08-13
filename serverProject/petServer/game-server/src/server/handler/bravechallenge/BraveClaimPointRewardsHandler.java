package server.handler.bravechallenge;

import cfg.BraveChallengePoint;
import cfg.BraveChallengePointObject;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.bravechallenge.dbCache.bravechallengeCache;
import model.bravechallenge.entity.bravechallengeEntity;
import model.reward.RewardManager;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.BraveChallenge.BravePoint;
import protocol.BraveChallenge.CS_BraveClaimPointRewards;
import protocol.BraveChallenge.SC_BraveChallengeComplete;
import protocol.BraveChallenge.SC_BraveClaimPointRewards;
import protocol.BraveChallengeDB.DB_ChallengeProgress;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020-11-23
 */
@MsgId(msgId = MsgIdEnum.CS_BraveClaimPointRewards_VALUE)
public class BraveClaimPointRewardsHandler extends AbstractBaseHandler<CS_BraveClaimPointRewards> {
    @Override
    protected CS_BraveClaimPointRewards parse(byte[] bytes) throws Exception {
        return CS_BraveClaimPointRewards.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BraveClaimPointRewards req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_BraveClaimPointRewards.Builder resultBuilder = SC_BraveClaimPointRewards.newBuilder();

        BraveChallengePointObject pointCfg = BraveChallengePoint.getById(req.getPointId());
        if (pointCfg == null || pointCfg.getPointtype() != bravechallengeEntity.POINT_TYPE_REWARDS) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_BraveClaimPointRewards_VALUE, resultBuilder);
            return;
        }

        bravechallengeEntity entity = bravechallengeCache.getInstance().getEntityByPlayer(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BraveClaimPointRewards_VALUE, resultBuilder);
            return;
        }

        RetCodeEnum ret = SyncExecuteFunction.executeFunction(entity, e -> {
            DB_ChallengeProgress.Builder progressBuilder = entity.getProgressBuilder();
            if (progressBuilder.getProgress() != (req.getPointId() - 1)) {
                return RetCodeEnum.RCE_ErrorParam;
            }

            BravePoint point = entity.getProgressBuilder().getPointCfgMap().get(req.getPointId());
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_BraveChallenge);
            RewardManager.getInstance().doRewardByList(playerIdx, point.getRewardsList(), reason, false);

            //修改进度
            progressBuilder.setProgress(progressBuilder.getProgress() + 1);

            //是否完成完成所有关卡
            if (progressBuilder.getProgress() >= BraveChallengePoint.maxPoint) {
                GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_BraveChallengeComplete_VALUE, SC_BraveChallengeComplete.newBuilder());
                EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuPassCouragePoint, 1, 0);
            }

            entity.sendProgress();
            return RetCodeEnum.RCE_Success;
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(ret));
        gsChn.send(MsgIdEnum.SC_BraveClaimPointRewards_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CourageTrial;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BraveClaimPointRewards_VALUE, SC_BraveClaimPointRewards.newBuilder().setRetCode(retCode));
    }
}
