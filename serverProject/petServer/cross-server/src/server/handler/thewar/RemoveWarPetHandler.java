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
import protocol.ServerTransfer.GS_CS_RemoveWarPetData;
import protocol.TheWarDefine.WarPetData;

@MsgId(msgId = MsgIdEnum.GS_CS_RemoveWarPetData_VALUE)
public class RemoveWarPetHandler extends AbstractHandler<GS_CS_RemoveWarPetData> {
    @Override
    protected GS_CS_RemoveWarPetData parse(byte[] bytes) throws Exception {
        return GS_CS_RemoveWarPetData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_RemoveWarPetData req, int i) {
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (warPlayer == null) {
            return;
        }

        WarRoom room = WarRoomCache.getInstance().queryObject(warPlayer.getRoomIdx());
        if (room == null || room.needClear()) {
            return;
        }

        WarPetData petData = warPlayer.getPlayerData().getPlayerPetsMap().get(req.getRemovePetIdx());
        if (petData == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(warPlayer, entity -> entity.removePetByPos(petData.getIndexOfList()));
    }
}
