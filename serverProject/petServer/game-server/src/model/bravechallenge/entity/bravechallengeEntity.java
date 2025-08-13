/**
 * created by tool DAOGenerate
 */
package model.bravechallenge.entity;

import cfg.*;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GlobalData;
import common.IdGenerator;
import common.SyncExecuteFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import model.bravechallenge.dbCache.bravechallengeCache;
import model.crossarena.CrossArenaManager;
import model.obj.BaseObj;
import model.pet.PetFactory;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.util.PlayerUtil;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleRemainPet;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.PlayerBaseInfo;
import protocol.BraveChallenge.BravePoint;
import protocol.BraveChallenge.ChallengeProgress;
import protocol.BraveChallenge.ChallengeProgress.Builder;
import protocol.BraveChallenge.SC_BraveChallengeFinished;
import protocol.BraveChallenge.SC_BraveChallengePetHP;
import protocol.BraveChallengeDB.DB_ChallengeProgress;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.Pet;
import protocol.PetMessage.PetProperty;
import protocol.PetMessage.PetPropertyEntity;
import protocol.PrepareWar.TeamNumEnum;
import util.EventUtil;
import util.LogUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class bravechallengeEntity extends BaseObj {
	bravechallengeEntity() {
	}

	@Override
	public String getClassType() {
		return "bravechallengeEntity";
	}

	@Override
	public void putToCache() {
		bravechallengeCache.put(this);
	}

	@Override
	public void transformDBData() {
		this.challengeprogress = getProgressBuilder().build().toByteArray();
	}

	/**
	 * 主键
	 */
	private String idx;

	/**
	 * 宠物所属玩家idx
	 */
	private String playeridx;

	/**
	 * 玩家进度信息
	 */
	private byte[] challengeprogress;

	/**
	 * 获得主键
	 */
	public String getIdx() {
		return idx;
	}

	/**
	 * 设置主键
	 */
	public void setIdx(String idx) {
		this.idx = idx;
	}

	/**
	 * 获得宠物所属玩家idx
	 */
	public String getPlayeridx() {
		return playeridx;
	}

	/**
	 * 设置宠物所属玩家idx
	 */
	public void setPlayeridx(String playeridx) {
		this.playeridx = playeridx;
	}

	/**
	 * 获得玩家进度信息
	 */
	public byte[] getChallengeprogress() {
		return challengeprogress;
	}

	/**
	 * 设置玩家进度信息
	 */
	public void setChallengeprogress(byte[] challengeprogress) {
		this.challengeprogress = challengeprogress;
	}

	@Override
	public String getBaseIdx() {
		return idx;
	}

	/*************************** 分割 **********************************/

	public static final int POINT_TYPE_FIGHT = 1;
	public static final int POINT_TYPE_REWARDS = 2;

	private DB_ChallengeProgress.Builder progressBuilder;

	public DB_ChallengeProgress.Builder getProgressBuilder() {
		if (progressBuilder == null) {
			if (getChallengeprogress() != null) {
				try {
					progressBuilder = DB_ChallengeProgress.parseFrom(getChallengeprogress()).toBuilder();
				} catch (InvalidProtocolBufferException e) {
					LogUtil.printStackTrace(e);
					LogUtil.error("parse ChallengeProgress fail, return new ChallengeProgress.builder");
					progressBuilder = DB_ChallengeProgress.newBuilder();
				}
			} else {
				progressBuilder = DB_ChallengeProgress.newBuilder();
			}
		}

		return progressBuilder;
	}

	/**
	 * 推送勇气试炼进度信息
	 */
	public void sendProgress() {
		SC_BraveChallengePetHP.Builder builder = SC_BraveChallengePetHP.newBuilder();
		builder.addAllPet(getProgressBuilder().getPetsRemainHpMap().values());
		builder.setProgress(getProgressBuilder().getProgress());
		builder.addAllBoss(getProgressBuilder().getBossRemainHpMap().values());
		GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_BraveChallengePetHP_VALUE, builder);
	}

	public bravechallengeEntity(String initPlayerId) {
		idx = IdGenerator.getInstance().generateId();
		playeridx = initPlayerId;
		getProgressBuilder().setNewGame(true);
	}

	public void updateDailyDate(boolean sendMsg) {
		if (PlayerUtil.queryFunctionLock(getPlayeridx(), EnumFunction.CourageTrial)) {
			return;
		}
		// 判断今日是否失败, 今日挑战且为通关才计算为未通关
		boolean todayFailed = getProgressBuilder().getProgress() < BraveChallengePoint.maxPoint && getProgressBuilder().getTodayChallenge();
		int failureTimes = getProgressBuilder().getFailureTimes() + (todayFailed ? 1 : -1);

		getProgressBuilder().clear();
		getProgressBuilder().setFailureTimes(failureTimes);
		getProgressBuilder().setNewGame(true);

		initAllPoint();

		// 清空队伍
		EventUtil.cleatTeam(getPlayeridx(), TeamNumEnum.TNE_Courge, sendMsg);

//		if (sendMsg) {
			GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_BraveChallengeFinished_VALUE, SC_BraveChallengeFinished.newBuilder());
