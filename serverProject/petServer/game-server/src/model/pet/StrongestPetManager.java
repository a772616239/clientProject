package model.pet;

import cfg.PetBaseProperties;
import common.tick.GlobalTick;
import common.tick.Tickable;
import db.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import model.arena.ArenaManager;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.petgem.dbCache.petgemCache;
import model.petrune.dbCache.petruneCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.lang.time.DateUtils;
import protocol.PetMessage;
import util.LogUtil;
import util.TimeUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrongestPetManager implements Tickable {

    private static final long refreshInterval = DateUtils.MILLIS_PER_MINUTE * 10;

    private long nextTickTime;

    private long msgExpireTime;

    @Getter
    private static final StrongestPetManager instance = new StrongestPetManager();

    //<petBookId,SC_StrongestPetPlayer>
    private static final Map<Integer, PetMessage.SC_StrongestPetPlayer.Builder> playerMsgMap = new HashMap<>(PetBaseProperties._ix_petid.size());

    //<petBookId,SC_StrongestPetDetail>
    private static final Map<Integer, PetMessage.SC_StrongestPetDetail.Builder> petDetailMsgMap = new HashMap<>(PetBaseProperties._ix_petid.size());

    //<petBookId,StrongestPetRank>
    private static final Map<Integer, StrongestPetRank> petRankDataMap = new HashMap<>(PetBaseProperties._ix_petid.size());

    private static  PetMessage.SC_StrongestPetPlayer.Builder defaultPlayerMsg = PetMessage.SC_StrongestPetPlayer.newBuilder();

    private static  PetMessage.SC_StrongestPetDetail.Builder defaultPetMsg = PetMessage.SC_StrongestPetDetail.newBuilder();

    public void rankStrongestPet() {
        for (BaseEntity value : petCache.getInstance()._ix_id.values()) {
            String playerIdx = ((petEntity) value).getPlayeridx();
            playerEntity player = playerCache.getByIdx(playerIdx);
            if (player == null) {
                LogUtil.error("StrongestPetManager init player is null by playerId:{} ", playerIdx);
                continue;
            }
            long petAbilityAddition = player.getDb_data().getPetAbilityAddition();
            for (PetMessage.Pet pet : ((petEntity) value).peekAllPetByUnModify()) {
                putRankData(playerIdx, petAbilityAddition, pet);
            }
        }
        buildClintMsg();
    }

    public void putRankData(String playerIdx, long addition, PetMessage.Pet pet) {
        StrongestPetRank strongestPetRank = petRankDataMap.get(pet.getPetBookId());
        if (strongestPetRank == null || addition + pet.getAbility() > strongestPetRank.getAbility()) {
            petRankDataMap.put(pet.getPetBookId(), new StrongestPetRank(playerIdx, pet.getAbility() + addition, pet));
        }

    }


    private void buildClintMsg() {
        for (StrongestPetRank petRank : petRankDataMap.values()) {
            putPlayerMsg(petRank);
            putPetMsg(petRank);
        }
        defaultPlayerMsg.setMsgExpireTime(msgExpireTime);
    }

    private void putPetMsg(StrongestPetRank petRank) {
        String playerId = petRank.getPlayerIdx();
        PetMessage.SC_StrongestPetDetail.Builder msg = PetMessage.SC_StrongestPetDetail.newBuilder();
        playerEntity player = playerCache.getByIdx(playerId);
        msg.setPlayerId(playerId);
        if (player != null) {
            msg.addAllArtifact(player.getSimpleArtifact());
            Map<Integer, Integer> additionMap = player.getDb_data().getGlobalAddition().getArtifactAdditionMap();
            msg.addAllArtifactAdditionKeys(additionMap.keySet());
            msg.addAllArtifactAdditionValues(additionMap.values());
            msg.addAllNewTitleId(player.getPlayerAllTitleIds());

        }
        msg.setPet(buildClientShowPet(petRank, player));
        List<PetMessage.Rune> runes = petruneCache.getInstance().getRuneListByPets(playerId, Collections.singletonList(petRank.getPet().getId()));
        msg.addAllRunes(runes);


        PetMessage.Gem gem = petgemCache.getInstance().getGemByGemIdx(playerId, petRank.getPet().getGemId());
        if (gem != null) {
            msg.setGemCfgId(gem.getGemConfigId());
        }
        petDetailMsgMap.put(petRank.getPet().getPetBookId(), msg);
    }

    private PetMessage.Pet.Builder buildClientShowPet(StrongestPetRank petRank, playerEntity player) {
        PetMessage.Pet.Builder showPet = petRank.getPet().toBuilder();
        Map<Integer, Integer> additionMap = player == null ? Collections.emptyMap() : player.getDb_data().getPetPropertyAdditionMap();
        PetMessage.PetProperties petProperties = petCache.getInstance().refreshProperty(showPet.getPetProperty(), additionMap);
        showPet.setPetProperty(petProperties);

        showPet.setAbility(petRank.getAbility());

        return showPet;
    }


    private void putPlayerMsg(StrongestPetRank petRank) {
        PetMessage.SC_StrongestPetPlayer.Builder playerMsg = PetMessage.SC_StrongestPetPlayer.newBuilder();
        playerEntity player = playerCache.getByIdx(petRank.getPlayerIdx());
        if (player != null) {
            playerMsg.setAvatarId(player.getAvatar());
            playerMsg.setAvatarBorder(player.getDb_data().getCurAvatarBorder());
            playerMsg.setRankIndex(ArenaManager.getInstance().getPlayerRank(petRank.getPlayerIdx()));
            playerMsg.setPlayerName(player.getName());
        }
        playerMsg.setMsgExpireTime(msgExpireTime);
        playerMsgMap.put(petRank.getPet().getPetBookId(), playerMsg);
    }

    public PetMessage.SC_StrongestPetPlayer.Builder getClientStrongestPetPlayerMsg(int petBookId) {
        PetMessage.SC_StrongestPetPlayer.Builder result = playerMsgMap.get(petBookId);
        return result == null ? defaultPlayerMsg : result;
    }

    public PetMessage.SC_StrongestPetDetail.Builder getClientStrongestPetDetailMsg(int petBookId) {
        PetMessage.SC_StrongestPetDetail.Builder result = petDetailMsgMap.get(petBookId);
        return result == null ? defaultPetMsg : result;

    }

    @Override
    public void onTick() {
        if (nextTickTime > GlobalTick.getInstance().getCurrentTime()) {
            return;
        }
        nextTickTime = GlobalTick.getInstance().getCurrentTime() + refreshInterval;
        //这里过期时间做一个短暂延时,因为排序需要耗时,会比tickTime晚一点
        msgExpireTime = nextTickTime + 5 * TimeUtil.MS_IN_A_S;
        long l1 = System.currentTimeMillis();
        rankStrongestPet();
        long l2 = System.currentTimeMillis();
        long cost = l2 - l1;
        LogUtil.info("strongest pet reRank finished cost time:{},nextTickTime :{}", cost, nextTickTime);
    }


    @Data
    @AllArgsConstructor
    private static class StrongestPetRank {
        private String playerIdx;
        private long ability;
        PetMessage.Pet pet;
    }
}
