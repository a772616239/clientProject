package server.handler.thewar;

import common.AbstractBaseHandler;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWar.CS_ClearOwnedGrid;
import protocol.TheWar.SC_ClearOwnedGrid;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClearOwnedGrid_VALUE)
public class ClearOwnedGridHandler extends AbstractBaseHandler<CS_ClearOwnedGrid> {
    @Override
    protected CS_ClearOwnedGrid parse(byte[] bytes) throws Exception {
        return CS_ClearOwnedGrid.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClearOwnedGrid req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_ClearOwnedGrid.Builder retBuilder = SC_ClearOwnedGrid.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_ClearOwnedGrid_VALUE, retBuilder);
            return;
        }
        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_ClearOwnedGrid_VALUE, req.toByteString())) {
            SC_ClearOwnedGrid.Builder retBuilder = SC_ClearOwnedGrid.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_ClearOwnedGrid_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_ClearOwnedGrid_VALUE,
                SC_ClearOwnedGrid.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}
