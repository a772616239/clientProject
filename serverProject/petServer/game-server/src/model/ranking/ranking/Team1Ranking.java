package model.ranking.ranking;

import model.team.dbCache.teamCache;
import protocol.PrepareWar;

/**
 * 主线编队1战力排行
 */
public class Team1Ranking extends AbstractRanking implements TargetRewardRanking {

    @Override
    public long getLocalScore(String playerIdx) {
        return teamCache.getInstance().getTeamFightAbility(playerIdx, PrepareWar.TeamNumEnum.TNE_Team_1);
    }
}
