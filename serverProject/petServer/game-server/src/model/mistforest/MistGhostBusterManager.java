package model.mistforest;

import common.GameConst.RedisKey;
import common.GlobalData;
import common.JedisUtil;
import static common.JedisUtil.jedis;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.Setter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.collections4.MapUtils;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_StopMatchGhostBuster;
import protocol.Server.ServerActivity;
import protocol.TransServerCommon.MistGhostBusterSyncData;
import util.LogUtil;
import util.TimeUtil;

@Getter
@Setter
public class MistGhostBusterManager {
    protected long activityId;
    protected Map<String, Long> playerMatchMap = new HashMap<>();
    protected Map<String, Long> playerPreMatchMap = new HashMap<>();
    protected Map<String, Long> playerRemoveMap = new HashMap<>();

    protected Map<String, Integer> playerJoiningGhostBusterMap = new HashMap<>();

    protected long startTime;
    protected long endTime;

    protected long dailyStartTime;
    protected long dailyEndTime;

    protected long dailyStartTimeStamp;
    protected long dailyEndTimeStamp;

    protected boolean activityOpenFlag;
    protected boolean dailyOpenFlag;

    protected long updateTime;

    public void clear() {
        activityId = 0;
        startTime = 0;
        endTime = 0;
        dailyStartTime = 0;
        dailyEndTime = 0;
        dailyStartTimeStamp = 0;
        dailyEndTimeStamp = 0;
        dailyOpenFlag = false;
        activityOpenFlag = false;
        playerMatchMap.clear();
        playerPreMatchMap.clear();
        playerRemoveMap.clear();
        playerJoiningGhostBusterMap.clear();
    }

    public synchronized void updateGhostActivityData(ServerActivity serverActivity) {
        setActivityId(serverActivity.getActivityId());
        setStartTime(serverActivity.getBeginTime());
        setEndTime(serverActivity.getEndTime());
        setDailyStartTime(serverActivity.getDailyBeginTime());
        setDailyEndTime(serverActivity.getDailyEndTime());
        updateDailyTimeStamp(GlobalTick.getInstance().getCurrentTime());
    }

    protected void updateDailyTimeStamp(long curTime) {
        if (getStartTime() > curTime || getEndTime() < curTime) {
            return;
        }
        long todayStamp = TimeUtil.getTodayStamp(curTime);
        setDailyEndTimeStamp(todayStamp + getDailyEndTime() * TimeUtil.MS_IN_A_MIN);
        if (getDailyEndTimeStamp() <= curTime) {
            setDailyStartTimeStamp(todayStamp + getDailyStartTime() * TimeUtil.MS_IN_A_MIN + TimeUtil.MS_IN_A_DAY);
            setDailyEndTimeStamp(getDailyEndTimeStamp()+ TimeUtil.MS_IN_A_DAY);
        } else {
            setDailyStartTimeStamp(todayStamp + getDailyStartTime() * TimeUtil.MS_IN_A_MIN);
        }
    }

    public synchronized MistRetCode startMatch(playerEntity player) {
        if (playerMatchMap.containsKey(player.getIdx()) || playerPreMatchMap.containsKey(player.getIdx())) {
            return MistRetCode.MRC_PlayerIsMatching;
        }
        if (!isInFightTime()) {
            return MistRetCode.MRC_GhostBusterNotOpen;
        }

        if (playerJoiningGhostBusterMap.containsKey(player.getIdx())) {
            return MistRetCode.MRC_InMistForest;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        playerMatchMap.put(player.getIdx(), curTime);

        if (!JedisUtil.lockRedisKey(RedisKey.MistGhostBusterMatchLock, 5000l)) {
            playerPreMatchMap.put(player.getIdx(), curTime);
            return MistRetCode.MRC_Success;
        }
        MistGhostBusterSyncData.Builder builder = MistGhostBusterSyncData.newBuilder();
        builder.setPlayerInfo(player.getBattleBaseData());
        builder.setFromSvrIndex(ServerConfig.getInstance().getServer());
        builder.setStartMatchTime(curTime);
        jedis.hset(RedisKey.MistGhostBusterMatchData.getBytes(), player.getIdx().getBytes(), builder.build().toByteArray());
        playerMatchMap.put(player.getIdx(), curTime);
        JedisUtil.unlockRedisKey(RedisKey.MistGhostBusterMatchLock);

        LogUtil.info("GhostBuster start match info to redis");
        return MistRetCode.MRC_Success;
    }

    public synchronized MistRetCode stopMatch(playerEntity player) {
        if (playerJoiningGhostBusterMap.containsKey(player.getIdx())) {
            return MistRetCode.MRC_InMistForest;
        }
        if (isInFightTime()) {

        }
        if (playerMatchMap.containsKey(player.getIdx())) {
            long curTime = GlobalTick.getInstance().getCurrentTime();
            if (JedisUtil.lockRedisKey(RedisKey.MistGhostBusterMatchLock, 5000l)) {
                jedis.hdel(RedisKey.MistGhostBusterMatchData, player.getIdx());
                playerMatchMap.remove(player.getIdx());
                JedisUtil.unlockRedisKey(RedisKey.MistGhostBusterMatchLock);
            } else {
                playerRemoveMap.put(player.getIdx(), curTime);
            }
            return MistRetCode.MRC_Success;
        } else if (playerPreMatchMap.containsKey(player.getIdx())) {
            playerPreMatchMap.remove(player.getIdx());
            return MistRetCode.MRC_Success;
        }
        return MistRetCode.MRC_PlayerNotMatch;
    }

    public synchronized void matchSuccess(playerEntity player, int serverIndex) {
        if (playerRemoveMap.containsKey(player.getIdx())) {
            playerRemoveMap.remove(player.getIdx());
        }
        if (playerMatchMap.containsKey(player.getIdx())) {
            playerMatchMap.remove(player.getIdx());
        }
        playerJoiningGhostBusterMap.put(player.getIdx(), serverIndex);
        LogUtil.info("match success,playerIdx=" + player.getIdx());
    }

    public synchronized void onPlayerExitGhostBuster(String playerIdx) {
        if (playerJoiningGhostBusterMap.containsKey(playerIdx)) {
            playerJoiningGhostBusterMap.remove(playerIdx);
        }
    }

    public synchronized void openGhostActivity(long curTime) {
        setActivityOpenFlag(true);
    }

    public synchronized void settleGhostActivity(long curTime) {
        closeDailyGhostActivity(curTime);
        setActivityOpenFlag(false);
    }

    public synchronized void openDailyGhostActivity(long curTime) {
        dailyOpenFlag = true;
    }

        // 每日结束时清空匹配中的玩家
    public synchronized void closeDailyGhostActivity(long curTime) {
        SC_StopMatchGhostBuster.Builder builder = SC_StopMatchGhostBuster.newBuilder();
        builder.setRetCode(MistRetCode.MRC_Success);
        for (String playerIdx : playerMatchMap.keySet()) {
            playerRemoveMap.put(playerIdx, curTime);
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_StopMatchGhostBuster_VALUE, builder);
        }
        playerPreMatchMap.clear();
        playerMatchMap.clear();
        dailyOpenFlag = false;
        updateRedisData();
        updateDailyTimeStamp(curTime);
    }

