package server.handler;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.cache.PlayerCache;
import model.player.entity.Player;
import model.room.cache.RoomCache;
import model.room.entity.Room;
import model.warpServer.WarpServerConst;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_RevertBattle;
import protocol.ServerTransfer.GS_BS_RevertBattle;

@MsgId(msgId = MsgIdEnum.GS_BS_RevertBattle_VALUE)
public class BattleReverHandler extends AbstractHandler<GS_BS_RevertBattle> {
    @Override
    protected GS_BS_RevertBattle parse(byte[] bytes) throws Exception {
        return GS_BS_RevertBattle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_RevertBattle req, int i) {
        BS_GS_RevertBattle.Builder builder = BS_GS_RevertBattle.newBuilder();
        builder.setPlayerIdx(req.getPlayerIdx());
        Player player = PlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (player == null) {
            gsChn.send(MsgIdEnum.BS_GS_RevertBattle_VALUE, builder);
            return;
        }
        Room room = RoomCache.getInstance().queryObject(player.getRoomId());
        if (room == null) {
            gsChn.send(MsgIdEnum.BS_GS_RevertBattle_VALUE, builder);
            return;
        }
        builder.setResult(true);
        builder.setRevertData(room.getRevertData(player.getIdx()));
        gsChn.send(MsgIdEnum.BS_GS_RevertBattle_VALUE, builder);
        SyncExecuteFunction.executeConsumer(player, entity->entity.setOnline(true));
        room.playerOnlineChange(player.getIdx(), true);
    }
}
