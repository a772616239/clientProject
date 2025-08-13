package model.warpServer.crossServer.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_ReqChangeMistStamina;

@MsgId(msgId = MsgIdEnum.CS_GS_ReqChangeMistStamina_VALUE)
public class ChangeMistStaminaHandler extends AbstractHandler<CS_GS_ReqChangeMistStamina> {
    @Override
    protected CS_GS_ReqChangeMistStamina parse(byte[] bytes) throws Exception {
        return CS_GS_ReqChangeMistStamina.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_ReqChangeMistStamina ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        if (ret.getChangeValue() == 0) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> {
            if (ret.getChangeValue() > 0) {
                entity.addMistStamina(ret.getChangeValue(), false);
            } else {
                entity.removeMistStamina(-ret.getChangeValue());
            }
        });
    }
}
