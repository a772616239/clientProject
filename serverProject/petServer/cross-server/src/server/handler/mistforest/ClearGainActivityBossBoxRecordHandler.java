package server.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_ClearRewardAndRankRecord;

@MsgId(msgId = MsgIdEnum.GS_CS_ClearRewardAndRankRecord_VALUE)
public class ClearGainActivityBossBoxRecordHandler extends AbstractHandler<GS_CS_ClearRewardAndRankRecord> {
    @Override
    protected GS_CS_ClearRewardAndRankRecord parse(byte[] bytes) throws Exception {
        return GS_CS_ClearRewardAndRankRecord.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ClearRewardAndRankRecord req, int i) {
        MistPlayer player = MistPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> entity.setGainActivityBossBoxFlag(false));
    }
}
