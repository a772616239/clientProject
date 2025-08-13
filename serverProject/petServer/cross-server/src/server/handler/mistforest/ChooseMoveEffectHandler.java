package server.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.ServerTransfer.GS_CS_ChooseMoveEffect;

@MsgId(msgId = MsgIdEnum.GS_CS_ChooseMoveEffect_VALUE)
public class ChooseMoveEffectHandler extends AbstractHandler<GS_CS_ChooseMoveEffect> {
    @Override
    protected GS_CS_ChooseMoveEffect parse(byte[] bytes) throws Exception {
        return GS_CS_ChooseMoveEffect.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ChooseMoveEffect req, int i) {
        MistPlayer player = MistPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        if (player.getMistRoom() == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(player.getMistRoom(), room -> {
            MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
            if (fighter == null) {
                return;
            }
            fighter.addMoveEffectBuff(req.getMoveEffectId(), false);
        });
    }
}
