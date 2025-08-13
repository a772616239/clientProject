package util;

import cfg.GameConfig;
import cfg.MonsterDifficulty;
import cfg.MonsterDifficultyObject;
import cfg.PatrolBuffDetail;
import cfg.PatrolConfig;
import cfg.PatrolConfigObject;
import cfg.PatrolGreed;
import com.bowlong.third.FastJSON;
import common.GameConst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import model.patrol.entity.PatrolPet;
import model.patrol.entity.PatrolTree;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.util.CollectionUtils;
import protocol.Common.Reward;
import protocol.Patrol.PatrolPoint;
import protocol.Patrol.PatrolSearchEvent;

/**
 * @author xiao_FL
 * @date 2019/10/16
 */
public class PatrolUtil {

	/**
	 * 获取随机节点事件类型 【宝箱/探索/魔灵/空】
	 *
	 * @param config
	 * @return 树节点类型
	 */
	public static int getRandomPatrolType(PatrolConfigObject config) {

		int prob = RandomUtil.getRandom1000();

		if (prob < config.getTreasureprob()) {
			return PatrolTree.EVENT_TREASURE;
		}
		prob -= config.getTreasureprob();

		if (prob < config.getExploreprob()) {
			return PatrolTree.EVENT_EXPLORE;
		}
		prob -= config.getExploreprob();

		if (prob < config.getChamberprob()) {
			return PatrolTree.EVENT_CHAMBER;
		}
		return PatrolTree.EVENT_EMPTY;
	}

	public static List<Integer> branchEndType = Arrays.asList(PatrolTree.EVENT_EXPLORE, PatrolTree.EVENT_CHAMBER,
			PatrolTree.EVENT_BASTARD);

	public static int getRandomBranchEndPatrolType() {
		return branchEndType.get(RandomUtils.nextInt(branchEndType.size()));

	}

	/**
	 * 寻找传入点
	 *
	 * @param root     根节点
	 * @param location 当前点
	 * @return 当前点的地图信息
	 */
	public static PatrolTree preOrderByLocation(PatrolTree root, PatrolTree location) {
		if (root.equals(location)) {
			return root;
		} else if (root.getChildList() != null) {
			for (PatrolTree patrolTree : root.getChildList()) {
				PatrolTree temp = preOrderByLocation(patrolTree, location);
				if (temp != null) {
					return temp;
				}
			}
		}
		return null;
	}

	/**
	 * 获得当前点信息，格式转换
	 *
	 * @return protocol格式
	 */
	public static PatrolPoint getPointByTree(PatrolTree treePoint) {
		PatrolPoint.Builder point = PatrolPoint.newBuilder();
		point.setX(treePoint.getX());
		point.setY(treePoint.getY());
		point.setMapEnumValue(treePoint.getPointType());
		point.setExplored(treePoint.getExplored());
		point.setTreasureGreedConfig(treePoint.getTreasureGreedConfig());
		if (treePoint.getPointType() == PatrolTree.EVENT_EXPLORE) {
			point.addExploreStatus(treePoint.getExploreStatus()[0]);
			point.addExploreStatus(treePoint.getExploreStatus()[1]);
			point.addExploreStatus(treePoint.getExploreStatus()[2]);
		}
		if (treePoint.getPointType() == PatrolTree.EVENT_BASTARD || treePoint.getPointType() == PatrolTree.EVENT_BOSS) {
			point.setFightMakeId(treePoint.getFightMakeId());
		}
		if (treePoint.getPointType() == PatrolTree.EVENT_CHAMBER) {
			if (!CollectionUtils.isEmpty(treePoint.getPetList())) {
				for (PatrolPet patrolPet : treePoint.getPetList()) {
					point.addPetList(toPet(patrolPet));
				}
			}
		}
		return point.build();
	}

	/**
	 * 寻找boss点
	 *
	 * @param root 根节点
	 * @return boss节点
	 */
	public static PatrolTree preOrder4Boss(PatrolTree root) {
		if (root.getPointType() == PatrolTree.EVENT_BOSS) {
			return root;
		} else if (root.getChildList() != null) {
			for (PatrolTree patrolTree : root.getChildList()) {
				PatrolTree temp = preOrder4Boss(patrolTree);
				if (temp != null) {
					return temp;
				}
			}
		}
		return null;
	}

