package model.training;

import cfg.GameConfig;
import cfg.Item;
import cfg.ItemObject;
import cfg.Mission;
import cfg.MissionObject;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.TrainingBuff;
import cfg.TrainingBuffObject;
import cfg.TrainingLuck;
import cfg.TrainingLuckHideBuffObject;
import cfg.TrainingLuckObject;
import cfg.TrainingLuckPool;
import cfg.TrainingLuckPoolLittle;
import cfg.TrainingLuckPoolLittleObject;
import cfg.TrainingLuckPoolObject;
import cfg.TrainingMap;
import cfg.TrainingMapEvent;
import cfg.TrainingMapEventObject;
import cfg.TrainingMapObject;
import cfg.TrainingPoint;
import cfg.TrainingPointObject;
import cfg.TrainingShop;
import cfg.TrainingShopObject;
import common.GameConst;
import common.GlobalData;
import common.IdGenerator;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import db.entity.BaseEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.itembag.ItemConst.ItemType;
import model.patrol.entity.PatrolBattleResult;
import model.patrol.entity.PatrolMapInitResult;
import model.patrol.entity.PlayerPatrolChamberData;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import model.team.util.TeamsUtil;
import model.training.bean.TrainPoint;
import model.training.bean.TrainPointType;
import model.training.bean.TrainRankMap;
import model.training.bean.TrainRankSortInfo;
import model.training.bean.TrainShopData;
import model.training.bean.TrainTreeMap;
import model.training.bean.TrainingHideBuffType;
import model.training.bean.TrainingLuckType;
import model.training.dbCache.trainingCache;
import model.training.entity.trainingEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.math.RandomUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.GamePlayLog;
import protocol.Battle.PetBuffData;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol;
import protocol.PetMessage.Pet;
import protocol.PrepareWar.TeamNumEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem;
import protocol.TargetSystem.TargetMission;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TargetSystemDB.TrainingMapTaskData;
import protocol.Training;
import protocol.Training.SC_TrainAlertBuff;
import protocol.Training.SC_TrainAlertBuffChose;
import protocol.Training.SC_TrainAllShop;
import protocol.Training.SC_TrainAlterItem;
import protocol.Training.SC_TrainAwardAll;
import protocol.Training.SC_TrainBagInfo;
import protocol.Training.SC_TrainBuffAll;
import protocol.Training.SC_TrainGoing;
import protocol.Training.SC_TrainJoin;
import protocol.Training.SC_TrainNpcEvent;
import protocol.Training.SC_TrainNpcEventResult;
import protocol.Training.SC_TrainOpenInfo;
import protocol.Training.SC_TrainRank;
import protocol.Training.SC_TrainReset;
import protocol.Training.SC_TrainShop;
import protocol.Training.SC_TrainShopBuy;
import protocol.Training.SC_TrainShopRefresh;
import protocol.Training.SC_TrainShowCards;
import protocol.Training.SC_TrainUse;
import protocol.Training.SC_TrainUseItem;
import protocol.Training.SC_TrainingCardPanel;
import protocol.Training.SC_TrainingMapPanel;
import protocol.Training.SC_TrainingReport;
import protocol.Training.TrainBloodMonsterBest;
import protocol.Training.TrainBuffBag;
import protocol.Training.TrainBuffData;
import protocol.Training.TrainItemBag;
import protocol.Training.TrainItemData;
import protocol.Training.TrainItemData.Builder;
import protocol.Training.TrainKV;
import protocol.Training.TrainMapReport;
import protocol.Training.TrainOpenInfo;
import protocol.Training.TrainRankInfo;
import protocol.Training.TrainShopItem;
import protocol.Training.TrainShopType;
import protocol.TrainingDB.TrainBloodMonster;
import protocol.TrainingDB.TrainCardChoice;
import protocol.TrainingDB.TrainDBMap;
import protocol.TrainingDB.TrainHelpPetData;
import protocol.TrainingDB.TrainLuckData;
import protocol.TrainingDB.TrainShopGroup;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

/**
 * @author Autumn 训练场管理器类,所有逻辑处理类
 * @author luoyun
 */
public class TrainingManager {

	private static class LazyHolder {
		private static final TrainingManager INSTANCE = new TrainingManager();
	}

	private TrainingManager() {
	}

	public static TrainingManager getInstance() {
		return LazyHolder.INSTANCE;
	}

	public boolean init() {
		loadCfgAfter();
		initRankInfo();
		return true;
	}

	/**
	 * 积分排行处理数据
	 */
	private Map<Integer, TrainRankMap> jifenData = new ConcurrentHashMap<Integer, TrainRankMap>();
	private Map<Integer, TrainTreeMap> cfgMap = new HashMap<Integer, TrainTreeMap>();
	private Map<Integer, Integer> rankLimt = new HashMap<Integer, Integer>();
	private Map<Integer, TrainShopData> shopMap = new HashMap<>();
	private Map<Integer, List<TrainingShopObject>> allShop = new HashMap<>();
	private Random r = new Random();
	// 池子祝福卡权重配置
	Map<Integer, Map<Integer, Integer>> poolWeight = new HashMap<>();
	// 池子祝福卡总权重
	Map<Integer, Integer> poolTotalWeight = new HashMap<>();

	Map<Integer, Map<Integer, Integer>> pointLevelMap = new HashMap<>();
	/**
	 * 加载配置文件过后
	 */
	public void loadCfgAfter() {

		// 池子祝福卡权重配置
		Map<Integer, Map<Integer, Integer>> poolWeight = new HashMap<>();

		// 池子祝福卡总权重
		Map<Integer, Integer> poolTotalWeight = new HashMap<>();

		// 祝福卡池子
		for (Entry<Integer, TrainingLuckPoolLittleObject> ent : TrainingLuckPoolLittle._ix_id.entrySet()) {
			Map<Integer, Integer> tmp = new HashMap<>();
			int total = 0;
			for (int[] i : ent.getValue().getCards()) {
				if (i.length < 2) {
					continue;
				}
				tmp.put(i[0], i[1]);
				total += i[1];
			}
			poolWeight.put(ent.getKey(), tmp);
			poolTotalWeight.put(ent.getKey(), total);
		}

		this.poolWeight = poolWeight;
		this.poolTotalWeight = poolTotalWeight;

		Map<Integer, Integer> rankLimtTemp = new HashMap<Integer, Integer>();
		Map<Integer, Map<Integer, Integer>> pointLevelTmpMap = new HashMap<>();
		for (TrainingPointObject ent : TrainingPoint._ix_pointid.values()) {
			TrainingMapObject mapConf = TrainingMap.getByMapid(ent.getMapid());
			if (mapConf == null) {
				continue;
			}
			rankLimtTemp.put(ent.getMapid(), TrainingMap.getByMapid(ent.getMapid()).getStar());
			TrainTreeMap tp = cfgMap.get(ent.getMapid());
			if (null == tp) {
				tp = new TrainTreeMap(ent.getMapid());
				cfgMap.put(ent.getMapid(), tp);
				int level = 2;
				Map<Integer, List<Integer>> temMap = new HashMap<>();
				for (int[] i : mapConf.getUnlocklevel()) {
					List<Integer> list = new ArrayList<>();
					for (Integer e : i) {
						list.add(e);
					}
					temMap.put(level++, list);
					tp.setUnlockLevelMap(temMap);

				}
			}
			boolean canReset = false;
			tp.addPoint(ent.getPointid(), ent.getChildnode(), canReset);
			if (ent.getPointid() < 10000000) { // 变种节点不算
				tp.setTotalPointCount(tp.getTotalPointCount() + 1);

				Map<Integer, Integer> pointLevelMap = pointLevelTmpMap.getOrDefault(ent.getMapid(), null);
				if (pointLevelMap == null) {
					pointLevelMap = new HashMap<>();
					pointLevelTmpMap.put(ent.getMapid(), pointLevelMap);
				}
				pointLevelMap.merge(ent.getPointlevel(), 1, (oldVal, newVal) -> oldVal+newVal);
			}

		}
		this.rankLimt = rankLimtTemp;
		this.pointLevelMap = pointLevelTmpMap;

		Map<Integer, Map<Integer, List<TrainingLuckHideBuffObject>>> hiddBuff = new HashMap<>();
		Map<Integer, List<TrainingShopObject>> groupMap = new HashMap<>();

		for (TrainingShopObject obj : TrainingShop._ix_id.values()) {
			if (!groupMap.containsKey(obj.getGroup())) {
				groupMap.put(obj.getGroup(), new ArrayList<>());
			}
			groupMap.get(obj.getGroup()).add(obj);
		}

		Map<Integer, TrainShopData> shopMap = new HashMap<>();
		for (TrainingMapObject obj : TrainingMap._ix_mapid.values()) {
			TrainShopData data = null;
			if (!shopMap.containsKey(obj.getMapid())) {
				data = new TrainShopData();
				shopMap.put(obj.getMapid(), data);
			} else {
				data = shopMap.get(obj.getMapid());
			}
			for (int i : obj.getShops()) {
				if (!groupMap.containsKey(i)) {
					continue;
				}
				data.getMap().put(i, groupMap.get(i));
			}
			TrainTreeMap trainTreeMap = cfgMap.get(obj.getMapid());
			if (trainTreeMap != null) {
				List<Integer> list = new ArrayList<>();
				for (Integer i : obj.getChoice()) {
					list.add(i);
				}
				trainTreeMap.setChange2Two(list);
			}
		}
		// 数据加载完成检查数据
		for (TrainTreeMap ttm : cfgMap.values()) {
			ttm.checkPoint();
//			if (mapEventIdType.containsKey(ttm.getMapid())) {
//				ttm.setEventIdType(mapEventIdType.get(ttm.getMapid()));
//			}
			if (hiddBuff.containsKey(ttm.getMapid())) {
				ttm.setHiddBuff(hiddBuff.get(ttm.getMapid()));
			}
		}
		this.shopMap = shopMap;
		this.allShop = groupMap;
	}

	/**
	 * 初始化训练场积分排行榜数据
	 */
	public void initRankInfo() {
		// 初始化加载数据
		for (BaseEntity be : trainingCache.getInstance().getAll().values()) {
			trainingEntity entity = (trainingEntity) be;
			for (TrainDBMap.Builder tdbm : entity.getInfoDB().getMapsBuilderList()) {
				refJifen(tdbm.getMapId(), entity.getPlayeridx(), tdbm.getStarNum(), getJifen(tdbm), tdbm.getJifenRefTime());
			}
		}
		// 数据加载完成
		for (TrainRankMap ent : jifenData.values()) {
			ent.sort();
		}
	}

