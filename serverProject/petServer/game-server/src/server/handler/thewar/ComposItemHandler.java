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
import protocol.TheWar.CS_ComposeNewItem;
import protocol.TheWar.SC_ComposeNewItem;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ComposeNewItem_VALUE)
public class ComposItemHandler extends AbstractBaseHandler<CS_ComposeNewItem> {
    @Override
    protected CS_ComposeNewItem parse(byte[] bytes) throws Exception {
        return CS_ComposeNewItem.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ComposeNewItem req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_ComposeNewItem.Builder retBuilder = SC_ComposeNewItem.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_ComposeNewItem_VALUE, retBuilder);
            return;
        }
        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_ComposeNewItem_VALUE, req.toByteString())) {
            SC_ComposeNewItem.Builder retBuilder = SC_ComposeNewItem.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_ComposeNewItem_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_ComposeNewItem_VALUE,
                SC_ComposeNewItem.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}
