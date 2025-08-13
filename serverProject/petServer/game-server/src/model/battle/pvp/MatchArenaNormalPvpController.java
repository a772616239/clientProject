package model.battle.pvp;

import java.util.List;
import model.battle.AbstractPvpBattleController;
import model.matcharena.MatchArenaUtil;
import protocol.Battle;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult.Builder;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId;

/**
 * @author huhan
 * @date 2021/05/18
 */
public class MatchArenaNormalPvpController extends AbstractPvpBattleController {

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
        MatchArenaUtil.sendNormalMatchArenaBattleFinish(getPlayerIdx(),realResult.getWinnerCamp()==1);
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_ArenaMatchNormal;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_MatchArena;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_MatchArena;
    }

    @Override
    public RetCodeId.RetCodeEnum initBattle(Battle.CS_EnterFight enterFight) {
        return RetCodeId.RetCodeEnum.RCE_Success;
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        return MatchArenaUtil.doMatchRewards(getPlayerIdx(), battleResult.getWinnerCamp() == 1);

    }

}
