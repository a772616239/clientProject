package model.warpServer.battleServer.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.battle.AbstractBattleController;
import model.battle.AbstractPvpBattleController;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_BattleResult;
import protocol.ServerTransfer.PvpBattlePlayerInfo;
import protocol.ServerTransfer.PvpBattleResultData;
import util.EventUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.BS_GS_BattleResult_VALUE)
public class PvpBattleResultHandler extends AbstractHandler<BS_GS_BattleResult> {
	@Override
	protected BS_GS_BattleResult parse(byte[] bytes) throws Exception {
		return BS_GS_BattleResult.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel bsChn, BS_GS_BattleResult ret, int i) {
		// 添加回放保存
		preHandler(ret.getPvpBattleResultData());
		switch (ret.getPvpBattleResultData().getSubBattleType()) {
		case BSTE_MineFight:
//			handlerMineFightBattleResult(ret.getPvpBattleResultData());
			break;
		case BSTE_GloryRoad:
			handlerGloryRoadBattleResult(ret.getPvpBattleResultData());
			break;
		case BSTE_MatchArena:
		case BSTE_ArenaMatchNormal:
		case BSTE_MatchArenaRanking:
			handlerMatchArenaBattleResult(ret.getPvpBattleResultData());
			break;
		case BSTE_MatchArenaLeitai:
			handlerMatchArenaLeitaiBattleResult(ret);
			break;
		case BSTE_CrossArenaPvp:
			handlerCrossArenaPvpBattleResult(ret);
			break;
		default:
			LogUtil.error("PvpBattleResultHandler not support SubBattleType");
		}
	}

	private void handlerGloryRoadBattleResult(PvpBattleResultData ret) {
		if (ret.getPlayerListCount() < 2) {
			LogUtil.error("PvpBattleResultHandler.handlerGloryRoadBattleResult, player size is less than 2");
			return;
		}

		String winPlayer;
		String failedPlayer;
		if (ret.getPlayerListList().get(0).getCamp() == ret.getBattleResult().getWinnerCamp()) {
			winPlayer = ret.getPlayerListList().get(0).getPlayerInfo().getPlayerId();
			failedPlayer = ret.getPlayerListList().get(1).getPlayerInfo().getPlayerId();
		} else {
			winPlayer = ret.getPlayerListList().get(1).getPlayerInfo().getPlayerId();
			failedPlayer = ret.getPlayerListList().get(0).getPlayerInfo().getPlayerId();
		}
		EventUtil.gloryRoadBattleResult(winPlayer, failedPlayer, 1, String.valueOf(ret.getBattleId()));

		// 此处调用为了回收controller
		for (PvpBattlePlayerInfo playerData : ret.getPlayerListList()) {
//            BattleManager.getInstance().settleBattle(playerData.getPlayerInfo().getPlayerId(), ret.getBattleId(), ret.getBattleResult().getWinnerCamp());
			BattleManager.getInstance().settleBattle(playerData.getPlayerInfo().getPlayerId(), ret.getBattleResult());
		}
	}

	private void preHandler(PvpBattleResultData ret) {
		if (ret == null) {
			return;
		}

		for (PvpBattlePlayerInfo playerInfo : ret.getPlayerListList()) {
			AbstractBattleController controller = BattleManager.getInstance().getController(playerInfo.getPlayerInfo().getPlayerId());
			if (!(controller instanceof AbstractPvpBattleController)) {
				continue;
			}

			AbstractPvpBattleController pveBattleController = (AbstractPvpBattleController) controller;
			// 此处添加frameData是为了进行战斗回放保存
			pveBattleController.addAllFrameData(ret.getFrameDataList());
		}
	}

	private void handlerMatchArenaBattleResult(PvpBattleResultData pvpBattleResultData) {
		if (pvpBattleResultData == null) {
			return;
		}
		for (PvpBattlePlayerInfo playerInfo : pvpBattleResultData.getPlayerListList()) {
			String playerIdx = playerInfo.getPlayerInfo().getPlayerId();
			if (playerCache.getByIdx(playerIdx) == null) {
				LogUtil.info("PvpBattleResultHandler.handlerMatchArenaBattleResult, playerIdx" + playerIdx + " is not belong this server, skip settle");
				continue;
			}
			BattleManager.getInstance().settleBattle(playerInfo.getPlayerInfo().getPlayerId(), pvpBattleResultData.getBattleResult());
		}
	}

	private void handlerMatchArenaLeitaiBattleResult(BS_GS_BattleResult ret) {
		if (ret.getPvpBattleResultData() == null) {
			return;
		}
		for (String playerIdx : ret.getBattleWatchIdsList()) {
			if (null != playerCache.getByIdx(playerIdx)) {
				BattleManager.getInstance().battleEndWatchResultSend(playerIdx, ret.getPvpBattleResultData());
			}
		}
		for (PvpBattlePlayerInfo playerInfo : ret.getPvpBattleResultData().getPlayerListList()) {
			String playerIdx = playerInfo.getPlayerInfo().getPlayerId();
			if (playerCache.getByIdx(playerIdx) == null) {
				LogUtil.info("PvpBattleResultHandler.handlerMatchArenaBattleResult, playerIdx" + playerIdx + " is not belong this server, skip settle");
				continue;
			}
			BattleManager.getInstance().settleBattle(playerInfo.getPlayerInfo().getPlayerId(), ret.getPvpBattleResultData().getBattleResult());
		}
	}

	private void handlerCrossArenaPvpBattleResult(BS_GS_BattleResult ret) {
		for (String playerIdx : ret.getBattleWatchIdsList()) {
			if (null != playerCache.getByIdx(playerIdx)) {
				BattleManager.getInstance().battleEndWatchResultSend(playerIdx, ret.getPvpBattleResultData());
			}
		}
		for (PvpBattlePlayerInfo playerInfo : ret.getPvpBattleResultData().getPlayerListList()) {
			String playerIdx = playerInfo.getPlayerInfo().getPlayerId();
			if (playerCache.getByIdx(playerIdx) == null) {
				LogUtil.info("PvpBattleResultHandler.handlerCrossArenaPvpBattleResult, playerIdx" + playerIdx + " is not belong this server, skip settle");
				continue;
			}
			BattleManager.getInstance().settleBattle(playerInfo.getPlayerInfo().getPlayerId(), ret.getPvpBattleResultData().getBattleResult());
		}
	}

}
