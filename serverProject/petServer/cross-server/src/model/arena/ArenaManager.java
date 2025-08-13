package model.arena;

import cfg.ArenaConfig;
import cfg.ArenaDan;
import cfg.ArenaDanObject;
import cfg.ArenaLeague;
import cfg.ArenaLeagueObject;
import cfg.GameConfig;
import com.bowlong.sql.AtomicInt;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GameConst.RedisKey;
import common.GlobalData;
import common.GlobalThread;
import common.GlobalTick;
import common.IdGenerator;
import common.Tickable;
import common.load.ServerConfig;
import model.arena.entity.ArenaDanGradingConfig;
import model.arena.entity.ArenaRoomRanking;
import model.arena.entity.ArenaTotalInfo;
import model.arena.entity.OpponentRange;
import model.arena.entity.TempArenaRoom;
import model.arena.util.ArenaUtil;
import model.timer.TimerConst.TimerExpireType;
import model.timer.TimerConst.TimerIdx;
import model.timer.TimerConst.TimerTargetType;
import model.timer.dbCache.timerCache;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.ArenaDB.DB_ArenaPlayerInfo;
import protocol.ArenaDB.DB_ArenaRoom;
import protocol.ArenaDB.DB_ArenaRoom.Builder;
import protocol.ArenaDB.DB_PlayerDanUp;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.ArenaRankingInfo;
import protocol.ServerTransfer.CS_GS_ArenaDirectUp;
import util.EventUtil;
import util.GameUtil;
import util.JedisUtil;
import static util.JedisUtil.jedis;
import util.LogUtil;
import util.TimeUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author huhan
 * @date 2020/05/11
 */
public class ArenaManager implements Tickable, Runnable {
    private static ArenaManager instance;

    public static ArenaManager getInstance() {
        if (instance == null) {
            synchronized (ArenaManager.class) {
                if (instance == null) {
                    instance = new ArenaManager();
                }
            }
        }
        return instance;
    }

    private ArenaManager() {
    }

    /**
     * <段位， 段位配置>
     */
    private final Map<Integer, ArenaDanGradingConfig> danCfgMap = new ConcurrentHashMap<>();

    /**
     * 房间排行榜存储 <房间id,>
     */
    private final Map<String, ArenaRoomRanking> roomRankingMap = new ConcurrentHashMap<>();

    /**
     * 房间段位映射
     */
    private final Map<String, Integer> roomDanMap = new ConcurrentHashMap<>();

    private final AtomicBoolean arenaTick = new AtomicBoolean(true);

    public void stopArenaTick() {
        arenaTick.set(false);
    }


    /**
     * 用于保存所有的房间个数
     */
    private final AtomicInt totalRoomSize = new AtomicInt();

    private int getTotalRoomSize() {
        return this.totalRoomSize.get();
    }

    private void setTotalRoomSize(int newValue) {
        if (newValue <= 0 || getTotalRoomSize() == newValue) {
            return;
        }

        this.totalRoomSize.set(newValue);
    }

    public boolean init() {
        for (ArenaDanObject value : cfg.ArenaDan._ix_id.values()) {
            if (value.getId() <= 0) {
                continue;
            }

            ArenaDanGradingConfig entity = ArenaDanGradingConfig.createEntity(value);
            if (entity == null || !putPartition(entity)) {
                return false;
            }
        }
        addTimer();
        GlobalThread.getInstance().execute(this);
        checkTotalRoomInfo();
        return checkCfg() && GlobalTick.getInstance().addTick(this);
    }

