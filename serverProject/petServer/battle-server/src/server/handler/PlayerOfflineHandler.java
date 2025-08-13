package server.handler;

import common.GameConst.EventType;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.cache.PlayerCache;
import model.player.entity.Player;
import model.room.cache.RoomCache;
import model.room.entity.Room;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.PlayerOffline;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.PlayerOffline_VALUE)
public class PlayerOfflineHandler extends AbstractHandler<PlayerOffline> {
    @Override
    protected PlayerOffline parse(byte[] bytes) throws Exception {
        return PlayerOffline.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, PlayerOffline req, int i) {
        int serverIndex = StringHelper.stringToInt(gsChn.getPlayerId(), 0);
        GameServerTcpChannel channel = WarpServerManager.getInstance().getGameServerChannel(serverIndex);
        if (channel == null) {
            return;
        }
        Player player = PlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        Room room = RoomCache.getInstance().queryObject(player.getRoomId());
        Event event;
        if (room != null) {
            event = Event.valueOf(EventType.ET_Offline, room, player);
        } else {
            event = Event.valueOf(EventType.ET_Logout, GameUtil.getDefaultEventSource(), player);
        }
        EventManager.getInstance().dispatchEvent(event);
    }
}
