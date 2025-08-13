package model.crossarenapvp;

import cfg.CrossArenaPvp;
import cfg.GameConfig;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GameConst.RedisKey;
import common.GlobalData;
import common.JedisUtil;
import static common.JedisUtil.jedis;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import common.tick.Tickable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import model.battle.BattleManager;
import model.battle.BattleUtil;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.crossarena.CrossArenaManager;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.PlayerBaseInfo;
import protocol.Battle.SkillBattleDict;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.CrossArenaPvp.CrossArenaPvpPlayer;
import protocol.CrossArenaPvp.CrossArenaPvpRoom;
import protocol.CrossArenaPvp.SC_CrossArenaOpen;
import protocol.CrossArenaPvp.SC_CrossArenaOpen.Builder;
import protocol.CrossArenaPvp.SC_CrossArenaPvpClose;
import protocol.CrossArenaPvp.SC_CrossArenaPvpInfoOne;
import protocol.CrossArenaPvp.SC_CrossArenaPvpJoin;
import protocol.CrossArenaPvp.SC_CrossArenaPvpKick;
import protocol.CrossArenaPvp.SC_CrossArenaPvpPanel;
import protocol.CrossArenaPvp.SC_CrossArenaPvpPrepare;
import protocol.CrossArenaPvp.SC_CrossArenaPvpReady;
import protocol.CrossArenaPvp.SC_CrossArenaPvpRefresh;
import protocol.CrossArenaPvp.SC_CrossArenaPvpViewFight;
import protocol.CrossArenaPvp.SC_CrossRenaPvpNeedBack;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.Pet;
import protocol.PrepareWar.PositionPetMap;
import protocol.PrepareWar.SkillMap;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_BS_CrossArenaNoticeStart;
import protocol.ServerTransfer.GS_BS_CrossArenaPvpConsumeResult;
import protocol.ServerTransfer.GS_BS_CrossArenaPvpInfoOne;
import protocol.ServerTransfer.GS_BS_CrossArenaPvpNoticeConsume;
import protocol.ServerTransfer.GS_BS_CrossArenaPvpStartbattle;
import server.handler.teams.UpdateTeamHandler;
import util.GameUtil;
import util.LogUtil;

/**
 * @author Hammer
 */
public class CrossArenaPvpManager implements Tickable {

	private final String FLAG = "_caproom";
	private Map<String, Long> readyRoom = new ConcurrentHashMap<>();

	private static class LazyHolder {
		private static final CrossArenaPvpManager INSTANCE = new CrossArenaPvpManager();
	}

	private Map<String, Long> notice = new ConcurrentHashMap<>();

	private Map<String, CrossArenaPvpRoom> roomMap = new HashMap<>();

	private CrossArenaPvpRoom maxLv = null;

	private CrossArenaPvpRoom maxPower = null;
	private long nextFreshTime = 0;

	private CrossArenaPvpManager() {
	}

	public static CrossArenaPvpManager getInstance() {
		return LazyHolder.INSTANCE;
	}

	public boolean init() {
		return GlobalTick.getInstance().addTick(this);
	}

	public void onPlayerLogIn(String playerIdx) {
		outRoom(playerIdx);
	}

	public void outRoom(String playerIdx) {

		String roomId = getMyRoomId(playerIdx);
		if (roomId.equals("")) {
			return;
		}
		String roomLock = createId(roomId, 1);
		if (!JedisUtil.lockRedisKey(roomLock, 2000)) {
			return;
		}
		byte[] roomByte = jedis.hget(RedisKey.CROSSARENAPVP_ROOM.getBytes(), roomId.getBytes());
		if (roomByte == null) {
			JedisUtil.unlockRedisKey(roomLock);
			return;
		}
		CrossArenaPvpRoom room = null;
		try {
			room = CrossArenaPvpRoom.parseFrom(roomByte);
		} catch (InvalidProtocolBufferException e) {
			JedisUtil.unlockRedisKey(roomLock);
			e.printStackTrace();
		}
		if (room.getOwner().getPlayerBaseInfo().getPlayerId().equals(playerIdx)) {
			JedisUtil.unlockRedisKey(roomLock);
			close(playerIdx, roomId);
		} else {
			JedisUtil.unlockRedisKey(roomLock);
			join(playerIdx, roomId, 1, new ArrayList<>(), new ArrayList<>());
		}

	}

