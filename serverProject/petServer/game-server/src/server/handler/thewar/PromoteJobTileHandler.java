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
import protocol.TheWar.CS_PromoteJobTile;
import protocol.TheWar.SC_PromoteJobTile;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_PromoteJobTile_VALUE)
public class PromoteJobTileHandler extends AbstractBaseHandler<CS_PromoteJobTile> {
    @Override
    protected CS_PromoteJobTile parse(byte[] bytes) throws Exception {
        return CS_PromoteJobTile.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PromoteJobTile req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_PromoteJobTile.Builder retBuilder = SC_PromoteJobTile.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_PromoteJobTile_VALUE, retBuilder);
            return;
        }
        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_PromoteJobTile_VALUE, req.toByteString())) {
            SC_PromoteJobTile.Builder retBuilder = SC_PromoteJobTile.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_PromoteJobTile_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_PromoteJobTile_VALUE,
                SC_PromoteJobTile.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}
