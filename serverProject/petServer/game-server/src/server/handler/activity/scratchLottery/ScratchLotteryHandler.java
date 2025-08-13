package server.handler.activity.scratchLottery;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ScratchLotteryManager;
import model.activity.entity.Lottery;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity.CS_ScratchLottery;
import protocol.Activity.LotteryResult;
import protocol.Activity.SC_ScratchLottery;
import protocol.Activity.SC_ScratchLottery.Builder;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB.DB_ScratchLottery;
import common.AbstractBaseHandler;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/03/24
 */
@MsgId(msgId = MsgIdEnum.CS_ScratchLottery_VALUE)
public class ScratchLotteryHandler extends AbstractBaseHandler<CS_ScratchLottery> {
    @Override
    protected CS_ScratchLottery parse(byte[] bytes) throws Exception {
        return CS_ScratchLottery.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ScratchLottery req, int i) {
        Builder resultBuilder = SC_ScratchLottery.newBuilder();
        if (!ScratchLotteryManager.getInstance().isOpen()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_ScratchLottery_VALUE, resultBuilder);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ScratchLottery_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_ScratchLottery.Builder scratchLotteryBuilder = entity.getDb_Builder().getSpecialInfoBuilder().getScratchLotteryBuilder();

            if (scratchLotteryBuilder.getLottyList().isEmpty()) {
                Lottery lottery = ScratchLotteryManager.getInstance().randomLottery(playerIdx);
                if (lottery == null || !lottery.checkContent()) {
                    resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                    gsChn.send(MsgIdEnum.SC_ScratchLottery_VALUE, resultBuilder);
                    return;
                }

                int[][] context = lottery.getContext();
                for (int j = 0; j < context.length; j++) {
                    int[] ints = context[j];
                    for (int k = 0; k < ints.length; k++) {
                        LotteryResult.Builder builder = LotteryResult.newBuilder();
                        builder.setX(j);
                        builder.setY(k);
                        builder.setPetBookId(context[j][k]);
                        scratchLotteryBuilder.addLotty(builder);
                    }
                }
                resultBuilder.addAllResult(scratchLotteryBuilder.getLottyList());
            } else {
                resultBuilder.addAllResult(scratchLotteryBuilder.getLottyList());
            }

            resultBuilder.setScratchTimes(scratchLotteryBuilder.getScratchTimes());
            resultBuilder.addAllClaimedIndex(scratchLotteryBuilder.getClaimedIndexList());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ScratchLottery_VALUE, resultBuilder);
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
