package model.ranking.ranking;

import cfg.PetBaseProperties;
import common.GameConst;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.PetRankDTO;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.team.dbCache.teamCache;
import org.springframework.util.CollectionUtils;
import protocol.Activity;
import protocol.PetMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetAbilityByClassRank {

    private static PetAbilityByClassRank instance;

    public static PetAbilityByClassRank getInstance() {
        if (instance == null) {
            synchronized (PetAbilityByClassRank.class) {
                if (instance == null) {
                    instance = new PetAbilityByClassRank();
                }
            }
        }
        return instance;
    }

    //<petClass,PetAbilityRanking>
    Map<Integer, AbstractPetRanking> map = new HashMap<>();


    public PetAbilityByClassRank() {
        map.put(GameConst.NaturePetClass, (AbstractPetRanking) RankingManager.getInstance().getRanking(Activity.EnumRankingType.ERT_NaturePet, RankingUtils.getRankingTypeDefaultName(Activity.EnumRankingType.ERT_NaturePet)));
        map.put(GameConst.WildPetClass, (AbstractPetRanking) RankingManager.getInstance().getRanking(Activity.EnumRankingType.ERT_WildPet, RankingUtils.getRankingTypeDefaultName(Activity.EnumRankingType.ERT_WildPet)));
        map.put(GameConst.AbyssPetClass, (AbstractPetRanking) RankingManager.getInstance().getRanking(Activity.EnumRankingType.ERT_AbyssPet, RankingUtils.getRankingTypeDefaultName(Activity.EnumRankingType.ERT_AbyssPet)));
        map.put(GameConst.HellPetClass, (AbstractPetRanking) RankingManager.getInstance().getRanking(Activity.EnumRankingType.ERT_HellPet, RankingUtils.getRankingTypeDefaultName(Activity.EnumRankingType.ERT_HellPet)));
    }


    public void settlePetAbilityUpdate(String playerIdx, List<PetMessage.Pet> petList) {
        if (CollectionUtils.isEmpty(petList)) {
            return;
        }
        teamCache.settlePetUpdate(playerIdx, petList);

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        long petAbilityAddition = player.getDb_data().getPetAbilityAddition();

        for (PetMessage.Pet pet : petList) {
            updatePetRankScore(playerIdx, petAbilityAddition, pet);

        }
    }

    public void updatePetRankScore(String playerIdx, long petAbilityAddition, PetMessage.Pet pet) {
        AbstractPetRanking ranking = map.get(PetBaseProperties.getTypeById(pet.getPetBookId()));
        if (ranking != null) {
            ranking.addModifyPet(new PetRankDTO(playerIdx, pet.getId(), pet.getPetBookId(), pet.getAbility() + petAbilityAddition));
        }

        RankingTargetManager.getInstance().updateRankTarget(getRankTypeByPetBookId(pet.getPetBookId()), playerIdx, pet.getAbility() + petAbilityAddition);
    }


    private int getRankTypeByPetBookId(int petBookId) {
        int petType = PetBaseProperties.getTypeById(petBookId);
        switch (petType) {
            case 1:
                return Activity.EnumRankingType.ERT_NaturePet_VALUE;
            case 2:
                return Activity.EnumRankingType.ERT_WildPet_VALUE;
            case 3:
                return Activity.EnumRankingType.ERT_AbyssPet_VALUE;
            case 4:
                return Activity.EnumRankingType.ERT_HellPet_VALUE;

        }
        return -1;
    }


}
