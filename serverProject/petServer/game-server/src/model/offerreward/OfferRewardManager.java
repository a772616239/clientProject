package model.offerreward;

import cfg.GameConfig;
import cfg.OfferReward;
import cfg.OfferRewardBoss;
import cfg.OfferRewardBossGroup;
import cfg.OfferRewardBossGroupObject;
import cfg.OfferRewardBossObject;
import cfg.OfferRewardLevelLimit;
import cfg.OfferRewardLevelLimitObject;
import cfg.OfferRewardObject;
import cfg.PlayerLevelConfig;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GameConst.RedisKey;
import common.GlobalData;
import static common.JedisUtil.jedis;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import common.tick.Tickable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.crossarena.CrossArenaHonorManager;
import model.crossarena.CrossArenaManager;
import model.crossarena.CrossArenaUtil;
import model.crossarenapvp.CrossArenaPvpManager;
import static model.offerreward.OfferRewardIdHelper.createOfferId;
import static model.offerreward.OfferRewardIdHelper.getPlayerIdByOfferId;
import static model.offerreward.OfferRewardIdHelper.getPlayerShortId;
import model.patrol.entity.PatrolBattleResult;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.util.CollectionUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.OfferRewardLog;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.SC_RetCode;
import protocol.CrossArena;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.OfferReward.OfferRewardData;
import protocol.OfferReward.OfferRewardMonster;
import protocol.OfferReward.OfferRewardMonster.Builder;
import protocol.OfferReward.OfferRewardPrePare;
import protocol.OfferReward.SC_OfferRewardGet;
import protocol.OfferReward.SC_OfferRewardOne;
import protocol.OfferReward.SC_OfferRewardPanel;
import protocol.OfferReward.SC_OfferRewardPrepare;
import protocol.OfferReward.SC_OfferRewardRefresh;
import protocol.OfferReward.SC_OfferRewardRelease;
import protocol.PlayerDB.DB_OfferReward;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_BS_OFFERNOTICE;
import protocol.TargetSystem;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.RandomUtil;
import util.TimeUtil;

/**
 * @author Hammer
 */
public class OfferRewardManager implements Tickable {

	private long nextFreshTime = 0;

	private Map<String, OfferRewardData> allTask = new HashMap<>();

	private Map<Integer, Map<String, OfferRewardData>> starTask = new HashMap<>();

	private Map<Integer, Integer> gradeWeightMap = new HashMap<>();
	private Map<Integer, Map<Integer, Integer>> bossGroupMap = new HashMap<>();

	private Map<String, OfferRewardData> mockOffer = new HashMap<>();
	private Map<String, OfferRewardData> mockOfferIdMap = new HashMap<>();
	private Random random = new Random();

	private Map<Integer, Integer> levelLimit = new HashMap<>();
	private int rewardSize = 3;
	private List<Integer> grades = new ArrayList<>();

	private static class LazyHolder {
		private static final OfferRewardManager INSTANCE = new OfferRewardManager();
	}

	private Map<String, Long> notice = new ConcurrentHashMap<>();

	private OfferRewardManager() {
	}

	public static OfferRewardManager getInstance() {
		return LazyHolder.INSTANCE;
	}

	@Override
	public void onTick() {

		long now = GlobalTick.getInstance().getCurrentTime();
		if (nextFreshTime > now) {
			return;
		}
		nextFreshTime = now + GameConst.offerRewardTick;

		Map<String, OfferRewardData> allTask = new HashMap<>();
		Map<Integer, Map<String, OfferRewardData>> starTask = new HashMap<>();

		Map<byte[], byte[]> taskBytes = jedis.hgetAll(RedisKey.OFFER_REWARD.getBytes());
		for (Entry<byte[], byte[]> ent : taskBytes.entrySet()) {
			OfferRewardData data = null;
			try {
				data = OfferRewardData.parseFrom(ent.getValue());
			} catch (InvalidProtocolBufferException e) {
				continue;
			}
			if (data == null) {
				continue;
			}
			allTask.put(data.getId(), data);
			if (!starTask.containsKey(data.getStar())) {
				starTask.put(data.getStar(), new HashMap<>());
			}
			Map<String, OfferRewardData> map = starTask.get(data.getStar());
			map.put(data.getId(), data);
		}
		this.allTask = allTask;
		this.starTask = starTask;
	}

	public void sendInfoOne(OfferRewardData data) {
		SC_OfferRewardOne.Builder b = SC_OfferRewardOne.newBuilder();
		long fight = getTaskStateNum(data.getId(), OfferRewardChangeStateEnum.FIGHT);
		long win = getTaskStateNum(data.getId(), OfferRewardChangeStateEnum.WIN);
		int canFightTimes = 0;
		OfferRewardObject cfg = OfferReward.getById(data.getGrade());
		if (cfg != null) {
			canFightTimes = cfg.getRewardnum();
		}
		long finalTime = win > canFightTimes ? canFightTimes : win;
		b.setTask(data.toBuilder().setState(fight > 0 ? 1 : 0).setWinCount((int) finalTime).build());
		long curTime = GlobalTick.getInstance().getCurrentTime();
		for (Entry<String, Long> ent : notice.entrySet()) {
			GlobalData.getInstance().sendMsg(ent.getKey(), MessageId.MsgIdEnum.SC_OfferRewardOne_VALUE, b);
			if (ent.getValue() < curTime) {
				notice.remove(ent.getKey());
				continue;
			}
		}
	}

