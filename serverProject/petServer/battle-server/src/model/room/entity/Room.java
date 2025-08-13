package model.room.entity;

import cfg.GameConfig;
import common.GameConst;
import common.GameConst.EventType;
import common.GlobalTick;
import common.TimeUtil;
import common.load.ServerConfig;
import datatool.StringHelper;
import model.crossarena.CrossArenaManager;
import model.crossarena.CrossArenaTopManager;
import model.http.HttpRequestUtil;
import model.obj.BaseObj;
import model.player.entity.Player;
import model.room.RoomConst.RoomStateEnum;
import model.room.RoomConst.RoomStateTime;
import model.warpServer.WarpServerManager;
import protocol.Battle;
import protocol.Battle.*;
import protocol.BattleCMD;
import protocol.BattleMono;
import protocol.BattleMono.CS_FrameData;
import protocol.BattleMono.SC_FrameData;
import protocol.Chat.SC_BattleChatData;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer;
import protocol.ServerTransfer.*;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;

import java.util.*;
import java.util.stream.Collectors;

public class Room extends BaseObj {
	private String idx;
	private int fromServerIndex;
	private ServerTypeEnum fromServerType;
	private int fightMakeId;
	private int roomState;
	private long createTime;
	private long battleStartTime;
	private long roomStateUpdateTime;
	private int subBattleType;
	private List<String> cacheParams; // 缓存参数,如矿区id

	private String crossArenaParam;
	private SC_EnterFight.Builder enterFightData;
	private long randSeed;
	private List<Player> memberList;

	private int frameIndex;
	private List<SC_FrameData.Builder> frameData;

	private long firstEndTime; // 辅助判断战斗结果
	private volatile int submitRetCount;
	private CS_BattleResult.Builder battleResult;

	private Map<String, Integer> watchPlayerMap = new HashMap<>(); // 缓存本战场的观战玩家信息

	private int robotNum = 0;

	public Room() {
		this.idx = "";
		this.fromServerIndex = 0;
		this.fromServerType = ServerTypeEnum.STE_Null;
		this.createTime = GlobalTick.getInstance().getCurrentTime();
		this.frameData = new ArrayList<>();
		this.enterFightData = SC_EnterFight.newBuilder();
		this.battleResult = CS_BattleResult.newBuilder();
		this.memberList = new ArrayList<>();
	}

	public void clear() {
		this.fromServerIndex = 0;
		this.fromServerType = ServerTypeEnum.STE_Null;
		this.fightMakeId = 0;
		this.subBattleType = 0;
		this.createTime = 0;
//        this.winnerCamp = 0;
		this.submitRetCount = 0;
		this.roomState = RoomStateEnum.closed;
		this.battleStartTime = 0;
		this.roomStateUpdateTime = 0;
		this.randSeed = 0;
		this.frameIndex = 0;
		this.firstEndTime = 0;
		this.enterFightData.clear();
		this.memberList.clear();
		this.frameData.clear();
		this.battleResult.clear();
		this.watchPlayerMap.clear();
		if (cacheParams != null) {
			cacheParams.clear();
		}
	}

	@Override
	public String getIdx() {
		return idx;
	}

	@Override
	public void setIdx(String idx) {
		this.idx = idx;
	}

	@Override
	public String getClassType() {
		return "Room";
	}

	public int getFightMakeId() {
		return fightMakeId;
	}

	public void setFightMakeId(int fightMakeId) {
		this.fightMakeId = fightMakeId;
	}

	public long getRandSeed() {
		return randSeed;
	}

	public int getRoomState() {
		return roomState;
	}

	public void setRoomState(int roomState) {
		this.roomState = roomState;
	}

	public int getSubBattleType() {
		return subBattleType;
	}

	public void setSubBattleType(int subBattleType) {
		this.subBattleType = subBattleType;
	}

	public CS_BattleResult.Builder getBattleResult() {
		return battleResult;
	}

