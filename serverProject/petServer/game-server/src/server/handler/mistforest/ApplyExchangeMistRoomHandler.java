package server.handler.mistforest;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_ApplyExchangeMistRoom;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_ApplyExchangeMistRoom;
import protocol.ServerTransfer.GS_CS_ReqExchangeMistRoom;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ApplyExchangeMistRoom_VALUE)
public class ApplyExchangeMistRoomHandler extends AbstractBaseHandler<CS_ApplyExchangeMistRoom> {
    @Override
    protected CS_ApplyExchangeMistRoom parse(byte[] bytes) throws Exception {
        return CS_ApplyExchangeMistRoom.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ApplyExchangeMistRoom req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }

//        if (req.get() < 1 || req.getNewMistLevel() > 9) { // 层数写死
//            SC_ApplyExchangeMistRoom.Builder builder = SC_ApplyExchangeMistRoom.newBuilder();
//            builder.setRetCode(MistRetCode.MRC_IllegalLevel);
//            GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_ApplyExchangeMistRoom_VALUE, builder);
//            return;
//        }
        if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(playerId) <= 0) {
            SC_ApplyExchangeMistRoom.Builder builder = SC_ApplyExchangeMistRoom.newBuilder();
            builder.setRetCode(MistRetCode.MRC_NotInMistForest);
            GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_ApplyExchangeMistRoom_VALUE, builder);
            return;
        }
        GS_CS_ReqExchangeMistRoom.Builder request = GS_CS_ReqExchangeMistRoom.newBuilder();
        request.setIdx(playerId);
        request.setNewMapId(req.getNewMapId());
        CrossServerManager.getInstance().sendMsgToMistForest(playerId, MsgIdEnum.GS_CS_ReqExchangeMistRoom_VALUE, request, true);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_ApplyExchangeMistRoom_VALUE,
                SC_ApplyExchangeMistRoom.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }
}