	public void addOneOffer(String playerId, int grade, int bossIndex, List<Reward> rewards, boolean gm) {
//		int max = GameConfig.getById(GameConst.CONFIG_ID).getOfferreward_person();
		SC_OfferRewardRelease.Builder b = SC_OfferRewardRelease.newBuilder();

		if (CollectionUtils.isEmpty(rewards)) {
			b.setRetcode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_ErrorParam));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardRelease_VALUE, b);
			return;
		}

		BaseNettyClient client = BattleServerManager.getInstance().getAvailableBattleServer();
		if (client == null) {
			b.setRetcode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_UnknownError));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardRelease_VALUE, b);
			return;
		}

		playerEntity player = playerCache.getByIdx(playerId);
		if (player == null) {
			b.setRetcode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_UnknownError));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardRelease_VALUE, b);
			return;
		}
		DB_OfferReward offerRewardPrepare = player.getDb_data().getOfferRewardPrepare();
		OfferRewardPrePare prepare = offerRewardPrepare.getPrepareMapMap().get(grade);

		if (bossIndex < 0 || bossIndex >= prepare.getMonsterInfoList().size()) {
			b.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardRelease_VALUE, b);
			return;
		}

		OfferRewardMonster offerRewardMonster = prepare.getMonsterInfo(bossIndex);

		OfferRewardObject config = OfferReward.getById(grade);
		if (config == null) {
			b.setRetcode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_ConfigError));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardRelease_VALUE, b);
			return;
		}
		if (rewards.size() > rewardSize) { // 写死的 后续优化
			b.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardRelease_VALUE, b);
			return;
		}
		// 校验客户端奖励数据
		for (Reward reward : rewards) {
		    if (!offerRewardMonster.getRewardList().contains(reward)) {
		    	b.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardRelease_VALUE, b);
				return;
			}
		}


		List<Reward> senderReward = offerRewardMonster.getRewardList();
//		List<Reward> fightFirstReward = offerRewardMonster.getFirstRewardList();
//		List<Reward> fightSecondReward = offerRewardMonster.getSecondRewardList();
//		List<Reward> fightThirdReward = offerRewardMonster.getThridRewardList();
		Set<Reward> ignoreList = new HashSet<>();
		List<Reward> choiceRewards = new ArrayList<>();
//		List<Reward> choiceFirstRewards = new ArrayList<>();
//		List<Reward> choiceSecondRewards = new ArrayList<>();
//		List<Reward> choiceThirdRewards = new ArrayList<>();
		List<Integer> choiceIndex = new ArrayList<>();
		for (int i = 0; i < rewards.size(); i++) {
			Reward checkReward = rewards.get(i);
			if (ignoreList.contains(checkReward)) {
				b.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardRelease_VALUE, b);
				return;
			}
			for (int j = 0; j < senderReward.size(); j++) {
				Reward reward = senderReward.get(j);
				if (reward.equals(checkReward)) {
					choiceRewards.add(reward);
					ignoreList.add(reward);
					choiceIndex.add(j);
//					if (j < fightFirstReward.size()) {
//						choiceFirstRewards.add(fightFirstReward.get(j));
//					}
//					if (j < fightSecondReward.size()) {
//						choiceSecondRewards.add(fightSecondReward.get(j));
//					}
//					if (j < fightThirdReward.size()) {
//						choiceThirdRewards.add(fightThirdReward.get(j));
//					}
					break;
				}
			}
		}
		// 成功修改数据
		int id = SyncExecuteFunction.executeFunction(player, e -> {
//			if (player.getDb_data().getOfferrewardCount() >= max) {
//				return 0;
//			}
			Map<Integer, Integer> tmpMap = new HashMap<>();
			for (String str : e.getDb_data().getOfferrewardList()) {
				String[] split = str.split("_");
				if (split.length < 3) {
					continue;
				}
				tmpMap.put(NumberUtils.toInt(split[2]), 0);
			}
			int tmpId = 1;
			for (Entry<Integer, Integer> ent : tmpMap.entrySet()) {
				if (!tmpMap.containsKey(tmpId)) {
					return tmpId;
				}
				tmpId++;
			}
			e.getDb_data().getOfferRewardPrepareBuilder().removePrepareMap(grade);
			return tmpId;
		});
		if (id <= 0) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardRelease_VALUE, b);
			return;
		}

		Consume consume = ConsumeUtil.parseConsume(config.getConsume());
		Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_OFFER_REWARD);
		if (!gm && !ConsumeManager.getInstance().consumeMaterial(playerId, consume, reason)) {
			b.setRetcode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardRelease_VALUE, b);
			return;
		}
		b.setRetcode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success));
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardRelease_VALUE, b);
		long curTime = GlobalTick.getInstance().getCurrentTime();
		// 生成悬赏令
//		offerRewardMonster.toBuilder().clear();
		OfferRewardMonster.Builder builder = offerRewardMonster.toBuilder();
		builder.clearReward();
		builder.addAllReward(choiceRewards);
		builder.addAllRewardIndex(choiceIndex);
