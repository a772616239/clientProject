package server.handler.resRecycle.impl;

import cfg.BossTowerConfig;
import cfg.BossTowerConfigObject;
import cfg.ResourceRecycleRewardCfg;
import common.SyncExecuteFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.bosstower.dbCache.bosstowerCache;
import model.bosstower.entity.bosstowerEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common;
import server.handler.resRecycle.ResourceRecycle;
import server.handler.resRecycle.ResourceRecycleInterface;

/**
 * boss塔资源回收
 */
@ResourceRecycle(function = Common.EnumFunction.BossTower)
public class BossTowerRecycle implements ResourceRecycleInterface {

    @Override
    public void resourceRecycle(String playerId, int settleInterval) {
        bosstowerEntity entity = bosstowerCache.getInstance().getEntity(playerId);
        playerEntity player = playerCache.getByIdx(playerId);
        if (entity == null || player == null) {
            return;
        }
        boolean yesterdayPlay = yesterdayPlay(entity, settleInterval);
        List<Common.Reward> rewards = calculateRewards(entity, yesterdayPlay);
        if (CollectionUtils.isEmpty(rewards)) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, target -> {
            player.addResRecycle(Common.EnumFunction.BossTower, rewards);
        });
    }

    private List<Common.Reward> calculateRewards(bosstowerEntity entity, boolean yesterdayPlay) {
        int maxCfgId = entity.getDbBuilder().getMaxCfgId();
        if (maxCfgId <= 0) {
            return Collections.emptyList();
        }
        List<Common.Reward> rewards = new ArrayList<>();
        List<Common.Reward> tempRewards;
        int canBattleTime = getCanBattleTime(entity, maxCfgId, yesterdayPlay);
        if (canBattleTime > 0) {
            for (int i = 0; i < canBattleTime; i++) {
                tempRewards = ResourceRecycleRewardCfg.getInstance().getByFunctionAndPoint(Common.EnumFunction.BossTower_VALUE, maxCfgId);
                rewards.addAll(RewardUtil.multiReward(tempRewards, canBattleTime));
            }
        }
        return RewardUtil.mergeReward(rewards);
    }

    private int getCanBattleTime(bosstowerEntity entity, int point, boolean yesterdayPlay) {
        if (yesterdayPlay) {
            return entity.getCanBattleTime(point);
        }
        BossTowerConfigObject cfg = BossTowerConfig.getById(point);
        if (cfg != null) {
            return cfg.getPasslimit();
        }
        return 0;
    }

    private boolean yesterdayPlay(bosstowerEntity entity, int settleInterval) {
        return entity.getDbBuilder().getTodayAlreadyChallengeTimes() > 0 && settleInterval <= 1;
    }
}
