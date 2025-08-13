package server.handler.mistforest.maze;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_UpdateMistMazeRecord;
import protocol.MistForest.SC_UpdateMistMazeRecord;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_UpdateMistMazeRecord_VALUE)
public class QueryMazeRecordHandler extends AbstractBaseHandler<CS_UpdateMistMazeRecord> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

    @Override
    protected CS_UpdateMistMazeRecord parse(byte[] bytes) throws Exception {
        return CS_UpdateMistMazeRecord.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpdateMistMazeRecord req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        SC_UpdateMistMazeRecord.Builder builder = SC_UpdateMistMazeRecord.newBuilder();
        builder.setMazeRecordData(player.getDb_data().getMazeDataBuilder().getMazeRecordDataBuilder());
        GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_UpdateMistMazeRecord_VALUE, builder);
    }
}
