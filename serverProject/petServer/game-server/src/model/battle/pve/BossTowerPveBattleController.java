package model.battle.pve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.CollectionUtils;

import cfg.BossTowerConfig;
import cfg.BossTowerConfigObject;
import cfg.PetRuneProperties;
import cfg.PetRunePropertiesObject;
import common.SyncExecuteFunction;
import model.battle.AbstractPveBattleController;
import model.battle.BattleUtil;
import model.bosstower.dbCache.bosstowerCache;
import model.bosstower.entity.bosstowerEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.GamePlayLog;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult.Builder;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.BossTower.EnumBossTowerDifficult;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;

/**
 * @author huhan
 * @date 2020/06/28
 */
public class BossTowerPveBattleController extends AbstractPveBattleController {

	/**
	 * 参数含义
	 */
	private static final String CONFIG_ID = "ConfigId";
	private static final String CHALLENGE_FIGHT_MAKE_ID = "ChallengeFightMakeId";

	@Override
	public boolean enterParamsSettle(List<String> enterParams) {
		if (CollectionUtils.size(enterParams) < 2) {
			return false;
		}

		putEnterParam(CONFIG_ID, enterParams.get(0));
		putEnterParam(CHALLENGE_FIGHT_MAKE_ID, enterParams.get(1));
		return true;
	}

	@Override
	protected RetCodeEnum initFightInfo() {
		int cfgId = getIntEnterParam(CONFIG_ID);
		BossTowerConfigObject cfg = BossTowerConfig.getById(cfgId);
		if (cfg == null) {
			return RetCodeEnum.RSE_ConfigNotExist;
		}

		bosstowerEntity entity = bosstowerCache.getByIdx(getPlayerIdx());
		if (entity == null) {
			return RetCodeEnum.RCE_ErrorParam;
		}
		if (cfgId > entity.getDbBuilder().getMaxCfgId() + 1) {
			return RetCodeEnum.RCE_ErrorParam;
		}
		// 等级限制
		if (cfg.getUnlevel() > PlayerUtil.queryPlayerLv(getPlayerIdx())) {
			return RetCodeEnum.RCE_LvNotEnough;
		}

		// 次数限制
		if (outOfBattleLimit()) {
			return RetCodeEnum.RCE_Battle_OutOfLimit;
		}

		int challengeFightMakeId = getIntEnterParam(CHALLENGE_FIGHT_MAKE_ID);
		if (challengeFightMakeId != cfg.getFightmakeid() && challengeFightMakeId != cfg.getDifficultfightmakeid() && challengeFightMakeId != cfg.getUnbeatablefightmakeid()) {
			return RetCodeEnum.RCE_ErrorParam;
		}

		setFightMakeId(challengeFightMakeId);
		initBuff();
		addFightParams(FightParamTypeEnum.FPTE_FightStar);
		return RetCodeEnum.RCE_Success;
	}

	private void initBuff() {
		int cfgId = getIntEnterParam(CONFIG_ID);
		int challengeFightMakeId = getIntEnterParam(CHALLENGE_FIGHT_MAKE_ID);

		// 优化内容-buff固定
//        Map<Integer, int[]> buffs = BossTowerManager.getInstance().getBuffs(cfgId);

//        for(Map.Entry<Integer,int[]> ent : buffs.entrySet()) {
//            Battle.ExtendProperty.Builder extendProperty = Battle.ExtendProperty.newBuilder().setCamp(ent.getKey());
//            for (Integer buffId : ent.getValue()) {
//                extendProperty.addBuffData(Battle.PetBuffData.newBuilder().setBuffCfgId(buffId).setBuffCount(1));
//            }
//            addExtendProp(extendProperty.build());
//        }
	}

	/**
	 * 判断玩家战斗次数是否已经超过限制
	 *
	 * @return
	 */
	private boolean outOfBattleLimit() {
		// 修改玩家挑战进度
		bosstowerEntity entity = bosstowerCache.getInstance().getEntity(getPlayerIdx());
		if (entity == null) {
			return false;
		}

		return !entity.canBattle(getIntEnterParam(CONFIG_ID), getIntEnterParam(CHALLENGE_FIGHT_MAKE_ID));
	}

