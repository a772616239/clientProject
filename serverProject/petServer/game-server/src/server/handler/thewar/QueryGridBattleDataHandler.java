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
import protocol.TheWar.CS_QueryGridBattleData;
import protocol.TheWar.SC_QueryGridBattleData;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_QueryGridBattleData_VALUE)
public class QueryGridBattleDataHandler extends AbstractBaseHandler<CS_QueryGridBattleData> {
    @Override
    protected CS_QueryGridBattleData parse(byte[] bytes) throws Exception {
        return CS_QueryGridBattleData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_QueryGridBattleData req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_QueryGridBattleData.Builder retBuilder = SC_QueryGridBattleData.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_QueryGridBattleData_VALUE, retBuilder);
            return;
        }
        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_QueryGridBattleData_VALUE, req.toByteString())) {
            SC_QueryGridBattleData.Builder retBuilder = SC_QueryGridBattleData.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_QueryGridBattleData_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_QueryGridBattleData_VALUE,
                SC_QueryGridBattleData.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}
