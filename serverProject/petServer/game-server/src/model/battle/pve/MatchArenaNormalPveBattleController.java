package model.battle.pve;

import cfg.Head;
import cfg.MatchArenaConfig;
import common.GameConst;
import java.util.ArrayList;
import java.util.List;
import model.arena.ArenaUtil;
import model.battle.AbstractPveBattleController;
import model.matcharena.MatchArenaManager;
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
import protocol.PrepareWar;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.ObjUtil;

/**
 * @author huhan
 * @date 2021/05/25
 */
public class MatchArenaNormalPveBattleController extends AbstractPveBattleController {

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, Builder resultBuilder) {
        MatchArenaUtil.sendNormalMatchArenaBattleFinish(getPlayerIdx(),realResult.getWinnerCamp()==1);
    }

    @Override
    public List<Battle.BattlePetData> buildBuildBattlePetData(String playerIdx, PrepareWar.TeamNumEnum teamNum) {
        List<String> petIdxList = teamCache.getInstance().getTeamPetIdxList(playerIdx, teamNum);
        if (CollectionUtils.isEmpty(petIdxList)) {
            return null;
        }
        List<PetMessage.Pet> petList = petCache.getInstance().getPetByIdList(playerIdx, petIdxList);
        if (CollectionUtils.isEmpty(petIdxList)){
            return null;
        }
        List<PetMessage.Pet> newPets = new ArrayList<>();
        for (PetMessage.Pet pet : petList) {
            PetMessage.Pet newPet = MatchArenaManager.getInstance().recreateBattlePet(pet.getPetBookId());
            if (newPet!=null){
                newPets.add(newPet);
            }
        }
        return petCache.getInstance().buildPetBattleData(playerIdx, newPets, getSubBattleType(),true);
    }


    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        return MatchArenaUtil.doMatchRewards(getPlayerIdx(),battleResult.getWinnerCamp()==1);
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
        return TeamTypeEnum.TTE_MatchArenaNormal;
    }

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {

        BattlePlayerInfo.Builder opponentInfo = BattlePlayerInfo.newBuilder();
        Battle.PlayerBaseInfo.Builder basePlayerInfo = Battle.PlayerBaseInfo.newBuilder();
        basePlayerInfo.setPlayerName(ObjUtil.createRandomName(PlayerUtil.queryPlayerLanguage(getPlayerIdx())));
        basePlayerInfo.setAvatar(Head.randomGetAvatar());
        basePlayerInfo.setLevel(MatchArenaUtil.randomRobotPlayerLv(PlayerUtil.queryPlayerLv(getPlayerIdx())));
        opponentInfo.setPlayerInfo(basePlayerInfo);
        opponentInfo.setCamp(2);
        opponentInfo.addAllPetList(MatchArenaManager.getInstance().randomNormalArenaPetList());
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
