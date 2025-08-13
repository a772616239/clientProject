package model.battle.pool;

import model.battle.AbstractBattleController;
import model.battle.AbstractControllerPool;
import model.battle.pvp.*;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.BattleTypeEnum;

/**
 * @author huhan
 * @date 2020/04/28
 */
public class PvpControllerPool extends AbstractControllerPool {
	@Override
	public BattleTypeEnum getBattleType() {
		return BattleTypeEnum.BTE_PVP;
	}

	@Override
	public AbstractBattleController createController(BattleSubTypeEnum subType) {
		if (subType == BattleSubTypeEnum.BSTE_MineFight) {
			return new MinePvpBattleController();
		} else if (subType == BattleSubTypeEnum.BSTE_MistForest) {
			return new MistPvpBattleController();
		} else if (subType == BattleSubTypeEnum.BSTE_GloryRoad) {
			return new GloryRoadPvpController();
		} else if (subType == BattleSubTypeEnum.BSTE_MatchArena) {
			return new MatchArenaPvpRankController();
		} else if (subType == BattleSubTypeEnum.BSTE_ArenaMatchNormal) {
			return new MatchArenaNormalPvpController();
		} else if (subType == BattleSubTypeEnum.BSTE_MatchArenaLeitai) {
			return new MatchArenaLTPvpController();
		} else if (subType == BattleSubTypeEnum.BSTE_CrossArenaPvp) {
			return new CrossArenaPvpController();
		}
		return null;
	}
}
