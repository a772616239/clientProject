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
import protocol.TheWar.CS_CancelClearOwnedGrid;
import protocol.TheWar.SC_CancelClearOwnedGrid;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_CancelClearOwnedGrid_VALUE)
public class CancelClearGridHandler extends AbstractBaseHandler<CS_CancelClearOwnedGrid> {
    @Override
    protected CS_CancelClearOwnedGrid parse(byte[] bytes) throws Exception {
        return CS_CancelClearOwnedGrid.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CancelClearOwnedGrid req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_CancelClearOwnedGrid.Builder retBuilder = SC_CancelClearOwnedGrid.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_CancelClearOwnedGrid_VALUE, retBuilder);
            return;
        }
        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_CancelClearOwnedGrid_VALUE, req.toByteString())) {
            SC_CancelClearOwnedGrid.Builder retBuilder = SC_CancelClearOwnedGrid.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_CancelClearOwnedGrid_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_CancelClearOwnedGrid_VALUE,
                SC_CancelClearOwnedGrid.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}
