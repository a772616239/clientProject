package model.warpServer.battleServer;

import common.GameConst;
import common.GameConst.EventType;
import common.GameConst.RedisKey;
import common.GlobalData;
import datatool.StringHelper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import model.warpServer.BaseNettyClient;
import server.event.Event;
import server.event.EventManager;
import static util.JedisUtil.jedis;
import util.LogUtil;

public class BattleServerManager {
    private static BattleServerManager instance = new BattleServerManager();

    // <serverIndex, BaseNettyClient>
    private Map<Integer, BaseNettyClient> bsClientMap = new ConcurrentHashMap<>();
    private List<Integer> bsList = new ArrayList<>(); // 已排序

    // <playerIdx, serverIndex>
    private Map<String, Integer> playerBattleInfo = new ConcurrentHashMap<>();
    //<serverIndex, ipPort>
    private Map<Integer, String> batServerIndexAddrMap = new ConcurrentHashMap<>();

    private long updateBattleServerTime;

    private long printServerInfoTime;

    public static BattleServerManager getInstance() {
        return instance;
    }

    public void addNettyChannel(Integer serverIndex, BaseNettyClient client) {
        if (serverIndex <= 0) {
            return;
        }
        if (client == null || client.getChannel() == null || !client.getChannel().isActive()) {
            return;
        }
        bsClientMap.put(serverIndex, client);
    }

    public void removeNettyChannelByAddr(String ipPort) {
        if (StringHelper.isNull(ipPort)) {
            return;
        }
        for (BaseNettyClient client : bsClientMap.values()) {
            if (client.getIpPort().equals(ipPort)) {
                bsClientMap.remove(ipPort);
                return;
            }
        }
    }

    public BaseNettyClient getActiveNettyClientByAddr(String addr) {
        return getActiveNettyClient(getServerIndexByAddr(addr));
    }

    public BaseNettyClient getActiveNettyClient(int serverIndex) {
        return bsClientMap.get(serverIndex);
    }

    public void addPlayerBattleInfo(String playerIdx, int serverIndex) {
        playerBattleInfo.put(playerIdx, serverIndex);
    }

    public void removePlayerBattleInfo(String playerIdx) {
        if (!playerBattleInfo.containsKey(playerIdx)) {
            return;
        }
        playerBattleInfo.remove(playerIdx);
    }

    public int getPlayerBattleInfo(String playerIdx) {
        Integer svrIndexObj = playerBattleInfo.get(playerIdx);
        return svrIndexObj != null ? svrIndexObj : 0;
    }

    public int getServerIndexByAddr(String addr) {
        if (StringHelper.isNull(addr)) {
            return 0;
        }
        for (Entry<Integer, String> entry : batServerIndexAddrMap.entrySet()) {
            if (entry.getValue().equals(addr)) {
                return entry.getKey();
            }
        }
        return 0;
    }

    public int getAvailableBattleServer() {
        int serverIndex;
        BaseNettyClient client;
        for (int i = 0; i < bsList.size(); i++) {
            serverIndex = bsList.get(i);
            client = bsClientMap.get(serverIndex);
            if (client != null) {
                return serverIndex;
            }
        }
        for (Entry<Integer, BaseNettyClient> entry : bsClientMap.entrySet()) {
            return entry.getKey();
        }
        return 0;
    }

