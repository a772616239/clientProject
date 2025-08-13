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
import protocol.TheWar.CS_QueryWarGridRecord;
import protocol.TheWar.SC_QueryWarGridRecord;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_QueryWarGridRecord_VALUE)
public class QueryWarGridRecordHandler extends AbstractBaseHandler<CS_QueryWarGridRecord> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_QueryWarGridRecord_VALUE,
                SC_QueryWarGridRecord.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }

    @Override
    protected CS_QueryWarGridRecord parse(byte[] bytes) throws Exception {
        return CS_QueryWarGridRecord.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_QueryWarGridRecord req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_QueryWarGridRecord.Builder retBuilder = SC_QueryWarGridRecord.newBuilder();
            gsChn.send(MsgIdEnum.SC_QueryWarGridRecord_VALUE, retBuilder);
            return;
        }
        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_QueryWarGridRecord_VALUE, req.toByteString())) {
            SC_QueryWarGridRecord.Builder retBuilder = SC_QueryWarGridRecord.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_QueryWarGridRecord_VALUE, retBuilder);
        }
    }
}
