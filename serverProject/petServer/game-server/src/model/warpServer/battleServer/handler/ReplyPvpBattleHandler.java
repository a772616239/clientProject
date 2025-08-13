package model.warpServer.battleServer.handler;

import common.GlobalData;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import model.battle.AbstractBattleController;
import model.battle.BattleManager;
import model.gloryroad.GloryRoadManager;
import model.matcharena.MatchArenaManager;
import model.player.dbCache.playerCache;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.BattleTypeEnum;
import protocol.Battle.ExtendProperty;
import protocol.Battle.SC_EnterFight.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_ReplyPvpBattle;
import protocol.ServerTransfer.PvpBattlePlayerInfo;
import protocol.ServerTransfer.ReplyPvpBattleData;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE)
public class ReplyPvpBattleHandler extends AbstractHandler<BS_GS_ReplyPvpBattle> {
	@Override
	protected BS_GS_ReplyPvpBattle parse(byte[] bytes) throws Exception {
		return BS_GS_ReplyPvpBattle.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel bsChn, BS_GS_ReplyPvpBattle ret, int i) {
		if (ret.getReplyPvpBattleData().getSubBattleType() == BattleSubTypeEnum.BSTE_MineFight) {
//			replyMine(bsChn, ret);
		} else if (ret.getReplyPvpBattleData().getSubBattleType() == BattleSubTypeEnum.BSTE_GloryRoad) {
			replyGloryRoad(bsChn, ret);
		} else if (ret.getReplyPvpBattleData().getSubBattleType() == BattleSubTypeEnum.BSTE_ArenaMatchNormal || ret.getReplyPvpBattleData().getSubBattleType() == BattleSubTypeEnum.BSTE_MatchArenaRanking) {
			replyMatchArena(bsChn, ret);
		} else if (ret.getReplyPvpBattleData().getSubBattleType() == BattleSubTypeEnum.BSTE_MatchArenaLeitai) {
			replyMatchArenaLeitai(bsChn, ret);
		} else if (ret.getReplyPvpBattleData().getSubBattleType() == BattleSubTypeEnum.BSTE_CrossArenaPvp) {
			replyCrossArenaPvp(bsChn, ret);
		}
	}

