package model.warpServer.crossServer.handler.mistforest;

import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.SC_UpdateMistMazeRecord;
import protocol.ServerTransfer.CS_GS_UpdateMistMazeRecord;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateMistMazeRecord_VALUE)
public class UpdateMazeRecordHandler extends AbstractHandler<CS_GS_UpdateMistMazeRecord> {
    @Override
    protected CS_GS_UpdateMistMazeRecord parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateMistMazeRecord.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateMistMazeRecord ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> {
            boolean exist = false;
            for (int index = 0; index < entity.getDb_data().getMazeDataBuilder().getMazeRecordDataBuilder().getLevelCount(); index++) {
                int level = entity.getDb_data().getMazeDataBuilder().getMazeRecordDataBuilder().getLevel(index);
                if (ret.getLevel() == level) {
                    entity.getDb_data().getMazeDataBuilder().getMazeRecordDataBuilder().setToward(index, ret.getToward());
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                entity.getDb_data().getMazeDataBuilder().getMazeRecordDataBuilder().addLevel(ret.getLevel()).addToward(ret.getToward());
            }
            SC_UpdateMistMazeRecord.Builder builder = SC_UpdateMistMazeRecord.newBuilder();
            builder.setMazeRecordData(entity.getDb_data().getMazeDataBuilder().getMazeRecordDataBuilder());
            GlobalData.getInstance().sendMsg(entity.getIdx(), MsgIdEnum.SC_UpdateMistMazeRecord_VALUE, builder);
        });
    }
}
