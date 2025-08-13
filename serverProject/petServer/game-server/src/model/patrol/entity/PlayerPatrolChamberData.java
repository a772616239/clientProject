package model.patrol.entity;

import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetGemConfig;
import cfg.PetGemConfigObject;
import cfg.PetRuneProperties;
import cfg.PetRunePropertiesObject;
import lombok.Getter;
import lombok.Setter;
import model.mainLine.dbCache.mainlineCache;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import model.player.dbCache.playerCache;
import org.springframework.util.CollectionUtils;
import protocol.PetMessage;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Rune;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xiao_FL
 * @date 2020/3/3
 */
@Getter
@Setter
public class PlayerPatrolChamberData {

    private int maxPetLvl;
    private int maxPetUpLvl;
    private int maxPetRarity;

    private int mainLinePoint;

    private String playerId;

    private List<Rune> runeList;

    private Map<Integer, PetGemConfigObject> gemMap = Collections.emptyMap();

    private PetGemConfigObject bestGem;

    private model.player.entity.playerEntity playerEntity;
    private long petAbilityAddition;

    private Map<Integer,Integer> propertyAddition;

    public PlayerPatrolChamberData(String playerId) {
        maxPetLvl = 1;
        maxPetUpLvl = 0;
        maxPetRarity = 5;
        this.playerId = playerId;
        this.mainLinePoint = mainlineCache.getInstance().getCurOnHookNode(playerId);
        setMaxPetRuneDesc();
        setMaxPetDesc();
        setMaxPetGemDesc();
        setArtifactAbilityAddition();
    }

    private void setArtifactAbilityAddition() {
        model.player.entity.playerEntity playerEntity = playerCache.getByIdx(playerId);
        if (playerEntity != null) {
            petAbilityAddition = playerEntity.getDb_data().getPetAbilityAddition();
            propertyAddition =playerEntity.getDb_data().getPetPropertyAdditionMap();
        }
    }

    private static Map<Integer, List<Integer>> gemSelectMap = new HashMap<>();

    static {
        //战士：等级>品质>类型4>类型5>类型3>类型1>类型2
        gemSelectMap.put(1, Arrays.asList(4, 5, 3, 1, 2));
        //坦克：等级>品质>类型2>类型1>类型5>类型3>类型4
        gemSelectMap.put(2, Arrays.asList(2, 1, 5, 3, 4));
        // 辅助：等级>品质>类型2>类型1>类型3>类型5>类型4
        gemSelectMap.put(3, Arrays.asList(2, 1, 3, 5, 4));
        //远程：等级>品质>类型4>类型5>类型3>类型1>类型2
        gemSelectMap.put(4, Arrays.asList(4, 5, 3, 1, 2));
    }


    /**
     * 为密室宠物挑选最合适的宝石配置id
     *
     * @param bookId
     * @return
     */
    public String selectGemForChamberPet(int bookId) {
        if (bestGem == null || CollectionUtils.isEmpty(gemMap)) {
            return "";
        }

        PetBasePropertiesObject config = PetBaseProperties.getByPetid(bookId);
        if (config == null) {
            return bestGem.getId() + "";
        }
        int petClass = config.getPetclass();

        List<Integer> select = gemSelectMap.get(petClass);
        if (CollectionUtils.isEmpty(select)) {
            return bestGem.getId() + "";
        }
        for (Integer gemType : select) {
            PetGemConfigObject curConfig = gemMap.get(gemType);
            if (curConfig == null) {
                continue;
            }
            if (curConfig.getId() == bestGem.getId()) {
                return bestGem.getId() + "";
            }

            if (curConfig.getLv() >= bestGem.getLv() && curConfig.getRarity() >= bestGem.getRarity()) {
                return curConfig.getId() + "";
            }
        }
        return bestGem.getId() + "";
    }


    private void setMaxPetGemDesc() {
        petgemEntity cache = petgemCache.getInstance().getEntityByPlayer(playerId);
        if (cache == null) {
            return;
        }
        Collection<PetMessage.Gem> gems = cache.getGemListBuilder().getGemsMap().values();
        if (CollectionUtils.isEmpty(gems)) {
            return;
        }
        gemMap = new HashMap<>();
        Map<Integer, List<PetGemConfigObject>> collect = gems.stream().map(PetMessage.Gem::getGemConfigId).distinct().map(PetGemConfig::getById)
                .filter(Objects::nonNull).collect(Collectors.groupingBy(PetGemConfigObject::getGemtype));

        collect.values().forEach(list -> list.stream().max(gemCfgComparator()).ifPresent(config -> gemMap.put(config.getGemtype(), config)));

        bestGem = gemMap.values().stream().max(gemCfgComparator()).orElse(null);

    }

    private Comparator<PetGemConfigObject> gemCfgComparator() {
        return (o1, o2) -> {
            if (o1.getLv() != o2.getLv()) {
                return o1.getLv() - o2.getLv();
            }
            return o1.getRarity() - o2.getRarity();
        };
    }


    public void setMaxPetData(Pet pet) {
        maxPetLvl = Math.max(maxPetLvl, pet.getPetLvl());
        maxPetUpLvl = Math.max(maxPetUpLvl, pet.getPetUpLvl());
        maxPetRarity = Math.max(maxPetRarity, pet.getPetRarity());
    }

    private Map<Integer, Rune> runeMap = new HashMap<>();

    public void setMaxRuneData(Rune rune) {

        if (rune == null) {
            return;
        }
        PetRunePropertiesObject runeConfig = PetRuneProperties.getByRuneid(rune.getRuneBookId());
        if (runeConfig == null) {
            LogUtil.error("Virtual Desc getMaxData() runeConfig is null,runeBookId=" + rune.getRuneBookId());
            return;
        }
        int runType = runeConfig.getRunetype();
        Rune existRune = runeMap.get(runType);
        if (existRune == null) {
            runeMap.put(runType, rune);
            return;
        }
        if (runeConfig.getRunerarity() > PetRuneProperties.getByRuneid(existRune.getRuneBookId()).getRunerarity()) {
            runeMap.put(runType, rune);
            return;
        }
        if (runeConfig.getRunerarity() == PetRuneProperties.getByRuneid(existRune.getRuneBookId()).getRunerarity() && rune.getRuneLvl() > existRune.getRuneLvl()) {
            runeMap.put(runType, rune);
        }

    }


    public void setMaxPetRuneDesc() {
        petruneEntity cache = petruneCache.getInstance().getEntityByPlayer(playerId);
        if (cache == null) {
            return;
        }
        for (Rune rune : cache.getRuneListBuilder().getRuneMap().values()) {
            setMaxRuneData(rune);
        }
        runeList = new ArrayList<>(runeMap.values());
    }


    public void setMaxPetDesc() {
        petEntity cache = petCache.getInstance().getEntityByPlayer(playerId);
        if (cache == null || CollectionUtils.isEmpty(cache.peekAllPetByUnModify())) {
            return;
        }
        for (Pet pet : cache.peekAllPetByUnModify()) {
            setMaxPetData(pet);
        }
    }


}