	public void onPlayerLogIn(String playerIdx) {
		sendTrainOpen(playerIdx);
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerIdx);
		if (null == cache) {
			return;
		}
		if (cache.getInfoDB().getNoticeMapCount() <= 0) {
			return;
		}
		for (Integer mapId : cache.getInfoDB().getNoticeMapList()) {
			TrainDBMap.Builder trainDBMap = cache.getTrainMapByMapId(mapId);
			if (trainDBMap == null) {
				continue;
			}
			sendReport(playerIdx, trainDBMap);
		}
	}

	public void trainReportGet(String playerId) {
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			return;
		}
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> cacheTemp.getInfoDB().clearNoticeMap());
	}

	public void refJifen(int mapId, String pid, int star, int jifen, long time, boolean isSort) {
		if (jifen <= 0) {
			return;
		}
		if (star < rankLimt.getOrDefault(mapId, 10)) {
			return;
		}
		TrainRankMap trm = jifenData.get(mapId);
		if (null == trm) {
			trm = new TrainRankMap();
			jifenData.put(mapId, trm);
		}
		if (isSort) {
			trm.refJifenAndSort(pid, star, jifen, time);
		} else {
			trm.refJifen(pid, star, jifen, time);
		}
	}

	public void refJifen(int mapId, String pid, int star, int jifen, long time) {
		refJifen(mapId, pid, star, jifen, time, false);
	}

	public void finishMainTask(String playerId, int gk) {
		for (TrainingMapObject ent : TrainingMap._ix_mapid.values()) {
			if (ent.getUnlock() <= gk) {
				// 开启副本
				openTrain(playerId, ent);
			}
		}
	}

	public void sendTrainOpen(String playerId) {
		SC_TrainOpenInfo.Builder msg = SC_TrainOpenInfo.newBuilder();
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			return;
		}
		for (TrainDBMap mapData : cache.getInfoDB().getMapsList()) {
			if (GlobalTick.getInstance().getCurrentTime() < mapData.getCloseTime()) {
				TrainOpenInfo.Builder openInfo = TrainOpenInfo.newBuilder();
				openInfo.setMapId(mapData.getMapId());
				openInfo.setEndTime(mapData.getCloseTime());
				msg.addOpenInfo(openInfo);
			}
		}
		msg.addAllEndedMapId(cache.getInfoDB().getEndMapMap().keySet());
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainOpenInfo_VALUE, msg);
	}

	/**
	 * 玩家开启某个训练场
	 * 
	 * @param playerId
	 * @param mapId
	 * @param time
	 */
	public void resetTrain(String playerId, int mapId, int time) {
		TrainingMapObject cfg = TrainingMap.getByMapid(mapId);
		if (null == cfg) {
			return;
		}
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			return;
		}
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			// 判断活动是否开启，副本是否对应
			TrainDBMap.Builder tMap = cacheTemp.getTrainMapByMapId(mapId);
			if (null == tMap) {
				return;
			}
			long openTime = GlobalTick.getInstance().getCurrentTime();
			long endTime = openTime + time * 1000L;
			tMap.setOpenTime(openTime);
			tMap.setCloseTime(endTime);
			tMap.setEndtime(0);
			cacheTemp.getInfoDB().removeEndMap(cfg.getMapid());
			sendTrainOpen(playerId);
		});
	}

	public void clearTrain(String playerId, int mapId) {
		TrainingMapObject cfg = TrainingMap.getByMapid(mapId);
		if (null == cfg) {
			return;
		}
		trainingEntity trainingEntity = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == trainingEntity) {
			return;
		}
		SyncExecuteFunction.executeConsumer(trainingEntity, entity -> {
			TrainDBMap.Builder tMap = entity.getTrainMapByMapId(mapId);
			if (null == tMap) {
				return;
			}
			for (int i = 0; i < entity.getInfoDB().getMapsCount(); i++) {
				TrainDBMap.Builder mapData = entity.getInfoDB().getMapsBuilderList().get(i);
				if (mapData != null && mapData.getMapId() == mapId) {
					entity.getInfoDB().removeMaps(i);
					break;
				}
			}
		});
	}

	/**
	 * 功能是否开启
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean isOpenFunction(String playerId) {
//		playerEntity player = playerCache.getByIdx(playerId);
//		if (null == player) {
//			return false;
//		}
//        if (player.getLevel() < FunctionOpenLvConfig.getOpenLv(EnumFunction.Training)) {
//        	return false;
//        }
		return true;
	}

	/**
	 * 玩家进入副本
	 */

	/**
	 * 玩家进入副本
	 */
	public void sendMainPane(String playerId, TrainDBMap.Builder tMap) {
		SC_TrainJoin.Builder msg = SC_TrainJoin.newBuilder();
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RCE_Success);
		msg.setResult(retCode);
		msg.setUpdateAll(true);
		msg.setMapId(tMap.getMapId());
		msg.setStarNum(tMap.getStarNum());
		msg.setJifen(getJifen(tMap));
		msg.setCurrPos(tMap.getCurrPos());

		msg.addAllGoNum(tMap.getGoNumList());
		for (TrainHelpPetData dbPet : tMap.getMlidsList()) {
			String petId = createPetId(dbPet.getPointId(), dbPet.getPetCfgId());
			Pet pet = createPet(dbPet.getPetCfgId(), petId, dbPet.getPetLevel(), dbPet.getPetRarity(), dbPet.getPetUpLevel());
			if (null != pet) {
				msg.addPetList(pet);
			}
		}
		msg.addAllPath(tMap.getCurpathList());
		TrainTreeMap trainTreeMap = cfgMap.get(tMap.getMapId());
		if (trainTreeMap != null) {
			msg.addAllSelectPoint(trainTreeMap.getChange2Two());
		}
		msg.setEventId(tMap.getNpcEventId());
		msg.setCurLevel(tMap.getEventData().getLevel());
		for (Entry<Integer, TrainBloodMonster> ent : tMap.getBloodMonsterMap().entrySet()) {
			TrainBloodMonsterBest.Builder best = TrainBloodMonsterBest.newBuilder();
			best.setJifen(ent.getValue().getMax());
			best.setPointId(ent.getKey());
			best.setTimes(ent.getValue().getTimes());
			msg.addBlood(best);
		}
		int ranking = 0;
		TrainRankMap mapRank = jifenData.get(tMap.getMapId());
		if (null != mapRank) {
			ranking = mapRank.getRankById(playerId);
		}
		msg.setPlayerRank(ranking);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainJoin_VALUE, msg);
	}

	public void updateMainPane(String playerId, TrainDBMap.Builder tMap, int pointId, TrainKV goNum) {
		SC_TrainJoin.Builder msg = SC_TrainJoin.newBuilder();
		msg.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
		msg.setMapId(tMap.getMapId());
		msg.setStarNum(tMap.getStarNum());
		msg.setJifen(getJifen(tMap));
		msg.setCurrPos(tMap.getCurrPos());

		if (goNum != null) {
			msg.addGoNum(goNum);
		}

		for (TrainHelpPetData dbPet : tMap.getMlidsList()) {
			String petId = createPetId(dbPet.getPointId(), dbPet.getPetCfgId());
			Pet pet = createPet(dbPet.getPetCfgId(), petId, dbPet.getPetLevel(), dbPet.getPetRarity(), dbPet.getPetUpLevel());
			if (null != pet) {
				msg.addPetList(pet);
			}
		}
		if (pointId > 0 && TrainingPoint.getByPointid(pointId) != null) {
			msg.addPath(pointId);
		}
		TrainTreeMap trainTreeMap = cfgMap.get(tMap.getMapId());
		if (trainTreeMap != null) {
			msg.addAllSelectPoint(trainTreeMap.getChange2Two());
		}
		msg.setEventId(tMap.getNpcEventId());
		msg.setCurLevel(tMap.getEventData().getLevel());
		for (Entry<Integer, TrainBloodMonster> ent : tMap.getBloodMonsterMap().entrySet()) {
			TrainBloodMonsterBest.Builder best = TrainBloodMonsterBest.newBuilder();
			best.setJifen(ent.getValue().getMax());
			best.setPointId(ent.getKey());
			best.setTimes(ent.getValue().getTimes());
			msg.addBlood(best);
		}
		int ranking = 0;
		TrainRankMap mapRank = jifenData.get(tMap.getMapId());
		if (null != mapRank) {
			ranking = mapRank.getRankById(playerId);
		}
		msg.setPlayerRank(ranking);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainJoin_VALUE, msg);
	}

	public void sendMapAll(String playerId) {
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);

		SC_TrainingMapPanel.Builder builder = SC_TrainingMapPanel.newBuilder();
		if (null == cache) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainingMapPanel_VALUE, builder);
			return;
		}
		long now = GlobalTick.getInstance().getCurrentTime();
		for (TrainDBMap.Builder mapData : cache.getInfoDB().getMapsBuilderList()) {
			if (now > mapData.getCloseTime()) {
				builder.addReport(createReport(mapData));
			}
		}
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainingMapPanel_VALUE, builder);
	}

	public TrainMapReport createReport(TrainDBMap.Builder map) {
		TrainMapReport.Builder builder = TrainMapReport.newBuilder();
		builder.setEndPower(map.getEndPower());
		builder.setJoinPower(map.getJoinPower());
		builder.addAllGoNum(map.getGoNumList());
		builder.setMapId(map.getMapId());
		builder.setReportCard(map.getReportCardLevel());
		builder.setScore(getJifen(map));
		builder.setScoreRank(map.getEndRankMC());
		builder.addAllReward(map.getRewardList());
		builder.setTime(map.getEndtime());
		int passPer = 0;
		if (map.getCurpathCount() > 1) {
			int totalPointCount = 0;
			TrainTreeMap mapCfg = cfgMap.get(map.getMapId());
			if (mapCfg != null) {
				totalPointCount = mapCfg.getTotalPointCount();
			}
			passPer = totalPointCount > 0 ? map.getCurpathCount() * 10000 / totalPointCount : 0;
		}
		builder.setPer(passPer);
		Integer beyondRateObj = map.getBeyondPlayerDataMap().get(map.getMapId());
		int betterPer = beyondRateObj != null ? beyondRateObj : 0;
		builder.setBetter(betterPer);
		return builder.build();
	}

	protected int calcBeyondPlayerRate(TrainDBMap.Builder dbMap, int rank) {
		TrainingMapObject mapCfg = TrainingMap.getByMapid(dbMap.getMapId());
		if (mapCfg == null) {
			return 0;
		}
		if (rank > 0) {
			int[][] rankBeyondData = mapCfg.getRankplayerbeyonddata();
			if (rankBeyondData == null || rankBeyondData.length < 1) {
				return 0;
			}
			for (int[] rankData : rankBeyondData) {
			    if (rankData == null || rankData.length < 4) {
			    	continue;
				}
				if (rankData[2] > rankData[3]) {
					continue;
				}
			    if (rankData[0] > rank || rankData[1] < rank) {
			    	continue;
				}
				int randNum = rankData[3] > rankData[2] ? RandomUtils.nextInt(rankData[3] - rankData[2]) : 0;
				return rankData[2] + randNum;
			}
			return 0;
		} else if (dbMap.getEventData().getLevel() > 2) { // 通过前两阶段，暂写死
			int[] beyondData = mapCfg.getUnrankplayerbeyonddata2();
			if (beyondData == null || beyondData.length < 2) {
				return 0;
			}
			if (beyondData[1] < beyondData[0]) {
				return 0;
			}
			int randNum = beyondData[1] > beyondData[0] ? RandomUtils.nextInt(beyondData[1] - beyondData[0]) : 0;
			return beyondData[0] + randNum;
		} else {
			int[] beyondData = mapCfg.getUnrankplayerbeyonddata1();
			if (beyondData == null || beyondData.length < 4) {
				return 0;
			}
			int curPointLevel = dbMap.getEventData().getLevel();
			Map<Integer, Integer> beyondDataMap = pointLevelMap.getOrDefault(dbMap.getMapId(), null);
			if (beyondDataMap == null || beyondDataMap.isEmpty()) {
				return 0;
			}
			if (dbMap.getCurpathCount() <= 1) { // 只有一个节点的时候
				return 0;
			}
			Integer pointSizeObj = beyondDataMap.get(curPointLevel);
			int pointSize = pointSizeObj != null ? pointSizeObj : 0;
			int finishedPointCount = 0;
			TrainingPointObject pointCfg;
			for (Integer pointId : dbMap.getCurpathList()) {
				pointCfg = TrainingPoint.getByPointid(pointId);
				if (pointCfg == null || pointCfg.getPointlevel() != curPointLevel) {
					continue;
				}
				if (pointCfg.getPointid() > 10000000) { // 变异节点不算
					continue;
				}
				finishedPointCount++;
			}

			int baseBeyondRate = curPointLevel == 1 ? beyondData[0] : beyondData[2]; // 1阶段读前两个参数，2阶段读后两个参数
			int totalPointRate = curPointLevel == 1 ? beyondData[1] : beyondData[3]; // 1阶段读前两个参数，2阶段读后两个参数
			int perPointRate = pointSize > 0 ? totalPointRate * 1000 / pointSize / 1000 : 0;

			int finishPointRate = finishedPointCount * perPointRate;
			return baseBeyondRate + finishPointRate;
		}
	}

	public int getJifen(TrainDBMap.Builder map) {
		int jifen = map.getJifen();
		for (Entry<Integer, TrainBloodMonster> ent : map.getBloodMonsterMap().entrySet()) {
			jifen += ent.getValue().getMax();
		}
		return jifen;
	}

	public void sendReport(String playerId, TrainDBMap.Builder map) {
		SC_TrainingReport.Builder builder = SC_TrainingReport.newBuilder();
		TrainMapReport createReport = createReport(map);
		builder.setReport(createReport);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainingReport_VALUE, builder);
	}

	public void sendAllLuckCard(String playerId, int mapId) {
		SC_TrainingCardPanel.Builder builder = SC_TrainingCardPanel.newBuilder();
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);

		if (null == cache) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainingCardPanel_VALUE, builder);
			return;
		}
		TrainDBMap.Builder map = cache.getTrainMapByMapId(mapId);
		if (map == null) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainingCardPanel_VALUE, builder);
			return;
		}
		builder.setMapId(mapId);
		builder.addAllId(map.getLuckbuffidList());
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainingCardPanel_VALUE, builder);
	}

	public void getPointPetInfo(String playerId, int mapId, int pointid, int itemId) {
		Training.SC_TrainPetInfo.Builder msg = Training.SC_TrainPetInfo.newBuilder();
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RCE_Success);
		msg.setResult(retCode);
		if (!isOpenFunction(playerId)) {
			retCode.setRetCode(RetCodeEnum.RCE_FunctionIsLock);
			msg.setResult(retCode);
			return;
		}
		int createId = 0;
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache || cache.getInfoDB().getCurrMap() != mapId) {
			// 第一次初始化玩法数据
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainPetInfo_VALUE, msg);
			return;
		}
		int[][] molings = null;
		if (pointid > 0) {
			createId = pointid;
			TrainingPointObject tpoCfg = TrainingPoint.getByPointid(pointid);
			if (null == tpoCfg) {
				retCode.setRetCode(RetCodeEnum.RCE_ConfigError);
				msg.setResult(retCode);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainPetInfo_VALUE, msg);
				return;
			}
			molings = tpoCfg.getMolings();
		} else {
			createId = itemId;
			ItemObject obj = Item.getById(itemId);
			if (obj == null) {
				retCode.setRetCode(RetCodeEnum.RCE_ConfigError);
				msg.setResult(retCode);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainPetInfo_VALUE, msg);
				return;
			}
			if (!obj.getParamname().equals("trainpet")) {
				retCode.setRetCode(RetCodeEnum.RCE_ConfigError);
				msg.setResult(retCode);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainPetInfo_VALUE, msg);
				return;
			}
			if (obj.getParamstr().length < 1) {
				retCode.setRetCode(RetCodeEnum.RCE_ConfigError);
				msg.setResult(retCode);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainPetInfo_VALUE, msg);
				return;
			}
			molings = obj.getParamstr();
		}

		PlayerPatrolChamberData playerPatrolData = new PlayerPatrolChamberData(playerId);
		Patrol.PatrolChamber.Builder msg2 = Patrol.PatrolChamber.newBuilder();
		if (molings != null) {
            for (int i = 0; i < molings.length; i++) {
                if (molings[i] == null || molings[i].length < 4) {
                    continue;
                }
				String newId = createPetId(createId, molings[i][0]);
				Pet pet = createPet(molings[i][0], newId, molings[i][1], molings[i][2], molings[i][3]);
				if (null != pet) {
					msg2.addPetList(pet);
				}
            }
        }
		PatrolMapInitResult returnMap = new PatrolMapInitResult();
		returnMap.setRuneList(playerPatrolData.getRuneList());
		playerEntity player = playerCache.getByIdx(playerId);
		if (player != null) {
			returnMap.setArtifactAddition(player.getDb_data().getGlobalAddition().getArtifactAdditionMap());
			returnMap.setArtifacts(player.getSimpleArtifact());
			returnMap.setTitleIds(player.getPlayerAllTitleIds());
		}
		msg2.addAllPetRuneList(returnMap.getRuneList()).addAllArtifactAdditionKeys(returnMap.getArtifactAddition().keySet()).addAllArtifactAdditionValues(returnMap.getArtifactAddition().values()).addAllNewTitleId(returnMap.getTitleIds()).addAllArtifact(returnMap.getArtifacts()).build();
		msg.setPetInfo(msg2);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainPetInfo_VALUE, msg);
	}

	/**
	 * 前进(非战斗点的移动消息)
	 * 
	 * @param playerId
	 * @param nextPointid
	 */
	public void going(String playerId, int mapId, int nextPointid, int param, boolean isAccept, boolean handMove, int hpRate) {
		SC_TrainGoing.Builder msg = SC_TrainGoing.newBuilder();
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RCE_Success);
		if (!isOpenFunction(playerId)) {
			retCode.setRetCode(RetCodeEnum.RCE_FunctionIsLock);
			msg.setResult(retCode);
			if (isAccept) {
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
			}
			return;
		}
		if (nextPointid >= 10000000) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_POSERROR);
			msg.setResult(retCode);
			if (isAccept) {
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
			}
			return;
		}
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache || cache.getInfoDB().getCurrMap() != mapId) {
			// 第一次初始化玩法数据
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			msg.setResult(retCode);
			if (isAccept) {
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
			}
			return;
		}
		TrainDBMap.Builder tMap1 = cache.getTrainMapByMapId(mapId);
		if (tMap1 == null || !trainingMapIsOpen(tMap1)) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			msg.setResult(retCode);
			if (isAccept) {
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
			}
			return;
		}
		TrainingMapObject tmoCfg = TrainingMap.getByMapid(mapId);
		TrainTreeMap ttmCfgAfter = cfgMap.get(mapId);
		TrainingPointObject tpoCfg = TrainingPoint.getByPointid(nextPointid);

		TrainingPointObject changePoint = null;
		if (tMap1.getChangePointMap().containsKey(nextPointid)) {
			changePoint = TrainingPoint.getByPointid(tMap1.getChangePointMap().get(nextPointid));
		}

		// 获取地图原始数据
		if (null == tmoCfg || null == ttmCfgAfter || null == tpoCfg) {
			retCode.setRetCode(RetCodeEnum.RCE_ConfigError);
			msg.setResult(retCode);
			if (isAccept) {
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
			}
			return;
		}
		if (tMap1.getNpcEventId() != 0) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NotFinishNpcEvent);
			msg.setResult(retCode);
			if (isAccept) {
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
			}
			return;
		}
		if (tMap1.getBuffIdsList().size() > 0) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NotFinishChoseBUFF);
			msg.setResult(retCode);
			if (isAccept) {
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
			}
			return;
		}
		TrainPoint nextPoint = ttmCfgAfter.getAllPoint().get(nextPointid);
		if (nextPoint == null) {
			retCode.setRetCode(RetCodeEnum.RCE_ConfigError);
			msg.setResult(retCode);
			if (isAccept) {
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
			}
			return;
		}
		if (!checkCanMove(tMap1, ttmCfgAfter, nextPoint, tpoCfg, changePoint)) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_POSNOTFLISH);
			msg.setResult(retCode);
			if (isAccept) {
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
			}
			return;
		}
		int starMax = tpoCfg.getStarmax();
		if (changePoint != null) {
			starMax = changePoint.getStarmax();
		}

		int type = changePoint == null ? tpoCfg.getType() : changePoint.getType();
		int[][] molings = changePoint == null ? tpoCfg.getMolings() : changePoint.getMolings();
		if (type == TrainPointType.BLOODMONSTER) {
			if (handMove && isAccept) {
				retCode.setRetCode(RetCodeEnum.RCE_TRAIN_POSERROR);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
				return;
			}
			TrainingPointObject bloodCfg = changePoint != null ? changePoint : tpoCfg;
			if (bloodCfg.getBloodmonster() != null && bloodCfg.getBloodmonster().length >= 2) {
				if (bloodCfg.getBloodmonster()[1] == 0) {
					starMax = 1000;
				} else {
					starMax = bloodCfg.getBloodmonster()[1];
				}
			}
		}
		boolean pointPassed = tMap1.containsStarMap(nextPoint.getPid()) && tMap1.getStarMapOrDefault(nextPoint.getPid(), 0) >= starMax;
		if (handMove && (type == TrainPointType.MONSTER_NORMAL || type == TrainPointType.MONSTER_BETTER || type == TrainPointType.BOSS)) {
			if (!pointPassed) {
				// 不能手动发送going到怪物点,除非已通关
				if (isAccept) {
					retCode.setRetCode(RetCodeEnum.RCE_TRAIN_POSNOTFLISH);
					GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
				}
				return;
			}
		}
		if (pointPassed) {// 已通关的点可以随便移动
			SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
				// 判断通过，往前走一步
				// 其他点直接移动
				TrainDBMap.Builder tMap = cache.getTrainMapByMapId(mapId);
				tMap.setCurrPos(nextPointid);
				updateMainPane(playerId, tMap, 0, null);
			});
			retCode.setRetCode(RetCodeEnum.RCE_Success);
			// 上行成功
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
			return;
		}

		if (!checkBloodMonster(tMap1, nextPointid, changePoint == null ? tpoCfg : changePoint)) {
			msg.setResult(retCode);
			if (isAccept) {
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
			}
			return;
		}

		retCode.setRetCode(RetCodeEnum.RCE_ConfigError);
		if (param >= 0 && type == TrainPointType.PET && molings.length < param) {
			msg.setResult(retCode);
			if (isAccept) {
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
			}
			return;
		}
		final TrainingPointObject tmpChangPoint = changePoint; // lambda只能用final类型
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			// 判断通过，往前走一步
			// 其他点直接移动
			TrainDBMap.Builder tMap = cache.getTrainMapByMapId(mapId);
			boolean newPoint = true;
			for (Integer i : tMap.getCurpathList()) {
				if (i == nextPointid) {
					newPoint = false;
					break;
				}
			}
			if (type != TrainPointType.NPCEVENT) {
				if (newPoint) {
					tMap.addCurpath(nextPointid);
				}
			}
			tMap.setCurrPos(nextPointid);
			tMap.setIsBlessing(0);
			if (type == TrainPointType.BOSS) {
				// 可以直接通过的点，达到即完成，给与奖励就行
			} else if (type == TrainPointType.MONSTER_NORMAL && tpoCfg.getFightmakeid().length <= 0) {
				// 空白点
			} else if (type == TrainPointType.BUFF) {
			} else if (type == TrainPointType.PET) {
				addPlayerMapPointDBdata(playerId, tMap, nextPointid, 0, tpoCfg.getMolings()[param]);
			} else if (type == TrainPointType.LIMIT_SHOP) {
				sendAlertItem(playerId, TrainShopType.SHOP_LIMITCARD_VALUE, addShopItemLimitCard(tMap, tpoCfg.getPoolid()));
			} else if (type == TrainPointType.LUCKCARD_THREE || type == TrainPointType.MONSTER_NORMAL || type == TrainPointType.MONSTER_BETTER) {
				if (tpoCfg.getPoolid() > 0) {
					List<Integer> luckCardByPoolId = getCardByPool(tMap, tpoCfg.getPoolid(), 3);
					int  blessing = 0;
					List<Integer> buffIds = new ArrayList<>();
					if (luckCardByPoolId.size() > 0) {
						blessing = 1;
						tMap.clearBuffIds();
						for (int entid : luckCardByPoolId) {
							buffIds.add(entid);
							tMap.addBuffIds(entid);
						}
					}
					alertBuffChose(playerId, buffIds, 0);
					tMap.setIsBlessing(blessing);
				}
//			} else if (type == TrainPointType.MONSTER_NORMAL || type == TrainPointType.MONSTER_BETTER) {
//				// 战斗完成弹出选择祝福卡
//				int blessing = 0;
//				if (tpoCfg.getBlessing().length > 0) {
//					blessing = 1;
//					SC_TrainAlertBuff.Builder alterB = SC_TrainAlertBuff.newBuilder();
//					tMap.clearBuffIds();
//					for (int entid : tpoCfg.getBlessing()) {
//						alterB.addBuffIds(entid);
//						tMap.addBuffIds(entid);
//					}
//					GlobalData.getInstance().sendMsg(cache.getPlayeridx(), MessageId.MsgIdEnum.SC_TrainAlertBuff_VALUE, alterB);
//				}
//				tMap.setIsBlessing(blessing);
			} else if (type == TrainPointType.FULISHANGDIAN) {
				sendAlertItem(playerId, TrainShopType.SHOP_FULI_VALUE, addShopItemFuli(tMap, tpoCfg.getFulishop()));
			} else if (type == TrainPointType.NPCEVENT) {
				onNpcEvent(playerId, tMap, tpoCfg.getPointid());
			} else if (type == TrainPointType.BLOODMONSTER) {
				onBloodMonster(playerId, tMap, nextPointid, tmpChangPoint == null ? tpoCfg : tmpChangPoint, hpRate);
			}
			TrainingPointObject changeCfg = null;
			if (tMap.getChangePointMap().containsKey(nextPointid)) {
				changeCfg = TrainingPoint.getByPointid(tMap1.getChangePointMap().get(nextPointid));
			}
			TrainKV goNum = passPoint(playerId, tMap, tpoCfg, ttmCfgAfter, changeCfg);

			// 行走成功，刷新主界面数据
			updateMainPane(playerId, tMap, tpoCfg.getPointid(), goNum);
		});
		retCode.setRetCode(RetCodeEnum.RCE_Success);
		// 上行成功
		msg.setResult(retCode);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, msg);
	}

	private void countRewards(TrainDBMap.Builder tMap, List<Reward> rewards) {
		ItemObject itemCfg;
		for (Reward reward : rewards) {
			if (reward.getRewardType() == RewardTypeEnum.RTE_Train) {
				continue;
			}
			if (reward.getRewardType() == RewardTypeEnum.RTE_Item) {
				itemCfg = Item.getById(reward.getId());
				// 训练营道具不计入数量
				if (itemCfg == null || itemCfg.getSpecialtype() == ItemType.TrainItem) {
					continue;
				}
			}
			boolean addNewFlag = true;
			for (Reward.Builder kv : tMap.getRewardBuilderList()) {
				if (kv.getId() == reward.getId() && kv.getRewardTypeValue() == reward.getRewardTypeValue()) {
					kv.setCount(kv.getCount() + reward.getCount());
					addNewFlag = false;
					break;
				}
			}
			if (addNewFlag) {
				tMap.addReward(reward);
			}
		}
	}

	private void onBloodMonster(String playerId, TrainDBMap.Builder tMap, int pointId, TrainingPointObject obj, float hprate) {
		if (obj.getBloodmonster().length < 2) {
			return;
		}
		int max = 0;
		float baseScore = obj.getBloodmonster()[0];
		TrainBloodMonster.Builder monster = null;
		if (tMap.getBloodMonsterMap().containsKey(pointId)) {
			monster = tMap.getBloodMonsterMap().get(pointId).toBuilder();
			max = monster.getMax();
		} else {
			monster = TrainBloodMonster.newBuilder();
		}
		monster.setTimes(monster.getTimes() + 1);
		baseScore *= 1000 - hprate;
		baseScore /= 1000;
		boolean updateScore = false;
		if (baseScore > max) {
			max = (int) baseScore;
			updateScore = true;
		}
		monster.setMax(max);
		monster.putHpRate((int) hprate, (int) baseScore);
		tMap.putBloodMonster(pointId, monster.build());
		if (updateScore) {
			refJifen(tMap.getMapId(), playerId, tMap.getStarNum(), getJifen(tMap), tMap.getJifenRefTime(), true);
		}
	}

	private boolean checkBloodMonster(TrainDBMap.Builder tMap, int point, TrainingPointObject obj) {
		if (obj.getType() != TrainPointType.BLOODMONSTER) {
			return true;
		}
		if (!tMap.getBloodMonsterMap().containsKey(point)) {
			return true;
		}
		if (obj.getBloodmonster().length < 2) {
			return false;
		}
		int time = 0;
		if (tMap.getBloodMonsterMap().containsKey(point)) {
			time = tMap.getBloodMonsterMap().get(point).getTimes();
		}
		if (obj.getBloodmonster()[1] != 0 && time >= obj.getBloodmonster()[1]) {
			return false;
		}
		return true;
	}

	private void onNpcEvent(String playerId, TrainDBMap.Builder tMap, int pointId) {
		TrainingPointObject obj = TrainingPoint.getByPointid(pointId);
		if (obj == null) {
			return;
		}
		int npc = obj.getNpc();
		TrainingMapEventObject npcConfig = TrainingMapEvent.getById(npc);
		if (npcConfig == null) {
			return;
		}
		tMap.setNpcEventId(npc);
		tMap.setNpcEventPoint(pointId);
		SC_TrainNpcEvent.Builder builder = SC_TrainNpcEvent.newBuilder();
		builder.setEventId(npc);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainNpcEvent_VALUE, builder);
	}

	public void getNpcReward(String playerId, List<Integer> choice) {
		SC_TrainNpcEventResult.Builder builder = SC_TrainNpcEventResult.newBuilder();
		RetCode.Builder ret = RetCode.newBuilder();
		builder.setResult(ret);
		ret.setRetCode(RetCodeEnum.RCE_Success);

		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, builder);
			return;
		}
		TrainDBMap.Builder tMap = cache.getCurTrainMap();
		if (tMap == null) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, builder);
			return;
		}
		if (tMap.getNpcEventId() == 0) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, builder);
			return;
		}
		TrainingMapEventObject config = TrainingMapEvent.getById(tMap.getNpcEventId());
		if (config == null) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainGoing_VALUE, builder);
			return;
		}

		List<Reward> rewards = new ArrayList<>();
		for (int i = 0; i < choice.size(); i++) {
			if (config == null) {// 无后续事件
				break;
			}
			int choiceInt = choice.get(i);
			if (choiceInt < config.getReward().length) {
				rewards.add(RewardUtil.parseReward(config.getReward()[choiceInt]));
			}
			if (choiceInt < config.getEvents().length) {
				config = TrainingMapEvent.getById(config.getEvents()[choiceInt]);
			}
		}
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			TrainDBMap.Builder dbMap = cache.getCurTrainMap();
			boolean newPoint = true;
			for (Integer i : dbMap.getCurpathList()) {
				if (i == dbMap.getNpcEventPoint()) {
					newPoint = false;
					break;
				}
			}
			if (newPoint) {
				dbMap.addCurpath(dbMap.getNpcEventPoint());
			}
			countRewards(dbMap, rewards);
			dbMap.setNpcEventId(0);
			dbMap.setNpcEventPoint(0);
			updateMainPane(playerId, dbMap, dbMap.getNpcEventPoint(), null);

		});
		RewardManager.getInstance().doRewardByList(playerId, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_TRAIN_POINT), true);

		builder.setResult(ret);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainNpcEventResult_VALUE, builder);
	}
