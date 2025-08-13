package server.handler.activity.scratchLottery;

import cfg.ScratchLotteryParams;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.activity.ScratchLotteryManager;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity.CS_ClaimLotteryProgressReward;
import protocol.Activity.SC_ClaimLotteryProgressReward;
import protocol.Activity.SC_ClaimLotteryProgressReward.Builder;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB.DB_ScratchLottery;
import common.AbstractBaseHandler;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/04/01
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimLotteryProgressReward_VALUE)
public class ClaimLotteryProgressRewardHandler extends AbstractBaseHandler<CS_ClaimLotteryProgressReward> {
    @Override
    protected CS_ClaimLotteryProgressReward parse(byte[] bytes) throws Exception {
        return CS_ClaimLotteryProgressReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimLotteryProgressReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        Builder resultBuilder = SC_ClaimLotteryProgressReward.newBuilder();
        if(entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimLotteryProgressReward_VALUE, resultBuilder);
            return;
        }

        if (!ScratchLotteryManager.getInstance().isOpen()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_ClaimLotteryProgressReward_VALUE, resultBuilder);
            return;
        }

        int[][] progressReward = ScratchLotteryParams.getById(GameConst.CONFIG_ID).getProgressreward();
        if (req.getIndex() >= progressReward.length) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimLotteryProgressReward_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_ScratchLottery.Builder scratchLotteryBuilder = entity.getDb_Builder().getSpecialInfoBuilder().getScratchLotteryBuilder();
            if (scratchLotteryBuilder.getClaimedIndexList().contains(req.getIndex())) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_RewardAlreadyClaim));
                gsChn.send(MsgIdEnum.SC_ClaimLotteryProgressReward_VALUE, resultBuilder);
                return;
            }

            int[] ints = progressReward[req.getIndex()];
            if (ints.length < 2) {
                LogUtil.error("ScratchLottery progress reward cfg error , index = " +req.getIndex() + ", length < 2");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ClaimLotteryProgressReward_VALUE, resultBuilder);
                return;
            }

            if (scratchLotteryBuilder.getScratchTimes() < ints[0]) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_MissionCanNotClaim));
                gsChn.send(MsgIdEnum.SC_ClaimLotteryProgressReward_VALUE, resultBuilder);
                return;
            }

            List<Reward> rewards = RewardUtil.getRewardsByRewardId(ints[1]);
            if(!GameUtil.collectionIsEmpty(rewards)) {
                RewardManager.getInstance().doRewardByList(playerIdx, rewards,
                        ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ScratchLottery), true);
            }

            scratchLotteryBuilder.addClaimedIndex(req.getIndex());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimLotteryProgressReward_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
