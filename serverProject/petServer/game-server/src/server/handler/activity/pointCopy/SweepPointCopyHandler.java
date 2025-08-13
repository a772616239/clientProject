package server.handler.activity.pointCopy;

import cfg.PointCopyCfg;
import cfg.PointCopyCfgObject;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.activity.PointCopyManager;
import model.consume.ConsumeManager;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity.CS_SweepPointCopy;
import protocol.Activity.SC_SweepPointCopy;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/12/3
 */
@MsgId(msgId = MsgIdEnum.CS_SweepPointCopy_VALUE)
public class SweepPointCopyHandler extends AbstractBaseHandler<CS_SweepPointCopy> {
    @Override
    protected CS_SweepPointCopy parse(byte[] bytes) throws Exception {
        return CS_SweepPointCopy.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_SweepPointCopy req, int i) {
        SC_SweepPointCopy.Builder resultBuilder = SC_SweepPointCopy.newBuilder();
        if (!PointCopyManager.getInstance().isOpen()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_SweepPointCopy_VALUE, resultBuilder);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        Consume consume = PointCopyManager.getInstance().getConsumeByCfgId(req.getIndex());
        PointCopyCfgObject pointCfg = PointCopyCfg.getById(req.getIndex());
        if (pointCfg == null || pointCfg.getMissiontype() != 1 || entity == null || consume == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_SweepPointCopy_VALUE, resultBuilder);
            return;
        }

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PointInstance);
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MsgIdEnum.SC_SweepPointCopy_VALUE, resultBuilder);
            return;
        }

        List<Reward> rewards = RewardUtil.getRewardsByFightMakeId(pointCfg.getFightmakeid());
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_SweepPointCopy_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PointCopy;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_SweepPointCopy_VALUE, SC_SweepPointCopy.newBuilder().setRetCode(retCode));
    }
}