    public synchronized void updateBattleServerInfo(long curTime) {
        Set<String> activeServerSet = jedis.zrangeByScore(RedisKey.BattleServerInfo, curTime, Long.MAX_VALUE);
        Set<String> serverOnlineCountSet = jedis.zrange(RedisKey.BattleOnlineCount,0, -1);
        Map<String, String> serverIndexMap = jedis.hgetAll(RedisKey.BattleServerIndexAddr);

        if (activeServerSet != null) {
            if (serverIndexMap != null) {
                batServerIndexAddrMap.clear();
                for (Entry<String, String> entry : serverIndexMap.entrySet()) {
                    if (!activeServerSet.contains(entry.getKey())) {
                        continue;
                    }
                    int svrIndex = StringHelper.stringToInt(entry.getKey(), 0);
                    if (svrIndex <= 0) {
                        continue;
                    }
                    batServerIndexAddrMap.put(svrIndex, entry.getValue());
                }
            }
            // 先清理过期的服务器
            Iterator<Entry<Integer, BaseNettyClient>> iter = bsClientMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, BaseNettyClient> entry = iter.next();
                BaseNettyClient client = entry.getValue();
                String checkSvrIndexStr = StringHelper.IntTostring(entry.getKey(), "0");
                if (activeServerSet.contains(checkSvrIndexStr) || client.getState() == 2) { // 如果正常连接状态不主动断开，可能是服务器人数满了
                    continue;
                }
                onServerCloseByServerIndex(entry.getKey());
                iter.remove();
            }
            if (serverOnlineCountSet == null) {
                return;
            }
            List<Integer> tmpBsList = new ArrayList<>();
            BaseNettyClient nettyClient;
            if (serverIndexMap != null) {
                String addr;
                for (String svrIndexStr : serverOnlineCountSet) {
                    if (!activeServerSet.contains(svrIndexStr)) {
                        continue;
                    }
                    int svrIndex = StringHelper.stringToInt(svrIndexStr, 0);
                    if (bsClientMap.containsKey(svrIndex)) {
                        continue;
                    }
                    addr = serverIndexMap.get(svrIndexStr);
                    if (StringHelper.isNull(addr)) {
                        continue;
                    }
                    try {
                        String[] ipPort = addr.split(":");
                        if (ipPort.length <= 0) {
                            continue;
                        }
                        bsList.add(svrIndex);
                        nettyClient = new BaseNettyClient(ipPort[0], Integer.valueOf(ipPort[1]));
                        nettyClient.init();
                        addNettyChannel(svrIndex, nettyClient);
                        nettyClient.setState(0);
                    } catch (Exception e) {
                        LogUtil.printStackTrace(e);
                    }
                }
                bsList.clear();
                bsList.addAll(tmpBsList);
            }
        } else {
            Iterator<Entry<Integer, BaseNettyClient>> iter = bsClientMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, BaseNettyClient> entry = iter.next();
                BaseNettyClient client = entry.getValue();
                if (client.getState() == 2) {
                    continue;
                }
                onServerCloseByServerIndex(entry.getKey());
                iter.remove();
            }
            bsList.clear();
        }
    }

    public void onServerCloseByAddr(String ipPort) {
        int serverIndex = BattleServerManager.getInstance().getServerIndexByAddr(ipPort);
        if (serverIndex <= 0) {
            return;
        }
        onServerCloseByServerIndex(serverIndex);
    }

    public void onServerCloseByServerIndex(int serverIndex) {
        if (serverIndex <= 0) {
            return;
        }
        BaseNettyClient client = bsClientMap.get(serverIndex);
        if (client == null || client.getState() == -1) {
            return;
        }

        LogUtil.info("BattleServer closed serverIndex:" + serverIndex + ",addr:" + client.getIpPort());
        client.setState(-1);
        client.close();
        // 结算PVP战斗并清除玩家战斗相关信息
        Iterator<Entry<String, Integer>> iter = playerBattleInfo.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Integer> entry = iter.next();
            if (serverIndex == entry.getValue()) {
                MistPlayer mistPlayer = MistPlayerCache.getInstance().queryObject(entry.getKey());
                if (mistPlayer != null && mistPlayer.getMistRoom() != null) {
                    Event event = Event.valueOf(EventType.ET_SettleMistPvpBattle, mistPlayer, mistPlayer.getMistRoom());
                    event.pushParam(-1, 1, false, false, false, 0);// 关闭战斗服导致战斗平局
                    EventManager.getInstance().dispatchEvent(event);
                }

                iter.remove();
            }
        }
    }

    public void printServerInfo() {
        // 打印游戏服
        GlobalData.getInstance().printGameServerInfo();

        // 打印战斗服
        LogUtil.info("===========BattleServerInfo start========");
        LogUtil.info("battle server count:" + bsClientMap.size());
        for (BaseNettyClient client : bsClientMap.values()) {
            LogUtil.info("battle server addr:" + client.getChannel().remoteAddress() + ",state:" + client.getState());
        }
        LogUtil.info("===========BattleServerInfo end========");
    }

    public void onTick(long curTime) {
        if (updateBattleServerTime <= curTime) {
            updateBattleServerInfo(curTime);
            updateBattleServerTime = curTime + GameConst.UpdateServerTime;
        }

        Iterator<Entry<Integer, BaseNettyClient>> iter = bsClientMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Integer, BaseNettyClient> entry = iter.next();
            BaseNettyClient nettyClient = entry.getValue();
            nettyClient.onTick(curTime);
            if (nettyClient.getState() == -1) {
                iter.remove();
            }
        }

        if (printServerInfoTime <= curTime) {
            printServerInfo();
            printServerInfoTime = curTime + 30000l;
        }
    }
}
