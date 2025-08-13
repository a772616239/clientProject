package model.rollcard;

import cfg.GameConfig;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetFragmentConfig;
import cfg.PetFragmentConfigObject;
import cfg.RollCard;
import cfg.RollCardObject;
import cfg.RollCardPool;
import cfg.RollCardPoolObject;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.gameplay.dbCache.gameplayCache;
import model.gameplay.entity.gameplayEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.rollcard.bean.RollCardConfigHelper;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Common.Consume;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.GameplayDB;
import protocol.GameplayDB.DB_RollCardInfo;
import protocol.MessageId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.RollCard.RollCardOne;
import protocol.RollCard.RollCardType;
import protocol.RollCard.SC_RollCard;
import protocol.RollCard.SC_RollCardChange;
import protocol.RollCard.SC_RollCardPanel;
import protocol.TargetSystemDB.DB_RollCard;
import protocol.TargetSystemDB.DB_RollCard.Builder;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

/*
*@author Hammer
*2021年11月8日
*/
public class RollCardManager {

	/** 紫色和核心卡池,表ID为KEY */
	private Map<Integer, Map<Integer, RollCardConfigHelper>> configMap = new HashMap<>();

	/** 轮换池子 */
	private List<Integer> poolList = new ArrayList<>();
	/** 绿色蓝色基础卡池,品质为KEY */
	private Map<Integer, RollCardConfigHelper> commonConfigMap = new HashMap<>();

	/** 卡池每个品质权重,卡池ID为KEY */
	private Map<Integer, Map<Integer, Integer>> gradeWeightMap = new HashMap<>();

	/** 卡池权重总值,卡池ID为KEY */
	private Map<Integer, Integer> gradeTotalWeightMap = new HashMap<>();

	private int curPoolId = 0;

	private DB_RollCardInfo.Builder dbBuilder = null;

	private static class LazyHolder {
		private static final RollCardManager INSTANCE = new RollCardManager();
	}

	private RollCardManager() {
	}

	public static RollCardManager getInstance() {
		return LazyHolder.INSTANCE;
	}

	public boolean init() {
		Map<Integer, Map<Integer, RollCardConfigHelper>> configMap = new HashMap<>();
		Map<Integer, RollCardConfigHelper> commonConfigMap = new HashMap<>();
		Map<Integer, Map<Integer, Integer>> gradeWeightMap = new HashMap<>();
		Map<Integer, Integer> gradeTotalWeightMap = new HashMap<>();
		List<Integer> poolList = new ArrayList<>();
		for (Entry<Integer, RollCardObject> ent : RollCard._ix_id.entrySet()) {
			if (!configMap.containsKey(ent.getKey())) {
				configMap.put(ent.getKey(), new HashMap<>());
			}
			poolList.add(ent.getKey());
			// 品质中宠物权重
			Map<Integer, RollCardConfigHelper> map = configMap.get(ent.getKey());
			map.put(5, poolInit(ent.getValue().getPool1()));// 紫色
			map.put(7, poolInit(ent.getValue().getPool2()));// 核心

			// 卡池品质权重
			Map<Integer, Integer> formatMap = formatGradeConfig(ent.getValue().getGrade());
			gradeWeightMap.put(ent.getKey(), formatMap);
			gradeTotalWeightMap.put(ent.getKey(), countValue(formatMap));

		}

		for (Entry<Integer, RollCardPoolObject> ent : RollCardPool._ix_id.entrySet()) {
			if (ent.getKey() <= 0) {
				continue;
			}
			if (!commonConfigMap.containsKey(ent.getValue().getGrade())) {
				RollCardConfigHelper helper = new RollCardConfigHelper();
				commonConfigMap.put(ent.getValue().getGrade(), helper);
			}
			RollCardConfigHelper helper = commonConfigMap.get(ent.getValue().getGrade());
			helper.addCommon(ent.getValue().getReward());
		}

		this.poolList = poolList;
		loadDbBuilder();
		this.configMap = configMap;
		this.commonConfigMap = commonConfigMap;
		this.gradeWeightMap = gradeWeightMap;
		this.gradeTotalWeightMap = gradeTotalWeightMap;
		return true;
	};

