package server.handler.mistforest.maze;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mistforest.MistForestManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_MazeNextRefreshTime;
import protocol.MistForest.SC_MazeNextRefreshTime;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_MazeNextRefreshTime_VALUE)
public class QueryNextRefreshMazeHandler extends AbstractBaseHandler<CS_MazeNextRefreshTime> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistMaze;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_MazeNextRefreshTime_VALUE, SC_MazeNextRefreshTime.newBuilder());
    }

    @Override
    protected CS_MazeNextRefreshTime parse(byte[] bytes) throws Exception {
        return CS_MazeNextRefreshTime.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MazeNextRefreshTime req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        SC_MazeNextRefreshTime.Builder builder = SC_MazeNextRefreshTime.newBuilder();
        builder.setNextRefreshTime(MistForestManager.getInstance().getMazeManager().getMazeSyncData().getNextRefreshTime());
        gsChn.send(MsgIdEnum.SC_MazeNextRefreshTime_VALUE, builder);
    }
}