//	private void onLoseEvent(String playerId, int pointId, int mapId) {
//		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
//		if (null == cache || cache.getInfoDB().getCurrMap() != mapId) {
//			return;
//		}
//		SyncExecuteFunction.executeConsumer(cache, p -> {
//			TrainDBData.Builder builder = p.getInfoDB().toBuilder();
//			TrainDBMap map = builder.getMapsMap().get(builder.getCurrMap());
//			TrainDBMap.Builder mapB = map.toBuilder();
//			int total = mapB.getEventData().getLoseCountMap().getOrDefault(pointId, 0);
//			mapB.getEventDataBuilder().putLoseCount(pointId, total + 1);
//
//			TrainTreeMap trainTreeMap = cfgMap.get(mapB.getMapId());
//			if (trainTreeMap == null) {
//				return;
//			}
//			for (Entry<Integer, Integer> ent : trainTreeMap.getEventIdType().entrySet()) {
//				if (ent.getValue() != TrainingPointEventType.LOSEPOINT) {
//					continue;
//				}
//				if (mapB.getEventDataBuilder().getEventIdList().contains(ent.getKey())) {
//					continue;
//				}
//				TrainingMapEventObject obj = TrainingMapEvent.getById(ent.getKey());
//				if (obj == null) {
//					continue;
//				}
//				if (obj.getCustom().length < 2) {
//					continue;
//				}
//				int point = obj.getCustom()[0];
//				int times = obj.getCustom()[1];
//				if (mapB.getEventDataBuilder().getLoseCountOrDefault(point, 0) >= times) {
//					sendEventReward(mapB, playerId, ent.getKey(), pointId);
//				}
//			}
//
//			builder.putMaps(builder.getCurrMap(), mapB.build());
//			p.setInfoDB(builder.build());
//		});
//	}

//	private void onLevelEvent(String playerId, TrainDBMap.Builder mapB, int unlockLevel, List<Integer> events) {
//		TrainTreeMap trainTreeMap = cfgMap.get(mapB.getMapId());
//		if (trainTreeMap == null) {
//			return;
//		}
//		for (Entry<Integer, Integer> ent : trainTreeMap.getEventIdType().entrySet()) {
//			if (ent.getValue() != TrainingPointEventType.LEVELUNLOCK) {
//				continue;
//			}
//			if (events.contains(ent.getKey())) {
//				continue;
//			}
//			TrainingMapEventObject obj = TrainingMapEvent.getById(ent.getKey());
//			if (obj == null) {
//				continue;
//			}
//			if (obj.getCustom().length <= 0) {
//				continue;
//			}
//			int i = obj.getCustom()[0];
//			if (unlockLevel >= i) {
//				sendEventReward(mapB, playerId, ent.getKey(), 0);
//			}
//		}
//	}

	private void onPassEvent(String playerId, TrainDBMap.Builder mapB, int pointId, List<Integer> events) {
		TrainTreeMap trainTreeMap = cfgMap.get(mapB.getMapId());
		if (trainTreeMap == null) {
			return;
		}
		TrainingPointObject pointConf = TrainingPoint.getByPointid(pointId);
		if (pointConf == null) {
			return;
		}

		TrainPoint trainPoint = trainTreeMap.getAllPoint().get(pointId);
		if (trainPoint == null) {
			return;
		}
		int curLevel = mapB.getEventData().getLevel();
		int unlockLevel = 0;
		// 阶段奖励检查
		if (trainTreeMap.getMaxLevel() > curLevel) {
			for (Entry<Integer, List<Integer>> ent : trainTreeMap.getUnlockLevelMap().entrySet()) {
				if (ent.getKey() <= curLevel) {
					continue;
				}
				boolean have = false;
				for (Integer i : ent.getValue()) {
					if (mapB.getStarMapMap().containsKey(i)) {
						have = true;
						break;
					}
				}
				if (have) {
					if (unlockLevel < ent.getKey()) {
						unlockLevel = ent.getKey();
					}
				}
			}
			if (unlockLevel != 0 && unlockLevel > curLevel) {
				mapB.getEventDataBuilder().setLevel(unlockLevel);
//				onLevelEvent(playerId, mapB, unlockLevel, mapB.getEventDataBuilder().getEventIdList());
			}
		}
		// 通关事件检查
//		for (Entry<Integer, Integer> ent : trainTreeMap.getEventIdType().entrySet()) {
//			if (ent.getValue() != TrainingPointEventType.PASSPOINT) {
//				continue;
//			}
//			if (events.contains(ent.getKey())) {
//				continue;
//			}
//			TrainingMapEventObject obj = TrainingMapEvent.getById(ent.getKey());
//			if (obj == null) {
//				continue;
//			}
//			if (obj.getCustom().length < 2) {
//				continue;
//			}
//			int needStar = obj.getCustom()[0];
//			int needPoint = obj.getCustom()[1];
//			if (mapB.getStarMapMap().getOrDefault(needPoint, 0) >= needStar) {
//				mapB.getEventDataBuilder().addEventId(ent.getKey());
//				sendEventReward(mapB, playerId, ent.getKey(), pointId);
//			}
//		}

		// 羁绊顺序检查
//		for (Entry<Integer, Integer> ent : trainTreeMap.getEventIdType().entrySet()) {
//			if (ent.getValue() != TrainingPointEventType.LINEPASS) {
//				continue;
//			}
//			if (events.contains(ent.getKey())) {
//				continue;
//			}
//			TrainingMapEventObject obj = TrainingMapEvent.getById(ent.getKey());
//			if (obj == null) {
//				continue;
//			}
//			if (obj.getCustom().length < 2) {
//				continue;
//			}
//			int first = obj.getCustom()[0];
//			int second = obj.getCustom()[1];
//			if (mapB.getStarMapMap().getOrDefault(first, 0) > 0 && mapB.getStarMapMap().getOrDefault(second, 0) == 0) {
//				mapB.getEventDataBuilder().addEventId(ent.getKey());
//				sendEventReward(mapB, playerId, ent.getKey(), pointId);
//			}
//		}
//		}
		List<Integer> removeIds = new ArrayList<>();
		for (Integer i : trainPoint.getFather()) {
			int indexOf = trainTreeMap.getChange2Two().indexOf(i);
			if (indexOf != -1) {// 父节点为二选一
				List<Integer> allGroupList = trainTreeMap.getGroupMap().get(i);
				for (Integer group : allGroupList) {
					if (group != pointConf.getPointgroup()) {
						removeIds.addAll(trainTreeMap.getGroupAllMap().getOrDefault(group, new ArrayList<>()));
					}
				}
			}
		}

		if (removeIds.size() > 0) {
			mapB.getEventDataBuilder().addAllRemoveIds(removeIds);
		}
//		if (pointConf.getCallcp() != 0) {
//			mapB.getEventDataBuilder().addStrongPoint(pointConf.getCallcp());
//		}
		int total = mapB.getEventData().getLoseCountMap().getOrDefault(pointId, 0);
		mapB.getEventDataBuilder().putLoseCount(pointId, total + 1);
	}

	public void gmInfo(String playerId, int mapId) {
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			TrainDBMap.Builder tMap = cacheTemp.getTrainMapByMapId(mapId);
			TrainTreeMap cfgAfter = cfgMap.get(mapId);
			if (null == cfgAfter) {
				return;
			}
			TrainingMapObject ent = TrainingMap.getByMapid(mapId);
			if (ent == null) {
				return;
			}
			long lastEndTime = 0;
			for (TrainDBMap tdbm : cacheTemp.getInfoDB().getMapsList()) {
				if (tdbm.getCloseTime() > lastEndTime) {
					lastEndTime = tdbm.getCloseTime();
				}
			}

			long now = System.currentTimeMillis();
			if (now > lastEndTime) {
				lastEndTime = now;
			}
			for (int i = 0; i < cache.getInfoDB().getMapsCount(); i++) {
				TrainDBMap.Builder mapData = cacheTemp.getInfoDB().getMapsBuilderList().get(i);
				if (mapData != null && mapData.getMapId() == mapId) {
					cacheTemp.getInfoDB().removeMaps(i);
					break;
				}
			}
			TrainDBMap map = createTrainDBMap(ent, cfgAfter, System.currentTimeMillis(), petCache.getInstance().totalAbility(playerId));
			cacheTemp.getInfoDB().addMaps(map);
			TeamsUtil.updateTeamInfoTrain(playerId);
			sendMainPane(playerId, tMap);
			tMap.clear();
		});

	}

	private boolean checkCanMove(TrainDBMap.Builder tMap, TrainTreeMap ttmCfgAfter, TrainPoint nextPoint, TrainingPointObject tpoCfg, TrainingPointObject changeCfg) {
		if (tMap == null) {
			return false;
		}
		if (tMap.getStartId() == 0) {// 没选初始点
			return ttmCfgAfter.getStartIds().contains(nextPoint.getPid()); // 只能选初始点
		}
		int size = tMap.getGoNumList().size();
		// 挑战次数不能超过总星数
		if (tMap.getEventData().getRemoveIdsList().contains(nextPoint.getPid())) {
			return false;
		}
		// 已通过的点,可以随便移动
		int starMax = changeCfg != null ? changeCfg.getStarmax() : tpoCfg.getStarmax();
		if (tMap.containsStarMap(nextPoint.getPid()) && tMap.getStarMapOrDefault(nextPoint.getPid(), 0) >= starMax) {
			return true;
		}
		List<Integer> mustIds = new ArrayList<>();
		List<Integer> justOnIds = new ArrayList<>();
		if (nextPoint.getBranchPoint() != 0) {// 有分支点,可随意在各分支顺序移动
			mustIds.add(nextPoint.getBranchPoint());
		}
		// 解析prepoint(关卡ID:星级)
		int[][] prepoint = tpoCfg.getPrepoint();
		Map<Integer, Integer> map = new HashMap<>();
		if (prepoint != null && prepoint.length > 0) {
			for (int[] intArr : prepoint) {
				if (intArr.length < 1) {
					continue;
				}
				map.put(intArr[0], intArr[1]);
			}
		}
		mustIds.addAll(map.keySet());
		for (Integer i : nextPoint.getFather()) {
			justOnIds.add(i);
		}

		for (Integer i : mustIds) {
			if (tMap.getStarMapOrDefault(i, 0) < map.getOrDefault(i, 0)) {
				return false;
			}
		}
		boolean firstMove = size == 1; // 第一次移动
		for (Integer i : justOnIds) {
			if (i == tMap.getStartId()) {
				return firstMove; // 初始节点不能分叉
			}
			if (!firstMove && tMap.containsStarMap(i)) {
				return true;
			}
		}
		return firstMove;
	}

	/**
	 * 弹出BUFF选择框
	 */
	public void alertBuffChose(String playerId, int[] buffIds, int type) {
		SC_TrainAlertBuff.Builder msg = SC_TrainAlertBuff.newBuilder();
		for (Integer i : buffIds) {
			msg.addBuffIds(i);
		}
		msg.setType(type);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAlertBuff_VALUE, msg);
	}

	public void alertBuffChose(String playerId, List<Integer> buffIds, int type) {
		if (buffIds.size() <= 0) {
			return;
		}
		SC_TrainAlertBuff.Builder msg = SC_TrainAlertBuff.newBuilder();
		for (Integer i : buffIds) {
			msg.addBuffIds(i);
		}
		msg.setType(type);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAlertBuff_VALUE, msg);
	}

	/**
	 * 通过某个点
	 * 
	 * @param playerId
	 * @param tMap
	 * @param tpoCfg
	 */
	private TrainKV passPoint(String playerId, TrainDBMap.Builder tMap, TrainingPointObject tpoCfg, TrainTreeMap ttmCfgAfter, TrainingPointObject changePoint) {
		// 完成某个点，处理星级和通过次数，处理积分给与
		TrainKV pkv = null;
		List<TrainKV> goNums = new ArrayList<TrainKV>();
		int type = tpoCfg.getType();
		int[] rewardArr = tpoCfg.getAwards();
		int star = tMap.getStarMapOrDefault(tpoCfg.getPointid(), 0) + 1;
		int starMax = tpoCfg.getStarmax();
		int[] jifen = tpoCfg.getJifen();
		if (changePoint != null) {
			type = changePoint.getType();
			rewardArr = changePoint.getAwards();
			starMax = changePoint.getStarmax();
			jifen = changePoint.getJifen();
		}
		for (TrainKV kv : tMap.getGoNumList()) {
			TrainKV.Builder kvnew = TrainKV.newBuilder();
			kvnew.setKey(kv.getKey());
			if (kv.getKey() == tpoCfg.getPointid()) {
				kvnew.setVue(kv.getVue() + 1);
				pkv = kvnew.build();
			} else {
				kvnew.setVue(kv.getVue());
			}
			goNums.add(kvnew.build());
		}
		// 第一次通过点的时候增加通过记录
		if (null == pkv) {
			TrainKV.Builder pkvNew = TrainKV.newBuilder();
			pkvNew.setKey(tpoCfg.getPointid());
			pkvNew.setVue(1);
			pkv = pkvNew.build();
			goNums.add(pkv);
		}
		if (type == TrainPointType.BOSS) {
			if (tMap.getCurBoss() < tpoCfg.getLevel()) {
				tMap.setCurBoss(tpoCfg.getLevel());
			}
		}
		if (ttmCfgAfter.getStartIds().contains(tpoCfg.getPointid())) {
			tMap.setStartId(tpoCfg.getPointid());
		}
		if (star > starMax) {
			star = starMax;
		}
		tMap.putStarMap(tpoCfg.getPointid(), star);
		tMap.clearGoNum();
		tMap.addAllGoNum(goNums);
		// 处理星星

		int passNum = pkv.getVue();
		if (starMax > 0 && passNum <= starMax) {
			// 星级还没有满需要给与星星
			tMap.setStarNum(tMap.getStarNum() + passNum);
		}
		passNum -= 1;
		// 处理积分
		if (type == TrainPointType.BLOODMONSTER) {
			EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_TrainScore, getJifen(tMap), tMap.getMapId());
		} else if ((jifen.length > 0 && passNum < jifen.length)) {
			tMap.setJifenRefTime(GlobalTick.getInstance().getCurrentTime());
			// 此次增加基数*(1+百分比增量)
			int addScore = (jifen[passNum]) * (1 + tMap.getJifenPer() / 100);

			int finalJiFen = tMap.getJifen() +addScore;

			tMap.setJifen(finalJiFen);

			finalJiFen = getJifen(tMap);
			EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_TrainScore, finalJiFen, tMap.getMapId());

			// 刷新缓存
			refJifen(tpoCfg.getMapid(), playerId, tMap.getStarNum(), finalJiFen, tMap.getJifenRefTime(), true);
		}
		if (tpoCfg.getChangepoint().length >= 2) {
			int tarPoint = tpoCfg.getChangepoint()[0];
			int changePointNum = tpoCfg.getChangepoint()[1];
			if (TrainingPoint.getByPointid(tarPoint) != null) {
				TrainingPointObject pointCfg = TrainingPoint.getByPointid(tarPoint);
				if (tMap.getStarMapMap().getOrDefault(tarPoint, 0) <= pointCfg.getStarmax()) {
					if (TrainingPoint.getByPointid(changePointNum) != null) {
						tMap.putChangePoint(tarPoint, changePointNum);
					}
				}
			}
		}

		// 给与奖励
		if (rewardArr.length > 0 && passNum < rewardArr.length) {

			int rewardId = rewardArr[passNum];
			List<Reward> rewards = RewardUtil.getRewardsByRewardId(rewardId);

			if (null != rewards) {
				boolean isShow = true;
				if (type == TrainPointType.MONSTER_NORMAL || type == TrainPointType.MONSTER_BETTER || type == TrainPointType.BOSS) {
					isShow = false;
				}
				countRewards(tMap, rewards);
				RewardManager.getInstance().doRewardByList(playerId, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_TRAIN_POINT), isShow);
			}
		}
		onPassEvent(playerId, tMap, tpoCfg.getPointid(), tMap.getEventDataBuilder().getEventIdList());

		return pkv;
	}

	/**
	 * 获取排行榜信息
	 * 
	 * @param playerId
	 * @param mapId
	 */
	public void getRankInfo(String playerId, int mapId) {
		SC_TrainRank.Builder msg = SC_TrainRank.newBuilder();
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RCE_Success);
		msg.setResult(retCode);
		if (!isOpenFunction(playerId)) {
			retCode.setRetCode(RetCodeEnum.RCE_FunctionIsLock);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainRank_VALUE, msg);
			return;
		}
