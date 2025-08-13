package server.handler.bossTower;

import cfg.BossTowerConfig;
import cfg.BossTowerConfigObject;
import cfg.FightMake;
import cfg.FightMakeObject;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.battle.pve.BossTowerPveBattleController;
import model.bosstower.dbCache.bosstowerCache;
import model.bosstower.entity.bosstowerEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.GamePlayLog;
import protocol.BossTower.CS_SweepBossTower;
import protocol.BossTower.EnumBossTowerDifficult;
import protocol.BossTower.SC_SweepBossTower;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;

import java.util.List;

/**
 * @author huhan
 * @date 2020/12/9
 */
@MsgId(msgId = MsgIdEnum.CS_SweepBossTower_VALUE)
public class SweepBossTowerHandler extends AbstractBaseHandler<CS_SweepBossTower> {
	@Override
	protected CS_SweepBossTower parse(byte[] bytes) throws Exception {
		return CS_SweepBossTower.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_SweepBossTower req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		SC_SweepBossTower.Builder resultBuilder = SC_SweepBossTower.newBuilder();
		resultBuilder.setRetCode(GameUtil.buildRetCode(doExecute(playerIdx, req)));
		gsChn.send(MsgIdEnum.SC_SweepBossTower_VALUE, resultBuilder);
	}

	private RetCodeEnum doExecute(String playerIdx, CS_SweepBossTower req) {
		BossTowerConfigObject cfg = BossTowerConfig.getById(req.getCfgId());
		if (cfg == null || cfg.getUnlevel() > PlayerUtil.queryPlayerLv(playerIdx)) {
			return RetCodeEnum.RCE_LvNotEnough;
		}

		// 定死地狱难度
		EnumBossTowerDifficult fightMakeDiff = BossTowerConfig.getFightMakeDiff(cfg, req.getFightMakeId());
		if (fightMakeDiff == null || fightMakeDiff == EnumBossTowerDifficult.EBS_Null || fightMakeDiff != EnumBossTowerDifficult.EBS_Unbeatable) {
			return RetCodeEnum.RCE_ErrorParam;
		}

		FightMakeObject fightMakeCfg = FightMake.getById(req.getFightMakeId());
		if (fightMakeCfg == null) {
			return RetCodeEnum.RCE_ErrorParam;
		}

		bosstowerEntity entity = bosstowerCache.getInstance().getEntity(playerIdx);
		if (entity == null) {
			return RetCodeEnum.RCE_UnknownError;
		}
		RetCodeEnum retCode = SyncExecuteFunction.executeFunction(entity, e -> {
			if (!entity.canBattle(req.getCfgId(), req.getFightMakeId())) {
				return RetCodeEnum.RCE_Battle_OutOfLimit;
			}
			boolean add = true;

//			int todayAlreadyChallengeTimes = entity.getDbBuilder().getTodayAlreadyChallengeTimes();
//			int todayTotalLimit = 0;
//			int otherTime = entity.getBossTowerPassConditionBuilder(req.getCfgId()).getOtherTimeMap().getOrDefault(req.getCfgId(), 0);
//			VIPConfigObject vipCfg = VIPConfig.getById(PlayerUtil.queryPlayerVipLv(playerIdx));
//			if (vipCfg != null) {
//				todayTotalLimit = vipCfg.getBosstowerchallengetimes();
//			}
//			if (todayAlreadyChallengeTimes >= todayTotalLimit) {
//				if (otherTime > 0) {
//					add = false;
//				}
//			}
			entity.addBattleTimes(req.getCfgId(), req.getFightMakeId(), 0, add);

			List<Reward> rewards = BossTowerPveBattleController.getWinBattleRewards(req.getCfgId(), req.getFightMakeId());
			Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_BossTower);
			RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);

			return RetCodeEnum.RCE_Success;
		});

		EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TEE_BossTower_CumuJoin, 1, 0);
		LogService.getInstance().submit(new GamePlayLog(playerIdx, EnumFunction.BossTower, fightMakeDiff.getNumber()));
		return retCode;
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.BossTower;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_SweepBossTower_VALUE, SC_SweepBossTower.newBuilder().setRetCode(retCode));
	}
}