	public void getPanel(String playerId, int type) {
		SC_RollCardPanel.Builder builder = SC_RollCardPanel.newBuilder();
		int poolId = 1;//
		targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
		if (entity == null) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCardPanel_VALUE, builder);
			return;
		}
		long weekResetTime = TimeUtil.getToWeekResetStamp(GlobalTick.getInstance().getCurrentTime());
		DB_RollCard rollGodTemp = entity.getDb_Builder().getRollGodTemp();
		if (rollGodTemp.getFreshTime() == 0) {
			SyncExecuteFunction.executeConsumer(entity, e -> {
				Builder rollBuilder = rollGodTemp.toBuilder().setFreshTime(weekResetTime);
				entity.getDb_Builder().setRollGodTemp(rollBuilder.build());
			});
		} else {
			if (rollGodTemp.getFreshTime() != weekResetTime) {
				SyncExecuteFunction.executeConsumer(entity, e -> {
					Builder rollBuilder = rollGodTemp.toBuilder().clearBase().clearDaily().clearChange().clearOne().clearTen().setFreshTime(weekResetTime);
					entity.getDb_Builder().setRollGodTemp(rollBuilder.build());
				});
			}
		}

		RollCardOne.Builder rb = RollCardOne.newBuilder();
		rb.setPool(poolId);
		rb.setToday(rollGodTemp.getBase().getToday());
		rb.setTotal(rollGodTemp.getBase().getTotal());
		rb.setType(type);
		rb.setRollLuckTimes(rollGodTemp.getBase().getRollLuckTimes());
		rb.setNextFreeTime(0);
		builder.setBaseInfo(rb);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCardPanel_VALUE, builder);
	}

	public void change(String playerId, int pool) {
		SC_RollCardChange.Builder builder = SC_RollCardChange.newBuilder();
		targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
		if (entity == null) {
			builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Failure));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCardChange_VALUE, builder);
			return;
		}
		if (RollCard.getById(pool) == null) {
			builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCardChange_VALUE, builder);
			return;
		}
		DB_RollCard rollGodTemp = entity.getDb_Builder().getRollGodTemp();
		if (rollGodTemp.getChange() != 0) {
			builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Failure));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCardChange_VALUE, builder);
			return;
		}
		SyncExecuteFunction.executeFunction(entity, temp -> {
			DB_RollCard.Builder rollGodTempBuilder = entity.getDb_Builder().getRollGodTempBuilder();
			rollGodTempBuilder.getBaseBuilder().setPool(pool);
			entity.getDb_Builder().setRollGodTemp(rollGodTemp);
			return RetCodeEnum.RCE_Success;
		});
		getPanel(playerId, RollCardType.R_GODTEMP_VALUE);
		builder.setRetCode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success));
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCardChange_VALUE, builder);
	}

	public void roll(String playerId, int type, int num, int cost) {
		SC_RollCard.Builder builder = SC_RollCard.newBuilder();
		targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
		if (entity == null) {
			builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCard_VALUE, builder);
			return;
		}
		DB_RollCard.Builder rollGodTemp = entity.getDb_Builder().getRollGodTempBuilder();
		int curPlId = this.curPoolId;
		if (rollGodTemp.getChange() != 0) {
			curPlId = rollGodTemp.getBase().getPool();
		}
		RollCardObject config = RollCard.getById(curPlId);
		if (config == null) {
			builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCard_VALUE, builder);
			return;
		}
		if (config.getLuckpool() == null || config.getLuckpool().length < 2) {
			builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCard_VALUE, builder);
			return;
		}

		int rollNum = num > 0 ? 10 : 1;
		int[] consumeConfig = null;
		if (cost == 0) {
			if (rollNum == 1) {
				consumeConfig = config.getCost1();
			} else {
				consumeConfig = config.getCost10();
			}
		} else {
			if (rollNum == 1) {
				consumeConfig = config.getOthercost1();
			} else {
				consumeConfig = config.getOthercost10();
			}

		}
		Consume consume = ConsumeUtil.parseConsume(consumeConfig);
		if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_New))) {
			builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCard_VALUE, builder);
			return;
		}
		boolean firstTen = rollGodTemp.getOne().getFirstTen() == 0;

		boolean dailyFirstTem = rollGodTemp.getDaily().getFirstTen() == 0;

		List<Integer> resultGradeList = new ArrayList<>();
		int luckCountTimes = rollGodTemp.getBase().getRollLuckTimes();
		if (firstTen) {// 第一次十连,无核心魔灵,最少一张紫卡
			List<Integer> tenGradeList = getOneGradeWithOutIgnore(curPlId, rollNum, createIgnoreList(7));
			if (tenGradeList.size() == 0) {
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCard_VALUE, builder);
				return;
			}
			if (!tenGradeList.contains(5)) {// 没有紫卡,随机替换一个
				tenGradeList.set(new Random().nextInt(tenGradeList.size()), 5);
			}
			resultGradeList = tenGradeList;
			luckCountTimes += rollNum;
		} else {
			List<Integer> ignoreList = new ArrayList<>();
			for (int i = 0; i < rollNum; i++) {
				luckCountTimes++;
				int luckGrade = computeBestLuckGrade(curPlId, luckCountTimes);
				if (luckGrade > 0) {// 有保底
					resultGradeList.add(luckGrade);
					luckCountTimes = 0;
				} else {
					resultGradeList.add(getOneGradeWithOutIgnore(curPlId, ignoreList));
				}
			}
		}

