package model.stoneRift;

import cfg.Head;
import cfg.HeadBorder;
import cfg.StoneRiftLevel;
import cfg.StoneRiftLevelObject;
import cfg.StoneRiftRobotObject;
import com.alibaba.fastjson.JSON;
import static common.GameConst.RedisKey.StoneRiftPrefix;
import common.GlobalData;
import common.JedisUtil;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import datatool.StringHelper;
import db.entity.BaseEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbPlayerWorldMap;
import model.stoneRift.entity.DbStoneRiftFactory;
import model.stoneRift.entity.DbStoneRiftSteal;
import model.stoneRift.entity.StoneRiftWorldMapPlayer;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.Reward;
import protocol.MessageId;
import static protocol.MessageId.MsgIdEnum.SC_UpdateStoneRiftWorldMapPlayer_VALUE;

import protocol.RetCodeId.RetCodeEnum;
import protocol.StoneRift;

import static protocol.RetCodeId.RetCodeEnum.RCE_StoneRift_NoResCanSteal;
import static protocol.StoneRift.StoneRiftScienceEnum.SRSE_CanStealRareItem;
import static protocol.StoneRift.StoneRiftScienceEnum.SRSE_StealMore;
import static protocol.TargetSystem.TargetTypeEnum.TTE_StoneRift_StealRes;
import util.EventUtil;
import util.LogUtil;
import util.ObjUtil;
import util.RandomUtil;
import util.TimeUtil;

@Slf4j
public class StoneRiftWorldMapManager {


    @Getter
    private static StoneRiftWorldMapManager instance = new StoneRiftWorldMapManager();

    private static final String worldMapOnline = StoneRiftPrefix + "worldMapOnline:";
    private static final String AllWorldMapId = StoneRiftPrefix + "allMapId";

    private static final String ServerWorldMapId = StoneRiftPrefix + "serverMapId:";


    private static final String WorldMapInfo = StoneRiftPrefix + "worldMapInfo:";

    private static final String MapPlayers = WorldMapInfo + "players:";


    private static final String lockPrefix = WorldMapInfo + ":lock:";

    private static final String PlayerIp = WorldMapInfo + "playerIp";

    private static final String FactoryInMap = WorldMapInfo + "factoryInMap:";


    public void dailyRefresh() {
        Map<String, List<StoneRiftWorldMapPlayer>> worldMap = new ConcurrentHashMap<>();
        int mapIndex = 1;
        String crossMapId = getCrossMapId(ServerConfig.getInstance().getServer(), mapIndex);
        List<StoneRiftWorldMapPlayer> mapPlayers = new ArrayList<>();
        for (BaseEntity value : stoneriftCache.getInstance()._ix_id.values()) {
            StoneRiftWorldMapPlayer mapPlayer = buildMapPlayer((stoneriftEntity) value);
            mapPlayers = worldMap.computeIfAbsent(crossMapId, a -> Collections.synchronizedList(new ArrayList<>()));
            mapPlayers.add(mapPlayer);

            if (mapPlayers.size() >= StoneRiftCfgManager.getInstance().getMapPlayerSize()) {
                putRobotToList(mapPlayers);
            }
            if (mapFullPlayer(mapPlayers)) {
                mapIndex++;
                crossMapId = getCrossMapId(ServerConfig.getInstance().getServer(), mapIndex);
            }
        }
        if (mapPlayers.size() < StoneRiftCfgManager.getInstance().getMapTotalSize()) {
            putRobotToList(mapPlayers);
        }
        if (mapIndex <= 3) {
            for (int i = mapIndex; i < 3; i++) {
                mapIndex++;
                crossMapId = getCrossMapId(ServerConfig.getInstance().getServer(), mapIndex);
                mapPlayers = worldMap.computeIfAbsent(crossMapId, a -> Collections.synchronizedList(new ArrayList<>()));
                putRobotToList(mapPlayers);
            }
        }
        saveMapIdToCache(worldMap);
        savePlayerToCache(worldMap);
    }


