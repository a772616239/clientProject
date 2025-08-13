package server.handler.feats;

import cfg.FeatsRewardConfig;
import cfg.FeatsRewardConfigObject;
import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem;
import protocol.TargetSystem.SC_ClaimFeatsReward;
import protocol.TargetSystemDB;
import protocol.TargetSystemDB.DB_Feats;
import util.GameUtil;

/**
 * @Description
 * @Author hanx @Date2020/5/12 0012 9:27
 **/
@MsgId(msgId = MessageId.MsgIdEnum.CS_ClaimFeatsReward_VALUE)
public class ClaimFeatsRewardHandler extends AbstractBaseHandler<TargetSystem.CS_ClaimFeatsReward> {
	@Override
	protected TargetSystem.CS_ClaimFeatsReward parse(byte[] bytes) throws Exception {
		return TargetSystem.CS_ClaimFeatsReward.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, TargetSystem.CS_ClaimFeatsReward req, int i) {
		String playerIdx = String.valueOf(gsChn.getPlayerId1());
		TargetSystem.SC_ClaimFeatsReward.Builder resultBuilder = TargetSystem.SC_ClaimFeatsReward.newBuilder();
		targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
		if (target == null) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
			gsChn.send(MessageId.MsgIdEnum.SC_ClaimFeatsReward_VALUE, resultBuilder);
			return;
		}
		itembagEntity bag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
		if (bag == null) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
			gsChn.send(MessageId.MsgIdEnum.SC_ClaimFeatsReward_VALUE, resultBuilder);
			return;
		}

		FeatsRewardConfigObject config = FeatsRewardConfig.getById(req.getCfgId());
		if (config == null) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
			gsChn.send(MessageId.MsgIdEnum.SC_ClaimFeatsReward_VALUE, resultBuilder);
			return;
		}
		int id = target.getGongXunId(config.getType());
		if (id == -1) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ConfigError));
			gsChn.send(MessageId.MsgIdEnum.SC_ClaimFeatsReward_VALUE, resultBuilder);
			return;
		}
		SyncExecuteFunction.executeConsumer(target, entity -> {
			DB_Feats db_Feats = target.getDb_Builder().getFeatsInfosMap().get(config.getType());
			TargetSystemDB.DB_Feats.Builder featsInfoBuilder = null;
			if (db_Feats == null) {
				featsInfoBuilder = DB_Feats.newBuilder();
			} else {
				featsInfoBuilder = db_Feats.toBuilder();
			}
			int currentFeats = (int) bag.getItemCount(id);
			if (!featsEnough(currentFeats, config)) {
				resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Feats_NotEnough));
				gsChn.send(MessageId.MsgIdEnum.SC_ClaimFeatsReward_VALUE, resultBuilder);
				return;
			}

			boolean canClaimBasicReward = !featsInfoBuilder.getClaimedBasicRewardList().contains(req.getCfgId());
			boolean canClaimAdvanceReward = !featsInfoBuilder.getClaimedAdvanceRewardList().contains(req.getCfgId()) && featsInfoBuilder.getFeatsType() == 1;

			if (!canClaimAdvanceReward && !canClaimBasicReward) {
				resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Feats_RewardAlreadyClaim));
				gsChn.send(MessageId.MsgIdEnum.SC_ClaimFeatsReward_VALUE, resultBuilder);
				return;
			}
			List<Common.Reward> rewards = getFeatsRewards(config, canClaimBasicReward, canClaimAdvanceReward);

			updateCache(req, featsInfoBuilder, canClaimBasicReward, canClaimAdvanceReward);
			// 功勋奖励
			RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FeatsReward), true);

			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
			target.getDb_Builder().putFeatsInfos(config.getType(), featsInfoBuilder.build());
			target.sendFeats();
			gsChn.send(MessageId.MsgIdEnum.SC_ClaimFeatsReward_VALUE, resultBuilder);
		});
	}



	private void updateCache(TargetSystem.CS_ClaimFeatsReward req, TargetSystemDB.DB_Feats.Builder featsInfoBuilder, boolean claimBasicReward, boolean claimAdvanceReward) {
		if (claimAdvanceReward) {
			featsInfoBuilder.addClaimedAdvanceReward(req.getCfgId());
		}
		if (claimBasicReward) {
			featsInfoBuilder.addClaimedBasicReward(req.getCfgId());
		}
	}

	private List<Common.Reward> getFeatsRewards(FeatsRewardConfigObject config, boolean claimBasicReward, boolean claimAdvanceReward) {
		List<Common.Reward> basicRewards = null;
		List<Common.Reward> advancedRewards = null;
		if (claimBasicReward) {
			basicRewards = RewardUtil.getRewardsByRewardId(config.getBasicreward());
		}
		if (claimAdvanceReward) {
			advancedRewards = RewardUtil.getRewardsByRewardId(config.getAdvancedreward());
		}
		return RewardUtil.mergeRewardList(basicRewards, advancedRewards);
	}

	private boolean featsEnough(int currentFeats, FeatsRewardConfigObject config) {
		return config.getFeatsneed() <= currentFeats;
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Feats;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_ClaimFeatsReward_VALUE, SC_ClaimFeatsReward.newBuilder().setRetCode(retCode));
	}
}
