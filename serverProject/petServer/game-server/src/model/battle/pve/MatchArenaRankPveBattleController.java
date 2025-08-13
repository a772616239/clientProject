package model.battle.pve;

import cfg.Head;
import cfg.MatchArenaConfig;
import common.GameConst;
import common.IdGenerator;
import java.util.List;
import model.battle.AbstractPveBattleController;
import model.matcharena.MatchArenaUtil;
import model.pet.dbCache.petCache;
import model.player.util.PlayerUtil;
import model.team.dbCache.teamCache;
import org.springframework.util.CollectionUtils;
import protocol.Battle;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult.Builder;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PetMessage;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.LogUtil;
import util.ObjUtil;

/**
 * @author huhan
 * @date 2021/05/25
 */
public class MatchArenaRankPveBattleController extends AbstractPveBattleController {

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, Builder resultBuilder) {
        MatchArenaUtil.tailMatchArenaRankBattle(getPlayerIdx(), 0, getCamp(), realResult);
        LogUtil.info("MatchArenaRankPveBattleController.tailSettle, camp 1 idx:" + getCampPlayerIdx(1) + ", camp 2 idx:"
                + getCampPlayerIdx(2) + ", playerIdx:" + getPlayerIdx() + ", battle result:" + realResult.getWinnerCamp());

    }


    public List<Battle.BattlePetData> buildRobotBattlePetData() {
        List<String> petIdxList = teamCache.getInstance().getCurUsedTeamPetIdxList(getPlayerIdx(), getUseTeamType());
        if (CollectionUtils.isEmpty(petIdxList)) {
            return null;
        }
        List<PetMessage.Pet> petList = petCache.getInstance().getPetByIdList(getPlayerIdx(), petIdxList);
        if (CollectionUtils.isEmpty(petIdxList)) {
            return null;
        }
        return petCache.getInstance().buildPetBattleData(getPlayerIdx(), MatchArenaUtil.copyTeamProperty(petList), getSubBattleType(), true);
    }


    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        return MatchArenaUtil.doMatchRewards(getPlayerIdx(), battleResult.getWinnerCamp() == 1);
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
        return TeamTypeEnum.TTE_MatchArenaRank;
    }

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {

        BattlePlayerInfo.Builder opponentInfo = BattlePlayerInfo.newBuilder();
        Battle.PlayerBaseInfo.Builder basePlayerInfo = Battle.PlayerBaseInfo.newBuilder();
        basePlayerInfo.setPlayerId(IdGenerator.getInstance().generateId());
        basePlayerInfo.setPlayerName(ObjUtil.createRandomName(PlayerUtil.queryPlayerLanguage(getPlayerIdx())));
        basePlayerInfo.setLevel(MatchArenaUtil.randomRobotPlayerLv(PlayerUtil.queryPlayerLv(getPlayerIdx())));
        basePlayerInfo.setAvatar(Head.randomGetAvatar());
        opponentInfo.setPlayerInfo(basePlayerInfo);
        opponentInfo.setCamp(2);
        opponentInfo.addAllPetList(buildRobotBattlePetData());
        opponentInfo.setIsAuto(true);

        addPlayerBattleData(opponentInfo.build());

        setFightMakeId(MatchArenaConfig.getById(GameConst.CONFIG_ID).getPvefightmakeid());

        return RetCodeEnum.RCE_Success;
    }


    @Override
    protected void initSuccess() {
    }

    @Override
    public int getPointId() {
        return 0;
    }
}