//		LogUtil.info("RollCard grade Result:" + resultGradeList);

		List<Reward> fragList = new ArrayList<>();
		List<Reward> petList = new ArrayList<>(); // 暂时分开，后续优化
		for (Integer grade : resultGradeList) {
			RollCardConfigHelper configHelper = getConfigHelper(curPlId, grade);
			if (configHelper == null) {
				continue;
			}
			Reward roll = configHelper.roll();
			if (roll != null) {
				if (checkIsPet(roll.getId(), roll.getCount())) {
					petList.add(roll);
				} else {
					fragList.add(roll);
				}
			}
		}

		final int tmpRollTimes = luckCountTimes;
		SyncExecuteFunction.executeConsumer(entity, temp -> {
			DB_RollCard.Builder rollGodTempBuilder = temp.getDb_Builder().getRollGodTempBuilder();
			if (firstTen) {
				rollGodTempBuilder.getOneBuilder().setFirstTen(1);
			}
			int totalTimes = rollGodTempBuilder.getBase().getTotal();
			int todayTimes = rollGodTempBuilder.getBase().getToday();
			rollGodTempBuilder.getBaseBuilder().setTotal(totalTimes + rollNum);
			rollGodTempBuilder.getBaseBuilder().setToday(todayTimes + rollNum);
			rollGodTempBuilder.getBaseBuilder().setRollLuckTimes(tmpRollTimes);
		});
		for (Reward reward : petList) {
			frag2Pet(reward.getId(), reward.getCount(), fragList);
		}
