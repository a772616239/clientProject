package model.battle.pve;

import cfg.EndlessSpireConfig;
import cfg.EndlessSpireConfigObject;
import cfg.FightMake;
import cfg.FightMakeObject;
import cfg.FunctionOpenLvConfig;
import cfg.GameConfig;
import common.GameConst;
import common.GameConst.EventType;
import model.battle.AbstractPveBattleController;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.GamePlayLog;
import platform.logs.entity.SpireLog;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.PlayerDB.EndlessSpireInfo;
import protocol.PlayerDB.DB_PlayerData.Builder;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;

import java.util.List;

/**
 * @author huhan
 * @date 2020/04/26
 */
public class EndlessSpirePveBattleController extends AbstractPveBattleController {

	public static final String SPIRE_LV = "spireLv";

	@Override
	protected void initSuccess() {
		LogService.getInstance().submit(new GamePlayLog(getPlayerIdx(), EnumFunction.Endless));
	}

	@Override
	protected RetCodeEnum initFightInfo() {
		playerEntity player = playerCache.getByIdx(getPlayerIdx());
		if (player == null) {
			return RetCodeEnum.RCE_ErrorParam;
		}
		if (!player.functionUnLock(EnumFunction.Endless)) {
			return RetCodeEnum.RCE_FunctionNotUnLock;
		}

		Builder db_data = player.getDb_data();
		if (db_data == null) {
			return RetCodeEnum.RCE_UnknownError;
		}

		int maxSpireLv = db_data.getEndlessSpireInfo().getMaxSpireLv();
		int lv = getIntEnterParam(SPIRE_LV);
		// 依次挑战
		if (EndlessSpireConfig.getBySpirelv(lv) == null || lv - maxSpireLv != 1) {
			return RetCodeEnum.RCE_EndlessSpire_ThisLvCanNotPlay;
		}

		EndlessSpireConfigObject endlessCfg = EndlessSpireConfig.getBySpirelv(lv);
		if (endlessCfg == null) {
			return RetCodeEnum.RCE_ErrorParam;
		}

		// 等级限制
		if (endlessCfg.getLvlimit() > player.getLevel()) {
			return RetCodeEnum.RCE_LvNotEnough;
		}

		setFightMakeId(endlessCfg.getMonsterteamid());
		return RetCodeEnum.RCE_Success;
	}

	@Override
	public boolean enterParamsSettle(List<String> enterParams) {
		if (GameUtil.collectionIsEmpty(enterParams)) {
			return false;
		}
		putEnterParam(SPIRE_LV, enterParams.get(0));
		return true;
	}

	@Override
	public int getPointId() {
		return getIntEnterParam(SPIRE_LV);
	}

	@Override
	protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
		// 统计：爬塔日志
		LogService.getInstance().submit(new SpireLog(getPlayerIdx(), getIntEnterParam("spireLv"), getEnterBattleTime(), realResult.getWinnerCamp() == 1, getPveEnterFightData()));

		// 胜利调用
		if (realResult.getWinnerCamp() == getCamp()) {
			Event event = Event.valueOf(EventType.ET_ENDLESS_SPIRE_BATTLE_SETTLE, GameUtil.getDefaultEventSource(), playerCache.getByIdx(getPlayerIdx()));
			event.pushParam(realResult);
			EventManager.getInstance().dispatchEvent(event);
		}
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

	@Override
	public BattleSubTypeEnum getSubBattleType() {
		return BattleSubTypeEnum.BSTE_EndlessSpire;
	}

	@Override
	public RewardSourceEnum getRewardSourceType() {
		return RewardSourceEnum.RSE_EndlessSpire;
	}

	@Override
	public String getLogExInfo() {
		return getEnterParam("spireLv");
	}

	@Override
	public TeamTypeEnum getUseTeamType() {
		return TeamTypeEnum.TTE_Common;
	}

	@Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {

        if (battleResult == null || battleResult.getWinnerCamp() != getCamp()) {
            return null;
        }

        FightMakeObject fightMakeCfg = FightMake.getById(getFightMakeId());
        List<Reward> rewards = null;
        if (fightMakeCfg != null) {
        	rewards = RewardUtil.getRewardsByRewardId(fightMakeCfg.getRewardid());
			EndlessSpireConfigObject bySpirelv = EndlessSpireConfig.getBySpirelv(getIntEnterParam("spireLv"));
			if (bySpirelv != null) {
				int i = GameConfig.getById(GameConst.CONFIG_ID).getFeatidlist()[1];
				Reward.Builder reward = Reward.newBuilder();
				reward.setId(i);
				reward.setRewardType(RewardTypeEnum.RTE_Item);
				reward.setCount(bySpirelv.getGongxun());
				rewards.add(reward.build());
			}
        	RewardManager.getInstance().doRewardByList(getPlayerIdx(), rewards, ReasonManager.getInstance().borrowReason(getRewardSourceType(), getLogExInfo()), false);
        }
        return rewards;
    
    }
}
