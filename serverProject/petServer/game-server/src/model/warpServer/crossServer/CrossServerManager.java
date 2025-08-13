package model.warpServer.crossServer;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import common.GameConst;
import common.GameConst.EventType;
import common.GameConst.RedisKey;
import common.GlobalData;
import static common.JedisUtil.jedis;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import common.tick.Tickable;
import datatool.StringHelper;
import io.netty.util.internal.ConcurrentSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import model.mistforest.MistForestManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.thewar.TheWarManager;
import model.warpServer.BaseNettyClient;
import org.springframework.util.CollectionUtils;
import platform.logs.LogService;
import platform.logs.entity.thewar.TheWarEnterLog;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.SC_KickOutFromMistForest;
import protocol.ServerTransfer.GS_CS_ExitTheWar;
import protocol.ServerTransfer.GS_CS_ForceKickOutFromMist;
import protocol.ServerTransfer.GS_CS_MistForestRoomInfo;
import protocol.ServerTransfer.GS_CS_TheWarTransInfo;
import protocol.TheWar.SC_KickOutFromTheWar;
import protocol.TransServerCommon.PlayerMistServerInfo;
import redis.clients.jedis.Tuple;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;

public class CrossServerManager implements Tickable {
    private static CrossServerManager instance = new CrossServerManager();
    /**
     * <serverIndex,>
     */
    private Map<Integer, BaseNettyClient> csClientMap = new ConcurrentHashMap<>();

    // <playerIdx, PlayerMistServerInfo>
    private Map<String, PlayerMistServerInfo> mistForestPlayerMap = new ConcurrentHashMap<>();
    // <roomLevel, Tuple<serverIndexList, score>> // 权重
    private Map<Long, List<Tuple>> availableMistForestMap = new ConcurrentHashMap<>();

    // <serverIndex, ipPort>
    private Map<Integer, String> crossServerIndexAddrMap = new ConcurrentHashMap<>();

    /**
     * 竞技场可使用的serverIndex
     */
    private Set<Integer> arenaAvailableIpSet = new ConcurrentSet<>();

    private Map<String, Integer> availableJoinRooms;
    // <CsIndex>
    private Set<Integer> theWarAvailableAddrSet = new ConcurrentSet<>();
    // <roomIdx, CsIndex>
    private Map<String, Integer> theWarRoomAddrMap = new ConcurrentHashMap<>();

    private Map<Integer, Set<String>> theWarPlayers = new ConcurrentHashMap<>(); // 正在远征玩法中的玩家

    private long updateCrossServerTime;
    private long printCrossServerTime;

    public static CrossServerManager getInstance() {
        return instance;
    }

    public void addNettyChannel(int serverIndex, BaseNettyClient client) {
        if (serverIndex <= 0) {
            return;
        }
        if (client == null || client.getChannel() == null || !client.getChannel().isActive()) {
            return;
        }
        csClientMap.put(serverIndex, client);
    }

    public void removeNettyChannelByAddr(String ipPort) {
        if (StringHelper.isNull(ipPort)) {
            return;
        }
//        if (mineFightClientIp != null && mineFightClientIp.equals(ipPort)) {
//            mineFightClientIp = null;
//        }
        for (BaseNettyClient client : csClientMap.values()) {
            if (client.getIpPort().equals(ipPort)) {
                csClientMap.remove(ipPort);
                break;
            }
        }
    }

    public BaseNettyClient getActiveNettyClient(int serverIndex) {
        return csClientMap.get(serverIndex);
    }

    public int getActiveNettyClientCount() {
        return csClientMap.size();
    }

    public int getServerIndexByCsAddr(String addr) {
        if (StringHelper.isNull(addr)) {
            return 0;
        }
        for (Entry<Integer, String> entry : crossServerIndexAddrMap.entrySet()) {
            if (entry.getValue().equals(addr)) {
                return entry.getKey();
            }
        }
        return 0;
    }

    //=========================迷雾森林Start================================//

