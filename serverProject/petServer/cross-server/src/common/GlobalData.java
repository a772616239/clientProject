package common;

import com.google.protobuf.GeneratedMessageV3.Builder;
import common.load.ServerConfig;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.mistforest.room.cache.MistRoomCache;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_MistForestRoomInfo;
import protocol.ServerTransfer.CS_GS_TheWarTransInfo;
import util.GameUtil;
import util.LogUtil;

public class GlobalData {
    private static GlobalData instance = new GlobalData();
    // <playerIdx, gsTcpChn>
    protected Map<String, GameServerTcpChannel> onlinePlayerMap;
    protected Map<Integer, GameServerTcpChannel> serverChannelMap;

    public GlobalData() {
        this.onlinePlayerMap = new ConcurrentHashMap<>();
        this.serverChannelMap = new ConcurrentHashMap<>();
    }

    public static GlobalData getInstance() {
        return instance;
    }

    public boolean init() {
        return true;
    }

    public GameServerTcpChannel getOnlinePlayerChannel(String id) {
        return StringHelper.isNull(id) ? null : onlinePlayerMap.get(id);
    }

    public void addOnlinePlayer(String idx, GameServerTcpChannel channel) {
        onlinePlayerMap.put(idx, channel);
    }

    public void removeOnlinePlayer(String idx) {
        onlinePlayerMap.remove(idx);
    }

    public boolean isServerFull() {
        return onlinePlayerMap.size() >= ServerConfig.getInstance().getMaxOnlinePlayerNum() - 10;
    }

    public int getOnlinePlayerCount() {
        return onlinePlayerMap.size();
    }

    public GameServerTcpChannel getServerChannel(int serverIndex) {
        return serverChannelMap.get(serverIndex);
    }

    public void addServerChannel(int serverIndex, GameServerTcpChannel channel) {
        serverChannelMap.put(serverIndex, channel);
    }

    public void removeServerChannel(int serverIndex) {
        if (!serverChannelMap.containsKey(serverIndex)) {
            return;
        }
        serverChannelMap.remove(serverIndex);
        MistRoomCache.getInstance().onGameServerClose(serverIndex);
    }

    // 兼容代码
    public int getServerIndexByIp(String ip) {
        for (Entry<Integer, GameServerTcpChannel> entry : serverChannelMap.entrySet()) {
            try {
                String addr = entry.getValue().channel.remoteAddress().toString().substring(1);
                String[] split = addr.split(":");
                if (split == null || split.length < 1) {
                    continue;
                }
                String tmpIp = split[0];
                if (tmpIp.equals(ip)) {
                    return entry.getKey();
                }
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return 0;
    }

    public boolean sendMsgToPlayer(String playerIdx, int msgId, Builder<?> builder) {
        GameServerTcpChannel channel = onlinePlayerMap.get(playerIdx);
        if (channel != null && channel.channel.isActive()) {
            channel.send(msgId, builder);
            return true;
        }
        LogUtil.warn("send msg player is offline,playerId=" + playerIdx + ",msgId=" + msgId);
        return false;
    }

    public boolean sendMsgToServer(int serverIndex, int msgId, Builder<?> builder) {
        if (serverIndex <= 0) {
            LogUtil.warn("send server msg serverIndex is null, msgId=" + msgId);
            return false;
        }
        GameServerTcpChannel channel = serverChannelMap.get(serverIndex);
        if (channel != null && channel.channel.isActive()) {
            channel.send(msgId, builder);
            return true;
        }
        LogUtil.warn("send server msg failed, serverIndex=" + serverIndex + ",msgId=" + msgId);
        return false;
    }

    public boolean sendMistMsgToServer(int serverIndex, int msgId, Set<String> playerSet, Builder<?> builder) {
        if (serverIndex <= 0 || playerSet == null || playerSet.isEmpty()) {
            return false;
        }

        CS_GS_MistForestRoomInfo.Builder builder1 = CS_GS_MistForestRoomInfo.newBuilder();
        builder1.addAllPlayerId(playerSet);
        builder1.setMsgId(msgId);
        builder1.setMsgData(builder.build().toByteString());
        return sendMsgToServer(serverIndex, MsgIdEnum.CS_GS_MistForestRoomInfo_VALUE, builder1);
    }

    public void printGameServerInfo() {
        LogUtil.info("===========GameServerInfo start========");
        LogUtil.info("game server count:" + serverChannelMap.size());
        for (GameServerTcpChannel client : serverChannelMap.values()) {
            LogUtil.info("game server addr:" + client.channel.remoteAddress());
        }
        LogUtil.info("===========GameServerInfo end========");
    }

    public void sendMsgToAllServer(int msgIdValue, Builder<?> builder) {
        if (builder == null) {
            return;
        }

        LogUtil.info("send msgId:" + msgIdValue + ", to all server, curTime:" + GlobalTick.getInstance().getCurrentTime()
                + ", cur channel Map size:" + serverChannelMap.size());
        for (Integer serverIndex : serverChannelMap.keySet()) {
            sendMsgToServer(serverIndex, msgIdValue, builder);
        }
    }

    public boolean sendWarMsgToServer(int serverIndex, int msgId, Set<String> playerSet, Builder<?> builder) {
        if (serverIndex <= 0 || playerSet == null || playerSet.isEmpty()) {
            return false;
        }

        CS_GS_TheWarTransInfo.Builder builder1 = CS_GS_TheWarTransInfo.newBuilder();
        builder1.addAllPlayerIds(playerSet);
        builder1.setMsgId(msgId);
        builder1.setMsgData(builder.build().toByteString());
        return sendMsgToServer(serverIndex, MsgIdEnum.CS_GS_TheWarTransInfo_VALUE, builder1);
    }
}
