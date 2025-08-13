package server.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.room.cache.RoomCache;
import model.room.entity.Room;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_BattleBulletCha;

@MsgId(msgId = MsgIdEnum.GS_BS_BattleBulletCha_VALUE)
public class BattleBulletChaHandler extends AbstractHandler<GS_BS_BattleBulletCha> {
    @Override
    protected GS_BS_BattleBulletCha parse(byte[] bytes) throws Exception {
        return GS_BS_BattleBulletCha.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_BattleBulletCha req, int i) {
        Room room = RoomCache.getInstance().queryObject(String.valueOf(req.getBattleId()));
        if (room != null) {
            room.broadcastBulletCha(req);
        }
    }

}
