package model.crossarenapvp;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.InvalidProtocolBufferException;

import common.GameConst.RedisKey;
import jdk.nashorn.internal.objects.Global;
import model.room.cache.RoomCache;
import model.warpServer.WarpServerManager;
import protocol.Battle.BattleSubTypeEnum;
import protocol.CrossArenaPvp.CrossArenaPvpPlayer;
import protocol.CrossArenaPvp.CrossArenaPvpRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.MessageId;
import protocol.ServerTransfer;
import protocol.ServerTransfer.BS_GS_CrossArenaPvpInfoOne;
import protocol.ServerTransfer.ServerTypeEnum;
import util.JedisUtil;
import util.LogUtil;

/**
 * @author Hammer
 */
public class CrossArenaPvpRoomManager {

	private static class LazyHolder {
		private static final CrossArenaPvpRoomManager INSTANCE = new CrossArenaPvpRoomManager();
	}

	private Map<String, Long> notice = new ConcurrentHashMap<>();

	private CrossArenaPvpRoomManager() {
	}

	public static CrossArenaPvpRoomManager getInstance() {
		return LazyHolder.INSTANCE;
	}

	public boolean init() {
		return true;
	}

	public void start(String roomId) {
		String roomLock = roomId + RedisKey.CROSSARENAPVP_LOCK;

		JedisUtil jedis = JedisUtil.jedis;
		// 没拿到锁说明房间里有人在改数据,作废
		if (!JedisUtil.lockRedisKey(roomLock, 2000)) {
			return;
		}
		byte[] hget = jedis.hget(RedisKey.CROSSARENAPVP_ROOM.getBytes(), roomId.getBytes());
		if (hget == null) {
			JedisUtil.unlockRedisKey(roomLock);
			return;
		}
		CrossArenaPvpRoom room = null;
		try {
			room = CrossArenaPvpRoom.parseFrom(hget);
		} catch (InvalidProtocolBufferException e) {
			LogUtil.printStackTrace(e);
			return;
		}
		if (room.getOwner().getReady() != 0) {
			JedisUtil.unlockRedisKey(roomLock);
			return;
		}
		if (room.getAtter().getReady() != 0) {
			JedisUtil.unlockRedisKey(roomLock);
			return;
		}

		// TODO fightMakeId
		int fightMakeId = 6;

		ServerTransfer.ApplyPvpBattleData.Builder applyPvpBuilder = ServerTransfer.ApplyPvpBattleData.newBuilder();
		applyPvpBuilder.setFightMakeId(fightMakeId);
		applyPvpBuilder.setSubBattleType(BattleSubTypeEnum.BSTE_CrossArenaPvp);

		//TODO 测试,全自动
		CrossArenaPvpPlayer own = room.getOwner();
		// 兼容代码
		int ownerSvrIndex = own.getSvrIndex();
		if (ownerSvrIndex <= 0) {
			String addr = own.getIp();
			ownerSvrIndex = WarpServerManager.getInstance().getSeverIndexByIp(addr);
		}
		CrossArenaPvpPlayer atter = room.getAtter();
		// 兼容代码
		int attackerSvrIndex = room.getAtter().getSvrIndex();
		if (attackerSvrIndex <= 0) {
			String addr = room.getAtter().getIp();
			attackerSvrIndex = WarpServerManager.getInstance().getSeverIndexByIp(addr);
		}
		ServerTransfer.PvpBattlePlayerInfo.Builder ownBuilder = ServerTransfer.PvpBattlePlayerInfo.newBuilder();

		ownBuilder.setPlayerInfo(own.getPlayerBaseInfo());
		ownBuilder.setFromSvrIndex(ownerSvrIndex);
		ownBuilder.setCamp(2);
		ownBuilder.setIsAuto(false);
//        resultBuilder.setPlayerExtData(playerInfo.getTeamInfo().getPlayerExtData());
		ownBuilder.addAllPetList(own.getBattlePetList());
		ownBuilder.addAllPlayerSkillIdList(own.getSkillList());

		ServerTransfer.PvpBattlePlayerInfo.Builder atterBuilder = ServerTransfer.PvpBattlePlayerInfo.newBuilder();
		atterBuilder.setPlayerInfo(atter.getPlayerBaseInfo());
		atterBuilder.setFromSvrIndex(attackerSvrIndex);
		atterBuilder.setCamp(1);
		atterBuilder.setIsAuto(false);
//        atterBuilderder.setPlayerExtData(playerInfo.getTeamInfo().getPlayerExtData());
		atterBuilder.addAllPetList(atter.getBattlePetList());
		atterBuilder.addAllPlayerSkillIdList(atter.getSkillList());

		applyPvpBuilder.addPlayerInfo(ownBuilder);
		applyPvpBuilder.addPlayerInfo(atterBuilder);

		applyPvpBuilder.addParam(String.valueOf(room.getCostIndex()));
		applyPvpBuilder.addParam(String.valueOf(own.getPlayerBaseInfo().getPower()));
		applyPvpBuilder.addParam(String.valueOf(atter.getPlayerBaseInfo().getPower()));
		applyPvpBuilder.addParam(room.getId());

		ServerTransfer.ReplyPvpBattleData.Builder replyBuilder = RoomCache.getInstance().createRoom(applyPvpBuilder.build(), ServerTransfer.ServerTypeEnum.STE_GameServer, ownerSvrIndex);
		// 房间创建失败
		if (!replyBuilder.getResult()) {
			return;
		}

		replyBuilder.addPlayerList(ownBuilder);
		replyBuilder.addPlayerList(atterBuilder);
		room = room.toBuilder().setBattleId(replyBuilder.getBattleId()+"").setFight(1).build();
		
		ServerTransfer.BS_GS_ReplyPvpBattle.Builder builder = ServerTransfer.BS_GS_ReplyPvpBattle.newBuilder();
		builder.setReplyPvpBattleData(replyBuilder);
		WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, ownerSvrIndex, MessageId.MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);
		if (ownerSvrIndex != attackerSvrIndex) {
			WarpServerManager.getInstance().sendMsgToServer(ServerTransfer.ServerTypeEnum.STE_GameServer_VALUE, attackerSvrIndex, MessageId.MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);
		}
//		JedisUtil.jedis.hdel(RedisKey.CROSSARENAPVP_ROOM.getBytes(), room.getId().getBytes());
		JedisUtil.jedis.hset(RedisKey.CROSSARENAPVP_ROOM.getBytes(), room.getId().getBytes(),room.toByteArray());
		jedis.hdel(RedisKey.CROSSARENAPVP_PLAYERROOM.getBytes(), own.getPlayerBaseInfo().getPlayerId().getBytes());
		jedis.hdel(RedisKey.CROSSARENAPVP_PLAYERROOM.getBytes(), own.getPlayerBaseInfo().getPlayerId().getBytes());
		
	
		BS_GS_CrossArenaPvpInfoOne.Builder b = BS_GS_CrossArenaPvpInfoOne.newBuilder();
		b.setRoom(room);
		b.setType(1);
		WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, ownerSvrIndex, MsgIdEnum.BS_GS_CrossArenaPvpInfoOne_VALUE, b);
		if (ownerSvrIndex != attackerSvrIndex) {
			WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, attackerSvrIndex, MsgIdEnum.BS_GS_CrossArenaPvpInfoOne_VALUE, b);
		}
	}
}
