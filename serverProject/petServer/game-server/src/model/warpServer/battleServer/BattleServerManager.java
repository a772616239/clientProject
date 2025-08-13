package model.warpServer.battleServer;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import common.GameConst;
import common.GameConst.EventType;
import common.GameConst.RedisKey;
import common.GlobalData;
import static common.JedisUtil.jedis;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import common.tick.Tickable;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.BaseNettyClient;
import protocol.Battle;
import protocol.Battle.BattleTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer;
import protocol.ServerTransfer.GS_BS_TransferBattleMsg;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;

public class BattleServerManager implements Tickable {
    private static BattleServerManager instance = new BattleServerManager();
    // <serverIndex, BaseNettyClient>
    private Map<Integer, BaseNettyClient> bsClientMap = new ConcurrentHashMap<>();
    private List<Integer> bsList = new ArrayList<>(); // 已排序
    // <playerIdx, serverIndex>
    private Map<String, Integer> playerBattleInfo = new ConcurrentHashMap<>();

    public Map<Integer, String> getBatServerIndexAddrMap() {
        return batServerIndexAddrMap;
    }

    //<serverIndex, ipPort>
    private Map<Integer, String> batServerIndexAddrMap = new ConcurrentHashMap<>();

    /**
     * 观战玩家信息<玩家ID, serverIndex>
     */
    private Map<String, Integer> playerWatchInfo = new ConcurrentHashMap<>();

    private long updateBattleServerTime;

    private long printBattleServerTime;

    public static BattleServerManager getInstance() {
        return instance;
    }