	public void getPanel(String playerId) {
		SC_CrossArenaPvpPanel.Builder builder = SC_CrossArenaPvpPanel.newBuilder();
		if (PlayerUtil.queryFunctionLock(playerId, Common.EnumFunction.QIECUO)) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpPanel_VALUE, builder);
			return;
		}

		if (!CrossArenaManager.getInstance().checkOpenDay(playerId, GameConfig.getById(GameConst.CONFIG_ID).getCrossarenapvp_open())) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpPanel_VALUE, builder);
			return;
		}
		Map<String, CrossArenaPvpRoom> roomMap = getRoomMap();
		List<CrossArenaPvpRoom> list = new ArrayList<>(roomMap.values());
		sort(list);
		int needNum = 50;
		int showNum = checkMaxShowNum(needNum, list);
		String myRoomId = getMyRoomId(playerId);
		ShowDataHelper showData = getShowData("", myRoomId, list, showNum);
		builder.addAllRoom(showData.getRooms());
		if (!myRoomId.equals("")) {
			builder.setMyRoom(showData.getMyRoom());
		}
		builder.addAllIds(roomMap.keySet());
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpPanel_VALUE, builder);
		addNotice(playerId);
	}

	public void refresh(String playerId, String roomId) {
		SC_CrossArenaPvpRefresh.Builder builder = SC_CrossArenaPvpRefresh.newBuilder();
		List<CrossArenaPvpRoom> list = new ArrayList<>(getRoomMap().values());
		sort(list);
		ShowDataHelper showData = getShowData("", "", list, 50);
		builder.addAllRoom(showData.getRooms());
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpRefresh_VALUE, builder);
		addNotice(playerId);
	}

	public CrossArenaPvpRoom addOne(String playerId, int costIndex, int maxLevel, List<Integer> blackPet, String roomId, List<PositionPetMap> pets, List<SkillMap> skills) {
		protocol.RetCodeId.RetCode.Builder ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Failure);

		if (pets.size() <= 0) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_PETNUll);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaOpen_VALUE, SC_CrossArenaOpen.newBuilder().setRet(ret));
			return null;
		}
		playerEntity entity = playerCache.getByIdx(playerId);
		if (entity == null) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_PETNUll);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaOpen_VALUE, SC_CrossArenaOpen.newBuilder().setRet(ret));
			return null;
		}
		int playerLv = entity.getLevel();
		playerLv -= 10;
		if (playerLv <= 0) {
			playerLv = 1;
		}
		int minLevel = playerLv;
		// 修改后不需要存储serverindex,暂时不处理
		int serverIndex = BattleServerManager.getInstance().getAvailableBattleServerIndex();
		BaseNettyClient client = BattleServerManager.getInstance().getActiveNettyClient(serverIndex);
		if (client == null) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_BATTLESERVERNUll);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaOpen_VALUE, SC_CrossArenaOpen.newBuilder().setRet(ret));
			return null;
		}
		DefaultKeyValue<RetCodeEnum,CrossArenaPvpPlayer.Builder> createResult = createCrossArenaPvpPlayer(playerId, blackPet, minLevel, maxLevel, pets, skills, false, ret);
		if (RetCodeEnum.RCE_Success != createResult.getKey() || createResult.getValue() == null) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaOpen_VALUE, SC_CrossArenaOpen.newBuilder().setRet(GameUtil.buildRetCode(createResult.getKey())));
			return null;
		}
		CrossArenaPvpPlayer.Builder pb = createResult.getValue();
		String createId = createId(playerId, 0);
		String roomLock = createId(createId, 1);
		CrossArenaPvpRoom.Builder room = createRoom(playerId, createId, pb.build(), costIndex, blackPet, minLevel, maxLevel, serverIndex);

		byte[] hget = jedis.hget(RedisKey.CROSSARENAPVP_ROOM.getBytes(), createId.getBytes());
		if (hget == null) {// 新建房间
			if (!JedisUtil.lockRedisKey(roomLock, 2000)) {
				ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_UPDATE);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaOpen_VALUE, SC_CrossArenaOpen.newBuilder().setRet(ret));
				return null;
			}
			CrossArenaPvpRoom roomData = room.build();
			setRoomInfo(roomData);
			addPlayerRoomInfo(playerId, createId);
			JedisUtil.unlockRedisKey(roomLock);
			sendInfoOne(roomData, "", CrossArenaPvpUpdateType.ADD);// 本服推送了
			noticeOtherServer(roomData, "", CrossArenaPvpUpdateType.ADD);
		} else {
			CrossArenaPvpRoom oldRoom = null;
			try {
				oldRoom = CrossArenaPvpRoom.parseFrom(hget);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}

			if (!JedisUtil.lockRedisKey(roomLock, 2000)) {
				ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_UPDATE);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaOpen_VALUE, SC_CrossArenaOpen.newBuilder().setRet(ret));
				return null;
			}
			if (!oldRoom.getAtter().getPlayerBaseInfo().getPlayerId().equals("")) {
				PlayerBaseInfo atter = oldRoom.getAtter().getPlayerBaseInfo();
				boolean kick = false;
				if (atter.getLevel() < room.getMinLv() || atter.getLevel() > room.getMaxLv()) {
					kick = true;
				} else {
					for (Integer id : oldRoom.getAtter().getBookIdsList()) {
						if (!room.getBlackpetList().contains(id)) {
							continue;
						}
						kick = true;
						break;
					}
				}
				if (!kick) {// 踢掉
					room.setAtter(oldRoom.getAtter());
				} else {
					removePlayerRoomInfo(oldRoom.getAtter().getPlayerBaseInfo().getPlayerId());
				}
			}

			room.setTotalLv(room.getAtter().getPlayerBaseInfo().getLevel() + room.getOwner().getPlayerBaseInfo().getLevel());
			room.setTotalPower(room.getAtter().getPlayerBaseInfo().getPower() + room.getOwner().getPlayerBaseInfo().getPower());
			CrossArenaPvpRoom roomData = room.build();
			setRoomInfo(roomData);
			JedisUtil.unlockRedisKey(roomLock);
			sendInfoOne(roomData, "", CrossArenaPvpUpdateType.UPDATE);// 本服推送了
			noticeOtherServer(roomData, "", CrossArenaPvpUpdateType.UPDATE);
		}
		Builder b = SC_CrossArenaOpen.newBuilder();
		b.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success));
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaOpen_VALUE, b);
		addNotice(playerId);
		return room.build();
	}

	public void join(String playerId, String roomId, int join, List<PositionPetMap> pets, List<SkillMap> skills) {
		RetCode.Builder ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Failure);
		if (PlayerUtil.queryFunctionLock(playerId, Common.EnumFunction.QIECUO)) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_FunctionIsLock);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
		}
		String myRoomId = getMyRoomId(playerId);
		if (!myRoomId.equals("") && !myRoomId.equals(roomId)) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_HAVEROOM);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
			return;
		}

		if (join == 1) {// 退房

			byte[] hget = jedis.hget(RedisKey.CROSSARENAPVP_ROOM.getBytes(), roomId.getBytes());
			if (hget == null) {
				ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_MISS);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
				return;
			}
			CrossArenaPvpRoom room = null;
			try {
				room = CrossArenaPvpRoom.parseFrom(hget);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			String old = room.getAtter().getPlayerBaseInfo().getPlayerId();
			if (!room.getAtter().getPlayerBaseInfo().getPlayerId().equals(playerId)) {
				ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_MISS);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
				return;
			}

			String roomLock = createId(roomId, 1);
			if (!JedisUtil.lockRedisKey(roomLock, 2000)) {
				ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_UPDATE);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
				return;
			}
			CrossArenaPvpRoom roomData = room.toBuilder().clearAtter().clearTotalPower().clearTotalLv().build();
			setRoomInfo(roomData);
			removePlayerRoomInfo(playerId);
			JedisUtil.unlockRedisKey(roomLock);
			noticeRoomPlayer(roomData, CrossArenaPvpUpdateType.UPDATE);
			sendInfoOne(roomData, old, CrossArenaPvpUpdateType.UPDATE);
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
			return;
		}
		if (pets.size() <= 0) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_PETNUll);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
			return;
		}
		playerEntity player = playerCache.getByIdx(playerId);
		if (player == null) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Failure);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
			return;
		}
		teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerId);
		if (entity == null) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_HAVEROOM);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
			return;
		}

		List<Team> teamsByTeamType = entity.getTeamsByTeamType(TeamTypeEnum.TTE_QIECUO);
		if (teamsByTeamType.size() <= 0) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Battle_UsedTeamNotHavePet);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
			return;
		}
		Team team = teamsByTeamType.get(0);
		List<Pet> petByIdList = petCache.getInstance().getPetByIdList(playerId, team.getLinkPetMap().values());
		List<Integer> bookIds = new ArrayList<>();

		for (Pet pet : petByIdList) {
			bookIds.add(pet.getPetBookId());
		}

		String roomLock = createId(roomId, 1);
		if (!JedisUtil.lockRedisKey(roomLock, 2000)) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_UPDATE);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
			return;
		}
		byte[] hget = jedis.hget(RedisKey.CROSSARENAPVP_ROOM.getBytes(), roomId.getBytes());
		if (hget == null) {
			JedisUtil.unlockRedisKey(roomLock);
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_UPDATE);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
			return;
		}
		CrossArenaPvpRoom room = null;
		try {
			room = CrossArenaPvpRoom.parseFrom(hget);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		if (room == null) {
			JedisUtil.unlockRedisKey(roomLock);
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_MISS);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
			return;
		}
		String atterPlayer = room.getAtter().getPlayerBaseInfo().getPlayerId();
		if (!atterPlayer.equals("")) {
			if (!atterPlayer.equals(playerId)) {
				JedisUtil.unlockRedisKey(roomLock);
				ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_LIMIT);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
				return;
			}
		}
		for (Integer boodId : bookIds) {
			if (room.getBlackpetList().contains(boodId)) {
				JedisUtil.unlockRedisKey(roomLock);
				ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_LIMIT);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
				return;
			}
		}
		DefaultKeyValue<RetCodeEnum,CrossArenaPvpPlayer.Builder> createResult = createCrossArenaPvpPlayer(playerId, room.getBlackpetList(), room.getMinLv(), room.getMaxLv(), pets, skills, true, ret);
		if (RetCodeEnum.RCE_Success!=createResult.getKey()||createResult.getValue()==null) {
			JedisUtil.unlockRedisKey(roomLock);
			SC_CrossArenaPvpJoin.Builder msg = SC_CrossArenaPvpJoin.newBuilder();
			if (createResult!=null){
				msg.setRet(GameUtil.buildRetCode(createResult.getKey()));
			}
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, msg);
			return;
		}
		CrossArenaPvpPlayer.Builder pb = createResult.getValue();
		CrossArenaPvpRoom.Builder roomBuilder = room.toBuilder();
		roomBuilder.setTotalLv(room.getAtter().getPlayerBaseInfo().getLevel() + room.getOwner().getPlayerBaseInfo().getLevel());
		roomBuilder.setTotalPower(room.getAtter().getPlayerBaseInfo().getPower() + room.getOwner().getPlayerBaseInfo().getPower());
		room = roomBuilder.setAtter(pb.build()).build();
		setRoomInfo(room);
		addPlayerRoomInfo(playerId, room.getId());
		JedisUtil.unlockRedisKey(roomLock);
		noticeRoomPlayer(room, CrossArenaPvpUpdateType.UPDATE);
		ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success);
		if (ret.getRetCode() != RetCodeEnum.RCE_Success) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
			return;
		}
		ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpJoin_VALUE, SC_CrossArenaPvpJoin.newBuilder().setRet(ret));
		addNotice(playerId);
	}

	public void ready(String playerId, String roomId, int ready) {
		RetCode.Builder ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_MISS);
		String myRoomId = getMyRoomId(playerId);
		if (myRoomId.equals("")) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_MISS);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpReady_VALUE, SC_CrossArenaPvpReady.newBuilder().setRet(ret));
			return;
		}
		String[] split = roomId.split("_");
		if (split.length < 2) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpReady_VALUE, SC_CrossArenaPvpReady.newBuilder().setRet(ret));
			return;
		}
		boolean owner = split[0].equals(playerId);
		String roomLock = createId(roomId, 1);

		if (!JedisUtil.lockRedisKey(roomLock, 2000)) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpReady_VALUE, SC_CrossArenaPvpReady.newBuilder().setRet(ret));
			return;
		}
		byte[] hget = jedis.hget(RedisKey.CROSSARENAPVP_ROOM.getBytes(), roomId.getBytes());
		if (hget != null) {
			CrossArenaPvpRoom room = null;
			try {
				room = CrossArenaPvpRoom.parseFrom(hget);
			} catch (InvalidProtocolBufferException e) {
				LogUtil.printStackTrace(e);
				return;
			}
			if (room.getCostIndex() != -1) {
				int[][] consumeConf = CrossArenaPvp.getById(GameConst.CONFIG_ID).getConsume();
				if (room.getCostIndex() >= consumeConf.length) {
					ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_ErrorParam);
					GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpReady_VALUE, SC_CrossArenaPvpReady.newBuilder().setRet(ret));
					return;
				}
				if (consumeConf[room.getCostIndex()].length < 3) {
					ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_ErrorParam);
					GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpReady_VALUE, SC_CrossArenaPvpReady.newBuilder().setRet(ret));
					return;
				}
				Consume consume = ConsumeUtil.parseConsume(consumeConf[room.getCostIndex()]);
				if (!ConsumeManager.getInstance().materialIsEnough(playerId, consume)) {
					ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_RES_NULL);
					GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpReady_VALUE, SC_CrossArenaPvpReady.newBuilder().setRet(ret));
					return;
				}
			}
			CrossArenaPvpRoom.Builder roomB = room.toBuilder();
			int readyNum = 0;
			if (playerId.equals(roomB.getOwner().getPlayerBaseInfo().getPlayerId())) {
				roomB.getOwnerBuilder().setReady(ready);
			} else {
				roomB.getAtterBuilder().setReady(ready);
			}
			roomB.setStartBattleTime(0);
			if (!roomB.getOwner().getPlayerBaseInfo().getPlayerId().equals("")) {
				readyNum += roomB.getOwnerBuilder().getReady() == 0 ? 1 : 0;
			}
			if (!roomB.getAtter().getPlayerBaseInfo().getPlayerId().equals("")) {
				readyNum += roomB.getAtterBuilder().getReady() == 0 ? 1 : 0;
			}
			if (readyNum >= 2) {
				roomB.setStartBattleTime(GlobalTick.getInstance().getCurrentTime() + (CrossArenaPvp.getById(GameConst.CONFIG_ID).getTime()) * 1000);
				start(roomId, room.getOwner().getIp(), room.getOwner().getSvrIndex(), room.getServerIndex(), roomB.getStartBattleTime());
			}
			room = roomB.build();
			setRoomInfo(room);
			JedisUtil.unlockRedisKey(roomLock);
			noticeRoomPlayer(room, CrossArenaPvpUpdateType.UPDATE);
		}

		ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpReady_VALUE, SC_CrossArenaPvpReady.newBuilder().setRet(ret));
		addNotice(playerId);
	}

	public boolean payConsume(String playerId, int costIndex, boolean check) {
		// 只能在开始战斗时扣除,房主可以随时变换彩金并且随时踢人解散重复准备
		if (costIndex >= CrossArenaPvp.getById(GameConst.CONFIG_ID).getConsume().length) {
			return false;
		}
		if (check) {
			Consume consume = ConsumeUtil.parseConsume(CrossArenaPvp.getById(GameConst.CONFIG_ID).getConsume()[costIndex]);
			if (!ConsumeManager.getInstance().materialIsEnough(playerId, consume)) {
				return false;
			}
		} else {
			Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CrossAreanPvp_PAY);
			Consume consume = ConsumeUtil.parseConsume(CrossArenaPvp.getById(GameConst.CONFIG_ID).getConsume()[costIndex]);
			if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, reason)) {
				return false;
			}
		}
		return true;
	}

	public void start(String roomId, String ownerIp, int ownerSvrIndex, int serverIndex, long battleTime) {
		BaseNettyClient activeNettyClient = BattleServerManager.getInstance().getActiveNettyClient(serverIndex);
		if (activeNettyClient == null) {
			return;
		}
		GS_BS_CrossArenaNoticeStart.Builder builder = GS_BS_CrossArenaNoticeStart.newBuilder();
		builder.setRoomId(roomId);
		builder.setIp(ownerIp); // 兼容代码
		builder.setSvrIndex(ownerSvrIndex);
		builder.setBattleTime(battleTime);
		activeNettyClient.send(MsgIdEnum.GS_BS_CrossArenaNoticeStart_VALUE, builder);
	}

	public void changeReadyFightData(String roomId, boolean add) {
		if (add) {
			readyRoom.put(roomId, GlobalTick.getInstance().getCurrentTime() + 10 * 1000);
		} else {
			readyRoom.remove(roomId);
		}
	}

	/*
	 * 有切磋房间时限制部分操作
	 */
	public boolean isHaveCrossArenaPvpRoomWithMsg(String playerId) {
		if (getMyRoomId(playerId).equals("")) {
			return false;
		}
		SC_CrossRenaPvpNeedBack.Builder builder = SC_CrossRenaPvpNeedBack.newBuilder();
		GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_CrossRenaPvpNeedBack_VALUE, builder);
		return true;
	}

	/*
	 * 有切磋房间时限制部分操作
	 */
	public boolean isHaveCrossArenaPvpRoom(String playerId) {
		return !getMyRoomId(playerId).equals("");
	}

	public void kick(String playerId, String roomId) {
		RetCode.Builder ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_MISS);
		String myRoomId = getMyRoomId(playerId);
		if (myRoomId.equals("")) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_MISS);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpKick_VALUE, SC_CrossArenaPvpKick.newBuilder().setRet(ret));
			return;
		}
		String[] split = roomId.split("_");
		if (split.length < 2) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpKick_VALUE, SC_CrossArenaPvpKick.newBuilder().setRet(ret));
			return;
		}
		boolean owner = split[0].equals(playerId);
		if (!owner) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpKick_VALUE, SC_CrossArenaPvpKick.newBuilder().setRet(ret));
			return;
		}
		String roomLock = createId(roomId, 1);

		if (!JedisUtil.lockRedisKey(roomLock, 2000)) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_UPDATE);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpKick_VALUE, SC_CrossArenaPvpKick.newBuilder().setRet(ret));
			return;
		}
		byte[] hget = jedis.hget(RedisKey.CROSSARENAPVP_ROOM.getBytes(), roomId.getBytes());
		if (hget != null) {
			CrossArenaPvpRoom room = null;
			try {
				room = CrossArenaPvpRoom.parseFrom(hget);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			CrossArenaPvpRoom.Builder roomB = room.toBuilder();
			String old = roomB.getAtter().getPlayerBaseInfo().getPlayerId();
			if (!roomB.getAtter().getPlayerBaseInfo().getPlayerId().equals("")) {
				String atterPlayerId = roomB.getAtter().getPlayerBaseInfo().getPlayerId();
				room = roomB.clearAtter().build();
				noticeRoomPlayer(room, CrossArenaPvpUpdateType.UPDATE);
				setRoomInfo(room);
				sendInfoOne(room, old, CrossArenaPvpUpdateType.UPDATE);
				removePlayerRoomInfo(atterPlayerId);
			}
			JedisUtil.unlockRedisKey(roomLock);
		}
		ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpKick_VALUE, SC_CrossArenaPvpReady.newBuilder().setRet(ret));

	}

	public void close(String playerId, String roomId) {

		RetCode.Builder ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_MISS);
		String myRoomId = getMyRoomId(playerId);
		if (myRoomId.equals("")) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_MISS);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpClose_VALUE, SC_CrossArenaPvpClose.newBuilder().setRet(ret));
			return;
		}
		String[] split = roomId.split("_");
		if (split.length < 2) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpClose_VALUE, SC_CrossArenaPvpClose.newBuilder().setRet(ret));
			return;
		}
		boolean owner = split[0].equals(playerId);
		if (!owner) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpClose_VALUE, SC_CrossArenaPvpClose.newBuilder().setRet(ret));
			return;
		}
		String roomLock = createId(roomId, 1);

		if (!JedisUtil.lockRedisKey(roomLock, 2000)) {
			ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_UPDATE);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpClose_VALUE, SC_CrossArenaPvpClose.newBuilder().setRet(ret));
			return;
		}
		byte[] hget = jedis.hget(RedisKey.CROSSARENAPVP_ROOM.getBytes(), roomId.getBytes());
		if (hget != null) {
			CrossArenaPvpRoom room = null;
			try {
				room = CrossArenaPvpRoom.parseFrom(hget);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			removePlayerRoomInfo(room.getOwner().getPlayerBaseInfo().getPlayerId());
			removePlayerRoomInfo(room.getAtter().getPlayerBaseInfo().getPlayerId());
			jedis.hdel(RedisKey.CROSSARENAPVP_ROOM.getBytes(), roomId.getBytes());
			JedisUtil.unlockRedisKey(roomLock);
//			noticeRoomPlayer(room, CrossArenaPvpUpdateType.DEL);
			sendInfoOne(room, "", CrossArenaPvpUpdateType.DEL);
			noticeOtherServer(room, "", CrossArenaPvpUpdateType.DEL);
		}

		ret = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpClose_VALUE, SC_CrossArenaPvpClose.newBuilder().setRet(ret));
	}

	// 准备到战斗都在房主服务器完成
	public void preStart(String roomId, long time) {
		readyRoom.put(roomId, time);
	}
	public void sendInfoOne(CrossArenaPvpRoom room, String onePlayer, int type) {
		SC_CrossArenaPvpInfoOne.Builder b = SC_CrossArenaPvpInfoOne.newBuilder();
		b.setRoom(room);
		b.setType(type);
		long curTime = GlobalTick.getInstance().getCurrentTime();

		int time = 0;
		if (room.getStartBattleTime() > curTime) {
			time = (int) (room.getStartBattleTime() - curTime);
		}
		SC_CrossArenaPvpPrepare.Builder prepar = SC_CrossArenaPvpPrepare.newBuilder();
		prepar.setTime(time / 1000);
		if (!onePlayer.equals("")) {
			if (!notice.containsKey(onePlayer)) {
				return;
			}
			GlobalData.getInstance().sendMsg(onePlayer, MessageId.MsgIdEnum.SC_CrossArenaPvpInfoOne_VALUE, b);
			if (onePlayer.equals(room.getOwner().getPlayerBaseInfo().getPlayerId()) || onePlayer.equals(room.getAtter().getPlayerBaseInfo().getPlayerId())) {
				GlobalData.getInstance().sendMsg(onePlayer, MessageId.MsgIdEnum.SC_CrossArenaPvpPrepare_VALUE, prepar);
			}
			if (notice.get(onePlayer) < curTime) {
				notice.remove(onePlayer);
			}
			return;
		}
		for (Entry<String, Long> ent : notice.entrySet()) {
			GlobalData.getInstance().sendMsg(ent.getKey(), MessageId.MsgIdEnum.SC_CrossArenaPvpInfoOne_VALUE, b);

			if (ent.getKey().equals(room.getOwner().getPlayerBaseInfo().getPlayerId()) || ent.getKey().equals(room.getAtter().getPlayerBaseInfo().getPlayerId())) {
				GlobalData.getInstance().sendMsg(ent.getKey(), MessageId.MsgIdEnum.SC_CrossArenaPvpPrepare_VALUE, prepar);
			}

			if (ent.getValue() < curTime) {
				notice.remove(ent.getKey());
				continue;
			}
		}
	}

	public void addNotice(String playerId) {
		notice.put(playerId, GlobalTick.getInstance().getCurrentTime() + 60 * 60 * 1000);
	}

	public void noticeOtherServer(CrossArenaPvpRoom room, String onePlyaer, int type) {
		GS_BS_CrossArenaPvpInfoOne.Builder b = GS_BS_CrossArenaPvpInfoOne.newBuilder();
		b.setRoom(room);
		b.setType(type);
		BaseNettyClient client = BattleServerManager.getInstance().getActiveNettyClient(room.getServerIndex());
		if (client != null) {
			client.send(MsgIdEnum.GS_BS_CrossArenaPvpInfoOne_VALUE, b);
		}
	}

	public void noticeRoomPlayer(CrossArenaPvpRoom room, int type) {

		checkNotiveRoom(room, room.getOwner().getPlayerBaseInfo().getPlayerId(), type);
		checkNotiveRoom(room, room.getAtter().getPlayerBaseInfo().getPlayerId(), type);

	}

	public void checkNotiveRoom(CrossArenaPvpRoom room, String playerId, int type) {
		if (playerId.equals("")) {
			return;
		}
		if (playerCache.getByIdx(playerId) != null) {
			sendInfoOne(room, playerId, type);
		} else {
			noticeOtherServer(room, playerId, type);
		}
	}

	private String createId(String id, int type) {
		if (type == 0) {
			return id + FLAG;
		} else {
			return id + RedisKey.CROSSARENAPVP_LOCK;
		}
	}

	private void sendStartToBattleServer(String roomId) {
		BaseNettyClient nettyClient = BattleServerManager.getInstance().getAvailableBattleServer();
		if (nettyClient == null) {
			return;
		}
		String roomLock = createId(roomId, 1);
		if (!JedisUtil.lockRedisKey(roomLock, 2000)) {
			return;
		}
		byte[] roomBytes = jedis.hget(RedisKey.CROSSARENAPVP_ROOM.getBytes(), roomId.getBytes());
		if (roomBytes == null) {
			JedisUtil.unlockRedisKey(roomLock);
			return;
		}
		CrossArenaPvpRoom room = null;
		try {
			room = CrossArenaPvpRoom.parseFrom(roomBytes);
		} catch (InvalidProtocolBufferException e) {
			LogUtil.printStackTrace(e);
			return;
		}
		if (room.getCostIndex() == -1) {
			JedisUtil.unlockRedisKey(roomLock);
			GS_BS_CrossArenaPvpStartbattle.Builder builder = GS_BS_CrossArenaPvpStartbattle.newBuilder();
			builder.setRoomId(roomId);
			nettyClient.send(MsgIdEnum.GS_BS_CrossArenaPvpStartbattle_VALUE, builder);
			return;
		}

		boolean sameServer = false;
		int ownerSvrIndex = room.getOwner().getSvrIndex();
		int attackerSvrIndex = room.getAtter().getSvrIndex();
		if (ownerSvrIndex > 0 && ownerSvrIndex == attackerSvrIndex) {
			sameServer = true;
		}
		if (sameServer) {
			boolean payConsume = payConsume(room.getOwner().getPlayerBaseInfo().getPlayerId(), room.getCostIndex(), true);
			if (payConsume) {
				payConsume = payConsume(room.getAtter().getPlayerBaseInfo().getPlayerId(), room.getCostIndex(), true);
				if (!payConsume) {
					JedisUtil.unlockRedisKey(roomLock);
					return;
				}
			} else {
				JedisUtil.unlockRedisKey(roomLock);
				return;
			}
			payConsume(room.getOwner().getPlayerBaseInfo().getPlayerId(), room.getCostIndex(), false);
			payConsume(room.getAtter().getPlayerBaseInfo().getPlayerId(), room.getCostIndex(), false);
			JedisUtil.unlockRedisKey(roomLock);
			GS_BS_CrossArenaPvpStartbattle.Builder builder = GS_BS_CrossArenaPvpStartbattle.newBuilder();
			builder.setRoomId(roomId);
			nettyClient.send(MsgIdEnum.GS_BS_CrossArenaPvpStartbattle_VALUE, builder);
		} else {
			if (!payConsume(room.getOwner().getPlayerBaseInfo().getPlayerId(), room.getCostIndex(), true)) {
				JedisUtil.unlockRedisKey(roomLock);
				return;
			}
			payConsume(room.getOwner().getPlayerBaseInfo().getPlayerId(), room.getCostIndex(), false);
			JedisUtil.unlockRedisKey(roomLock);
			GS_BS_CrossArenaPvpNoticeConsume.Builder builder = GS_BS_CrossArenaPvpNoticeConsume.newBuilder();
			builder.setRoomId(room.getId());
			builder.setOwnPlayerId(room.getOwner().getPlayerBaseInfo().getPlayerId());
			builder.setAtterPlayerId(room.getAtter().getPlayerBaseInfo().getPlayerId());
			builder.setCostIndex(room.getCostIndex());
			builder.setOwnIp(room.getOwner().getIp());
			builder.setOwnSvrIndex(ownerSvrIndex);
			builder.setAtterIp(room.getAtter().getIp());
			builder.setAtterSvrIndex(attackerSvrIndex);
			nettyClient.send(MsgIdEnum.GS_BS_CrossArenaPvpNoticeConsume_VALUE, builder);
		}
	}

	public void consumeResult(String playerId, int ownerSvrIndex, String roomId, String ownPlayerId, String atterPlayerId, int costId) {
		BaseNettyClient client = BattleServerManager.getInstance().getAvailableBattleServer();
		if (client == null) {
			return;
		}
		GS_BS_CrossArenaPvpConsumeResult.Builder builder = GS_BS_CrossArenaPvpConsumeResult.newBuilder();
		builder.setAtterPlayerId(atterPlayerId);
		builder.setOwnPlayerId(ownPlayerId);
		builder.setCostIndex(costId);
		builder.setRoomId(roomId);
		builder.setOwnSvrIndex(ownerSvrIndex);
		if (payConsume(atterPlayerId, costId, false)) {
			builder.setResult(1);
		}
		client.send(MsgIdEnum.GS_BS_CrossArenaPvpConsumeResult_VALUE, builder);
	}

	public void consumeBack(String playerId, int costId) {
		if (costId >= CrossArenaPvp.getById(GameConst.CONFIG_ID).getConsume().length) {
			return;
		}
		Reward reward = RewardUtil.parseReward(CrossArenaPvp.getById(GameConst.CONFIG_ID).getConsume()[costId]);
		Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CrossAreanPvp_BACK);
		RewardManager.getInstance().doReward(playerId, reward, reason, true);
	}

	@Override
	public void onTick() {
		long curTime = GlobalTick.getInstance().getCurrentTime();
		for (Entry<String, Long> ent : readyRoom.entrySet()) {
			if (ent.getValue() <= curTime) {
				changeReadyFightData(ent.getKey(), false);
				sendStartToBattleServer(ent.getKey());
			}
		}

		long now = GlobalTick.getInstance().getCurrentTime();
		if (nextFreshTime > now) {
			return;
		}
		nextFreshTime = now + GameConst.offerRewardTick;
		Map<String, CrossArenaPvpRoom> map = new HashMap<>();
		Map<byte[], byte[]> hgetAll = jedis.hgetAll(RedisKey.CROSSARENAPVP_ROOM.getBytes());
		if (hgetAll == null) {
			return;
		}
		for (Entry<byte[], byte[]> ent : hgetAll.entrySet()) {
			CrossArenaPvpRoom room;
			try {
				room = CrossArenaPvpRoom.parseFrom(ent.getValue());
				map.put(room.getId(), room);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}

		this.roomMap = map;
	}

	private void sort(List<CrossArenaPvpRoom> rooms) {
		rooms.sort(new Comparator<CrossArenaPvpRoom>() {
			@Override
			public int compare(CrossArenaPvpRoom o1, CrossArenaPvpRoom o2) {
				if (o1.getCreteTime() > o2.getCreteTime()) {
					return 1;
				} else if (o1.getCreteTime() == o2.getCreteTime()) {
					return 0;
				} else {
					return -1;
				}
			}
		});
	}

	private ShowDataHelper getShowData(String firstRoomId, String myRoomId, List<CrossArenaPvpRoom> rooms, int needNum) {
		boolean first = firstRoomId.equals("");
		boolean find = false;
		boolean findMyRoom = !myRoomId.equals("");
		boolean alreadyFindMyRoom = false;
		CrossArenaPvpRoom myRoom = null;
		List<CrossArenaPvpRoom> showList = new ArrayList<>();
		for (int i = 0; i < rooms.size(); i++) {
			CrossArenaPvpRoom room = rooms.get(i);
			if (first) {
				showList.add(room);
				needNum--;
			} else {
				if (find) {
					showList.add(room);
					needNum--;
				} else {
					if (room.getId().equals(firstRoomId)) {
						find = true;
					}
					showList.add(room);
					needNum--;
				}
			}
			if (findMyRoom) {
				if (!alreadyFindMyRoom) {
					if (room.getId().equals(myRoomId)) {
						myRoom = room;
						alreadyFindMyRoom = true;
					}
				}
			}
			if (needNum <= 0) {
				if (!findMyRoom) {
					break;
				} else {
					if (alreadyFindMyRoom) {
						break;
					}
				}
			}
		}
		ShowDataHelper data = new ShowDataHelper(showList, myRoom);
		return data;
	}

	private class ShowDataHelper {

		private List<CrossArenaPvpRoom> rooms = null;
		private CrossArenaPvpRoom myRoom = null;

		public ShowDataHelper(List<CrossArenaPvpRoom> rooms, CrossArenaPvpRoom myRoom) {
			if (rooms == null) {
				this.rooms = new ArrayList<>();
			} else {
				this.rooms = rooms;
			}
			if (myRoom == null) {
				this.myRoom = CrossArenaPvpRoom.newBuilder().build();
			} else {
				this.myRoom = myRoom;
			}
		}

		public List<CrossArenaPvpRoom> getRooms() {
			return rooms;
		}

		public CrossArenaPvpRoom getMyRoom() {
			return myRoom;
		}
	}

	/**
	 * 检查队伍并完善队伍数据结构
	 */
	private RetCode updateTeamCheck(String playerId, List<Integer> blackPetList, CrossArenaPvpPlayer.Builder pb, List<PositionPetMap> pets, List<SkillMap> skills) {
		RetCode.Builder builder = RetCode.newBuilder();

		teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerId);
		if (entity == null) {
			return builder.setRetCode(RetCodeEnum.RCE_Failure).build();
		}
		List<Team> teamsByTeamType = entity.getTeamsByTeamType(TeamTypeEnum.TTE_QIECUO);
		if (teamsByTeamType.size() <= 0) {
			return builder.setRetCode(RetCodeEnum.RCE_Failure).build();
		}
		// 需要使用该方法,考虑提出来
		RetCodeEnum retCodeEnum = new UpdateTeamHandler().checkPetAndSkill(entity, TeamNumEnum.TNE_QIECUO, pets, skills);
		if (retCodeEnum != RetCodeEnum.RCE_Success) {
			return builder.setRetCode(RetCodeEnum.RCE_Failure).build();
		}

		Team team = new Team();
		team.setTeamNum(TeamNumEnum.TNE_QIECUO);
		for (PositionPetMap positionPetMap : pets) {
			team.putLinkPet(positionPetMap.getPositionValue(), positionPetMap.getPetIdx());
		}
		for (SkillMap skillMap : skills) {
			team.putLinkSkill(skillMap.getSkillPositionValue(), skillMap.getSkillCfgId());
		}
		List<Pet> petByIdList = petCache.getInstance().getPetByIdList(playerId, team.getLinkPetMap().values());
		List<Integer> bookIds = new ArrayList<>();

		for (Pet pet : petByIdList) {
			bookIds.add(pet.getPetBookId());
		}

		for (Integer boodId : bookIds) {
			if (blackPetList.contains(boodId)) {
				return builder.setRetCode(RetCodeEnum.RCE_Failure).build();
			}
		}
		pb.addAllBookIds(bookIds);
		pb.addAllPet(petByIdList);
		pb.setTeamInfo(entity.buildTeamInfo(team));
		return builder.setRetCode(RetCodeEnum.RCE_Success).build();
	}

	private Map<String, CrossArenaPvpRoom> getRoomMap() {
		return roomMap;
	}

	private int checkMaxShowNum(int showNum, List<? extends Object> list) {
		if (showNum > list.size()) {
			return list.size();
		} else {
			return showNum;
		}
	}

	private DefaultKeyValue<RetCodeEnum,CrossArenaPvpPlayer.Builder> createCrossArenaPvpPlayer(String playerId, List<Integer> blackPet, int minLv, int maxLv, List<PositionPetMap> pets, List<SkillMap> skils, boolean join, RetCode.Builder ret) {
		DefaultKeyValue<RetCodeEnum,CrossArenaPvpPlayer.Builder> result= new DefaultKeyValue<>();
		playerEntity player = playerCache.getByIdx(playerId);
		if (player == null) {
			result.setKey(RetCodeEnum.RCE_Arena_PlayerIsNotExist);
			return result;
		}
		CrossArenaPvpPlayer.Builder pb = CrossArenaPvpPlayer.newBuilder();

		pb.setPlayerBaseInfo(BattleUtil.buildPlayerBattleBaseInfo(playerId));

		List<String> petIds = new ArrayList<>();
		for (PositionPetMap e : pets) {
			petIds.add(e.getPetIdx());
		}
		long calculateTeamAbility = petCache.getInstance().calculateTeamAbility(playerId, petIds);
		pb.getPlayerBaseInfoBuilder().setPower(calculateTeamAbility);
		pb.setSvrIndex(ServerConfig.getInstance().getServer());
		pb.setReady(1);
		if (CrossArenaPvp.getById(GameConst.CONFIG_ID).getPowerlimit().length < 2) {
			result.setKey(RetCodeEnum.RCE_ConfigError);
			return result;
		}
		double confMinPower = CrossArenaPvp.getById(GameConst.CONFIG_ID).getPowerlimit()[0];
		double confMaxPower = CrossArenaPvp.getById(GameConst.CONFIG_ID).getPowerlimit()[1];
		double temPower = calculateTeamAbility;
		double temMinPower = temPower * confMinPower / 100d;
		double temMaxPower = temPower * confMaxPower / 100d;

		if (join) {
//			if (temPower < minPower) {
//				return null;
//			}
//			if (temPower > maxPower) {
//				return null;
//			}
		} else {
			// TODO 前后端战力算不一致

//			if (minPower < Math.floor(temMinPower)) {
//				return null;
//			}
//			if (maxPower > Math.floor(temMaxPower)) {
//				return null;
//			}
		}
		if (CrossArenaPvp.getById(GameConst.CONFIG_ID).getLevellimit().length < 2) {
			result.setKey(RetCodeEnum.RCE_ConfigError);
			return result;
		}
		double confMinLv = CrossArenaPvp.getById(GameConst.CONFIG_ID).getLevellimit()[0];
		double confMaxLv = CrossArenaPvp.getById(GameConst.CONFIG_ID).getLevellimit()[1];

		double temLv = pb.getPlayerBaseInfo().getLevel();
		double temMinLv = temLv * confMinLv / 100d;
		double temMaxLv = temLv * confMaxLv / 100d;

		if (join) {
			if (temLv < minLv) {
				result.setKey(RetCodeEnum.RCE_CROSSARERNAPVP_LEVEL);
				return result;
			}
			if (temLv > maxLv) {
				result.setKey(RetCodeEnum.RCE_CROSSARERNAPVP_LEVEL);
				return result;
			}
		} else {
			if (player.getLevel() > maxLv) {
				result.setKey(RetCodeEnum.RCE_CROSSARERNAPVP_LEVEL);
				return result;
			}
			// TODO 前后端战力算不一致
//			if (minLv < Math.floor(temMinLv)) {
//				ret.setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_LEVEL);
//				return null;
//			}
//			if (maxLv > Math.floor(temMaxLv)) {
//				ret.setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_LEVEL);
//				return null;
//			}
//			if (temLv < temMinLv || temLv > temMaxLv) {
//				ret.setRetCode(RetCodeEnum.RCE_CROSSARERNAPVP_LEVEL);
//				return null;
//			}
		}
		RetCodeEnum teamCheck = updateTeamCheck(playerId, blackPet, pb, pets, skils).getRetCode();
		if (teamCheck != RetCodeEnum.RCE_Success) {
			result.setKey(teamCheck);
			return result;
		}
		List<BattlePetData> petDatas = petCache.getInstance().getPetBattleData(playerId, petIds, BattleSubTypeEnum.BSTE_CrossArenaPvp);
		if (petDatas != null) {
			pb.addAllBattlePet(petDatas);
		}

//		List<Integer> skillList = teamCache.getInstance().getPlayerTeamSkillList(playerId, TeamNumEnum.TNE_QIECUO);

		for (SkillMap e : skils) {
			pb.addSkill(SkillBattleDict.newBuilder().setSkillId(e.getSkillCfgId()).setSkillLv(player.getSkillLv(e.getSkillCfgId())).build());
		}
//		if (skillList != null) {
//			for (Integer skillId : skillList) {
//				pb.addSkill(SkillBattleDict.newBuilder().setSkillId(skillId).setSkillLv(player.getSkillLv(skillId)).build());
//			}
//		}
		result.setKey(RetCodeEnum.RCE_Success);
		result.setValue(pb);
		return result;
	}

	private CrossArenaPvpRoom.Builder createRoom(String playerId, String createId, CrossArenaPvpPlayer playerPb, int costIndex, List<Integer> blackPet, int minLv, int maxLv, int serverIndex) {
		CrossArenaPvpRoom.Builder room = CrossArenaPvpRoom.newBuilder();
		room.clear();
		room.setId(createId);
		room.setCostIndex(costIndex);
		room.addAllBlackpet(blackPet);
		room.setMaxLv(maxLv);
		room.setMinLv(minLv);
		room.setServerIndex(serverIndex);
		room.setOwner(playerPb);
		return room;
	}

	private void setRoomInfo(CrossArenaPvpRoom room) {
		jedis.hset(RedisKey.CROSSARENAPVP_ROOM.getBytes(), room.getId().getBytes(), room.toByteArray());
	}

	private void addPlayerRoomInfo(String playerId, String roomId) {
		if (playerId.equals("")) {
			return;
		}
		jedis.hset(RedisKey.CROSSARENAPVP_PLAYERROOM.getBytes(), playerId.getBytes(), roomId.getBytes());
	}

	private void removePlayerRoomInfo(String playerId) {
		if (playerId.equals("")) {
			return;
		}
		jedis.hdel(RedisKey.CROSSARENAPVP_PLAYERROOM.getBytes(), playerId.getBytes());
	}

	private String getMyRoomId(String playerId) {

		String id = jedis.hget(RedisKey.CROSSARENAPVP_PLAYERROOM, playerId);
		if (id == null) {
			return "";
		}
		return id;
	}

	public void reqViewFight(String playerId, String roomId) {
		SC_CrossArenaPvpViewFight.Builder builder = SC_CrossArenaPvpViewFight.newBuilder();
		builder.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Failure));

		byte[] hget = jedis.hget(RedisKey.CROSSARENAPVP_ROOM.getBytes(), roomId.getBytes());
		if (hget == null) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpViewFight_VALUE, builder);
			return;
		}
		CrossArenaPvpRoom room = null;
		try {
			room = CrossArenaPvpRoom.parseFrom(hget);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		BaseNettyClient activeNettyClient = BattleServerManager.getInstance().getActiveNettyClient(room.getServerIndex());
		if (activeNettyClient == null) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpViewFight_VALUE, builder);
			return;
		}
		BattleManager.getInstance().sendBattleServerBattleWatch(activeNettyClient, room.getBattleId(), playerId);

		builder.setRet(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success));
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_CrossArenaPvpViewFight_VALUE, builder);
	}
}
