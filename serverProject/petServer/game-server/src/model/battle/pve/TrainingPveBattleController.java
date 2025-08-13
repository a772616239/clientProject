package model.battle.pve;

import cfg.TrainingPoint;
import cfg.TrainingPointObject;
import common.GameConst.EventType;
import java.util.List;
import model.battle.AbstractPveBattleController;
import model.battle.BattleUtil;
import model.patrol.entity.PatrolBattleResult;
import model.training.TrainingManager;
import model.training.bean.TrainPointType;
import model.training.dbCache.trainingCache;
import model.training.entity.trainingEntity;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.ExtendProperty;
import protocol.Battle.SC_BattleResult;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TrainingDB.TrainDBMap;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;

public class TrainingPveBattleController extends AbstractPveBattleController {

	private final String MAPID = "MID";
	private final String POINTID = "PID";

	@Override
	public boolean enterParamsSettle(List<String> enterParams) {
		if (enterParams == null || enterParams.size() < 2) {
			return false;
		}
		putEnterParam(MAPID, enterParams.get(0));
		putEnterParam(POINTID, enterParams.get(1));
		return true;
	}

	@Override
	protected RetCodeEnum initFightInfo() {
		int mapId = getIntEnterParam(MAPID);
		int pointId = getIntEnterParam(POINTID);
		trainingEntity entity = trainingCache.getInstance().getCacheByPlayer(getPlayerIdx());
		if (null == entity) {
			return RetCodeEnum.RCE_TRAIN_NOTOPEN;
		}
		TrainDBMap.Builder tMap = entity.getTrainMapByMapId(mapId);
		if (null == tMap) {
			return RetCodeEnum.RCE_TRAIN_NOTOPEN;
		}
		TrainingPointObject tpoCfg = TrainingPoint.getByPointid(pointId);
		if (tpoCfg == null) {
			return RetCodeEnum.RCE_ErrorParam;
		}
		PatrolBattleResult result = TrainingManager.getInstance().getFightMakeId(getPlayerIdx(), mapId, pointId);
		if (result.isSuccess()) {
			if (result.getBuffList() != null) {
				ExtendProperty.Builder buff = ExtendProperty.newBuilder();
				buff.setCamp(1);
				buff.addAllBuffData(result.getBuffList());
				addExtendProp(buff.build());
			}

			// 怪物属性变化
			ExtendProperty.Builder deBuff = ExtendProperty.newBuilder();
			// debuff或者怪物增强阵营为2
			deBuff.setCamp(2);
			if (result.getDebuffList() != null) {
				deBuff.addAllBuffData(result.getDebuffList());
			}
			// 属性加强
			ExtendProperty monsterExProperty = result.getMonsterExProperty();
			if (monsterExProperty != null) {
				deBuff.setPropDict(monsterExProperty.getPropDict());
				deBuff.addAllBuffData(monsterExProperty.getBuffDataList());
			}
			addExtendProp(deBuff.build());

			TrainingPointObject changePointCfg = null;
			if (tMap.getChangePointMap().containsKey(pointId)) {
				changePointCfg = TrainingPoint.getByPointid(tMap.getChangePointMap().get(pointId));
			}
			boolean needRemainHp = changePointCfg != null ? changePointCfg.getType() == TrainPointType.BLOODMONSTER : tpoCfg.getType() == TrainPointType.BLOODMONSTER;
			if (needRemainHp) {
				addFightParams(FightParamTypeEnum.FPTE_BossRemainHpRate);
			}
			setFightMakeId(result.getMakeId());
			return RetCodeEnum.RCE_Success;
		}
		return result.getCode();
	}

	@Override
	protected void initSuccess() {

	}

	@Override
	public int getPointId() {
		return 0;
	}

	@Override
	protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
		int  hpRate = (int) BattleUtil.getFightParamsValue(realResult.getFightParamsList(),
				FightParamTypeEnum.FPTE_BossRemainHpRate);

		Event event = Event.valueOf(EventType.ET_TRAIN_BATTLE_SETTLE, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
		event.pushParam(getPlayerIdx(), realResult.getWinnerCamp(), getIntEnterParam(MAPID), getIntEnterParam(POINTID), rewardListList,hpRate);
		EventManager.getInstance().dispatchEvent(event);
	}

	@Override
	public BattleSubTypeEnum getSubBattleType() {
		return BattleSubTypeEnum.BSTE_Training;
	}

	@Override
	public RewardSourceEnum getRewardSourceType() {
		return RewardSourceEnum.RSE_TRAIN_BATTLE;
	}

	@Override
	public String getLogExInfo() {
		return null;
	}

	@Override
	public TeamTypeEnum getUseTeamType() {
		return TeamTypeEnum.TTE_Training;
	}

	@Override
	public EnumFunction getFunctionEnum() {
		return EnumFunction.Training;
	}

	@Override
	public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
		if (battleResult == null || battleResult.getWinnerCamp() != getCamp()) {
			return null;
		}
		// 推送奖励更新消息
		String playerId = getPlayerIdx();
		List<Reward> battleReward = TrainingManager.getInstance().getBattleReward(playerId, getIntEnterParam(MAPID), getIntEnterParam(POINTID));
		return battleReward;
	}

	@Override
	public boolean checkSpeedUp(CS_BattleResult clientResult, long realBattleTime) {
		if (isSkipBattle()) {
			return true;
		}
		return super.checkSpeedUp(clientResult, realBattleTime);
	}

	@Override
	public boolean checkRealResultSpeedUp(CS_BattleResult realResult, long realBattleTime) {
		if (isSkipBattle()) {
			return true;
		}
		return super.checkRealResultSpeedUp(realResult, realBattleTime);
	}
}