	public void mergeBattleResult(CS_BattleResult battleResult) {
		if (battleResult != null) {
			this.battleResult.mergeFrom(battleResult);
		}
	}

	public SC_EnterFight.Builder getEnterFightData() {
		return enterFightData;
	}

	public void setEnterFightData(SC_EnterFight.Builder enterFightData) {
		this.enterFightData = enterFightData;
	}

	public long getFirstEndTime() {
		return firstEndTime;
	}

	public void setFirstEndTime(long firstEndTime) {
		this.firstEndTime = firstEndTime;
	}

	public void addCacheParams(List<String> params) {
		if (cacheParams == null) {
			cacheParams = new ArrayList<>();
		}
		cacheParams.addAll(params);
	}

	public List<String> getCacheParams() {
		return cacheParams;
	}

	public void addMember(Player player) {
		if (memberList == null) {
			return;
		}
		memberList.add(player);
		LogUtil.info("battle room add member id:" + player.getIdx() + ",name:" + player.getIdx() + ",fromServerIndex:" + player.getFromServerIndex());
	}

	public boolean initRoom(ServerTypeEnum serverType, int serverIndex, ApplyPvpBattleData applyInfo) {
		if (applyInfo.getPlayerInfoList() == null || applyInfo.getPlayerInfoList().isEmpty()) {
			return false;
		}
		this.randSeed = new Random().nextLong();
		this.fromServerIndex = serverIndex;
		this.fromServerType = serverType;
		this.fightMakeId = applyInfo.getFightMakeId();
		roomState = RoomStateEnum.init;
		roomStateUpdateTime = GlobalTick.getInstance().getCurrentTime();
		setSubBattleType(applyInfo.getSubBattleTypeValue());
		if (applyInfo.getParamCount() > 0) {
			addCacheParams(applyInfo.getParamList());
			crossArenaParam = applyInfo.getParam(0);
		}
		return true;
	}

	public SC_BattleRevertData.Builder getRevertData(String playerIdx) {
		SC_BattleRevertData.Builder builder = SC_BattleRevertData.newBuilder();
		SC_EnterFight.Builder enterFight = getEnterFightData();
		for (Player member : memberList) {
			if (member.getIdx().equals(playerIdx)) {
				enterFight.setCamp(member.getCamp());
				break;
			}
		}
		builder.setEnterFightData(enterFight);
		builder.setFrameIndex(frameIndex);
		for (SC_FrameData.Builder keyFrame : frameData) {
			builder.addFrameData(keyFrame);
		}
		return builder;
	}

	public void playerOnlineChange(String playerIdx, boolean isLogin) {
		BattleMono.CS_FrameData.Builder off = BattleMono.CS_FrameData.newBuilder();
		BattleMono.BattleOperation.Builder offOper = BattleMono.BattleOperation.newBuilder();
		boolean isChange = false;
		for (Player member : memberList) {
			if (member.getIdx().equals(playerIdx)) {
				offOper.setCamp(member.getCamp());
				isChange = true;
				break;
			}
		}
		if (isChange) {
			return;
		}
		offOper.setFramType(BattleMono.BattleFrameTypeEnum.BFTE_OfflineStateChange);
		BattleCMD.BattleFrameParam_OfflineStateChange.Builder msg1 = BattleCMD.BattleFrameParam_OfflineStateChange.newBuilder();
		msg1.setIsOfflline(!isLogin);// true:掉线 false:上线
		offOper.setFramParam(msg1.build().toByteString());
		addBattleOperation(off.build());
	}

	public void addBattleOperation(CS_FrameData operation) {
		SC_FrameData.Builder builder = getCurrentBattleOperation();
		if (builder == null) {
			builder = SC_FrameData.newBuilder();
			builder.setFrameIndex(frameIndex);
			frameData.add(builder);
		}
		builder.addOperation(operation.getOperation());
	}

