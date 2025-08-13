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
import protocol.TheWarDefine.CS_QueryWarGridList;
import protocol.TheWarDefine.SC_QueryWarGridList;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_QueryWarGridList_VALUE)
public class QueryWarGridListHandler extends AbstractBaseHandler<CS_QueryWarGridList> {
    @Override
    protected CS_QueryWarGridList parse(byte[] bytes) throws Exception {
        return CS_QueryWarGridList.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_QueryWarGridList req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_QueryWarGridList.Builder retBuilder = SC_QueryWarGridList.newBuilder();
            gsChn.send(MsgIdEnum.SC_QueryWarGridList_VALUE, retBuilder);
            return;
        }
        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_QueryWarGridList_VALUE, req.toByteString())) {
            SC_QueryWarGridList.Builder retBuilder = SC_QueryWarGridList.newBuilder();
            gsChn.send(MsgIdEnum.SC_QueryWarGridList_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_QueryWarGridList_VALUE, SC_QueryWarGridList.newBuilder());
    }
}
