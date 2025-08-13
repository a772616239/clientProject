package server.handler.patrol;

import cfg.GameConfig;
import cfg.MonsterDifficulty;
import common.AbstractBaseHandler;
import common.GameConst;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.mainLine.dbCache.mainlineCache;
import model.patrol.dbCache.patrolCache;
import model.patrol.dbCache.service.IPatrolService;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.patrol.entity.patrolEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.MessageId;
import protocol.Patrol;
import protocol.RetCodeId;
import protocol.Common.Reward;
import protocol.Common.RewardTypeEnum;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_PatrolSweep_VALUE;

/**
 * 虚空秘境扫荡
 */
@MsgId(msgId = MessageId.MsgIdEnum.CS_PatrolSweep_VALUE)
public class PatrolSweepHandler extends AbstractBaseHandler<Patrol.CS_PatrolSweep> {
	private IPatrolService patrolService = PatrolServiceImpl.getInstance();

	@Override
	protected Patrol.CS_PatrolSweep parse(byte[] bytes) throws Exception {
		return Patrol.CS_PatrolSweep.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gameServerTcpChannel, Patrol.CS_PatrolSweep req, int i) {
		// 获取当前channel对应playerId
		String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());

		Patrol.SC_PatrolSweep.Builder msg = Patrol.SC_PatrolSweep.newBuilder();
		// 查询巡逻队
		patrolEntity cache = patrolCache.getInstance().getCacheByPlayer(playerId);
		if (cache == null) {
			msg.setResult(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Patrol_TodayNotFinishPlay));
			gameServerTcpChannel.send(SC_PatrolSweep_VALUE, msg);
			return;
		}

		if (cache.getFinish() != 1) {
			msg.setResult(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Patrol_TodayNotFinishPlay));
			gameServerTcpChannel.send(SC_PatrolSweep_VALUE, msg);
			return;
		}

		Common.Consume consume = ConsumeUtil.parseConsume(GameConfig.getById(GameConst.CONFIG_ID).getPatrolsweepconsume());

		List<Common.Reward> sweepRewards = getSweepRewards(playerId);

		if (CollectionUtils.isEmpty(sweepRewards)) {
			msg.setResult(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError));
			gameServerTcpChannel.send(SC_PatrolSweep_VALUE, msg);
			return;
		}

		ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_Patrol);

		if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, reason)) {
			msg.setResult(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatieralNotEnough));
			gameServerTcpChannel.send(SC_PatrolSweep_VALUE, msg);
			return;

		}
		Reward.Builder reward = Reward.newBuilder();
		reward.setId(GameConfig.getById(GameConst.CONFIG_ID).getFeatidlist()[2]);
		reward.setRewardType(RewardTypeEnum.RTE_Item);
		reward.setCount(GameConfig.getById(GameConst.CONFIG_ID).getPatrol_sweep());
		sweepRewards.add(reward.build());
		RewardManager.getInstance().doRewardByList(playerId, sweepRewards, reason, true);
		msg.setResult(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
		gameServerTcpChannel.send(SC_PatrolSweep_VALUE, msg);
	}

	private List<Common.Reward> getSweepRewards(String playerId) {
		int playerCurCheckPoint = mainlineCache.getInstance().getPlayerCurCheckPoint(playerId);
		return RewardUtil.parseRewardIntArrayToRewardList(MonsterDifficulty.getById(playerCurCheckPoint).getPatrolsweepreward());

	}

	@Override
	public Common.EnumFunction belongFunction() {
		return Common.EnumFunction.Patrol;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MessageId.MsgIdEnum.SC_PatrolSweep_VALUE, Patrol.SC_PatrolSweep.newBuilder().setResult(retCode));
	}
}
