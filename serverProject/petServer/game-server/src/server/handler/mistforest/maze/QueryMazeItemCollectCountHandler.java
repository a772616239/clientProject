package server.handler.mistforest.maze;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_MazeItemCollectCount;
import protocol.MistForest.SC_MazeItemCollectCount;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_MazeItemCollectCount_VALUE)
public class QueryMazeItemCollectCountHandler extends AbstractBaseHandler<CS_MazeItemCollectCount> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistMaze;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_MazeItemCollectCount_VALUE, SC_MazeItemCollectCount.newBuilder());
    }

    @Override
    protected CS_MazeItemCollectCount parse(byte[] bytes) throws Exception {
        return CS_MazeItemCollectCount.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MazeItemCollectCount req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        SC_MazeItemCollectCount.Builder builder = SC_MazeItemCollectCount.newBuilder();
        builder.setCollectCount(player.getDb_data().getMazeDataBuilder().getMazeItemCollectCount());
        gsChn.send(MsgIdEnum.SC_MazeItemCollectCount_VALUE, builder);
    }
}
