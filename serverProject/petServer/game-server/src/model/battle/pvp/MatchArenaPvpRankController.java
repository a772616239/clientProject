package model.battle.pvp;

import common.GameConst.RedisKey;
import static common.JedisUtil.jedis;
import java.util.List;
import java.util.Objects;
import model.battle.AbstractPvpBattleController;
import model.matcharena.MatchArenaUtil;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult.Builder;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;
import util.LogUtil;

/**
 * @author huhan
 * @date 2021/05/18
 */
public class MatchArenaPvpRankController extends AbstractPvpBattleController {

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
        MatchArenaUtil.tailMatchArenaRankBattle(getPlayerIdx(),getAnotherPlayerScore(),getCamp(),realResult);
        LogUtil.info("MatchArenaPvpRankController.tailSettle, camp 1 idx:" + getCampPlayerIdx(1) + ", camp 2 is robot."," playerIdx:" + getPlayerIdx() +
                ", battle result:" + realResult.getWinnerCamp());

    }


    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_MatchArenaRanking;
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
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        return MatchArenaUtil.doMatchRewards(getPlayerIdx(),battleResult.getWinnerCamp()==1);
    }

    public int getAnotherPlayerScore() {
        for (BattlePlayerInfo playerInfo : getPlayerBattleData()) {
            String curPlayerIdx = playerInfo.getPlayerInfo().getPlayerId();
            if (!Objects.equals(getPlayerIdx(), playerInfo.getPlayerInfo().getPlayerId())) {
                Double doubleScore = jedis.zscore(RedisKey.MatchArenaPlayerScore, curPlayerIdx);
                return doubleScore == null ? 0 : doubleScore.intValue();
            }
        }
        return 0;
    }



}
