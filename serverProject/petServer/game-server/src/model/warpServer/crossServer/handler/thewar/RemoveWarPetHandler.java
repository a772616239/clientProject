package model.warpServer.crossServer.handler.thewar;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_RemoveWarPetData;

@MsgId(msgId = MsgIdEnum.CS_GS_RemoveWarPetData_VALUE)
public class RemoveWarPetHandler extends AbstractHandler<CS_GS_RemoveWarPetData> {
    @Override
    protected CS_GS_RemoveWarPetData parse(byte[] bytes) throws Exception {
        return CS_GS_RemoveWarPetData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_RemoveWarPetData ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null || !player.getDb_data().getTheWarData().containsInWarPets(ret.getRemovePetIdx())) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity-> entity.getDb_data().getTheWarDataBuilder().removeInWarPets(ret.getRemovePetIdx()));
    }
}
