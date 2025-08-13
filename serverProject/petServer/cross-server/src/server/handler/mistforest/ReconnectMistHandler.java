package server.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_ReconnectMistForest;
import protocol.ServerTransfer.GS_CS_ReconnectMistForest;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_CS_ReconnectMistForest_VALUE)
public class ReconnectMistHandler extends AbstractHandler<GS_CS_ReconnectMistForest> {
    @Override
    protected GS_CS_ReconnectMistForest parse(byte[] bytes) throws Exception {
        return GS_CS_ReconnectMistForest.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ReconnectMistForest req, int i) {
        LogUtil.info("recv reconnect mist server,playerIdx=" + req.getPlayerIdx());
        CS_GS_ReconnectMistForest.Builder builder = CS_GS_ReconnectMistForest.newBuilder();
        builder.setPlayerIdx(req.getPlayerIdx());
        MistPlayer mistPlayer = MistPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (mistPlayer == null) {
            gsChn.send(MsgIdEnum.CS_GS_ReconnectMistForest_VALUE, builder);
            return;
        }
        MistRoom mistRoom = mistPlayer.getMistRoom();
        if (mistRoom == null) {
            gsChn.send(MsgIdEnum.CS_GS_ReconnectMistForest_VALUE, builder);
            return;
        }
        MistFighter fighter = mistRoom.getObjManager().getMistObj(mistPlayer.getFighterId());
        if (fighter == null) {
            gsChn.send(MsgIdEnum.CS_GS_ReconnectMistForest_VALUE, builder);
            return;
        }
        builder.setResult(true);
        gsChn.send(MsgIdEnum.CS_GS_ReconnectMistForest_VALUE, builder);
        SyncExecuteFunction.executeConsumer(mistPlayer, player -> {
            player.setOnline(true);
            player.setOfflineTime(0);
        });

        if (!fighter.isBattling()) {
            fighter.updateRevertRoomInfo();
        }
    }
}
