package model.battle.pvp;

import java.util.List;
import model.battle.AbstractPvpBattleController;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;

/**
 * @author huhan
 * @date 2020/04/27
 */
public class MinePvpBattleController extends AbstractPvpBattleController {

    @Override
    protected void battleLog(int winnerCamp, List<Reward> rewardListList) {

    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {

    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_MineFight;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_MineFight;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Mine;
    }
}
