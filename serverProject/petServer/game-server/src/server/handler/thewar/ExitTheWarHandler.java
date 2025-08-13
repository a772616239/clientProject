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
import protocol.ServerTransfer.GS_CS_ExitTheWar;
import protocol.TheWar.CS_ExitTheWar;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ExitTheWar_VALUE)
public class ExitTheWarHandler extends AbstractBaseHandler<CS_ExitTheWar> {
    @Override
    protected CS_ExitTheWar parse(byte[] bytes) throws Exception {
        return CS_ExitTheWar.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ExitTheWar req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            return;
        }
        CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_ExitTheWar_VALUE, GS_CS_ExitTheWar.newBuilder().addPlayerIdx(playerIdx));
        CrossServerManager.getInstance().removeTheWarPlayer(player);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
