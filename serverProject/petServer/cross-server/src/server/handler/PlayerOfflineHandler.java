package server.handler;

import common.GameConst.EventType;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.warpServer.WarpServerConst;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.PlayerOffline;
import server.event.Event;
import server.event.EventManager;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.PlayerOffline_VALUE)
public class PlayerOfflineHandler extends AbstractHandler<PlayerOffline> {
    @Override
    protected PlayerOffline parse(byte[] bytes) throws Exception {
        return PlayerOffline.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, PlayerOffline req, int i) {
        String ip = WarpServerConst.parseIp(gsChn.channel.remoteAddress().toString().substring(1));
        onPlayerLeaveMistForest(req.getPlayerIdx());
        onPlayerLeaveTheWar(req.getPlayerIdx());
        LogUtil.info("player offline from CS,idx=" + req.getPlayerIdx() + ",ip=" + ip);
    }

    protected void onPlayerLeaveMistForest(String playerId) {
        MistPlayer mistPlayer = MistPlayerCache.getInstance().queryObject(playerId);
        if (mistPlayer == null) {
            return;
        }
        MistRoom room = mistPlayer.getMistRoom();
        if (room == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_Offline, room, mistPlayer);
        EventManager.getInstance().dispatchEvent(event);
    }

    protected void onPlayerLeaveTheWar(String playerId) {
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(playerId);
        if (warPlayer == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(warPlayer, entity -> entity.onPlayerLogout());
    }
}
