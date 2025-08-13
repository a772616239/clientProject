package model.battle.pve;

import cfg.GameConfig;
import common.GameConst;
import model.battle.AbstractPveBattleController;
import model.team.dbCache.teamCache;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult.Builder;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;

import java.util.List;
import java.util.Objects;

/**
 * @author huhan
 * @date 2021/3/3
 */
public class ChallengePlayerPveBattleController extends AbstractPveBattleController {

    public static final String CHALLENGE_PLAYER_IDX = "challengePlayerIdx";

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, Builder resultBuilder) {

    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_ChallengePlayer;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_ChallengePlayer;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Common;
    }

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (CollectionUtils.isEmpty(enterParams)) {
            return false;
        }
        putEnterParam(CHALLENGE_PLAYER_IDX, enterParams.get(0));
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        if (Objects.equals(getPlayerIdx(), getEnterParam(CHALLENGE_PLAYER_IDX))) {
            return RetCodeEnum.RCE_Battle_CanNotChallengeSelf;
        }

        int fightMake = GameConfig.getById(GameConst.CONFIG_ID).getChallengeplayerfightmake();
        setFightMakeId(fightMake);

        String challengePlayer = getEnterParam(CHALLENGE_PLAYER_IDX);
        return addPlayerBattleData(challengePlayer, getUsedTeam(challengePlayer), 2, true);
    }

    @Override
    protected void initSuccess() {
    }

    @Override
    public int getPointId() {
        return 0;
    }

    @Override
    public RetCodeEnum initPlayerBattleData() {
        return addPlayerBattleData(getPlayerIdx(), getUsedTeam(getPlayerIdx()), 1);
    }

    private TeamNumEnum getUsedTeam(String playerIdx) {
        return teamCache.getInstance().getCurUsedTeamNum(playerIdx, getUseTeamType());
    }
}