    /**
     * 检查所有房间信息是否正确
     *
     * @return
     */
    private void checkTotalRoomInfo() {
        Set<String> roomIdxSet = getTotalRoomIdxSet();
        if (CollectionUtils.isEmpty(roomIdxSet)) {
            Builder newRoom = createNewRoom(1);
            if (newRoom != null) {
                updateRoomInfo(newRoom.build());
            } else {
                LogUtil.warn("checkTotalRoomInfo createNewRoom is null, serverIndex={}", ServerConfig.getInstance().getServer());
            }
            return;
        }

        //需要检查的段位与房间是否匹配的玩家idx
        Set<String> needCheckPlayerRoomDanMatch = new HashSet<>();
        for (String roomIdx : roomIdxSet) {
            //检查房间是否与玩家信息匹配
            List<String> remove = JedisUtil.syncExecSupplier(ArenaUtil.getArenaRoomLockKey(roomIdx), () -> {
                TempArenaRoom tempRoom = TempArenaRoom.create(getRoomByRoomId(roomIdx), getRoomRanking(roomIdx));
                if (tempRoom == null) {
                    return null;
                }
                List<String> removeIdx = tempRoom.checkRoomInfo();
                tempRoom.updateRoomInfoToRedis();

                //重新更新房间内所有玩家的排行榜
                Map<String, DB_ArenaPlayerInfo> totalPlayerInfoMap = ArenaPlayerManager.getInstance().getPlayerInfoMap(tempRoom.getPlayerIdsList());
                if (MapUtils.isNotEmpty(totalPlayerInfoMap)) {
                    updatePlayerRanking(roomIdx, totalPlayerInfoMap.values());
                }
                return removeIdx;
            });

            if (CollectionUtils.isNotEmpty(remove)) {
                needCheckPlayerRoomDanMatch.addAll(remove);
            }
        }

        //检查玩家信息是否与段位匹配
        for (String checkIdx : needCheckPlayerRoomDanMatch) {
            JedisUtil.syncExecBooleanSupplier(ArenaUtil.getPlayerLockKey(checkIdx), () -> {
                DB_ArenaPlayerInfo.Builder infoBuilder = ArenaPlayerManager.getInstance().getPlayerBaseInfoBuilder(checkIdx);
                if (infoBuilder == null) {
                    return false;
                }

                int curRoomDan = getRoomBelongDan(infoBuilder.getRoomId());
                if (curRoomDan != infoBuilder.getDan()) {
                    LogUtil.info("model.arena.ArenaManager.checkTotalRoom, player roomId:" + infoBuilder.getRoomId()
                            + ", room dan:" + curRoomDan + ", is not match player dan:" + infoBuilder.getDan()
                            + ",clear player room");
                    infoBuilder.clearRoomId();
                }
                ArenaPlayerManager.getInstance().updatePlayerInfoToRedis(infoBuilder.build());
                return true;
            });
        }
    }

    private void addTimer() {
        timerCache.getInstance().addTimer(TimerIdx.TI_SETTLE_ARENA_DAN
                , ArenaConfig.getById(GameConst.ConfigId).getDansettlehour() * TimeUtil.MS_IN_A_HOUR
                , ArenaConfig.getById(GameConst.ConfigId).getDansettleinterval() * TimeUtil.MS_IN_A_DAY
                , TimerTargetType.TT_SETTLE_ARENA_DAN
                , TimerExpireType.ET_EXPIRE_BY_TIME, -1, true);
    }

    private boolean putPartition(ArenaDanGradingConfig partition) {
        if (partition == null) {
            return true;
        }

        if (this.danCfgMap.containsKey(partition.getDanId())) {
            LogUtil.error("the same partitionId is already exist, please check cfg, partitionId:" + partition.getDanId());
            return false;
        }
        this.danCfgMap.put(partition.getDanId(), partition);
        return true;
    }

    /**
     * 将玩家加入房间并返回房间id
     *
     * @return 房间id
     */
    public String allocationRoom(String playerIdx, int dan) {
        if (StringUtils.isBlank(playerIdx) || ArenaDan.getById(dan) == null) {
            LogUtil.error("ArenaManager.allocationRoom, error params, playerIdx:" + playerIdx
                    + ", dan:" + dan);
            return null;
        }

        List<String> roomList = getDanRoomIdxList(dan);
        if (CollectionUtils.isNotEmpty(roomList)) {
            for (String roomId : roomList) {
                String arenaRoomLockKey = ArenaUtil.getArenaRoomLockKey(roomId);
                String lockParam = JedisUtil.generateLockParam();
                if (!JedisUtil.tryLockWithRetry(arenaRoomLockKey, lockParam)) {
                    LogUtil.error("ArenaManager.allocationRoom, try to lock room failed, roomId:" + roomId);
                    return null;
                }

                DB_ArenaRoom room = getRoomByRoomId(roomId);
                if (room == null) {
                    LogUtil.error("ArenaManager.allocationRoom, room is null, roomId:" + roomId);
                    JedisUtil.unlockEntry(arenaRoomLockKey, lockParam);
                    return null;
                }

                //房间已满
                if (room.getPlayerIdCount() >= ArenaUtil.getRoomMaxSize(dan)) {
                    JedisUtil.unlockEntry(arenaRoomLockKey, lockParam);
                    continue;
                }

                //加入房间
                DB_ArenaRoom.Builder builder = room.toBuilder();
                if (!builder.getPlayerIdList().contains(playerIdx)) {
                    builder.addPlayerId(playerIdx);
                    LogUtil.info("ArenaManager.allocationRoom, player join room, playerIdx:" + playerIdx + ", roomId:" + builder.getRoomId());
                }
                //更新房间信息
                updateRoomInfo(builder.build());

                //解锁
                JedisUtil.unlockEntry(arenaRoomLockKey, lockParam);
                return roomId;
            }
        }

        //未找到空房间,创建新房间
        LogUtil.info("ArenaManager.allocationRoom, all room is full, dan:" + dan);

        DB_ArenaRoom.Builder newRoom = createNewRoom(dan);
        if (newRoom == null) {
            LogUtil.error("ArenaManager.allocationRoom, create new room failed");
            return null;
        }
        newRoom.addPlayerId(playerIdx);
        //更新新房间
        updateRoomInfo(newRoom.build());
        LogUtil.info("ArenaManager.allocationRoom, player join room, playerIdx:" + playerIdx + ", roomId:" + newRoom.getRoomId());
        return newRoom.getRoomId();
    }