    private void saveStealPlayer(String mapId, StoneRiftWorldMapPlayer mapPlayer) {
        JedisUtil.jedis.hset(MapPlayers + mapId, mapPlayer.getPlayerIdx(), JSON.toJSONString(mapPlayer));
    }


    private void savePlayerFactoryMap(String playerIdx, String mapId) {
        JedisUtil.jedis.hset(FactoryInMap, playerIdx, mapId);
    }

    private String findPlayerFactoryMap(String playerIdx) {
        return JedisUtil.jedis.hget(FactoryInMap, playerIdx);
    }

    private void savePlayerToCache(Map<String, List<StoneRiftWorldMapPlayer>> worldMap) {
        for (Map.Entry<String, List<StoneRiftWorldMapPlayer>> entry : worldMap.entrySet()) {
            clearAllPlayerCache(entry.getKey());
            for (StoneRiftWorldMapPlayer mapPlayer : entry.getValue()) {
                JedisUtil.jedis.hset(MapPlayers + entry.getKey(), mapPlayer.getPlayerIdx(), JSON.toJSONString(mapPlayer));
                savePlayerFactoryMap(mapPlayer.getPlayerIdx(), entry.getKey());
            }

        }
    }

    private void clearAllPlayerCache(String mapId) {
        Set<String> hkeys = JedisUtil.jedis.hkeys(MapPlayers + mapId);
        if (CollectionUtils.isEmpty(hkeys)) {
            return;
        }
        JedisUtil.jedis.hdel(MapPlayers + mapId, hkeys.toArray(new String[0]));
    }

    private void saveMapIdToCache(Map<String, List<StoneRiftWorldMapPlayer>> worldMap) {
        JedisUtil.jedis.sadd(AllWorldMapId, worldMap.keySet().toArray(new String[0]));
        JedisUtil.jedis.sadd(ServerWorldMapId + ServerConfig.getInstance().getServer(), worldMap.keySet().toArray(new String[0]));
    }

    public Set<String> findAllLocalMapId() {
        return JedisUtil.jedis.smembers(ServerWorldMapId + ServerConfig.getInstance().getServer());
    }

    public List<StoneRiftWorldMapPlayer> findAllPlayersByMapId(String mapId) {
        List<StoneRiftWorldMapPlayer> result = new ArrayList<>();
        for (String hval : JedisUtil.jedis.hvals(MapPlayers + mapId)) {
            result.add(JSON.parseObject(hval, StoneRiftWorldMapPlayer.class));
        }
        return result;
    }


    private boolean mapFullPlayer(List<StoneRiftWorldMapPlayer> mapPlayers) {
        return mapPlayers.size() >= StoneRiftCfgManager.getInstance().getMapTotalSize();
    }

    private void putRobotToList(List<StoneRiftWorldMapPlayer> mapPlayers) {
        int needSize = StoneRiftCfgManager.getInstance().getMapTotalSize() - mapPlayers.size();
        if (needSize <= 0) {
            return;
        }
        for (int i = 0; i < needSize; i++) {
            StoneRiftRobotObject cfg = StoneRiftCfgManager.getInstance().randomOneRobotCfg();
            StoneRiftWorldMapPlayer robot = buildMapRobot(cfg,i);
            if (robot!=null) {
                mapPlayers.add(robot);
            }

        }
    }

    private static final String StoneRobotStr = "rb-stone-";