    public boolean isInFightTime() {
        if (!isGhostActivityOpen()) {
            return false;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        return dailyStartTimeStamp <= curTime && dailyEndTimeStamp > curTime;
    }

    public boolean isGhostActivityOpen() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        return startTime <= curTime && endTime > curTime;
    }

    protected boolean openFlagUpdate(long curTime) {
        boolean openFlag = isGhostActivityOpen();
        if (openFlag) {
            if (!activityOpenFlag) {
                openGhostActivity(curTime);
                return true;
            }

            boolean dailyFlag = isInFightTime();
            if (dailyFlag) {
                if (!dailyOpenFlag) {
                    openDailyGhostActivity(curTime);
                    return true;
                }
            } else {
                if (dailyOpenFlag) {
                    closeDailyGhostActivity(curTime);
                }
            }
        } else {
            if (activityOpenFlag) {
                settleGhostActivity(curTime);
            }
        }
        return false;
    }

    protected void updateRedisData() {
        if (MapUtils.isEmpty(playerMatchMap) && MapUtils.isEmpty(playerRemoveMap) && MapUtils.isEmpty(playerPreMatchMap)) {
            return;
        }
        if (!JedisUtil.lockRedisKey(RedisKey.MistGhostBusterMatchLock, 5000l)) {
            return;
        }
        for (Entry<String, Long> entry : playerRemoveMap.entrySet()) {
            jedis.hdel(RedisKey.MistGhostBusterMatchData.getBytes(), entry.getKey().getBytes());
        }
        playerEntity player;
        for (Entry<String, Long> entry : playerPreMatchMap.entrySet()) {
            player = playerCache.getByIdx(entry.getKey());
            if (player == null) {
                continue;
            }
            MistGhostBusterSyncData.Builder builder = MistGhostBusterSyncData.newBuilder();
            builder.setPlayerInfo(player.getBattleBaseData());
            builder.setFromSvrIndex(ServerConfig.getInstance().getServer());
            builder.setStartMatchTime(entry.getValue());
            builder.addAllItemData(player.getDb_data().getGhostBusterDataBuilder().getMistGhostItemDataList());
            builder.putAllDailyOwnedRewards(player.getDb_data().getGhostBusterDataBuilder().getMistGhostDailyGainRewardsMap());
            jedis.hset(RedisKey.MistGhostBusterMatchData.getBytes(), player.getIdx().getBytes(), builder.build().toByteArray());
            playerMatchMap.put(entry.getKey(), entry.getValue());
            LogUtil.info("GhostBuster onTick match info to redis");
        }
        JedisUtil.unlockRedisKey(RedisKey.MistGhostBusterMatchLock);
        playerRemoveMap.clear();
        playerPreMatchMap.clear();
    }

    public synchronized void onTick(long curTime) {
        if (activityId == 0) {
            return;
        }
        if (updateTime > curTime) {
            return;
        }
        updateTime = curTime + TimeUtil.MS_IN_A_S;
        if (openFlagUpdate(curTime)) {
            return;
        }
        updateRedisData();
    }
}
