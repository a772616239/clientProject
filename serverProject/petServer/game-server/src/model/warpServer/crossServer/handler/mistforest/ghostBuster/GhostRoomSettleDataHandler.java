package model.warpServer.crossServer.handler.mistforest.ghostBuster;

import cfg.MistGhostRankReward;
import cfg.MistGhostRankRewardObject;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.warpServer.crossServer.CrossServerManager;
import platform.logs.ReasonManager;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Gameplay.SC_GhostBusterSettleData;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.GhostBusterRankData;
import protocol.ServerTransfer.CS_GS_GhostBusterRoomSettleData;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_GhostBusterRoomSettleData_VALUE)
public class GhostRoomSettleDataHandler extends AbstractHandler<CS_GS_GhostBusterRoomSettleData> {
    @Override
    protected CS_GS_GhostBusterRoomSettleData parse(byte[] bytes) throws Exception {
        return CS_GS_GhostBusterRoomSettleData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_GhostBusterRoomSettleData ret, int i) {
        playerEntity player;
        String ipPort = gsChn.channel.remoteAddress().toString().substring(1);
        LogUtil.info("recv CS_GS_GhostBusterRoomSettleData from:" + ipPort);
        SC_GhostBusterSettleData.Builder builder = SC_GhostBusterSettleData.newBuilder();
        builder.addAllRandData(ret.getRandDataList());
        for (GhostBusterRankData rankData : ret.getRandDataList()) {
            if (rankData.getExited()) {
                continue;
            }
            player = playerCache.getByIdx(rankData.getPlayerInfo().getId());
            if (player == null) {
                continue;
            }
            builder.clearRewards();
            List<Reward> rewards = RewardManager.getInstance().doRewardByRewardId(player.getIdx(), getGhostRankReward(rankData.getRank()),
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistGhostBuster), false);
            builder.addAllRewards(rewards);
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_GhostBusterSettleData_VALUE, builder);

            CrossServerManager.getInstance().removeMistForestPlayer(player.getIdx());
            SyncExecuteFunction.executeConsumer(player, entity -> {
                entity.addGhostScoreRecord(rankData, ret.getSettleTime());
                entity.settleMistCarryReward();
            });

        }
    }

    protected int getGhostRankReward(int rank) {
        MistGhostRankRewardObject cfg = null;
        if (rank > 0) {
            cfg = MistGhostRankReward.getByRank(rank);
            return cfg.getRewardid();
        } else {
            // 未上榜获得最后一名奖励
            for (MistGhostRankRewardObject config : MistGhostRankReward._ix_rank.values()) {
                if (cfg == null || config.getRank() > cfg.getRank()) {
                    cfg = config;
                }
            }
            return cfg.getRewardid();
        }
    }
}