//		builder.addAllFirstReward(choiceFirstRewards);
//		builder.addAllSecondReward(choiceSecondRewards);
//		builder.addAllThridReward(choiceThirdRewards);
		offerRewardMonster = builder.build();

		OfferRewardData.Builder task = OfferRewardData.newBuilder();
		String createId = createOfferId(player.getShortid() + "", id, 0);
		task.setId(createId);
		task.setBoss(offerRewardMonster);
		task.setGrade(grade);
		task.setEndTime(curTime + (config.getTime() * TimeUtil.MS_IN_A_MIN));
		task.setSendPlayerId(playerId);
		task.setStartTime(curTime);
		task.setStar(offerRewardMonster.getStar());
		task.setLevelLimit(getLevelLimit(player.getLevel()));
		EventUtil.triggerUpdateTargetProgress(playerId, TargetSystem.TargetTypeEnum.TTE_CrossArena_XSFB, 1, 0);
		onAddAfter(playerId, grade, 1);
		// 刷入新数据
		jedis.hset(RedisKey.OFFER_REWARD.getBytes(), createId.getBytes(), task.build().toByteArray());

		SyncExecuteFunction.executeConsumer(player, e -> {
			e.getDb_data().addOfferreward(createId);
			e.getDb_data().getOfferRewardPrepareBuilder().removePrepareMap(grade);
		});
		GS_BS_OFFERNOTICE.Builder nb = GS_BS_OFFERNOTICE.newBuilder();
		nb.setData(task);
		client.send(MsgIdEnum.GS_BS_OFFERNOTICE_VALUE, nb);
		EventUtil.triggerUpdateCrossArenaWeeklyTask(playerId, CrossArena.CrossArenaGradeType.GRADE_XS_Join, 1);

		LogService.getInstance().submit(new OfferRewardLog(playerId,true, grade));
	}

	private OfferRewardData createMockOfferRewardData(String playerId, int playerLv) {
		if (grades.size() == 0) {
			return null;
		}
		playerEntity player = playerCache.getByIdx(playerId);
		if (player == null) {
			return null;
		}
		int grade = grades.get(random.nextInt(grades.size()));

		OfferRewardPrePare.Builder prepare = createOfferRewardPrePare(grade, playerLv);
		if (prepare == null) {
			return null;
		}
		List<OfferRewardMonster> monsterInfoList = prepare.getMonsterInfoList();
		OfferRewardMonster boss = monsterInfoList.get(random.nextInt(monsterInfoList.size()));
		List<Reward> rewards = new ArrayList<>();

		if (boss.getRewardList().size() <= rewardSize) {
			rewards.addAll(boss.getRewardList());
		} else {
			int size = this.rewardSize;
			Set<Reward> ignoreList = new HashSet<>();

			int maxCount = 50;
			while (size > 0) {
				if (maxCount <= 0) {
					break;
				}
				Reward reward = boss.getReward(random.nextInt(boss.getRewardList().size()));
				if (ignoreList.contains(reward)) {
					maxCount--;
					continue;
				}
				rewards.add(reward);
				ignoreList.add(reward);
				maxCount--;
				size--;
			}

		}

		boss = boss.toBuilder().clearReward().addAllReward(rewards).build();
		OfferRewardData.Builder task = OfferRewardData.newBuilder();
		String createId = createOfferId(player.getShortid() + "", 0, 0);
		task.setId(createId);
		task.setBoss(boss);
		task.setGrade(grade);
		task.setEndTime(GlobalTick.getInstance().getCurrentTime() + (12 * TimeUtil.MS_IN_A_HOUR));
		task.setSendPlayerId(playerId);
		task.setStartTime(GlobalTick.getInstance().getCurrentTime());
		task.setStar(boss.getStar());
		return task.build();
	}

	public void onAddAfter(String playerId, int grade, int num) {
		CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerId, CrossArenaUtil.HR_XS_NUM, num);

		switch (grade) {
		case 3:

			break;
		case 5:
			CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerId, CrossArenaUtil.HR_XS_NUM_Z, num);
			break;
		case 7:
			CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerId, CrossArenaUtil.HR_XS_NUM_C, num);
			break;
		case 9:
			CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerId, CrossArenaUtil.HR_XS_NUM_H, num);
			CrossArenaHonorManager.getInstance().honorVueFirst(playerId, CrossArenaUtil.HR_FIRST_XS_H);
			break;

		default:
			break;
		}
	}

	public void onWinAfter(String playerId, int grade, int num) {
		CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerId, CrossArenaUtil.HR_XS_NUM_F, num);
		CrossArenaManager.getInstance().savePlayerDBInfo(playerId, CrossArena.CrossArenaDBKey.XS_CompleteTimes,num, CrossArenaUtil.DbChangeAdd);
		switch (grade) {
		case 3:
			break;
		case 5:
			CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerId, CrossArenaUtil.HR_XS_NUM_FZ, num);
			break;
		case 7:
			CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerId, CrossArenaUtil.HR_XS_NUM_FC, num);
			break;
		case 9:
			CrossArenaHonorManager.getInstance().honorVueByKeyAdd(playerId, CrossArenaUtil.HR_XS_NUM_FH, num);
			CrossArenaHonorManager.getInstance().honorVueFirst(playerId, CrossArenaUtil.HR_FIRST_XS_FH);
			break;

		default:
			break;
		}
	}

	public void getOfferReward(String playerId, String id) {
		SC_OfferRewardGet.Builder b = SC_OfferRewardGet.newBuilder();
		playerEntity player = playerCache.getByIdx(playerId);
		if (player == null) {
			b.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardGet_VALUE, b);
			return;
		}
		boolean have = false;
		for (String e : player.getDb_data().getOfferrewardList()) {
			if (e.equals(id)) {
				have = true;
				break;
			}
		}
		if (!have) {
			b.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_OfferReward_NoTask));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardGet_VALUE, b);
			return;
		}
		long curTime = GlobalTick.getInstance().getCurrentTime();
		List<byte[]> hmget = jedis.hmget(RedisKey.OFFER_REWARD.getBytes(), id.getBytes());
		if (CollectionUtils.isEmpty(hmget)) {
			return;
		}
		byte[] taskByte = hmget.get(0);
		OfferRewardData data = null;
		try {
			data = OfferRewardData.parseFrom(taskByte);
			if (data.getEndtype() == 0 && data.getEndTime() > curTime) {
				b.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_OfferReward_TaskNotFinish));
				GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardGet_VALUE, b);
				return;
			}
		} catch (InvalidProtocolBufferException e) {
			LogUtil.printStackTrace(e);
		}

		if (data == null) {
			b.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_OfferReward_NoTask));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardGet_VALUE, b);
			return;
		}
		OfferRewardObject cfg = OfferReward.getById(data.getGrade());
		if (cfg == null) {
			return;
		}
		List<Reward> rewardList = data.getBoss().getRewardList();
		int winNum = (int) getTaskStateNum(id, OfferRewardChangeStateEnum.WIN);
		if (winNum > cfg.getRewardnum()) {
			winNum = cfg.getRewardnum();
		}
		int rewardRate = 0;
		if (cfg.getPublisherrewardrate() != null && winNum >= 0 && winNum < cfg.getPublisherrewardrate().length) {
			rewardRate = cfg.getPublisherrewardrate()[winNum];
		}
		if (data.getEndtype() == 0 && data.getEndTime() > curTime) {
			b.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_OfferReward_TaskNotFinish));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardGet_VALUE, b);
			return;// 没结束,不能领奖
		} else {
			List<Reward> finalReward = new ArrayList<>();
			calPublisherReward(rewardList, rewardRate, finalReward);
			Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_OFFER_REWARD_OVER);
			RewardManager.getInstance().doRewardByList(playerId, finalReward, reason, true);
		}

		SyncExecuteFunction.executeConsumer(player, e -> {
			List<String> ids = new ArrayList<>();
			for (String eId : e.getDb_data().getOfferrewardList()) {
				if (eId.equals(id)) {
					continue;
				}
				ids.add(eId);
				e.getDb_data().clearOfferreward();
				e.getDb_data().addAllOfferreward(ids);
			}
		});
		jedis.hdel(RedisKey.OFFER_REWARD, id);
		jedis.del(RedisKey.OFFER_REWARD_WIN + id);
		jedis.del(RedisKey.OFFER_REWARD_Fight + id);
		b.setRetcode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success));
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardGet_VALUE, b);
	}

	private void calPublisherReward(List<Reward> rewardList, int rewardRate, List<Reward> finalReward) {
		if (rewardRate <= 0) {
			return;
		}
		for (Reward reward : rewardList) {
			int count = reward.getCount();
			if (count <= 0) {
				continue;
			}
			int finalCount = count * rewardRate / 1000;
			finalReward.add(reward.toBuilder().setCount(finalCount).build());
		}
	}

	public int fightCheck(String playerId, String id, PatrolBattleResult result) {

		int grade = 0;
		OfferRewardData data = null;

		if (mockOfferIdMap.containsKey(id)) {
			data = mockOfferIdMap.get(id);
		} else {
			try {
				byte[] hget = jedis.hget(RedisKey.OFFER_REWARD.getBytes(), id.getBytes());
				data = OfferRewardData.parseFrom(hget);
			} catch (InvalidProtocolBufferException e) {
				LogUtil.printStackTrace(e);
			}
		}
		if (CrossArenaPvpManager.getInstance().isHaveCrossArenaPvpRoom(playerId)) {
			result.setCode(RetCodeEnum.RCE_PLAY_LTPVP);
			return 0;
		}
		if (data == null) {
			result.setCode(RetCodeEnum.RCE_OfferReward_NoTask);
			return 0;
		}
		if (data.getEndTime() < GlobalTick.getInstance().getCurrentTime()) {
			result.setCode(RetCodeEnum.RCE_OfferReward_TaskExpired);
			return 0;
		}

		if (data.getWinnerPlayerIdsList().contains(playerId)) {
			result.setCode(RetCodeEnum.RCE_OfferReward_NoTask);
			return 0;
		}
		grade = data.getGrade();
		OfferRewardObject conf = OfferReward.getById(grade);
		if (conf == null) {
			result.setCode(RetCodeEnum.RCE_ConfigError);
			return 0;
		}
		if (data.getWinCount() >= conf.getRewardnum()) {
			result.setCode(RetCodeEnum.RCE_OfferReward_TaskExpired);
			return 0;
		}
		Consume consume = ConsumeUtil.parseConsume(conf.getAcceptconsume());
		ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_OFFER_REWARD_FIGHT);
		if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, reason)) {
			result.setCode(RetCodeEnum.RCE_MatieralNotEnough);
			return 0;
		}
		result.setCode(RetCodeEnum.RCE_Success);
		result.setMakeId(data.getBoss().getFightMakeId());
		BaseNettyClient client = BattleServerManager.getInstance().getAvailableBattleServer();
		if (client != null) {
			GS_BS_OFFERNOTICE.Builder nb = GS_BS_OFFERNOTICE.newBuilder();
			nb.setData(data.toBuilder().setState(1));
			client.send(MsgIdEnum.GS_BS_OFFERNOTICE_VALUE, nb);
		}
		changeState(id, true, OfferRewardChangeStateEnum.FIGHT);
		return data.getGrade();
	}

	public List<Reward> fightEnd(String playerId, boolean win, String id, int grade) {
		playerEntity entity = playerCache.getByIdx(playerId);
		if (entity == null) {
			return null;
		}
		EventUtil.triggerUpdateCrossArenaWeeklyTask(playerId, CrossArena.CrossArenaGradeType.GRADE_XS_Join,1);
		// 悬赏数据
		OfferRewardData.Builder endOffer = null;
		byte[] offerBytes = jedis.hget(RedisKey.OFFER_REWARD.getBytes(), id.getBytes());
		if (offerBytes != null) {
			try {
				endOffer = OfferRewardData.parseFrom(offerBytes).toBuilder();
			} catch (InvalidProtocolBufferException e) {
				return null;
			}
		}
		List<Reward> rewards = null;
		if (mockOfferIdMap.containsKey(id)) {// 保底悬赏
			if (win) {
				OfferRewardData remove = mockOffer.remove(playerId);
				mockOfferIdMap.remove(id);
				rewards = doReward(playerId, remove.getBoss(),  1, remove.getGrade());

//				SyncExecuteFunction.executeConsumer(entity, tmp -> tmp.getDb_data().getOfferRewardPrepareBuilder().setDayFight(today + 1));
				onWinAfter(playerId, grade, 1);
				LogService.getInstance().submit(new OfferRewardLog(playerId,false, grade));
			}
			return rewards;
		}
		// 挑战的过程中上个人已完成并且发布者领了奖删除了悬赏单
		if (!jedis.hexists(RedisKey.OFFER_REWARD.getBytes(), id.getBytes())) {
			return null;
		}

		changeState(id, false, OfferRewardChangeStateEnum.FIGHT);
		// 真人完成的次数
		long winNum = getTaskStateNum(id, OfferRewardChangeStateEnum.WIN);

		if (win) {
			int maxFightCount = 0;
			OfferRewardObject cfg = OfferReward.getById(endOffer.getGrade());
			if (cfg != null) {
				maxFightCount = cfg.getRewardnum();
			}
			onWinAfter(playerId, grade, 1);
			boolean recordFirst = winNum == 0;
			winNum++;
			endOffer = changeOffer(id, recordFirst, entity.getIdx(), entity.getName(), winNum >= maxFightCount);
			rewards = doReward(playerId, endOffer.getBoss(), (int) winNum, endOffer.getGrade());
			LogService.getInstance().submit(new OfferRewardLog(playerId,false, grade));
			changeState(id, true, OfferRewardChangeStateEnum.WIN);

		}
		BaseNettyClient client = BattleServerManager.getInstance().getAvailableBattleServer();
		if (client != null) {
			if (endOffer != null) {
				GS_BS_OFFERNOTICE.Builder nb = GS_BS_OFFERNOTICE.newBuilder();
				nb.setData(endOffer);
				client.send(MsgIdEnum.GS_BS_OFFERNOTICE_VALUE, nb);
			}
		}
		return rewards;
	}

	private List<Reward> doReward(String playerId, OfferRewardMonster monster, int num, int grade) {
		if (num <= 0) {
			return null;
		}
		List<Reward> fightMakeRewards = new ArrayList<>();
		if (num > 3) {
			OfferRewardObject cfg = OfferReward.getById(grade);
			if (cfg != null) {
				Reward reward = RewardUtil.parseReward(cfg.getAcceptconsume());
				if (reward != null) {
					fightMakeRewards.add(reward);
				}
			}
			SC_RetCode.Builder builder = SC_RetCode.newBuilder().setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_OfferReward_TaskFinishedByOthers));
			GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_RetCode_VALUE, builder);
		}  else {
//			if (num == 1) {
//				fightMakeRewards.addAll(monster.getFirstRewardList());
//			} else if (num == 2) {
//				fightMakeRewards.addAll(monster.getSecondRewardList());
//			} else if (num == 3) {
//				fightMakeRewards.addAll(monster.getThridRewardList());
//			}
			fightMakeRewards.addAll(monster.getRewardList());
		}

