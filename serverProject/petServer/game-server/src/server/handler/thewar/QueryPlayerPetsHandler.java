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
import protocol.TheWarDefine.CS_QueryWarPetData;
import protocol.TheWarDefine.SC_QueryWarPetData;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_QueryWarPetData_VALUE)
public class QueryPlayerPetsHandler extends AbstractBaseHandler<CS_QueryWarPetData> {
    @Override
    protected CS_QueryWarPetData parse(byte[] bytes) throws Exception {
        return CS_QueryWarPetData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_QueryWarPetData req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_QueryWarPetData.Builder retBuilder = SC_QueryWarPetData.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_QueryWarPetData_VALUE, retBuilder);
            return;
        }
        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_QueryWarPetData_VALUE, req.toByteString())) {
            SC_QueryWarPetData.Builder retBuilder = SC_QueryWarPetData.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_QueryWarPetData_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_QueryWarPetData_VALUE,
                SC_QueryWarPetData.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}
