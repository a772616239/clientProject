/**
 * created by tool DAOGenerate
 */
package model.pet.entity;

import cfg.GameConfig;
import cfg.LinkConfig;
import cfg.LinkConfigObject;
import cfg.PetBaseProperties;
import cfg.PetCollectExpCfg;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst.WarPetUpdate;
import common.GlobalData;
import common.IdGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import model.obj.BaseObj;
import model.pet.dbCache.petCache;
import model.petrune.dbCache.petruneCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.ranking.PetAbilityByClassRank;
import model.team.dbCache.teamCache;
import model.team.entity.teamEntity;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.DailyDateLog;
import platform.logs.entity.PetRefreshLog;
import platform.logs.statistics.PetStatistics;
import protocol.Common;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetDB.SerializablePet;
import protocol.PetDB.SerializablePetCollection;
import protocol.PetDB.SerializablePetCollection.Builder;
import protocol.PetDB.SerializablePetGem;
import protocol.PetMessage;
import protocol.PetMessage.Pet;
import protocol.PetMessage.PetChangeStatus;
import protocol.PetMessage.PetMissionStatus;
import protocol.PetMessage.PetTeamStatus;
import protocol.PetMessage.SC_PetBagRefresh;
import protocol.PetMessage.SC_PetChangeStatus;
import protocol.PetMessage.SC_PetMissionStatus;
import protocol.PetMessage.SC_PetRemove;
import protocol.PetMessage.SC_PetTeamStatus;
import protocol.PetMessage.SC_PetUpdate;
import protocol.PrepareWar.TeamNumEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.event.Event;
import server.event.EventManager;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

