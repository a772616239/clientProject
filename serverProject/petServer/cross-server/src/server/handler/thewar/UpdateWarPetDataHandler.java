package server.handler.thewar;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_UpdateWarPetData;

@MsgId(msgId = MsgIdEnum.GS_CS_UpdateWarPetData_VALUE)
public class UpdateWarPetDataHandler extends AbstractHandler<GS_CS_UpdateWarPetData> {
    @Override
    protected GS_CS_UpdateWarPetData parse(byte[] bytes) throws Exception {
        return GS_CS_UpdateWarPetData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_UpdateWarPetData req, int i) {
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (warPlayer == null) {
            return;
        }

        WarRoom room = WarRoomCache.getInstance().queryObject(warPlayer.getRoomIdx());
        if (room == null || room.needClear()) {
            return;
        }

        SyncExecuteFunction.executeConsumer(warPlayer, entity-> entity.updatePetData(req.getPetData()));
    }

}
