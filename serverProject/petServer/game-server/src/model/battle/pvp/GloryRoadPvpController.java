package model.battle.pvp;

import java.util.List;
import model.battle.AbstractPvpBattleController;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult.Builder;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;

/**
 * @author huhan
 * @date 2021/4/2
 */
public class GloryRoadPvpController extends AbstractPvpBattleController {
    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    protected void battleLog(int winnerCamp, List<Reward> rewardListList) {

    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, Builder resultBuilder) {

    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_GloryRoad;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return null;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return null;
    }
}
