package model.battle.pve;

import common.GameConst.EventType;
import model.battle.AbstractPveBattleController;
import model.crossarena.CrossArenaManager;
import model.patrol.entity.PatrolBattleResult;
import model.reward.RewardManager;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.ExtendProperty;
import protocol.Battle.SC_BattleResult;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;

import java.util.List;

import static model.crossarena.CrossArenaManager.EventRewardDefault;

public class CrossArenaEventPveBattleController extends AbstractPveBattleController {

	private final String EVENTID = "EVENTID";

	@Override
	public boolean enterParamsSettle(List<String> enterParams) {
		if (enterParams == null || enterParams.size() < 1) {
			return false;
		}
		putEnterParam(EVENTID, enterParams.get(0));
		return true;
	}

	@Override
	protected RetCodeEnum initFightInfo() {
		PatrolBattleResult result = CrossArenaManager.getInstance().getFightMakeId(getPlayerIdx(), getIntEnterParam(EVENTID));
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
		Event event = Event.valueOf(EventType.ET_CROSSARENAEVENT_BATTLE_SETTLE, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
		event.pushParam(getPlayerIdx(), realResult.getWinnerCamp(), getIntEnterParam(EVENTID));
		EventManager.getInstance().dispatchEvent(event);
	}

	@Override
	public BattleSubTypeEnum getSubBattleType() {
		return BattleSubTypeEnum.BSTE_CrossArenaEvent;
	}

	@Override
	public RewardSourceEnum getRewardSourceType() {
		return RewardSourceEnum.RSE_CrossAreanEVENT;
	}

	@Override
	public String getLogExInfo() {
		return null;
	}

	@Override
	public TeamTypeEnum getUseTeamType() {
		return TeamTypeEnum.TTE_MatchArenaLeiTai;
	}

	@Override
	public EnumFunction getFunctionEnum() {
		return EnumFunction.CrossArena;
	}

	@Override
	public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
		if (battleResult == null || battleResult.getWinnerCamp() != getCamp()) {
			return null;
		}
		// 推送奖励更新消息
		String playerId = getPlayerIdx();
		List<Reward> eventReward = CrossArenaManager.getInstance().getEventReward(getIntEnterParam(EVENTID),EventRewardDefault);
		if (CollectionUtils.isNotEmpty(eventReward)) {
			RewardManager.getInstance().doRewardByList(getPlayerIdx(), eventReward,
					ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CrossAreanEVENT), false);
		}
		return eventReward;
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
