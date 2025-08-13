package server.handler.drawCard;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.DrawCard.CS_AbandonHighPool;
import protocol.DrawCard.SC_AbandonHighPool;
import protocol.DrawCard.SC_AbandonHighPool.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/03/19
 */
@MsgId(msgId = MsgIdEnum.CS_AbandonHighPool_VALUE)
public class AbandonHighPoolHandler extends AbstractBaseHandler<CS_AbandonHighPool> {
    @Override
    protected CS_AbandonHighPool parse(byte[] bytes) throws Exception {
        return CS_AbandonHighPool.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_AbandonHighPool req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        Builder resultBuilder = SC_AbandonHighPool.newBuilder();
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_AbandonHighPool_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, p -> {
            player.clearHighPoolData();
            player.sendDrawCardInfo();
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_AbandonHighPool_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.DrawCard_AncientCall;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_AbandonHighPool_VALUE, SC_AbandonHighPool.newBuilder().setRetCode(retCode));
    }
}
