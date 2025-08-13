package server.handler.mistforest.ghostBuster;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_LoadGhostBusterFinish;
import protocol.ServerTransfer.GS_CS_EnterGhostBuster;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_LoadGhostBusterFinish_VALUE)
public class LoadRoomFinishHandler extends AbstractBaseHandler<CS_LoadGhostBusterFinish> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistGhostBuster;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

    @Override
    protected CS_LoadGhostBusterFinish parse(byte[] bytes) throws Exception {
        return CS_LoadGhostBusterFinish.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_LoadGhostBusterFinish req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        GS_CS_EnterGhostBuster.Builder builder = GS_CS_EnterGhostBuster.newBuilder();
        builder.setPlayerInfo(player.getBattleBaseData());
        if (!CrossServerManager.getInstance().sendMsgToMistForest(playerId, MsgIdEnum.GS_CS_EnterGhostBuster_VALUE, builder, true)) {
            LogUtil.error("player send load GhostBusterRoom msg failed");
        }
    }
}
