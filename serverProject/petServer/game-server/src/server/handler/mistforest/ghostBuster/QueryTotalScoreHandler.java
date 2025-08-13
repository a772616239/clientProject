package server.handler.mistforest.ghostBuster;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_GhostBusterTotalScore;
import protocol.MistForest.SC_GhostBusterTotalScore;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_GhostBusterTotalScore_VALUE)
public class QueryTotalScoreHandler extends AbstractBaseHandler<CS_GhostBusterTotalScore> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistGhostBuster;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_GhostBusterTotalScore_VALUE, SC_GhostBusterTotalScore.newBuilder());
    }

    @Override
    protected CS_GhostBusterTotalScore parse(byte[] bytes) throws Exception {
        return CS_GhostBusterTotalScore.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GhostBusterTotalScore req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        SC_GhostBusterTotalScore.Builder builder = SC_GhostBusterTotalScore.newBuilder();
        builder.setGhoseTotalScore(player.getDb_data().getGhostBusterDataBuilder().getTotalGainScore());
        gsChn.send(MsgIdEnum.SC_GhostBusterTotalScore_VALUE, builder);
    }
}
