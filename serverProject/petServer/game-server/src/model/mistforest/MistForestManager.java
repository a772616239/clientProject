package model.mistforest;

import cfg.MistSeasonConfig;
import cfg.MistSeasonConfigObject;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.GameConst.EventType;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import common.tick.Tickable;
import lombok.Getter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.crossServer.CrossServerManager;
import util.EventUtil;
import util.LogUtil;

@Getter
public class MistForestManager implements Tickable {
    private static MistForestManager instance = new MistForestManager();

    private MistSeasonConfigObject curSeason;
    private boolean seasonOpen;

    private MistMazeManager mazeManager;

    private MistGhostBusterManager ghostBusterManager;

    private MistBossActivityManager bossActivityManager;

    public static MistForestManager getInstance() {
        return instance;
    }


    public boolean init() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        curSeason = MistSeasonConfig.getCurSeasonConfig(curTime);
        if (curSeason != null) {
            if (curSeason.getStarttime() <= curTime && curSeason.getEndtime() > curTime) {
                seasonOpen = true;
            }
        }

        bossActivityManager = new MistBossActivityManager();
        bossActivityManager.init();
        mazeManager = new MistMazeManager();
        ghostBusterManager = new MistGhostBusterManager();
        return true;
    }

    public long getSeasonStartTime() {
        return curSeason != null ? curSeason.getStarttime() : 0;
    }

    public long getSeasonEndTime() {
        return curSeason != null ? curSeason.getEndtime() : 0;
    }

    public boolean isCurrentSeasonOpen(long curTime) {
        if (curSeason == null) {
            return false;
        }
        return curSeason.getStarttime() <= curTime && curSeason.getEndtime() > curTime;
    }

    protected void openSeason() {
        if (curSeason == null) {
            return;
        }
        // 结算上赛季奖励
        try {
//            int openLv = FunctionOpenLvConfig.getOpenLv(EnumFunction.MistForest);
//            for (BaseEntity baseObj : playerCache.getInstance().getAll().values()) {
//                playerEntity player = (playerEntity) baseObj;
//                if (player.getLevel() < openLv) {
//                    continue;
//                }
//
//                Event event = Event.valueOf(EventType.ET_MistForestSeasonEnd, GameUtil.getDefaultEventSource(), player);
//                EventManager.getInstance().dispatchEvent(event);
//            }

            //清空迷雾深林目标进度
            EventUtil.unlockObjEvent(EventType.ET_ClearAllMistTargetProgress);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public void onPlayerLogin(playerEntity player) {
        if (player == null) {
            return;
        }
        if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(player.getIdx()) <= 0) {
            SyncExecuteFunction.executeConsumer(player, entity -> entity.settleMistCarryReward());
        }
    }

    @Override
    public void onTick() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (curSeason != null) {
            if (seasonOpen && curSeason.getEndtime() <= curTime) {
                seasonOpen = false;
                curSeason = MistSeasonConfig.getCurSeasonConfig(curTime);
            } else if (!seasonOpen && curSeason.getStarttime() <= curTime) {
                openSeason();
                seasonOpen = true;
            }
        }
        if (bossActivityManager != null) {
            bossActivityManager.onTick(curTime);
        }
        if (mazeManager != null) {
            mazeManager.onTick(curTime);
        }
        if (ghostBusterManager != null) {
            ghostBusterManager.onTick(curTime);
        }
    }

    /**
     * vip升级后增加免费入场次数
     *
     * @param playerIdx   玩家id
     * @param beforeVipLv 之前vip等级
     * @param afterVipLv  当前vip等级
     */
    public void addFreeTickByVipLevelUp(String playerIdx, int beforeVipLv, int afterVipLv) {
        VIPConfigObject beforeConfig = VIPConfig.getById(beforeVipLv);
        VIPConfigObject afterConfig = VIPConfig.getById(afterVipLv);
        if (beforeConfig == null || afterConfig == null) {
            LogUtil.error("MisForestMange.updateDataByVipLevelUp,vipConfig not fond by id:{}|{}", beforeVipLv, afterVipLv);
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("MisForestMange.updateDataByVipLevelUp,player not found by id:{}", playerIdx);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, cache -> {
            int addFreeCount = afterConfig.getGhostbusterfreecount() - beforeConfig.getGhostbusterfreecount();
            cache.getDb_data().getGhostBusterDataBuilder().setFreeTickets(cache.getDb_data().getGhostBusterDataBuilder().getFreeTickets() + addFreeCount);
            LogUtil.info("player[{}] vipLvUp {}--->{} add freeGhostBusterTicket{}", playerIdx, beforeVipLv, afterVipLv, addFreeCount);
        });

    }
}