    /**
     * 更新房间信息
     *
     * @param arenaRoom
     * @return
     */
    public boolean updateRoomInfo(DB_ArenaRoom arenaRoom) {
        if (arenaRoom == null) {
            return false;
        }

        //如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。  返回1
        //如果域 field 已经存在于哈希表中，旧值将被覆盖。         返回0
        jedis.hset(RedisKey.ARENA_ROOM_INFO.getBytes(), arenaRoom.getRoomIdBytes().toByteArray(), arenaRoom.toByteArray());
        return true;
    }

    /**
     * 获取段位对应的所有房间列表
     *
     * @param dan
     * @return
     */
    public List<String> getDanRoomIdxList(int dan) {
        List<String> result = new ArrayList<>();
        Set<String> totalRoomId = getTotalRoomIdxSet();
        if (CollectionUtils.isEmpty(totalRoomId)) {
            return result;
        }

        for (String idx : totalRoomId) {
            DB_ArenaRoom roomByRoomId = getRoomByRoomId(idx);
            if (roomByRoomId != null && roomByRoomId.getDan() == dan) {
                result.add(idx);
            }
        }
        return result;
    }

    /**
     * 返回房间所属于的段位
     *
     * @param roomId
     * @return
     */
    public int getRoomBelongDan(String roomId) {
        if (StringUtils.isBlank(roomId)) {
            return 0;
        }

        if (this.roomDanMap.containsKey(roomId)) {
            return this.roomDanMap.get(roomId);
        } else {
            DB_ArenaRoom room = getRoomByRoomId(roomId);
            if (room == null) {
                LogUtil.error("ArenaManager.getRoomBelongDan, room is not exist, roomId:" + roomId);
                return 0;
            }
            this.roomDanMap.put(roomId, room.getDan());
            return room.getDan();
        }
    }

    /**
     * 房间是否存在
     *
     * @param roomId
     * @return
     */
    public boolean roomIsExist(String roomId) {
        if (StringUtils.isBlank(roomId)) {
            return false;
        }
        return jedis.hexists(RedisKey.ARENA_ROOM_INFO, roomId);
    }


    public DB_ArenaRoom getRoomByRoomId(String roomId) {
        if (StringUtils.isBlank(roomId)) {
            return null;
        }

        byte[] roomInfo = jedis.hget(RedisKey.ARENA_ROOM_INFO.getBytes(), roomId.getBytes());
        if (roomInfo != null) {
            try {
                return DB_ArenaRoom.parseFrom(roomInfo);
            } catch (InvalidProtocolBufferException e) {
                LogUtil.printStackTrace(e);
            }
        }
        return null;
    }

    public void updateDailyData() {
        setRankingSettleList();
    }

    private void setRankingSettleList() {
        LogUtil.info("model.arena.ArenaManager.setRankingSettleList, curTime:" + GlobalTick.getInstance().getCurrentTime());
        //不重复获取锁,拿到锁的服务器将列表放置tempList, 上锁时间为10分钟,服务器时差容错
        long lockMs = TimeUtil.MS_IN_A_MIN * 10;
        String lockKey = ArenaUtil.getLockKey(RedisKey.ARENA_ROOM_RANKING_SETTLE_LIST);
        String lockParam = JedisUtil.generateLockParam();
        if (JedisUtil.tryLockWithOutRetry(lockKey, lockParam, lockMs)) {
            Set<String> roomIdSet = getTotalRoomIdxSet();
            if (CollectionUtils.isEmpty(roomIdSet)) {
                LogUtil.warn("ArenaManager.settleDan, arena room list is empty");
                return;
            }

            //提前更新所有房间排行榜
            EventUtil.queryArenaRoomRanking(new ArrayList<>(roomIdSet));

            //将房间列表push进temp list
            String[] roomIdArray = new String[roomIdSet.size()];
            roomIdSet.toArray(roomIdArray);
            //返回的是更新后列表的个数
            jedis.lpush(RedisKey.ARENA_ROOM_RANKING_SETTLE_LIST, roomIdArray);
            LogUtil.info("ArenaManager.updateDailyData, success update room dan  settle temp list, roomId:" + GameUtil.collectionToString(roomIdSet));
        }
    }

    /**
     * 获取所有键通过该方法获取
     * 获取所有房间的id
     *
     * @return
     */
    public Set<String> getTotalRoomIdxSet() {

        Set<String> hKeys = jedis.hkeys(RedisKey.ARENA_ROOM_INFO);
        setTotalRoomSize(CollectionUtils.size(hKeys));
        return hKeys;
    }

