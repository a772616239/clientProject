package model.battle.pve;

import common.GameConst.EventType;
import java.util.ArrayList;
import java.util.List;
import model.battle.AbstractPveBattleController;
import model.offerreward.OfferRewardManager;
import model.patrol.entity.PatrolBattleResult;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import server.event.Event;
import server.event.EventManager;
import util.CollectionUtil;
import util.GameUtil;

public class OfferRewardpveBattleController extends AbstractPveBattleController {

	private final String TASKID = "TASKID";
	private final String GRADE = "GRADE";

	@Override
	public boolean enterParamsSettle(List<String> enterParams) {
		if (enterParams == null || enterParams.size() < 1) {
			return false;
		}
		putEnterParam(TASKID, enterParams.get(0));
		return true;
	}

	@Override
	protected RetCodeEnum initFightInfo() {
		PatrolBattleResult result = new PatrolBattleResult();
		int grade = OfferRewardManager.getInstance().fightCheck(getPlayerIdx(), getEnterParam(TASKID), result);
		if (result.getCode() == RetCodeEnum.RCE_Success) {
			putEnterParam(GRADE, grade);
			setFightMakeId(result.getMakeId());
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
		int grade = Integer.valueOf(getEnterParam(GRADE));
		List<Reward> rewards = OfferRewardManager.getInstance().fightEnd(getPlayerIdx(), realResult.getWinnerCamp() == 1, getEnterParam(TASKID), grade);
		if (!CollectionUtils.isEmpty(rewards)) {
			resultBuilder.addAllRewardList(rewards);
		}
	}

	@Override
	public BattleSubTypeEnum getSubBattleType() {
		return BattleSubTypeEnum.BSTE_OfferReward;
	}

	@Override
	public RewardSourceEnum getRewardSourceType() {
		return RewardSourceEnum.RSE_OFFER_REWARD_FIGHT;
	}

	@Override
	public String getLogExInfo() {
		return null;
	}

	@Override
	public TeamTypeEnum getUseTeamType() {
		return TeamTypeEnum.TTE_OfferReward;
	}

	@Override
	public EnumFunction getFunctionEnum() {
		return EnumFunction.OfferReward;
	}

	@Override
	public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
		if (battleResult == null || battleResult.getWinnerCamp() != getCamp()) {
			return null;
		}
		// 推送奖励更新消息
	/*	String playerId = getPlayerIdx();
		long bossDamage = BattleUtil.getFightParamsValue(battleResult.getFightParamsList(), FightParamTypeEnum.FPTE_PM_BossDamage);
		long bossBlood = BattleUtil.getFightParamsValue(battleResult.getFightParamsList(), FightParamTypeEnum.FPTE_ActivityBossBloodLv);
*/
//		List<Reward> battleReward = TrainingManager.getInstance().getBattleReward(playerId, getIntEnterParam(MAPID), getIntEnterParam(POINTID));
		return new ArrayList<>();
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