import static common.GameConst.EventType.ET_RemoveDisplayPet;
import static protocol.MessageId.MsgIdEnum.SC_HelpPetRemove_VALUE;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class petEntity extends BaseObj {
    public petEntity() {
    }

    @Override
    public String getClassType() {
        return "petEntity";
    }

    @Override
    public void putToCache() {
        petCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.pet = getDbPetsBuilder().clearPet().addAllPet(peekAllPetByUnModify()).build().toByteArray();
        this.collection = getPetCollectionBuilder().build().toByteArray();
    }

    /**
     * 主键
     */
    private String idx;

    /**
     * 宠物所属玩家idx
     */
    private String playeridx;

    /**
     * 宠物信息
     */
    private byte[] pet;

    /**
     * 背包扩容次数
     */
    private int bagenlarge;

    /**
     * 背包容量
     */
    private int capacity;

    /**
     * 背包已使用容量
     */
    private int occupancy;

    /**
     * 收集进度
     */
    private byte[] collection;

    /**
     * 宝石
     */
    private byte[] gem;


    /**
     * 获得主键
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置主键
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得宠物所属玩家idx
     */
    public String getPlayeridx() {
        return playeridx;
    }

    /**
     * 设置宠物所属玩家idx
     */
    public void setPlayeridx(String playeridx) {
        this.playeridx = playeridx;
    }

    /**
     * 获得宠物信息
     */
    public byte[] getPet() {
        return pet;
    }

    /**
     * 设置宠物信息
     */
    public void setPet(byte[] pet) {
        this.pet = pet;
    }

    /**
     * 获得背包扩容次数
     */
    public int getBagenlarge() {
        return bagenlarge;
    }

    /**
     * 设置背包扩容次数
     */
    public void setBagenlarge(int bagenlarge) {
        this.bagenlarge = bagenlarge;
    }

    /**
     * 获得背包容量
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * 设置背包容量
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 获得背包已使用容量
     */
    public int getOccupancy() {
        return petMap.size();
    }

    /**
     * 获得收集进度
     */
    public byte[] getCollection() {
        return collection;
    }

    /**
     * 设置收集进度
     */
    public void setCollection(byte[] collection) {
        this.collection = collection;
    }


    /**
     * 鑾峰緱宝石
     */
    public byte[] getGem() {
        return gem;
    }

    /**
     * 璁剧疆宝石
     */
    public void setGem(byte[] gem) {
        this.gem = gem;
    }

    @Getter
    @Setter
    private volatile long totalAbility;


    @Override
    public String getBaseIdx() {
        return idx;
    }

    /***************************分割**********************************/
    private SerializablePet.Builder petsBuilder;

    private SerializablePetCollection.Builder petCollectionBuilder;

    private SerializablePetGem.Builder petGemBuilder;

    private Map<String, Pet> petMap = new ConcurrentHashMap<>();

    public SerializablePet.Builder getDbPetsBuilder() {
        if (this.petsBuilder == null) {
            if (getPet() != null) {
                synchronized (this) {
                    if (this.petsBuilder != null) {
                        return this.petsBuilder;
                    }
                    try {
                        this.petsBuilder = parseFromByteData();
                    } catch (InvalidProtocolBufferException e) {
                        LogUtil.error("model.pet.entity.petEntity.getPetById, parse player pet failed");
                        LogUtil.printStackTrace(e);
                    }
                }
            } else {
                this.petsBuilder = SerializablePet.newBuilder();
            }
        }
        return this.petsBuilder;
    }

    private SerializablePet.Builder parseFromByteData() throws InvalidProtocolBufferException {
        SerializablePet.Builder newBuilder = SerializablePet.parseFrom(pet).toBuilder();
        //转移字段 pet -> petMap
        if (newBuilder.getPetCount() > 0) {
            List<Pet> temp = newBuilder.getPetList();
            temp.forEach(e -> petMap.put(e.getId(), e));
        }
        return newBuilder;
    }

    public Builder getPetCollectionBuilder() {
        if (petCollectionBuilder == null) {
            if (this.collection != null) {
                try {
                    petCollectionBuilder = SerializablePetCollection.parseFrom(this.collection).toBuilder();
                } catch (InvalidProtocolBufferException e) {
                    LogUtil.printStackTrace(e);
                    LogUtil.warn("model.pet.entity.petEntity.getPetCollectionBuilder, parse failed, return new SerializablePetCollection");
                    petCollectionBuilder = SerializablePetCollection.newBuilder();
                }
            } else {
                petCollectionBuilder = SerializablePetCollection.newBuilder();
            }
        }
        return petCollectionBuilder;
    }

    public SerializablePetGem.Builder getPetGemBuilder() {
        if (petGemBuilder == null) {
            if (this.collection != null) {
                try {
                    petGemBuilder = SerializablePetGem.parseFrom(this.gem).toBuilder();
                } catch (InvalidProtocolBufferException e) {
                    LogUtil.printStackTrace(e);
                    LogUtil.warn("model.pet.entity.petEntity.getPetCollectionBuilder, parse failed, return new SerializablePetCollection");
                    petGemBuilder = SerializablePetGem.newBuilder();
                }
            } else {
                petGemBuilder = SerializablePetGem.newBuilder();
            }
        }
        return petGemBuilder;
    }

    /**
     * 从此方法获取宠物并build修改后请调用putPet 时更改生效
     *
     * @param petId
     * @return
     */
    public Pet getPetById(String petId) {
        return petMap.get(petId);
    }

    public List<Pet> getAllPet() {
        return new ArrayList<>(petMap.values());
    }

    /**
     * 以不可修改集合的形式查看所有宠物
     *
     * @return
     */
    public Collection<Pet> peekAllPetByUnModify() {
        return petMap.values();
    }

    public void putPet(Pet pet) {
        putAllPet(Collections.singletonList(pet));
    }

    public void putAllPet(List<Pet> pets) {
        if (GameUtil.collectionIsEmpty(pets)) {
            return;
        }

        pets.forEach(e -> petMap.put(e.getId(), e));
    }


    public void sendPetTeamStatus(Collection<String> petIdx) {
        if (GameUtil.collectionIsEmpty(petIdx)) {
            return;
        }

        SC_PetTeamStatus.Builder result = SC_PetTeamStatus.newBuilder();
        result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));

        for (String idx : petIdx) {
            Pet petById = getPetById(idx);
            if (null == petById) {
                continue;
            }
            PetTeamStatus.Builder status = PetTeamStatus.newBuilder();
            status.setPetId(petById.getId());
            status.setStatus(petById.getPetTeamStatus());
            result.addPetStatus(status);
        }
        GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_PetTeamStatus_VALUE, result);
    }

    public void sendPetMissionStatus(List<String> petIdList) {
        SC_PetMissionStatus.Builder result = SC_PetMissionStatus.newBuilder();
        for (String petId : petIdList) {
            Pet pet = getPetById(petId);
            if (pet == null) {
                continue;
            }
            PetMissionStatus.Builder status = PetMissionStatus.newBuilder();
            status.setPetId(petId);
            status.setStatus(pet.getPetMissionStatus());
            result.addPetStatus(status.build());
        }
        result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_PetMissionStatus_VALUE, result);
    }

    /**
     * 消息：更新宠物转换状态
     */
    public void sendPetChangeStatus(String petIdx) {
        Pet petById = getPetById(petIdx);
        if (null == petById) {
            return;
        }

        SC_PetChangeStatus.Builder result = SC_PetChangeStatus.newBuilder();
        result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));

        PetChangeStatus.Builder status = PetChangeStatus.newBuilder();
        status.setPetId(petById.getId());
        status.setStatus(petById.getPetChangeStatus());
        result.setPetStatus(status);

        GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_PetChangeStatus_VALUE, result);
    }

    public void sendUpdatePetMsg(List<Pet> petList) {
        if (GameUtil.collectionIsEmpty(petList)) {
            return;
        }
        SC_PetUpdate.Builder result = SC_PetUpdate.newBuilder();
        result.addAllPet(petList);
        result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_PetUpdate_VALUE, result);
    }

    /**
     * 消息：宠物删除，通知展示
     *
     * @param playerId 玩家id
     * @param idList   被移除宠物idList
     */
    public static void sendPetRemove(String playerId, List<String> idList, petEntity cache) {
        SC_PetRemove.Builder result = SC_PetRemove.newBuilder();
        RetCode.Builder retCode = RetCode.newBuilder();
        if (idList != null && idList.size() > 0) {
            retCode.setRetCode(RetCodeEnum.RCE_Success);
        } else {
            retCode.setRetCode(RetCodeEnum.RCE_Failure);
        }
        result.addAllId(idList);
        result.setResult(retCode);
        GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_PetRemove_VALUE, result);
        //通知展示
        playerEntity playerEntity = (playerEntity) playerCache.getInstance().getBaseEntityByIdx(playerId);
        Event event = Event.valueOf(ET_RemoveDisplayPet, cache, playerEntity);
        event.pushParam(idList);
        EventManager.getInstance().dispatchEvent(event);
    }

    public void sendPetRemoveMsg(List<String> idList) {
        if (GameUtil.collectionIsEmpty(idList)) {
            return;
        }
        SC_PetRemove.Builder result = SC_PetRemove.newBuilder();
        result.addAllId(idList);
        result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_PetRemove_VALUE, result);
    }


