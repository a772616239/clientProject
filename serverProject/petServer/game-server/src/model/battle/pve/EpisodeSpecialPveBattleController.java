package model.battle.pve;


import common.SyncExecuteFunction;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult;
import protocol.Common.Reward;
import protocol.PrepareWar;
import protocol.PrepareWar.TeamTypeEnum;

@Slf4j
public class EpisodeSpecialPveBattleController extends BaseEpisodePveBattleController {

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Episode;
    }

    @Override
    public void tailSettle(CS_BattleResult resultData, List<Reward> rewardList, SC_BattleResult.Builder resultBuilder) {
        super.tailSettle(resultData, rewardList, resultBuilder);
        if (resultData.getWinnerCamp() == 1) {
            clearEpisodeTeam();
        }
    }

    private void clearEpisodeTeam() {
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(getPlayerIdx());
        if (teamEntity == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(teamEntity, t -> {
            Team dbTeam = teamEntity.getDBTeam(PrepareWar.TeamNumEnum.TNE_Episode_1);
            if (dbTeam == null) {
                return;
            }
            teamEntity.clearTeam(PrepareWar.TeamNumEnum.TNE_Episode_1, true);
        });
    }


    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_EpisodeSpecial;
    }

}
