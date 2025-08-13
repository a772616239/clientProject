package model.pet;

import cfg.GameConfig;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import common.GameConst;
import common.IdGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.entity.RankingQuerySingleResult;
import lombok.Getter;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.ranking.ranking.AbstractRanking;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Activity;
import protocol.PetMessage;
import protocol.PetMessage.Pet;
import util.LogUtil;

public class PetManager {
	@Getter
	private static final PetManager instance = new PetManager();

	private final Map<String, PetMessage.Pet> level1PetMap = new HashMap<>();

	private final Map<Integer, List<PetMessage.Pet>> baseGradePetMap = new HashMap<>();

	public boolean init() {
		int petBookId;
		int curRarity;
		PetMessage.Pet.Builder petBuilder;
		for (PetBasePropertiesObject config : PetBaseProperties._ix_petid.values()) {
			if ((petBookId = config.getPetid()) == 0) {
				continue;
			}
			for (curRarity = config.getStartrarity(); curRarity <= config.getMaxrarity(); curRarity++) {
				petBuilder = getPetBuilder(petBookId, curRarity);
				if (petBuilder == null) {
					return false;
				}
				Pet build = petBuilder.build();
				if (curRarity == config.getStartrarity()) {
					if (!baseGradePetMap.containsKey(config.getStartrarity())) {
						baseGradePetMap.put(config.getStartrarity(), new ArrayList<>());
					}
					baseGradePetMap.get(config.getStartrarity()).add(build);
				}
				level1PetMap.put(getKey(petBookId, curRarity), build);
			}
		}

		return true;
	}

	private String getKey(int petBookId, int petRarity) {
		return petBookId + "-" + petRarity;
	}

	public PetMessage.Pet buildLevel1NewPet(int bookId, int rarity, int source) {
		PetMessage.Pet pet = level1PetMap.get(getKey(bookId, rarity));
		if (pet == null) {
			return null;
		}
		PetMessage.Pet.Builder result = pet.toBuilder();
		result.setSource(source);
		result.setId(IdGenerator.getInstance().generateId());
		return result.build();
	}

	private PetMessage.Pet.Builder getPetBuilder(int bookId, int rarity) {

		PetBasePropertiesObject petBaseCfg = PetBaseProperties.getByPetid(bookId);
		if (petBaseCfg == null) {
			LogUtil.error("petBaseCfg is null by pet bookId:{}", bookId);
			return null;
		}

		// 配置表属性部分数据已*1000，注意

		PetMessage.Pet.Builder result = PetMessage.Pet.newBuilder();
		// 基础属性
		result.setPetBookId(bookId);
		result.setPetLvl(1);
		result.setPetRarity(rarity);

		result.setPetAliveStatus(1);
		// 属性初始化
		PetMessage.PetProperties.Builder properties = PetMessage.PetProperties.newBuilder();
		for (int[] propertyCfg : petBaseCfg.getPetproperties()) {
			PetMessage.PetPropertyEntity.Builder property = PetMessage.PetPropertyEntity.newBuilder();
			property.setPropertyType(propertyCfg[0]);
			property.setPropertyValue(propertyCfg[1]);
			properties.addProperty(property);
		}
		result.setPetProperty(properties);
		petCache.getInstance().refreshPetData(result, null, null, false);
		return result;
	}

	public List<PetMessage.Pet> buildLevel1NewPetList(int bookId, int rarity, int amount, int source) {
		List<PetMessage.Pet> result = new ArrayList<>();
		PetMessage.Pet pet;
		for (int i = 0; i < amount; i++) {
			pet = buildLevel1NewPet(bookId, rarity, source);
			if (pet != null) {
				result.add(pet);
			}
		}
		return result;
	}

	public PetMessage.PetProperties getLevel1NewPetProperty(int petBookId, int petRarity) {
		PetMessage.Pet.Builder petBuilder = getPetBuilder(petBookId, petRarity);
		if (petBuilder != null) {
			return petBuilder.getPetProperty();
		}

		return null;
	}

	public List<PetMessage.Pet> getPetByGrade(int grade) {
		return baseGradePetMap.getOrDefault(grade, new ArrayList<>());
	}

	public int findWorldMapPetLv() {
		AbstractRanking ranking = RankingManager.getInstance().getRanking(Activity.EnumRankingType.ERT_MainLine, RankingUtils.getRankingTypeDefaultName(Activity.EnumRankingType.ERT_MainLine));
		if (ranking == null) {
			return 0;
		}
		List<RankingQuerySingleResult> rankingTotalInfoList =
				ranking.getRankingTotalInfoList();
		if (CollectionUtils.isEmpty(rankingTotalInfoList)) {
			return 0;
		}
		int worldLvRank = GameConfig.getById(GameConst.CONFIG_ID).getWorldlvrank();
		int sum = 0;
		int num = 0;
		for (RankingQuerySingleResult item : rankingTotalInfoList) {
			if (item.getRanking() > worldLvRank) {
				continue;
			}
			petEntity petEntity = petCache.getInstance().getEntityByPlayer(item.getPrimaryKey());
			if (petEntity == null) {
				continue;
			}
			int petMaxLvHis = petEntity.getPetMaxLvHis();
			if (petMaxLvHis <= 0) {
				continue;
			}
			LogUtil.debug("findWorldMapPetLv playerIdx:{}, rank:{} ,score:{}", item.getPrimaryKey(), item.getRanking(), petMaxLvHis);
			sum += petMaxLvHis;
			num++;
		}
		LogUtil.debug("petManager findWorldMapPetLv claimRankSize:{},totalPetLv:{},totalNum:{}", rankingTotalInfoList.size(), sum, num);
		if (num > 0) {
			return sum / num;
		}
		return 0;
	}
}