//		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
//		if (null == cache || cache.getInfoDB().getCurrMap() != mapId) {
//			// 第一次初始化玩法数据
//			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
//			msg.setResult(retCode);
//			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainRank_VALUE, msg);
//			return;
//		}
		TrainRankMap trm = jifenData.get(mapId);
		if (null == trm || trm.getSort().isEmpty()) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainRank_VALUE, msg);
			return;
		}
		int i = 1;
		int selfRank = 0;
		for (TrainRankSortInfo one : trm.getSort()) {
			TrainRankInfo.Builder msg1 = TrainRankInfo.newBuilder();
			msg1.setPlayerIdx(one.getPlayerId());
			playerEntity player = playerCache.getByIdx(one.getPlayerId());
			if (null != player) {
				msg1.setName(player.getName());
				msg1.setHead("" + player.getAvatar());
				if (player.getIdx().equals(playerId)) {
					selfRank = i;
				}
			}
			msg1.setJifen(one.getJifen());
			msg1.setStarNum(one.getStar());
			msg1.setRank(i);
			msg.addInfos(msg1);
			if (i >= 100) {
				break;
			}
			i++;
		}
		if (selfRank <= 0) {
			selfRank = trm.getRankById(playerId);
		}
		msg.setSelfRank(selfRank);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainRank_VALUE, msg);
	}

	public int getTotalRankSize(int mapId) {
		TrainRankMap trm = jifenData.get(mapId);
		if (null == trm) {
			return 0;
		}
		return trm.getSort().size();
	}

	public int getPlayerRank(String playerId, int mapId) {
		int selfRank = -1; // 平台用 从-1为未上榜
		TrainRankMap trm = jifenData.get(mapId);
		if (null == trm || trm.getSort().isEmpty()) {
			return -1;
		}
		int i = 1;
		for (TrainRankSortInfo one : trm.getSort()) {
			playerEntity player = playerCache.getByIdx(one.getPlayerId());
			if (null != player && player.getIdx().equals(playerId)) {
				selfRank = i;
				break;
			}
			i++;
		}
		return selfRank;
	}

	public Map<Integer, TrainRankSortInfo> getTrainRankData(int mapId, int startIndex, int queryNum) {
		TrainRankMap trm = jifenData.get(mapId);
		if (null == trm || trm.getSort().isEmpty()) {
			return null;
		}
		if (startIndex >= trm.getSort().size()) {
			return null;
		}
		int index = startIndex - 1;
		int maxIndex = Math.min(trm.getSort().size(), index + queryNum);
		Map<Integer, TrainRankSortInfo> rankMap = null;
		for (; index < maxIndex; index++) {
			if (rankMap == null) {
				rankMap = new HashMap<>();
			}
			rankMap.put(index + 1, trm.getSort().get(index));
		}
		return rankMap;
	}

	/**
	 * 查看当前拥有的BUFF
	 * 
	 * @param playerId
	 * @param mapId
	 */
	public void getBuffAll(String playerId, int mapId) {
		SC_TrainBuffAll.Builder msg = SC_TrainBuffAll.newBuilder();
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RCE_Success);
		if (!isOpenFunction(playerId)) {
			retCode.setRetCode(RetCodeEnum.RCE_FunctionIsLock);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainBuffAll_VALUE, msg);
			return;
		}
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		TrainDBMap.Builder tMap = cache.getTrainMapByMapId(mapId);
		if (null == cache || cache.getInfoDB().getCurrMap() != mapId || null == tMap) {
			// 第一次初始化玩法数据
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainBuffAll_VALUE, msg);
			return;
		}
		msg.setResult(retCode);
		for (TrainKV ent : tMap.getUsebuffList()) {
			msg.addIds(ent.getVue());
		}
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainBuffAll_VALUE, msg);
	}

	/**
	 * BUFF选择
	 * 
	 * @param playerId
	 * @param buffid
	 */
	public void choseBuff(String playerId, int mapId, int buffid, int type) {
		SC_TrainAlertBuffChose.Builder msg = SC_TrainAlertBuffChose.newBuilder();
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RCE_Success);
		if (!isOpenFunction(playerId)) {
			retCode.setRetCode(RetCodeEnum.RCE_FunctionIsLock);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAlertBuffChose_VALUE, msg);
			return;
		}
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache || cache.getInfoDB().getCurrMap() != mapId) {
			// 第一次初始化玩法数据
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAlertBuffChose_VALUE, msg);
			return;
		}
		TrainDBMap.Builder tMap = cache.getCurTrainMap();
		if (null == tMap) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAlertBuffChose_VALUE, msg);
			return;
		}
		List<Integer> buffIds;
		if (type == 0) {
			if (tMap.getIsBlessing() % 10 != 1) {
				retCode.setRetCode(RetCodeEnum.RCE_Activity_RewardAlreadyClaim);
				msg.setResult(retCode);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAlertBuffChose_VALUE, msg);
				return;
			}
			buffIds = tMap.getBuffIdsList();
		} else {
			if (tMap.getTraincardList().size() <= 0) {
				retCode.setRetCode(RetCodeEnum.RCE_Activity_RewardAlreadyClaim);
				msg.setResult(retCode);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAlertBuffChose_VALUE, msg);
				return;
			}
			buffIds = tMap.getTraincardList().get(0).getCardIdList();
		}

		boolean isBuff = false;
		for (int bid : buffIds) {
			if (bid == buffid) {
				isBuff = true;
				break;
			}
		}
		if (!isBuff) {
			// BUFF不对
			retCode.setRetCode(RetCodeEnum.RCE_ConfigError);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAlertBuffChose_VALUE, msg);
			return;
		}
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			TrainDBMap.Builder dbMap = cacheTemp.getCurTrainMap();
//			 增加一个BUFF
//			addPlayerMapPointDBdata(playerId, tMapChange, tMap.getCurrPos(), buffid, 0);

			// 增加一个祝福卡
			addCard(dbMap, buffid);

			// 选择完成修改祝福状态
			if (type == 0) {
				int bless = dbMap.getIsBlessing() / 10 * 10 + 0;
				dbMap.setIsBlessing(bless);
				dbMap.clearBuffIds();
			} else if (type == 1) {
				List<TrainCardChoice> list = new ArrayList<>();
				for (int i = 1; i < dbMap.getTraincardList().size(); i++) {
					list.add(dbMap.getTraincardList().get(i));
				}
				dbMap.clearTraincard();
				dbMap.addAllTraincard(list);
			}
		});
		updateMainPane(playerId, tMap, 0, null);
		msg.setResult(retCode);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAlertBuffChose_VALUE, msg);
	}

	public void addCard(TrainDBMap.Builder tMapChange, int cardId) {
		TrainingLuckObject obj = TrainingLuck.getById(cardId);
		if (obj == null) {
			return;
		}
		TrainLuckData.Builder b = null;
		if (tMapChange.getLuckDataMap().containsKey(cardId)) {
			b = tMapChange.getLuckDataMap().get(cardId).toBuilder();

		} else {
			b = TrainLuckData.newBuilder();
			b.setId(cardId);
			b.setType(obj.getType());
			b.setLevel(obj.getLevel());
		}
		switch (obj.getType()) {
		case TrainingLuckType.TYPE_BUFF_MORE:
			// 叠加BUFF,重复获得叠加一次
			if (obj.getConfigformat().length < 1) {
				return;
			}
			if (obj.getConfigformat()[0].length < 3) {
				return;
			}
			if (b.getParam1() < obj.getConfigformat()[0][2]) {
				b.setParam1(b.getParam1() + 1);
			}
			// 存储最高级别叠加buff
			if (tMapChange.getBuffFlagMap().containsKey(obj.getBuffflag())) {
				TrainLuckData.Builder trainLuckData = tMapChange.getBuffFlagMap().get(obj.getBuffflag()).toBuilder();
				if (trainLuckData.getLevel() < obj.getLevel()) {// 高等级覆盖低等级
					trainLuckData.setId(obj.getId());
					trainLuckData.setLevel(obj.getLevel());
					if (trainLuckData.getParam1() > obj.getConfigformat()[0][2]) {
						trainLuckData.setParam1(obj.getConfigformat()[0][2]);
					}
					tMapChange.putBuffFlag(b.getId(), trainLuckData.build());
				} else if (trainLuckData.getLevel() == obj.getLevel()) {// 同等级叠加
					if (trainLuckData.getParam1() >= obj.getConfigformat()[0][2]) {
						trainLuckData.setParam1(obj.getConfigformat()[0][2]);
					} else {
						trainLuckData.setParam1(trainLuckData.getParam1() + 1);
					}
					tMapChange.putBuffFlag(b.getId(), trainLuckData.build());
				}
			} else {
				tMapChange.putBuffFlag(b.getId(), b.build());
			}
			break;
		case TrainingLuckType.TYPE_LIMIT_TIME:
			// 限定次数重复获得刷新次数
			if (obj.getConfigformat().length < 1) {
				return;
			}
			if (obj.getConfigformat()[0].length < 1) {
				return;
			}
			b.setParam1(obj.getConfigformat()[0][0]);
			break;
		case TrainingLuckType.TYPE_CHANGE_WEIGHT:

			if (obj.getConfigformat().length < 1) {
				return;
			}
			for (int[] t : obj.getConfigformat()) {
				if (t.length < 2) {
					continue;
				}
				tMapChange.putCardWeightChange(t[0], tMapChange.getCardWeightChangeMap().getOrDefault(t[0], 0) + t[1]);
			}
			break;
		case TrainingLuckType.TYPE_JIFEN:
			if (obj.getConfigformat().length < 1) {
				return;
			}
			if (obj.getConfigformat()[0].length < 1) {
				return;
			}
			b.setParam1(obj.getConfigformat()[0][0]);
			if (obj.getConfigformat()[0][0] > tMapChange.getJifenPer()) {
				tMapChange.setJifenPer(obj.getConfigformat()[0][0]);
			}
			break;
		case TrainingLuckType.TYPE_NORMAL_BUFF:
			if (obj.getConfigformat().length < 1) {
				return;
			}
			if (obj.getConfigformat()[0].length < 1) {
				return;
			}
			// 普通BUFF卡额外有BUFF
			b.setParam1(obj.getConfigformat()[0][0]);
			TrainKV.Builder pkvNew = TrainKV.newBuilder();
			pkvNew.setKey(obj.getId());
			pkvNew.setVue(obj.getConfigformat()[0][0]);
			tMapChange.addUsebuff(pkvNew);
			break;
		default:
			break;
		}

		if (obj.getBuff() > 0) {
			TrainKV.Builder pkvNew = TrainKV.newBuilder();
			pkvNew.setKey(cardId);
			pkvNew.setVue(obj.getBuff());
			tMapChange.addUsebuff(pkvNew);
		}
		tMapChange.putLuckData(cardId, b.build());
		tMapChange.addLuckbuffid(cardId);

		addCardTimes(tMapChange, TrainingLuckType.BUFF_MORE_CARD, 1);
	}

	public void addCardTimes(TrainDBMap.Builder tMap, int subType, int num) {
		for (Entry<Integer, TrainLuckData> ent : tMap.getBuffFlagMap().entrySet()) {
			TrainingLuckObject config = TrainingLuck.getById(ent.getKey());
			if (config == null) {
				continue;
			}
			if (config.getType() != TrainingLuckType.TYPE_BUFF_MORE) {
				continue;
			}
			if (config.getConfigformat().length < 1) {
				continue;
			}
			if (config.getConfigformat()[0].length < 3) {
				continue;
			}
			if (config.getConfigformat()[0][0] != subType) {
				continue;
			}
			if (ent.getValue().getParam1() >= config.getConfigformat()[0][2]) {
				continue;
			}
			int max = config.getConfigformat()[0][2] - ent.getValue().getParam1();
			if (num > max) {
				num = max;
			}
			TrainLuckData.Builder builder = ent.getValue().toBuilder();
			builder.setParam1(builder.getParam1() + num);
			tMap.putBuffFlag(builder.getId(), builder.build());
		}
	}

	/**
	 * 获取助战宠物
	 * 
	 * @param playerId
	 * @param petId
	 * @return
	 */
	public Pet getVirtualPet(String playerId, String petId) {
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			return null;
		}
		TrainDBMap.Builder tMap = cache.getCurTrainMap();
		if (null == tMap || cache.getInfoDB().containsEndMap(tMap.getMapId())) {
			return null;
		}
		for (TrainHelpPetData dbPet : tMap.getMlidsList()) {
			String petIdx = createPetId(dbPet.getPointId(), dbPet.getPetCfgId());
			if (petIdx.equals(petId)) {
				return createPet(dbPet.getPetCfgId(), petIdx, dbPet.getPetLevel(), dbPet.getPetRarity(), dbPet.getPetUpLevel());
			}
		}
		return null;
	}

	private String createPetId(int pointId, int mlid) {
		return pointId + "_" + mlid;
	}

	/**
	 * 创建助战魔灵
	 * 
	 * @param mlId
	 */
	private Pet createPet(int mlId, String key, int level, int petRarity, int petUpLevel) {
		PetBasePropertiesObject cfgPet = PetBaseProperties.getByPetid(mlId);
		if (null == cfgPet) {
			return null;
		}
		// TODO 等级修改
		// 取玩家最高品质、最高等级、最高觉醒等级随机宠物
		Pet.Builder pet = petCache.getInstance().getPetBuilder(cfgPet, Common.RewardSourceEnum.RSE_TRAIN_POINT_VALUE);
		// 修改等级
		pet.setId(key);
		pet.setPetLvl(level);
		pet.setPetRarity(petRarity);
		pet.setPetUpLvl(petUpLevel);
		// 刷新属性
		petCache.getInstance().refreshPetData(pet, null);
		return pet.build();
	}

	/**
	 * 修改点数据
	 * 
	 * @param playerId
	 * @param tMap
	 * @param pointId
	 * @param bid
	 * @param moling
	 */
	private void addPlayerMapPointDBdata(String playerId, TrainDBMap.Builder tMap, int pointId, int bid, int[] moling) {
		if (bid > 0) {
			TrainKV.Builder pkvNew = TrainKV.newBuilder();
			pkvNew.setKey(pointId);
			pkvNew.setVue(bid);
			tMap.addUsebuff(pkvNew);
		}
		if (moling != null && moling.length > 3) {
			TrainHelpPetData.Builder helpPetData = TrainHelpPetData.newBuilder();
			helpPetData.setPointId(pointId);
			helpPetData.setPetCfgId(moling[0]);
			helpPetData.setPetLevel(moling[1]);
			helpPetData.setPetRarity(moling[2]);
			helpPetData.setPetUpLevel(moling[3]);
			tMap.addMlids(helpPetData);
		}
	}

	/**
	 * 已经获取的累计奖励
	 * 
	 * @param playerId
	 * @param mapId
	 */
	public void getAwardAll(String playerId, int mapId) {
		SC_TrainAwardAll.Builder msg = SC_TrainAwardAll.newBuilder();
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RCE_Success);
		if (!isOpenFunction(playerId)) {
			retCode.setRetCode(RetCodeEnum.RCE_FunctionIsLock);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAwardAll_VALUE, msg);
			return;
		}
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache || cache.getInfoDB().getCurrMap() != mapId) {
			// 第一次初始化玩法数据
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAwardAll_VALUE, msg);
			return;
		}
		msg.setResult(retCode);
		TrainDBMap.Builder tMap = cache.getTrainMapByMapId(mapId);
		msg.addAllReward(tMap.getRewardList());
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAwardAll_VALUE, msg);
	}

	/**
	 * 重置路劲
	 * 
	 * @param playerId
	 * @param isAgree
	 */
	public void reset(String playerId, int mapId, int isAgree, int pointId) {
		SC_TrainReset.Builder msg = SC_TrainReset.newBuilder();
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RCE_Success);
		boolean giveUp = true;
		if (giveUp) {
			retCode.setRetCode(RetCodeEnum.RCE_LvNotEnough);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainReset_VALUE, msg);
			return;
		}
		if (!isOpenFunction(playerId)) {
			retCode.setRetCode(RetCodeEnum.RCE_FunctionIsLock);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainReset_VALUE, msg);
			return;
		}
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache || cache.getInfoDB().getCurrMap() != mapId) {
			// 第一次初始化玩法数据
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainReset_VALUE, msg);
			return;
		}
		TrainDBMap.Builder tMap1 = cache.getCurTrainMap();
		TrainTreeMap cfgAfter = cfgMap.get(mapId);
		if (null == tMap1 || null == cfgAfter || tMap1.getCurrPos() == pointId) {
			retCode.setRetCode(RetCodeEnum.RCE_FunctionIsLock);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainReset_VALUE, msg);
			return;
		}
		List<Integer> currpath = tMap1.getCurpathList();
		if (null == currpath) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainReset_VALUE, msg);
			return;
		}
		// 判断该点是否可以重置
		if (!currpath.contains(pointId)) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_POSERROR);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainReset_VALUE, msg);
			return;
		}
		// 生成可重置的点
		List<Integer> canreset = new ArrayList<Integer>();
		boolean isreset = false;
		for (int pid : currpath) {
			if (isreset) {
				canreset.add(pid);
				continue;
			}
			if (pid == pointId) {
				isreset = true;
			}
		}
		// 同意重置，回到重置点清除部分数据
		if (isAgree == 1) {

		}
		msg.setResult(retCode);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainReset_VALUE, msg);
	}

	/**
	 * 获取可以重置的点
	 * 
	 * @param mapId
	 * @param pointCurr
	 * @return
	 */
	private List<Integer> getCanResetPoint(trainingEntity cache, int mapId, int pointCurr) {/*
																							 */
		return new ArrayList<>();
	}

	public PatrolBattleResult getFightMakeId(String playerId, int mapId, int pointId) {

		PatrolBattleResult result = new PatrolBattleResult();
		if (pointId >= 10000000) {
			result.setCode(RetCodeEnum.RCE_ErrorParam);
			return result;
		}
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache || cache.getInfoDB().getCurrMap() != mapId) {
			result.setCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			return result;
		}
		TrainDBMap.Builder tMap = cache.getTrainMapByMapId(mapId);
		if (tMap == null || !trainingMapIsOpen(tMap)) {
			result.setCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			return result;
		}
		TrainTreeMap ttmCfgAfter = cfgMap.get(mapId);
		TrainingPointObject changePointCfg = null;
		if (tMap.getChangePointMap().containsKey(pointId)) {
			changePointCfg = TrainingPoint.getByPointid(tMap.getChangePointMap().get(pointId));
		}
		TrainingPointObject tpoCfg = TrainingPoint.getByPointid(pointId);
		if (null == ttmCfgAfter || null == tpoCfg) {
			result.setCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			return result;
		}

		if (!checkBloodMonster(tMap, pointId, changePointCfg == null ? tpoCfg : changePointCfg)) {
			result.setCode(RetCodeEnum.RCE_TRAIN_POSERROR);
			return result;
		}
		if (tMap.getNpcEventId() != 0) {
			result.setCode(RetCodeEnum.RCE_TRAIN_NotFinishNpcEvent);
			return result;
		}
		if (tMap.getBuffIdsList().size() > 0) {
			result.setCode(RetCodeEnum.RCE_TRAIN_NotFinishChoseBUFF);
			return result;
		}
		TrainPoint trainPoint = ttmCfgAfter.getAllPoint().get(pointId);
		if (trainPoint == null) {
			result.setCode(RetCodeEnum.RCE_TRAIN_POSERROR);
			return result;
		}
		// 这里做了处理,通过的点可以移动,所以下面需要单独判断实际通关次数passnum
		if (!checkCanMove(tMap, ttmCfgAfter, trainPoint, tpoCfg, changePointCfg)) {
			result.setCode(RetCodeEnum.RCE_TRAIN_POSNOTFLISH);
			return result;
		}

		int type = tpoCfg.getType();
		int[] fightMakeIdArr = tpoCfg.getFightmakeid();
		if (changePointCfg != null) {
			type = changePointCfg.getType();
			fightMakeIdArr = changePointCfg.getFightmakeid();
		}
		if (fightMakeIdArr.length <= 0) {
			return result;
		}
		if (type != TrainPointType.MONSTER_NORMAL && type != TrainPointType.MONSTER_BETTER && type != TrainPointType.BOSS && type != TrainPointType.BLOODMONSTER) {
			result.setCode(RetCodeEnum.RCE_TRAIN_POSERROR);
			return result;
		}
		// 获取点位通过次数
		int passNum = 0;
		for (TrainKV kv : tMap.getGoNumList()) {
			if (kv.getKey() == tpoCfg.getPointid()) {
				passNum = kv.getVue();
			}
		}
		int fightMakeId = 0;
		if (type != TrainPointType.BLOODMONSTER && passNum >= fightMakeIdArr.length) {
			result.setCode(RetCodeEnum.RCE_TRAIN_POSERROR);
			return result;
		}
		if (passNum >= fightMakeIdArr.length) {
			passNum = fightMakeIdArr.length - 1;
		}
		fightMakeId = fightMakeIdArr[passNum];
		result.setMakeId(fightMakeId);

		// 合并buff效果，传递给前端使用
		Map<Integer, Integer> buffid = new HashMap<Integer, Integer>();

		// 隐藏BUFF
		// 总卡片
		int totalCardNum = tMap.getLuckbufftime();
		// 品质数量

		Map<Integer, Integer> gradeMap = new HashMap<>();
		gradeMap.put(0, totalCardNum);
		petEntity petEntity = petCache.getInstance().getEntityByPlayer(playerId);
		if (petEntity == null || CollectionUtils.isEmpty(petEntity.peekAllPetByUnModify())) {
			result.setCode(RetCodeEnum.RCE_PrepareWar_PetNoExist);
			return result;
		}

		// 核心魔灵数量
		int mainPet = 0;
		int midPetLv = 0;
		int petNum = 0;
		int levelCount = 0;
		for (Pet pet : petEntity.peekAllPetByUnModify()) {
			levelCount += pet.getPetLvl();
			petNum++;
			if (!gradeMap.containsKey(pet.getPetRarity())) {
				gradeMap.put(pet.getPetRarity(), 0);
				if (PetBaseProperties.isCorePet(pet.getPetBookId())) {
					mainPet++;
				}
			}
			gradeMap.put(pet.getPetRarity(), gradeMap.get(pet.getPetRarity()) + 1);
		}
		// 平均等级
		midPetLv = levelCount / petNum;

		// 每张祝福卡都有固定BUFF
		for (TrainKV kv : tMap.getUsebuffList()) {
			int total = buffid.getOrDefault(kv.getVue(), 0) + 1;
			buffid.put(kv.getVue(), total);
		}

		for (Entry<Integer, Integer> ent : tMap.getPrepareUseMap().entrySet()) {
			TrainBuffBag buffBag = tMap.getBuffBag();
			for (TrainBuffData data : buffBag.getBuffList()) {
				if (data.getItemId() == ent.getKey()) {
					buffid.put(data.getBuffId(), buffid.getOrDefault(data.getBuffId(), 0) + data.getNum());
					break;
				}
			}
		}

		// 叠加BUFF
		for (Entry<Integer, TrainLuckData> ent : tMap.getBuffFlagMap().entrySet()) {
			if (ent.getValue().getType() == TrainingLuckType.TYPE_BUFF_MORE) {
				TrainingLuckObject luck = TrainingLuck.getById(ent.getValue().getId());
				if (luck == null) {
					continue;
				}
				if (luck.getConfigformat().length < 1) {
					continue;
				}
				if (luck.getConfigformat()[0].length < 3) {
					continue;
				}

				int buffId = luck.getConfigformat()[0][1];
				buffid.put(buffId, buffid.getOrDefault(buffId, 0) + ent.getValue().getParam1());
			}
		}
		// 限定BUFF
		for (Entry<Integer, TrainLuckData> ent : tMap.getLuckDataMap().entrySet()) {
			if (ent.getValue().getType() == TrainingLuckType.TYPE_LIMIT_TIME) {
				TrainingLuckObject luck = TrainingLuck.getById(ent.getValue().getId());
				if (luck == null) {
					continue;
				}
				if (luck.getConfigformat().length < 1) {
					continue;
				}
				if (luck.getConfigformat()[0].length < 3) {
					continue;
				}
				int targetType = luck.getConfigformat()[0][2];
				if (targetType != -1) {// 针对特定类型生效
					if (targetType != tpoCfg.getType()) {
						continue;
					}
				}
				if (ent.getValue().getParam1() <= 0) {
					continue;
				}
				int buffId = luck.getConfigformat()[0][1];
				buffid.put(buffId, buffid.getOrDefault(buffId, 0) + 1);
			}
		}
		Map<Integer, Integer> hideBuff = getHideBuff(gradeMap, pointId, tMap.getPrepareUseMap(), midPetLv, mapId);
		for (Integer buff : hideBuff.values()) {
			buffid.put(buff, buffid.getOrDefault(buff, 0) + 1);
		}
		for (Entry<Integer, Integer> bid : buffid.entrySet()) {

			TrainingBuffObject obj = TrainingBuff.getById(bid.getKey());

			if (obj != null && obj.getBuffcamp() == 0) {
				PetBuffData.Builder dataBuilder = PetBuffData.newBuilder();
				dataBuilder.setBuffCount(bid.getValue());
				dataBuilder.setBuffCfgId(bid.getKey());
				result.adddebuff(dataBuilder.build());
			} else {
				PetBuffData.Builder dataBuilder = PetBuffData.newBuilder();
				dataBuilder.setBuffCount(bid.getValue());
				dataBuilder.setBuffCfgId(bid.getKey());
				result.addBuff(dataBuilder.build());
			}
		}
		result.setSuccess(true);
		return result;
	}

	private Map<Integer, Integer> getHideBuff(Map<Integer, Integer> gradeMap, int pointId, Map<Integer, Integer> preUse, int midPetLv, int mapId) {
		int mainPet = 0;
		Map<Integer, Integer> map = new HashMap<>();
		TrainTreeMap conf = cfgMap.get(mapId);
		if (conf == null) {
			return map;
		}
		for (Entry<Integer, List<TrainingLuckHideBuffObject>> ent : conf.getHiddBuff().entrySet()) {
			Map<Integer, Integer> weight = new HashMap<>();
			for (TrainingLuckHideBuffObject obj : ent.getValue()) {

				int temWeight = weight.getOrDefault(obj.getType(), 0);
				if (temWeight >= obj.getWeight()) {
					continue;
				}
				int[] confArr = obj.getConfigformat();
				boolean suc = false;
				switch (obj.getType()) {
				case TrainingHideBuffType.ZHUFUKA:
					if (confArr.length < 2) {
						break;
					}
					int grade = confArr[0];
					int num = confArr[1];
					int haveNum = gradeMap.getOrDefault(grade, 0);
					if (haveNum >= num) {
						suc = true;
					}
					break;
				case TrainingHideBuffType.POINTITEM:
					if (confArr.length < 2) {
						break;
					}
					int tarPoint = confArr[0];
					int tarItem = confArr[1];

					if (pointId != tarPoint) {
						break;
					}
					if (!preUse.containsKey(tarItem)) {
						break;
					}
					suc = true;
					break;
				case TrainingHideBuffType.MOLINLV:
					if (confArr.length < 1) {
						break;
					}
					int needMidLv = confArr[0];
					if (midPetLv >= needMidLv) {
						suc = true;
					}
					break;
				case TrainingHideBuffType.MOLINGRADE:
					if (confArr.length < 2) {
						break;
					}
					int needGrade = confArr[0];
					int needNum = confArr[1];
					int temGradeNum = gradeMap.getOrDefault(needGrade, 0);
					if (temGradeNum >= needNum) {
						suc = true;
					}
					break;
				case TrainingHideBuffType.MOLINMAIN:
					if (confArr.length < 1) {
						break;
					}
					int needMainPetNum = confArr[0];
					if (mainPet >= needMainPetNum) {
						suc = true;
					}
					break;

				default:
					break;
				}
				if (suc) {
					weight.put(obj.getType(), obj.getWeight());
					map.put(obj.getType(), obj.getBuff());
				}
			}
		}
		return map;
	}

	public List<Reward> getBattleReward(String playerId, int mapId, int pointId) {
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache || cache.getInfoDB().getCurrMap() != mapId) {
			return new ArrayList<Reward>();
		}
		TrainingPointObject tpoCfg = TrainingPoint.getByPointid(pointId);
		if (null == tpoCfg) {
			return new ArrayList<Reward>();
		}
		TrainDBMap.Builder tMap = cache.getTrainMapByMapId(mapId);
		if (null == tMap) {
			return new ArrayList<Reward>();
		}
		TrainingPointObject changePoint = null;
		if (tMap.getChangePointMap().containsKey(pointId)) {
			changePoint = TrainingPoint.getByPointid(tMap.getChangePointMap().get(pointId));
		}
		int passNum = 0;
		for (TrainKV kv : tMap.getGoNumList()) {
			if (kv.getKey() == tpoCfg.getPointid()) {
				passNum = kv.getVue();
			}
		}
		int[] rewardData = changePoint == null ? tpoCfg.getAwards() : changePoint.getAwards();
		if (rewardData != null && rewardData.length > 0 && passNum < rewardData.length) {
			int rewardId = rewardData[passNum];
			List<Reward> rewards = RewardUtil.getRewardsByRewardId(rewardId);
			if (null == rewards) {
				return new ArrayList<Reward>();
			}
			return rewards;
		}
		return new ArrayList<Reward>();
	}

	public void battleSettle(String playerId, int battleResult, List<Reward> rewardList, int mapId, int pointId, int hprate) {
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache || cache.getInfoDB().getCurrMap() != mapId) {
			return;
		}
		TrainingPointObject pointObj = TrainingPoint.getByPointid(pointId);
		if (pointObj == null) {
			return;
		}
		// 胜利扣除使用的道具
		if (battleResult == 1) {
			SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
				TrainDBMap.Builder tMap = cacheTemp.getTrainMapByMapId(mapId);
//			TrainItemBag.Builder itemBagB = tMap.getItemBagBuilder();
//			List<TrainItemData> itemList = itemBagB.getItemList();

				List<TrainItemData> newList = new ArrayList<>();
				for (TrainItemData data : tMap.getItemBag().getItemList()) {
					int total = data.getNum();
					if (tMap.getPrepareUseMap().containsKey(data.getItemId())) {
						total -= tMap.getPrepareUseMap().get(data.getItemId());
					}
					if (total <= 0) {
						total = 0;
					} else {
						TrainItemData newdata = data.toBuilder().setNum(total).build();
						newList.add(newdata);
					}
				}

				// 限定BUFF
				for (Entry<Integer, TrainLuckData> ent : tMap.getLuckDataMap().entrySet()) {
					if (ent.getValue().getType() == TrainingLuckType.TYPE_LIMIT_TIME) {
						TrainingLuckObject luck = TrainingLuck.getById(ent.getValue().getId());
						if (luck == null) {
							continue;
						}
						if (luck.getConfigformat().length < 1) {
							continue;
						}
						if (luck.getConfigformat()[0].length < 3) {
							continue;
						}
						int targetType = luck.getConfigformat()[0][2];
						if (targetType != 0) {// 针对特定类型生效
							if (targetType != pointObj.getType()) {
								continue;
							}
						}
						if (ent.getValue().getParam1() <= 0) {
							continue;
						}
						int num = ent.getValue().getParam1() - 1;
						TrainLuckData.Builder setParam1 = ent.getValue().toBuilder().setParam1(num);
						tMap.putLuckData(setParam1.getId(), setParam1.build());
					}
				}
				teamEntity teams = teamCache.getInstance().getTeamEntityByPlayerId(playerId);
				if (teams != null) {
					Team dbTeam = teams.getDBTeam(TeamNumEnum.TNE_Training_1);
					List<Integer> removeMl = new ArrayList<>();
					for (Map.Entry<Integer, String> ent : dbTeam.getLinkPetMap().entrySet()) {
						String[] split = ent.getValue().split("_");
						if (split.length < 2) {
							continue;
						}
						String pre = split[0];
						int id = NumberUtils.toInt(pre);
//					if (id > 10000000) {// 助战魔灵
						if (!tMap.getMlTimesMap().containsKey(id + "")) {
							tMap.putMlTimes(id + "", 1);
						} else {
							int times = tMap.getMlTimesMap().get(id + "");
							tMap.putMlTimes(id + "", times + 1);
							if (times + 1 >= GameConfig.getById(GameConst.CONFIG_ID).getTrainhelpcount()) {
								removeMl.add(id);
							}
						}
//					}
					}
					List<TrainHelpPetData> newMlList = new ArrayList<>();
					for (TrainHelpPetData petData : tMap.getMlidsList()) {
						if (removeMl.contains(petData.getPointId())) {
							continue;
						}
						newMlList.add(petData);
					}
					tMap.clearMlids();
					tMap.addAllMlids(newMlList);
					if (removeMl.size() > 0) {
						TeamsUtil.updateTeamInfoTrain(playerId);
//						sendMainPane(playerId, tMap, tMap.getCurpathList());
					}
				}

				tMap.clearBuffBag();
				tMap.getItemBagBuilder().clearItem();
				tMap.getItemBagBuilder().addAllItem(newList);
				tMap.clearPrepareUse();
				addCardTimes(tMap, TrainingLuckType.BUFF_MORE_WIN, 1);
				sendItemBag(playerId, tMap.getItemBagBuilder(), tMap.getBuffBagBuilder());
			});
		}

		// 处理投降(还原状态)
		if (battleResult == 3) {
//			onLoseEvent(playerId, pointId, mapId);
			return;
		}
		boolean bloodMonster = pointObj.getType() == TrainPointType.BLOODMONSTER;
		boolean victory = battleResult == 1;
		if (bloodMonster) {// 血量怪没有失败
			victory = true;
		}
