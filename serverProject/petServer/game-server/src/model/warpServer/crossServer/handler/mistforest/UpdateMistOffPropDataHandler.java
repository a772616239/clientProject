package model.warpServer.crossServer.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_UpdateOffPropData;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateOffPropData_VALUE)
public class UpdateMistOffPropDataHandler extends AbstractHandler<CS_GS_UpdateOffPropData> {
    @Override
    protected CS_GS_UpdateOffPropData parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateOffPropData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateOffPropData ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> {
            if (ret.getOffPropDataCount() > 0) {
                entity.getDb_data().getMistForestDataBuilder().putAllOfflinePropData(ret.getOffPropDataMap());
            }
            if (ret.getSelfOffPropDataCount() > 0) {
                entity.getDb_data().getMistForestDataBuilder().putAllSelfOffPropData(ret.getSelfOffPropDataMap());
            }
            for (Integer propType : ret.getRemoveOffPropDataList()) {
                if (entity.getDb_data().getMistForestData().containsOfflinePropData(propType)) {
                    entity.getDb_data().getMistForestDataBuilder().removeOfflinePropData(propType);
                }
            }
            for (Integer selfPropType : ret.getRemoveSelfOffPropDataList()) {
                if (entity.getDb_data().getMistForestData().containsSelfOffPropData(selfPropType)) {
                    entity.getDb_data().getMistForestDataBuilder().removeSelfOffPropData(selfPropType);
                }
            }
        });
    }
}
