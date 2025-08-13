package server.handler.mainLine;

import cfg.MainLineNodeShowReward;
import cfg.MainLineNodeShowRewardObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MainLine.CS_ClaimMainLineAdditionRewards;
import protocol.MainLine.SC_ClaimMainLineAdditionRewards;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/05/10
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimMainLineAdditionRewards_VALUE)
public class ClaimMainLineAdditionRewardsHandler extends AbstractBaseHandler<CS_ClaimMainLineAdditionRewards> {

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MainLine;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_ClaimMainLineAdditionRewards.Builder resultBuilder = SC_ClaimMainLineAdditionRewards.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_ClaimMainLineAdditionRewards_VALUE, resultBuilder);
    }

    @Override
    protected CS_ClaimMainLineAdditionRewards parse(byte[] bytes) throws Exception {
        return CS_ClaimMainLineAdditionRewards.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimMainLineAdditionRewards req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimMainLineAdditionRewards.Builder resultBuilder = SC_ClaimMainLineAdditionRewards.newBuilder();

        MainLineNodeShowRewardObject nodeCfg = MainLineNodeShowReward.getById(req.getNodeId());
        if (nodeCfg == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimMainLineAdditionRewards_VALUE, resultBuilder);
            return;
        }

        Reward rewards = RewardUtil.parseReward(nodeCfg.getOnshowreward());
        if (rewards == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimMainLineAdditionRewards_VALUE, resultBuilder);
            return;
        }

        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimMainLineAdditionRewards_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            if (entity.getDBBuilder().getOnHookIncome().getCurOnHookNode() < req.getNodeId()) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MainLine_ThisAdditionRewardCanNotClaimed));
                gsChn.send(MsgIdEnum.SC_ClaimMainLineAdditionRewards_VALUE, resultBuilder);
                return;
            }

            if (entity.getDBBuilder().getClaimedAdditionRewardsNodeIdList().contains(req.getNodeId())) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MainLine_ThisAdditionRewardClaimed));
                gsChn.send(MsgIdEnum.SC_ClaimMainLineAdditionRewards_VALUE, resultBuilder);
                return;
            }

            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MainlineAdditionRewards);
            RewardManager.getInstance().doReward(playerIdx, rewards, reason, true);

            entity.getDBBuilder().addClaimedAdditionRewardsNodeId(req.getNodeId());

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimMainLineAdditionRewards_VALUE, resultBuilder);
        });
    }
}
