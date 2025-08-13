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
import protocol.TheWarDefine.CS_OperateCollectionPos;
import protocol.TheWarDefine.SC_OperateCollectionPos;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_OperateCollectionPos_VALUE)
public class OperateCollectPosHandler extends AbstractBaseHandler<CS_OperateCollectionPos> {
    @Override
    protected CS_OperateCollectionPos parse(byte[] bytes) throws Exception {
        return CS_OperateCollectionPos.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_OperateCollectionPos req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_OperateCollectionPos.Builder retBuilder = SC_OperateCollectionPos.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_OperateCollectionPos_VALUE, retBuilder);
            return;
        }
        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_OperateCollectionPos_VALUE, req.toByteString())) {
            SC_OperateCollectionPos.Builder retBuilder = SC_OperateCollectionPos.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_OperateCollectionPos_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_OperateCollectionPos_VALUE,
                SC_OperateCollectionPos.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}
