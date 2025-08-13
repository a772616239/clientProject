package server.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_EnterGhostBuster;

@MsgId(msgId = MsgIdEnum.GS_CS_EnterGhostBuster_VALUE)
public class LoadGhostBusterFinishHandler extends AbstractHandler<GS_CS_EnterGhostBuster> {
    @Override
    protected GS_CS_EnterGhostBuster parse(byte[] bytes) throws Exception {
        return GS_CS_EnterGhostBuster.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_EnterGhostBuster req, int i) {
        MistPlayer player = MistPlayerCache.getInstance().queryObject(req.getPlayerInfo().getPlayerId());
        if (player == null) {
            return;
        }
        MistRoom room = player.getMistRoom();
        if (room == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> entity.setReadyState(true));
    }
}
