package model.drawCard;

import cfg.DrawCard;
import cfg.GameConfig;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetFragmentConfig;
import cfg.PetFragmentConfigObject;
import common.GameConst;
import common.SyncExecuteFunction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import protocol.Common.Reward;
import protocol.Common.RewardTypeEnum;
import protocol.DrawCard.SelectedPetIndex.Builder;
import protocol.PlayerDB.DB_DrawCardData;
import protocol.PlayerDB.DB_SelectedPet;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2019/10/18
 */
public class DrawCardUtil {

	/**
	 * 根据高级抽卡的次数拿到每次的消耗
	 */
	public static int getDrawCardConsumeByDrawCount(int drawCount) {
		int[] cfg = DrawCard.getById(GameConst.CONFIG_ID).getDrawhighcardconsume();
		if (drawCount >= cfg.length || drawCount <= 0) {
			return cfg[cfg.length - 1];
		}

		return cfg[drawCount - 1];
	}

	/**
	 * 判断是否含有指定品质
	 *
	 * @param randoms
	 * @param quality
	 * @return
	 */
	public static boolean containSpecifyQuality(List<OddsRandom> randoms, int quality) {
		if (GameUtil.collectionIsEmpty(randoms)) {
			return false;
		}

		for (OddsRandom random : randoms) {
			if (Objects.equals(random.getQuality(), quality)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取权重总和
	 *
	 * @param qualityWeights
	 * @return
	 */
	public static int calculateTotalOdds(List<QualityWeight> qualityWeights) {
		int totalOdds = 0;
		if (GameUtil.collectionIsEmpty(qualityWeights)) {
			return totalOdds;
		}

		for (QualityWeight qualityWeight : qualityWeights) {
			totalOdds += qualityWeight.getWeight();
		}
		return totalOdds;
	}

	/**
	 * 将抽卡奖励配置转化为奖励
	 *
	 * @param oddsRandoms
	 * @return
	 */
	public static List<Reward> parseOddsRandomToReward(List<OddsRandom> oddsRandoms) {
		if (GameUtil.collectionIsEmpty(oddsRandoms)) {
			return null;
		}
		List<Reward> result = new ArrayList<>();
		for (OddsRandom random : oddsRandoms) {
			Reward reward = RewardUtil.parseReward(random.getRewards());
			if (reward != null) {
				result.add(reward);
			}
		}
		return result;
	}

	public static int calculateQualityCount(List<OddsRandom> randoms, int quality) {
		if (CollectionUtils.isEmpty(randoms)) {
			return 0;
		}
		int count = 0;
		for (OddsRandom random : randoms) {
			if (random.getQuality() == quality) {
				count++;
			}
		}
		return count;
	}

	/**
	 * @param qualityWeights
	 * @return -1 随机失败
	 */
	public static int randomQuality(List<QualityWeight> qualityWeights) {
		if (GameUtil.collectionIsEmpty(qualityWeights)) {
			return -1;
		}
		int totalOdds = DrawCardUtil.calculateTotalOdds(qualityWeights);
		int randomNum = new Random().nextInt(totalOdds);
		int curNum = 0;
		for (QualityWeight qualityWeight : qualityWeights) {
			if ((curNum += qualityWeight.getWeight()) >= randomNum) {
				if (qualityWeight.getQuality() == DrawCardManager.HIGHEST_QUALITY) {
					LogUtil.info("DrawCardUtil.randomQuality, random quality:" + qualityWeight.getQuality() + ", curNum:" + randomNum + ", quality weight:" + GameUtil.collectionToString(qualityWeights));
				}
				return qualityWeight.getQuality();
			}
		}

		return -1;
	}

	/**
	 * 获取轮盘抽卡特殊处理轮次配置 {当前开启次数, 初始概率,未出红概率增长}
	 *
	 * @param times
	 * @return
	 */
	public static int[] getHighSpecialDealConfig(int times) {
		int[][] ints = DrawCard.getById(GameConst.CONFIG_ID).getHighspecialdealtimes();
		for (int[] anInt : ints) {
			if (anInt.length < 3) {
				LogUtil.error("DrawCardUtil.getHighSpecialDealConfig, high draw card special deal times config length is not enough");
				continue;
			}
			if (anInt[0] == times) {
				return anInt;
			}
		}
		return null;
	}

	/**
	 * 获取玩家自选某个种族内指定品质的宠物
	 *
	 * @param playerIdx
	 * @param quality
	 * @return
	 */
	public static Set<Integer> getPlayerChoicePet(String playerIdx, int quality, int petType) {
		playerEntity entity = playerCache.getByIdx(playerIdx);
		if (entity == null) {
			return null;
		}

		Set<Integer> result = new HashSet<>();
		for (int i = 0; i < entity.getDb_data().getSelectedPetBuilder().getPetDataCount(); i++) {
			int type = entity.getDb_data().getSelectedPetBuilder().getPetType(i);
			if (type == petType) {
				DB_SelectedPet.Builder selectedPet = entity.getDb_data().getSelectedPetBuilder().getPetDataBuilder(i);
				for (Builder petData : selectedPet.getSelectPetDataBuilderList()) {
					if (PetBaseProperties.getQualityByPetId(petData.getPetId()) == quality) {
						result.add(petData.getPetId());
					}
				}
				break;
			}
		}

		return result;
	}

	/**
	 * 获得奖次奖励关联的petId
	 *
	 * @param rewardIntArr
	 * @return
	 */
	public static int getRewardsLinkPetId(int[] rewardIntArr) {
		Reward reward = RewardUtil.parseReward(rewardIntArr);
		if (reward == null) {
			return 0;
		}

		if (reward.getRewardType() == RewardTypeEnum.RTE_PetFragment) {
			return PetFragmentConfig.getLinkPetId(reward.getId());
		} else if (reward.getRewardType() == RewardTypeEnum.RTE_Pet) {
			return reward.getId();
		}
		return 0;
	}

	public static List<Reward> changeFrag2Pet(List<Reward> rewards) {

		List<Reward> result = new ArrayList<>();
		for (Reward reward : rewards) {
			if (reward.getRewardType() != RewardTypeEnum.RTE_PetFragment) {
				result.add(reward);
				continue;
			}
			PetFragmentConfigObject fragmentCfg = PetFragmentConfig.getById(reward.getId());
			if (fragmentCfg == null) {
				result.add(reward);
				continue;
			}
			if (reward.getCount() < fragmentCfg.getAmount()) {
				result.add(reward);
				continue;
			}
			int petid = fragmentCfg.getPetid();
			PetBasePropertiesObject petConfig = PetBaseProperties.getByPetid(petid);
			if (petConfig == null) {
				result.add(reward);
				continue;
			}
			if (petConfig.getStartrarity() < GameConfig.getById(GameConst.CONFIG_ID).getRoll_frag2pet_grade()) {
				result.add(reward);
				continue;
			}
			Reward petReward = RewardUtil.parseReward(RewardTypeEnum.RTE_Pet, fragmentCfg.getPetid(), 1);
			result.add(petReward);
		}

		return result;
	}

	/**
	 * 是否已经到达保底轮次
	 * @param playerIdx 玩家id
	 * @param mustGetFlag 是否是保底标识
	 *
	 * @return
	 */
	public static boolean canMustGet(String playerIdx, boolean mustGetFlag) {
		if (StringUtils.isBlank(playerIdx)) {
			return false;
		}

		playerEntity entity = playerCache.getByIdx(playerIdx);
		if (entity == null) {
			return false;
		}

		//保底抽次
		return SyncExecuteFunction.executePredicate(entity, e -> {
			DB_DrawCardData.Builder drawCardBuilder = entity.getDb_data().getDrawCardBuilder();
			int mustDrawCount = DrawCard.getById(GameConst.CONFIG_ID).getCommonmustdrawcount();
			if (!mustGetFlag) {
				--mustDrawCount;
			}
			boolean canMustGet = drawCardBuilder.getCommonMustDrawCount() >= mustDrawCount;
			if (canMustGet) {
				LogUtil.info("CommonDrawCardPool.canMustGet, player:" + playerIdx + " can must get"
						+ " cur draw times is :" + drawCardBuilder.getCommonMustDrawCount());
			}
			return canMustGet;
		});
	}
}
