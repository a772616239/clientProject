package server.handler.thewar;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_UpdatePlayerBaseAdditions;

@MsgId(msgId = MsgIdEnum.GS_CS_UpdatePlayerBaseAdditions_VALUE)
public class UpdatePlayerBaseAdditionsHandler extends AbstractHandler<GS_CS_UpdatePlayerBaseAdditions> {
    @Override
    protected GS_CS_UpdatePlayerBaseAdditions parse(byte[] bytes) throws Exception {
        return GS_CS_UpdatePlayerBaseAdditions.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_UpdatePlayerBaseAdditions req, int i) {
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (warPlayer == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(warPlayer, entity->entity.updatePlayerBaseAdditions(req.getPlayerBaseAdditionsMap()));
    }
}
