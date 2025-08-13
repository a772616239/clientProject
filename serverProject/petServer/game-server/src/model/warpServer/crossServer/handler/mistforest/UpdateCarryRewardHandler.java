package model.warpServer.crossServer.handler.mistforest;

import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.Map.Entry;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.SC_UpdateMistCarryInfo;
import protocol.MistForest.SC_UpdateMistLootPackInfo;
import protocol.ServerTransfer.CS_GS_UpdateCarryReward;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateCarryReward_VALUE)
public class UpdateCarryRewardHandler extends AbstractHandler<CS_GS_UpdateCarryReward> {
    @Override
    protected CS_GS_UpdateCarryReward parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateCarryReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateCarryReward req, int i) {
        playerEntity player = playerCache.getByIdx(req.getIdx());
        if (player == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> {
            switch (req.getMistRule()) {
                case EMRK_Common: {
                    entity.getDb_data().getMistForestDataBuilder().putAllMistCarryRewards(req.getDeltaCarryRewardsMap());
                    break;
                }
                case EMRK_Maze: {
                    entity.getDb_data().getMazeDataBuilder().putAllMistMazeCarryRewards(req.getDeltaCarryRewardsMap());
                    break;
                }
                case EMRK_GhostBuster: {
                    entity.getDb_data().getGhostBusterDataBuilder().putAllMistGhostCarryRewards(req.getDeltaCarryRewardsMap());
                    break;
                }
                default:
                    return;
            }

            SC_UpdateMistLootPackInfo.Builder lootPackBuilder = SC_UpdateMistLootPackInfo.newBuilder();
            lootPackBuilder.setFullUpdate(false);
            lootPackBuilder.setRule(req.getMistRule());

            SC_UpdateMistCarryInfo.Builder dailyRewardBuilder = SC_UpdateMistCarryInfo.newBuilder();
            dailyRewardBuilder.setFullUpdate(false);
            dailyRewardBuilder.setRule(req.getMistRule());

            for (Entry<Integer, Integer> entry : req.getDeltaCarryRewardsMap().entrySet()) {
                int count = entry.getValue();
                lootPackBuilder.getItemDictBuilder().addCarryRewardId(entry.getKey()).addCount(count);

                int limit = entity.getMistDailyConfigLimit(req.getMistRuleValue(), entry.getKey());
                if (limit == 0) {
                    continue;
                }
                count += entity.getMistDailyRewardCount(req.getMistRuleValue(), entry.getKey());
                dailyRewardBuilder.getCarryInfoDictBuilder()
                        .addCarryRewardId(entry.getKey())
                        .addCarryCount(count)
                        .addCarryLimit(limit);
            }
            GlobalData.getInstance().sendMsg(entity.getIdx(), MsgIdEnum.SC_UpdateMistLootPackInfo_VALUE, lootPackBuilder);
            if (dailyRewardBuilder.getCarryInfoDictBuilder().getCarryCountCount() > 0) {
                GlobalData.getInstance().sendMsg(entity.getIdx(), MsgIdEnum.SC_UpdateMistCarryInfo_VALUE, dailyRewardBuilder);
            }
        });
    }
}
