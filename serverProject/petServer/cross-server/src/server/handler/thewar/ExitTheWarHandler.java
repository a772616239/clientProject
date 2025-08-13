package server.handler.thewar;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_ExitTheWar;

@MsgId(msgId = MsgIdEnum.GS_CS_ExitTheWar_VALUE)
public class ExitTheWarHandler extends AbstractHandler<GS_CS_ExitTheWar> {
    @Override
    protected GS_CS_ExitTheWar parse(byte[] bytes) throws Exception {
        return GS_CS_ExitTheWar.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ExitTheWar req, int i) {
        WarPlayer warPlayer;
        for (String playerIdx : req.getPlayerIdxList()) {
            warPlayer = WarPlayerCache.getInstance().queryObject(playerIdx);
            if (warPlayer == null) {
                return;
            }
            SyncExecuteFunction.executeConsumer(warPlayer, entity -> entity.onPlayerLogout());
        }
    }
}
