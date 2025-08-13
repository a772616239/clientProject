package model.pet;

import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetRarityConfig;
import common.IdGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import model.pet.dbCache.petCache;
import org.springframework.util.CollectionUtils;
import protocol.Battle;
import protocol.Collection;
import protocol.Common;
import protocol.PetMessage;
import util.LogUtil;
import util.MapUtil;

public class PetFactory {

    public static PetMessage.Pet.Builder buildPetBuilder(int petBookId, int sourceValue) {
        PetBasePropertiesObject petInit = PetBaseProperties.getByPetid(petBookId);
        if (petInit == null) {
            LogUtil.error("model.pet.dbCache.service.PetServiceImpl.getPetEntity, pet bool id is not exist, book id ="
                    + petBookId);
            return null;
        }
        return buildPetBuilder(petInit, sourceValue);
    }

    /**
     * 通过配置初始化宠物
     *
     * @return 初始化宠物实体
     */
    public static PetMessage.Pet.Builder buildPetBuilder(PetBasePropertiesObject pet, int sourceValue) {
        if (pet == null) {
            LogUtil.error("error in PetServiceImpl,method getPetEntity():pet cfg is null" + "\n");
            return null;
        }
        // 配置表属性部分数据已*1000，注意

        PetMessage.Pet.Builder result = PetMessage.Pet.newBuilder();
        // 基础属性
        result.setId(IdGenerator.getInstance().generateId());
        result.setPetBookId(pet.getPetid());
        result.setPetLvl(1);
        result.setPetRarity(pet.getStartrarity());

        result.setPetAliveStatus(1);
        // 属性初始化
        PetMessage.PetProperties.Builder properties = PetMessage.PetProperties.newBuilder();
        for (int[] propertyCfg : pet.getPetproperties()) {
            PetMessage.PetPropertyEntity.Builder property = PetMessage.PetPropertyEntity.newBuilder();
            property.setPropertyType(propertyCfg[0]);
            property.setPropertyValue(propertyCfg[1]);
            properties.addProperty(property);
        }
        result.setPetProperty(properties);
        result.setSource(sourceValue);
        return result;
    }

    public static PetMessage.Pet buildPet(int petBookId, int rarity, int level) {
        PetMessage.Pet.Builder petBuilder = PetFactory.buildPetBuilder(petBookId, 0);
        if (petBuilder == null) {
            return null;
        }
        petBuilder.setPetRarity(rarity).setPetLvl(level);
        return petCache.getInstance().refreshPetData(petBuilder, null).build();
    }

    public static List<PetMessage.Pet.Builder> buildPetBuilderList(int[][] petRarityCount, int pointPetLv) {
        return buildPetBuilderList(petRarityCount, pointPetLv, null);
    }

    public static List<PetMessage.Pet.Builder> buildPetBuilderList(int[][] petRarityCount, int pointPetLv,
                                                                   Battle.ExtendProperty.Builder exProperty, boolean isMonster) {
        if (petRarityCount == null) {
            LogUtil.error("PetFactory.buildPetBuilderList, petRarityCount is null");
            return null;
        }

        List<PetMessage.Pet.Builder> petList = new ArrayList<>();
        for (int[] petCount : petRarityCount) {
            if (petCount.length < 2) {
                LogUtil.error("PetFactory.buildPetBuilderList, pet count cfg length is not enough");
                continue;
            }

            for (int i = 0; i < petCount[1]; i++) {
                int tmpRarity = petCount[0];
                PetBasePropertiesObject randomPet = null;
                while (tmpRarity > 0) {
                    randomPet = PetBaseProperties.randomBravePetByStartRarity(tmpRarity);
                    if (randomPet != null) {
                        break;
                    }
                    tmpRarity--;
                }
                if (randomPet == null) {
                    LogUtil.error("PetFactory.buildPetBuilderList, random pet by rarity failed, rarity:" + petCount[0]);
                    continue;
                }

                PetMessage.Pet.Builder petBuilder = petCache.getInstance().getPetBuilder(randomPet, 0);
                if (petBuilder == null) {
                    continue;
                }

                petBuilder.setPetLvl(Math.max(1, pointPetLv));
                petBuilder.setPetRarity(PetRarityConfig.getRarity(randomPet, petBuilder.getPetLvl()));
                petBuilder.setSource(Common.RewardSourceEnum.RSE_BraveChallenge_VALUE);
                PetMessage.Pet.Builder newPetBuilder = petCache.getInstance().refreshPetData(petBuilder, null, null,
                        false, MapUtil.exPropertyToMap(exProperty), queryBuffIdsFromExProperty(exProperty), isMonster);
                petList.add(newPetBuilder);
            }
        }
        return petList;
    }

    private static List<Integer> queryBuffIdsFromExProperty(protocol.Battle.ExtendProperty.Builder exProperty) {
        if (exProperty == null || org.springframework.util.CollectionUtils.isEmpty(exProperty.getBuffDataList())) {
            return Collections.emptyList();
        }
        return exProperty.getBuffDataList().stream().map(protocol.Battle.PetBuffData::getBuffCfgId)
                .collect(Collectors.toList());
    }

    public static List<PetMessage.Pet> createPetList(int[][] petRarityCount, int pointPetLv) {
        List<PetMessage.Pet.Builder> builders = buildPetBuilderList(petRarityCount, pointPetLv, null);
        if (CollectionUtils.isEmpty(builders)) {
            return Collections.emptyList();
        }
        List<PetMessage.Pet> pets = new ArrayList<>();
        builders.forEach(e -> pets.add(e.build()));
        return pets;

    }

    public static List<PetMessage.Pet.Builder> buildPetBuilderList(int[][] petRarityCount, int pointPetLv,
                                                                   Battle.ExtendProperty.Builder exProperty) {

        return buildPetBuilderList(petRarityCount, pointPetLv, exProperty, false);
    }

    public static List<PetMessage.Pet.Builder> buildMonsterBuilderList(int[][] petRarityCount, int pointPetLv,
                                                                       Battle.ExtendProperty.Builder exProperty) {

        return buildPetBuilderList(petRarityCount, pointPetLv, exProperty, true);
    }


}
