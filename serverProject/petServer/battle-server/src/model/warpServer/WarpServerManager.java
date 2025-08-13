package model.warpServer;

import com.google.protobuf.GeneratedMessageV3.Builder;
import hyzNet.GameServerTcpChannel;
import protocol.MessageId;
import protocol.ServerTransfer;
import protocol.ServerTransfer.ServerTypeEnum;
import util.LogUtil;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class WarpServerManager {
	private static WarpServerManager instance = null;

	// <ServerIndex,>
	protected Map<Integer, GameServerTcpChannel> gameServerChannelMap;
	// <ServerIndex,>
	protected Map<Integer, GameServerTcpChannel> crossServerChannelMap;

	protected long printServerInfoTime;

	public WarpServerManager() {
		this.gameServerChannelMap = new ConcurrentHashMap<>();
		this.crossServerChannelMap = new ConcurrentHashMap<>();
	}

	public static WarpServerManager getInstance() {
		if (instance == null) {
			instance = new WarpServerManager();
		}
		return instance;
	}

	public boolean init() {
		return true;
	}

	public GameServerTcpChannel getGameServerChannel(int serverIndex) {
		return gameServerChannelMap.get(serverIndex);
	}

	public void addGameServerChannel(int serverIndex, GameServerTcpChannel channel) {
		gameServerChannelMap.put(serverIndex, channel);
	}

	//兼容代码
	public int getSeverIndexByIp(String ip) {
		for (Entry<Integer, GameServerTcpChannel> entry : gameServerChannelMap.entrySet()) {
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

	public void removeGameServerChannel(int serverIndex) {
		gameServerChannelMap.remove(serverIndex);
	}

	public GameServerTcpChannel getCrossServerChannel(int serverIndex) {
		return crossServerChannelMap.get(serverIndex);
	}

	public void addCrossServerChannel(int serverIndex, GameServerTcpChannel channel) {
		crossServerChannelMap.put(serverIndex, channel);
	}

	public void removeCrossServerChannel(int serverIndex) {
		crossServerChannelMap.remove(serverIndex);
	}

	public boolean sendMsgToServer(int serverType, int serverIndex, int msgId, Builder<?> builder) {
		GameServerTcpChannel channel = null;
		if (serverType == ServerTypeEnum.STE_GameServer_VALUE) {
			channel = gameServerChannelMap.get(serverIndex);
		} else if (serverType == ServerTypeEnum.STE_CrossServer_VALUE) {
			channel = crossServerChannelMap.get(serverIndex);
		}
		if (channel != null && channel.channel.isActive()) {
			channel.send(msgId, builder);
			return true;
		}
		LogUtil.warn("send server msg failed, serverIndex=" + serverIndex + ",msgId=" + msgId);
		return false;
	}

	public boolean sendMsgToRandomServer(int serverType, int msgId, Builder<?> builder) {
		Map<Integer, GameServerTcpChannel> map = null;
		if (serverType == ServerTypeEnum.STE_GameServer_VALUE) {
			map = gameServerChannelMap;
		} else if (serverType == ServerTypeEnum.STE_CrossServer_VALUE) {
			map = crossServerChannelMap;
		} else {
			return false;
		}
		for (Entry<Integer, GameServerTcpChannel> ent : map.entrySet()) {
			GameServerTcpChannel channel = ent.getValue();
			if (channel != null && channel.channel.isActive()) {
				channel.send(msgId, builder);
				break;
			}
		}
		return false;
	}

	public boolean sendMsgToAllServer(int serverType, int msgId, Builder<?> builder) {

		Map<Integer, GameServerTcpChannel> map = null;

		if (serverType == ServerTypeEnum.STE_GameServer_VALUE) {
			map = gameServerChannelMap;
		} else if (serverType == ServerTypeEnum.STE_CrossServer_VALUE) {
			map = crossServerChannelMap;
		} else {
			return false;
		}

		for (Entry<Integer, GameServerTcpChannel> ent : map.entrySet()) {
			GameServerTcpChannel channel = ent.getValue();
			if (channel != null && channel.channel.isActive()) {
				channel.send(msgId, builder);
			}
		}
		return false;

	}

	public void sendMsgToGSAll(int msgId, Builder<?> builder) {
		for (GameServerTcpChannel channel : gameServerChannelMap.values()) {
			if (channel != null && channel.channel.isActive()) {
				channel.send(msgId, builder);
			}
		}
	}

	public void sendMsgToGSExcept(int msgId, Builder<?> builder, int serverIndex) {

		for (Entry<Integer, GameServerTcpChannel> ent : gameServerChannelMap.entrySet()) {
			if (ent.getKey() == serverIndex) {
				continue;
			}
			GameServerTcpChannel channel = ent.getValue();
			if (channel != null && channel.channel.isActive()) {
				channel.send(msgId, builder);
			}
		}
	}

	public void printServerInfo(long curTime) {
		if (printServerInfoTime <= curTime) {
			printServerInfoTime = curTime + 30000;

			LogUtil.info("===========GameServerInfo start========");
			LogUtil.info("game server count:" + gameServerChannelMap.size());
			for (GameServerTcpChannel client : gameServerChannelMap.values()) {
				LogUtil.info("game server addr:" + client.channel.remoteAddress());
			}
			LogUtil.info("===========GameServerInfo end========");

			LogUtil.info("===========CrossServerInfo start========");
			LogUtil.info("cross server count:" + crossServerChannelMap.size());
			for (GameServerTcpChannel client : crossServerChannelMap.values()) {
				LogUtil.info("cross server addr:" + client.channel.remoteAddress());
			}
			LogUtil.info("===========CrossServerInfo end========");
		}
	}

	public boolean sendBattleMsgToServer(String plsyerIdx, int serverIndex, int serverType, int msgId, Builder<?> builder) {
		if (serverIndex <= 0) {
			return false;
		}
		ServerTransfer.BS_GS_TransferBattleMsg.Builder builder1 = ServerTransfer.BS_GS_TransferBattleMsg.newBuilder();
		builder1.setPlayerIdx(plsyerIdx);
		builder1.setMsgId(msgId);
		builder1.setMsgData(builder.build().toByteString());
		return WarpServerManager.getInstance().sendMsgToServer(serverType, serverIndex, MessageId.MsgIdEnum.BS_GS_TransferBattleMsg_VALUE, builder1);
	}

	public int getGameServerCount() {
		return gameServerChannelMap.size();
	}
}