//		}
	}

	/**
	 * 获取发送给客户端的进度信息
	 *
	 * @return
	 */
	public ChallengeProgress getClientProgress() {
		// 初始化所有关卡
		if (needInit()) {
			if (!initAllPoint()) {
				LogUtil.error("braveChallengeEntity, init player:" + getPlayeridx() + " all point failed");
				return null;
			}
		}

		Builder result = ChallengeProgress.newBuilder();
		DB_ChallengeProgress.Builder progressBuilder = getProgressBuilder();
		if (progressBuilder != null) {
			result.setProgress(progressBuilder.getProgress());
			result.addAllPet(progressBuilder.getPetsRemainHpMap().values());
			result.addAllBoss(progressBuilder.getBossRemainHpMap().values());
			result.setTodayRebornTimes(progressBuilder.getTodayRebornTimes());
			result.addAllPoint(progressBuilder.getPointCfgMap().values());
			result.setNewGame(progressBuilder.getNewGame());
		}
		return result.build();
	}

	/**
	 * 判断是否需要初始化
	 *
	 * @return
	 */
	private boolean needInit() {
		return getProgressBuilder().getPointCfgCount() <= 0;
	}

	public boolean initAllPoint() {
		MonsterDifficultyObject difficultyObject = MonsterDifficulty.getByPlayerIdx(getPlayeridx());
		if (difficultyObject == null) {
			LogUtil.error("model.bravechallenge.entity.bravechallengeEntity.initAllPoint, can not get monster difficult config, playerIdx:" + getPlayeridx());
			return false;
		}

		int playerLv = PlayerUtil.queryPlayerLv(getPlayeridx());
		PlayerLevelConfigObject levelConfig = PlayerLevelConfig.getByLevel(playerLv);
		if (levelConfig == null) {
			LogUtil.error("model.bravechallenge.entity.bravechallengeEntity.initAllPoint, can not get level config, playerIdx:" + getPlayeridx() + ", level:" + playerLv);
			return false;
		}

		// 初始化关卡信息
		for (BraveChallengePointObject braveCfg : BraveChallengePoint._ix_id.values()) {
			if (braveCfg.getId() <= 0) {
				continue;
			}

			BravePoint.Builder point = null;
			if (braveCfg.getPointtype() == POINT_TYPE_FIGHT) {
				PlayerBaseInfo baseInfo = initPointMonsterInfo(difficultyObject.getBravabaselv());
				getProgressBuilder().putMonsterInfo(braveCfg.getId(), baseInfo);
				point = initBattlePoint(difficultyObject, braveCfg);
			} else if (braveCfg.getPointtype() == POINT_TYPE_REWARDS) {
				point = initRewardsPoint(braveCfg);
			}

			if (point == null) {
				LogUtil.error("bravechallengeEntity.initAllPoint, init point failed， playerIdx:" + getPlayeridx() + " point id:" + braveCfg.getId());
				return false;
			}

			// 设置结点奖励
			List<Reward> rewards = getPointRewards(levelConfig.getBravefightreward(), braveCfg.getId());
			if (CollectionUtils.isEmpty(rewards)) {
				LogUtil.error("bravechallengeEntity.initAllPoint, can not get point  rewards， playerIdx:" + getPlayeridx() + " point id:" + braveCfg.getId());
				return false;
			}
			point.addAllRewards(rewards);
			getProgressBuilder().putPointCfg(point.getPointId(), point.build());
		}
		return true;
	}

	public static List<Reward> getPointRewards(int[][] rewardConfigs, int pointId) {
		if (rewardConfigs == null) {
			return null;
		}
		int findRewardsId = 0;
		for (int[] rewardConfig : rewardConfigs) {
			if (rewardConfig[0] == pointId) {
				findRewardsId = rewardConfig[1];
				break;
			}
		}
		return RewardUtil.getRewardsByRewardId(findRewardsId);
	}

	private PlayerBaseInfo initPointMonsterInfo(int baseLv) {
		PlayerBaseInfo.Builder resultBuilder = PlayerBaseInfo.newBuilder();
		resultBuilder.setPlayerId(IdGenerator.getInstance().generateId());
		resultBuilder.setPlayerName(playerCache.getInstance().randomGetName());
		resultBuilder.setAvatar(Head.randomGetAvatar());
		resultBuilder.setLevel(baseLv);
		return resultBuilder.build();
	}

	private BravePoint.Builder initBattlePoint(MonsterDifficultyObject difficultyCfg, BraveChallengePointObject pointCfg) {
		// 初始化怪物属性修正
/*		ExtendProperty.Builder property = BattleUtil.initMonsterExPropertyAdjust(getPlayeridx(), 2);

		if (difficultyCfg == null || pointCfg == null) {
			return null;
		}*/

		int[][] bravepets = difficultyCfg.getBravepets();
		if (bravepets.length <= 0) {
			return null;
		}
		int nextInt = new Random().nextInt(bravepets.length);
		int[] is = bravepets[nextInt];

		List<int[]> cfgPetArr = new ArrayList<>();

		int[] tmp = new int[2];
		int count = 0;
		for (int i : is) {
			count++;
			tmp[count - 1] = i;
			if (count == 2) {
				count = 0;
				cfgPetArr.add(tmp);
				tmp = new int[2];
			}
		}

		int[][] cfgPets = new int[cfgPetArr.size()][2];
		for (int i = 0; i < cfgPetArr.size(); i++) {
			cfgPets[i] = cfgPetArr.get(i);
		}
		List<Pet.Builder> petList = PetFactory.buildMonsterBuilderList(cfgPets, getCurPointPetLv(difficultyCfg.getBravabaselv(), pointCfg.getRiviselv()), null);
		if (CollectionUtils.isEmpty(petList)) {
			LogUtil.error("bravechallengeEntity.initAllPoint, init point pets failed, pointId:" + pointCfg.getId());
			return null;
		}

		BravePoint.Builder resultBuilder = BravePoint.newBuilder();

		if (pointCfg.getNeedboss()) {
			int[] exProperty = pointCfg.getExproperty();
			if (exProperty.length < 3) {
				LogUtil.error("bravechallengeEntity.initAllPoint, point ex boss property length is less than 3");
			} else {
				Pet.Builder exPet = petList.get(new Random().nextInt(petList.size()));
				if (exPet != null) {
					addExProperty(Collections.singletonList(exPet), pointCfg.getExproperty());
					resultBuilder.setBossPetId(exPet.getId());
				}
			}
		}

		List<Pet> collect = petList.stream().map(Pet.Builder::build).collect(Collectors.toList());
		List<BattlePetData> battlePetData = petCache.getInstance().buildPetBattleData(playeridx, collect, BattleSubTypeEnum.BSTE_BreaveChallenge, false);
		if (CollectionUtils.isNotEmpty(battlePetData)) {
			resultBuilder.addAllEnemyPets(battlePetData);
		}

		resultBuilder.setPointId(pointCfg.getId());
		return resultBuilder;
	}

	/**
	 * 奖励调整至PlayerLevelConfig 获取
	 *
	 * @param pointCfg
	 * @return
	 */
	private BravePoint.Builder initRewardsPoint(BraveChallengePointObject pointCfg) {
		if (pointCfg == null) {
			return null;
		}

		BravePoint.Builder resultBuilder = BravePoint.newBuilder();
		resultBuilder.setPointId(pointCfg.getId());
//        List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(pointCfg.getFightreward());
//        if (CollectionUtils.isNotEmpty(rewards)) {
//            resultBuilder.addAllRewards(rewards);
//        }
//
//        List<Reward> randomRewards = RewardUtil.drawMustRandomReward(pointCfg.getRewardpointrandomrewards(), pointCfg.getRewardpointrandomtimes());
//        if (CollectionUtils.isNotEmpty(randomRewards)) {
//            resultBuilder.addAllRewards(randomRewards);
//        }
		return resultBuilder;
	}

	/**
	 * @param petList
	 * @param exPro   [攻击，防御，血量] 百分比
	 * @return
	 */
	private List<Pet.Builder> addExProperty(List<Pet.Builder> petList, int[] exPro) {
		if (CollectionUtils.isEmpty(petList) || exPro == null || exPro.length <= 3) {
			return petList;
		}

		for (Pet.Builder builder : petList) {
			for (PetPropertyEntity.Builder proBuilder : builder.getPetPropertyBuilder().getPropertyBuilderList()) {
				if (proBuilder.getPropertyType() == PetProperty.ATTACK_VALUE) {
					double newValue = proBuilder.getPropertyValue() * (1 + ((double) exPro[0] / 100));
					proBuilder.setPropertyValue((int) newValue);
				} else if (proBuilder.getPropertyType() == PetProperty.DEFENSIVE_VALUE) {
					double newValue = proBuilder.getPropertyValue() * (1 + ((double) exPro[1] / 100));
					proBuilder.setPropertyValue((int) newValue);
				} else if (proBuilder.getPropertyType() == PetProperty.HEALTH_VALUE) {
					double newValue = proBuilder.getPropertyValue() * (1 + ((double) exPro[2] / 100));
					proBuilder.setPropertyValue((int) newValue);
				}
			}
		}
		return petList;
	}

	/**
	 * 计算玩家当前关卡宠物等级
	 *
	 * @param baseLv
	 * @param reviseLv
	 * @return
	 */
	private int getCurPointPetLv(int baseLv, int reviseLv) {
		int tempLv = baseLv + reviseLv;

		// 判断是否需要难度等级修正
		int adjustLv = BraveChallengeDiffAdjust.getByFailureTimes(getProgressBuilder().getFailureTimes());
		return tempLv + adjustLv;
	}

