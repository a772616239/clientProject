package model.warpServer.crossServer.handler.thewar;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_UpdateLastSettleTime;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateLastSettleTime_VALUE)
public class UpdateLastSettleTimeHandler extends AbstractHandler<CS_GS_UpdateLastSettleTime> {
    @Override
    protected CS_GS_UpdateLastSettleTime parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateLastSettleTime.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateLastSettleTime ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> entity.getDb_data().getTheWarDataBuilder().setLastSettleTime(ret.getLastSettleTime()));
    }
}