    public void addNettyChannel(int serverIndex, BaseNettyClient client) {
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

    /**
     * @return
     * 获取全部战斗服务器
     */
    public List<BaseNettyClient> getAllBattleServer() {
        List<BaseNettyClient> temp = new ArrayList<>();
        temp.addAll(bsClientMap.values());
        return temp;
    }

    public int getAvailableBattleServerIndex() {
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

    public BaseNettyClient getAvailableBattleServer() {
        int serverIndex = getAvailableBattleServerIndex();
        if (serverIndex <= 0){
            LogUtil.error("getAvailableBattleServer error,  no AvailableBattleServerIndex");
            return null;
        }
        return bsClientMap.get(serverIndex);
    }

    public BaseNettyClient getActiveNettyClientByIpPort(String ipPort) {
        return getActiveNettyClient(getServerIndexByAddr(ipPort));
    }

    public BaseNettyClient getActiveNettyClient(int serverIndex) {
        if (serverIndex <= 0) {
            return null;
        }
        return bsClientMap.get(serverIndex);
    }

    public String getBattleServerIpport() {
        for (BaseNettyClient netClient : bsClientMap.values()) {
            if (netClient.getState() == 2) {
                return netClient.getIpPort();
            }
        }
        return null;
    }

    public void addPlayerBattleInfo(String playerIdx, int serverIndex) {
        if (serverIndex <= 0) {
            return;
        }
        playerBattleInfo.put(playerIdx, serverIndex);
    }

    public void removePlayerBattleInfo(String playerIdx) {
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

    public BaseNettyClient getBsClientByPlayerIdx(String idx, boolean checkOnline) {
        playerEntity player = playerCache.getByIdx(idx);
        if (player == null) {
            return null;
        }
        //TODO 改动
        if (!BattleManager.getInstance().isInBattle(idx)
                || BattleManager.getInstance().getBattleType(idx) != BattleTypeEnum.BTE_PVP) {
            return null;
        }
//        if (player.getBattleController().getBattleId() == 0
//                || player.getBattleController().getBattleType() != BattleTypeEnum.BTE_PVP_VALUE) {
//            return null;
//        }
        if (checkOnline) {
            GameServerTcpChannel channel = GlobalData.getInstance().getOnlinePlayerChannel(idx);
            if (channel != null && !channel.channel.isActive()) {
                return null;
            }
        }
        int serverIndex = getPlayerBattleInfo(player.getIdx());
        if (serverIndex <= 0) {
            return null;
        }
        return getActiveNettyClient(serverIndex);
    }

    //todo 将以前的消息号用到Forward.proto的消息号下
    public boolean transferMsgGSToGSRom(int msgId, ByteString msgData) {
        transferMsgGSToGS(msgId,msgData,0);
        return true;
    }

    public boolean transferMsgGSToGS(int msgId, ByteString msgData, int toSvrIndex) {
        BaseNettyClient nettyClient = getAvailableBattleServer();
        if (nettyClient == null) {
            return false;
        }
        ServerTransfer.GS_BS_TransferGSTOGSMsg.Builder builder = ServerTransfer.GS_BS_TransferGSTOGSMsg.newBuilder();
        builder.setMsgId(msgId);
        builder.setMsgData(msgData);
        if (toSvrIndex > 0) {
            builder.setSvrIndex(toSvrIndex);
        }
        nettyClient.send(MsgIdEnum.GS_BS_TransferGSTOGSMsg_VALUE, builder);
        return true;
    }


    public boolean transferMsgToBattleServer(String playerIdx, int msgId, ByteString msgData, boolean checkOnline) {
        BaseNettyClient nettyClient = getBsClientByPlayerIdx(playerIdx, checkOnline);
        if (nettyClient == null) {
            return false;
        }
        GS_BS_TransferBattleMsg.Builder builder = GS_BS_TransferBattleMsg.newBuilder();
        builder.setMsgId(msgId);
        builder.setMsgData(msgData);
        builder.setPlayerIdx(playerIdx);
        nettyClient.send(MsgIdEnum.GS_BS_TransferBattleMsg_VALUE, builder);
        return true;
    }

    public boolean transferMsgToBattleServerExt(String playerIdx, int msgId, ByteString msgData) {
        Integer serverIndex = getPlayerWatchInfo().get(playerIdx);
        if (serverIndex == null || serverIndex <= 0) {
            return false;
        }
        BaseNettyClient nettyClient = getActiveNettyClient(serverIndex);
        if (nettyClient == null) {
            return false;
        }
        GS_BS_TransferBattleMsg.Builder builder = GS_BS_TransferBattleMsg.newBuilder();
        builder.setMsgId(msgId);
        builder.setMsgData(msgData);
        builder.setPlayerIdx(playerIdx);
        nettyClient.send(MsgIdEnum.GS_BS_TransferBattleMsg_VALUE, builder);
        return true;
    }

    public boolean sendMsgToBattleServer(String playerIdx, int msgId, GeneratedMessageV3.Builder<?> builder, boolean checkOnline) {
        if (builder == null) {
            return false;
        }
        BaseNettyClient nettyClient = getBsClientByPlayerIdx(playerIdx, checkOnline);
        if (nettyClient == null) {
            return false;
        }
        nettyClient.send(msgId, builder);
        return true;
    }

    public synchronized void updateBattleServerInfo(long curTime) {
        if (ServerConfig.getInstance().isSingleRun()) {
            return;
        }
        Set<String> activeServerSet = jedis.zrangeByScore(RedisKey.BattleServerInfo, curTime, Long.MAX_VALUE);
        Set<String> serverOnlineCountSet = jedis.zrange(RedisKey.BattleOnlineCount,0, -1);
        Map<String, String> serverIndexMap = jedis.hgetAll(RedisKey.BattleServerIndexAddr);


        if (activeServerSet != null) {
            batServerIndexAddrMap.clear();
            if (serverIndexMap != null) {
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
                String checkSvrIndex = StringHelper.IntTostring(entry.getKey(), "0");
                if (activeServerSet.contains(checkSvrIndex) || client.getState() == 2) {
                    continue;
                }
                onServerCloseByServerIndex(entry.getKey());
                iter.remove();
            }
            if (serverOnlineCountSet == null) {
                return;
            }
            String addr;
            BaseNettyClient nettyClient;
            List<Integer> tmpBsList = new ArrayList<>();
            for (String svrIndexStr : serverOnlineCountSet) {
                if (!activeServerSet.contains(svrIndexStr)) {
                    continue;
                }
                int svrIndex = StringHelper.stringToInt(svrIndexStr, 0);
                if (svrIndex <= 0) {
                    continue;
                }
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
                    tmpBsList.add(svrIndex);
                    nettyClient = new BaseNettyClient(ipPort[0], Integer.valueOf(ipPort[1]), 0);
                    nettyClient.init();
                    nettyClient.setServerIndex(svrIndex);
                    addNettyChannel(svrIndex, nettyClient);
                    nettyClient.setState(0);
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                }
            }
            bsList.clear();
            bsList.addAll(tmpBsList);
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

        playerEntity player;
        Iterator<Entry<String, Integer>> iter = playerBattleInfo.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Integer> entry = iter.next();
            if (serverIndex != entry.getValue()) {
                continue;
            }
            player = playerCache.getByIdx(entry.getKey());
            if (player != null) {
                Event event = Event.valueOf(EventType.ET_BattleServerClosed, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
                event.pushParam(player);
                EventManager.getInstance().dispatchEvent(event);
            }
            iter.remove();
        }

//        MineObjCache.getInstance().onBattleServerClose();
    }

    public void printActiveBattleServerInfo() {
        LogUtil.info("===========BattleServerInfo start========");
        LogUtil.info("battle server count:" + bsClientMap.size());
        for (BaseNettyClient client : bsClientMap.values()) {
            LogUtil.info("battle server addr:" + client.getChannel().remoteAddress() + ",state:" + client.getState());
        }
        LogUtil.info("===========BattleServerInfo end========");
    }

    @Override
    public void onTick() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (updateBattleServerTime <= curTime) {
            updateBattleServerInfo(curTime);
            updateBattleServerTime = curTime + GameConst.UpdateServerTime;
        }

        for (BaseNettyClient nettyClient : bsClientMap.values()) {
            nettyClient.onTick(curTime);
        }

        if (printBattleServerTime <= curTime) {
            printActiveBattleServerInfo();
            printBattleServerTime = curTime + 30000l;
        }
    }

    public Map<String, Integer> getPlayerWatchInfo() {
        return playerWatchInfo;
    }

    public void setPlayerWatchInfo(Map<String, Integer> playerWatchInfo) {
        this.playerWatchInfo = playerWatchInfo;
    }

    public void quitWatch(String playerIdx) {
        Battle.CS_BattleWatchQuit.Builder msg = Battle.CS_BattleWatchQuit.newBuilder();
        quitWatch(playerIdx, msg.build().toByteString());
    }

    public void quitWatch(String playerIdx, ByteString str) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("recv battle frame data but player is null");
            return;
        }
        if (!BattleServerManager.getInstance().getPlayerWatchInfo().containsKey(playerIdx)) {
            return;
        }
        BattleServerManager.getInstance().transferMsgToBattleServerExt(playerIdx, MsgIdEnum.CS_BattleWatchQuit_VALUE, str);
        BattleServerManager.getInstance().getPlayerWatchInfo().remove(playerIdx);
    }

}
