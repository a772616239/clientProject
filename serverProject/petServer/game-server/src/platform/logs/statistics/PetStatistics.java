package platform.logs.statistics;

import cfg.PetBaseProperties;
import com.alibaba.fastjson.annotation.JSONField;
import common.tick.GlobalTick;
import db.entity.BaseEntity;
import lombok.Getter;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import org.apache.commons.collections4.CollectionUtils;
import protocol.PetMessage.Pet;
import protocol.PrepareWar.TeamNumEnum;
import util.MapUtil;
import util.TimeUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author hanx
 * @Date2020/10/14 0014 14:51
 **/
@Getter
public class PetStatistics extends AbstractStatistics {
    private static final PetStatistics instance = new PetStatistics();

    public static PetStatistics getInstance() {
        return instance;
    }

    private PetStatistics() {
    }


    /**
     * 上上阵魔灵进阶分布
     * map<品质，宠物数量>
     */
    private final Map<Integer, Long> fightPetRarityMap = new ConcurrentHashMap<>();


    /**
     * * 魔灵（所有初始紫品质）上阵人数
     * *
     * * 魔灵<宠物bookId，人数>
     */
    @JSONField(serialize = false)
    private final Map<Integer, Long> fightPetBookIdMap = new ConcurrentHashMap<>();


    /**
     * 魔灵（所有初始紫品质魔灵）拥有人数
     * <p>
     * map<宠物名，人数>
     */
    @JSONField(serialize = false)
    private final Map<Integer, Long> ownPetBookIdMap = new ConcurrentHashMap<>();

    /**
     * 觉醒等级统计
     * <觉醒等级，上阵宠物数>
     */
    private final Map<Integer, Long> fightPetAwakeMap = new ConcurrentHashMap<>();

    /**
     * 玩家拥有宠物觉醒统计<宠物觉醒等级，拥有人数>
     */
    private final Map<Integer, Long> ownPetAwakeMap = new ConcurrentHashMap<>();


    /**
     * 觉醒等级统计（上阵人数）
     * <觉醒等级，上阵人数>
     */
    private final Map<Integer, Long> fightAwakePersonMap = new ConcurrentHashMap<>();


    /**
     * <进化等级，数量>
     */
    private final Map<Integer, Integer> petEvolveLevelMap = new ConcurrentHashMap<>();


    @JSONField(serialize = false)
    public static final int petRarityLimit = 5;


    @Override
    public void init() {

        for (BaseEntity value : petCache.getInstance()._ix_id.values()) {
            petEntity petEntity = (petEntity) value;
            statisticsOwnPet(petEntity);
            statisticsFightPet(petEntity);
        }
    }

    private void statisticsOwnPet(petEntity petEntity) {
        Set<Integer> allPetBookIds = petEntity.peekAllPetByUnModify().stream().filter(pet -> PetBaseProperties.getQualityByPetId(pet.getPetBookId()) >= petRarityLimit).map(Pet::getPetBookId).collect(Collectors.toSet());
        for (Integer bookId : allPetBookIds) {
            MapUtil.add2LongMapValue(ownPetBookIdMap, bookId, 1L);
        }

        for (Pet pet : petEntity.peekAllPetByUnModify()) {
            if (pet.getPetUpLvl() > 0) {
                MapUtil.add2LongMapValue(ownPetAwakeMap, pet.getPetUpLvl(), 1L);
            }
        }
    }

    private void statisticsFightPet(petEntity petEntity) {
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(petEntity.getPlayeridx());
        if (teamEntity == null) {
            return;
        }
        Team dbTeam = teamEntity.getDBTeam(TeamNumEnum.TNE_Team_1);
        if (dbTeam == null) {
            return;
        }
        Collection<String> petIds = dbTeam.getLinkPetMap().values();
        if (CollectionUtils.isEmpty(petIds)) {
            return;
        }
        List<Pet> petInTeam = petEntity.distinctGetPetByIdList(petIds);
        if (CollectionUtils.isEmpty(petInTeam)) {
            return;
        }
        for (Pet pet : petInTeam) {
            if (pet.getPetRarity() >= petRarityLimit) {
                MapUtil.add2LongMapValue(fightPetRarityMap, pet.getPetRarity(), 1L);
            }
            if (pet.getPetUpLvl() > 0) {
                MapUtil.add2LongMapValue(fightPetAwakeMap, pet.getPetUpLvl(), 1L);
            }
        }
        Set<Integer> awakeLvSet = petInTeam.stream().filter(pet -> pet.getPetUpLvl() > 0).map(Pet::getPetUpLvl).collect(Collectors.toSet());
        for (Integer upLv : awakeLvSet) {
            MapUtil.add2LongMapValue(fightAwakePersonMap, upLv, 1L);
        }


        Set<Integer> team1PetBookIds = petInTeam.stream().filter(pet -> PetBaseProperties.getQualityByPetId(pet.getPetBookId()) >= petRarityLimit).map(Pet::getPetBookId).collect(Collectors.toSet());
        for (Integer bookId : team1PetBookIds) {
            MapUtil.add2LongMapValue(fightPetBookIdMap, bookId, 1L);

        }
    }

    public synchronized void updateFightPetRarityMap(Map<Integer, Long> adds) {
        MapUtil.mergeLongMaps(fightPetRarityMap, adds);
    }


    public synchronized void updateFightPetBookIdMap(Map<Integer, Long> adds) {
        MapUtil.mergeLongMaps(fightPetBookIdMap, adds);
    }

    public synchronized void updateFightPetAwakeMap(Map<Integer, Long> adds) {
        MapUtil.mergeLongMaps(fightPetAwakeMap, adds);
    }

    public synchronized void updateOwnPetBookIdMap(Map<Integer, Long> adds) {
        MapUtil.mergeLongMaps(ownPetBookIdMap, adds);
    }

    public synchronized void updateOwnPetAwakeMap(Map<Integer, Long> adds) {
        MapUtil.mergeLongMaps(ownPetAwakeMap, adds);
    }


    public synchronized void updateFightAwakePersonMap(Map<Integer, Long> adds) {
        MapUtil.mergeLongMaps(fightAwakePersonMap, adds);
    }


    public Map<String, Long> getFightPetNameMap() {

        return fightPetBookIdMap.entrySet().stream().collect(Collectors.toMap(
                entry -> PetBaseProperties.getNameById(entry.getKey()), Entry::getValue));

    }

    public Map<String, Long> getOwnPetNameMap() {
        return ownPetBookIdMap.entrySet().stream().collect(Collectors.toMap(
                entry -> PetBaseProperties.getNameById(entry.getKey()), Entry::getValue));

    }

    public static int nextPetEvolveLevelMapReClaTime;

    public synchronized Map<Integer, Integer> getPetEvolveLevelMap() {
        if (GlobalTick.getInstance().getCurrentTime() < nextPetEvolveLevelMapReClaTime) {
            return petEvolveLevelMap;
        }
        petEvolveLevelMap.clear();
        for (BaseEntity value : petCache.getInstance()._ix_id.values()) {
            for (Pet pet : ((petEntity) value).peekAllPetByUnModify()) {
                if (pet.getEvolveLv() > 0) {
                    MapUtil.add2IntMapValue(petEvolveLevelMap, pet.getEvolveLv(), 1);
                }
            }
        }
        nextPetEvolveLevelMapReClaTime += TimeUtil.MS_IN_A_S * 10;
        return petEvolveLevelMap;
    }
}
