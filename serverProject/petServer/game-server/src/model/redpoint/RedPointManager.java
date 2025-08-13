package model.redpoint;


import datatool.StringHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import common.GlobalData;
import model.cp.CpTeamManger;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.ranking.RankingTargetManager;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.SC_RedPointClientControl_VALUE;
import protocol.PlayerDB;
import protocol.RedPoint.RedPointInfo;
import protocol.RedPoint.SC_RedPointAll;
import protocol.RedPoint.SC_RedPointClientControl;
import protocol.RedPoint.SC_RedPointOne;
import protocol.RedPoint.SC_RedPointOne.Builder;
import protocol.RedPoint.SC_RedPointRead;
import protocol.RedPointIdEnum.*;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_BS_RedPoint;
import util.GameUtil;

/*
*@author Hammer
*已读不考虑入库,缓存处理
*2021年12月14日
*/
public class RedPointManager {
	// 红点数据缓存
	private Map<String, RedPointData> readPointCache = new ConcurrentHashMap<>();
	// 红点状态handler
	private static Map<Integer, RedPointStateHandler> redStatusHandlerMap = new HashMap<>();
	// 每天推一次的红点id
	private static Set<Integer> dailyOncePointIdSet = new HashSet<>();
	// 每次登录推一次的红点id
	private static Set<Integer> onLoginPointIdSet = new HashSet<>();

	static {

		// 每天推一次的红点id dailyOncePointIdSet.add();

		// 每次登录推一次的红点id onLoginPointIdSet.add();

		redStatusHandlerMap.put(RedPointId.RP_CROSSARENA_CPTeam_NewJoin_LEAF_VALUE, (playerId) -> CpTeamManger.getInstance().getRedPointStateApply(playerId));
		redStatusHandlerMap.put(RedPointId.RP_CROSSARENA_CPTeam_NewInvite_LEAF_VALUE, (playerId) -> CpTeamManger.getInstance().getRedPointStateInvite(playerId));
		redStatusHandlerMap.put(RedPointId.RP_HONORWALL_VALUE, (playerId) -> RankingTargetManager.getInstance().getRedPointStateClaimReward(playerId));
		redStatusHandlerMap.put(RedPointId.ALBUM_PET_VALUE, RedPointManager::albumPetRed);
		redStatusHandlerMap.put(RedPointId.ALBUM_ARTIFACT_VALUE, RedPointManager::albumArtifactRed);
		redStatusHandlerMap.put(RedPointId.ALBUM_CHAIN_VALUE, RedPointManager::albumChainRed);
	}

	private static class LazyHolder {
		private static final RedPointManager INSTANCE = new RedPointManager();
	}

	private RedPointManager() {
	}

	public static RedPointManager getInstance() {
		return LazyHolder.INSTANCE;
	}

	public void onPlayerLogIn(String playerId, boolean isResume) {
		if (!isResume) {
			clearCliCtlPoint(playerId);
		}
		sendAllRedPoint(playerId, isResume);
	}

	public void onPlayerLogout(String playerId) {
		clearCliCtlPoint(playerId);
	}