//        String event = victory ? PatrolEvent.BATTLE_VICTORY : PatrolEvent.BATTLE_FAILED;
		// 胜利处理
		if (victory) {
			// 战斗胜利移动至该点
			going(playerId, mapId, pointId, -1, false, false, hprate);
		} else {
//			onLoseEvent(playerId, pointId, mapId);
		}
	}

	/**
	 * 判断当前训练场是否开启
	 * 
	 * @param mapInfo
	 * @return
	 */
	public boolean trainingMapIsOpen(TrainDBMap.Builder mapInfo) {
		long now = GlobalTick.getInstance().getCurrentTime();
		if (now >= mapInfo.getOpenTime() && now <= mapInfo.getCloseTime()) {
			return true;
		}
		return false;
	}

	public void addTrainItem(String playerIdx, Map<Integer, Integer> itemMap) {
		LogUtil.info("Player[{}] AddTrainItem data:{}", playerIdx, itemMap);
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerIdx);
		if (null == cache) {
			return;
		}
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			TrainDBMap.Builder tMap = cacheTemp.getCurTrainMap();
			if (tMap == null) {
				return;
			}
//			TrainItemBag.Builder itemBagB = tMap.getItemBagBuilder();
			List<TrainItemData> newItems = new ArrayList<>();

			List<Integer> cardIdList = new ArrayList<>();
			List<TrainCardChoice> choiceList = new ArrayList<>();
			for (Entry<Integer, Integer> ent : itemMap.entrySet()) {
				boolean have = false;
				for (TrainItemData item : tMap.getItemBag().getItemList()) {
					if (item.getItemId() == ent.getKey()) {
						have = true;
						break;
					}
				}
				if (!have) {
					ItemObject byId = Item.getById(ent.getKey());
					if (byId == null) {
						continue;
					}
					int type = 0;
					if (byId.getParamname().equals("trainbuff")) {
					} else if (byId.getParamname().equals("trainpet")) {
						type = 1;
					} else if (byId.getParamname().equals("trainbox")) {
						type = 2;
					} else if (byId.getParamname().equals("traincard")) {
						type = 3;
					} else if (byId.getParamname().equals("trainthree")) {
						type = 4;
					}
					if (type == 3 || type == 4) {
						if (byId.getParamstr().length > 0) {
							int[] is = byId.getParamstr()[0];
							if (is.length >= 2) {
								int poolId = is[0];
								int num = is[1];
//								List<Integer> ignorlist = new ArrayList<>(tMap.getLuckDataMap().keySet());
								List<Integer> luckCardByPoolId = getCardByPool(tMap, poolId, num);
								if (type == 3) {
									cardIdList.addAll(luckCardByPoolId);
								} else if (type == 4) {
									TrainCardChoice.Builder choiceB = TrainCardChoice.newBuilder();
									choiceB.addAllCardId(luckCardByPoolId);
									choiceList.add(choiceB.build());
								}
							}
						}
					} else {
						TrainItemData.Builder newB = TrainItemData.newBuilder();
						newB.setItemId(ent.getKey());
						newB.setNum(ent.getValue());
						newB.setType(type);
						newItems.add(newB.build());
					}
				}
			}
			//已存在的道具处理
			for (int i = 0; i < tMap.getItemBag().getItemCount(); i++) {
				TrainItemData.Builder item = tMap.getItemBagBuilder().getItemBuilder(i);
				int total = 0;
				if (itemMap.containsKey(item.getItemId())) {
					total = itemMap.get(item.getItemId());
				}
				item.setNum(item.getNum() + total);
			}
			for (Integer i : cardIdList) {
				addCard(tMap, i);
			}
			tMap.addAllTraincard(choiceList);
			if (choiceList.size() > 0) {// 弹出祝福卡选择
				TrainCardChoice trainCardChoice = choiceList.get(0);
				alertBuffChose(playerIdx, trainCardChoice.getCardIdList(), 1);
			}
			tMap.getItemBagBuilder().addAllItem(newItems);
			// 更新数据
			sendItemBag(playerIdx, tMap.getItemBagBuilder(), tMap.getBuffBagBuilder());

			if (!cardIdList.isEmpty()) {
				SC_TrainShowCards.Builder showBuilder = SC_TrainShowCards.newBuilder();
				showBuilder.addAllCardIds(cardIdList);
				GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_TrainShowCards_VALUE, showBuilder);
			}
		});
	}

	/**
	 * 使用buff用于下次挑战
	 */
	public void useBuff(String playerId, int mapId, int point, Map<Integer, Integer> buffs) {

		// 新需求,所有同ID道具只能使用一次2021年10月25日 16:21:08
		for (Entry<Integer, Integer> ent : buffs.entrySet()) {
			ent.setValue(1);
		}
		SC_TrainUse.Builder b = SC_TrainUse.newBuilder();
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RCE_Success);
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache || cache.getInfoDB().getCurrMap() != mapId) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			b.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUse_VALUE, b);
			return;
		}
		int buffCount = buffs.size();
		if (buffCount > 0 && buffCount > GameConfig.getById(GameConst.CONFIG_ID).getMaxtrainuseitemnum()) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_ItemUseBuffLimit);
			b.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUse_VALUE, b);
			return;
		}
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			TrainDBMap.Builder tMap = cacheTemp.getTrainMapByMapId(mapId);
			TrainBuffBag.Builder bagB = tMap.getBuffBagBuilder();
			TrainItemBag.Builder itemBagB = tMap.getItemBagBuilder();

			int maxNum = GameConfig.getById(GameConst.CONFIG_ID).getTrainbufflimit();
			Map<Integer, Integer> trueUse = new HashMap<>();
			if (buffCount == 0) {
				bagB.clear();
				tMap.clearPrepareUse();
			} else {
				bagB.clear();
				tMap.clearPrepareUse();
				Map<Integer, TrainBuffData.Builder> buffMap = new HashMap<>();
				for (Entry<Integer, Integer> ent : buffs.entrySet()) {
					for (TrainItemData item : itemBagB.getItemList()) {
						if (item.getItemId() == ent.getKey() && item.getNum() >= ent.getValue()) {

							ItemObject obj = Item.getById(ent.getKey());
							if (obj == null) {
								continue;
							}
							if (!obj.getParamname().equals("trainbuff")) {
								continue;
							}
							if (obj.getParamstr().length < 1) {
								continue;
							}
							int[] is = obj.getParamstr()[0];
							if (is.length < 1) {
								continue;
							}
							int buffId = is[0];
							int num = 0;
							if (buffMap.containsKey(buffId)) {
								num = buffMap.get(buffId).getNum();
							}
							if (num + ent.getValue() > maxNum) {
								num = maxNum;
							} else {
								num += ent.getValue();
							}
							if (!buffMap.containsKey(buffId)) {
								buffMap.put(buffId, TrainBuffData.newBuilder().setItemId(ent.getKey()).setNum(num).setBuffId(buffId));
							} else {
								TrainBuffData.Builder buffB = buffMap.get(buffId);
								buffB.setNum(num);
							}

							trueUse.put(ent.getKey(), num);
						}
					}
				}
				List<TrainBuffData> list = new ArrayList<>();
				for (Entry<Integer, TrainBuffData.Builder> e : buffMap.entrySet()) {
					list.add(e.getValue().build());
				}
				bagB.addAllBuff(list);
				tMap.clearPrepareUse();

			}
			tMap.putAllPrepareUse(trueUse);
			sendItemBag(playerId, itemBagB, bagB);
		});
		b.setResult(retCode);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUse_VALUE, b);
	}

	public void useItem(String playerId, int itemId, int param) {
		SC_TrainUseItem.Builder b = SC_TrainUseItem.newBuilder();
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RCE_Success);
		int num = 1;
		ItemObject item = Item.getById(itemId);
		if (item == null) {
			retCode.setRetCode(RetCodeEnum.RCE_ErrorParam);
			b.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUseItem_VALUE, b);
			return;
		}
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			b.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUseItem_VALUE, b);
			return;
		}
		int mapId = cache.getInfoDB().getCurrMap();
		if (item.getParamname().equals("trainpet")) {
			if (item.getParamstr().length < 1) {
				retCode.setRetCode(RetCodeEnum.RCE_ConfigError);
				b.setResult(retCode);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUseItem_VALUE, b);
				return;
			}
			int[][] petArr = item.getParamstr();
			if (param >= petArr.length) {
				retCode.setRetCode(RetCodeEnum.RCE_ConfigError);
				b.setResult(retCode);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUseItem_VALUE, b);
				return;
			}
			int[] moling = petArr[param];
			if (moling == null || moling.length < 4) {
				retCode.setRetCode(RetCodeEnum.RCE_ConfigError);
				b.setResult(retCode);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUseItem_VALUE, b);
				return;
			}
			SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
				TrainDBMap.Builder tMap = cacheTemp.getTrainMapByMapId(mapId);
				TrainItemBag.Builder bagB = tMap.getItemBagBuilder();
				TrainItemData.Builder data = null;
				int index = 0;
				for (int i = 0; i < bagB.getItemCount(); i++) {
					if (bagB.getItemBuilder(i).getItemId() == itemId) {
						data = bagB.getItemBuilder(i);
						index = i;
						break;
					}
				}
				if (data == null) {
					retCode.setRetCode(RetCodeEnum.RCE_ErrorParam);
					b.setResult(retCode);
					GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUseItem_VALUE, b);
					return;
				}
				if (data.getNum() < num) {
					retCode.setRetCode(RetCodeEnum.RCE_ErrorParam);
					b.setResult(retCode);
					GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUseItem_VALUE, b);
					return;
				}
				data.setNum(data.getNum() - num);
				if (data.getNum() <= 0) {
					bagB.removeItem(index);
				}

				TrainHelpPetData.Builder helpPetData = TrainHelpPetData.newBuilder();
				helpPetData.setPointId(10000000 + tMap.getMlidsCount());
				helpPetData.setPetCfgId(moling[0]);
				helpPetData.setPetLevel(moling[1]);
				helpPetData.setPetRarity(moling[2]);
				helpPetData.setPetUpLevel(moling[3]);
				tMap.addMlids(helpPetData);

				retCode.setRetCode(RetCodeEnum.RCE_Success);
				b.setResult(retCode);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUseItem_VALUE, b);
				sendItemBag(playerId, bagB, tMap.getBuffBagBuilder());
				updateMainPane(playerId, tMap, 0, null);
			});
		} else if (item.getParamname().equals("trainbox")) {
			if (item.getParamstr().length < 1) {
				return;
			}
			int[] reward = item.getParamstr()[0];
			List<Reward> rewards = new ArrayList<>();
			for (int i : reward) {
				Reward.Builder rb = Reward.newBuilder();
				rb.setRewardType(RewardTypeEnum.RTE_Train);
				rb.setCount(1);
				rb.setId(i);
				rewards.add(rb.build());
			}
			Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_TRAINBUYSHOP);
			retCode.setRetCode(RetCodeEnum.RCE_Success);
			RewardManager.getInstance().doRewardByList(playerId, rewards, reason, false);
			b.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUseItem_VALUE, b);
			SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
				TrainDBMap.Builder tMap = cacheTemp.getTrainMapByMapId(mapId);
				TrainItemBag.Builder bagB = tMap.getItemBagBuilder();
				TrainItemData.Builder data = null;
				int index = 0;
				for (int i = 0; i < bagB.getItemCount(); i++) {
					if (bagB.getItemBuilder(i).getItemId() == itemId) {
						data = bagB.getItemBuilder(i);
						index = i;
						break;
					}
				}
				if (data == null) {
					retCode.setRetCode(RetCodeEnum.RCE_ErrorParam);
					b.setResult(retCode);
					GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUseItem_VALUE, b);
					return;
				}
				if (data.getNum() < num) {
					retCode.setRetCode(RetCodeEnum.RCE_ErrorParam);
					b.setResult(retCode);
					GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUseItem_VALUE, b);
					return;
				}
				data.setNum(data.getNum() - num);
				if (data.getNum() <= 0) {
					bagB.removeItem(index);
				}
				countRewards(tMap, rewards);
				TrainItemBag bag = bagB.build();
				tMap.setItemBag(bag);

				retCode.setRetCode(RetCodeEnum.RCE_Success);
				b.setResult(retCode);
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainUseItem_VALUE, b);
				sendItemBag(playerId, bagB, tMap.getBuffBagBuilder());
				updateMainPane(playerId, tMap, 0, null);
			});
		} else {
			return;
		}

	}

	public void sendItemBag(String playerId) {
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			return;
		}
		TrainDBMap.Builder map = cache.getCurTrainMap();
		if (map == null) {
			return;
		}
		sendItemBag(playerId, map.getItemBagBuilder(), map.getBuffBagBuilder());
	}

	private void sendItemBag(String playerId, TrainItemBag.Builder bag, TrainBuffBag.Builder buff) {
		SC_TrainBagInfo.Builder b = SC_TrainBagInfo.newBuilder();
		b.setResult(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success));
		TrainItemBag.Builder builder = TrainItemBag.newBuilder();
		// 客户端优化内容需要拆开展示,服务器数据结构保持不变
		for (TrainItemData item : bag.getItemList()) {
			Builder setNum = item.toBuilder().setNum(1);
			for (int i = 0; i < item.getNum(); i++) {
				builder.addItem(setNum);
			}
		}
		b.setBag(builder);
		b.setBuff(buff);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainBagInfo_VALUE, b);
	}

	/**
	 * 开启训练营
	 */
	public void openTrain(String playerId, int mapId) {
		TrainingMapObject ent = TrainingMap.getByMapid(mapId);
		if (null != ent) {
			openTrain(playerId, ent);
		}
	}

	public void openTrain(String playerId, TrainingMapObject mapCfg) {
		TrainTreeMap cfgAfter = cfgMap.get(mapCfg.getMapid());
		if (null == cfgAfter) {
			return;
		}
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			initTrainEntity(playerId, mapCfg);
		} else {
			TrainDBMap.Builder mapData = cache.getTrainMapByMapId(mapCfg.getMapid());
			if (mapData != null) {
				return;
			}
			SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
				long lastEndTime = 0;
				long now = GlobalTick.getInstance().getCurrentTime();
				if (now > lastEndTime) {
					lastEndTime = now;
				}
				cacheTemp.getInfoDB().addMaps(createTrainDBMap(mapCfg, cfgAfter, lastEndTime, petCache.getInstance().totalAbility(playerId)));
			});
		}
		sendTrainOpen(playerId);
	}

	private void initTrainEntity(String playerId, TrainingMapObject ent) {
		TrainTreeMap cfgAfter = cfgMap.get(ent.getMapid());
		if (null == cfgAfter) {
			return;
		}
		trainingEntity cache = new trainingEntity();
		cache.setIdx(IdGenerator.getInstance().generateId());
		cache.setPlayeridx(playerId);
		cache.setFinishinfo("");
		cache.setOpens("");
		TrainDBMap map = createTrainDBMap(ent, cfgAfter, GlobalTick.getInstance().getCurrentTime(), petCache.getInstance().totalAbility(playerId));
		cache.getInfoDB().addMaps(map);
		// 初始化数据设置当前地图为0，刷帧逻辑会开启
		trainingCache.getInstance().flush(cache);

	}

	private TrainDBMap createTrainDBMap(TrainingMapObject ent, TrainTreeMap tp, long openTime, long joinPower) {
		TrainDBMap.Builder data = TrainDBMap.newBuilder();
		data.setMapId(ent.getMapid());
		long endTime = openTime;
		if (ent.getOpentime() > 1000000L) {
			endTime = 1000000L * 60000L + endTime;
		} else {
			endTime = ent.getOpentime() * 60000L + endTime;
		}
		data.setOpenTime(openTime);
		data.setCloseTime(endTime);
		data.setEndtime(0);
		data.setStarNum(0);
		data.setJifen(0);
		data.setJifenRefTime(openTime);
		data.setEndRankMC(0);
		data.getEventDataBuilder().setLevel(1);
		int startId = 0;
		if (tp.getStartIds().size() > 0) {// 多起点客户端不处理,让服务器自动选点
			for (Integer i : tp.getStartIds()) {
				data.setStartId(i);
				startId = i;
				TrainKV.Builder b = TrainKV.newBuilder();
				b.setKey(i);
				b.setVue(1);
				data.addGoNum(b);
				data.putStarMap(i, 1);
				break;
			}
		}
		data.addCurpath(data.getStartId());
		data.setCurrPos(startId);
		data.clearShop();
		data.putAllShop(initTrainShopGroup(tp.getMapid()));// 初始商店
		data.setJoinPower(joinPower);
		return data.build();
	}

	/**
	 * 进入训练营
	 */
	public void joinTrain(String playerId, int mapId) {
		SC_TrainJoin.Builder msg = SC_TrainJoin.newBuilder();
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RCE_Success);
		if (!isOpenFunction(playerId)) {
			retCode.setRetCode(RetCodeEnum.RCE_FunctionIsLock);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainJoin_VALUE, msg);
			return;
		}
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainJoin_VALUE, msg);
			return;
		}
		TrainDBMap.Builder tMap = cache.getTrainMapByMapId(mapId);
		if (null == tMap) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainJoin_VALUE, msg);
			return;
		}
		if (!trainingMapIsOpen(tMap)) {
			retCode.setRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN);
			msg.setResult(retCode);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainJoin_VALUE, msg);
			return;
		}
		boolean changeMapId = cache.getInfoDB().getCurrMap() != mapId;
		if (changeMapId) {
			SyncExecuteFunction.executeConsumer(cache, emp -> {
				emp.getInfoDB().setCurrMap(mapId);
			});
			TeamsUtil.updateTeamInfoTrain(playerId);
		}

		sendMainPane(playerId, tMap);
		LogService.getInstance().submit(new GamePlayLog(playerId, EnumFunction.Training));
		int size = tMap.getBuffIdsList().size();
		if (size > 0) {
			// 有祝福卡没有领取
			int[] buffIds = new int[size];
			for (int i = 0; i < tMap.getBuffIdsList().size(); i++) {
				buffIds[i] = tMap.getBuffIds(i);
			}
			alertBuffChose(playerId, buffIds, 0);
		} else {
			size = tMap.getTraincardList().size();
			if (size > 0) {// 先点祝福卡选择,再道具祝福卡选择
				alertBuffChose(playerId, tMap.getTraincard(0).getCardIdList(), 1);
			}
		}
		sendAllShop(playerId);
	}

	private int getPrice(int baseCount, float time, float configPer) {
		// 基础价格 + 基础价格*配置百分比*购买次数
		float countFloat = baseCount;
		countFloat += countFloat * configPer * time;
		// 抹掉小数
		return (int) countFloat;
	}

	private Map<Integer, TrainShopGroup> initTrainShopGroup(int mapId) {
		Map<Integer, TrainShopGroup> map = new HashMap<>();
		putTrainShop(map, mapId, TrainShopType.SHOP_NORMAL_VALUE);
		putTrainShop(map, mapId, TrainShopType.SHOP_SHILIAN_VALUE);
		putTrainShop(map, mapId, TrainShopType.SHOP_LIMITCARD_VALUE);
		putTrainShop(map, mapId, TrainShopType.SHOP_FULI_VALUE);
		return map;
	}

	private boolean checkCanFreshThisShop(int type) {
		switch (type) {
		case TrainShopType.SHOP_NORMAL_VALUE:
		case TrainShopType.SHOP_SHILIAN_VALUE:
			return true;
		default:
			return false;
		}
	}

	private List<TrainShopItem> createLimitCard(TrainDBMap.Builder tMap, int poolId, int num, int id) {
		List<Integer> luckCardByPoolId = getCardByPool(tMap, poolId, num);
		List<TrainShopItem> list = new ArrayList<>();
		for (Integer i : luckCardByPoolId) {
			TrainingLuckObject config = TrainingLuck.getById(i);
			if (config == null) {
				continue;
			}
			list.add(createLimitCardItem(config, ++id));
		}

		return list;
	}

	private void sendAlertItem(String playerId, int type, List<TrainShopItem> list) {
		SC_TrainAlterItem.Builder alertBuilder = SC_TrainAlterItem.newBuilder();
		alertBuilder.setType(type);
		alertBuilder.addAllItem(list);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAlterItem_VALUE, alertBuilder);
		sendAllShop(playerId);
	}

	private void putTrainShop(Map<Integer, TrainShopGroup> map, int mapId, int type) {
		TrainShopGroup shop = createTrainShop(mapId, type);
		if (shop != null) {
			map.put(type, shop);
		}
	}

	private TrainShopGroup createTrainShop(int mapId, int type) {
		TrainShopGroup shop = null;
		switch (type) {
		case TrainShopType.SHOP_NORMAL_VALUE:
		case TrainShopType.SHOP_SHILIAN_VALUE:
			shop = createNormalShop(mapId, type);
			break;
		case TrainShopType.SHOP_LIMITCARD_VALUE:
		case TrainShopType.SHOP_FULI_VALUE:
			shop = createCommonShop(mapId, type);
			break;
		default:
			break;
		}
		return shop;
	}

	private TrainShopGroup.Builder refreshTrainShop(int mapId, int level, int groupId) {
		if (!this.shopMap.containsKey(mapId)) {
			return null;
		}
		TrainShopGroup commonShop = createCommonShop(mapId, groupId);
		if (commonShop == null) {
			return null;
		}
		return commonShop.toBuilder();
	}

	private TrainShopGroup createNormalShop(int mapId, int type) {
		TrainShopData data = this.shopMap.get(mapId);
		if (!data.getMap().containsKey(type)) {
			return null;
		}
		List<TrainingShopObject> goods = data.getMap().get(type);
		TrainShopGroup.Builder b = TrainShopGroup.newBuilder();
		List<TrainingShopObject> canUse = new ArrayList<>();
		for (TrainingShopObject e : goods) {
			canUse.add(e);
		}
//		boolean showAll = false;
//		if (canUse.size() <= total) {
//			total = canUse.size();
//			showAll = true;
//		}
//		// 普通商店的商品都是全覆盖,位置没有特殊要求,按照个数来
//		if (!showAll) {
//			Collections.shuffle(canUse);
//		}
		int total = canUse.size();
		Collections.shuffle(canUse);
		for (int i = 0; i < total; i++) {
			TrainShopItem.Builder item = createShopItem(canUse.get(i), i);
			b.putItem(i, item.build());
		}
		b.setGroupId(type);
		return b.build();
	}

	private TrainShopGroup createCommonShop(int mapId, int type) {
		TrainShopGroup.Builder b = TrainShopGroup.newBuilder();
		b.setGroupId(type);
		return b.build();
	}

	private TrainShopItem.Builder createShopItem(TrainingShopObject obj, int pos) {
		TrainShopItem.Builder b = TrainShopItem.newBuilder();
		b.setId(obj.getId());
		b.setPos(pos);
		setShopItemConsume(b, obj.getPrice(), obj.getDiscount());
		b.setHave(obj.getLimit());
		if (obj.getFree() != 0) {
			b.setNextFreeTime(GlobalTick.getInstance().getCurrentTime() + obj.getFree() * TimeUtil.MS_IN_A_MIN);
		}
		b.setType(0);
		b.setBaseCount(b.getConsume().getCount());
		return b;
	}

	private TrainShopItem.Builder createShopItem(int itemModelId, int pos) {

		TrainingShopObject config = TrainingShop.getById(itemModelId);
		if (config != null) {
			return createShopItem(config, pos);
		}
		return null;
	}

	private TrainShopItem createLimitCardItem(TrainingLuckObject config, int id) {
		TrainShopItem.Builder b = TrainShopItem.newBuilder();
		b.setId(config.getId());
		b.setPos(id);
		b.setHave(1);
		b.setNextFreeTime(0);
		b.setType(1);
		setShopItemConsume(b, config.getPrice(), config.getDiscount());
		b.setBaseCount(b.getConsume().getCount());
		b.setEndTime(GlobalTick.getInstance().getCurrentTime() + TimeUtil.MS_IN_A_DAY);
		return b.build();
	}

	private void setShopItemConsume(TrainShopItem.Builder item, int[][] price, int[][] discount) {
		if (price != null && price.length > 0) {
			int index = r.nextInt(price.length);
			Consume consume = ConsumeUtil.parseConsume(price[index]);
			if (consume != null) {
				item.setConsume(consume);
			}
			if (discount != null && discount.length > 0) {
				if (index < discount.length) {
					int[] is = discount[index];
					if (is.length > 0) {
						item.setDiscount(is[0]);
					}
				}
			}
			item.setBaseCount(item.getConsume().getCount());
		}
	}

	/**
	 * 获取商店
	 */
	public void getShopInfo(String playerId, int mapId, int group) {
		SC_TrainShop.Builder msg = SC_TrainShop.newBuilder();
		msg.setType(group);
		if (!isOpenFunction(playerId)) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShop_VALUE, msg);
			return;
		}
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShop_VALUE, msg);
			return;
		}
		TrainingMapObject tmo = TrainingMap.getByMapid(mapId);
		if (null == tmo) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShop_VALUE, msg);
			return;
		}
		TrainDBMap.Builder tMap = cache.getTrainMapByMapId(mapId);
		if (null == tMap) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShop_VALUE, msg);
			return;
		}
		TrainShopGroup shopGroup = tMap.getShopMap().get(group);
		if (shopGroup == null) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShop_VALUE, msg);
			return;
		}
		msg.addAllItem(shopGroup.getItemMap().values());
		msg.setFresh(shopGroup.getFreshTime());
		for(Entry<Integer, Integer> ent : tMap.getCardWeightChangeMap().entrySet()) {
			msg.addWeightChange(TrainKV.newBuilder().setKey(ent.getKey()).setVue(ent.getValue()).build());
		}
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShop_VALUE, msg);
	}

	/**
	 * 购买商品
	 */
	public void buyGoods(String playerId, int mapId, int group, int pos) {

		int buyNum = 1;// 价格递增客户端显示原因,屏蔽多数量购买
		SC_TrainShopBuy.Builder b = SC_TrainShopBuy.newBuilder();
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
			return;
		}
		TrainingMapObject tmo = TrainingMap.getByMapid(mapId);
		if (null == tmo) {
			b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
			return;
		}
		TrainDBMap.Builder tMap = cache.getTrainMapByMapId(mapId);
		if (null == tMap) {
			b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
			return;
		}
		TrainShopGroup checkShop = tMap.getShopMap().get(group);
		if (checkShop == null) {
			b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
			return;
		}
		if (!checkShop.containsItem(pos)) {
			b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
			return;
		}
		TrainShopItem checkItem = checkShop.getItemOrDefault(pos, null);
		if (checkItem == null) {
			b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
			return;
		}

		TrainingShopObject obj = TrainingShop.getById(checkItem.getId());
		if (checkItem.getType() == 0) { //普通道具配置检测
			if (obj == null) {
				b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
				return;
			}
			ItemObject itemCfg = Item.getById(obj.getItemid());
			if (itemCfg == null) {
				b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
				return;
			}
		} else {
			TrainingLuckObject luckCfg = TrainingLuck.getById(checkItem.getId());
			if (luckCfg == null) {
				b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
				return;
			}
			if (tMap.containsLuckData(checkItem.getId())) {
				b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_TRAIN_ExistTrainBuff));
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
				return;
			}