    public BaseNettyClient getAvailableMistForestClient(int mistMode, int level) {
        long mistKey = GameUtil.mergeIntToLong(mistMode, level);
        BaseNettyClient availableClient = calcAvailableClient(mistKey);
        //找不到最优服务器时的容错处理
        if (availableClient == null) {
            for (BaseNettyClient baseNettyClient : csClientMap.values()) {
                availableClient = baseNettyClient;
                break;
            }
        }
        return availableClient;
    }

    protected BaseNettyClient calcAvailableClient(long mistKey) {
        List<Tuple> serverIndexList = availableMistForestMap.get(mistKey);
        if (serverIndexList == null) {
            return null;
        }
        try {
            long score;
            BaseNettyClient availableClient = null;
            BaseNettyClient tmpClient;

            int tmpCount;
            int tmpOtherFactor;
            int emptyCount = 0;
            int otherFactor = 0;
            for (Tuple tuple : serverIndexList) {
                tmpClient = csClientMap.get(tuple.getElement());
                if (tmpClient == null) {
                    continue;
                }
                score = (long) tuple.getScore();
                tmpCount = GameUtil.getLowLong(score); // 剩余人数
                tmpOtherFactor = GameUtil.getHighLong(score); // 其他影响因子
                if (tmpOtherFactor > 100000) {
                    continue;
                }
                if ((emptyCount == 0 && (availableClient == null || tmpCount > 0)) || emptyCount > tmpCount || (emptyCount == tmpCount && otherFactor > tmpOtherFactor)){
                    emptyCount = tmpCount;
                    otherFactor = tmpOtherFactor;
                    availableClient = tmpClient;
                }
            }
            return availableClient;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    public void addMistForestPlayer(String playerIdx, EnumMistRuleKind mistRule, int serverIndex) {
        if (StringHelper.isNull(playerIdx) || serverIndex <= 0) {
            return;
        }
        mistForestPlayerMap.put(playerIdx, PlayerMistServerInfo.newBuilder().setMistRule(mistRule).setServerIndex(serverIndex).build());
        LogUtil.info("Add MistForestPlayer[" + playerIdx + "] serverIndex=" + serverIndex + ",mistRule="+mistRule);

        if (mistRule == EnumMistRuleKind.EMRK_Maze) {
            MistForestManager.getInstance().getMazeManager().addPlayerInMazeMap(playerIdx, serverIndex);
        }
    }

    public void removeMistForestPlayer(String playerIdx) {
        if (StringHelper.isNull(playerIdx) || !mistForestPlayerMap.containsKey(playerIdx)) {
            return;
        }
        PlayerMistServerInfo serverInfo = mistForestPlayerMap.get(playerIdx);
        if (serverInfo != null && serverInfo.getMistRule() == EnumMistRuleKind.EMRK_GhostBuster) {
            MistForestManager.getInstance().getGhostBusterManager().onPlayerExitGhostBuster(playerIdx);
        }
        mistForestPlayerMap.remove(playerIdx);
        LogUtil.info("Remove MistForestPlayer[" + playerIdx + "]");
    }

    public PlayerMistServerInfo getMistForestPlayerServerInfo(String playerIdx) {
        return mistForestPlayerMap.get(playerIdx);
    }

    public int getMistForestPlayerServerIndex(String playerIdx) {
        PlayerMistServerInfo plyMistSvrInfo = mistForestPlayerMap.get(playerIdx);
        if (plyMistSvrInfo == null) {
            return 0;
        }
        return plyMistSvrInfo.getServerIndex();
    }

    public boolean sendMsgToMistForest(String playerIdx, int msgId, GeneratedMessageV3.Builder<?> builder, boolean checkOnline) {
        if (checkOnline && !GlobalData.getInstance().checkPlayerOnline(playerIdx)) {
            return false;
        }
        int serverIndex = getMistForestPlayerServerIndex(playerIdx);
        if (serverIndex <= 0) {
            return false;
        }
        BaseNettyClient nettyClient = getActiveNettyClient(serverIndex);
        if (nettyClient == null) {
            return false;
        }
        nettyClient.send(msgId, builder);
        return true;
    }

    public boolean transferMsgToMistForest(String playerIdx, int msgId, ByteString msgData, boolean checkOnline) {
        if (checkOnline && !GlobalData.getInstance().checkPlayerOnline(playerIdx)) {
            return false;
        }
        int serverIndex = getMistForestPlayerServerIndex(playerIdx);
        if (serverIndex <= 0) {
            return false;
        }
        BaseNettyClient nettyClient = getActiveNettyClient(serverIndex);
        if (nettyClient == null) {
            return false;
        }
        GS_CS_MistForestRoomInfo.Builder builder = GS_CS_MistForestRoomInfo.newBuilder();
        builder.setMsgId(msgId);
        builder.setMsgData(msgData);
        builder.setPlayerId(playerIdx);
        nettyClient.send(MsgIdEnum.GS_CS_MistForestRoomInfo_VALUE, builder);
        return true;
    }

    public void kickOutAllMistPlayer() {
        playerEntity player;
        GS_CS_ForceKickOutFromMist.Builder builder = GS_CS_ForceKickOutFromMist.newBuilder();
        for (Integer serverIndex : crossServerIndexAddrMap.keySet()) {
            builder.clear();
            for (Entry<String, PlayerMistServerInfo> playerEntry : mistForestPlayerMap.entrySet()) {
                player = playerCache.getByIdx(playerEntry.getKey());
                if (player != null && playerEntry.getValue().getServerIndex() ==serverIndex) {
                    builder.addPlayerIdx(player.getIdx());
                    GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_KickOutFromMistForest_VALUE, SC_KickOutFromMistForest.newBuilder());
                    SyncExecuteFunction.executeConsumer(player, entity -> entity.settleMistCarryReward());
                }
            }
            BaseNettyClient client = csClientMap.get(serverIndex);
            if (client == null) {
                continue;
            }
            client.send(MsgIdEnum.GS_CS_ForceKickOutFromMist_VALUE, builder);
        }
        mistForestPlayerMap.clear();
    }
    //=========================迷雾森林End================================//

    //=========================竞技场 start================================//

    public BaseNettyClient getAvailableArenaClient() {
        for (Integer serverIndex : arenaAvailableIpSet) {
            BaseNettyClient client = csClientMap.get(serverIndex);
            if (client != null) {
                return client;
            }
        }
        LogUtil.error("CrossServerManager.getAvailableArenaIpClient, can not find a available arena server, arenaIpSet:" + arenaAvailableIpSet);
        return null;
    }

    public boolean sendMsgToArena(String playerIdx, int msgId, GeneratedMessageV3.Builder<?> builder, boolean checkOnline) {
        if (checkOnline && !GlobalData.getInstance().checkPlayerOnline(playerIdx)) {
            return false;
        }
        return sendMsgToArena(msgId, builder);
    }

    public boolean sendMsgToArena(int msgId, GeneratedMessageV3.Builder<?> builder) {
        BaseNettyClient client = getAvailableArenaClient();
        if (client == null) {
            LogUtil.error("CrossServerManager.sendMsgToArena, can not find available arena client, need send msgId:" + msgId);
            return false;
        }
        client.send(msgId, builder);
        return true;
    }
    //=========================竞技场 End================================//

    //=========================战戈 start================================//
    public String getAvailableJoinRoomIdx() {
        if (!CollectionUtils.isEmpty(availableJoinRooms)) {
            // 获取第一个
            List<String> roomList = availableJoinRooms.entrySet().stream()
                    .filter(entry->entry.getValue() > 0)
                    .sorted(Comparator.comparingInt(o -> o.getValue()))
                    .map(Entry::getKey).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(roomList)) {
                for (String roomId : roomList) {
                    return roomId;
                }
            }
        }
        return "";
    }