    private StoneRiftWorldMapPlayer buildMapRobot(StoneRiftRobotObject cfg, int robotIndex) {
        if (cfg == null) {
            LogUtil.error("random StoneRiftRobot cfg error .cfg is null");
            return null;
        }
        StoneRiftWorldMapPlayer mapPlayer = new StoneRiftWorldMapPlayer();
        mapPlayer.setPlayerName(ObjUtil.createRandomName(Common.LanguageEnum.forNumber(ServerConfig.getInstance().getLanguage())));
        mapPlayer.setRiftLv(randomFromIntList(cfg.getStoneriftlevel()));
        mapPlayer.setServerIndex(ServerConfig.getInstance().getServer());
        mapPlayer.setIcon(randomIcon());
        mapPlayer.setPlayerIdx(StoneRobotStr + cfg.getId() + "-" + robotIndex);
        mapPlayer.setHeader(Head.randomGetAvatar());
        mapPlayer.setBackGroundId(StoneRiftCfgManager.getInstance().randomPlayerMap());
        StoneRiftLevelObject lvCfg = StoneRiftLevel.getByLevel(mapPlayer.getRiftLv());
        if (lvCfg != null) {
            mapPlayer.setExp(RandomUtil.randomInScope(0, lvCfg.getUpexp()));
        }
        mapPlayer.setHeadBroder(HeadBorder.getDefaultHeadBorder());
        for (int[] ints : cfg.getFactorycfg()) {
            DbStoneRiftSteal riftSteal = new DbStoneRiftSteal();
            riftSteal.setLevel(RandomUtil.randomInScope(ints[1], ints[2]));
            riftSteal.setFactoryId(ints[0]);
            riftSteal.setCanSteal(RandomUtil.getRandom1000() < ints[3]);
            if (riftSteal.isCanSteal()) {
                riftSteal.setStealRewardCount(ints[4]);
                riftSteal.setCanSteal(true);
            }
            mapPlayer.getCanStealMap().put(riftSteal.getFactoryId(), riftSteal);
        }
        return mapPlayer;
    }

    private int randomFromIntList(int[] cfg) {
        return RandomUtil.randomInScope(cfg[0], cfg[1]);
    }

    private StoneRiftWorldMapPlayer buildMapPlayer(stoneriftEntity tempEntity) {
        StoneRiftWorldMapPlayer mapPlayer = new StoneRiftWorldMapPlayer();
        playerEntity dbPlayer = playerCache.getByIdx(tempEntity.getIdx());
        if (dbPlayer != null) {
            mapPlayer.setPlayerName(dbPlayer.getName());
            mapPlayer.setHeader(dbPlayer.getAvatar());
        }
        mapPlayer.setRiftLv(tempEntity.getDB_Builder().getLevel());
        mapPlayer.setServerIndex(ServerConfig.getInstance().getServer());
        mapPlayer.setIcon(randomIcon());
        mapPlayer.setPlayerIdx(tempEntity.getIdx());

        mapPlayer.setBackGroundId(tempEntity.getDB_Builder().getMapId());

        for (DbStoneRiftFactory factory : tempEntity.getDB_Builder().getFactoryMap().values()) {
            DbStoneRiftSteal riftSteal = new DbStoneRiftSteal();
            riftSteal.setFactoryId(factory.getCfgId());
            riftSteal.setLevel(factory.getLevel());
            boolean canSteal = isCanSteal(tempEntity, factory);
            if (canSteal && !CollectionUtils.isEmpty(factory.getSettleReward())) {
                Common.Reward reward = factory.getSettleReward().get(0);
                riftSteal.setStealRewardCount((int) (reward.getCount() * 0.1));
                riftSteal.setCanSteal(true);
            }
            mapPlayer.getCanStealMap().put(riftSteal.getFactoryId(), riftSteal);
        }

        return mapPlayer;
    }

    private boolean isCanSteal(stoneriftEntity value, DbStoneRiftFactory factory) {
        return value.storeMax(factory) && StoneRiftCfgManager.getInstance().isSpecial(factory.getCfgId());
    }

    private int randomIcon() {
        int[] canRandomIcon = StoneRiftCfgManager.getInstance().getCanRandomIcon();
        if (ArrayUtils.isEmpty(canRandomIcon)) {
            return 0;
        }
        return canRandomIcon[RandomUtils.nextInt(canRandomIcon.length)];
    }

