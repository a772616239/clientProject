package server.handler.mistforest.maze;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_MistMazeBuyGoodsTimes;
import protocol.MistForest.SC_MistMazeBuyGoodsTimes;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_MistMazeBuyGoodsTimes_VALUE)
public class MazeBuyGoodsTimesHandler extends AbstractBaseHandler<CS_MistMazeBuyGoodsTimes> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistMaze;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_MistMazeBuyGoodsTimes.Builder builder = SC_MistMazeBuyGoodsTimes.newBuilder();
        gsChn.send(MsgIdEnum.SC_MistMazeBuyGoodsTimes_VALUE, builder);
    }

    @Override
    protected CS_MistMazeBuyGoodsTimes parse(byte[] bytes) throws Exception {
        return CS_MistMazeBuyGoodsTimes.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MistMazeBuyGoodsTimes req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        SC_MistMazeBuyGoodsTimes.Builder builder = SC_MistMazeBuyGoodsTimes.newBuilder();
        builder.setMazeBuyGoodsTimes(player.getDb_data().getMazeDataBuilder().getBuyGoodsTimes());
        gsChn.send(MsgIdEnum.SC_MistMazeBuyGoodsTimes_VALUE, builder);
    }
}