//		List<Reward> perRewards = new ArrayList<>();
//		for (Reward reward : monster.getRewardList()) {
//			Reward.Builder rb = Reward.newBuilder();
//			rb.setId(reward.getId());
//			rb.setRewardType(reward.getRewardType());
//			float count = reward.getCount();
//			count = count * per;
//			count /= 100;
//			rb.setCount((int) count);
//			perRewards.add(rb.build());
//		}

		Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_OFFER_REWARD_FIGHT);
		RewardManager.getInstance().doRewardByList(playerId, fightMakeRewards, reason, false);
		return fightMakeRewards;

	}

	private OfferRewardData.Builder changeOffer(String id, boolean recordFirst, String winnerIdx, String winnerName, boolean end) {
		// 部分字段仅供展示,数据存储在自己rediskey中
		byte[] hget = jedis.hget(RedisKey.OFFER_REWARD.getBytes(), id.getBytes());
		if (hget == null) {
			return null;
		}
		try {
			OfferRewardData b = OfferRewardData.parseFrom(hget);
			OfferRewardData.Builder builder = b.toBuilder();
			builder.setWinCount(builder.getWinCount() + 1);
			builder.addWinnerPlayerIds(winnerIdx);
			if (recordFirst) {
				builder.setFirstAttPlayerName(winnerName);
			}
			if (end) {
				if (builder.getFirstAttPlayerName().equals("")) {
					builder.setFirstAttPlayerName(builder.getTmpFirstName());
				}
				OfferRewardObject cfg = OfferReward.getById(b.getGrade());
				if (cfg != null) {
					builder.setWinCount(cfg.getRewardnum());
				}
				builder.setEndtype(1);
			}
			jedis.hset(RedisKey.OFFER_REWARD.getBytes(), id.getBytes(), builder.build().toByteArray());
			return builder;
		} catch (Exception e) {
			return null;
		}
	}

	public long changeState(String offerId, boolean add, int type) {
		switch (type) {
		case OfferRewardChangeStateEnum.FIGHT:
			return changeRedisNum(RedisKey.OFFER_REWARD_Fight + offerId, 1, add);
		case OfferRewardChangeStateEnum.WIN:
			return changeRedisNum(RedisKey.OFFER_REWARD_WIN + offerId, 1, add);
//		case OfferRewardChangeStateEnum.MOCK:
//			return changeRedisNum(RedisKey.OFFER_REWARD_MOCK + offerId, 1, add);
		default:
			return -1;
		}
	}

	public long getTaskStateNum(String offerId, int type) {
		switch (type) {
		case OfferRewardChangeStateEnum.FIGHT:
			return NumberUtils.toLong(jedis.get(RedisKey.OFFER_REWARD_Fight + offerId));
		case OfferRewardChangeStateEnum.WIN: {
			return NumberUtils.toLong(jedis.get(RedisKey.OFFER_REWARD_WIN + offerId));
		}
//		case OfferRewardChangeStateEnum.MOCK:
//			return NumberUtils.toLong(jedis.get(RedisKey.OFFER_REWARD_MOCK + offerId));
		default:
			return -1;
		}
	}

	private class OfferRewardChangeStateEnum {
		public static final int FIGHT = 0;
		public static final int WIN = 1;
	}

	private long changeRedisNum(String id, int num, boolean add) {
		if (add) {
			return jedis.incrBy(id, num);
		} else {
			Long rNum = jedis.decrBy(id, num);
			if (rNum <= 0) {
				jedis.del(id);
			}
			return rNum;
		}
	}

	public void addNotice(String playerId) {
		notice.put(playerId, GlobalTick.getInstance().getCurrentTime() + 10000);
	}

	private int getStartIndex(String firstId, List<String> sortList) {
		if (firstId.equals("")) {
			return 0;
		} else {
			for (int i = 0; i < sortList.size(); i++) {
				if (sortList.get(i).equals(firstId)) {
					return i;
				}
			}
		}
		return 0;
	}

	private void sort(List<OfferRewardData> dataList, List<String> sortList) {
		dataList.sort(new Comparator<OfferRewardData>() {
			@Override
			public int compare(OfferRewardData o1, OfferRewardData o2) {
				if (o2.getStartTime() > o1.getStartTime()) {
					return -1;
				} else if (o2.getStartTime() == o1.getStartTime()) {
					return 0;
				} else {
					return 1;
				}
			}
		});

		for (OfferRewardData e : dataList) {
			sortList.add(e.getId());
		}
	}

	private List<String> getIds(int startIndex, List<String> sortList, int num) {
		List<String> ids = new ArrayList<>();
		for (int i = startIndex; i < sortList.size(); i++) {
			ids.add(sortList.get(i));
			num--;
			if (num <= 0) {
				break;
			}
		}
		return ids;
	}

	private List<OfferRewardData> getShowTask(List<String> showList, Map<String, OfferRewardData> map) {
		List<OfferRewardData> show = new ArrayList<>();
		for (String id : showList) {
			if (!map.containsKey(id)) {
				continue;
			}
			show.add(map.get(id));
		}
		return show;
	}

	public boolean init() {

		Map<Integer, Integer> gradeWeightMap = new HashMap<>();
		Map<Integer, Map<Integer, Integer>> bossGroupMap = new HashMap<>();
		List<Integer> grades = new ArrayList<>();
		for (Entry<Integer, OfferRewardObject> ent : OfferReward._ix_id.entrySet()) {
			if (ent.getKey() == 0) {
				continue;
			}
			int bossGroupWeight = 0;
			int[][] boss = ent.getValue().getBoss();
			Map<Integer, Integer> bossMap = new HashMap<>();
			for (int[] each : boss) {
				if (each.length < 2) {
					continue;
				}
				int weight = each[1];
				int bossGroup = each[0];
				bossGroupWeight += weight;
				bossMap.put(bossGroup, weight);
			}
			gradeWeightMap.put(ent.getKey(), bossGroupWeight);
			bossGroupMap.put(ent.getKey(), bossMap);
			grades.add(ent.getKey());
		}

		Set<Integer> offerRewardLimit = OfferRewardLevelLimit._ix_id.keySet();
		List<Integer> offerRewardOrderedLimit = offerRewardLimit.stream().sorted(Comparator.comparingInt(o -> o)).collect(Collectors.toList());
		int end = PlayerLevelConfig.maxLevel;
		int levelLimitCount = 1;
		Map<Integer, Integer> levelLimit = new HashMap<>();
		if (offerRewardOrderedLimit != null && !offerRewardOrderedLimit.isEmpty()) {
			for (Integer level : offerRewardOrderedLimit) {
				if (level > 0) {
					levelLimit.put(level, levelLimitCount++);
				}
			}
		}
		levelLimit.put(end, levelLimitCount++);
		this.levelLimit = levelLimit;
		this.gradeWeightMap = gradeWeightMap;
		this.bossGroupMap = bossGroupMap;
		this.grades = grades;
		GlobalTick.getInstance().addTick(this);
		return true;
	}

	private int getLevelLimit(int lv) {
		int level = 1;
		for (Entry<Integer, Integer> ent : this.levelLimit.entrySet()) {
			if (lv > ent.getKey() && level < ent.getValue()) {
				level = ent.getValue();
			}
		}
		return level;
	}

	private OfferRewardLevelLimitObject getLevelLimitConfig(int lv) {
		int level = 1;
		OfferRewardLevelLimitObject base = null;
		int levelLimitId = 0;
		for (Entry<Integer, Integer> ent : this.levelLimit.entrySet()) {
			if (ent.getValue() == 1) {
				base = OfferRewardLevelLimit.getById(ent.getKey());
			}
			if (lv > ent.getKey() && level < ent.getValue()) {
				level = ent.getValue();
				levelLimitId = ent.getKey();
			}
		}
		if (levelLimitId == 0) {
			return base;
		} else {
			return OfferRewardLevelLimit.getById(levelLimitId);
		}
	}

	private int rollBossGroup(int grade, int lv) {

		OfferRewardLevelLimitObject config = getLevelLimitConfig(lv);

		if (config == null) {
			return 0;
		}
		int[][] bossArr = null;
		if (grade == 3) {
			bossArr = config.getBoss1();
		} else if (grade == 5) {
			bossArr = config.getBoss2();
		} else if (grade == 7) {
			bossArr = config.getBoss3();
		} else if (grade == 9) {
			bossArr = config.getBoss4();
		}
		if (bossArr == null) {
			return 0;
		}

		Map<Integer, Integer> weightMap = new HashMap<>();
		int totalWeight = 0;
		for (int[] arr : bossArr) {
			if (arr.length < 2) {
				continue;
			}
			weightMap.put(arr[0], arr[1]);
			totalWeight += arr[1];
		}

		Random random = new Random();
		int luck = random.nextInt(totalWeight);
		int cur = 0;
		for (Entry<Integer, Integer> ent : weightMap.entrySet()) {
			cur += ent.getValue();
			if (luck <= cur) {
				return ent.getKey();
			}
		}
		return 0;
	}

	public void getPanelPrepare(String playerId, int grade) {
		SC_OfferRewardPrepare.Builder b = SC_OfferRewardPrepare.newBuilder();

		playerEntity player = playerCache.getByIdx(playerId);
		if (player == null) {
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardPrepare_VALUE, b);
			return;
		}

		DB_OfferReward offerRewardPrepare = player.getDb_data().getOfferRewardPrepare();
		Map<Integer, OfferRewardPrePare> prepareMapMap = offerRewardPrepare.getPrepareMapMap();
		OfferRewardPrePare.Builder prepare = null;
		boolean needSave = false;
		if (prepareMapMap.containsKey(grade)) {
			prepare = prepareMapMap.get(grade).toBuilder();
		} else {
			prepare = createOfferRewardPrePare(grade, player.getLevel());
			if (prepare == null) {
				return;
			}
			needSave = true;
		}

		b.setPrepare(prepare);

		OfferRewardPrePare savePrepare = prepare.build();
		if (needSave) {
			SyncExecuteFunction.executeConsumer(player, entity -> {
				DB_OfferReward.Builder offerReward = entity.getDb_data().getOfferRewardPrepare().toBuilder();
				offerReward.putPrepareMap(grade, savePrepare);
				entity.getDb_data().setOfferRewardPrepare(offerReward);
			});
		}
		GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardPrepare_VALUE, b);
	}

	private OfferRewardPrePare.Builder createOfferRewardPrePare(int grade, int playerlv) {
		int lvLimit = getLevelLimit(playerlv);
		int rollBossGroup = rollBossGroup(grade, playerlv);
		if (rollBossGroup == 0) {
			return null;
		}
		OfferRewardBossGroupObject config = OfferRewardBossGroup.getById(rollBossGroup);
		if (config == null) {
			return null;
		}
		OfferRewardPrePare.Builder builder = OfferRewardPrePare.newBuilder();
		builder.setGrade(grade);
		for (Integer boss : config.getAllboss()) {
			Builder createOfferRewardMonster = createOfferRewardMonster(boss);
			if (createOfferRewardMonster == null) {
				continue;
			}
			builder.addMonsterInfo(createOfferRewardMonster);
		}
		builder.setLevelLimit(lvLimit);
		return builder;
	}

	private OfferRewardMonster.Builder createOfferRewardMonster(int bossId) {
		OfferRewardBossObject config = OfferRewardBoss.getById(bossId);
		if (config == null) {
			return null;
		}
		OfferRewardMonster.Builder builder = OfferRewardMonster.newBuilder();
		builder.setFightMakeId(config.getBoss());
		builder.setStar(config.getStar());
		int buffSize = 2;
		if (config.getBuff().length <= buffSize) {
			for (int buff : config.getBuff()) {
				builder.addBuffId(buff);
			}
		} else {
			List<Integer> ignoreList = new ArrayList<>();
			int maxCount = 30;
			while (buffSize > 0) {
				int nextInt = random.nextInt(config.getBuff().length);
				int buffId = config.getBuff()[nextInt];
				if (ignoreList.contains(buffId)) {
					maxCount--;
					if (maxCount <= 0) {
						break;
					}
					continue;
				}
				ignoreList.add(buffId);
				builder.addBuffId(buffId);
				buffSize--;
				maxCount--;
				if (maxCount <= 0) {
					break;
				}
			}
		}
		List<int[]> rewardConfigs = randomBossRewardCfg(config.getReward(), config.getGeneraterewardcount());
		List<Reward> allShowReward = parseRewardByConfigList(rewardConfigs);

		builder.addAllReward(allShowReward);
//		builder.addAllFirstReward(getClaimPlayerRewards(config.getFight_reward1(), rewardConfigs));
//		builder.addAllSecondReward(getClaimPlayerRewards(config.getFight_reward2(), rewardConfigs));
//		builder.addAllThridReward(getClaimPlayerRewards(config.getFight_reward3(), rewardConfigs));
		return builder;
	}

