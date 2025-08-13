package server.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_UpdateMistStamina;

@MsgId(msgId = MsgIdEnum.GS_CS_UpdateMistStamina_VALUE)
public class UpdateMistStaminaHandler extends AbstractHandler<GS_CS_UpdateMistStamina> {
    @Override
    protected GS_CS_UpdateMistStamina parse(byte[] bytes) throws Exception {
        return GS_CS_UpdateMistStamina.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_UpdateMistStamina req, int i) {
        MistPlayer mistPlayer = MistPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (mistPlayer == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(mistPlayer, player -> player.setMistStamina(req.getNewValue()));
    }
}