    public void addNewAvailableRoomIdx(String newRoomIdx, int svrIndex) {
        if (availableJoinRooms == null) {
            availableJoinRooms = new ConcurrentHashMap<>();
        }
        // 针对新房间可能延迟造成出生点查询不到的问题
        if (!availableJoinRooms.containsKey(newRoomIdx)) {
            availableJoinRooms.put(newRoomIdx, 1);
        }

        if (!theWarRoomAddrMap.containsKey(newRoomIdx)) {
            theWarRoomAddrMap.put(newRoomIdx, svrIndex);
        }
    }

    public int getSvrIndexByWarRoomIdx(String roomIdx) {
        if (StringHelper.isNull(roomIdx)) {
            return 0;
        }
        Integer svrIndexStr = theWarRoomAddrMap.get(roomIdx);
        return svrIndexStr != null ? svrIndexStr : 0;
    }

    public boolean sendMsgToWarRoom(String roomIdx, int msgId, GeneratedMessageV3.Builder<?> builder) {
        int serverIndex = getSvrIndexByWarRoomIdx(roomIdx);
        if (serverIndex <= 0) {
            return false;
        }
        BaseNettyClient client = csClientMap.get(serverIndex);
        if (client == null) {
            return false;
        }
        client.send(msgId, builder);
        return true;
    }