/*    public void sendUpdatePetVoidStone(String petId) {
        Pet pet = getPetById(petId);
        if (pet == null) {
            return;
        }
        SC_UpdatePetVoidStone.Builder result = SC_UpdatePetVoidStone.newBuilder().setStoneId(pet.getVoidStoneId()).setPetId(petId);
        GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_UpdatePetVoidStone_VALUE, result);
    }*/

    /**
     * 推送宠物背包容量更新
     */
    public void sendPetBagRefresh() {
        SC_PetBagRefresh.Builder result = SC_PetBagRefresh.newBuilder();
        result.setCapacity(getCapacity());
        result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_PetBagRefresh_VALUE, result);
    }

    public petEntity(String initPlayerId) {
        idx = IdGenerator.getInstance().generateId();
        playeridx = initPlayerId;
        bagenlarge = 0;
        // 背包总容量，读取配置
        capacity = GameConfig.getById(1).getPetbaginit();
        this.petCollectionBuilder = SerializablePetCollection.newBuilder().setRewardId(1);

        putToCache();
    }

    /**
     * 获得当前玩家某种宠物的拥有量
     *
     * @param petBookId
     * @return
     */
    public int getPetOwnedCount(int petBookId) {

        return (int) peekAllPetByUnModify().stream()
                .filter(e -> e.getPetBookId() == petBookId)
                .count();
    }

    public List<Integer> collectionAllPet(Collection<Integer> petBookIdList, Reason reason, Collection<Pet> pets) {
        List<Integer> newPetBookIds = unlockAvatar(petBookIdList, reason);
        if (!CollectionUtils.isEmpty(petBookIdList)){
            EventUtil.triggerCollectPets(getPlayeridx(),pets);
        }
        return newPetBookIds;
    }

    private List<Integer> unlockAvatar(Collection<Integer> petBookIdList, Reason reason) {
        Builder petCollectionBuilder = getPetCollectionBuilder();
        //解锁的新头像
        Set<Integer> unlockNewAvatar = new HashSet<>();
        List<Integer> newPetBookIds = new ArrayList<>();
        for (Integer bookId : petBookIdList) {
            if (!collectionContainsPet(bookId)) {
                petCollectionBuilder.addCfgId(bookId);
                newPetBookIds.add(bookId);
                int avatarId = PetBaseProperties.getAvatarIdByPetId(bookId);
                if (avatarId >= 0) {
                    unlockNewAvatar.add(avatarId);
                }
            }
        }

        if (!unlockNewAvatar.isEmpty()) {
            sendPetCollectionUpdate(newPetBookIds);
            EventUtil.triggerAddAvatar(getPlayeridx(), unlockNewAvatar, reason);
        }
        return newPetBookIds;
    }


    private void sendPetCollectionUpdate(List<Integer> newPetBookIds) {
        playerEntity player = playerCache.getByIdx(playeridx);
        if (player != null) {
            player.sendPetCollectionUpdate(newPetBookIds, null);
        }
    }

    /**
     * 图鉴是否包含某类宠物/是否获得过某宠物
     *
     * @param bookId 宠物bookId
     * @return
     */
    public boolean collectionContainsPet(int bookId) {
        return getPetCollectionBuilder().getCfgIdList().contains(bookId);
    }






    public List<Integer> collectionPet(Pet pet, Reason reason) {
        return collectionAllPet(Collections.singletonList(pet.getPetBookId()), reason, Collections.singletonList(pet));
    }

    public List<Pet> distinctGetPetByIdList(Collection<String> petIdxList) {
        if (GameUtil.collectionIsEmpty(petIdxList)) {
            return null;
        }
        return petIdxList.stream().map(this::getPetById).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    /**
     * 宠物属性刷新并更新到玩家,并发送更新消息到客户端
     *
     * @param petList          宠物数据
     * @param reason           刷新刷新原因：Rune符文/PetLvlUp升级/PetStarUp升星/PetUpLvlUp觉醒/PetTransfer宠物转换/GM
     * @param sendPetUpdate    是否发送更新消息到客户端
     * @param sendTotalAbility
     * @return 刷新后属性
     */
    public void refreshAllPetPropertyAndPut(Collection<Pet.Builder> petList, Reason reason
            , boolean showAbilityChange, boolean sendPetUpdate, boolean sendTotalAbility) {
        if (GameUtil.collectionIsEmpty(petList)) {
            return;
        }

        List<Pet> after = new ArrayList<>();
        petList.forEach(e -> {
            Pet.Builder afterPet = petCache.getInstance().refreshPetData(e, petruneCache.getInstance().getPetRune(getPlayeridx(), e.getId()), getPlayeridx(), showAbilityChange);
            after.add(afterPet.build());
            // 埋点日志
            LogService.getInstance().submit(new PetRefreshLog(getPlayeridx(), afterPet, e, reason));
        });

        putAllPet(after);

        if (reason != null) {
            PetAbilityByClassRank.getInstance().settlePetAbilityUpdate(getPlayeridx(), after);
        }

        if ((sendPetUpdate || sendTotalAbility) && GlobalData.getInstance().checkPlayerOnline(getPlayeridx())) {
            // 推送消息
            if (sendPetUpdate) {
                sendUpdatePetMsg(after);
            }
            //刷新总战力
            if (sendTotalAbility) {
                petCache.getInstance().refreshAndSendTotalAbility(getPlayeridx());
            }
        }
    }

    /**
     * 宠物属性刷新并更新到玩家,并发送更新消息到客户端
     *
     * @param pet    宠物数据
     * @param reason 刷新刷新原因：Rune符文/PetLvlUp升级/PetStarUp升星/PetUpLvlUp觉醒/PetTransfer宠物转换/GM
     */
    public void refreshPetPropertyAndPut(Pet.Builder pet, Reason reason, boolean showAbilityChange) {
        refreshAllPetPropertyAndPut(Collections.singletonList(pet), reason, showAbilityChange, true, true);
    }

    public void refreshPetPropertyAndPut(Pet pet, Reason reason, boolean showAbilityChange) {
        if (pet == null) {
            return;
        }
        refreshPetPropertyAndPut(pet.toBuilder(), reason, showAbilityChange);
    }

    public void lockPet(String petIdx, boolean lock) {
        Pet pet = getPetById(petIdx);
        if (pet == null) {
            return;
        }
        Pet build = pet.toBuilder().setPetLockStatus(lock ? 1 : 0).build();
        putPet(build);
    }

    /**
     * @param petIdxList
     * @param missionStatus true 在编队中，
     */
    public void updatePetMissionStatus(List<String> petIdxList, boolean missionStatus) {
        if (GameUtil.collectionIsEmpty(petIdxList)) {
            return;
        }

        petIdxList.forEach(idx -> {
            Pet pet = getPetById(idx);
            if (pet == null) {
                return;
            }
            Pet build = pet.toBuilder().setPetMissionStatus(missionStatus ? 1 : 0).build();
            putPet(build);
        });

        sendPetMissionStatus(petIdxList);
    }

    /**
     * 更新宠物编队状态
     */
    public void updatePetTeamStatus(Collection<String> petIdxList, boolean inOrNot, boolean sendMsg) {
        if (GameUtil.collectionIsEmpty(petIdxList)) {
            return;
        }

        petIdxList.forEach(e -> {
            Pet pet = getPetById(e);
            if (null != pet) {
                putPet(pet.toBuilder().setPetTeamStatus(inOrNot ? 1 : 0).build());
            }
        });
        if (sendMsg) {
            sendPetTeamStatus(petIdxList);
        }
    }

    /**
     * 删除宠物
     *
     * @param petIdList
     * @param reason
     * @return
     */
    public List<Pet> removePets(List<String> petIdList, Reason reason) {
        if (GameUtil.collectionIsEmpty(petIdList)) {
            return null;
        }

        List<Pet> removePets = distinctGetPetByIdList(petIdList);
        if (GameUtil.collectionIsEmpty(removePets) || petIdList.size() != removePets.size()) {
            return null;
        }

        List<String> originalTeam1Pets = getTeam1PetsId(petIdList);

        EventUtil.resetRuneStatus(getPlayeridx(), petIdList);
        EventUtil.removeDisPet(getPlayeridx(), petIdList);
        EventUtil.removePetFromTeams(getPlayeridx(), new HashSet<>(petIdList));
        List<String> gemIds = removePets.stream().filter(pet -> !StringUtils.isEmpty(pet.getGemId())).map(Pet::getGemId).collect(Collectors.toList());
        EventUtil.resetPetGemStatus(getPlayeridx(), gemIds);

        //计算原来拥有的宠物个数
        Map<Integer, Integer> ownedBookIdCountMap = new HashMap<>();
        removePets.forEach(e -> {
            int ownedCount = getPetOwnedCount(e.getPetBookId());
            ownedBookIdCountMap.put(e.getPetBookId(), ownedCount);
        });

        //日志使用 记录移除个数
        Map<Integer, Integer> removeBookIdMap = new HashMap<>();
        removePets.forEach(pet -> {
            removePet(pet, originalTeam1Pets);
            if (removeBookIdMap.containsKey(pet.getPetBookId())) {
                removeBookIdMap.put(pet.getPetBookId(), removeBookIdMap.get(pet.getPetBookId()) + 1);
            } else {
                removeBookIdMap.put(pet.getPetBookId(), 1);
            }
            LogUtil.info("player:{} remove petId:{},bookId:{},rarity:{},lv:{},reason:{}",
                    getPlayeridx(), pet.getId(), pet.getPetBookId(), pet.getPetRarity(), pet.getPetLvl(), reason);
        });

        //日志输出
        for (Entry<Integer, Integer> entry : removeBookIdMap.entrySet()) {
            int owned = ownedBookIdCountMap.get(entry.getKey());
            LogService.getInstance().submit(new DailyDateLog(getPlayeridx(), true, RewardTypeEnum.RTE_Pet,
                    entry.getKey(), owned, entry.getValue(), owned - entry.getValue(), reason));
        }

        sendPetRemoveMsg(petIdList);

        settleLinkByPetRemove(removePets);

        return removePets;
    }

    private void settleLinkByPetRemove(List<Pet> removePets) {
        Collection<Pet> playerPets = peekAllPetByUnModify();
        List<Pet.Builder> modifyPets = new ArrayList<>();
        for (Pet removePet : removePets) {
            if (removePet.getActiveLinkCount() <= 0) {
                continue;
            }
            for (Integer linkId : removePet.getActiveLinkList()) {
                if (playerPets.stream().anyMatch(pet -> pet.getEvolveLv() > 0)) {
                    continue;
                }
                for (Pet playerPet : playerPets) {
                    if (playerPet.getActiveLinkList().contains(linkId)) {
                        modifyPets.add(removePetLink(playerPet.toBuilder(), linkId));
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(modifyPets)) {
            List<Pet> sendUpdatePets = new ArrayList<>();
            for (Pet.Builder modifyPet : modifyPets) {
                Pet.Builder pet = petCache.getInstance().refreshPetData(modifyPet,
                        petruneCache.getInstance().getPetRune(getPlayeridx(), modifyPet.getId()));
                Pet build = pet.build();
                putPet(build);
                sendUpdatePets.add(build);
            }
            sendUpdatePetMsg(sendUpdatePets);
        }

    }

    private Pet.Builder removePetLink(Pet.Builder pet, Integer linkId) {

        List<Integer> activeLinkList = pet.getActiveLinkList();
        List<Integer> newLinkIds = new ArrayList<>(activeLinkList);
        pet.clearActiveLink().addAllActiveLink(newLinkIds);

        int petLinkBuffId = LinkConfig.getPetLinkBuffId(pet.getPetBookId(), linkId);
        List<Integer> buffIdsList = pet.getBuffIdsList();
        List<Integer> newBuffs = new ArrayList<>(buffIdsList);
        newBuffs.remove(petLinkBuffId);
        pet.clearBuffIds().addAllBuffIds(newBuffs);
        return pet;
    }


    private List<String> getTeam1PetsId(List<String> petIdList) {
        if (CollectionUtils.isEmpty(petIdList)) {
            return Collections.emptyList();
        }
        teamEntity team = teamCache.getInstance().getTeamEntityByPlayerId(getPlayeridx());
        if (team == null) {
            return Collections.emptyList();
        }
        return petIdList.stream().filter(petId -> team.petExistInTeam(TeamNumEnum.TNE_Team_1, petId)).collect(Collectors.toList());

    }

    private void removePet(Pet pet, List<String> originalTeam1Pets) {
        petMap.remove(pet.getId());
        //通知战戈宠物更新
        EventUtil.triggerWarPetUpdate(getPlayeridx(), pet.getId(), WarPetUpdate.REMOVE);

        statisticsByPetRemove(pet, originalTeam1Pets);

    }

    public void statisticsByPetRemove(Pet pet, List<String> beforeTeam1Pets) {
        if (beforeTeam1Pets.contains(pet.getId()) && pet.getPetRarity() >= PetStatistics.petRarityLimit) {
            PetStatistics.getInstance().updateFightPetRarityMap(Collections.singletonMap(pet.getPetRarity(), -1L));
            return;
        }


        if (PetBaseProperties.getQualityByPetId(pet.getPetBookId()) >= PetStatistics.petRarityLimit && getPetOwnedCount(pet.getPetBookId()) <= 0) {
            PetStatistics.getInstance().updateOwnPetBookIdMap(Collections.singletonMap(pet.getPetBookId(), -1L));
        }

        if (pet.getPetUpLvl() > 0) {
            PetStatistics.getInstance().updateOwnPetAwakeMap(Collections.singletonMap(pet.getPetUpLvl(), -1L));
        }
    }


    public Pet getMaxAbilityPet() {
        Collection<Pet> allPet = peekAllPetByUnModify();
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(allPet)) {
            return null;
        }

        Pet pet = null;
        for (Pet pet_1 : allPet) {
            if (pet == null) {
                pet = pet_1;
                continue;
            }

            if (pet_1.getAbility() > pet.getAbility()) {
                pet = pet_1;
            }
        }
        return pet;
    }

    public void init() {
        getDbPetsBuilder();
    }





    public Map<String, Integer> getAllPetLvMap() {
        return peekAllPetByUnModify().stream().collect(Collectors.toMap(Pet::getId, Pet::getPetLvl));
    }

    public boolean clearHelpPet(Common.EnumFunction function) {
        PetMessage.HelpPetBagItem petBagItem = getDbPetsBuilder().getHelpPetMap().get(function.getNumber());
        if (petBagItem == null || petBagItem.getPetCount() <= 0) {
            return true;
        }
        getDbPetsBuilder().removeHelpPet(function.getNumber());
        PetMessage.SC_HelpPetRemove.Builder msg = PetMessage.SC_HelpPetRemove.newBuilder();
        for (Pet pet : petBagItem.getPetList()) {
            msg.addPetId(pet.getId());
        }
        GlobalData.getInstance().sendMsg(getPlayeridx(), SC_HelpPetRemove_VALUE, msg);
        return false;
    }

    public void tryUpdatePetMaxLvHis(int petLv) {
        if (getDbPetsBuilder().getPetMaxLvHis() < petLv) {
            getDbPetsBuilder().setPetMaxLvHis(petLv);
        }
    }

    public int getPetMaxLvHis() {
        return getDbPetsBuilder().getPetMaxLvHis();
    }
}