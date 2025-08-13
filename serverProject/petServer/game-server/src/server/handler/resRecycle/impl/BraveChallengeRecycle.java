package server.handler.resRecycle.impl;

import cfg.ResourceRecycleCfg;
import common.SyncExecuteFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import model.bravechallenge.dbCache.bravechallengeCache;
import model.bravechallenge.entity.bravechallengeEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.BraveChallenge;
import protocol.Common;
import server.handler.resRecycle.ResourceRecycle;
import server.handler.resRecycle.ResourceRecycleInterface;

@ResourceRecycle(function = Common.EnumFunction.CourageTrial)
public class BraveChallengeRecycle implements ResourceRecycleInterface {

    @Override
    public void resourceRecycle(String playerId, int settleInterval) {
        bravechallengeEntity bravechallengeEntity = bravechallengeCache.getInstance().getEntityByPlayer(playerId);
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        boolean yesterdayPlay = yesterdayPlay(bravechallengeEntity, settleInterval);
        List<Common.Reward> rewards = calculateOneDayBraveChallengeRewards(bravechallengeEntity, yesterdayPlay);
        SyncExecuteFunction.executeConsumer(player, target -> {
            player.addResRecycle(Common.EnumFunction.CourageTrial, rewards);
        });
    }

    private boolean yesterdayPlay(bravechallengeEntity bravechallengeEntity, int settleInterval) {
        return !bravechallengeEntity.todayFirstPlay() && settleInterval <= 1;
    }

    private List<Common.Reward> calculateOneDayBraveChallengeRewards(bravechallengeEntity bravechallengeEntity, boolean yesterdayPlay) {
        if (yesterdayPlay) {
            return Collections.emptyList();
        }
        List<Common.Reward> rewards = new ArrayList<>();
        Map<Integer, BraveChallenge.BravePoint> pointCfgMap = bravechallengeEntity.getProgressBuilder().getPointCfgMap();
        BraveChallenge.BravePoint bravePoint;
        for (int i = 1; i <= ResourceRecycleCfg.getBraveChallengePoint(); i++) {
            if ((bravePoint = pointCfgMap.get(i)) != null) {
                rewards.addAll(bravePoint.getRewardsList());
            }
        }
        return rewards;
    }


}