    public void randomNextWorldMap(DbPlayerWorldMap dbPlayerWorldMap) {
        String mapId;
        //先随机本服地图
        if (!allLocalMapGo(dbPlayerWorldMap)) {
            mapId = randomLocalMapId(dbPlayerWorldMap);
        } else {
            //随机跨服地图
            mapId = randomCrossMap(dbPlayerWorldMap);
        }
        if (mapId == null) {
            dbPlayerWorldMap.getAlreadyGoMapId().clear();
            dbPlayerWorldMap.setUniqueMapId(null);
            randomNextWorldMap(dbPlayerWorldMap);
            return;
        }
        dbPlayerWorldMap.setUniqueMapId(mapId);
        dbPlayerWorldMap.getAlreadyGoMapId().add(mapId);
    }

    private boolean allLocalMapGo(DbPlayerWorldMap dbPlayerWorldMap) {
        return dbPlayerWorldMap.getAlreadyGoMapId().containsAll(findAllLocalMapId());

    }


    private String randomCrossMap(DbPlayerWorldMap dbPlayerWorldMap) {
        Set<String> maps = JedisUtil.jedis.smembers(AllWorldMapId);
        if (CollectionUtils.isEmpty(maps)) {
            throw new RuntimeException("no value from redis where key:" + AllWorldMapId);
        }

        if (maps.size() <= dbPlayerWorldMap.getAlreadyGoMapId().size()) {
            return null;
        }

        return randomOneFromSetByWithout(maps, dbPlayerWorldMap.getAlreadyGoMapId());
    }

    private String randomLocalMapId(DbPlayerWorldMap dbPlayerWorldMap) {

        return randomOneFromSetByWithout(findAllLocalMapId(), dbPlayerWorldMap.getAlreadyGoMapId());
    }

    private String randomOneFromSetByWithout(Set<String> keySet, List<String> alreadyGoWorldMap) {
        ArrayList<String> source = new ArrayList<>(keySet);
        String val;
        while (true) {
            val = source.get(RandomUtils.nextInt(source.size()));
            if (!alreadyGoWorldMap.contains(val)) {
                return val;
            }
        }
    }

    public String getCrossMapId(int serverIndex, int mapCfgId) {
        return serverIndex + "-" + mapCfgId;
    }

    public RetCodeEnum stealRes(String playerId, String stealPlayerIdx, int factoryId, String mapId, stoneriftEntity entity) {
        if (!JedisUtil.tryLockRedisKey(getStealLock(stealPlayerIdx), TimeUtil.MS_IN_A_S, 3)) {
            return RetCodeEnum.RCE_UnknownError;
        }
        StoneRiftWorldMapPlayer stealPlayer = findWorldMapPlayerById(mapId, stealPlayerIdx);
        if (stealPlayer == null) {
            JedisUtil.unlockRedisKey(getStealLock(stealPlayerIdx));
            return RetCodeEnum.RCE_Player_QueryPlayerNotExist;
        }
        if (stealPlayer.getStealPlayers().contains(playerId)) {
            JedisUtil.unlockRedisKey(getStealLock(stealPlayerIdx));
            return RetCodeEnum.RCE_StoneRift_AlreadyStealPlayer;
        }

        if (factoryId == 0) {
            Optional<Integer> first = StoneRiftCfgManager.getInstance().getFactoryWorth().stream().filter(id -> isPlayerFactoryCanSteal(stealPlayer, id)).findFirst();
            if (!first.isPresent()) {
                JedisUtil.unlockRedisKey(getStealLock(stealPlayerIdx));
                return RCE_StoneRift_NoResCanSteal;
            }
            factoryId = first.get();

        }
        DbStoneRiftSteal dbStoneRiftSteal = stealPlayer.getCanStealMap().get(factoryId);
        if (dbStoneRiftSteal == null || dbStoneRiftSteal.getStealRewardCount() <= 0) {
            JedisUtil.unlockRedisKey(getStealLock(stealPlayerIdx));
            return RCE_StoneRift_NoResCanSteal;
        }
        dbStoneRiftSteal.setStealCount(dbStoneRiftSteal.getStealCount() + 1);
        stealPlayer.getStealPlayers().add(playerId);
        saveStealPlayer(mapId, stealPlayer);

        List<Reward> stealReward = getStealReward(entity, dbStoneRiftSteal, factoryId);

        RewardManager.getInstance().doRewardByList(playerId, stealReward,
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_StoneRift, "矿区偷取"), true);