	/**
	 * 发送所有红点给客户端
	 *
	 * @param playerId
	 */
	public void sendAllRedPoint(String playerId, boolean isResume) {
		SC_RedPointAll.Builder builder = SC_RedPointAll.newBuilder();
		List<RedPointInfo> pointList = new ArrayList<>();

		RedPointData readPointData = getRedPointData(playerId);
		for (Integer redPointId : redStatusHandlerMap.keySet()) {
			RedPointStateHandler redPointStateHandler = redStatusHandlerMap.get(redPointId);
			int redPointState = getRedPointState(playerId, redPointStateHandler);
			readPointData.setState(redPointId, redPointState == 1?RedPointStateEnum.RED:RedPointStateEnum.COMMON);

			RedPointInfo.Builder redPointInfo = RedPointInfo.newBuilder();
			redPointInfo.setIdValue(redPointId);
			redPointInfo.setState(redPointState);

			pointList.add(redPointInfo.build());
		}
		builder.addAllRedPoint(pointList);
		GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_RedPointAll_VALUE, builder);
	}

	/**
	 * 发送红点
	 *
	 * @param playerId
	 * @param type
	 * @param option
	 */
	public void sendRedPoint(String playerId, int type, RedPointOptionEnum option) {
		switch (option) {
			case ADD:
				sendAddRedPoint(playerId, type);
				break;
			case REMOVE:
				sendRemoveRedPoint(playerId, type);
				break;
			default:
				checkAndSend(playerId, type);
		}
	}

	/**
	 * 转发红点
	 *
	 * @param playerId
	 * @param serverIndex
	 * @param type
	 * @param option
	 */
	public void sendRedPointBS(String playerId, String serverIndex, int type, RedPointOptionEnum option) {
		GS_BS_RedPoint.Builder builder = GS_BS_RedPoint.newBuilder();
		builder.setPlayerId(playerId);
		int svrIndex = StringHelper.stringToInt(serverIndex, 0);
		if (svrIndex > 0) {
			builder.setSvrIndex(svrIndex);
		} else {
			builder.setIp(serverIndex);
		}
		builder.setType(type);
		builder.setState(option.ordinal());
		BaseNettyClient client = BattleServerManager.getInstance().getAvailableBattleServer();
		if (client != null) {
			client.send(MsgIdEnum.GS_BS_RedPoint_VALUE, builder);
		}
	}

	private boolean sendRedPoint(String playerId, Builder builder) {
		return GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_RedPointOne_VALUE, builder);
	}

	private void sendAddRedPoint(String playerId, int type) {
		RedPointData readPointData = getRedPointData(playerId);
		if (readPointData.isCliCtl(type)) {
			return;
		}
		RedPointStateEnum preState = readPointData.setState(type, RedPointStateEnum.RED);
		if (preState == RedPointStateEnum.RED) {
			return;
		}

		SC_RedPointOne.Builder builder = SC_RedPointOne.newBuilder();
		RedPointInfo.Builder redPointInfo = RedPointInfo.newBuilder();
		redPointInfo.setIdValue(type);
		redPointInfo.setState(RedPointStateEnum.RED.ordinal());
		builder.setRedPoint(redPointInfo);
		sendRedPoint(playerId, builder);
	}

	private void sendRemoveRedPoint(String playerId, int type) {
		RedPointData readPointData = getRedPointData(playerId);
		if (readPointData.isCliCtl(type)) {
			return;
		}
		RedPointStateEnum preState = readPointData.setState(type, RedPointStateEnum.COMMON);

		if (preState == RedPointStateEnum.RED) {
			SC_RedPointOne.Builder builder = SC_RedPointOne.newBuilder();
			RedPointInfo.Builder redPointInfo = RedPointInfo.newBuilder();
			redPointInfo.setIdValue(type);
			redPointInfo.setState(RedPointStateEnum.COMMON.ordinal());
			builder.setRedPoint(redPointInfo);
			sendRedPoint(playerId, builder);
		}
	}

	private void checkAndSend(String playerId, int type) {
		RedPointData readPointData = getRedPointData(playerId);
		if (readPointData.isCliCtl(type)) {
			return;
		}

		RedPointStateHandler redPointStateHandler = redStatusHandlerMap.get(type);
		int redPointState = getRedPointState(playerId, redPointStateHandler);

		RedPointStateEnum pre = readPointData.setState(type, redPointState == 1 ? RedPointStateEnum.RED : RedPointStateEnum.COMMON);
		if (redPointState == pre.ordinal()) {
			return;
		}
		SC_RedPointOne.Builder builder = SC_RedPointOne.newBuilder();
		RedPointInfo.Builder redPointInfo = RedPointInfo.newBuilder();
		redPointInfo.setIdValue(type);
		redPointInfo.setState(getRedPointState(playerId, redPointStateHandler));
		builder.setRedPoint(redPointInfo);
		sendRedPoint(playerId, builder);

	}

	/**
	 * 已读
	 *
	 * @param playerId
	 * @param type
	 */
	public void redPointRead(String playerId, int type) {
		RedPointData data = getRedPointData(playerId);
		data.setState(type, RedPointStateEnum.COMMON);
		GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_RedPointRead_VALUE, SC_RedPointRead.newBuilder().setIdValue(type));
	}

	/**
	 * 清除已读
	 *
	 * @param playerId
	 * @param type
	 */
	public void redPointReadClear(String playerId, int type) {
		RedPointData data = getRedPointData(playerId);
		data.setState(type, RedPointStateEnum.COMMON);
	}

	private RedPointData getRedPointData(String playerId) {

		if (!readPointCache.containsKey(playerId)) {
			readPointCache.put(playerId, new RedPointData());
		}
		return readPointCache.get(playerId);
	}

	public void change2ClientControl(String playerId, List<RedPointId> redPointIdList) {
		RedPointData readPointData = getRedPointData(playerId);
		readPointData.addCliCtlType(redPointIdList);
		SC_RedPointClientControl.Builder result = SC_RedPointClientControl.newBuilder();
		result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
		GlobalData.getInstance().sendMsg(playerId, SC_RedPointClientControl_VALUE, result);
	}

	private void clearCliCtlPoint(String playerId) {
		RedPointData readPointData = getRedPointData(playerId);
		if (readPointData != null) {
			readPointData.clearCliCtlPointMap();
		}
	}

	private static boolean albumPetRed(String playerId) {
		playerEntity player = playerCache.getByIdx(playerId);
		if (player == null) {
			return false;
		}
		PlayerDB.DB_Collection collection = player.getDb_data().getCollection();
		return !CollectionUtils.isEmpty(collection.getCanClaimedPetExpIdList());
	}

	private static boolean albumArtifactRed(String playerId) {
		playerEntity player = playerCache.getByIdx(playerId);
		return player != null && !CollectionUtils.isEmpty(player.getCanClaimArtifactExp());
	}

	private static boolean albumChainRed(String playerId) {
		playerEntity player = playerCache.getByIdx(playerId);
		return player != null && !CollectionUtils.isEmpty(player.getLinkExp());
	}

	/**
	 * 获取红点状态
	 *
	 * @param playerId
	 * @param redPointStateHandler
	 * @return
	 */
	private int getRedPointState(String playerId, RedPointStateHandler redPointStateHandler) {
		if (redPointStateHandler == null) {
			return RedPointStateEnum.COMMON.ordinal();
		}
		return redPointStateHandler.isRed(playerId)? RedPointStateEnum.RED.ordinal() : RedPointStateEnum.COMMON.ordinal();
	}

	interface RedPointStateHandler {

		boolean isRed(String playerId);
	}

}
