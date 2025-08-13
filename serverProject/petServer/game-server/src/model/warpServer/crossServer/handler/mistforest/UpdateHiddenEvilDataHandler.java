package model.warpServer.crossServer.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_UpdateHiddenEvilData;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateHiddenEvilData_VALUE)
public class UpdateHiddenEvilDataHandler extends AbstractHandler<CS_GS_UpdateHiddenEvilData> {
    @Override
    protected CS_GS_UpdateHiddenEvilData parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateHiddenEvilData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateHiddenEvilData ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> {
            entity.getDb_data().getMistForestDataBuilder().setHiddenEvilId(ret.getHiddenEvilId());
            entity.getDb_data().getMistForestDataBuilder().setHiddenEvilExpireTime(ret.getHiddenEvilExpireTime());
        });
    }
}