    public boolean sendMsgToWarServer(int msgId, GeneratedMessageV3.Builder<?> builder) {
        BaseNettyClient client = getAvailableTheWarServer();
        if (client == null) {
            return false;
        }
        client.send(msgId, builder);
        return true;
    }

    public boolean transferMsgToTheWar(String roomIdx, String playerIdx, int msgId, ByteString msgData) {
        int serverIndex = getSvrIndexByWarRoomIdx(roomIdx);
        if (serverIndex <= 0) {
            return false;
        }
        BaseNettyClient client = csClientMap.get(serverIndex);
        if (client == null) {
            return false;
        }
        GS_CS_TheWarTransInfo.Builder builder = GS_CS_TheWarTransInfo.newBuilder();
        builder.setMsgId(msgId);
        builder.setMsgData(msgData);
        builder.setPlayerIdx(playerIdx);
        client.send(MsgIdEnum.GS_CS_TheWarTransInfo_VALUE, builder);
        return true;
    }

    public BaseNettyClient getAvailableTheWarServer() {
        for (Integer serverIndex : theWarAvailableAddrSet) {
            BaseNettyClient client = csClientMap.get(serverIndex);
            if (client != null) {
                return client;
            }
        }
        LogUtil.error("CrossServerManager.getAvailableTheWarServer not found theWarServer");
        return null;
    }

    public void addTheWarPlayer(int serverIndex, String playerIdx) {
        if (serverIndex <= 0 || StringHelper.isNull(playerIdx)) {
            return;
        }
        Set<String> playerSet = theWarPlayers.get(serverIndex);
        if (playerSet == null) {
            playerSet = new ConcurrentSet<>();
            theWarPlayers.put(serverIndex, playerSet);
        }
        playerSet.add(playerIdx);
    }

    public void removeTheWarPlayer(int serverIndex, String playerIdx) {
        if (StringHelper.isNull(playerIdx)) {
            return;
        }

        Set<String> playerSet = theWarPlayers.get(serverIndex);
        if (!CollectionUtils.isEmpty(playerSet) && playerSet.contains(playerIdx)) {
            playerSet.remove(playerIdx);
        }
    }

    public void removeTheWarPlayer(playerEntity player) {
        if (player == null || StringHelper.isNull(player.getDb_data().getTheWarRoomIdx())) {
            return;
        }
        int serverIndex = theWarRoomAddrMap.get(player.getDb_data().getTheWarRoomIdx());
        if (serverIndex <= 0) {
            return;
        }
        removeTheWarPlayer(serverIndex, player.getIdx());
        LogService.getInstance().submit(new TheWarEnterLog(player, TheWarManager.getInstance().getMapName(), false, 0));
    }