    /**
     * 段位结算通过房间id
     * <p>
     * //对房间上锁并进行段位结算,不重试上锁
     *
     * @param roomId
     * @return
     */
    private boolean rankingSettle(String roomId) {
        if (StringUtils.isBlank(roomId)) {
            LogUtil.error("ArenaManager.rankingSettleByRoomId, roomId:" + roomId + ",is not exist");
            return false;
        }
        return JedisUtil.syncUnRetryBooleanSupplier(ArenaUtil.getRoomRankingSettleLockKey(roomId)
                , () -> {
                    long start = Instant.now().toEpochMilli();

                    if (!settleRoomRanking(roomId)) {
                        LogUtil.error("ArenaManager.rankingSettle, settle room ranking failed, roomId:" + roomId);
                        return false;
                    }

                    LogUtil.debug("ArenaManager.rankingSettle, ranking settle success, roomId:" + roomId
                            + ",use time:" + (Instant.now().toEpochMilli() - start));
                    return true;
                });
    }

    /**
     * 结算房间排行榜
     *
     * @param roomId
     * @return
     */
    private boolean settleRoomRanking(String roomId) {
        if (StringUtils.isBlank(roomId)) {
            return true;
        }
        ArenaRoomRanking roomRanking = getRoomRanking(roomId);
        if (roomRanking == null) {
            LogUtil.error("ArenaManager.settleRoomRanking, arena ranking room is not exist, roomId:" + roomId);
            return false;
        }
        return roomRanking.settleRanking();
    }

    private boolean checkCfg() {
        return checkLeague();
    }