	private void replyGloryRoad(GameServerTcpChannel bsChn, BS_GS_ReplyPvpBattle ret) {
		if (ret.getReplyPvpBattleData().getResult()) {
			String battleIp = bsChn.channel.remoteAddress().toString().substring(1);
			BaseNettyClient channel = BattleServerManager.getInstance().getActiveNettyClientByIpPort(battleIp);
			int serverIndex = channel != null ? channel.getServerIndex() : 0;
			List<BattlePlayerInfo> battlePlayerInfoList = convertToBattlePlayerInfo(ret.getReplyPvpBattleData().getPlayerListList());

			for (PvpBattlePlayerInfo playerInfo : ret.getReplyPvpBattleData().getPlayerListList()) {
				String playerIdx = playerInfo.getPlayerInfo().getPlayerId();
				if (BattleManager.getInstance().isInBattle(playerIdx)) {
					LogUtil.error("ReplyPvpBattleHandler.replyGloryRoad, playerIdx:" + playerIdx + ", is in battle");
					continue;
				}

				AbstractBattleController controller = BattleManager.getInstance().createBattleController(playerIdx, BattleTypeEnum.BTE_PVP, BattleSubTypeEnum.BSTE_GloryRoad);
				if (controller == null) {
					return;
				}

				controller.setBattleId(ret.getReplyPvpBattleData().getBattleId());
				controller.setFightMakeId(ret.getReplyPvpBattleData().getFightMakeId());
				controller.setCamp(playerInfo.getCamp());
				controller.setRandSeed(ret.getReplyPvpBattleData().getRandSeed());
				controller.addAllPlayerBattleData(battlePlayerInfoList);

				Builder enterBattleBuilder = controller.buildEnterBattleBuilder();
				controller.setPveEnterFightData(enterBattleBuilder);
				GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_EnterFight_VALUE, enterBattleBuilder);

				controller.initTime();
				BattleManager.getInstance().managerBattle(controller);

				BattleServerManager.getInstance().addPlayerBattleInfo(playerIdx, serverIndex);
			}
		} else {
			Set<String> playerIdSet = ret.getReplyPvpBattleData().getPlayerListList().stream().map(e -> e.getPlayerInfo().getPlayerId()).collect(Collectors.toSet());
			LogUtil.error("ReplyPvpBattleHandler.replyGloryRoad, enter pvp battle failed, playerList:" + GameUtil.collectionToString(playerIdSet));
			if (ret.getReplyPvpBattleData().getParamsCount() > 0) {
				int parentIndex = StringHelper.stringToInt(ret.getReplyPvpBattleData().getParams(0), -1);
				GloryRoadManager.getInstance().directCheckBattle(parentIndex);
			} else {
				LogUtil.error("ReplyPvpBattleHandler.replyGloryRoad, enter pvp failed and params is empty");
			}
		}
	}

	private List<BattlePlayerInfo> convertToBattlePlayerInfo(List<PvpBattlePlayerInfo> list) {
		if (CollectionUtils.isEmpty(list)) {
			return Collections.emptyList();
		}
		return list.stream().map(e -> {
			BattlePlayerInfo.Builder battlePlyInfo = BattlePlayerInfo.newBuilder();
			battlePlyInfo.setPlayerInfo(e.getPlayerInfo());
			battlePlyInfo.setCamp(e.getCamp());
			battlePlyInfo.addAllPetList(e.getPetListList());
			battlePlyInfo.addAllPlayerSkillIdList(e.getPlayerSkillIdListList());
			battlePlyInfo.addAllFriendHelpPets(e.getFriendPetsList());
			battlePlyInfo.setIsAuto(e.getIsAuto());
			battlePlyInfo.setPlayerExtData(e.getPlayerExtData());
			return battlePlyInfo.build();
		}).collect(Collectors.toList());
	}

	private void replyMatchArena(GameServerTcpChannel bsChn, BS_GS_ReplyPvpBattle ret) {
		if (!matchArenaBeforeCheck(ret)) {
			return;
		}

		String battleIp = bsChn.channel.remoteAddress().toString().substring(1);
		BaseNettyClient channel = BattleServerManager.getInstance().getActiveNettyClientByIpPort(battleIp);
		int serverIndex = channel != null ? channel.getServerIndex() : 0;
		List<BattlePlayerInfo> battlePlayerInfoList = convertToBattlePlayerInfo(ret.getReplyPvpBattleData().getPlayerListList());

		List<ExtendProperty> battleExProp = getBattleExProp(ret.getReplyPvpBattleData().getPlayerListList());

		for (PvpBattlePlayerInfo playerInfo : ret.getReplyPvpBattleData().getPlayerListList()) {
			String playerIdx = playerInfo.getPlayerInfo().getPlayerId();

			if (playerCache.getByIdx(playerIdx) == null) {
				LogUtil.error("ReplyPvpBattleHandler.replyMatchArena, playerIdx:" + playerIdx + ", is in battle");
				continue;
			}

			if (BattleManager.getInstance().isInBattle(playerIdx)) {
				LogUtil.error("ReplyPvpBattleHandler.replyMatchArena, playerIdx:" + playerIdx + ", is in battle");
				continue;
			}

			AbstractBattleController controller = BattleManager.getInstance().createBattleController(playerIdx, BattleTypeEnum.BTE_PVP, BattleSubTypeEnum.BSTE_MatchArena);
			if (controller == null) {
				LogUtil.error("ReplyPvpBattleHandler.replyMatchArena, create match arena pvp controller failed");
				return;
			}

			controller.setBattleId(ret.getReplyPvpBattleData().getBattleId());
			controller.setFightMakeId(ret.getReplyPvpBattleData().getFightMakeId());
			controller.setCamp(playerInfo.getCamp());
			controller.setRandSeed(ret.getReplyPvpBattleData().getRandSeed());
			controller.addAllPlayerBattleData(battlePlayerInfoList);
			if (CollectionUtils.isNotEmpty(battleExProp)) {
				controller.addAllExtendProp(battleExProp);
			}

			Builder enterBattleBuilder = controller.buildEnterBattleBuilder();
			controller.setPveEnterFightData(enterBattleBuilder);
			GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_EnterFight_VALUE, enterBattleBuilder);

			controller.initTime();
			BattleManager.getInstance().managerBattle(controller);

			BattleServerManager.getInstance().addPlayerBattleInfo(playerIdx, serverIndex);
			MatchArenaManager.getInstance().removeMatchingPlayer(playerIdx);
		}
	}

	private void replyMatchArenaLeitai(GameServerTcpChannel bsChn, BS_GS_ReplyPvpBattle ret) {
		String battleIp = bsChn.channel.remoteAddress().toString().substring(1);
		BaseNettyClient channel = BattleServerManager.getInstance().getActiveNettyClientByIpPort(battleIp);
		int serverIndex = channel != null ? channel.getServerIndex() : 0;
		List<BattlePlayerInfo> battlePlayerInfoList = convertToBattlePlayerInfo(ret.getReplyPvpBattleData().getPlayerListList());

		List<ExtendProperty> battleExProp = getBattleExProp(ret.getReplyPvpBattleData().getPlayerListList());

		for (PvpBattlePlayerInfo playerInfo : ret.getReplyPvpBattleData().getPlayerListList()) {
			String playerIdx = playerInfo.getPlayerInfo().getPlayerId();

			if (playerCache.getByIdx(playerIdx) == null) {
				LogUtil.error("ReplyPvpBattleHandler.replyMatchArena, playerIdx:" + playerIdx + ", is in battle");
				continue;
			}

			if (BattleManager.getInstance().isInBattle(playerIdx)) {
				LogUtil.error("ReplyPvpBattleHandler.replyMatchArena, playerIdx:" + playerIdx + ", is in battle");
				continue;
			}

			AbstractBattleController controller = BattleManager.getInstance().createBattleController(playerIdx, BattleTypeEnum.BTE_PVP, BattleSubTypeEnum.BSTE_MatchArenaLeitai);
			if (controller == null) {
				LogUtil.error("ReplyPvpBattleHandler.replyMatchArena, create match arena pvp controller failed");
				return;
			}

			controller.setBattleId(ret.getReplyPvpBattleData().getBattleId());
			controller.setFightMakeId(ret.getReplyPvpBattleData().getFightMakeId());
			controller.setCamp(playerInfo.getCamp());
			controller.setRandSeed(ret.getReplyPvpBattleData().getRandSeed());
			controller.addAllPlayerBattleData(battlePlayerInfoList);
			if (CollectionUtils.isNotEmpty(battleExProp)) {
				controller.addAllExtendProp(battleExProp);
			}

			Builder enterBattleBuilder = controller.buildEnterBattleBuilder();
			controller.setPveEnterFightData(enterBattleBuilder);
			GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_EnterFight_VALUE, enterBattleBuilder);

			controller.initTime();
			BattleManager.getInstance().managerBattle(controller);

			BattleServerManager.getInstance().addPlayerBattleInfo(playerIdx, serverIndex);
			MatchArenaManager.getInstance().removeMatchingPlayer(playerIdx);
		}
	}

	private void replyCrossArenaPvp(GameServerTcpChannel bsChn, BS_GS_ReplyPvpBattle ret) {
		String battleIp = bsChn.channel.remoteAddress().toString().substring(1);
		BaseNettyClient channel = BattleServerManager.getInstance().getActiveNettyClientByIpPort(battleIp);
		int serverIndex = channel != null ? channel.getServerIndex() : 0;
		List<BattlePlayerInfo> battlePlayerInfoList = convertToBattlePlayerInfo(ret.getReplyPvpBattleData().getPlayerListList());

		List<ExtendProperty> battleExProp = getBattleExProp(ret.getReplyPvpBattleData().getPlayerListList());

		for (PvpBattlePlayerInfo playerInfo : ret.getReplyPvpBattleData().getPlayerListList()) {
			String playerIdx = playerInfo.getPlayerInfo().getPlayerId();

			if (playerCache.getByIdx(playerIdx) == null) {
				LogUtil.error("ReplyPvpBattleHandler.replyCrossArenaPvp, playerIdx:" + playerIdx + ", is in battle");
				continue;
			}

			if (BattleManager.getInstance().isInBattle(playerIdx)) {
				LogUtil.error("ReplyPvpBattleHandler.replyCrossArenaPvp, playerIdx:" + playerIdx + ", is in battle");
				continue;
			}

			AbstractBattleController controller = BattleManager.getInstance().createBattleController(playerIdx, BattleTypeEnum.BTE_PVP, BattleSubTypeEnum.BSTE_CrossArenaPvp);
			if (controller == null) {
				LogUtil.error("ReplyPvpBattleHandler.replyCrossArenaPvp, create match arena pvp controller failed");
				return;
			}
			String costStr = "0";
			String ownPower = "0";
			String atterPower = "0";
			String roomId = "";
			if (ret.getReplyPvpBattleData().getParamsList().size() > 3) {
				costStr = ret.getReplyPvpBattleData().getParams(0);
				ownPower = ret.getReplyPvpBattleData().getParams(1);
				atterPower = ret.getReplyPvpBattleData().getParams(2);
				roomId = ret.getReplyPvpBattleData().getParams(3);
			}
			Map<String, String> paramMap = new HashMap<>();
			paramMap.put("COSTID", costStr);
			paramMap.put("OWNPOWER", ownPower);
			paramMap.put("ATTERPOWER", atterPower);
			paramMap.put("ROOMID", roomId);
			controller.setEnterParam(paramMap);
			controller.setBattleId(ret.getReplyPvpBattleData().getBattleId());
			controller.setFightMakeId(ret.getReplyPvpBattleData().getFightMakeId());
			controller.setCamp(playerInfo.getCamp());
			controller.setRandSeed(ret.getReplyPvpBattleData().getRandSeed());
			controller.addAllPlayerBattleData(battlePlayerInfoList);
			if (CollectionUtils.isNotEmpty(battleExProp)) {
				controller.addAllExtendProp(battleExProp);
			}

			Builder enterBattleBuilder = controller.buildEnterBattleBuilder();
			controller.setPveEnterFightData(enterBattleBuilder);
			GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_EnterFight_VALUE, enterBattleBuilder);

			controller.initTime();
			BattleManager.getInstance().managerBattle(controller);

			BattleServerManager.getInstance().addPlayerBattleInfo(playerIdx, serverIndex);
			MatchArenaManager.getInstance().removeMatchingPlayer(playerIdx);
		}
	}

	public boolean matchArenaBeforeCheck(BS_GS_ReplyPvpBattle ret) {
		if (ret == null) {
			return false;
		}

		ReplyPvpBattleData replyPvpBattleData = ret.getReplyPvpBattleData();
		if (!replyPvpBattleData.getResult()) {
			LogUtil.error("ReplyPvpBattleHandler.matchArenaBeforeCheck, result is false");
			return false;
		}

		if (replyPvpBattleData.getPlayerListCount() < 2) {
			LogUtil.error("ReplyPvpBattleHandler.matchArenaBeforeCheck, battle player info size is less than 2");
			return false;
		}

		boolean bothIsNotExist = true;
		for (PvpBattlePlayerInfo playerInfo : replyPvpBattleData.getPlayerListList()) {
			if (playerCache.getByIdx(playerInfo.getPlayerInfo().getPlayerId()) != null) {
				bothIsNotExist = false;
			}
		}

		List<String> playerIdxList = replyPvpBattleData.getPlayerListList().stream().map(e -> e.getPlayerInfo().getPlayerId()).collect(Collectors.toList());
		if (bothIsNotExist) {
			LogUtil.error("ReplyPvpBattleHandler.matchArenaBeforeCheck, both of player is not exist in server, playerList:" + GameUtil.collectionToString(playerIdxList));
			return false;
		}

		return true;
	}

	private List<ExtendProperty> getBattleExProp(List<PvpBattlePlayerInfo> list) {
		if (CollectionUtils.isEmpty(list)) {
			return Collections.emptyList();
		}
		return list.stream().map(PvpBattlePlayerInfo::getExtendPropList).reduce(new ArrayList<>(), (list1, list2) -> {
			list1.addAll(list2);
			return list1;
		});
	}
}
