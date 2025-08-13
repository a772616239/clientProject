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
import protocol.TheWar.CS_ClearStationTroops;
import protocol.TheWar.SC_ClearStationTroops;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClearStationTroops_VALUE)
public class ClearStationTroopsHandler extends AbstractBaseHandler<CS_ClearStationTroops> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_ClearStationTroops_VALUE,
                SC_ClearStationTroops.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }

    @Override
    protected CS_ClearStationTroops parse(byte[] bytes) throws Exception {
        return CS_ClearStationTroops.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClearStationTroops req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_ClearStationTroops.Builder retBuilder = SC_ClearStationTroops.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_ClearStationTroops_VALUE, retBuilder);
            return;
        }
        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_ClearStationTroops_VALUE, req.toByteString())) {
            SC_ClearStationTroops.Builder retBuilder = SC_ClearStationTroops.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_ClearStationTroops_VALUE, retBuilder);
        }
    }
}