    public boolean isPlayerInTheWarNow(playerEntity player) {
        if (player == null) {
            return false;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            return false;
        }
        int serverIndex = getSvrIndexByWarRoomIdx(roomIdx);
        if (serverIndex <= 0) {
            return false;
        }
        Set<String> playerSet = theWarPlayers.get(serverIndex);
        if (CollectionUtils.isEmpty(playerSet)) {
            return false;
        }
        return playerSet.contains(player.getIdx());
    }

    public void kickOutAllWarPlayers() {
        playerEntity player;
        for (Entry<Integer, Set<String>> entry : theWarPlayers.entrySet()) {
            GS_CS_ExitTheWar.Builder builder = GS_CS_ExitTheWar.newBuilder();
            for (String playerIdx : entry.getValue()) {
                player = playerCache.getByIdx(playerIdx);
                if (player == null || !player.isOnline()) {
                    continue;
                }
                builder.addPlayerIdx(playerIdx);
                GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_KickOutFromTheWar_VALUE, SC_KickOutFromTheWar.newBuilder());
            }
            BaseNettyClient client = csClientMap.get(entry.getKey());
            if (client == null) {
                continue;
            }
            client.send(MsgIdEnum.GS_CS_ExitTheWar_VALUE, builder);
        }
        theWarPlayers.clear();
    }
    //=========================战戈 End================================//

    public synchronized void updateCrossServerInfo(long curTime) {
        if (ServerConfig.getInstance().isSingleRun()) {
            return;
        }

        Set<String> activeServerSet = null;
        Map<String, String> serverIndexMap = null;
        Set<Tuple> mistRoomLevelSet;
        activeServerSet = jedis.zrangeByScore(RedisKey.CrossServerInfo, curTime, Long.MAX_VALUE);
        serverIndexMap = jedis.hgetAll(RedisKey.CrossServerIndexAddr);
        if (activeServerSet != null && !activeServerSet.isEmpty()) {
            crossServerIndexAddrMap.clear();
            if (serverIndexMap != null) {
                for (Entry<String, String> entry : serverIndexMap.entrySet()) {
                    if (!activeServerSet.contains(entry.getKey())) {
                        continue;
                    }
                    int serverIndex = StringHelper.stringToInt(entry.getKey(), 0);
                    if (serverIndex <= 0) {
                        continue;
                    }
                    crossServerIndexAddrMap.put(serverIndex, entry.getValue());
                }
            }
            for (int mode = EnumMistRuleKind.EMRK_Common_VALUE; mode <= EnumMistRuleKind.EMRK_GhostBuster_VALUE; ++mode) {
                for (int level = 1; level <= GameConst.MistMaxRoomCount; ++level) {
                    if (mode != EnumMistRuleKind.EMRK_Common_VALUE && level > 1) {
                        continue;
                    }
                    long mistKey = GameUtil.mergeIntToLong(mode, level);
                    mistRoomLevelSet = jedis.zrangeByScoreWithScores(RedisKey.MistForestInfo + mistKey, 0, Long.MAX_VALUE);
                    if (mistRoomLevelSet == null || mistRoomLevelSet.isEmpty()) {
                        if (availableMistForestMap.containsKey(mistKey)) {
                            availableMistForestMap.remove(mistKey);
                        }
                        continue;
                    }
                    List<Tuple> mistRoomSrvList = new ArrayList<>();
                    for (Tuple mistRoomServerTuple : mistRoomLevelSet) {
                        if (!activeServerSet.contains(mistRoomServerTuple.getElement())) {
                            continue;
                        }
                        mistRoomSrvList.add(mistRoomServerTuple);
                    }
                    availableMistForestMap.put(mistKey, mistRoomSrvList);
                }
            }

            // 单人地图
//                mistRoomLevelSet = jedis.zrangeByScore(RedisKey.MistForestInfo + 1000, 0, 100000);
//                if (mistRoomLevelSet != null) {
//                    List<String> mistRoomSrvList = new ArrayList<>();
//                    for (String mistRoomIp : mistRoomLevelSet) {
//                        if (!activeServerSet.contains(mistRoomIp)) {
//                            continue;
//                        }
//                        mistRoomSrvList.add(mistRoomIp);
//                    }
//                    availableMistForestMap.put(1000, mistRoomSrvList);
//                }

//                tmpMineFightClientIp = jedis.get(RedisKey.MineFightInfo);
        }
        //矿区
//            mineFightClientIp = tmpMineFightClientIp;

        //竞技场
        Set<String> serverIndexSet = jedis.zrangeByScore(RedisKey.ARENA_SERVER_INFO, 0, 1000);
        if (CollectionUtils.isEmpty(serverIndexSet)) {
            LogUtil.info("CrossServerManager.updateCrossServerInfo, arena available ips is null");
        } else {
            arenaAvailableIpSet.clear();
            for (String svrIndexStr : serverIndexSet) {
                int svrIndex = StringHelper.stringToInt(svrIndexStr, 0);
                if (svrIndex > 0) {
                    arenaAvailableIpSet.add(svrIndex);
                }
            }
        }

        // 远征
        Map<String, String> availableJoinRoomMap = jedis.hgetAll(RedisKey.TheWarAvailableJoinRoomInfo);
        if (availableJoinRoomMap != null && !availableJoinRoomMap.isEmpty()) {
            if (availableJoinRooms == null) {
                availableJoinRooms = new ConcurrentHashMap<>();
            }
            availableJoinRooms.clear();
            for (Entry<String, String> entry : availableJoinRoomMap.entrySet()) {
                int svrIndex = StringHelper.stringToInt(entry.getValue(), 0);
                if (svrIndex > 0) {
                    availableJoinRooms.put(entry.getKey(), svrIndex);
                }
            }
        }

        Set<String> theWarAddsSet = jedis.zrangeByScore(RedisKey.TheWarServerLoadInfo, 0, 100000);
        if (!CollectionUtils.isEmpty(theWarAddsSet)) {
            theWarAvailableAddrSet.clear();
            for (String svrIndexStr : theWarAddsSet) {
                int svrIndex = StringHelper.stringToInt(svrIndexStr, 0);
                if (svrIndex <= 0) {
                    continue;
                }
                theWarAvailableAddrSet.add(svrIndex);
            }
            LogUtil.info("updateCrossServerInfo, theWar addr updated, size=" + theWarAvailableAddrSet.size());
        }
        Map<String, String> tmpTheWarRoomAddrMap = jedis.hgetAll(RedisKey.TheWarRoomServerIndex);
        theWarRoomAddrMap.clear();
        if (!CollectionUtils.isEmpty(tmpTheWarRoomAddrMap)) {
            for (Entry<String, String> entry : tmpTheWarRoomAddrMap.entrySet()) {
                int svrIndex = StringHelper.stringToInt(entry.getValue(), 0);
                if (svrIndex <= 0) {
                    continue;
                }
                theWarRoomAddrMap.put(entry.getKey(), svrIndex);
            }
        }

        // 清除不活跃的服务器
        Iterator<Entry<Integer, BaseNettyClient>> iter = csClientMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Integer, BaseNettyClient> entry = iter.next();
            String checkSvrIndexStr = StringHelper.IntTostring(entry.getKey(), "0");
            if (activeServerSet != null && activeServerSet.contains(checkSvrIndexStr)) {
                continue;
            }
            BaseNettyClient client = entry.getValue();
            if (client.getState() != 2) {
                onServerCloseByServerIndex(entry.getKey());
                iter.remove();
            }
        }

        // 更新迷雾森林服务器，须在矿区服更新后
        if (activeServerSet != null && serverIndexMap != null) {
            String addr;
            for (String svrIndexStr : activeServerSet) {
                addr = serverIndexMap.get(svrIndexStr);
                if (StringHelper.isNull(addr)) {
                    continue;
                }
                int svrIndex = StringHelper.stringToInt(svrIndexStr, 0);
                if (svrIndex <= 0) {
                    continue;
                }
                if (csClientMap.containsKey(svrIndex)) {
                    continue;
                }
                try {
                    //如果不是IP地址则跳过
                    String[] ipPort = addr.split(":");
                    if (ipPort.length <= 0) {
                        continue;
                    }
                    BaseNettyClient mistForestClient = new BaseNettyClient(ipPort[0], Integer.parseInt(ipPort[1]), 1);
                    mistForestClient.init();
                    mistForestClient.setServerIndex(svrIndex);

                    addNettyChannel(svrIndex, mistForestClient);
                    mistForestClient.setState(0);
                } catch (NumberFormatException e) {
                    LogUtil.printStackTrace(e);
                }
            }
        }
    }

    public void onServerCloseByAddr(String ipPort) {
        int serverIndex = CrossServerManager.getInstance().getServerIndexByCsAddr(ipPort);
        if (serverIndex <= 0) {
            return;
        }
        onServerCloseByServerIndex(serverIndex);
    }

    public void onServerCloseByServerIndex(int serverIndex) {
        BaseNettyClient client = csClientMap.get(serverIndex);
        if (client == null || client.getState() == -1) {
            return;
        }
        LogUtil.info("CrossServer closed serverIndex:" + serverIndex + ",addr:" + client.getIpPort());
        client.setState(-1);
        client.close();

        playerEntity player;
        for (Entry<String, PlayerMistServerInfo> entry : mistForestPlayerMap.entrySet()) {
            if (serverIndex != entry.getValue().getServerIndex()) {
                continue;
            }
            player = playerCache.getByIdx(entry.getKey());
            if (player == null) {
                continue;
            }
            if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(player.getIdx()) <= 0) {
                continue;
            }
            Event event = Event.valueOf(EventType.ET_MistForestServerClosed, GameUtil.getDefaultEventSource(), player);
            EventManager.getInstance().dispatchEvent(event);
        }

        List<String> removePlayerList = new ArrayList<>();
        Set<String> theWarPlayerSet = theWarPlayers.get(serverIndex);
        if (!CollectionUtils.isEmpty(theWarPlayerSet)) {
            String warMapName = TheWarManager.getInstance().getMapName();
            for (String playerIdx : theWarPlayerSet) {
                removePlayerList.add(playerIdx);
                player = playerCache.getByIdx(playerIdx);
                if (player == null || StringHelper.isNull(player.getDb_data().getTheWarRoomIdx())) {
                    continue;
                }
                GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_KickOutFromTheWar_VALUE, SC_KickOutFromTheWar.newBuilder());
                LogService.getInstance().submit(new TheWarEnterLog(player, warMapName, false, 0));
            }
        }

        for (String removePlayerIdx : removePlayerList) {
            removeTheWarPlayer(serverIndex, removePlayerIdx);
        }