	public static List<Reward> getPatrolTreasureBaseRewards(int mainLinePoint) {
		MonsterDifficultyObject config = MonsterDifficulty.getById(mainLinePoint);
		if (config == null) {
			LogUtil.error("PatrolUtil.patrolTreasure MonsterDifficultyObject is null by mainLinePoint:[{}]",
					mainLinePoint);
			return Collections.emptyList();
		}
		return RewardUtil.parseRewardIntArrayToRewardList(config.getPatrolfixedtreasurereward());
	}

	/**
	 * 巡逻队开启宝箱，获取奖励
	 *
	 * @param playerId       玩家id
	 * @param greed          贪婪值
	 * @param mainLinePoint  主线关卡
	 * @param branchTreasure 分支宝箱
	 * @return 奖励内容
	 */
	public static List<Reward> patrolTreasure(String playerId, int greed, int mainLinePoint, boolean branchTreasure) {
		List<Reward> fixedReward = getPatrolTreasureBaseRewards(mainLinePoint);
		if (CollectionUtils.isEmpty(fixedReward)) {
			return fixedReward;
		}
		List<Reward> pointRandomReward = getPointRandomReward(PatrolTree.EVENT_TREASURE);
		List<Reward> rewards = RandomUtil.mergeReward(fixedReward, pointRandomReward);

		List<Reward> result = patrolReward(queryRewardAddition(playerId, greed), rewards);

		if (!branchTreasure) {
			return result;
		}
		List<Reward> exTreasureReward = PatrolConfig.getInstance().randomExTreasureReward();

		return RewardUtil.mergeRewardList(exTreasureReward, result);
	}

	/**
	 * 按贪婪值加成物品获取
	 *
	 * @param additional 加成量，千分比
	 * @param rewardList 奖励列表
	 * @return 加成奖励
	 */
	public static List<Reward> patrolReward(int additional, List<Reward> rewardList) {
		if (additional == 0 || CollectionUtils.isEmpty(rewardList)) {
			return rewardList;
		}
		List<Reward> result = new ArrayList<>();
		for (Reward reward : rewardList) {
			result.add(reward.toBuilder()
					.setCount((int) (Math.ceil((reward.getCount() * (1 + (double) additional / 1000))))).build());
		}
		return result;
	}

	/**
	 * 巡逻队探索获取一个buff或debuff
	 *
	 * @param point     探索点信息
	 * @param eventType 探索方式（揭开/赞美）
	 * @return 获得的效果
	 */
	public static int patrolEffectList(PatrolTree point, PatrolSearchEvent eventType) {
		switch (eventType) {
		case explore1: {
			return point.getExploreStatus()[0];
		}
		case explore2: {
			return point.getExploreStatus()[1];
		}
		case explore3: {
			return point.getExploreStatus()[2];
		}
		default:
			LogUtil.error("error in PatrolUtil,method patrolEffectList(),point =" + FastJSON.format(point)
					+ ",eventType =" + eventType.toString());
			return -1;
		}
	}

	/**
	 * 巡逻队随机获得战斗奖励
	 *
	 * @param playerId      玩家id
	 * @param greed         贪婪值
	 * @param pointType     战斗点类型
	 * @param mainLinePoint 主线关卡
	 * @return 奖励列表
	 */
	public static List<Reward> patrolBattleReward(String playerId, int greed, int pointType, int mainLinePoint) {

		List<Reward> fixedReward = patrolBattleFixReward(mainLinePoint, pointType);

		List<Reward> randomReward = PatrolUtil.getPointRandomReward(pointType);

		List<Reward> result = new ArrayList<>();

		if (!GameUtil.collectionIsEmpty(fixedReward)) {
			result.addAll(fixedReward);
		}

		if (!GameUtil.collectionIsEmpty(randomReward)) {
			result.addAll(randomReward);
		}
		List<Reward> rewards = patrolReward(queryRewardAddition(playerId, greed), result);
		return RewardUtil.mergeReward(rewards);

	}