//	private List<Reward> getClaimPlayerRewards(int[][] config, List<int[]> rewardConfigs) {
//		List<Reward> rewards = new ArrayList<>();
//		for (Integer rewardConfigIndex : getRewardConfigIndex(rewardConfigs)) {
//			for (int[] ints : config) {
//				if (ints[0] == rewardConfigIndex) {
//					Reward reward = RewardUtil.parseReward(ints[1], ints[2], ints[3]);
//					if (reward==null) {
//						LogUtil.error("OfferRewardBoss reward config error ,reward is null by config:{}", Arrays.asList(ints));
//						continue;
//					}
//					rewards.add(reward);
//				}
//			}
//		}
//		return rewards;
//	}
//
//	private List<Integer> getRewardConfigIndex(List<int[]> rewardConfigs) {
//		List<Integer> rewardIndex = new ArrayList<>();
//		for (int[] rewardConfig : rewardConfigs) {
//			rewardIndex.add(rewardConfig[0]);
//		}
//		return rewardIndex;
//	}

	private List<Reward> parseRewardByConfigList(List<int[]> rewardConfigs) {
		if (CollectionUtils.isEmpty(rewardConfigs)) {
			return Collections.emptyList();
		}
		List<Reward> result = new ArrayList<>();
		for (int[] rewardConfig : rewardConfigs) {
			Reward reward = RewardUtil.parseReward(rewardConfig[2], rewardConfig[3], rewardConfig[4]);
			if (reward==null){
				LogUtil.error("OfferRewardBoss reward config error ,reward is null by config:{}",Arrays.asList(rewardConfig));
				continue;
			}
			result.add(reward);
		}
		return result;
	}

	private List<int[]> randomBossRewardCfg(int[][] reward, int needNum) {
		List<int[]> rewardList = new ArrayList<>();
		int[] randomCfgBy2;
		if (reward.length <= needNum){
			rewardList.addAll(Arrays.asList(reward));
		} else {
			do {
				randomCfgBy2 = RandomUtil.getRandomCfgBy2(reward);
				if (!rewardList.contains(randomCfgBy2)) {
					rewardList.add(randomCfgBy2);
				}
			} while (rewardList.size() < needNum);
		}
		return rewardList;
	}

	public void getPanel(String playerId, int self, int star) {
		sendOfferRewardPanel(playerId, self, star, "", false);
	}

	public void refresh(String playerId, int self, int star, String firstId) {
		sendOfferRewardPanel(playerId, self, star, firstId, true);
	}

	private void sendOfferRewardPanel(String playerId, int self, int star, String firstId, boolean refresh) {

		playerEntity entity = playerCache.getByIdx(playerId);
		if (entity == null) {
			return;
		}
		if (!CrossArenaManager.getInstance().checkOpenDay(playerId, GameConfig.getById(GameConst.CONFIG_ID).getOfferreward_open())) {
			return;
		}
		int onePageNum = GameConfig.getById(GameConst.CONFIG_ID).getOfferreward_eachpage();
		Map<String, OfferRewardData> allTask = getLimitTask(playerId, entity.getLevel(), self, star, getPlayerShortId(playerId), getLevelLimit(entity.getLevel()));
		List<String> sortList = new ArrayList<>();
		List<OfferRewardData> dataList = new ArrayList<>(allTask.values());
		sort(dataList, sortList);
		if (refresh) {
			SC_OfferRewardRefresh.Builder b = SC_OfferRewardRefresh.newBuilder();
			b.setSelf(self);
			b.addAllShowTask(getShowTask(getIds(getStartIndex(firstId, sortList) + 1, sortList, onePageNum), allTask));
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardRefresh_VALUE, b);
		} else {
			SC_OfferRewardPanel.Builder b = SC_OfferRewardPanel.newBuilder();
			b.setSelf(self);
			b.addAllShowTask(getShowTask(getIds(getStartIndex(firstId, sortList), sortList, onePageNum), allTask));
			b.addAllIds(sortList);
			GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_OfferRewardPanel_VALUE, b);
		}
		addNotice(playerId);
	}

	private Map<String, OfferRewardData> getLimitTask(String playerId, int level, int self, int star, int playerShortId, int levelLimit) {
		long curTime = GlobalTick.getInstance().getCurrentTime();
		Map<String, OfferRewardData> allTask = new HashMap<>();

		Map<String, OfferRewardData> dataMap;
		if (self == 0) {
			OfferRewardData mockData = null;
			if (!mockOffer.containsKey(playerId)) {
				mockData = createMockOfferRewardData(playerId, level);
				mockOffer.put(playerId, mockData);
				mockOfferIdMap.put(mockData.getId(), mockData);
			} else {
				mockData = mockOffer.get(playerId);
			}
			if (mockData.getEndTime() <= GlobalTick.getInstance().getCurrentTime()) {
				mockData = createMockOfferRewardData(playerId, level);
				mockOffer.put(playerId, mockData);
				mockOfferIdMap.put(mockData.getId(), mockData);
			}
			if (star == 0 || star == mockData.getStar()) {
				allTask.put(mockData.getId(), mockData);
			}
		}

		if (star == 0) {
			dataMap = this.allTask;
		} else {
			dataMap = starTask.getOrDefault(star, new HashMap<>());
		}
		for (Entry<String, OfferRewardData> ent : dataMap.entrySet()) {
			int shortId = getPlayerIdByOfferId(ent.getValue().getId());
			if (shortId == 0) {
				continue;
			}
			OfferRewardObject cfg = OfferReward.getById(ent.getValue().getGrade());
			if (cfg == null) {
				continue;
			}
			if (self > 0) {
				if (shortId == playerShortId) {
					allTask.put(ent.getKey(), ent.getValue());
				}
			} else {
				if (shortId == playerShortId) {
					continue;
				}
				if (ent.getValue().getEndtype() != 0) {
					continue;
				}
				if (ent.getValue().getEndTime() <= curTime) {
					continue;
				}
				if (ent.getValue().getLevelLimit() != levelLimit) {
					continue;
				}
				if (ent.getValue().getWinCount() >= cfg.getRewardnum()) {
					continue;
				}
				if (ent.getValue().getWinnerPlayerIdsList().contains(playerId)) {
					continue;
				}
				allTask.put(ent.getKey(), ent.getValue());
			}
		}

		return allTask;
	}

	public void gmClearAllTask() {
		this.mockOffer.clear();
		this.mockOfferIdMap.clear();
		this.allTask.clear();
		this.starTask.clear();
		jedis.del(RedisKey.OFFER_REWARD.getBytes());
	}
}
