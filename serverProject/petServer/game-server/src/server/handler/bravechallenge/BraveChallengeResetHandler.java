package server.handler.bravechallenge;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.bravechallenge.dbCache.bravechallengeCache;
import model.bravechallenge.entity.bravechallengeEntity;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.util.PlayerUtil;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.BraveChallenge.CS_BraveChallengeReset;
import protocol.BraveChallenge.ChallengeProgress;
import protocol.BraveChallenge.SC_BraveChallengeInit;
import protocol.BraveChallenge.SC_BraveChallengeReset;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 重置
 *
 * @author hammer
 * @date 2021/11/01
 */
@MsgId(msgId = MsgIdEnum.CS_BraveChallengeReset_VALUE)
public class BraveChallengeResetHandler extends AbstractBaseHandler<CS_BraveChallengeReset> {

	@Override
	protected CS_BraveChallengeReset parse(byte[] bytes) throws Exception {
		return CS_BraveChallengeReset.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_BraveChallengeReset req, int i) {
		String playerId = String.valueOf(gsChn.getPlayerId1());
		SC_BraveChallengeReset.Builder resultBuilder = SC_BraveChallengeReset.newBuilder();
		if (PlayerUtil.queryFunctionLock(playerId, EnumFunction.CourageTrial)) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
			gsChn.send(MsgIdEnum.SC_BraveChallengeReset_VALUE, resultBuilder);
			return;
		}

		bravechallengeEntity entity = bravechallengeCache.getInstance().getEntityByPlayer(playerId);
		if (entity == null) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
			gsChn.send(MsgIdEnum.SC_BraveChallengeReset_VALUE, resultBuilder);
			return;
		}

		
		Consume consume = ConsumeUtil.parseConsume(GameConfig.getById(GameConst.CONFIG_ID).getBravechallengereset());
		Reason borrowReason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_BraveReset);
		if(!ConsumeManager.getInstance().consumeMaterial(playerId, consume, borrowReason)) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Itembag_ItemNotEnought));
			gsChn.send(MsgIdEnum.SC_BraveChallengeReset_VALUE, resultBuilder);
			return;
		}
		SyncExecuteFunction.executeConsumer(entity, e -> {
			entity.getProgressBuilder().clear();
			ChallengeProgress clientProgress = entity.getClientProgress();
			if (clientProgress == null) {
				resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
				gsChn.send(MsgIdEnum.SC_BraveChallengeReset_VALUE, resultBuilder);
				return;
			}
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
			resultBuilder.setProgressMsg(clientProgress);
			gsChn.send(MsgIdEnum.SC_BraveChallengeReset_VALUE, resultBuilder);
			entity.getProgressBuilder().setNewGame(false);
		});
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.CourageTrial;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

	}
}