	/**
	 * 虚空秘境战斗固定奖励
	 * 
	 * @param mainLinePoint
	 * @param pointType
	 * @return
	 */
	public static List<Reward> patrolBattleFixReward(int mainLinePoint, int pointType) {
		MonsterDifficultyObject config = MonsterDifficulty.getById(mainLinePoint);
		if (config == null) {
			LogUtil.error("PatrolUtil.patrolTreasure MonsterDifficultyObject is null by mainLinePoint:[{}]",
					mainLinePoint);
			return Collections.emptyList();
		}

		if (pointType == PatrolTree.EVENT_BASTARD) {
			return RewardUtil.parseRewardIntArrayToRewardList(config.getPatrolfixedbastardreward());
		} else if (pointType == PatrolTree.EVENT_BOSS) {
			return RewardUtil.parseRewardIntArrayToRewardList(config.getPatrolfixedbossreward());
		}
		return Collections.emptyList();
	}

	private static int queryRewardAddition(String playerId, int greed) {
		int earn = PatrolGreed.getByGreed(greed).getEarn();
		targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
		if (target == null) {
			return earn;
		}
		protocol.TargetSystemDB.DB_PatrolMission patrolMission = target.getDb_Builder().getPatrolMission();
		if (!patrolMission.hasMission()
				|| patrolMission.getMission().getStatus() != protocol.Common.MissionStatusEnum.MSE_Finished) {
			return earn;
		}
		earn += patrolMission.getRewardUp();
		return earn;
	}

	private static List<Reward> getPointRandomReward(int pointType) {
		PatrolConfigObject patrolCfg = PatrolConfig.getById(GameConst.CONFIG_ID);
		// boss 关卡 或者随机是否获取
		if (pointType == PatrolTree.EVENT_BOSS || RandomUtil.getRandom1000() < patrolCfg.getRandomdroprate()) {
			return RewardUtil.drawMustRandomReward(getRandomRewardByPatrolTree(pointType), patrolCfg.getRandomtimes());
		}
		return Collections.emptyList();
	}

	private static int[][] getRandomRewardByPatrolTree(int pointType) {
		PatrolConfigObject patrolCfg = PatrolConfig.getById(GameConst.CONFIG_ID);
		if (pointType == PatrolTree.EVENT_BASTARD) {
			return patrolCfg.getRandombastardreward();
		}
		if (pointType == PatrolTree.EVENT_TREASURE) {
			return patrolCfg.getRandomtreasurereward();
		}
		if (pointType == PatrolTree.EVENT_BOSS) {
			return patrolCfg.getRandombossreward();
		}
		return null;
	}

	public static protocol.PetMessage.Pet toPet(PatrolPet pet) {
		protocol.PetMessage.PetProperties.Builder builder = protocol.PetMessage.PetProperties.newBuilder();
		for (PatrolPet.PetProperty petProperty : pet.getPetPropertyList()) {
			protocol.PetMessage.PetPropertyEntity.Builder builder1 = protocol.PetMessage.PetPropertyEntity.newBuilder();
			builder1.setPropertyType(petProperty.getPropertyType());
			builder1.setPropertyValue(petProperty.getPropertyValue());
			builder.addProperty(builder1);
		}
		return protocol.PetMessage.Pet.newBuilder().setId(pet.getId()).setPetBookId(pet.getPetBookId())
				.setPetLvl(pet.getPetLvl()).setPetRarity(pet.getPetRarity()).setPetUpLvl(pet.getPetUpLvl())
				.setAbility(pet.getAbility()).setGemId(pet.getGemId()).setPetProperty(builder).build();
	}

	/**
	 * 移除一次性buffId
	 *
	 * @param statusList
	 * @return
	 */
	public static List<Integer> removeDisposableStatus(List<Integer> statusList) {
		if (!CollectionUtils.isEmpty(statusList)) {
			statusList = statusList.stream()
					.filter(statusId -> 2 != PatrolBuffDetail.getByBuffid(statusId).getBufftype())
					.collect(Collectors.toList());
		}
		return statusList;
	}

	public static boolean isChamberPets(int bookId) {
		return ArrayUtil.intArrayContain(PatrolConfig.getById(1).getChamberrandompets(), bookId);
	}
}