//			if (checkItem.getEndTime() > 0 && checkItem.getEndTime() < GlobalTick.getInstance().getCurrentTime()) {
//				b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_GoodsExpired));
//				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
//				return;
//			}
		}

		boolean free = checkItem.getNextFreeTime() != 0 && checkItem.getNextFreeTime() < GlobalTick.getInstance().getCurrentTime();
		Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_TRAINBUYSHOP);
		if (!free) {
			int haveBuy = checkItem.getBuy();
			int canBuy = checkItem.getHave() - haveBuy;
			if (canBuy <= 0) {
				b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Store_GoodsBuyUpperLimit));
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
				return;
			}
			if (buyNum > canBuy) {
				b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
				return;
			}
			if (!ConsumeManager.getInstance().consumeMaterial(playerId, checkItem.getConsume(), reason)) {
				b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);
				return;
			}
		}

		if (checkItem.getType() == 0) {// 普通道具
			ItemObject itemCfg = Item.getById(obj.getItemid());
			Reward.Builder reward = Reward.newBuilder();
			if (itemCfg != null) {
				boolean bShow = false;
				if (itemCfg.getSpecialtype() == ItemType.TrainItem) {
					reward.setRewardType(RewardTypeEnum.RTE_Train);
				} else {
					reward.setRewardType(RewardTypeEnum.RTE_Item);
					bShow = true;
				}
				reward.setId(obj.getItemid());
				reward.setCount(buyNum);
				RewardManager.getInstance().doReward(playerId, reward.build(), reason, bShow);
			}
		}
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			TrainDBMap.Builder dbMap = cacheTemp.getTrainMapByMapId(mapId);

			TrainShopGroup trainShopGroup = dbMap.getShopMap().get(group);
			TrainShopGroup.Builder shopBuilder = trainShopGroup.toBuilder();
			TrainShopItem item = shopBuilder.getItemOrDefault(pos, null);
			TrainShopItem.Builder itemB = item.toBuilder();
			if (itemB.getType() == 0) {
				if (obj.getPriceadd() > 0) {
					itemB.setConsume(itemB.getConsumeBuilder().setCount(getPrice(itemB.getBaseCount(), itemB.getBuy(), obj.getPriceadd())).build());
				}
				if (free) {
					itemB.setNextFreeTime(GlobalTick.getInstance().getCurrentTime() + obj.getFree() * TimeUtil.MS_IN_A_MIN);
				} else {
					int num = itemB.getBuy();
					itemB.setBuy(num + buyNum);
				}
			} else {
				addCard(dbMap, itemB.getId());

				int num = itemB.getBuy();
				itemB.setBuy(num + buyNum);

				SC_TrainShowCards.Builder showBuilder = SC_TrainShowCards.newBuilder();
				showBuilder.addCardIds(itemB.getId());
				GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_TrainShowCards_VALUE, showBuilder);
			}

			shopBuilder.putItem(pos, itemB.build());
			dbMap.putShop(group, shopBuilder.build());

			b.setItem(itemB.build());
		});
		b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopBuy_VALUE, b);

	}

	// 特殊商店商品添加
	private List<TrainShopItem> addShopItemLimitCard(TrainDBMap.Builder tMap, int poolId) {
		int type = TrainShopType.SHOP_LIMITCARD_VALUE;
		TrainShopGroup group = tMap.getShopMap().get(type);
		if (group == null) {
			return new ArrayList<>();
		}
		int id = tMap.getLimitCardId();
		int num = 4;
		List<TrainShopItem> list = createLimitCard(tMap, poolId, num, ++id);
		TrainShopGroup.Builder builder = group.toBuilder();
		for (TrainShopItem item : list) {
			builder.putItem(item.getPos(), item);
		}
		tMap.setLimitCardId(tMap.getLimitCardId() + num);
		tMap.putShop(type, builder.build());
		return list;
	}

	// 特殊商店商品添加
	private List<TrainShopItem> addShopItemFuli(TrainDBMap.Builder tMap, int shop) {

		List<Integer> itemIds = getRandomShopItem(tMap.getMapId(), shop, 5);
		if (itemIds.size() <= 0) {
			return new ArrayList<>();
		}
		int type = TrainShopType.SHOP_FULI_VALUE;
		TrainShopGroup group = tMap.getShopMap().get(type);
		if (group == null) {
			return new ArrayList<>();
		}
		Map<Integer, TrainShopItem> itemMap = new HashMap<>(group.getItemMap());
		List<TrainShopItem.Builder> newItemList = new ArrayList<>();
		List<TrainShopItem> showList = new ArrayList<>();
		int id = tMap.getLimitCardId();
		for (int i = 0; i < itemIds.size(); i++) {
			int itemId = itemIds.get(i);
			TrainingShopObject itemConfig = TrainingShop.getById(itemId);
			if (itemConfig == null) {
				continue;
			}
			TrainShopItem.Builder createShopItem = null;
			boolean have = false;
			for (Entry<Integer, TrainShopItem> ent : itemMap.entrySet()) {
				if (ent.getValue().getId() == itemId) {
					have = true;
					createShopItem = ent.getValue().toBuilder().setHave(ent.getValue().getHave() + itemConfig.getLimit());
					ent.setValue(createShopItem.build());
					break;
				}
			}
			if (!have) {
				createShopItem = createShopItem(itemId, ++id);
				newItemList.add(createShopItem);
			}
			showList.add(createShopItem.build());
		}
		for (TrainShopItem.Builder item : newItemList) {
			itemMap.put(item.getPos(), item.build());
		}
		TrainShopGroup.Builder builder = group.toBuilder();
		builder.clearItem();
		builder.putAllItem(itemMap);
		tMap.setLimitCardId(++id);

		tMap.putShop(type, builder.build());

		return showList;
	}

	public List<Integer> getRandomShopItem(int mapId, int shop, int num) {
		if (!allShop.containsKey(shop)) {
			return new ArrayList<>();
		}
		List<TrainingShopObject> list = allShop.get(shop);
		if (list.size() <= 0) {
			return new ArrayList<>();
		}
		List<Integer> result = new ArrayList<>();
		if (list.size() <= num) {
			for (TrainingShopObject obj : list) {
				result.add(obj.getId());
			}
			return result;
		}

		List<Integer> ignore = new ArrayList<>();

		int count = 50;

		int need = num;
		while (need > 0) {
			TrainingShopObject trainingShopObject = list.get(r.nextInt(list.size()));
			if (ignore.contains(trainingShopObject.getId())) {
				count--;
			} else {
				result.add(trainingShopObject.getId());
				ignore.add(trainingShopObject.getId());
				need--;
				count--;
			}
			if (count <= 0) {
				break;
			}
		}
		return result;
	}

	public void sendAllShop(String playerId) {
		SC_TrainAllShop.Builder builder = SC_TrainAllShop.newBuilder();
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAllShop_VALUE, builder);
			return;
		}
		TrainingMapObject tmo = TrainingMap.getByMapid(cache.getInfoDB().getCurrMap());
		if (null == tmo) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAllShop_VALUE, builder);
			return;
		}
		TrainDBMap.Builder tMap = cache.getCurTrainMap();
		if (null == tMap) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAllShop_VALUE, builder);
			return;
		}
		List<Integer> types = new ArrayList<>();
		for (TrainShopGroup group : tMap.getShopMap().values()) {
			if (group.getItemCount() > 0) {
				types.add(group.getGroupId());
			}
		}
		builder.addAllType(types);
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainAllShop_VALUE, builder);
	}

	/**
	 * 商店刷新
	 */
	public void handRefreshTrainShop(String playerId, int mapId, int groupId) {
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		SC_TrainShopRefresh.Builder b = SC_TrainShopRefresh.newBuilder();
		if (cache == null) {
			b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopRefresh_VALUE, b);
			return;
		}
		if (!checkCanFreshThisShop(groupId)) {
			b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_TRAIN_CanNotRefreshShop));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopRefresh_VALUE, b);
			return;
		}
		TrainDBMap.Builder tMap = cache.getTrainMapByMapId(mapId);
		if (tMap == null) {
			b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopRefresh_VALUE, b);
			return;
		}

		TrainShopGroup group = tMap.getShopMap().get(groupId);
		if (group == null) {
			b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopRefresh_VALUE, b);
			return;
		}
		int freeTime = GameConfig.getById(GameConst.CONFIG_ID).getTrainfree();
		int freshTime = group.getFreshTime();
		boolean free = false;
		if (freshTime < freeTime) {
			free = true;
		}
		if (!free) {
			Consume consume = ConsumeUtil.parseConsume(GameConfig.getById(GameConst.CONFIG_ID).getTrainconsume());
			int over = freshTime - freeTime;
			over += 1;
			int num = 10 * over + consume.getCount();
			consume = consume.toBuilder().setCount(num).build();
			Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_TRAINBUYSHOP);
			if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, reason)) {
				b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopRefresh_VALUE, b);
				return;
			}
		}
		// 成功修改数据
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			// 成功处理数据
			TrainDBMap.Builder tMapChange = cacheTemp.getTrainMapByMapId(mapId);
			TrainShopGroup.Builder groupBuilder = tMapChange.getShopMap().get(groupId).toBuilder();

			TrainShopGroup.Builder tsgb = refreshTrainShop(mapId, tMapChange.getCurBoss(), groupId);
			tsgb.setFreshTime(groupBuilder.getFreshTime() + 1);
			tMapChange.putShop(groupId, tsgb.build());
		});
		b.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_TrainShopRefresh_VALUE, b);
		getShopInfo(playerId, mapId, groupId);
	}

	/**
	 * 副本结束
	 */
	public void endTrain(String playerId, int mapId) {
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerId);
		if (null == cache) {
			return;
		}
		TrainingMapObject tmoCfg = TrainingMap.getByMapid(mapId);
		if (null == tmoCfg) {
			return;
		}
		TrainDBMap.Builder tMap = cache.getTrainMapByMapId(mapId);
		if (tMap == null) {
			return;
		}
		TrainTreeMap cfg = cfgMap.get(mapId);
		if (cfg == null) {
			return;
		}
		int score = getJifen(tMap);
		gainScoreTaskReward(playerId, mapId, score);

		int ranking = 0;
		TrainRankMap mapRank = jifenData.get(mapId);
		if (null != mapRank) {
			ranking = mapRank.getRankById(playerId);
		}
		int saveRank = ranking;
		// 修改玩家获得的名次数据数据
		SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
			TrainDBMap.Builder dbMap = cacheTemp.getTrainMapByMapId(mapId);
			dbMap.setEndRankMC(saveRank);

			int reportCard = cfg.getReportCard(score);
			dbMap.setReportCardLevel(reportCard);
			dbMap.setEndPower(petCache.getInstance().totalAbility(playerId));
			dbMap.setEndtime(GlobalTick.getInstance().getCurrentTime());

			int beyondRate = calcBeyondPlayerRate(dbMap, saveRank);
			dbMap.putBeyondPlayerData(mapId, beyondRate);
			playerEntity player = playerCache.getByIdx(playerId);
			if (player != null && player.isOnline()) {
				sendReport(playerId, dbMap);
			}
			cacheTemp.getInfoDB().addNoticeMap(mapId);
		});
		EventUtil.triggerUpdateTargetProgress(playerId, TargetSystem.TargetTypeEnum.TTE_Train_CumuComplete, 1, 0);
	}

	protected void gainScoreTaskReward(String playerIdx, int mapId, int score) {
		targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
		if (targetEntity == null) {
			return;
		}
		TrainingMapObject trainingMapCfg = TrainingMap.getByMapid(mapId);
		if (trainingMapCfg == null) {
			return;
		}
		List<Integer> rewardMissionIds = SyncExecuteFunction.executeFunction(targetEntity, entity->{
			if (entity.getDb_Builder().getTrainingTaskData().getMapTaskDataCount() <= 0) {
				return null;
			}
			int index = 0;
			int removeIndex = -1;
			List<Integer> finishedMissionIds = null;
			while (index < entity.getDb_Builder().getTrainingTaskData().getMapTaskDataCount()) {
				TrainingMapTaskData mapTargetData = entity.getDb_Builder().getTrainingTaskData().getMapTaskData(index);
				if (mapTargetData.getMapId() == mapId) {
					removeIndex = index;
					for (TargetMission target : mapTargetData.getTrainTaskList()) {
						if (target.getStatus() == MissionStatusEnum.MSE_Finished) {
							if (finishedMissionIds == null) {
								finishedMissionIds = new ArrayList<>();
							}
							finishedMissionIds.add(target.getCfgId());
						}
					}
					break;
				}
				index++;
			}
			if (removeIndex >= 0) {
				entity.getDb_Builder().getTrainingTaskDataBuilder().removeMapTaskData(index);
			}
			return finishedMissionIds;
		});
		if (rewardMissionIds == null) {
			return;
		}
		MissionObject missionCfg;
		List<Reward> rewardList = new ArrayList<>();
		for (Integer missionId : rewardMissionIds) {
		    missionCfg = Mission.getById(missionId);
		    if (missionCfg == null) {
		    	continue;
			}
		    List<Reward> tmpRewards = RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward());
		    if (tmpRewards != null) {
		    	rewardList.addAll(tmpRewards);
			}
		}
		Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Train_Task);
		EventUtil.triggerAddMailEvent(playerIdx, trainingMapCfg.getMailtempid(), rewardList, reason, String.valueOf(score));
	}

	public List<Integer> getCardByPool(TrainDBMap.Builder dbMap, int bigPoolId, int num) {
		Map<Integer, Integer> weightMap = new HashMap<>(dbMap.getCardWeightChangeMap());
		List<Integer> ignorlist = new ArrayList<>(dbMap.getLuckDataMap().keySet());
		List<Integer> result = new ArrayList<>();

		TrainingLuckPoolObject bigPool = TrainingLuckPool.getById(bigPoolId);
		if (bigPool == null) {
			return result;
		}
		Map<Integer, Integer> littlePoolGradeWeightMap = new HashMap<>();
		Map<Integer, Integer> littlePoolGradeIdMap = new HashMap<>();
		List<Integer> gradeList = new ArrayList<>();
		float littlePoolTotalWeight = 0;
		for (int[] weightArr : bigPool.getCards()) {
			if (weightArr.length < 2) {
				continue;
			}
			int littlePoolId = weightArr[0];
			int littlePoolWeight = weightArr[1];
			TrainingLuckPoolLittleObject littlePool = TrainingLuckPoolLittle.getById(littlePoolId);
			if (littlePool == null || littlePool.getCards() == null) {
				continue;
			}
			boolean allGain = true;
			for (int i = 0; i < littlePool.getCards().length; i++) {
				if (littlePool.getCards()[i].length < 2) {
					continue;
				}
				if (!ignorlist.contains(littlePool.getCards()[i][0])) {
					allGain = false;
					break;
				}
			}
			if (allGain) {
				continue;
			}
			if (!gradeList.contains(littlePool.getGrade())) {
				gradeList.add(littlePool.getGrade());
			}
			littlePoolGradeWeightMap.put(littlePool.getGrade(), littlePoolWeight);
			littlePoolGradeIdMap.put(littlePool.getGrade(), littlePoolId); // 大卡池中小卡池卡池品质必须唯一
			littlePoolTotalWeight += littlePoolWeight;
		}
		gradeList.sort(null);// 自然排序
		Map<Integer, Integer> delWeightMap = new HashMap<>();
		for (Entry<Integer, Integer> ent : weightMap.entrySet()) {
			if (littlePoolGradeWeightMap.containsKey(ent.getKey())) {// 权重变化
				float tmpPer = ent.getValue();
				float addWeightF = littlePoolTotalWeight * tmpPer / 100;
				int addWeight = (int) addWeightF;

				int indexOfGrade = gradeList.indexOf(ent.getKey());
				if (indexOfGrade == -1 || indexOfGrade == 0) {// 不存在或本组最低品质的,不处理变化权重
					continue;
				}
				int over = addWeight % indexOfGrade;
				int eachDelWeight = addWeight / indexOfGrade;// 将增加的权重分配到每个比自身低品质卡池上
				littlePoolGradeWeightMap.put(ent.getKey(), littlePoolGradeWeightMap.get(ent.getKey()) + addWeight);
				for (int i = 0; i < indexOfGrade; i++) {
					int grade = gradeList.get(i);
					int total = delWeightMap.getOrDefault(grade, 0);
					if (i == 0) {// 最低档次品质需要承担没除尽的余数部分权重
						delWeightMap.put(grade, total + eachDelWeight + over);
					} else {
						delWeightMap.put(grade, total + eachDelWeight);
					}
				}
			}
		}

		// 将增加的权重部分由各个低品质平均减少
		int finalTotalWeight = 0;
		for (Entry<Integer, Integer> ent : littlePoolGradeWeightMap.entrySet()) {
			int have = ent.getValue();
			int del = delWeightMap.getOrDefault(ent.getKey(), 0);
			if (have - del < 0) {
				have = 0;
			} else {
				have -= del;
			}
			finalTotalWeight += have;
			ent.setValue(have);
		}


		for (int i = 0; i < num; i++) {
			boolean getCardIdFlag = false;
			for (int j = 0; j < 5; j++) {  // 随机到重复的额外再随5次
				int luck = r.nextInt(finalTotalWeight) + 1;
				int cur = 0;
				for (Entry<Integer, Integer> ent : littlePoolGradeWeightMap.entrySet()) {
					Integer littlePId = littlePoolGradeIdMap.get(ent.getKey());
					if (littlePId == null) {
						continue;
					}
					cur += ent.getValue();
					if (cur < luck) {
						continue;
					}
					int cardId = getLuckCardByLittlePoolId(littlePId, ignorlist);
					if (cardId != 0) {
						ignorlist.add(cardId);
						result.add(cardId);
						getCardIdFlag = true;
						break;
					}
				}
				if (getCardIdFlag) {
					break;
				}
			}
		}
		return result;
	}

	private int getLuckCardByLittlePoolId(int pool, List<Integer> ignoreList) {
		if (!poolTotalWeight.containsKey(pool)) {
			return 0;
		}
		int total = poolTotalWeight.get(pool);
		if (!poolWeight.containsKey(pool)) {
			return 0;
		}
		Map<Integer, Integer> map = poolWeight.get(pool);
		int luckNum = r.nextInt(total) + 1;
		for (Entry<Integer, Integer> ent : map.entrySet()) {
			if (luckNum <= ent.getValue() && !ignoreList.contains(ent.getKey())) {
				return ent.getKey();
			}
			luckNum -= ent.getValue();
		}
		return 0;
	}
}