    private boolean checkLeague() {
        for (ArenaLeagueObject value : ArenaLeague._ix_id.values()) {
            for (int i = value.getStartdan(); i <= value.getEnddan(); i++) {
                if (cfg.ArenaDan.getById(i) == null) {
                    LogUtil.error("partition is not exist, partition id :" + i + ", league id:" + value.getId());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 结算段位
     */
    public void setDanSettleList() {
        LogUtil.info("model.arena.ArenaManager.setDanSettleList");
        //不重复获取锁,拿到锁的服务器将列表放置tempList,服务器时差容错
        long lockMs = TimeUtil.MS_IN_A_MIN * 5;
        String lockKey = ArenaUtil.getLockKey(RedisKey.ARENA_ROOM_DAN_SETTLE_LIST);
        String lockParam = JedisUtil.generateLockParam();
        if (JedisUtil.tryLockWithOutRetry(lockKey, lockParam, lockMs)) {
            Set<String> roomIdSet = getTotalRoomIdxSet();
            if (CollectionUtils.isEmpty(roomIdSet)) {
                LogUtil.warn("ArenaManager.settleDan, arena room list is empty");
                return;
            }

            //提前更新所有房间排行榜
            EventUtil.queryArenaRoomRanking(new ArrayList<>(roomIdSet));

            //将房间列表push进temp list
            String[] roomIdArray = new String[roomIdSet.size()];
            roomIdSet.toArray(roomIdArray);
            //返回的是更新后列表的个数
            jedis.lpush(RedisKey.ARENA_ROOM_DAN_SETTLE_LIST, roomIdArray);
            LogUtil.info("ArenaManager.updateDailyData, success update room dan  settle temp list, roomId:" + GameUtil.collectionToString(roomIdSet));

            //向服务器发送段位结算消息,需要延迟发送,段位结算需要一定时间
            addDanSettleAdviceTimer();
        }
    }


    /**
     * 添加段位结算通知定时器
     */
    private void addDanSettleAdviceTimer() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        //延迟通知
        long invokeTime = TimeUtil.MS_IN_A_S * 10 + currentTime;
        timerCache.getInstance().addTimer(invokeTime, 0, TimerTargetType.TT_ARENA_DAN_SETTLE_ADVICE
                , TimerExpireType.ET_EXPIRE_BY_TRIGGER_TIMES, 1, false);
    }

    /**
     * 结算房间段位
     *
     * @param settleRoomId
     * @return
     */
    private boolean danSettle(String settleRoomId) {
        if (StringUtils.isBlank(settleRoomId)) {
            LogUtil.error("ArenaManager.danSettle, error params, settle room id is null");
            return true;
        }

        return JedisUtil.syncUnRetrySupplier(ArenaUtil.getRoomDanSettleLockKey(settleRoomId), () -> {
            TempArenaRoom tempArenaRoom = TempArenaRoom.create(getRoomByRoomId(settleRoomId), getRoomRanking(settleRoomId));
            if (tempArenaRoom == null) {
                LogUtil.error("ArenaManager.danSettle, create temp room failed");
                return false;
            }

            return tempArenaRoom.settleDan();
        });
    }

    /**
     * 根据段位和品质随机获得宠物
     *
     * @param dan
     * @param quality
     * @return
     */
    public int randomPet(int dan, int quality) {
        ArenaDanGradingConfig danGrading = getDanGradingConfig(dan);
        if (danGrading == null) {
            return 0;
        }
        return danGrading.randomGetPetByQuality(quality);
    }

    @Override
    public void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        for (ArenaRoomRanking value : roomRankingMap.values()) {
            if (value.getNextUpdateRankingTime() <= 0) {
                synchronized (value) {
                    value.setNextUpdateRankingTime(getNextUpdateTime());
                }
            } else if (currentTime > value.getNextUpdateRankingTime()) {
                EventUtil.queryArenaRoomRanking(Collections.singletonList(value.getRoomId()));
                synchronized (value) {
                    value.setNextUpdateRankingTime(getNextUpdateTime());
                }
            }
        }
    }

    private long getNextUpdateTime() {
        long cfgUpdateTime = ArenaConfig.getById(GameConst.ConfigId).getRankrefrshtime() * TimeUtil.MS_IN_A_S;
        long addTime = Math.max(30 * TimeUtil.MS_IN_A_S, ((new Random().nextInt(10) * cfgUpdateTime) / 10));
        return GlobalTick.getInstance().getCurrentTime() + addTime;
    }

    public ArenaDanGradingConfig getDanGradingConfig(int dan) {
        return this.danCfgMap.get(dan);
    }

    /**
     * 查询玩家排行
     *
     * @param roomId
     * @param playerIdx
     * @return
     */
    public int queryPlayerRanking(String roomId, String playerIdx) {
        if (StringUtils.isBlank(roomId) || StringUtils.isBlank(playerIdx)) {
            return -1;
        }

        ArenaRoomRanking roomRanking = getRoomRanking(roomId);
        if (roomRanking == null) {
            return -1;
        }
        return roomRanking.queryPlayerRanking(playerIdx);
    }

    public ArenaRoomRanking getRoomRanking(String roomId) {
        if (StringUtils.isBlank(roomId) || !roomIsExist(roomId)) {
            LogUtil.error("ArenaManager.getRoomRanking, room is not exist, roomId:" + roomId);
            return null;
        }

        if (this.roomRankingMap.containsKey(roomId)) {
            return this.roomRankingMap.get(roomId);
        } else {
            ArenaRoomRanking roomRanking = ArenaRoomRanking.create(roomId, getRoomBelongDan(roomId));
            if (roomRanking != null) {
                this.roomRankingMap.put(roomRanking.getRoomId(), roomRanking);
                return roomRanking;
            }
        }
        return null;
    }


    /**
     * 更新玩家排行榜
     *
     * @param playerInfo
     */
    public void updatePlayerRanking(DB_ArenaPlayerInfo playerInfo) {
        if (playerInfo == null) {
            return;
        }
        updatePlayerRanking(playerInfo.getRoomId(), Collections.singletonList(playerInfo));
    }

    /**
     * 更新玩家排行榜
     *
     * @param playerInfoList
     */
    public void updatePlayerRanking(String roomId, Collection<DB_ArenaPlayerInfo> playerInfoList) {
        if (StringUtils.isBlank(roomId) || CollectionUtils.isEmpty(playerInfoList)) {
            return;
        }

        ArenaRoomRanking roomRanking = getRoomRanking(roomId);
        if (roomRanking != null) {
            roomRanking.updatePlayerRanking(playerInfoList);
        }
    }

    @Override
    public void run() {
        GlobalThread.getInstance().execute(() -> {
            LogUtil.info("ArenaManager start settle event, curTime = " + GlobalTick.getInstance().getCurrentTime());
            Thread.currentThread().setName("ArenaManager.arenaTick");
            while (arenaTick.get()) {
                try {
                    roomRankingSettleEvent();
                    roomDanSettleEvent();
                    playerDanUpEvent();
                    allocationPlayerRoomEvent();

                    Thread.sleep(ServerConfig.getInstance().getArenaEventTickCycle());
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                }
            }
            LogUtil.warn("=========================ArenaManager event tick is over==============================");
        });
    }

    /**
     * 房间排行结算
     *
     */
    private void roomRankingSettleEvent() {
        String popRoomId = jedis.rpoplpush(RedisKey.ARENA_ROOM_RANKING_SETTLE_LIST, RedisKey.ARENA_ROOM_RANKING_SETTLE_LIST);
        if (StringUtils.isBlank(popRoomId)) {
            return;
        }

        boolean settleRet = rankingSettle(popRoomId);
        if (!settleRet) {
            LogUtil.error("model.arena.ArenaManager.roomRankingSettleEvent, room ranking settle failed, roomId:" + popRoomId);
        } else {
            jedis.lrem(RedisKey.ARENA_ROOM_RANKING_SETTLE_LIST, 1, popRoomId);
        }
    }

    /**
     * 房间段位结算
     *
     */
    private void roomDanSettleEvent() {
        String popRoomId = jedis.rpoplpush(RedisKey.ARENA_ROOM_DAN_SETTLE_LIST, RedisKey.ARENA_ROOM_DAN_SETTLE_LIST);
        if (StringUtils.isBlank(popRoomId)) {
            return;
        }

        boolean settleRet = danSettle(popRoomId);
        if (settleRet) {
            jedis.lrem(RedisKey.ARENA_ROOM_DAN_SETTLE_LIST, 1, popRoomId);
        }

        LogUtil.info("model.arena.ArenaManager.roomDanSettleEvent, room dan settle result:" + settleRet + ", roomId:" + popRoomId);
    }

    private void playerDanUpEvent() {
        byte[] playerDanUpBytes = jedis.rpoplpush(RedisKey.ARENA_PLAYER_DAN_UP.getBytes(), RedisKey.ARENA_PLAYER_DAN_UP.getBytes());
        if (playerDanUpBytes == null) {
            return;
        }

        try {
            DB_PlayerDanUp playerDanUp = DB_PlayerDanUp.parseFrom(playerDanUpBytes);
            JedisUtil.syncExecBooleanSupplier(ArenaUtil.getPlayerLockKey(playerDanUp.getPlayerIdx()), () -> {
                DB_ArenaPlayerInfo.Builder infoBuilder = ArenaPlayerManager.getInstance().getPlayerBaseInfoBuilder(playerDanUp.getPlayerIdx());
                //如果entity == null 或者 玩家段位已经是下一个段位, 无操作
                if (infoBuilder == null) {
                    LogUtil.error("ArenaManager.playerDanUp, playerIdx:" + playerDanUp.getPlayerIdx() + ", entity is not exist");
                } else {

                    infoBuilder.setDan(playerDanUp.getNextDan());
                    infoBuilder.setScore(ArenaUtil.getDanStartScore(playerDanUp.getNextDan()));
                    infoBuilder.clearRoomId();
                    infoBuilder.clearKillDirectUpCount();
                    //更新玩家信息
                    ArenaPlayerManager.getInstance().updatePlayerInfoToRedis(infoBuilder.build());

                    LogUtil.info("ArenaManager.playerDanUp, set player dan up info success, playerIdx:" + playerDanUp.getPlayerIdx());

                    if (playerDanUp.getIsDirectUp()) {
                        //发送段位晋升消息到服务器
                        int svrIndex = infoBuilder.getLastLoginSIndex();
                        if (svrIndex <= 0) { // 兼容代码
                            svrIndex = GlobalData.getInstance().getServerIndexByIp(infoBuilder.getLastLoginIp());
                        }
                        GlobalData.getInstance().sendMsgToServer(svrIndex, MsgIdEnum.CS_GS_ArenaDirectUp_VALUE
                                , CS_GS_ArenaDirectUp.newBuilder().setPlayerIdx(playerDanUp.getPlayerIdx()).setNewDan(playerDanUp.getNextDan()));
                    } else {
                        //添加移除房间和重新分配房间事件, 玩家重新拉取竞技场信息时再分配房间
                        submitPlayerAllocationRoom(Collections.singletonList(playerDanUp.getPlayerIdx()));
                    }
                }

                jedis.lrem(RedisKey.ARENA_PLAYER_DAN_UP.getBytes(), 1, playerDanUpBytes);
                return true;
            });
        } catch (InvalidProtocolBufferException e) {
            LogUtil.printStackTrace(e);
        }

    }

    /**
     * 分配玩家房间
     *
     */
    private void allocationPlayerRoomEvent() {

        String playerIdx = jedis.rpoplpush(RedisKey.ARENA_ALLOCATION_PLAYER_ROOM, RedisKey.ARENA_ALLOCATION_PLAYER_ROOM);
        if (playerIdx == null) {
            return;
        }

        boolean allocationResult = JedisUtil.syncExecBooleanSupplier(ArenaUtil.getPlayerLockKey(playerIdx), () -> {
            DB_ArenaPlayerInfo.Builder infoBuilder = ArenaPlayerManager.getInstance().getPlayerBaseInfoBuilder(playerIdx);
            if (infoBuilder == null) {
                LogUtil.error("ArenaManager.allocationPlayerRoom, playerIdx entity is not");
                return true;
            }

            //玩家房间不存在,分配新房间
            if (!roomIsExist(infoBuilder.getRoomId())) {
                String newRoomId = allocationRoom(playerIdx, infoBuilder.getDan());
                if (newRoomId == null) {
                    LogUtil.error("ArenaManager.allocationPlayerRoom， allocation room failed, playerIdx:" + playerIdx);
                    return false;
                } else {
                    infoBuilder.setRoomId(newRoomId);
                    ArenaPlayerManager.getInstance().updatePlayerInfoToRedis(infoBuilder.build());
                    LogUtil.error("ArenaManager.allocationPlayerRoom, playerIdx:" + playerIdx + "join new room:" + newRoomId);

                    //移除队列
                    jedis.lrem(RedisKey.ARENA_ALLOCATION_PLAYER_ROOM, 1, playerIdx);
                    return true;
                }
            } else {
                //移除队列
                jedis.lrem(RedisKey.ARENA_ALLOCATION_PLAYER_ROOM, 1, playerIdx);
                return true;
            }
        });

        if (allocationResult) {
            jedis.lrem(RedisKey.ARENA_ALLOCATION_PLAYER_ROOM, 1, playerIdx);
        }

        LogUtil.info("ArenaManager.allocationPlayerRoom, allocation playerIdx:" + playerIdx + ", result:" + allocationResult);
    }

    private DB_ArenaRoom.Builder createNewRoom(int dan) {
        //加锁创建,未上锁成功不创建,不重试上锁
        return JedisUtil.syncUnRetrySupplier(ArenaUtil.getDanRoomCreateLockKey(dan), () -> {
            //房间ID,段位+随机id
            String roomId = dan + "_" + IdGenerator.getInstance().generateId();

            //初始化房间机器人
            List<ArenaTotalInfo> robots = ArenaRobotManager.getInstance().initRoomRobot(roomId, dan);
            if (CollectionUtils.isEmpty(robots)) {
                LogUtil.error("ArenaDanGrading.createNewRoom, create room robot failed, roomId:" + roomId + ", dan:" + dan);
                return null;
            }

            List<DB_ArenaPlayerInfo> successUpdate = ArenaPlayerManager.getInstance().updateAllPlayerToRedis(robots);
            if (CollectionUtils.size(successUpdate) != robots.size()) {
                LogUtil.warn("ArenaDanGrading.createNewRoom, update robot to redis, not total update success");
            }

            Builder builder = DB_ArenaRoom.newBuilder();
            builder.setRoomId(roomId);
            builder.setDan(dan);

            //添加机器人到房间
            if (CollectionUtils.isNotEmpty(successUpdate)) {
                for (DB_ArenaPlayerInfo robotBase : successUpdate) {
                    builder.addPlayerId(robotBase.getBaseInfo().getPlayerIdx());

                    Integer oldCount = builder.getRobotCfgIdCountMapMap().get(robotBase.getRobotCfgId());
                    int newCount = oldCount == null ? 1 : oldCount + 1;
                    builder.putRobotCfgIdCountMap(robotBase.getRobotCfgId(), newCount);
                }
            }

            //更新房间信息
            updateRoomInfo(builder.build());

            //更新机器人排行榜
            updatePlayerRanking(roomId, successUpdate);
            //查询房间排行榜
            queryRoomRanking(Collections.singletonList(roomId));
            return builder;
        });
    }

    /**
     * 查询房间排行榜
     *
     * @param roomIdList
     */
    public void queryRoomRanking(List<String> roomIdList) {
        if (CollectionUtils.isEmpty(roomIdList)) {
            return;
        }

        LogUtil.debug("server.event.QueryArenaRoomRankingHandler, update ranking rooms:" + GameUtil.collectionToString(roomIdList));
        for (String roomId : roomIdList) {
            ArenaRoomRanking roomRanking = ArenaManager.getInstance().getRoomRanking(roomId);
            if (roomRanking == null) {
                LogUtil.error("QueryArenaRoomRankingHandler, room id ranking is not exist, roomId:" + roomIdList);
                return;
            }
            roomRanking.queryRoomRanking();
        }
    }

    /**
     * 刷新对手
     *
     * @param roomId
     * @param playerIdx
     * @return
     */
    public List<ArenaOpponentTotalInfo> randomOpponent(String roomId, String playerIdx) {
        if (StringUtils.isBlank(roomId) || StringUtils.isBlank(playerIdx)) {
            LogUtil.error("ArenaManager.randomOpponent, error params, roomId:" + roomId + ", playerIdx:" + playerIdx);
            return null;
        }

        return JedisUtil.syncExecSupplier(ArenaUtil.getArenaRoomLockKey(roomId)
                , () -> {
                    TempArenaRoom arenaRoom = TempArenaRoom.create(getRoomByRoomId(roomId), getRoomRanking(roomId));
                    if (arenaRoom == null) {
                        return null;
                    }
                    return arenaRoom.randomOpponent(getRangeList(arenaRoom.getDan()), playerIdx, allRobotOpponent(playerIdx));
                });
    }

    private boolean allRobotOpponent(String playerIdx) {
        DB_ArenaPlayerInfo playerBaseInfo = ArenaPlayerManager.getInstance().getPlayerBaseInfo(playerIdx);
        if (playerBaseInfo == null) {
            return true;
        }
        return playerBaseInfo.getBattleCount() < GameConfig.getById(GameConst.ConfigId).getArenaunlockperson();
    }

    /**
     * 返回指定段位对手推荐区间
     *
     * @param dan
     * @return
     */
    private List<OpponentRange> getRangeList(int dan) {
        ArenaDanGradingConfig grading = getDanGradingConfig(dan);
        if (grading == null) {
            return null;
        }
        return grading.getRangeList();
    }

//    public void clearPlayerRanking(String roomId, String playerIdx) {
//        if (StringUtils.isBlank(roomId) || StringUtils.isBlank(playerIdx)) {
//            return;
//        }
//        ArenaRoomRanking ranking = getRoomRanking(roomId);
//        if (ranking == null) {
//            return;
//        }
//        ranking.clearPlayerRanking(playerIdx);
//    }

//    public void clearPlayerRanking(String roomId, List<String> playerIdxList) {
//        if (StringUtils.isBlank(roomId) || CollectionUtils.isEmpty(playerIdxList)) {
//            return;
//        }
//        ArenaRoomRanking ranking = getRoomRanking(roomId);
//        if (ranking == null) {
//            return;
//        }
//        ranking.clearPlayerRankingByList(playerIdxList);
//    }

    /**
     * 提交至重新分配玩家房间队列
     *
     * @param playerIdxList
     */
    public void submitPlayerAllocationRoom(List<String> playerIdxList) {
        if (CollectionUtils.isEmpty(playerIdxList)) {
            return;
        }


        String[] playerIdxArray = new String[playerIdxList.size()];
        playerIdxList.toArray(playerIdxArray);
        jedis.lpush(RedisKey.ARENA_ALLOCATION_PLAYER_ROOM, playerIdxArray);
        LogUtil.debug("ArenaManager.submitPlayerAllocationRoom, submit player to allocation, idxs:" + GameUtil.collectionToString(playerIdxList));
    }

    public void submitPlayerDanUp(List<String> playerIdxList, int nextDan, boolean directUp) {
        if (CollectionUtils.isEmpty(playerIdxList) || ArenaDan.getById(nextDan) == null) {
            LogUtil.error("ArenaManager.submitPlayerDanUp, error params, playerIdxList is null:" + Objects.isNull(playerIdxList) + ", next dan：" + nextDan);
            return;
        }

        //拆分，分批次更新
        List<List<String>> splitList = GameUtil.splitCollection(playerIdxList, JedisUtil.EACH_UPDATE_MAX_COUNT);
        if (CollectionUtils.isNotEmpty(splitList)) {

            DB_PlayerDanUp.Builder builder = DB_PlayerDanUp.newBuilder();
            builder.setNextDan(nextDan);
            builder.setIsDirectUp(directUp);

            //每一批的数据
            for (List<String> idxList : splitList) {

                //待更新的数据集合
                byte[][] bytes = new byte[idxList.size()][];
                for (int i = 0; i < idxList.size(); i++) {
                    String playerIdx = idxList.get(i);
                    builder.clearPlayerIdx();
                    builder.setPlayerIdx(playerIdx);

                    bytes[i] = builder.build().toByteArray();
                }
                //更新数据
                jedis.rpush(RedisKey.ARENA_PLAYER_DAN_UP.getBytes(), bytes);
            }
        }

        LogUtil.debug("ArenaManager.submitPlayerDanUp, success submit dan up, idx:" + GameUtil.collectionToString(playerIdxList));
    }

    /**
     * 查询某个段位所有房间内的排名信息， 排名默认从0开始
     *
     * @param dan
     * @param limit
     * @return
     */
    public List<ArenaRankingInfo> queryDanRanking(int dan, int limit) {
        if (limit == 0 || ArenaDan.getById(dan) == null) {
            return null;
        }
        List<String> roomIdList = getDanRoomIdxList(dan);
        if (CollectionUtils.isEmpty(roomIdList)) {
            LogUtil.error("ArenaManager.queryDanRanking, dan room list is empty, dan:" + dan);
            return null;
        }

        List<ArenaRankingInfo> result = new ArrayList<>();
        for (String roomId : roomIdList) {
            ArenaRoomRanking ranking = getRoomRanking(roomId);
            if (ranking == null) {
                LogUtil.error("ArenaManager.queryDanRanking, room ranking info is not exist, roomId:" + roomId);
                continue;
            }

            ArenaRankingInfo rankingInfo = ranking.queryRanking(limit);
            if (rankingInfo != null) {
                result.add(rankingInfo);
            }
        }
        return result;
    }

    /**
     * 将玩家从指定房间移除
     *
     * @param roomId
     * @param playerIdx
     * @return 移除是否成功
     */
    public boolean removePlayerFromRoom(String roomId, List<String> playerIdx) {
        if (StringUtils.isBlank(roomId)) {
            return false;
        }

        if (CollectionUtils.isEmpty(playerIdx)) {
            return true;
        }

        return JedisUtil.syncExecBooleanSupplier(ArenaUtil.getArenaRoomLockKey(roomId), () -> {
            TempArenaRoom tempArenaRoom = TempArenaRoom.create(getRoomByRoomId(roomId), getRoomRanking(roomId));
            if (tempArenaRoom == null) {
                return false;
            }
            tempArenaRoom.removePlayer(playerIdx);
            tempArenaRoom.updateRoomInfoToRedis();
            return true;
        });
    }
}