//        if (!StringHelper.isNull(mineFightClientIp) && mineFightClientIp.equals(serverIndex)) {
//            mineFightClientIp = null;
//        }

    }

    public void printActiveCrossServerInfo() {
        LogUtil.info("===========CrossServerInfo start========");
        LogUtil.info("cross server count:" + csClientMap.size());
        for (Entry<Integer, BaseNettyClient> entry : csClientMap.entrySet()) {
            LogUtil.info("cross server serverIndex:" + entry.getKey() + ", addr:" + entry.getValue().getIpPort() + ",state:" + entry.getValue().getState());
        }
        LogUtil.info("===========CrossServerInfo end========");
    }

    @Override
    public void onTick() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (updateCrossServerTime <= curTime) {
            updateCrossServerInfo(curTime);
            updateCrossServerTime = curTime + GameConst.UpdateServerTime;
        }
        Iterator<Entry<Integer, BaseNettyClient>> iter = csClientMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Integer, BaseNettyClient> entry = iter.next();
            BaseNettyClient nettyClient = entry.getValue();
            nettyClient.onTick(curTime);
            if (nettyClient.getState() == -1) {
                iter.remove();
            }
        }

        if (printCrossServerTime <= curTime) {
            printActiveCrossServerInfo();
            printCrossServerTime = curTime + 30000L;
        }
    }
}
