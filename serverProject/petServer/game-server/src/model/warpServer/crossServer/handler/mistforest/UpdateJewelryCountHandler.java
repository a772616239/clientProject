package model.warpServer.crossServer.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_UpdateJewelryCountData;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateJewelryCountData_VALUE)
public class UpdateJewelryCountHandler extends AbstractHandler<CS_GS_UpdateJewelryCountData> {
    @Override
    protected CS_GS_UpdateJewelryCountData parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateJewelryCountData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateJewelryCountData ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> {
            entity.getDb_data().getMistForestDataBuilder().setOfflineJewelryCount(ret.getNewJewelryCount());
            entity.getDb_data().getMistForestDataBuilder().setOfflineJewelryMistLevel(ret.getMistLevel());
        });
    }
}
