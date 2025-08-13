package server.handler.mistforest.ghostBuster;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_PlayerGhostBusterRecord;
import protocol.MistForest.SC_PlayerGhostBusterRecord;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_PlayerGhostBusterRecord_VALUE)
public class PlayerGhostBusterRecorHandler extends AbstractBaseHandler<CS_PlayerGhostBusterRecord> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistGhostBuster;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_PlayerGhostBusterRecord_VALUE, SC_PlayerGhostBusterRecord.newBuilder());
    }

    @Override
    protected CS_PlayerGhostBusterRecord parse(byte[] bytes) throws Exception {
        return CS_PlayerGhostBusterRecord.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PlayerGhostBusterRecord req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        SC_PlayerGhostBusterRecord.Builder builder = SC_PlayerGhostBusterRecord.newBuilder();
        builder.setHighestRecord(player.getDb_data().getGhostBusterDataBuilder().getHighestRecord());
        builder.addAllRecentRecords(player.getDb_data().getGhostBusterDataBuilder().getRecentRecordsList());
        gsChn.send(MsgIdEnum.SC_PlayerGhostBusterRecord_VALUE, builder);
    }

}
