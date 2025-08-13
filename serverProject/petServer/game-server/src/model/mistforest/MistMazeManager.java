package model.mistforest;

import cfg.MailTemplateUsed;
import cfg.Mission;
import cfg.MissionObject;
import common.GameConst;
import common.GameConst.EventType;
import common.GameConst.RedisKey;
import common.GlobalData;
import common.JedisUtil;
import static common.JedisUtil.jedis;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common.MissionStatusEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.SC_MazeItemCollectCount;
import protocol.MistForest.SC_MistMazeBuyGoodsTimes;
import protocol.MistForest.SC_UpdateMistMazeRecord;
import protocol.Server.ServerActivity;
import protocol.TargetSystem.TargetMission;
import protocol.TransServerCommon.MistMazeSyncData;
import protocol.TransServerCommon.PlayerMistServerInfo;
import server.event.Event;
import server.event.EventManager;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

@Getter
@Setter
public class MistMazeManager {
    MistMazeSyncData.Builder mazeSyncData;
    protected long nextTickTime;
    protected Map<String, Integer> playerJoinedMazeMap;

    public MistMazeManager() {
        mazeSyncData = MistMazeSyncData.newBuilder();
        playerJoinedMazeMap = new ConcurrentHashMap<>();
    }

    public boolean isOpen() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        return mazeSyncData.getStartTime() <= curTime && mazeSyncData.getEndTime() > curTime;
    }

    public void addPlayerInMazeMap(String playerIdx, int serverIndex) {
        playerJoinedMazeMap.put(playerIdx, serverIndex);
    }

    public void removePlayerInMazeMap(String playerIdx) {
        if (!playerJoinedMazeMap.containsKey(playerIdx)) {
            return;
        }
        playerJoinedMazeMap.remove(playerIdx);
    }

    public void refreshMazePlayerData() {
        playerEntity player;
        for (String playerIdx : playerJoinedMazeMap.keySet()) {
            player = playerCache.getByIdx(playerIdx);
            if (player == null) {
                continue;
            }
            SyncExecuteFunction.executeConsumer(player, entity -> {
                entity.getDb_data().getMazeDataBuilder().clearMazeRecordData().clearMistMazeDailyGainRewards().clearBuyGoodsTimes();


                PlayerMistServerInfo info = CrossServerManager.getInstance().getMistForestPlayerServerInfo(entity.getIdx());
                if (info != null && info.getMistRule() == EnumMistRuleKind.EMRK_Maze) {
                    entity.sendMistDailyRewardInfoByRule(EnumMistRuleKind.EMRK_Maze_VALUE);

                    SC_UpdateMistMazeRecord.Builder recordBuilder = SC_UpdateMistMazeRecord.newBuilder();
                    recordBuilder.getMazeRecordDataBuilder().setRecordRefreshed(true);
                    GlobalData.getInstance().sendMsg(entity.getIdx(), MsgIdEnum.SC_UpdateMistMazeRecord_VALUE, recordBuilder);

                    SC_MistMazeBuyGoodsTimes.Builder buyTimesBuilder = SC_MistMazeBuyGoodsTimes.newBuilder();
                    GlobalData.getInstance().sendMsg(entity.getIdx(), MsgIdEnum.SC_MistMazeBuyGoodsTimes_VALUE, buyTimesBuilder);
                }
            });
        }
    }

    public void settleMazeActivity() {
        try {
            byte[] bytesData = jedis.get(RedisKey.MistMazeSyncData.getBytes());
            if (bytesData == null) {
                return;
            }
            MistMazeSyncData mazeData = MistMazeSyncData.parseFrom(bytesData);
            long curTime = GlobalTick.getInstance().getCurrentTime();
            if (mazeData.getActivityId() != mazeSyncData.getActivityId()) {
                return;
            }
            if (mazeData.getStartTime() <= curTime && mazeData.getEndTime() > curTime) {
                return;
            }
            if (JedisUtil.lockRedisKey(RedisKey.MistMazeUpdateLock,5000l)) {
                jedis.set(RedisKey.MistMazeSyncData.getBytes(), null);
                JedisUtil.unlockRedisKey(RedisKey.MistMazeUpdateLock);
            }
            playerEntity player;
            targetsystemEntity targetEntity;
            for (String playerIdx : playerJoinedMazeMap.keySet()) {
                player = playerCache.getByIdx(playerIdx);
                if (player != null) {
                    SyncExecuteFunction.executeConsumer(player, entity -> {
                        entity.getDb_data().getMazeDataBuilder().clearMazeItemCollectCount();
                        GlobalData.getInstance().sendMsg(entity.getIdx(), MsgIdEnum.SC_MazeItemCollectCount_VALUE, SC_MazeItemCollectCount.newBuilder());
                    });
                }
                targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
                if (targetEntity != null) {
                    SyncExecuteFunction.executeConsumer(targetEntity, entity -> {
                        MissionObject mission;
                        List<Reward> rewardList = new ArrayList<>();
                        for (TargetMission targetMission : entity.getDb_Builder().getSpecialInfo().getMazeActivityMission().getMissionProList()) {
                            if (targetMission.getStatus() != MissionStatusEnum.MSE_Finished) {
                                continue;
                            }
                            mission = Mission.getById(targetMission.getCfgId());
                            if (mission == null) {
                                continue;
                            }
                            List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(mission.getFinishreward());
                            if (CollectionUtils.isEmpty(rewards)) {
                                continue;
                            }
                            rewardList.addAll(rewards);
                        }
                        List<Reward> totalRewards = RewardUtil.mergeReward(rewardList);
                        if (!CollectionUtils.isEmpty(totalRewards)) {
                            EventUtil.triggerAddMailEvent(playerIdx, MailTemplateUsed.getById(GameConst.CONFIG_ID).getMazemission(),
                                    totalRewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistMaze));
                        }
                        entity.getDb_Builder().getSpecialInfoBuilder().clearMazeActivityMission();
                    });
                }
            }
            playerJoinedMazeMap.clear();
            mazeSyncData.clear();
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    protected boolean needUpdateLocalData(ServerActivity activity) {
        if (activity == null) {
            return false;
        }
        if (mazeSyncData.getActivityId() != activity.getActivityId()) {
            return true;
        }
        if (mazeSyncData.getStartTime() != activity.getBeginTime()) {
            return true;
        }
        if (mazeSyncData.getEndTime() != activity.getEndTime()) {
            return true;
        }
        if (mazeSyncData.getRefreshInterval() != activity.getMazeRefreshInterval()) {
            return true;
        }
        return false;
    }

    protected boolean needUpdateSyncData(MistMazeSyncData mazeData) {
        if (mazeData == null) {
            return true;
        }
        if (mazeData.getActivityId() != mazeSyncData.getActivityId()) {
            return true;
        }
        if (mazeData.getStartTime() != mazeSyncData.getStartTime()) {
            return true;
        }
        if (mazeData.getEndTime() != mazeSyncData.getEndTime()) {
            return true;
        }
        if (mazeData.getRefreshInterval() != mazeSyncData.getRefreshInterval()) {
            return true;
        }
        return false;
    }

    public void updateMazeSyncData(ServerActivity activity) {
        if (activity == null) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (activity.getBeginTime() > curTime || activity.getEndTime() <= curTime) {
            return;
        }
        if (!needUpdateLocalData(activity)) {
            return;
        }
        mazeSyncData.setActivityId(activity.getActivityId());
        mazeSyncData.setStartTime(activity.getBeginTime());
        mazeSyncData.setEndTime(activity.getEndTime());
        mazeSyncData.setRefreshInterval(activity.getMazeRefreshInterval());
        if (mazeSyncData.getActivityId() != activity.getActivityId()) {
            mazeSyncData.setNextRefreshTime(0);
            mazeSyncData.clearMazeServerRecord();
        }
        try {
            byte[] bytesData = jedis.get(RedisKey.MistMazeSyncData.getBytes());
            MistMazeSyncData mazeData = null;
            if (bytesData != null) {
                 mazeData = MistMazeSyncData.parseFrom(bytesData);
            }
            if (!needUpdateSyncData(mazeData)) {
                return;
            }
            if (!JedisUtil.lockRedisKey(RedisKey.MistMazeUpdateLock,5000l)) {
                return;
            }
            if (mazeData != null && mazeData.getActivityId() == mazeSyncData.getActivityId()) {
                mazeSyncData.addAllMazeServerRecord(mazeData.getMazeServerRecordList());
            }
            jedis.set(RedisKey.MistMazeSyncData.getBytes(), mazeSyncData.build().toByteArray());
            JedisUtil.unlockRedisKey(RedisKey.MistMazeUpdateLock);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public void onTick(long curTime) {
        if (nextTickTime > curTime || mazeSyncData.getActivityId() == 0) {
            return;
        }
        nextTickTime = curTime + TimeUtil.MS_IN_A_S;
        if (mazeSyncData.getEndTime() <= curTime || mazeSyncData.getStartTime() > curTime) {
            if (mazeSyncData.getActivityId() > 0) {
                Event event = Event.valueOf(EventType.ET_SettleMazeActivity, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
                EventManager.getInstance().dispatchEvent(event);
            }
            return;
        }
        if (mazeSyncData.getNextRefreshTime() > curTime) {
            return;
        }
        if (mazeSyncData.getNextRefreshTime() > 0) {
            Event event = Event.valueOf(EventType.ET_RefreshMazeData, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
            EventManager.getInstance().dispatchEvent(event);
        }
        mazeSyncData.setNextRefreshTime(Math.min(mazeSyncData.getEndTime(), curTime + mazeSyncData.getRefreshInterval() * TimeUtil.MS_IN_A_HOUR));
    }
}