//		LogUtil.info("RollCard showList:"+ fragList);
		RewardManager.getInstance().doRewardByList(playerId, fragList, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_New), false);
		builder.addAllRewards(fragList);
		builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
		getPanel(playerId, type);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_RollCard_VALUE, builder);
	}

	public RollCardConfigHelper getConfigHelper(int id, int grade) {
		if (grade != 5 && grade!= 7) {// 基础卡池, 6未算高级卡池,后续在检查
			if (!commonConfigMap.containsKey(grade)) {
				return null;
			}
			return commonConfigMap.get(grade);
		} else {
			if (!configMap.containsKey(id)) {
				return null;
			}
			return configMap.get(id).getOrDefault(grade, null);
		}
	}

	private RollCardConfigHelper poolInit(int[][] poolConfig) {
		RollCardConfigHelper helper = new RollCardConfigHelper();
		for (int[] rewardConfig : poolConfig) {
			helper.addHigh(rewardConfig);
		}
		return helper;
	}

	private Map<Integer, Integer> formatGradeConfig(int[][] gradeConfig) {
		Map<Integer, Integer> map = new HashMap<>();

		for (int[] e : gradeConfig) {
			if (e.length < 2) {
				continue;
			}
			map.put(e[0], e[1]);
		}
		return map;
	}

	private int countValue(Map<? extends Object, Integer> map) {
		int count = 0;
		for (Integer i : map.values()) {
			count += i;
		}
		return count;
	}

	private List<Integer> createIgnoreList(Integer... grades) {
		ArrayList<Integer> list = new ArrayList<>(Arrays.asList(grades));
		return list;
	}

	private List<Integer> getOneGradeWithOutIgnore(int id, int times, List<Integer> ignoreList) {
		if (!gradeWeightMap.containsKey(id)) {
			return new ArrayList<>();
		}
		List<Integer> gradeList = new ArrayList<>();
		Map<Integer, Integer> map = gradeWeightMap.get(id);

		Map<Integer, Integer> rollMap;
		if (ignoreList.size() > 0) {
			rollMap = removeIgnore(map, ignoreList);
		} else {
			rollMap = map;
		}
		int totalWeight = countValue(rollMap);
		for (int i = 0; i < times; i++) {
			gradeList.add(rollMap(rollMap, totalWeight));
		}
		return gradeList;
	}

	private int getOneGradeWithOutIgnore(int id, List<Integer> ignoreList) {
		if (!gradeWeightMap.containsKey(id)) {
			return 0;
		}
		Map<Integer, Integer> map = gradeWeightMap.get(id);
		if (ignoreList.size() > 0) {
			Map<Integer, Integer> temMap = removeIgnore(map, ignoreList);
			int totalWeight = countValue(temMap);
			return rollMap(temMap, totalWeight);
		} else {
			return rollMap(map, gradeTotalWeightMap.get(id));
		}
	}

	private Map<Integer, Integer> removeIgnore(Map<Integer, Integer> map, List<Integer> ignoreList) {
		Map<Integer, Integer> temMap = new HashMap<>();
		for (Entry<Integer, Integer> ent : map.entrySet()) {
			if (ignoreList.contains(ent.getKey())) {
				continue;
			}
			temMap.put(ent.getKey(), ent.getValue());
		}
		return temMap;
	}

	private int rollMap(Map<Integer, Integer> map) {
		int totalWeight = countValue(map);
		return rollMap(map, totalWeight);
	}

	private int rollMap(Map<Integer, Integer> map, int totalWeight) {
		int luck = new Random().nextInt(totalWeight) + 1;

		for (Entry<Integer, Integer> ent : map.entrySet()) {
			if (ent.getValue() >= luck) {
				return ent.getKey();
			}
			luck -= ent.getValue();
		}
		return 0;
	}

	private int add(int num1, int num2) {
		int max = Integer.MAX_VALUE - num1;
		if (num2 > max) {
			num2 = max;
		}
		return num1 + num2;
	}

	/**
	 * 计算优先保底
	 * 
	 * @return
	 */
	private int computeBestLuckGrade(int curPlId, int curRollTimes) {
		RollCardObject config = RollCard.getById(curPlId);
		if (config == null) {
			return 0;
		}
		if (config.getLuckpool() == null || config.getLuckpool().length < 2) {
			return 0;
		}

		if (curRollTimes < config.getLuckpool()[1]) {
			return 0;
		}
		return config.getLuckpool()[0];
	}

	private boolean frag2Pet(int fragmentId, int amount, List<Reward> rewards) {
		PetFragmentConfigObject fragmentConfig = PetFragmentConfig.getById(fragmentId);
		if (fragmentConfig == null) {
			return false;
		}
		int petid = fragmentConfig.getPetid();
		PetBasePropertiesObject petConfig = PetBaseProperties.getByPetid(petid);
		if (petConfig == null) {
			return false;
		}
		if (petConfig.getStartrarity() < GameConfig.getById(GameConst.CONFIG_ID).getRoll_frag2pet_grade()) {
			return false;
		}
		if (amount < fragmentConfig.getAmount()) {
			return false;
		}
		rewards.add(RewardUtil.parseReward(RewardTypeEnum.RTE_Pet, fragmentConfig.getPetid(), 1));
		return true;
	}

	private boolean checkIsPet(int fragmentId, int count) {
		PetFragmentConfigObject fragmentCfg = PetFragmentConfig.getById(fragmentId);
		if (fragmentCfg == null) {
			return false;
		}
		// 不能合成一个完整的魔灵不播放
		if (count < fragmentCfg.getAmount()) {
			return false;
		}
		if(fragmentCfg.getDebrisrarity() <= GameConfig.getById(GameConst.CONFIG_ID).getRoll_frag2pet_grade()) {
			return false;
		}
		return true;
	}

	private void loadDbBuilder() {
		gameplayEntity entity = gameplayCache.getInstance().getByGamePlayType(GameplayDB.GameplayTypeEnum.GTE_RollCard);
		if (entity != null && entity.getGameplayinfo() != null) {
			try {
				this.dbBuilder = GameplayDB.DB_RollCardInfo.parseFrom(entity.getGameplayinfo()).toBuilder();
			} catch (InvalidProtocolBufferException e) {
				LogUtil.printStackTrace(e);
			}
		}
		long toWeekResetStamp = TimeUtil.getToWeekResetStamp(GlobalTick.getInstance().getCurrentTime());
		if (this.dbBuilder == null) {
			this.dbBuilder = GameplayDB.DB_RollCardInfo.newBuilder().setPreRefreshTime(toWeekResetStamp).setIndex(0);
			this.curPoolId = poolList.get(0);
			updateGamePlayInfo();
		} else {
			if (dbBuilder.getIndex() < 0 || dbBuilder.getIndex() >= poolList.size()) {
				this.curPoolId = poolList.get(0);
				updateGamePlayInfo();
			} else {
				this.curPoolId = poolList.get(dbBuilder.getIndex());
			}
		}
	}

	public GameplayDB.DB_RollCardInfo.Builder getDbBuilder() {
		return this.dbBuilder;
	}

	private void updateGamePlayInfo() {
		gameplayEntity entity = gameplayCache.getInstance().getByGamePlayType(GameplayDB.GameplayTypeEnum.GTE_RollCard);
		entity.setGameplayinfo(this.dbBuilder.build().toByteArray());
		gameplayCache.put(entity);
	}

	public void updateWeek() {
		long toWeekResetStamp = TimeUtil.getToWeekResetStamp(GlobalTick.getInstance().getCurrentTime());
		if (this.dbBuilder == null) {
			return;
		}
		if (this.dbBuilder.getPreRefreshTime() != toWeekResetStamp) {
			this.dbBuilder.setPreRefreshTime(toWeekResetStamp);
			int index = this.dbBuilder.getIndex();
			index++;
			if (index >= poolList.size()) {
				index = 0;
			}
			this.dbBuilder.setIndex(index);
			this.curPoolId = poolList.get(index);
		}
	}
}
