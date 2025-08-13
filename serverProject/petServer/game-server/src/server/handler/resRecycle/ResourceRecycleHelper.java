package server.handler.resRecycle;

import cfg.ResourceCopy;
import cfg.ResourceCopyConfig;
import cfg.ResourceCopyObject;
import common.SyncExecuteFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import protocol.Common;
import protocol.PlayerDB;

import static protocol.ResourceCopy.ResourceCopyTypeEnum.RCTE_Awaken_VALUE;
import static protocol.ResourceCopy.ResourceCopyTypeEnum.RCTE_Crystal_VALUE;
import static protocol.ResourceCopy.ResourceCopyTypeEnum.RCTE_Gold_VALUE;
import static protocol.ResourceCopy.ResourceCopyTypeEnum.RCTE_Rune_VALUE;
import static protocol.ResourceCopy.ResourceCopyTypeEnum.RCTE_SoulStone_VALUE;

public class ResourceRecycleHelper {

    public static void resourceCopyRecycle(String playerId, Common.EnumFunction function, int resourceOfflineDay) {
        playerEntity player = playerCache.getByIdx(playerId);
        int resType = getFunctionByResType(function);
        if (resType == -1 || player == null) {
            return;
        }

        List<Common.Reward> rewards = calculateResCopyLastDayRemainRewards(player, resType, resourceOfflineDay);

        SyncExecuteFunction.executeConsumer(player, target -> {
            player.addResRecycle(function, rewards);
        });
    }

    /**
     * 计算资源副本前一日剩余未扫荡的资源(按最大算)
     *
     * @param player
     * @param resType
     * @param resourceOfflineDay
     * @return
     */
    private static List<Common.Reward> calculateResCopyLastDayRemainRewards(playerEntity player, int resType, int resourceOfflineDay) {

        PlayerDB.DB_ResourceCopy.Builder resourceCopy = player.getResourceCopyData(resType);

        //玩家最大可扫荡的层数
        int canMaxSweepProgressIndex = player.queryCanMaxSweepProgress(resType);

        if (canMaxSweepProgressIndex <= 0) {
            return Collections.emptyList();
        }
        ResourceCopyObject cfg = ResourceCopy.getInstance().getCopyCfgByTypeAndIndex(resType, canMaxSweepProgressIndex);

        //剩余扫荡次数
        int remainSweepTimes = resourceOfflineDay > 1 ? ResourceCopyConfig.getById(resType).getChallengetimes() :
                ResourceCopyConfig.getById(resType).getChallengetimes() - resourceCopy.getChallengeTimes();

        return calculateRewards(player, cfg, remainSweepTimes);

    }

    private static List<Common.Reward> calculateRewards(playerEntity player, ResourceCopyObject cfg, int remainSweepTimes) {
        if (remainSweepTimes <= 0) {
            return Collections.emptyList();
        }
        List<Common.Reward> rewards = RewardUtil.getRewardsByFightMakeId(cfg.getFightmakeid());

        rewards = RewardUtil.additionResourceCopyRewardByVip(player.getVip(), rewards);

        if (remainSweepTimes != 1) {
            rewards = RewardUtil.multiReward(rewards, remainSweepTimes);
        }
        return rewards;
    }

    public static int getFunctionByResType(Common.EnumFunction function) {
        switch (function) {
            case RelicsRes:
                return RCTE_Crystal_VALUE;
            case SoulRes:
                return RCTE_SoulStone_VALUE;
            case ArtifactRes:
                return RCTE_Rune_VALUE;
            case RuinsRes:
                return RCTE_Awaken_VALUE;
            case GoldenRes:
                return RCTE_Gold_VALUE;
        }

        return -1;
    }

    public static List<Common.Reward> parseResourceReward(PlayerDB.DB_ResourceRecycleItem item) {
        List<Common.Reward> rewards = new ArrayList<>();
        for (PlayerDB.DB_OnceResourceCycleInfo info : item.getRecycleInfoList()) {
            for (Common.ListReward listReward : info.getRewardList()) {
                rewards.addAll(listReward.getRewardList());
            }
        }
        return RewardUtil.mergeReward(rewards);
    }

}
