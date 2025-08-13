package server.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_ForceKickOutFromMist;

@MsgId(msgId = MsgIdEnum.GS_CS_ForceKickOutFromMist_VALUE)
public class KickOutMistPlayerHandler extends AbstractHandler<GS_CS_ForceKickOutFromMist> {
    @Override
    protected GS_CS_ForceKickOutFromMist parse(byte[] bytes) throws Exception {
        return GS_CS_ForceKickOutFromMist.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ForceKickOutFromMist req, int i) {
        MistPlayer mistPlayer;
        for (String playerIdx : req.getPlayerIdxList()) {
            mistPlayer = MistPlayerCache.getInstance().queryObject(playerIdx);
            if (mistPlayer == null || mistPlayer.getMistRoom() == null) {
                continue;
            }
            SyncExecuteFunction.executeConsumer(mistPlayer.getMistRoom(), mistRoom -> mistRoom.forceKickPlayer(playerIdx));
            SyncExecuteFunction.executeConsumer(mistPlayer, entity -> entity.onPlayerLogout(false));
        }
    }
}
