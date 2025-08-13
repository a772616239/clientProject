package model.ranking.ranking;

import cfg.PetBaseProperties;
import common.HttpRequestUtil;
import common.entity.RankingQuerySingleResult;
import common.entity.RankingUpdateRequest;
import db.entity.BaseEntity;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.PetRankDTO;
import org.springframework.util.CollectionUtils;
import protocol.PetMessage;
import util.LogUtil;

public abstract class AbstractPetRanking extends AbstractRanking implements TargetRewardRanking {

    private Map<String, PetRankDTO> modifyPets = new ConcurrentHashMap<>();

    @Getter
    protected int petType;

    private final int maxRequestSize = 500;

    //<playerId,分数>
    @Getter
    private Map<String, RankingQuerySingleResult> playerRankingScore = new ConcurrentHashMap<>();

    @Override
    public RankingQuerySingleResult queryPlayerRankingData(String playerId) {
        return this.playerRankingScore.get(playerId);
    }

    private void updatePlayerRanking() {
        this.playerRankingScore.clear();
        if (CollectionUtils.isEmpty(this.totalRankingInfo)) {
            return;
        }
        for (RankingQuerySingleResult value : this.totalRankingInfo.values()) {
            String playerIdx = parsePlayerIdxFromRankingResult(value);
            if (playerIdx == null) {
                continue;
            }
            RankingQuerySingleResult existsValue = this.playerRankingScore.get(playerIdx);
            if (existsValue == null || value.getRanking() < existsValue.getRanking()) {
                this.playerRankingScore.put(playerIdx, value);
            }
        }

    }

    /**
     * 这里查询的是玩家该种族最强魔灵的战力
     *
     * @param playerIdx
     * @return
     */
    @Override
    public long getLocalScore(String playerIdx) {
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerIdx);
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (entity == null || player == null) {
            return 0L;
        }

        long ability = entity.peekAllPetByUnModify().stream().filter(pet -> getPetType() == PetBaseProperties.getTypeById(pet.getPetBookId()))
                .mapToLong(PetMessage.Pet::getAbility).max().orElse(0L);

        if (ability <= 0L) {
            return 0L;
        }

        return ability + player.getDb_data().getPetAbilityAddition();
    }


    @Override
    public void updateRanking() {
        updateModifyPetsScore();
        super.updateRanking();
        updatePlayerRanking();
    }


    @Override
    public void updateTotalPlayerScore() {
        RankingUpdateRequest request = new RankingUpdateRequest(getRankingName(), getRankingServerIndex(), getSortRules());
        for (BaseEntity value : petCache.getInstance()._ix_id.values()) {
            petEntity petEntity = (model.pet.entity.petEntity) value;
            if (petEntity == null) {
                break;
            }
            playerEntity player = playerCache.getByIdx(petEntity.getPlayeridx());
            if (player == null) {
                break;
            }
            long petAbilityAddition = player.getDb_data().getPetAbilityAddition();
            for (PetMessage.Pet pet : petEntity.peekAllPetByUnModify()) {
                if (this.getPetType() != PetBaseProperties.getTypeById(pet.getPetBookId())) {
                    continue;
                }
                request.addPetScore(petEntity.getPlayeridx(), pet.getId(), pet.getAbility() + petAbilityAddition);
                if (request.getItems().size() >= maxRequestSize) {
                    sendUpdateRequest(request);
                    request = new RankingUpdateRequest(getRankingName(), getRankingServerIndex(), getSortRules());
                }
            }
        }
        if (!CollectionUtils.isEmpty(request.getItems())) {
            sendUpdateRequest(request);
        }

    }

    private void sendUpdateRequest(RankingUpdateRequest request) {
        if (!HttpRequestUtil.updateRanking(request)) {
            LogUtil.error("AbstractRanking.updatePlayerRankingScore, update ranking failed, rankingName：" + getRankingName());
        }
    }

    public void updateModifyPetsScore() {
        if (CollectionUtils.isEmpty(this.modifyPets)) {
            return;
        }
        Collection<PetRankDTO> modifyData = this.modifyPets.values();
        this.modifyPets = new ConcurrentHashMap<>();

        RankingUpdateRequest request = new RankingUpdateRequest(this.getRankingName(), getRankingServerIndex(), getSortRules());
        for (PetRankDTO modifyPet : modifyData) {
            request.addPetScore(modifyPet.getPlayerId(), modifyPet.getPetId(), modifyPet.getScore());
            if (request.getItems().size() >= maxRequestSize) {
                sendUpdateRequest(request);
                request = new RankingUpdateRequest(this.getRankingName(), getRankingServerIndex(), getSortRules());
            }
        }
        sendUpdateRequest(request);
    }

    public void addModifyPet(PetRankDTO petRankDTO) {
        this.modifyPets.put(petRankDTO.getPetId(), petRankDTO);
    }

    @Override
    protected void no1DataTriggerRankingTargetReward() {
        long score;
        RankingQuerySingleResult no1Pet = this.rankingInfo.get(1);
        if (no1Pet == null) {
            LogUtil.error("rankType:{} no1DataTriggerRankingTargetReward no1Data not exists:{}", getRankingType());
            return;
        }
        String playerIdx = parsePlayerIdxFromRankingResult(no1Pet);
        if (playerIdx == null) {
            LogUtil.error("no1DataTriggerRankingTargetReward parsePlayerIdxFromRankingResult is null by no1Pet:{}", no1Pet);
            return;
        }
        score = no1Pet.getPrimaryScore();

        RankingTargetManager.getInstance().updateRankTarget(getRankingType().getNumber(), playerIdx, score);
        RankingTargetManager.getInstance().updateRankingTargetRewardInfo(this.getRankingType());
    }
}