        EventUtil.triggerAddStoneRiftFactoryExp(playerId,factoryId,stealReward.get(0));

        if (!stealPlayer.isCanSteal()) {
            //广播不能再偷取
            broadcastWoldPlayerUpdateToAll(stealPlayer, mapId, false);
        }else {
            broadcastWoldPlayerUpdate(stealPlayer, playerId, false);
        }
        JedisUtil.unlockRedisKey(getStealLock(stealPlayerIdx));
        //扣对方的资源
        sendStolenRes(stealPlayer.getPlayerIdx(), stealPlayer.getServerIndex(), factoryId);
        EventUtil.triggerUpdateTargetProgress(playerId, TTE_StoneRift_StealRes, 1, 0);
        return RetCodeEnum.RCE_Success;
    }

    private boolean isPlayerFactoryCanSteal(StoneRiftWorldMapPlayer stealPlayer, Integer id) {
        DbStoneRiftSteal item = stealPlayer.getCanStealMap().get(id);
        return item != null && item.isCanSteal();
    }

    private List<Reward> getStealReward(stoneriftEntity entity, DbStoneRiftSteal dbStoneRiftSteal, int factoryId) {
        Common.Reward reward = StoneRiftCfgManager.getInstance().getBaseReward(factoryId).get(0).toBuilder().setCount(dbStoneRiftSteal.getStealRewardCount()).build();

        Common.Reward stealReward = RewardUtil.multiRewardByPerThousand(reward, entity.getScienceEffect(SRSE_StealMore));

        int rate = entity.getScienceEffect(SRSE_CanStealRareItem);

        if (rate <= 0 || !RandomUtil.canRandomHit(rate)) {
            return Collections.singletonList(stealReward);
        }
        List<Common.Reward> result = StoneRiftCfgManager.getInstance().getStealRateItem();

        result.add(stealReward);

        return result;
    }

    private void sendStolenRes(String playerIdx, int svrIndex, int factoryId) {
        if (isRobot(playerIdx)){
            return;
        }
        StoneRift.FG_StealStoneRiftRes.Builder msg = StoneRift.FG_StealStoneRiftRes.newBuilder();
        msg.setFactoryId(factoryId);
        msg.setPlayerId(playerIdx);
        BattleServerManager.getInstance().transferMsgGSToGS(MessageId.MsgIdEnum.BS_GS_CrossArenaRefInfo_VALUE, msg.build().toByteString(), svrIndex);
    }

    private boolean isRobot(String playerIdx) {
        return playerIdx != null && playerIdx.startsWith(StoneRobotStr);
    }

    public void broadcastWoldPlayerUpdateToAll(String playerIdx, boolean canSteal) {
        String playerFactoryMap = findPlayerFactoryMap(playerIdx);
        if (StringUtils.isEmpty(playerFactoryMap)) {
            return;
        }
        StoneRiftWorldMapPlayer mapPlayer = findWorldMapPlayerById(playerFactoryMap, playerIdx);
        if (mapPlayer == null) {
            LogUtil.error("findWorldMapPlayerById is null by mapId:{},playerId:{}", playerFactoryMap, playerIdx);
            return;
        }
        updateMapPlayer(playerFactoryMap, mapPlayer);
        broadcastWoldPlayerUpdateToAll(mapPlayer, playerFactoryMap, canSteal);
    }

    private void updateMapPlayer(String playerFactoryMap, StoneRiftWorldMapPlayer mapPlayer) {
        stoneriftEntity tempEntity = stoneriftCache.getByIdx(mapPlayer.getPlayerIdx());
        if (tempEntity == null) {
            LogUtil.error("only can update local server map player");
            return;
        }

        for (DbStoneRiftFactory factory : tempEntity.getDB_Builder().getFactoryMap().values()) {
            DbStoneRiftSteal riftSteal = mapPlayer.getCanStealMap().get(factory.getCfgId());
            if (riftSteal == null) {
                riftSteal = new DbStoneRiftSteal();
                riftSteal.setFactoryId(factory.getCfgId());
                riftSteal.setLevel(factory.getLevel());
            }
            boolean canSteal = isCanSteal(tempEntity, factory)
                    && riftSteal.getStealCount() < StoneRiftCfgManager.getInstance().getCanStolenTime()
                    && !CollectionUtils.isEmpty(factory.getSettleReward());

            riftSteal.setCanSteal(canSteal);
            if (canSteal) {
                Common.Reward reward = factory.getSettleReward().get(0);
                riftSteal.setStealRewardCount((int) (reward.getCount() * 0.1));
                riftSteal.setCanSteal(true);
            }
            mapPlayer.getCanStealMap().put(riftSteal.getFactoryId(), riftSteal);
        }
        saveStealPlayer(playerFactoryMap, mapPlayer);
    }

    public void broadcastWoldPlayerUpdate(StoneRiftWorldMapPlayer factoryOwner, String sendPlayer, boolean canSteal) {
        StoneRift.SC_UpdateStoneRiftWorldMapPlayer.Builder msg = buildWoldPlayerUpdateMsg(factoryOwner, canSteal);
        GlobalData.getInstance().sendMsg(sendPlayer, SC_UpdateStoneRiftWorldMapPlayer_VALUE, msg);
    }

    public void broadcastWoldPlayerUpdateToAll(StoneRiftWorldMapPlayer factoryOwner, String mapId, boolean canSteal) {
        StoneRift.SC_UpdateStoneRiftWorldMapPlayer.Builder msg = buildWoldPlayerUpdateMsg(factoryOwner, canSteal);
        Map<String, Integer> playerSvrIndex = findOnlinePlayerIpByMapId(mapId);
        // 兼容代码
        Map<String, String> sendMap = new HashMap<>();
        for (Entry<String, Integer> entry : playerSvrIndex.entrySet()) {
            sendMap.put(entry.getKey(), StringHelper.IntTostring(entry.getValue(), "0"));
        }
        GlobalData.getInstance().forwardMsg(sendMap, SC_UpdateStoneRiftWorldMapPlayer_VALUE, msg.build());
    }

    private StoneRift.SC_UpdateStoneRiftWorldMapPlayer.Builder buildWoldPlayerUpdateMsg(StoneRiftWorldMapPlayer factoryOwner, boolean canSteal) {
        StoneRift.SC_UpdateStoneRiftWorldMapPlayer.Builder msg = StoneRift.SC_UpdateStoneRiftWorldMapPlayer.newBuilder();
        StoneRift.StoneRiftWorldPlayer.Builder player = StoneRift.StoneRiftWorldPlayer.newBuilder();
        player.setIconId(factoryOwner.getIcon());
        player.setPlayerName(factoryOwner.getPlayerName());
        player.setLevel(factoryOwner.getRiftLv());
        player.setCanSteal(canSteal);
        msg.setPlayer(player);
        return msg;
    }

    private Map<String, Integer> findOnlinePlayerIpByMapId(String mapId) {
        Set<String> onlinePlayers = findOnlinePlayerByMapId(mapId);
        if (CollectionUtils.isEmpty(onlinePlayers)) {
            return Collections.emptyMap();
        }
        ArrayList<String> source = new ArrayList<>(onlinePlayers);
        List<String> playerSvrIndex = findPlayerServerIndex(onlinePlayers);
        return source.stream().collect(Collectors.toMap(key -> key, key -> {
            String svrIndexStr = playerSvrIndex.get(source.indexOf(key));
            return StringHelper.stringToInt(svrIndexStr, 0);
        }));
    }

    private List<String> findPlayerServerIndex(Set<String> onlinePlayers) {
        return JedisUtil.jedis.hmget(PlayerIp, onlinePlayers.toArray(new String[0]));
    }

    private void putPlayerServerIndex(String playerIdx, int serverIndex) {
        if (serverIndex <= 0) {
            return;
        }
        JedisUtil.jedis.hset(PlayerIp, playerIdx, StringHelper.IntTostring(serverIndex, "0"));
    }


    private String getStealLock(String stealPlayerIdx) {
        return lockPrefix + stealPlayerIdx;

    }

    private StoneRiftWorldMapPlayer findWorldMapPlayerById(String mapId, String playerIdx) {
        String data = JedisUtil.jedis.hget(MapPlayers + mapId, playerIdx);
        if (data == null) {
            return null;
        }
        return JSON.parseObject(data, StoneRiftWorldMapPlayer.class);
    }

    public void enterRiftWorldMap(String playerId) {
        stoneriftEntity stoneRift = stoneriftCache.getByIdx(playerId);
        if (stoneRift == null) {
            return;
        }
        String mapId = stoneRift.getDB_Builder().getDbPlayerWorldMap().getUniqueMapId();
        JedisUtil.jedis.sadd(worldMapOnline + mapId, playerId);
    }

    public void leaveRiftWorldMap(String playerId) {
        stoneriftEntity stoneRift = stoneriftCache.getByIdx(playerId);
        if (stoneRift == null) {
            return;
        }
        String mapId = stoneRift.getDB_Builder().getDbPlayerWorldMap().getUniqueMapId();
        JedisUtil.jedis.srem(worldMapOnline + mapId, playerId);
    }

    public Set<String> findOnlinePlayerByMapId(String mapId) {
        return JedisUtil.jedis.smembers(worldMapOnline + mapId);
    }

    public void dealStolen(String playerId, int factoryId) {
        stoneriftEntity st = stoneriftCache.getByIdx(playerId);
        if (st == null) {
            LogUtil.error("cant find entity by playerIdx:{}", playerId);
        }
        DbStoneRiftFactory factory = st.getDB_Builder().getFactoryMap().get(factoryId);
        if (factory == null) {
            LogUtil.error("player:{} factory:{} unlock ", playerId, factoryId);
            return;
        }
        SyncExecuteFunction.executeConsumer(st, t -> {
            if (!isCanSteal(st, factory)) {
                LogUtil.error("player:{},factory:{} store not reach max,can`t store", playerId, factory);
                return;
            }
            StoneRiftWorldMapPlayer mapPlayer = findWorldMapPlayerById(findPlayerFactoryMap(playerId), playerId);
            if (mapPlayer == null) {
                LogUtil.error("find WorldMapPlayerById:{} is null", playerId);
                return;
            }
            settleStolen(factory, mapPlayer.getCanStealMap().get(factoryId));
        });
    }

    private void settleStolen(DbStoneRiftFactory factory, DbStoneRiftSteal steal) {
        if (steal == null) {
            LogUtil.error("settleStolen fail ,steal is null");
            return;
        }
        List<Common.Reward> settleReward = factory.getSettleReward();
        List<Common.Reward> rewards = new ArrayList<>();
        for (Common.Reward reward1 : settleReward) {
            rewards.add(reward1.toBuilder().setCount(reward1.getCount() - steal.getStealRewardCount()).build());
        }
        factory.setSettleReward(rewards);
    }


    public int parseMapId(String mapId) {
        return Integer.parseInt(mapId.split("-")[1]);
    }

    public StoneRiftWorldMapPlayer findMapPlayerById(String uniqueMapId, String playerIdx) {
        return findWorldMapPlayerById(uniqueMapId, playerIdx);
    }
}