	@Override
	protected void initSuccess() {
		EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TEE_BossTower_CumuJoin, 1, 0);
	}

	@Override
	public int getPointId() {
		return getIntEnterParam(CONFIG_ID);
	}

	@Override
	public String getLogExInfo() {
		return null;
	}

	@Override
	protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, Builder resultBuilder) {
		boolean win = realResult.getWinnerCamp() == getCamp();
		if (!win) {
			return;
		}
		BossTowerConfigObject towerCfg = BossTowerConfig.getById(getIntEnterParam(CONFIG_ID));
		if (towerCfg == null) {
			return;
		}
		// 修改玩家挑战进度
		bosstowerEntity entity = bosstowerCache.getInstance().getEntity(getPlayerIdx());
		if (entity == null) {
			return;
		}
		int battleStar = (int) BattleUtil.getFightParamsValue(realResult.getFightParamsList(), FightParamTypeEnum.FPTE_FightStar);
		SyncExecuteFunction.executeConsumer(entity, p -> {
			boolean firstPass = entity.isFirstPass(getIntEnterParam(CONFIG_ID), getFightMakeId());
			boolean add = true;
			if (firstPass) {
				add = false;
			} /*else {
				//非首通,今日次数用完并且有额外次数
				int todayFightTime = entity.getBossTowerPassConditionBuilder(getIntEnterParam(CONFIG_ID)).getPassInfoMap().getOrDefault(getFightMakeId(), 0);
				int dayLimit = towerCfg.getPasslimit();
				if (todayFightTime >= dayLimit) {
					add = false;
				}
			}*/
			entity.addBattleTimes(getIntEnterParam(CONFIG_ID), getFightMakeId(), battleStar, add);
		});

		// 玩法统计,胜利才统计
		EnumBossTowerDifficult difficult = BossTowerConfig.getDifficult(getIntEnterParam(CONFIG_ID), getIntEnterParam(CHALLENGE_FIGHT_MAKE_ID));
		LogService.getInstance().submit(new GamePlayLog(getPlayerIdx(), EnumFunction.BossTower, difficult.getNumber()));

		// 目标击败boss
		EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TEE_BossTower_DefeatBoss, 1, getIntEnterParam(CHALLENGE_FIGHT_MAKE_ID));
	}

	@Override
	public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
		// 失败不发放奖励
		if (battleResult == null || battleResult.getWinnerCamp() != getCamp()) {
			return Collections.emptyList();
		}
		List<Reward> totalReward = getFirstWinBattleRewards(getIntEnterParam(CONFIG_ID), getFightMakeId());
		Reason reason = ReasonManager.getInstance().borrowReason(getRewardSourceType(), getLogExInfo());
		RewardManager.getInstance().doRewardByList(getPlayerIdx(), totalReward, reason, false);
		return totalReward;
	}

	public static List<Reward> getWinBattleRewards(int cfgId, int fightMakeId) {
		List<Reward> totalReward = new ArrayList<>();
		// 固定奖励
//		List<Reward> fightMakeReward = new ArrayList<>();
		List<Reward> fightMakeReward = RewardUtil.getRewardsByFightMakeId(fightMakeId);
		if (CollectionUtils.isNotEmpty(fightMakeReward)) {
			totalReward.addAll(fightMakeReward);
		}
//		// 随机奖励
//		int[][] rewardArray = BossTowerConfig.getRandomRewardArray(cfgId, fightMakeId);
//		if (rewardArray != null) {
//			List<Reward> randomReward = randomReward(rewardArray);
//			if (CollectionUtils.isNotEmpty(randomReward)) {
//				totalReward.addAll(randomReward);
//			}
//		}
//		BossTowerConfigObject towerCfg = BossTowerConfig.getById(cfgId);
//		if (towerCfg != null) {
//			fightMakeReward = RewardUtil.getRewardsByRewardId(towerCfg.getUnbeatablereward());
//		}

		return RewardUtil.mergeReward(totalReward);
//		return fightMakeReward;
	}

	public List<Reward> getFirstWinBattleRewards(int cfgId, int fightMakeId) {
//		List<Reward> totalReward = new ArrayList<>();

		List<Reward> fightMakeReward = new ArrayList<>();

		boolean first = false;
		bosstowerEntity entity = bosstowerCache.getInstance().getEntity(getPlayerIdx());
		// 固定奖励
		if (entity != null) {
			first = entity.isFirstPass(cfgId, fightMakeId);
		}
		if (first) {
			fightMakeReward = RewardUtil.getRewardsByRewardId(BossTowerConfig.getFirstReward(cfgId));
		} else {
//			BossTowerConfigObject towerCfg = BossTowerConfig.getById(cfgId);
//			if (towerCfg != null) {
//				fightMakeReward = RewardUtil.getRewardsByRewardId(towerCfg.getUnbeatablereward());
//			}
			fightMakeReward = RewardUtil.getRewardsByFightMakeId(fightMakeId);
		}
//		if (CollectionUtils.isNotEmpty(fightMakeReward)) {
//			totalReward.addAll(fightMakeReward);
//		}
		// 随机奖励
//		int[][] rewardArray = BossTowerConfig.getRandomRewardArray(cfgId, fightMakeId);
//		if (rewardArray != null) {
//			List<Reward> randomReward = randomReward(rewardArray);
//			if (CollectionUtils.isNotEmpty(randomReward)) {
//				totalReward.addAll(randomReward);
//			}
//		}
		return fightMakeReward;
	}

	/**
	 * 随机符文奖励, 概率随机一次,符文随机x次
	 *
	 * @param randomArray
	 * @return
	 */
	private static List<Reward> randomReward(int[][] randomArray) {
		if (randomArray == null) {
			return null;
		}

		List<Reward> result = new ArrayList<>();
		Random random = new Random();
		for (int[] ints : randomArray) {
			if (ints.length < 3) {
				continue;
			}

			// 总概率1000写死
			if (ints[1] > random.nextInt(1000)) {
				for (int i = 0; i < ints[2]; i++) {
					PetRunePropertiesObject rune = PetRuneProperties.randomGetRuneByRarity(ints[0]);
					if (rune != null) {
						result.add(RewardUtil.parseReward(RewardTypeEnum.RTE_Rune, rune.getRuneid(), 1));
					}
				}
			}
		}
		return result;
	}

	@Override
	public BattleSubTypeEnum getSubBattleType() {
		return BattleSubTypeEnum.BSTE_BossTower;
	}

	@Override
	public RewardSourceEnum getRewardSourceType() {
		return RewardSourceEnum.RSE_BossTower;
	}

	@Override
	public TeamTypeEnum getUseTeamType() {
		return TeamTypeEnum.TTE_Common;
	}
}
