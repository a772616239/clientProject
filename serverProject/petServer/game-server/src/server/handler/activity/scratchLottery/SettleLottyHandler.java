package server.handler.activity.scratchLottery;

import cfg.ScratchLotteryParams;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.activity.ScratchLotteryManager;
import model.activity.entity.Lottery;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity.CS_SettleLottery;
import protocol.Activity.LotteryResult;
import protocol.Activity.SC_LotteryMarquee;
import protocol.Activity.SC_SettleLottery;
import protocol.Activity.SC_SettleLottery.Builder;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB.DB_ScratchLottery;
import common.AbstractBaseHandler;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/04/01
 */
@MsgId(msgId = MsgIdEnum.CS_SettleLottery_VALUE)
public class SettleLottyHandler extends AbstractBaseHandler<CS_SettleLottery> {
    @Override
    protected CS_SettleLottery parse(byte[] bytes) throws Exception {
        return CS_SettleLottery.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_SettleLottery req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        Builder resultBuilder = SC_SettleLottery.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_SettleLottery_VALUE, resultBuilder);
            return;
        }
        if (!ScratchLotteryManager.getInstance().isOpen()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_SettleLottery_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_ScratchLottery.Builder scratchLotteryBuilder = entity.getDb_Builder().getSpecialInfoBuilder().getScratchLotteryBuilder();
            List<LotteryResult> lottyList = scratchLotteryBuilder.getLottyList();
            if (lottyList.isEmpty()) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_MissionCanNotClaim));
                gsChn.send(MsgIdEnum.SC_SettleLottery_VALUE, resultBuilder);
                return;
            }

            //扣除奖券
            Consume consume = ConsumeUtil.parseConsume(ScratchLotteryParams.getById(GameConst.CONFIG_ID).getConsume());
            if (consume == null || !ConsumeManager.getInstance().consumeMaterial(playerIdx, consume,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Activity, "刮刮乐"))) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
                gsChn.send(MsgIdEnum.SC_ScratchLottery_VALUE, resultBuilder);
                return;
            }

            List<Reward> rewards = ScratchLotteryManager.getInstance().settleLottery(Lottery.valueOf(lottyList));
            if (rewards != null) {
                RewardManager.getInstance().doRewardByList(playerIdx, rewards,
                        ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ScratchLottery), true);

                sendRewardMarqueeToAllOnlinePlayer(playerIdx, rewards);
            }

            scratchLotteryBuilder.clearLotty();
            scratchLotteryBuilder.setScratchTimes(scratchLotteryBuilder.getScratchTimes() + 1);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_SettleLottery_VALUE, resultBuilder);
        });
    }

    public void sendRewardMarqueeToAllOnlinePlayer(String playerIdx, List<Reward> rewards) {
        if (StringHelper.isNull(playerIdx) || GameUtil.collectionIsEmpty(rewards)) {
            return;
        }

        SC_LotteryMarquee.Builder builder = SC_LotteryMarquee.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setPlayerName(PlayerUtil.queryPlayerName(playerIdx));
        builder.addAllReward(rewards);

        GlobalData.getInstance().sendMsgToAllOnlinePlayer(MsgIdEnum.SC_LotteryMarquee, builder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
