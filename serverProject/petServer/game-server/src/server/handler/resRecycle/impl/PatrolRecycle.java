/*
package server.handler.resRecycle.impl;

import cfg.ResourceRecycleCfg;
import common.SyncExecuteFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.mainLine.dbCache.mainlineCache;
import model.patrol.dbCache.patrolCache;
import model.patrol.entity.PatrolTree;
import model.patrol.entity.patrolEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import org.springframework.util.CollectionUtils;
import protocol.Common;
import protocol.Patrol;
import server.handler.resRecycle.ResourceRecycle;
import server.handler.resRecycle.ResourceRecycleInterface;
import util.LogUtil;
import util.PatrolUtil;

*/
/** 虚空秘境找回逻辑游戏逻辑上存在问题,找回来玩家还可以接着玩,或者玩家不玩,该次虚空秘境无限找回.因此功能先屏蔽
 * 巡逻队资源回收
 *//*

@ResourceRecycle(function = Common.EnumFunction.Patrol)
public class PatrolRecycle implements ResourceRecycleInterface {

    private static final Common.EnumFunction function = Common.EnumFunction.Patrol;

    @Override
    public void resourceRecycle(String playerId, int resourceOfflineDay) {
        patrolEntity patrolEntity = patrolCache.getInstance().getCacheByPlayer(playerId);
        playerEntity player = playerCache.getByIdx(playerId);
        if (patrolEntity == null || player == null) {
            return;
        }
        boolean yesterdayPlay = yesterdayPlay(patrolEntity, resourceOfflineDay);
        recyclePatrolSource(playerId, yesterdayPlay);

    }

    private boolean yesterdayPlay(patrolEntity patrolEntity, int resourceOfflineDay) {
        return lastTimePlay(patrolEntity) && resourceOfflineDay <= 1;
    }

    private boolean lastTimePlay(patrolEntity patrolEntity) {
        Patrol.PatrolStatus status = patrolEntity.getPatrolStatusEntity();
        return !CollectionUtils.isEmpty(status.getRewardList())
                || status.getFailure() >= 1
                || patrolEntity.getTodayCreateCount() > 1;
    }

    public static void recyclePatrolSource(String playerId, boolean yesterdayPlay) {
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        List<Common.Reward> rewards = calculateOneDayPatrolReward(playerId, yesterdayPlay);
        SyncExecuteFunction.executeConsumer(player, target -> {
            player.addResRecycle(Common.EnumFunction.Patrol, rewards);
        });
    }

    private static List<Common.Reward> calculateOneDayPatrolReward(String playerId, boolean yesterdayPlay) {

        if (yesterdayPlay) {
            return Collections.emptyList();
        }

        List<Common.Reward> result = new ArrayList<>();

        int patrolTreasure = ResourceRecycleCfg.getPatrolTreasure();

        int mainLinePoint = mainlineCache.getInstance().getCurOnHookNode(playerId);

        List<Common.Reward> patrolTreasureBaseRewards = PatrolUtil.getPatrolTreasureBaseRewards(mainLinePoint);

        List<Common.Reward> fightRewards = PatrolUtil.patrolBattleFixReward(mainLinePoint, PatrolTree.EVENT_BASTARD);

        if (CollectionUtils.isEmpty(fightRewards) || CollectionUtils.isEmpty(patrolTreasureBaseRewards)) {
            LogUtil.error("PatrolRecycle player:{} calculateOneDayPatrolReward empty", playerId);
            return Collections.emptyList();
        }

        result.addAll(RewardUtil.multiReward(patrolTreasureBaseRewards, patrolTreasure));
        result.addAll(RewardUtil.multiReward(fightRewards, ResourceRecycleCfg.getPatrolFightNum()));

        return RewardUtil.mergeReward(result);
    }

}
*/
