package server.handler;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_ClearEliteMonsterRewardTimes;

@MsgId(msgId = MsgIdEnum.GS_CS_ClearEliteMonsterRewardTimes_VALUE)
public class ClearEliteMonsterRewardTimesHandler extends AbstractHandler<GS_CS_ClearEliteMonsterRewardTimes> {
    @Override
    protected GS_CS_ClearEliteMonsterRewardTimes parse(byte[] bytes) throws Exception {
        return GS_CS_ClearEliteMonsterRewardTimes.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ClearEliteMonsterRewardTimes req, int i) {
        MistPlayer player = MistPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> entity.setEliteMonsterRewardTimes(0));
    }
}
