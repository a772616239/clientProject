package server.handler.thewar;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.thewar.WarConst.RoomState;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_BuyBackAllPets;

@MsgId(msgId = MsgIdEnum.GS_CS_BuyBackAllPets_VALUE)
public class BuyBackAllPetsHandler extends AbstractHandler<GS_CS_BuyBackAllPets> {
    @Override
    protected GS_CS_BuyBackAllPets parse(byte[] bytes) throws Exception {
        return GS_CS_BuyBackAllPets.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_BuyBackAllPets req, int i) {
        WarRoom warRoom = WarRoomCache.getInstance().queryObject(req.getRoomIdx());
        if (warRoom == null || warRoom.getRoomState() != RoomState.FightingState) {
            return;
        }
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (warPlayer == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(warPlayer, entity -> entity.reviveAllPets());
    }
}
