package model.warpServer.crossServer.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistAlchemyData;
import protocol.MistForest.MistAlchemyData.Builder;
import protocol.ServerTransfer.CS_GS_UpdateAlchemyData;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateAlchemyData_VALUE)
public class UpdateAlchemyHandler extends AbstractHandler<CS_GS_UpdateAlchemyData> {
    @Override
    protected CS_GS_UpdateAlchemyData parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateAlchemyData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateAlchemyData ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> {
            if (ret.getBAdd()) {
                boolean exist = false;
                for (Builder alchemyData : entity.getDb_data().getMistForestDataBuilder().getAlchemyDataBuilderList()) {
                    if (alchemyData.getExchangeRewardId() == ret.getExchangeRewardId()) {
                        alchemyData.clearRewardIdList();
                        alchemyData.addAllRewardIdList(ret.getRewardIdListList());
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    MistAlchemyData.Builder builder = MistAlchemyData.newBuilder();
                    builder.setExchangeRewardId(ret.getExchangeRewardId());
                    builder.addAllRewardIdList(ret.getRewardIdListList());
                    entity.getDb_data().getMistForestDataBuilder().addAlchemyData(builder);
                }
            } else {
                for (int index = 0; index < entity.getDb_data().getMistForestDataBuilder().getAlchemyDataCount(); index++) {
                    MistAlchemyData alchemyData = entity.getDb_data().getMistForestData().getAlchemyData(index);
                    if (alchemyData.getExchangeRewardId() == ret.getExchangeRewardId()) {
                        entity.getDb_data().getMistForestDataBuilder().removeAlchemyData(index);
                        break;
                    }
                }
            }
        });
    }
}
