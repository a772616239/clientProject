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
import protocol.TheWar.CS_SubmitDpResource;
import protocol.TheWar.SC_SubmitDpResource;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_SubmitDpResource_VALUE)
public class SubmitDpResourceHandler extends AbstractBaseHandler<CS_SubmitDpResource> {
    @Override
    protected CS_SubmitDpResource parse(byte[] bytes) throws Exception {
        return CS_SubmitDpResource.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_SubmitDpResource req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_SubmitDpResource.Builder retBuilder = SC_SubmitDpResource.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_SubmitDpResource_VALUE, retBuilder);
            return;
        }
        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_SubmitDpResource_VALUE, req.toByteString())) {
            SC_SubmitDpResource.Builder retBuilder = SC_SubmitDpResource.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_SubmitDpResource_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_SubmitDpResource_VALUE,
                SC_SubmitDpResource.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}