//    private void initMonsterExPropertyAdjust() {
//        int curNode = mainlineCache.getInstance().getPlayerCurNode(getPlayeridx());
//        MainLineNodeObject nodeCfg = null;
//        for (int i = curNode; i > 0; i--) {
//            MainLineNodeObject tempNodeCfg = MainLineNode.getById(i);
//            if (tempNodeCfg != null && tempNodeCfg.isBattleNode()) {
//                nodeCfg = tempNodeCfg;
//                break;
//            }
//        }
//
//        if (nodeCfg == null) {
//            return;
//        }
//
//        FightMakeObject fightObj = FightMake.getById(nodeCfg.getFightmakeid());
//        if (fightObj == null) {
//            return;
//        }
//
//        ExtendProperty.Builder property = BattleUtil.builderMonsterExtendProperty(2, fightObj.getMonsterpropertyext());
//        if (property != null) {
//            getProgressBuilder().setMonsterExProperty(property);
//        }
//    }

	public void rebornPets() {
		SyncExecuteFunction.executeConsumer(this, cacheTemp -> {
			cacheTemp.getProgressBuilder().setTodayRebornTimes(cacheTemp.getProgressBuilder().getTodayRebornTimes() + 1).clearPetsRemainHp();
		});
	}

	/**
	 * 更新宠物的剩余血量
	 *
	 * @param win           是否胜利
	 * @param remainPetList
	 */
	public void updateRemainHp(boolean win, List<BattleRemainPet> remainPetList) {
		//擂台荣誉等级额外恢复血量千分比
		int resume = findResumeHpByCrossArenaGrade();

		for (protocol.Battle.BattleRemainPet battleRemainPet : remainPetList) {
			if (1 == battleRemainPet.getCamp()) {
				battleRemainPet = getBattleRemainPet(resume, battleRemainPet);
				getProgressBuilder().putPetsRemainHp(battleRemainPet.getPetId(), battleRemainPet);
			}
		}
		if (win) {
			getProgressBuilder().clearBossRemainHp();
		} else {
			// 更新boss剩余血量
			for (protocol.Battle.BattleRemainPet battleRemainPet : remainPetList) {
				if (2 == battleRemainPet.getCamp()) {
					getProgressBuilder().putBossRemainHp(battleRemainPet.getPetId(), battleRemainPet);
				}
			}
		}
	}

	private BattleRemainPet getBattleRemainPet(int resume, BattleRemainPet battleRemainPet) {
		if (resume <= 0) {
			return battleRemainPet;
		}
		if (battleRemainPet.getRemainHpRate() != 1000) {
			return battleRemainPet.toBuilder().setRemainHpRate(Math.min(1000, battleRemainPet.getRemainHpRate() + resume)).build();
		}
		return battleRemainPet;
	}

	private int findResumeHpByCrossArenaGrade() {
		int gradeLv = CrossArenaManager.getInstance().findPlayerGradeLv(getPlayeridx());
		CrossArenaLvCfgObject cfg = CrossArenaLvCfg.getByLv(gradeLv);
		if (cfg == null) {
			return 0;
		}
		return cfg.getBraveresumehp();
	}

	public boolean todayFirstPlay() {
		return !getProgressBuilder().getTodayChallenge();
	}
}