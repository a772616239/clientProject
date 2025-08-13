package server.handler.mistforest.ghostBuster;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mistforest.MistForestManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_StopMatchGhostBuster;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_StopMatchGhostBuster;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_StopMatchGhostBuster_VALUE)
public class StopMatchGhostBusterHandler extends AbstractBaseHandler<CS_StopMatchGhostBuster> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistGhostBuster;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_StopMatchGhostBuster_VALUE,
                SC_StopMatchGhostBuster.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }

    @Override
    protected CS_StopMatchGhostBuster parse(byte[] bytes) throws Exception {
        return CS_StopMatchGhostBuster.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_StopMatchGhostBuster req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        SC_StopMatchGhostBuster.Builder builder = SC_StopMatchGhostBuster.newBuilder();
        if (CrossServerManager.getInstance().getMistForestPlayerServerInfo(playerId) != null) {
            builder.setRetCode(MistRetCode.MRC_InMistForest);
            gsChn.send(MsgIdEnum.SC_StopMatchGhostBuster_VALUE, builder);
            return;
        }
        builder.setRetCode(MistForestManager.getInstance().getGhostBusterManager().stopMatch(player));
        gsChn.send(MsgIdEnum.SC_StopMatchGhostBuster_VALUE, builder);
    }
}
