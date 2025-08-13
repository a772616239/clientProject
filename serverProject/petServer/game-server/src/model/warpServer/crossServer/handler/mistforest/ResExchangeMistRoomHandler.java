package model.warpServer.crossServer.handler.mistforest;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.SC_ApplyExchangeMistRoom;
import protocol.ServerTransfer.CS_GS_ResExchangeMistRoom;

@MsgId(msgId = MsgIdEnum.CS_GS_ResExchangeMistRoom_VALUE)
public class ResExchangeMistRoomHandler extends AbstractHandler<CS_GS_ResExchangeMistRoom> {
    @Override
    protected CS_GS_ResExchangeMistRoom parse(byte[] bytes) throws Exception {
        return CS_GS_ResExchangeMistRoom.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_ResExchangeMistRoom req, int i) {
        playerEntity player = playerCache.getByIdx(req.getIdx());
        if (player == null) {
            return;
        }
        SC_ApplyExchangeMistRoom.Builder builder = SC_ApplyExchangeMistRoom.newBuilder();
        builder.setRetCode(req.getRetCode());
        GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_ApplyExchangeMistRoom_VALUE, builder);
    }
}
