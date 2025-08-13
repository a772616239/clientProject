package model.patrol.dbCache.service;

import cfg.FunctionOpenLvConfig;
import cfg.GameConfig;
import cfg.PatrolBuffDetail;
import cfg.PatrolBuffDetailObject;
import cfg.PatrolConfig;
import cfg.PatrolDiffculty;
import cfg.PatrolDiffcultyObject;
import cfg.PatrolGreed;
import cfg.VIPConfig;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import entity.CommonResult;
import entity.RewardResult;

import java.util.ArrayList;
import java.util.List;

import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.mainLine.dbCache.mainlineCache;
import model.patrol.dbCache.patrolCache;
import model.patrol.entity.PatrolBattleResult;
import model.patrol.entity.PatrolChildTree;
import model.patrol.entity.PatrolExploreResult;
import model.patrol.entity.PatrolFinishResult;
import model.patrol.entity.PatrolInitResult;
import model.patrol.entity.PatrolMoveResult;
import model.patrol.entity.PatrolPurchaseResult;
import model.patrol.entity.PatrolTree;
import model.patrol.entity.patrolEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.team.dbCache.teamCache;
import org.springframework.util.CollectionUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.GamePlayLog;
import platform.logs.entity.PatrolLog;
import platform.logs.entity.PatrolLog.PatrolEvent;
import protocol.Battle.PetBuffData;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId;
import protocol.Patrol;
import protocol.Patrol.PatrolChamber;
import protocol.Patrol.PatrolChamberEvent;
import protocol.Patrol.PatrolMap;
import protocol.Patrol.PatrolPoint;
import protocol.Patrol.PatrolSearchEvent;
import protocol.Patrol.PatrolStatus;
import protocol.PetMessage.Pet;
import protocol.PrepareWar;
import protocol.PrepareWar.TeamNumEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TargetSystemDB;
import util.EventUtil;
import util.GameUtil;
import util.PatrolUtil;
import util.RandomUtil;

import static model.patrol.entity.PatrolTree.EVENT_BOSS;
import static model.patrol.entity.PatrolTree.EVENT_CHAMBER;
import static model.patrol.entity.PatrolTree.EVENT_EXPLORE;
import static model.patrol.entity.PatrolTree.EVENT_PATH;
import static model.patrol.entity.PatrolTree.EVENT_TREASURE;
import static util.PatrolUtil.toPet;

/**
 * @author xiao_FL
 * @date 2019/8/1
 */
public class PatrolServiceImpl implements IPatrolService {
	private static final IPatrolService patrolService = new PatrolServiceImpl();

	public static IPatrolService getInstance() {
		return patrolService;
	}

	private final patrolCache patrolCacheInstance = patrolCache.getInstance();

	private static final int ADD_GREED = 1;
	private static final int ADD_TREASURE = 2;

