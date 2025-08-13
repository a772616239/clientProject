package model.warpServer.crossServer.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_MistForestData.Builder;
import protocol.ServerTransfer.CS_GS_UpdateOfflineBuffs;
import protocol.ServerTransfer.MistOfflineBuffData;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateOfflineBuffs_VALUE)
public class UpdateOfflineBuffHandler extends AbstractHandler<CS_GS_UpdateOfflineBuffs> {
    @Override
    protected CS_GS_UpdateOfflineBuffs parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateOfflineBuffs.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateOfflineBuffs ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getIdx());
        if (player == null) {
            return;
        }
        LogUtil.info("UpdateMistOfflineBuff playerIdx={},buffId={},buffLayer={},buffExpireTime={}",
                player.getIdx(), ret.getOfflineBuff().getBuffId(), ret.getOfflineBuff().getBuffLayer(), ret.getOfflineBuff().getBuffExpireTime());
        SyncExecuteFunction.executeConsumer(player, entity -> {
            boolean existFlag = false;
            Builder mistBuilder = entity.getDb_data().getMistForestDataBuilder();
            for (int index = 0; index < mistBuilder.getOfflineBuffsCount(); index++) {
                MistOfflineBuffData.Builder buffData = mistBuilder.getOfflineBuffsBuilder(index);
                if (buffData.getBuffId() == ret.getOfflineBuff().getBuffId()) {
                    existFlag = true;
                    if (ret.getOfflineBuff().getBuffExpireTime() > 0) {
                        buffData.setBuffLayer(ret.getOfflineBuff().getBuffLayer());
                        buffData.setBuffExpireTime(ret.getOfflineBuff().getBuffExpireTime());
                    } else {
                        mistBuilder.removeOfflineBuffs(index);
                    }
                    break;
                }
            }
            if (!existFlag && ret.getOfflineBuff().getBuffExpireTime() > 0) {
                mistBuilder.addOfflineBuffs(ret.getOfflineBuff());
            }
        });
    }
}