	private SC_FrameData.Builder getCurrentBattleOperation() {
		try {
			if (frameData == null || frameData.isEmpty()) {
				return null;
			}
			SC_FrameData.Builder battleFrameData = frameData.get(frameData.size() - 1);
			if (battleFrameData == null) {
				return null;
			}
			return battleFrameData.getFrameIndex() == frameIndex ? battleFrameData : null;
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
			return null;
		}
	}

	private boolean checkAllReady() {
		boolean allReady = true;
		for (Player member : memberList) {
			if (!member.isReadyBattle()) {
				allReady = false;
				break;
			}
		}
		return allReady;
	}

	public void broadcastFrameData() {
		SC_FrameData.Builder builder = getCurrentBattleOperation();
		if (builder == null) {
			builder = SC_FrameData.newBuilder();
			builder.setFrameIndex(frameIndex);
		}
		for (Player player : memberList) {
			player.sendBattleMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, MsgIdEnum.SC_FrameData_VALUE, builder);
		}
		for (Map.Entry<String, Integer> ent : watchPlayerMap.entrySet()) {
			WarpServerManager.getInstance().sendBattleMsgToServer(ent.getKey(), ent.getValue(), ServerTypeEnum.STE_GameServer_VALUE, MsgIdEnum.SC_FrameData_VALUE, builder);
		}
	}

	public void addSubmitResultCount(Player player) {
		if (player == null) {
			return;
		}
		boolean isMember = false;
		for (int i = 0; i < memberList.size(); i++) {
			Player member = memberList.get(i);
			if (member.getIdx().equals(player.getIdx())) {
				isMember = true;
				break;
			}
		}
		if (!isMember) {
			return;
		}
		if (player.getBattleResult().getWinnerCamp() == 3) {
			submitRetCount = memberList.size(); // 投降则直接结算
			firstEndTime = GlobalTick.getInstance().getCurrentTime();
		} else if (player.getBattleResult().getWinnerCamp() != 0) {
			++submitRetCount;
			firstEndTime = GlobalTick.getInstance().getCurrentTime();
			if (robotNum > 0) {
				submitRetCount += robotNum;
				for (Player membe : memberList) {
					if (membe.isRobot()) {
						membe.mergeBattleResult(player.getBattleResult().build());
					}
				}
			}
		}
	}

	public boolean checkGiveUp() {
		for (Player member : memberList) {
			if (member.getBattleResult().getWinnerCamp() == 3) {
				if (member.getCamp() == 1) {
					member.getBattleResult().setWinnerCamp(2);
					mergeBattleResult(member.getBattleResult().build());
				} else {
					member.getBattleResult().setWinnerCamp(1);
					mergeBattleResult(member.getBattleResult().build());
				}
				return true;
			}
		}
		return false;
	}

	public void calcWinnerCamp() {
		// -1为平局,0为待定,1、2分别为玩家阵营,3为投降
		Player player1 = memberList.get(0);
		Player player2 = memberList.get(1);
		if (player1 == null || player2 == null) {
			getBattleResult().setWinnerCamp(-1);
			return;
		}
		if (checkGiveUp()) {
			return;
		}
		if (player1.getBattleResult().getWinnerCamp() != 0 && player1.getBattleResult().build().toByteString().equals(player2.getBattleResult().build().toByteString())) {
			mergeBattleResult(player1.getBattleResult().build());
		} else {
			Event event = Event.valueOf(GameConst.EventType.ET_BattleCheck, GameUtil.getDefaultEventSource(), this);
			event.pushParam(false);
			EventManager.getInstance().dispatchEvent(event);
		}
	}

	protected BattleCheckParam generateBattleCheckParam() {
		BattleCheckParam.Builder builder = BattleCheckParam.newBuilder();
		builder.setEnterFightData(enterFightData);
		for (SC_FrameData.Builder keyFrame : frameData) {
			builder.addFrameData(keyFrame);
		}
		return builder.build();
	}

	public void checkBattle(boolean preCheck) {
		CS_BattleResult result = null;
		if (ServerConfig.getInstance().isOpenBattleCheck()) {
			result = HttpRequestUtil.checkBattle(generateBattleCheckParam());
		} else {
			for (Player player : memberList) {
				if (player.getBattleResult().getWinnerCamp() != 0) {
					result = player.getBattleResult().build();
					break;
				}
			}
		}
		if (result != null) {
			LogUtil.warn("Room[" + getIdx() + "] checkBattle,winnerCamp=" + result.getWinnerCamp() + ",preCheck=" + preCheck + ",SvrFrameIndex=" + frameIndex + ",resultFrameIndex=" + result.getEndFrame());
			if (frameIndex > result.getEndFrame() || !ServerConfig.getInstance().isOpenBattleCheck()) {
				mergeBattleResult(result);
			}
		}
		if (getBattleResult().getWinnerCamp() == 0) {
			if (!preCheck) {
				getBattleResult().setWinnerCamp(-1);
			} else {
				setFirstEndTime(0); // 仅收到一个玩家的结束消息且验证失败,则继续战斗PlayerInfo
			}
		}
	}

	public void broadcastBattleChatInfo(String playerIdx, SC_BattleChatData.Builder builder) {
		for (Player player : memberList) {
			if (player.getIdx().equals(playerIdx)) {
				continue;
			}
			player.sendBattleMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, MsgIdEnum.SC_BattleChatData_VALUE, builder);
		}
	}

	public void onTick(long curTime) {
		switch (roomState) {
		case RoomStateEnum.init:
			if (checkAllReady() || roomStateUpdateTime + GameConfig.getById(GameConst.ConfgId).getFightwaittimeout() * TimeUtil.MS_IN_A_S <= curTime) {
				onBattleStart();
				setRoomState(RoomStateEnum.battling);
				battleStartTime = curTime;
				roomStateUpdateTime = curTime;
			}
			break;
		case RoomStateEnum.battling:
			if (battleStartTime + RoomStateTime.battleTime <= curTime) {
				calcWinnerCamp();
				setRoomState(RoomStateEnum.verifyResult);
				roomStateUpdateTime = curTime;
			} else {
				if (getBattleResult().getWinnerCamp() != 0) {
					setRoomState(RoomStateEnum.verifyResult);
					roomStateUpdateTime = curTime;
				} else if (submitRetCount < memberList.size()) {
					if (submitRetCount > 0 && firstEndTime > 0 && firstEndTime + 5000l <= curTime) {
						Event event = Event.valueOf(EventType.ET_BattleCheck, GameUtil.getDefaultEventSource(), this);
						event.pushParam(true);
						EventManager.getInstance().dispatchEvent(event);
					}
					broadcastFrameData();
					++frameIndex;
				} else if (submitRetCount >= memberList.size()) {
					if (submitRetCount == memberList.size()) {
						calcWinnerCamp();
					}
					setRoomState(RoomStateEnum.verifyResult);
					roomStateUpdateTime = curTime;
				}
			}
			break;
		case RoomStateEnum.verifyResult:
			if (getBattleResult().getWinnerCamp() != 0 || roomStateUpdateTime + RoomStateTime.verifyResultTime <= curTime) {
				if (getBattleResult().getWinnerCamp() == 0) {
					getBattleResult().setWinnerCamp(-1);
				}
				onBattleSettlement();
				setRoomState(RoomStateEnum.battleEnd);
				roomStateUpdateTime = curTime;
			}
			break;
		case RoomStateEnum.battleEnd:
			if (roomStateUpdateTime + RoomStateTime.endBattleTime <= curTime) {
				clear();
			}
			break;
		default:
			break;
		}
	}

	public void onBattleStart() {
		SC_BattleStart.Builder builder = SC_BattleStart.newBuilder();
		builder.setBattleId(GameUtil.stringToLong(getIdx(), 0));
		for (Player member : memberList) {
			member.sendBattleMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, MsgIdEnum.SC_BattleStart_VALUE, builder);
		}

		getBattleResult().setBattleId(enterFightData.getBattleId());
	}

	public void onBattleSettlement() {
		try {
			long roomId = GameUtil.stringToLong(getIdx(), 0);
			PvpBattleResultData.Builder resultBuilder = PvpBattleResultData.newBuilder();
			resultBuilder.setBattleId(roomId);
			resultBuilder.setSubBattleTypeValue(getSubBattleType());
//        resultBuilder.setWinnerCamp(getBattleResult().getWinnerCamp());
			resultBuilder.addAllRemainPetData(getBattleResult().getRemainPetList());
			resultBuilder.setBattleResult(getBattleResult());
			List<SC_FrameData> frameColl = this.frameData.stream().map(SC_FrameData.Builder::build).collect(Collectors.toList());
			resultBuilder.addAllFrameData(frameColl);
			for (Player player : memberList) {
				PvpBattlePlayerInfo.Builder playerInfo = PvpBattlePlayerInfo.newBuilder();
				playerInfo.setPlayerInfo(player.builderPlayerBaseInfo());
				playerInfo.setCamp(player.getCamp());
				playerInfo.setFromSvrIndex(player.getFromServerIndex());
				resultBuilder.addPlayerList(playerInfo);

				if (!player.isRobot()) {
					Event event = Event.valueOf(EventType.ET_Logout, this, player);
					EventManager.getInstance().dispatchEvent(event);
				}
			}
			if (getCacheParams() != null) {
				resultBuilder.addAllParams(getCacheParams());
			}

			if (fromServerType == ServerTypeEnum.STE_GameServer) {
				BS_GS_BattleResult.Builder result = BS_GS_BattleResult.newBuilder();
				result.setPvpBattleResultData(resultBuilder);

				if (this.subBattleType == BattleSubTypeEnum.BSTE_MatchArena_VALUE || this.subBattleType == BattleSubTypeEnum.BSTE_ArenaMatchNormal_VALUE || this.subBattleType == BattleSubTypeEnum.BSTE_MatchArenaRanking_VALUE) {
					settleMatchArenaResult(result);
				} else if (this.subBattleType == BattleSubTypeEnum.BSTE_MatchArenaLeitai_VALUE) {
					int defNum = CrossArenaManager.getInstance().settleMatchWinCot(Long.valueOf(idx), getBattleResult().getWinnerCamp());
					BattleResultExt.Builder extmsg = BattleResultExt.newBuilder();
					extmsg.setKey(BattleResultExtType.CROSSARENA_LS);
					extmsg.setParm("" + defNum);
					battleResult.addExt(extmsg);
					resultBuilder.setBattleResult(getBattleResult());
					result.setPvpBattleResultData(resultBuilder);
					settleMatchArenaResult(result);
					// 竞技场擂台赛战斗结束返回管理器处理业务逻辑
					CrossArenaManager.getInstance().settleMatchTable(Long.valueOf(idx), getBattleResult().getWinnerCamp());
				} else if (this.subBattleType == BattleSubTypeEnum.BSTE_CrossArenaPvp_VALUE) {
					settleMatchArenaResult(result);
				} else if (this.subBattleType == BattleSubTypeEnum.BSTE_CrossArenaTop_VALUE) {
					settleMatchArenaResult(result);
					CrossArenaTopManager.getInstance().settleEvent(Long.valueOf(idx), getBattleResult().getWinnerCamp());
				} else {
					WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, fromServerIndex, MsgIdEnum.BS_GS_BattleResult_VALUE, result);
				}
			} else {
				BS_CS_BattleResult.Builder result = BS_CS_BattleResult.newBuilder();
				result.setPvpBattleResultData(resultBuilder);
				WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_CrossServer_VALUE, fromServerIndex, MsgIdEnum.BS_CS_BattleResult_VALUE, result);
			}

			LogUtil.info("battle[" + getIdx() + "] fromServerType:" + fromServerType + " settled winnerCamp=" + getBattleResult().getWinnerCamp());
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
		}
	}

	public void settleMatchArenaResult(BS_GS_BattleResult.Builder result) {
		if (result.getPvpBattleResultData().getPlayerListCount() < 2) {
			LogUtil.error("Room.settleMatchArenaResult, member size is less than 2");
			return;
		}
		result.addAllBattleWatchIds(watchPlayerMap.keySet());
		Set<Integer> fromSvrIndexSet = new HashSet<>();
		int firstFromSvrIndex = result.getPvpBattleResultData().getPlayerList(0).getFromSvrIndex();
		int secondFromSvrIndex = result.getPvpBattleResultData().getPlayerList(1).getFromSvrIndex();
		fromSvrIndexSet.add(firstFromSvrIndex);
		fromSvrIndexSet.add(secondFromSvrIndex);
		fromSvrIndexSet.addAll(watchPlayerMap.values());
		for (Integer fromSvrIndex : fromSvrIndexSet) {
			if (fromSvrIndex <= 0) {
				continue;
			}
			WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, fromSvrIndex, MsgIdEnum.BS_GS_BattleResult_VALUE, result);
		}
	}

	/**
	 * @param playerIdx 玩家请求观战
	 * @return
	 */
	public Battle.SC_BattleWatch.Builder jionBattleWatch(String playerIdx, int fromSvrIndex) {
		if (roomState == RoomStateEnum.battleEnd) {
			return null;
		}
		watchPlayerMap.put(playerIdx, fromSvrIndex);
		Battle.SC_BattleWatch.Builder builder = Battle.SC_BattleWatch.newBuilder();
		SC_EnterFight.Builder enterFight = getEnterFightData();
		enterFight.setCamp(2);
		builder.setEnterFightData(enterFight);
		builder.setFrameIndex(frameIndex);
		for (SC_FrameData.Builder keyFrame : frameData) {
			builder.addFrameData(keyFrame);
		}
		return builder;
	}

	/**
	 * @param playerIdx 退出观战
	 */
	public void quitBattleWatch(String playerIdx) {
		watchPlayerMap.remove(playerIdx);
	}

	/**
	 * @param req 广播弹幕
	 */
	public void broadcastBulletCha(ServerTransfer.GS_BS_BattleBulletCha req) {
		Battle.SC_BattleBulletChaPush.Builder msgBack = Battle.SC_BattleBulletChaPush.newBuilder();
		msgBack.setBattleId(req.getBattleId());
		msgBack.setTalkFixedId(req.getTalkFixedId());
		msgBack.setTalkFree(req.getTalkFree());
		msgBack.setTalkType(req.getTalkType());
		msgBack.setPlayerIdx(req.getPlayerIdx());
		msgBack.setName(req.getName());
		for (Map.Entry<String, Integer> ent : watchPlayerMap.entrySet()) {
			WarpServerManager.getInstance().sendBattleMsgToServer(ent.getKey(), ent.getValue(), ServerTypeEnum.STE_GameServer_VALUE, MsgIdEnum.SC_BattleBulletChaPush_VALUE, msgBack);
		}
		for (Player ent : memberList) {
			WarpServerManager.getInstance().sendBattleMsgToServer(ent.getIdx(), ent.getFromServerIndex(), ServerTypeEnum.STE_GameServer_VALUE, MsgIdEnum.SC_BattleBulletChaPush_VALUE, msgBack);
		}
	}

	public int getRobotNum() {
		return robotNum;
	}

	public void setRobotNum(int robotNum) {
		this.robotNum = robotNum;
	}
}