	@Override
	public PatrolInitResult patrolMapInit(String playerId) {
		PatrolInitResult result = new PatrolInitResult();
		playerEntity player = playerCache.getByIdx(playerId);
		if (player != null && !player.functionUnLock(EnumFunction.Patrol)) {
			result.setCode(RetCodeEnum.RCE_FunctionNotUnLock);
			return result;
		}
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache != null && cache.gameNotFinished()) {
			// 探索未结束，返回上次探索信息
			tree2Map(result, cache.getPatrolTree());
			SyncExecuteFunction.executeConsumer(cache, cacheTemp -> cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().setTime(GlobalTick.getInstance().getCurrentTime()).build()));
			result.setPatrolStatus(cache.getPatrolStatusEntity());
			result.setNewGame(false);
			result.setSuccess(true);
		} else {
			// 每日首次免费
			if (!todayFirstPlay(cache)) {
				// 扣除物品开始游戏
				/*
				 * Consume consume =
				 * ConsumeUtil.parseConsume(PatrolConfig.getById(1).getPatrolconsume()); if
				 * (!ConsumeManager.getInstance().consumeMaterial(playerId, consume,
				 * ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Patrol))) {
				 * result.setCode(RetCodeEnum.RCE_Itembag_ItemNotEnought); }
				 */
				result.setCode(RetCodeEnum.RCE_Patrol_TodayNotFinishPlay);

			}

			cache = upsertPatrolEntityByRandomConfig(playerId, cache);
			LogService.getInstance().submit(new PatrolLog(playerId, cache.getPatrolStatusEntity(), null, 0, PatrolEvent.ENTER, 0, 0));
			tree2Map(result, cache.getPatrolTree());
			result.setPatrolStatus(cache.getPatrolStatusEntity());
			result.setNewGame(true);
			result.setSuccess(true);

			// 玩法统计:巡逻队
			LogService.getInstance().submit(new GamePlayLog(playerId, EnumFunction.Patrol));

			// 初始化玩家难度,放置每日更新时更新
//            EventUtil.recreateMonsterDiff(playerId, EnumFunction.Patrol);
		}
		return result;
	}

	@Override
	public boolean todayFirstPlay(patrolEntity patrolEntity) {
		if (patrolEntity == null) {
			return true;
		}
		if (patrolEntity.getTodayCreateCount() == 0) {
			return true;
		}
		return false;
	}

	public void sendPatrolInit(String playerId) {
		targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
		if (target == null) {
			return;
		}
		TargetSystemDB.DB_PatrolMission patrolMission = target.getDb_Builder().getPatrolMission();
		if (!target.getDb_Builder().hasPatrolMission()) {
			return;
		}
		Patrol.SC_PatrolMissionInit.Builder msg = Patrol.SC_PatrolMissionInit.newBuilder();
		msg.setEndTime(target.getPatrolMissionEndTime());
		msg.setMission(patrolMission.getMission());
		if (patrolMission.getMission().getStatus() == Common.MissionStatusEnum.MSE_Finished) {
			msg.setRewardUp(patrolMission.getRewardUp());
		}
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_PatrolMissionInit_VALUE, msg);
	}

	private patrolEntity upsertPatrolEntityByRandomConfig(String playerId, patrolEntity cache) {
		int mapId = randomMapId();
		int cfgId = randomMapConfig();
		cache = upsertPatrolEntity(playerId, cache, cfgId, mapId);
		return cache;
	}

	public patrolEntity upsertPatrolEntity(String playerIdx, patrolEntity cache, int patrolCfg, int mapId) {
		if (cache == null) {
			// 玩家第一次探索，初始化
			cache = new patrolEntity(playerIdx, patrolCfg, mapId);
			// 埋点日志
		} else {
			// 重新生成地图，初始化状态
			cache.reCreate(patrolCfg, mapId);
			// 埋点日志
		}
		patrolCacheInstance.flush(cache);
		return cache;
	}

	private int randomMapConfig() {
		return RandomUtil.getRandomValue(1, PatrolConfig._ix_id.size());
	}

	private int randomMapId() {
		return RandomUtil.getRandomValue(1, cfg.PatrolMap._ix_mapid.size());
	}

	/**
	 * 将地图信息转换成给客户端商定结构
	 *
	 * @param result 转换地图结果
	 * @param tree   存储的地图信息
	 */
	private void tree2Map(PatrolInitResult result, PatrolTree tree) {
		// 主线线路
		PatrolChildTree mainBranch = new PatrolChildTree();
		PatrolTree.getMainChildTree(tree, mainBranch);
		List<PatrolPoint> mainList = new ArrayList<>(mainBranch.getBranch());
		PatrolMap.Builder map = PatrolMap.newBuilder();
		map.setMainLine(pointList2Line(mainList));
		// 分支路线
		for (PatrolTree patrolTree : mainBranch.getDegreeMap().keySet()) {
			for (int i = 1; i <= mainBranch.getDegreeMap().get(patrolTree); i++) {
				List<PatrolPoint> branchList = PatrolTree.getMainChildTree(patrolTree, i);
				map.addBranchLine(pointList2Line(branchList));
			}
		}
		result.setPatrolMap(map.build());
	}

	private static Patrol.PatrolLine pointList2Line(List<PatrolPoint> points) {
		return Patrol.PatrolLine.newBuilder().addAllPoint(points).build();
	}

	@Override
	public PatrolMoveResult move(String playerId, PatrolPoint point) {
		PatrolMoveResult result = new PatrolMoveResult();
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache == null || point == null) {
			result.setCode(RetCodeEnum.RCE_ErrorParam);
			return result;
		}
		// 当前位置（仅含坐标信息）
		if (cache.gameFailed()) {
			result.setCode(RetCodeEnum.RCE_Patrol_FailureError);
			return result;
		}
		PatrolPoint location = cache.getPatrolStatusEntity().getLocation();
		PatrolTree locationPoint = new PatrolTree(location.getX(), location.getY());
		// 前进点位置（仅含坐标信息）
		PatrolTree goal = PatrolUtil.preOrderByLocation(cache.getPatrolTree(), new PatrolTree(point.getX(), point.getY()));
		locationPoint = PatrolUtil.preOrderByLocation(cache.getPatrolTree(), locationPoint);
		if (locationPoint == null) {
			result.setCode(RetCodeEnum.RCE_ErrorParam);
			return result;
		}
		boolean movable = goal != null && goal.ifReachable() && goal.getPointType() != EVENT_PATH;
		// 目标点是否可达
		if (!movable) {
			result.setCode(RetCodeEnum.RCE_Patrol_NotExplored);
			return result;
		}
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().setLocation(PatrolUtil.getPointByTree(goal)).build());
			patrolCacheInstance.flush(cacheTemp);
			result.setSuccess(true);
			result.setLocation(PatrolUtil.getPointByTree(goal));
		});
		return result;
	}

	@Override
	public CommonResult finish(String playerId) {
		CommonResult result = new CommonResult();
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache != null && cache.gameNotFinished()) {
			SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
				// 重置奖励展示标记
				cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().setDisplayReward(0).build());
				// 标记结束
				cacheTemp.setFinish(1);
				patrolCacheInstance.flush(cacheTemp);
			});
			// 清空编队信息
			teamCache.getInstance().clearTeamPetAndSkill(playerId, TeamNumEnum.TNE_Patrol_1, true);

			PatrolTree bossPoint = PatrolUtil.preOrder4Boss(cache.getPatrolTree());
			assert bossPoint != null;
			// 发放奖励
			settlePatrolReward(playerId, cache, bossPoint.ifExplored());

			result.setSuccess(true);
			clearPatrolTask(playerId);

		} else {
			result.setCode(RetCodeEnum.RCE_ErrorParam);
		}
		return result;
	}

	@Override
	public PatrolFinishResult patrolStatus(String playerId) {
		PatrolFinishResult result = new PatrolFinishResult();
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache != null) {
			result.setFinish(cache.getFinish());
			result.setTodayCreateCount(cache.getTodayCreateCount());
		} else {
			// 玩家第一次请求巡逻队，默认上次游戏结束
			result.setFinish(1);
			result.setTodayCreateCount(0);
		}
		result.setSuccess(true);
		return result;
	}

	@Override
	public void reborn(String playerId, Patrol.SC_PatrolReborn.Builder result) {
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache == null) {
			result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
			return;
		}
		// 免费重生
		if (cache.getPatrolStatusEntity().getFreeReborn() > 0) {
			SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
				cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().setFreeReborn(cacheTemp.getPatrolStatusEntity().getFreeReborn() - 1).setNowFailure(0).setGreed(getRebornGreed(cache.getPatrolStatusEntity()))
						// 这里免费重生不计算在每天的限制重生次数中，补偿失败次数
						.setFailure(cacheTemp.getPatrolStatusEntity().getFailure() - 1).build());
				patrolCacheInstance.flush(cacheTemp);
				result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
				cacheTemp.sendPatrolUpdate();
			});
		}
		// 付费重生
		else {
			if (cache.getPatrolStatusEntity().getFailure() <= PatrolConfig.getById(1).getReborntime()) {
				// 检查玩家道具
				Consume consume = ConsumeUtil.parseConsume(PatrolConfig.getById(1).getChallengeagain());
				if (ConsumeManager.getInstance().consumeMaterial(playerId, consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Patrol))) {
					SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
						cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().setTime(GlobalTick.getInstance().getCurrentTime()).setNowFailure(0).setGreed(getRebornGreed(cache.getPatrolStatusEntity())).build());
						patrolCacheInstance.flush(cacheTemp);
						result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
					});
				} else {
					result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Itembag_ItemNotEnought));
				}
			} else {
				result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Patrol_DailyMaxFailure));
			}
		}
		result.setGreed(cache.getPatrolStatusEntity().getGreed());
	}

	/**
	 * 获取重生后的贪婪值 不为零的情况下当前-25
	 *
	 * @param patrolStatus
	 * @return
	 */
	private int getRebornGreed(PatrolStatus patrolStatus) {
		int greed = 0;
		if (patrolStatus != null) {
			greed = patrolStatus.getGreed() - PatrolConfig.getById(1).getIrritategreed();
		}
		return Math.max(greed, 0);
	}

	@Override
	public PatrolExploreResult explore(String playerId, PatrolSearchEvent event, PatrolPoint forward) {
		PatrolExploreResult result = new PatrolExploreResult();
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache == null || event == null || forward == null) {
			result.setCode(RetCodeEnum.RCE_ErrorParam);
			return result;
		}
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			// 查询目标点
			PatrolTree goal = PatrolUtil.preOrderByLocation(cacheTemp.getPatrolTree(), new PatrolTree(forward.getX(), forward.getY()));
			if (cacheTemp.gameFailed()) {
				result.setCode(RetCodeEnum.RCE_Patrol_FailureError);
				return;
			}
			if (goal != null && goal.ifReachable() && goal.getPointType() == EVENT_EXPLORE) {
				// 贪婪值变化
				int greed;
				if (event == PatrolSearchEvent.explore1) {
					greed = cacheTemp.getPatrolStatusEntity().getGreed() + PatrolConfig.getById(1).getExplore1greed();
				} else if (event == PatrolSearchEvent.explore2) {
					greed = cacheTemp.getPatrolStatusEntity().getGreed() + PatrolConfig.getById(1).getExplore2greed();
				} else {
					greed = cacheTemp.getPatrolStatusEntity().getGreed() + PatrolConfig.getById(1).getExplore3greed();
				}
				greed = Math.min(greed, PatrolConfig.getById(1).getMaxgreed());
				greed = Math.max(greed, PatrolConfig.getById(1).getMingreed());
				// 设置本节点和路径点探索状态
				goal.setExplored(1);
				// 这个parentPoint好像是以前的路径点，现在已经不存在，需沟通验证去除逻辑，下同
				goal.getParentPoint().setExplored(1);
				// 获取效果
				int newStatus = PatrolUtil.patrolEffectList(goal, event);
				PatrolBuffDetailObject buff = PatrolBuffDetail.getByBuffid(newStatus);
				greed = handlerNewBuff(cache, cacheTemp, greed, buff);
				// 设置点状态
				PatrolStatus.Builder builder = cacheTemp.getPatrolStatusEntity().toBuilder().setLocation(cacheTemp.getPatrolStatusEntity().getLocation().toBuilder().clear().setExplored(1).setX(goal.getX()).setY(goal.getY()).build()).addStatus(newStatus).addStatusStartBastard(cacheTemp.getPatrolStatusEntity().getBastardCount()).setGreed(greed);

				cacheTemp.setPatrolStatusEntity(builder.build());

				patrolCacheInstance.flush(cacheTemp);
				result.setStatus(newStatus);
				result.setAllStatus(cacheTemp.getPatrolStatusEntity().getStatusList());
				result.setGreed(cacheTemp.getPatrolStatusEntity().getGreed());
				result.setLocation(cacheTemp.getPatrolStatusEntity().getLocation());
				result.setSuccess(true);
				// 埋点日志
				LogService.getInstance().submit(new PatrolLog(playerId, cache.getPatrolStatusEntity(), null, greed, PatrolEvent.EXPLORE, goal.getX(), goal.getY()));
			} else {
				result.setCode(RetCodeEnum.RCE_ErrorParam);
			}
		});
		return result;
	}

	private int handlerNewBuff(patrolEntity cache, patrolEntity cacheTemp, int greed, PatrolBuffDetailObject buff) {
		if (buff.getEffecttype() == ADD_GREED) {
			greed = increaseGreedByBuff(greed, buff);
		} else if (buff.getEffecttype() == ADD_TREASURE) {
			increaseRewardByBuff(cacheTemp, buff);
			// 推送奖励更新消息
			cache.sendRewardRefreshMsg();
		}
		return greed;
	}

	private int increaseGreedByBuff(int greed, PatrolBuffDetailObject buff) {
		return greed * (1 + buff.getEffectratge()) / 100;
	}

	private void increaseRewardByBuff(patrolEntity cacheTemp, PatrolBuffDetailObject buff) {
		List<Reward> rewardList = cacheTemp.getPatrolStatusEntity().getRewardList();
		List<Reward> newRewardList = new ArrayList<>();
		for (Reward reward : rewardList) {
			newRewardList.add(reward.toBuilder().setCount(reward.getCount() * (100 + buff.getEffectratge()) / 100).build());
		}
		cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().clearReward().addAllReward(newRewardList).build());
	}

	@Override
	public PatrolMoveResult treasure(String playerId, PatrolPoint forward, boolean buy) {
		PatrolMoveResult result = new PatrolMoveResult();
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		playerEntity playerEntity = playerCache.getByIdx(playerId);
		if (cache == null || forward == null || playerEntity == null) {
			result.setCode(RetCodeEnum.RCE_ErrorParam);
			return result;
		}
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			// 查询目标点
			PatrolTree goal = PatrolUtil.preOrderByLocation(cacheTemp.getPatrolTree(), new PatrolTree(forward.getX(), forward.getY()));
			if (goal == null || !goal.ifReachable() || goal.getPointType() != EVENT_TREASURE) {
				result.setCode(RetCodeEnum.RCE_ErrorParam);
				return;
			}
			if (!beforeTreasureCheck(playerId, buy, result, cacheTemp, goal)) {
				return;
			}
			List<Reward> rewardList = PatrolUtil.patrolTreasure(playerId, cacheTemp.getPatrolStatusEntity().getGreed(), mainlineCache.getInstance().getCurOnHookNode(playerId), goal.getTreasureGreedConfig() > 0);
			// 设置本节点和路径点探索状态
			goal.setExplored(1);
			goal.getParentPoint().setExplored(1);
			cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().clearReward().addAllReward(RandomUtil.mergeReward(cacheTemp.getPatrolStatusEntity().getRewardList(), rewardList)).build());
			// 设置当前点状态
			cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().setLocation(cacheTemp.getPatrolStatusEntity().getLocation().toBuilder().clear().setExplored(1).setX(goal.getX()).setY(goal.getY()).build()).build());
			patrolCacheInstance.flush(cacheTemp);
			result.setSuccess(true);
			result.setLocation(cacheTemp.getPatrolStatusEntity().getLocation());
			// 通知奖励消息
			GlobalData.getInstance().sendDisRewardMsg(playerId, rewardList, RewardSourceEnum.RSE_Patrol);
			// 推送奖励更新消息
			cache.sendRewardRefreshMsg();
			// 埋点日志
			LogService.getInstance().submit(new PatrolLog(playerId, cacheTemp.getPatrolStatusEntity(), rewardList, 0, PatrolEvent.BOX, goal.getX(), goal.getY()));

			// 目标：秘境探索打开箱子x次
			EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TEE_Patrol_OpenBox, 1, 0);
		});
		return result;
	}

	private boolean beforeTreasureCheck(String playerId, boolean buy, PatrolMoveResult result, patrolEntity cacheTemp, PatrolTree goal) {
		if (cacheTemp.gameFailed()) {
			result.setCode(RetCodeEnum.RCE_Patrol_FailureError);
			return false;
		}
		if (buy) {
			// 购买打开宝箱
			int[] price = GameConfig.getById(GameConst.CONFIG_ID).getResopeningtreasure();
			Consume consume = ConsumeUtil.parseConsume(price);
			if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Patrol))) {
				result.setCode(RetCodeEnum.RCE_MatieralNotEnough);
				return false;
			}

		} else {
			// 贪婪值打开宝箱
			int[][] treasureGreedConfig = GameConfig.getById(GameConst.CONFIG_ID).getTreasuregreedconfig();
			if (treasureGreedConfig.length <= goal.getTreasureGreedConfig()) {
				result.setCode(RetCodeEnum.RSE_ConfigNotExist);
				return false;
			}
			int[] greedConfig = treasureGreedConfig[goal.getTreasureGreedConfig()];
			if (greedConfig.length < 2) {
				return false;
			}
			if (greedConfig[0] == -1 && greedConfig[1] == -1) {
				return true;
			}
			int greed = cacheTemp.getPatrolStatusEntity().getGreed();
			if (greed < greedConfig[0] || greed > greedConfig[1]) {
				result.setCode(RetCodeEnum.RCE_Patrol_DissatisfyGreed);
				return false;
			}
		}
		return true;
	}

	@Override
	public PatrolBattleResult preBattleInfo(String playerId, int irritate) {
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		int lastGreed = cache.getPatrolStatusEntity().getGreed();
		PatrolBattleResult result = new PatrolBattleResult();
		// 检查玩家buff状态和boss增强
		combineBattleBuff(cache, lastGreed, result);
		result.setMonsterExProperty(cache.getPatrolStatusEntity().getMonsterExProperty());
		result.setSuccess(true);
		return result;
	}

	private void combineBattleBuff(patrolEntity cache, int lastGreed, PatrolBattleResult result) {
		if (!CollectionUtils.isEmpty(cache.getPatrolStatusEntity().getStatusList())) {
			// 合并buff效果，传递给前端使用
			PetBuffData.Builder dataBuilder = PetBuffData.newBuilder();
			for (int index = 0; index < cache.getPatrolStatusEntity().getStatusCount(); index++) {
				int buffId = cache.getPatrolStatusEntity().getStatus(index);
				PatrolBuffDetailObject buffConfig = PatrolBuffDetail.getByBuffid(buffId);
				// 非战斗buff跳过
				if (buffConfig.getEffecttype() != 0) {
					continue;
				}
				dataBuilder.clear();
				int nowBastard = cache.getPatrolStatusEntity().getBastardCount();
				int startBastard = getStatusStartBastard(cache, index);
				// 当前buff叠加层数
				int buffCount = getBuffCount(nowBastard - startBastard, buffConfig);
				if (buffCount <= 0) {
					continue;
				}
				dataBuilder.setBuffCount(buffCount);

				dataBuilder.setBuffCfgId(buffId);
				// 判断效果所属阵营
				if (buffConfig.getBuffcamp() == 1) {
					result.addBuff(dataBuilder.build());
				} else if (buffConfig.getBuffcamp() == 0) {
					result.adddebuff(dataBuilder.build());
				}
			}
		}
		// 贪婪值大于等于0触发怪物增强
		if (lastGreed >= 0) {
			PetBuffData.Builder enemyStrengthen = PetBuffData.newBuilder();
			result.adddebuff(enemyStrengthen.setBuffCfgId(PatrolGreed.getByGreed(lastGreed).getStrengthen()).build());
		}
		// 前几场战斗触发怪物减弱
		int fightCount = cache.getPatrolStatusEntity().getBastardCount() + 1;
		PatrolDiffcultyObject config = PatrolDiffculty.getByBastard(fightCount);
		if (config != null && config.getBuffid() > 0) {
			PetBuffData.Builder enemyStrengthen = PetBuffData.newBuilder();
			result.adddebuff(enemyStrengthen.setBuffCfgId(config.getBuffid()).build());
		}
	}

	@Override
	public PatrolBattleResult getFightMakeId(String playerId, int x, int y, int irritate) {
		PatrolBattleResult result = new PatrolBattleResult();
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		// 检查当前点
		if (cache == null) {
			result.setCode(RetCodeEnum.RCE_ErrorParam);
			return result;
		}
		if (cache.gameFailed()) {
			result.setCode(RetCodeEnum.RCE_Patrol_FailureError);
			return result;
		}
		PatrolTree goalPoint = PatrolUtil.preOrderByLocation(cache.getPatrolTree(), new PatrolTree(x, y));
		if (goalPoint == null || !goalPoint.ifReachable()) {
			result.setCode(RetCodeEnum.RCE_ErrorParam);
			return result;
		}
		if (goalPoint.ifExplored()) {
			result.setCode(RetCodeEnum.RCE_Patorl_Explored);
			return result;
		}
		if (!goalPoint.ifBattlePoint()) {
			result.setCode(RetCodeEnum.RCE_Patorl_EventFail);
			return result;
		}
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			// 未被触发过的战斗点位置
			// 设置战斗位置
			cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().setBattlePoint(PatrolUtil.getPointByTree(goalPoint)).build());
			// 处理激怒效果
			int greedChange;
			int lastGreed = cacheTemp.getPatrolStatusEntity().getGreed();
			if (irritate == 1) {
				greedChange = Math.min(PatrolConfig.getById(1).getMaxgreed(), lastGreed + PatrolConfig.getById(1).getIrritategreed());
				cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().setBeforeBattleGreed(lastGreed).setGreed(greedChange).setIfIrritate(1).build());
			} else {
                greedChange = 0;
                cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().setIfIrritate(0).setBeforeBattleGreed(lastGreed).build());
            }

			// 检查玩家buff状态和boss增强
			combineBattleBuff(cacheTemp, lastGreed, result);
			result.setMakeId(goalPoint.getFightMakeId());
			result.setMonsterExProperty(cache.getPatrolStatusEntity().getMonsterExProperty());
			result.setSuccess(true);
			patrolCacheInstance.flush(cacheTemp);
			// 埋点日志
			LogService.getInstance().submit(new PatrolLog(playerId, cacheTemp.getPatrolStatusEntity(), null, greedChange, PatrolEvent.ENTER_BATTLE, cacheTemp.getPatrolStatusEntity().getLocation().getX(), cacheTemp.getPatrolStatusEntity().getLocation().getY()));
		});
		return result;
	}

	private int getStatusStartBastard(patrolEntity cacheTemp, int index) {
		if (cacheTemp.getPatrolStatusEntity().getStatusCount() <= index) {
			return cacheTemp.getPatrolStatusEntity().getBastardCount();
		}
		return cacheTemp.getPatrolStatusEntity().getStatusStartBastard(index);
	}

	/**
	 * 获取巡逻队buff层数
	 *
	 * @param buffCount buff层数 * @param buffConfig 当前buff属性
	 */
	private int getBuffCount(int buffCount, PatrolBuffDetailObject buffConfig) {
		// 0为普通类型，1为累计类型，2为单次类型
		if (buffConfig.getBufftype() == 1) {
			int maxBuffCount = buffConfig.getBuffmaxcount();
			// 超过最大叠加层数限制，返回配置的最大层数
			if (maxBuffCount > 0 && buffCount > maxBuffCount) {
				return maxBuffCount;
			}
			return buffCount;
		} else {
			// 非累积型默认层数传1
			return 1;
		}
	}

	@Override
	public List<Reward> getBattleReward(String playerId) {
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache == null || cache.gameFailed()) {
			return null;
		}
		PatrolTree location = PatrolUtil.preOrderByLocation(cache.getPatrolTree(), new PatrolTree(cache.getPatrolStatusEntity().getBattlePoint().getX(), cache.getPatrolStatusEntity().getBattlePoint().getY()));
		if (location == null || !location.ifBattlePoint() || location.ifExplored()) {
			return null;
		}
		return PatrolUtil.patrolBattleReward(playerId, cache.getPatrolStatusEntity().getGreed(), location.getPointType(), mainlineCache.getInstance().getCurOnHookNode(playerId));
	}

	@Override
	public void battleSettle(String playerId, int battleResult, List<Reward> rewardList, PatrolTree location) {
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache == null || cache.gameFailed() || location == null) {
			return;
		}
		// 处理投降(还原状态)
		if (battleResult == 3) {
			settlePatrolSurrender(cache);
			return;
		}
		boolean victory = battleResult == 1;
		String event = victory ? PatrolEvent.BATTLE_VICTORY : PatrolEvent.BATTLE_FAILED;
		int irritate = 0;
		// 胜利处理
		if (victory) {
			PatrolStatus.Builder patrolStatus = buildVictoryPatrolStatus(rewardList, cache, location);
			cache.setPatrolStatusEntity(patrolStatus.build());
			// boss节点
			if (location.getPointType() == EVENT_BOSS) {
				doKillPatrolBoss(playerId, cache);
			}
			// 目标：在贪婪值达到X情况下完成X场战斗
			EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_Patrol_FightAboveGreed, 1, cache.getPatrolStatusEntity().getBeforeBattleGreed());
		} else {
			PatrolStatus.Builder builder = buildFailFightPatrolStatus(cache);
			cache.setPatrolStatusEntity(builder.build());
			rewardList = null;
		}
		patrolCacheInstance.flush(cache);
		LogService.getInstance().submit(new PatrolLog(playerId, cache.getPatrolStatusEntity(), rewardList, irritate, event, location.getX(), location.getY()));
		sendPatrolBattleResult(cache);

	}

	private void settlePatrolSurrender(patrolEntity cache) {
		if (cache.getPatrolStatusEntity().getBeforeBattleGreed() != cache.getPatrolStatusEntity().getGreed()) {
			PatrolStatus.Builder patrolStatus = cache.getPatrolStatusEntity().toBuilder().setGreed(cache.getPatrolStatusEntity().getBeforeBattleGreed());
			cache.setPatrolStatusEntity(patrolStatus.build());
			patrolCacheInstance.flush(cache);
		}
	}

	private void sendPatrolBattleResult(patrolEntity cache) {
		Patrol.SC_PatrolBatlleResult.Builder msg = Patrol.SC_PatrolBatlleResult.newBuilder();
		PatrolStatus patrolStatusEntity = cache.getPatrolStatusEntity();
		msg.setDisplayReward(patrolStatusEntity.getDisplayReward());
		msg.setFailure(patrolStatusEntity.getFailure());
		msg.setGreed(patrolStatusEntity.getGreed());
		msg.setLocation(patrolStatusEntity.getLocation());
		msg.addAllStatus(patrolStatusEntity.getStatusList());
		msg.addAllReward(patrolStatusEntity.getRewardList());
		msg.setNowFailure(patrolStatusEntity.getNowFailure());

		GlobalData.getInstance().sendMsg(cache.getPlayeridx(), MessageId.MsgIdEnum.SC_PatrolBatlleResult_VALUE, msg);
	}

	private PatrolStatus.Builder buildFailFightPatrolStatus(patrolEntity cache) {
		int irritate = 0;
		// 上次战斗激怒过，补偿
		if (cache.getPatrolStatusEntity().getIfIrritate() == 1) {
			irritate = -PatrolConfig.getById(1).getIrritategreed();
		}
		// 修改失败次数和状态
		return cache.getPatrolStatusEntity().toBuilder().setFailure(cache.getPatrolStatusEntity().getFailure() + 1).setTime(GlobalTick.getInstance().getCurrentTime()).setGreed(cache.getPatrolStatusEntity().getGreed() + irritate).setNowFailure(1).setIfIrritate(0);
	}

	private PatrolStatus.Builder buildVictoryPatrolStatus(List<Reward> rewardList, patrolEntity cache, PatrolTree location) {
		// 处理一次性效果列表
		List<Integer> statusList = cache.getPatrolStatusEntity().getStatusList();
		statusList = PatrolUtil.removeDisposableStatus(new ArrayList<>(statusList));
		// 处理本节点和父节点探索情况
		location.setExplored(1);
		location.getParentPoint().setExplored(1);
		PatrolStatus.Builder builder = cache.getPatrolStatusEntity().toBuilder()
				// 修改奖励信息
				.clearReward().addAllReward(RandomUtil.mergeReward(cache.getPatrolStatusEntity().getRewardList(), rewardList))
				// 修改当前点位置
				.setLocation(PatrolUtil.getPointByTree(location)).clearStatus().addAllStatus(statusList)
				// 清除保存战斗点
				.clearBattlePoint()
				// 战斗点胜利记录+1
				.setBastardCount(cache.getPatrolStatusEntity().getBastardCount() + 1);

		if (location.getPointType() == EVENT_BOSS) {
			builder.setDisplayReward(1).setIfIrritate(0);
		}
		return builder;
	}

	private void doKillPatrolBoss(String playerId, patrolEntity cache) {
		// 目标：巡逻队累积击杀boss
		EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_CumuKillPatrolBoss, 1, 0);
		// 目标：指定贪婪值完成秘境探索x次（全量）（额外条件：贪婪值）
		EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TEE_Patrol_SpecialGreedFinish, 1, cache.getPatrolStatusEntity().getGreed());
		teamCache.getInstance().clearTeamPetAndSkill(playerId, PrepareWar.TeamNumEnum.TNE_Patrol_1, true);
	}

	private void settlePatrolReward(String playerId, patrolEntity cache, boolean killBoss) {
		// 未通过boss点发放减半奖励
		int rewardRate = killBoss ? 0 : -(GameConst.commonMagnification - GameConfig.getById(GameConst.CONFIG_ID).getPatrolfailrewardrate());
		List<Reward> rewardList = PatrolUtil.patrolReward(rewardRate, cache.getPatrolStatusEntity().getRewardList());
		List<Reward> rewards = new ArrayList<>(rewardList);
		// 此处不展示奖励，客户端自行处理
		Reward.Builder reward = Reward.newBuilder();
		reward.setId(GameConfig.getById(GameConst.CONFIG_ID).getFeatidlist()[2]);
		reward.setCount(GameConfig.getById(GameConst.CONFIG_ID).getPatrol_join());
		reward.setRewardType(RewardTypeEnum.RTE_Item);
		rewards.add(reward.build());
		RewardManager.getInstance().doRewardByList(playerId, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Patrol), false);
		// 埋点日志
		LogService.getInstance().submit(new PatrolLog(playerId, cache.getPatrolStatusEntity(), cache.getPatrolStatusEntity().getRewardList(), 0, PatrolEvent.FINISHED, cache.getPatrolStatusEntity().getLocation().getX(), cache.getPatrolStatusEntity().getLocation().getY()));
	}

	@Override
	public PatrolPurchaseResult patrolPurchase(String playerId) {
		PatrolPurchaseResult result = new PatrolPurchaseResult();
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache == null) {
			result.setCode(RetCodeEnum.RCE_ErrorParam);
			return result;
		}

		if (ConsumeManager.getInstance().consumeMaterial(playerId, ConsumeUtil.parseConsume(PatrolConfig.getById(1).getTimeroll()), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Patrol))) {

			List<Reward> rewardList = new ArrayList<>();
			Reward.Builder reward = Reward.newBuilder();
			reward.setRewardType(RewardTypeEnum.RTE_Item);
			// 配置数组第二个元素为物品id
			reward.setId(PatrolConfig.getById(1).getChallengeagain()[1]);
			// 加1个
			reward.setCount(1);
			rewardList.add(reward.build());
			RewardManager.getInstance().doRewardByList(playerId, rewardList, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Patrol), false);
			result.setItemCount(1);
			result.setSuccess(true);
		} else {
			result.setCode(RetCodeEnum.RCE_Player_DiamondNotEnought);
		}
		return result;
	}

	@Override
	public boolean vipLevelUp(String playerId, int beforeVipLv, int afterVipLv) {
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache != null) {
			SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
				cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().setFreeReborn(VIPConfig.getById(afterVipLv).getPatrolreborntimes() - VIPConfig.getById(beforeVipLv).getPatrolreborntimes() + cacheTemp.getPatrolStatusEntity().getFreeReborn()).build());
				patrolCacheInstance.flush(cacheTemp);
				cache.sendPatrolUpdate();
			});
		}
		return true;
	}

	@Override
	public RewardResult displayReward(String playerId) {
		RewardResult result = new RewardResult();
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache != null) {
			result.setSuccess(true);
			result.setRewardList(cache.getPatrolStatusEntity().getRewardList());
			return result;
		}
		result.setCode(RetCodeEnum.RCE_ErrorParam);
		return result;
	}

	@Override
	public PatrolStatus getStateByPlayerIdx(String playerIdx) {
		patrolEntity cacheByPlayer = patrolCacheInstance.getCacheByPlayer(playerIdx);
		if (cacheByPlayer != null) {
			return cacheByPlayer.getPatrolStatusEntity();
		}
		return null;
	}

	@Override
	public PatrolMoveResult chooseChamber(String playerId, PatrolPoint forward, PatrolChamberEvent event) {
		PatrolMoveResult result = new PatrolMoveResult();
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache == null || event == null || forward == null) {
			result.setCode(RetCodeEnum.RCE_ErrorParam);
			return result;
		}
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			// 查询目标点
			PatrolTree goal = PatrolUtil.preOrderByLocation(cacheTemp.getPatrolTree(), new PatrolTree(forward.getX(), forward.getY()));
			if (cacheTemp.gameFailed()) {
				result.setCode(RetCodeEnum.RCE_Patrol_FailureError);
				return;
			}
			if (goal != null && goal.ifReachable() && goal.getPointType() == EVENT_CHAMBER && !goal.ifExplored()) {
				PatrolChamber.Builder builder = cacheTemp.getPatrolStatusEntity().getPatrolChamber().toBuilder().addPetList(toPet(goal.getPetList().get(event.getNumber())));
				// 修改状态
				goal.setExplored(1);
				cacheTemp.setPatrolStatusEntity(cacheTemp.getPatrolStatusEntity().toBuilder().setPatrolChamber(builder).setLocation(cacheTemp.getPatrolStatusEntity().getLocation().toBuilder().clear().setX(goal.getX()).setY(goal.getY()).setExplored(1).build()).build());
				patrolCacheInstance.flush(cacheTemp);
				result.setLocation(cacheTemp.getPatrolStatusEntity().getLocation());
				result.setSuccess(true);
			} else {
				result.setCode(RetCodeEnum.RCE_ErrorParam);
			}
		});
		return result;
	}

	@Override
	public Pet getVirtualPet(String playerId, String petId) {
		patrolEntity cache = patrolCacheInstance.getCacheByPlayer(playerId);
		if (cache == null || cache.getPatrolStatusEntity() == null || CollectionUtils.isEmpty(cache.getPatrolStatusEntity().getPatrolChamber().getPetListList())) {
			return null;
		}
		for (Pet pet : cache.getPatrolStatusEntity().getPatrolChamber().getPetListList()) {
			if (pet.getId().equals(petId)) {
				return pet;
			}
		}
		return null;
	}

	private void clearPatrolTask(String playerId) {
		targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
		if (target == null) {
			return;
		}

		SyncExecuteFunction.executeConsumer(target, cacheTemp -> {
			target.getDb_Builder().clearPatrolMission();
		});
	}
}
