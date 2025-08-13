package model.battle.pvp;

import model.battle.AbstractPvpBattleController;
import model.matcharena.MatchArenaUtil;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult.Builder;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;

import java.util.ArrayList;
import java.util.List;

public class MatchArenaLTPvpController extends AbstractPvpBattleController {

    public static final String OPPONENT_SCORE = "OPPONENT_SCORE";

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
        return BattleSubTypeEnum.BSTE_MatchArenaLeitai;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_MatchArenaleitai;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_MatchArenaLeiTai;
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        return new ArrayList<>();
    }

}
