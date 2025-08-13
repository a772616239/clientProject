package server.handler.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import cfg.ItemCard;
import cfg.ItemCardObject;
import cfg.Mission;
import cfg.MissionObject;
import cfg.PetBaseProperties;
import cfg.PetFragmentConfig;
import cfg.PetRuneProperties;
import cfg.RuneTreasure;
import cfg.TimeLimitActivity;
import cfg.TimeLimitActivityTask;
import cfg.TimeLimitActivityTaskObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil.LocalActivityId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.pet.dbCache.petCache;
import model.petrune.dbCache.petruneCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.PayActivityLog;
import platform.logs.entity.PayActivityLog.PayActivityEnum;
import protocol.Activity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.Addition;
import protocol.Activity.ApposeAddition;
import protocol.Activity.CS_ClaimActivityReward;
import protocol.Activity.ExSlotCondition;
import protocol.Activity.ExchangeSlot;
import protocol.Activity.ItemCardData;
import protocol.Activity.SC_ClaimActivityReward;
import protocol.Activity.SC_RefreshItemCard;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Rune;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server;
import protocol.Server.ServerActivity;
import protocol.Server.ServerBuyMission;
import protocol.Server.ServerExMission;
import protocol.Server.ServerSubMission;
import protocol.TargetSystem.TargetMission;
import protocol.TargetSystem.TargetMission.Builder;
import protocol.TargetSystemDB;
import protocol.TargetSystemDB.DB_HadesActivityInfo;
import protocol.TargetSystemDB.DB_ItemCard;
import protocol.TargetSystemDB.DB_RuneTreasureInfo;
import protocol.TargetSystemDB.DB_TimeLimitActivity;
import util.ArrayUtil;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimActivityReward_VALUE)
public class ClaimActivityRewardHandler extends AbstractBaseHandler<CS_ClaimActivityReward> {
	@Override
	protected CS_ClaimActivityReward parse(byte[] bytes) throws Exception {
		return CS_ClaimActivityReward.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, CS_ClaimActivityReward req, int i) {
		String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

		LogUtil.info("receive player:{} claim activity reward,req:{}", playerIdx, req);
		targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
		SC_ClaimActivityReward.Builder resultBuilder = SC_ClaimActivityReward.newBuilder();

		if (target == null) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
			gsChn.send(MsgIdEnum.SC_ClaimActivityReward_VALUE, resultBuilder);
			return;
		}

		// 新手限时任务
		if (req.getActivityId() == LocalActivityId.TIME_LIMIT_MAIN_LINE || req.getActivityId() == LocalActivityId.TIME_LIMIT_ENDLESS_SPIRE || req.getActivityId() == LocalActivityId.TIME_LIMIT_ARENA || req.getActivityId() == LocalActivityId.CumuRecharge || req.getActivityId() == LocalActivityId.SEVEN_DAYS_SIGN_IN) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(claimTimeLimitActivityReward(target, req.getIndex(), req.getActivityId())));
			gsChn.send(MsgIdEnum.SC_ClaimActivityReward_VALUE, resultBuilder);
			return;
		}

		// 累计在线活动
		if (LocalActivityId.DailyOnline == req.getActivityId() || LocalActivityId.CumuOnline == req.getActivityId()) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(claimOnlineTimeActivityReward(target, req.getIndex(), req.getActivityId())));
			gsChn.send(MsgIdEnum.SC_ClaimActivityReward_VALUE, resultBuilder);
			return;
		}

		ServerActivity activityCfgById = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
		if (activityCfgById == null) {
			resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
			gsChn.send(MsgIdEnum.SC_ClaimActivityReward_VALUE, resultBuilder);
			return;
		}

		RetCodeEnum retCode;
		if (buyItemActivity(activityCfgById)) {
			retCode = claimBuyReward(playerIdx, activityCfgById, target, req.getIndex(), req.getBuyCount());
		} else if (apocalypseBlessingActivity(activityCfgById)) {
			retCode = claimApocalypseBlessReward(playerIdx, activityCfgById, target, req.getIndex());
		} else if (activityCfgById.getType() == ActivityTypeEnum.ATE_General) {
			retCode = claimGeneralReward(playerIdx, activityCfgById, target, req.getIndex());
		} else if (activityCfgById.getType() == ActivityTypeEnum.ATE_Exchange) {
			retCode = claimExchangeReward(playerIdx, activityCfgById, target, req.getIndex(), req.getConditionList(), req.getBuyCount());
		} else if (activityCfgById.getType() == ActivityTypeEnum.ATE_DemonDescends) {
			retCode = claimDemonDescendsReward(target, req.getActivityId(), req.getIndex());
		} else if (activityCfgById.getType() == ActivityTypeEnum.ATE_HadesTreasure) {
			retCode = claimHadesMissionReward(target, req.getActivityId(), req.getIndex());
		} else if (activityCfgById.getType() == ActivityTypeEnum.ATE_RuneTreasure) {
			retCode = claimRuneTreasureReward(target, req.getActivityId(), req.getIndex());
		} else if (activityCfgById.getType() == ActivityTypeEnum.ATE_MistGhostBuster) {
			retCode = claimGhostBusterMissionReward(playerIdx, activityCfgById, target, req.getIndex());
		} else if (activityCfgById.getType() == ActivityTypeEnum.ATE_ItemCard) {
			retCode = claimItemCard(playerIdx, activityCfgById, target, req.getIndex());
		} else {
			retCode = RetCodeEnum.RCE_ErrorParam;
		}
		resultBuilder.setRetCode(GameUtil.buildRetCode(retCode));
		gsChn.send(MsgIdEnum.SC_ClaimActivityReward_VALUE, resultBuilder);
	}

	private RetCodeEnum claimGhostBusterMissionReward(String playerIdx, ServerActivity serverActivity, targetsystemEntity target, int index) {
		List<Reward> all = new ArrayList<>();
		playerEntity player = playerCache.getByIdx(target.getLinkplayeridx());
		if (player == null) {
			return RetCodeEnum.RCE_UnknownError;
		}
		if (player.alreadyClaimed(serverActivity.getActivityId(), index)) {
			return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
		}
		RetCodeEnum code = SyncExecuteFunction.executeFunction(target, t -> {
			ServerSubMission subMissionsCfg = serverActivity.getMissionsMap().get(index);
			if (subMissionsCfg == null) {
				return RetCodeEnum.RCE_ErrorParam;
			}
			for (TargetMission.Builder missionPro : t.getDb_Builder().getSpecialInfoBuilder().getGhostActivityMissionBuilder().getMissionProBuilderList()) {
				if (missionPro.getCfgId() == index) {
					if (missionPro.getStatus() != MissionStatusEnum.MSE_Finished) {
						return RetCodeEnum.RCE_Activity_MissionCanNotClaim;
					}
					if (subMissionsCfg.getRewardCount() > 0) {
						all.addAll(subMissionsCfg.getRewardList());
					}
					if (subMissionsCfg.getRandomTimes() > 0 && subMissionsCfg.getRandomsCount() > 0) {
						all.addAll(RewardUtil.drawMustRandomReward(subMissionsCfg.getRandomsList(), subMissionsCfg.getRandomTimes()));
					}
					missionPro.setStatus(MissionStatusEnum.MSE_FinishedAndClaim);

					t.sendRefreshActivityMission(t.buildRefreshMission(serverActivity.getActivityId(), missionPro.build()));
					return RetCodeEnum.RCE_Success;
				}
			}
			return RetCodeEnum.RCE_ErrorParam;
		});

		if (code == RetCodeEnum.RCE_Success && !all.isEmpty()) {
			player.increasePlayerRewardRecord(serverActivity.getActivityId(), index);
			return RewardManager.getInstance().doRewardByList(playerIdx, all, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Activity), true) ? RetCodeEnum.RCE_Success : RetCodeEnum.RCE_UnknownError;
		}
		return code;
	}

	private RetCodeEnum claimItemCard(String playerIdx, ServerActivity serverActivity, targetsystemEntity target, int index) {
		playerEntity player = playerCache.getByIdx(target.getLinkplayeridx());
		if (player == null) {
			return RetCodeEnum.RCE_UnknownError;
		}
		ItemCardObject itemCardCfg = ItemCard.getById(index);
		if (itemCardCfg == null) {
			return RetCodeEnum.RCE_ErrorParam;
		}
		SC_RefreshItemCard.Builder msg = SC_RefreshItemCard.newBuilder();

		RetCodeEnum code = SyncExecuteFunction.executeFunction(target, t -> {
			Map<Integer, DB_ItemCard> itemCardMap = target.getDb_Builder().getItemCardMap();
			if (itemCardMap.containsKey(index)) {// 已购买
				DB_ItemCard db_ItemCard = itemCardMap.get(index);
				if (db_ItemCard.getToday() != 0) {
					return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
				}
				DB_ItemCard.Builder toBuilder = db_ItemCard.toBuilder().setToday(1);
				Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ItemCard_day);
				RewardManager.getInstance().doRewardByRewardId(playerIdx, itemCardCfg.getReward(), reason, true);
				target.getDb_Builder().putItemCard(index, toBuilder.build());
				return RetCodeEnum.RCE_Success;
			} else {
				return RetCodeEnum.RCE_Failure;
			}
		});
		for (Entry<Integer, DB_ItemCard> ent : target.getDb_Builder().getItemCardMap().entrySet()) {
			ItemCardData.Builder b = ItemCardData.newBuilder();
			
//			b.setHave(ent.getValue().getHave());
			b.setIndex(ent.getKey());
			b.setToday(ent.getValue().getToday());
			msg.addItemCard(b);
		}
		GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RefreshItemCard_VALUE, msg);
		return code;
	}

	private RetCodeEnum claimApocalypseBlessReward(String playerIdx, ServerActivity serverActivity, targetsystemEntity target, int index) {
		List<Reward> all = new ArrayList<>();
		playerEntity player = playerCache.getByIdx(target.getLinkplayeridx());
		if (player == null) {
			return RetCodeEnum.RCE_UnknownError;
		}
		if (player.alreadyClaimed(serverActivity.getActivityId(), index)) {
			return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
		}
		RetCodeEnum code = SyncExecuteFunction.executeFunction(target, t -> {
			ServerSubMission subMissionsCfg = serverActivity.getMissionsMap().get(index);
			if (subMissionsCfg == null) {
				return RetCodeEnum.RCE_ErrorParam;
			}
			TargetMission dbActivityPro = target.getDb_Builder().getSpecialInfo().getBless().getMissionProMap().get(index);
			if (dbActivityPro == null) {
				return RetCodeEnum.RCE_ErrorParam;
			}
			if (dbActivityPro.getStatus() != MissionStatusEnum.MSE_Finished) {
				return RetCodeEnum.RCE_Activity_MissionCanNotClaim;
			}

			if (subMissionsCfg.getRewardCount() > 0) {
				all.addAll(subMissionsCfg.getRewardList());
			}
			if (subMissionsCfg.getRandomTimes() > 0 && subMissionsCfg.getRandomsCount() > 0) {
				all.addAll(RewardUtil.drawMustRandomReward(subMissionsCfg.getRandomsList(), subMissionsCfg.getRandomTimes()));
			}

			TargetMission newTarget = dbActivityPro.toBuilder().setStatus(MissionStatusEnum.MSE_FinishedAndClaim).build();
			target.getDb_Builder().getSpecialInfoBuilder().getBlessBuilder().putMissionPro(newTarget.getCfgId(), newTarget);

			target.sendRefreshActivityMission(target.buildRefreshMission(serverActivity.getActivityId(), newTarget));
			return RetCodeEnum.RCE_Success;
		});

		if (code == RetCodeEnum.RCE_Success && !all.isEmpty()) {
			player.increasePlayerRewardRecord(serverActivity.getActivityId(), index);
			return RewardManager.getInstance().doRewardByList(playerIdx, all, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Activity), true) ? RetCodeEnum.RCE_Success : RetCodeEnum.RCE_UnknownError;
		}

		return code;
	}

	private boolean apocalypseBlessingActivity(ServerActivity activityCfgById) {
		return Activity.GeneralActivityTemplate.GAT_ApocalypseBlessing == activityCfgById.getTemplate();
	}

	private RetCodeEnum claimRuneTreasureReward(targetsystemEntity target, long activityId, int index) {
		if (target == null || !ArrayUtil.intArrayContain(RuneTreasure.getById(GameConst.CONFIG_ID).getDailymission(), index)) {
			return RetCodeEnum.RCE_ErrorParam;
		}

		MissionObject missionCfg = Mission.getById(index);
		if (missionCfg == null) {
			return RetCodeEnum.RCE_ErrorParam;
		}
		playerEntity player = playerCache.getByIdx(target.getLinkplayeridx());
		if (player == null) {
			return RetCodeEnum.RCE_UnknownError;
		}
		if (player.alreadyClaimed(activityId, index)) {
			return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
		}

		RetCodeEnum codeEnum = SyncExecuteFunction.executeFunction(target, e -> {
			DB_RuneTreasureInfo.Builder infoBuilder = target.getDbRuneTreasureInfoBuilder(activityId);
			TargetMission targetMission = infoBuilder.getDailyMissionProMap().get(index);
			RetCodeEnum retCode = missionCanClaim(targetMission);
			if (retCode != RetCodeEnum.RCE_Success) {
				return retCode;
			}

			List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward());
			if (CollectionUtils.isEmpty(rewards)) {
				LogUtil.error("server.handler.activity.ClaimActivityRewardHandler.claimRuneTreasureReward, can not get mission rewards, mission cfg id:" + missionCfg.getId());
				return RetCodeEnum.RCE_ConfigError;
			}

			Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_RuneTreasure);
			RewardManager.getInstance().doRewardByList(target.getLinkplayeridx(), rewards, reason, true);

			TargetMission newStatus = targetMission.toBuilder().setStatus(MissionStatusEnum.MSE_FinishedAndClaim).build();
			infoBuilder.putDailyMissionPro(newStatus.getCfgId(), newStatus);
			target.putRuneTreasureInfoBuilder(infoBuilder);

			return RetCodeEnum.RCE_Success;
		});

		if (codeEnum == RetCodeEnum.RCE_Success) {
			player.increasePlayerRewardRecord(activityId, index);
		}
		return codeEnum;

	}

	private RetCodeEnum claimHadesMissionReward(targetsystemEntity entity, long activityId, int index) {
		if (entity == null) {
			return RetCodeEnum.RCE_UnknownError;
		}

		playerEntity player = playerCache.getByIdx(entity.getLinkplayeridx());
		if (player == null) {
			return RetCodeEnum.RCE_UnknownError;
		}
		if (player.alreadyClaimed(activityId, index)) {
			return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
		}
		RetCodeEnum codeEnum = SyncExecuteFunction.executeFunction(entity, e -> {
			DB_HadesActivityInfo.Builder infoBuilder = entity.getHadesActivityInfoBuilder(activityId);
			TargetMission missionPro = infoBuilder.getMissionStatusMap().get(index);
			if (missionPro == null || missionPro.getStatus() == MissionStatusEnum.MSE_UnFinished) {
				return RetCodeEnum.RCE_Activity_MissionCanNotClaim;
			}

			if (missionPro.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
				return RetCodeEnum.RCE_Target_MissionAlreadyClaim;
			}

			infoBuilder.setRemainTimes(infoBuilder.getRemainTimes() + 1);
			TargetMission newStatus = missionPro.toBuilder().setStatus(MissionStatusEnum.MSE_FinishedAndClaim).build();
			infoBuilder.putMissionStatus(newStatus.getCfgId(), newStatus);

			entity.putHadesActivityInfoBuilder(infoBuilder);
			entity.sendHadesActivityInfo(activityId);

			return RetCodeEnum.RCE_Success;
		});
		if (codeEnum == RetCodeEnum.RCE_Success) {
			player.increasePlayerRewardRecord(activityId, index);
		}
		return codeEnum;
	}

	private RetCodeEnum claimDemonDescendsReward(targetsystemEntity entity, long activityId, int index) {
		if (entity == null) {
			return RetCodeEnum.RCE_UnknownError;
		}

		MissionObject missionCfg = Mission.getById(index);
		if (missionCfg == null) {
			return RetCodeEnum.RCE_ErrorParam;
		}
		playerEntity player = playerCache.getByIdx(entity.getLinkplayeridx());
		if (player == null) {
			return RetCodeEnum.RCE_UnknownError;
		}
		if (player.alreadyClaimed(activityId, index)) {
			return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
		}

		RetCodeEnum codeEnum = SyncExecuteFunction.executeFunction(entity, e -> {
			TargetMission missionPro = entity.getDemonDescendsMissionPro(activityId, index);
			RetCodeEnum retCode = missionCanClaim(missionPro);
			if (retCode != RetCodeEnum.RCE_Success) {
				return retCode;
			}

			List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward());
			if (CollectionUtils.isEmpty(rewards)) {
				LogUtil.error("server.handler.activity.ClaimActivityRewardHandler.claimDemonDescendsReward, can not get mission rewards, mission cfg id:" + missionCfg.getId());
				return RetCodeEnum.RCE_ConfigError;
			}

			Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DemonDescends_DailyMission);
			RewardManager.getInstance().doRewardByList(entity.getLinkplayeridx(), rewards, reason, true);

			// 修改任务状态
			TargetMission newStatus = missionPro.toBuilder().setStatus(MissionStatusEnum.MSE_FinishedAndClaim).build();

			entity.putDemonDescendsMissionPro(activityId, newStatus);

			entity.refreshDemonDescendsActivityInfo(activityId);
			return RetCodeEnum.RCE_Success;
		});
		if (codeEnum == RetCodeEnum.RCE_Success) {
			player.increasePlayerRewardRecord(activityId, index);
		}
		return codeEnum;

	}

	private boolean buyItemActivity(ServerActivity activityCfgById) {
		ActivityTypeEnum type = activityCfgById.getType();
		return type == ActivityTypeEnum.ATE_DailyGift || type == ActivityTypeEnum.ATE_WeeklyGift || type == ActivityTypeEnum.ATE_MonthlyGift
//                || type == ActivityTypeEnum.ATE_BestExchange
				|| type == ActivityTypeEnum.ATE_BuyItem || type == ActivityTypeEnum.ATE_RichMan||type==ActivityTypeEnum.ATE_FestivalBoss;
	}

	private RetCodeEnum claimBuyReward(String playerIdx, ServerActivity activity, targetsystemEntity target, int index, int buyCount) {
		long activityId = activity.getActivityId();
		if (!ActivityManager.activityIsOpen(activityId, playerIdx)) {
			return RetCodeEnum.RCE_Activity_NotOpen;
		}
		if (!checkRichManBuy(activity, target, index)) {
			return RetCodeEnum.RCE_ErrorParam;
		}
		ServerBuyMission missionCfg = activity.getBuyMissionMap().get(index);
		if (missionCfg == null || buyCount <= 0) {
			return RetCodeEnum.RCE_ErrorParam;
		}

		if (missionCfg.getEndTimestamp() != -1 && GlobalTick.getInstance().getCurrentTime() > missionCfg.getEndTimestamp()) {
			return RetCodeEnum.RCE_Activity_MissionOutOfTime;
		}
		playerEntity player = playerCache.getByIdx(playerIdx);
		if (player == null) {
			return RetCodeEnum.RCE_UnknownError;
		}

		RetCodeEnum codeEnum = SyncExecuteFunction.executeFunction(target, e -> {
			TargetSystemDB.DB_Activity.Builder db = target.getDBActivityBuilder(activityId);
			TargetMission mission = db.getMissionProMap().get(index);
			int claimedCount = mission == null ? 0 : mission.getProgress();
			if ((claimedCount + buyCount) > missionCfg.getLimitBuy() || !player.canClaim(activityId, index, buyCount, missionCfg.getLimitBuy())) {
				return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
			}
			RewardSourceEnum sourceEnum = getRewardSourceEnum(activityId,activity.getType());

			ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(sourceEnum,"商品购买", buyCount);

			Consume consume = ConsumeUtil.multiConsume(missionCfg.getPrice(), buyCount);
			if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
				return RetCodeEnum.RCE_Player_CurrencysNotEnought;
			}

			List<Reward> rewards = RewardUtil.multiReward(missionCfg.getRewardsList(), buyCount);
			RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);

			TargetMission newMission = TargetMission.newBuilder().setCfgId(index).setProgress(claimedCount + buyCount).build();

			db.putMissionPro(index, newMission);
			target.getDb_Builder().putActivities(activityId, db.build());
			target.sendRefreshActivityMission(target.buildRefreshMission(activityId, newMission));
			addPayActivityLog(playerIdx, activity, missionCfg.getPrice());
			return RetCodeEnum.RCE_Success;
		});
		if (codeEnum == RetCodeEnum.RCE_Success) {
			player.addPlayerRewardRecord(activityId, index, buyCount);
		}
		return codeEnum;
	}

	private boolean checkRichManBuy(ServerActivity activity, targetsystemEntity target, int index) {
		if (ActivityTypeEnum.ATE_RichMan != activity.getType()) {
			return true;
		}
		int curPoint = target.getDb_Builder().getSpecialInfo().getRichMan().getCurPoint();
		Server.ServerRichManPoint point = activity.getRichManPointMap().get(curPoint);
		if (point == null || CollectionUtils.isEmpty(point.getBuyItemList())) {
			return false;
		}
		return point.getBuyItemList().stream().anyMatch(item -> item.getIndex() == index);
	}

	private void addPayActivityLog(String playerIdx, ServerActivity activity, Consume consume) {
		if (consume != null && consume.getCount() > 0 && ActivityTypeEnum.ATE_DailyGift == activity.getType()) {
			LogService.getInstance().submit(new PayActivityLog(playerIdx, consume, PayActivityEnum.DailyGift));
		}
		if (consume != null && consume.getCount() > 0 && ActivityTypeEnum.ATE_WeeklyGift == activity.getType()) {
			LogService.getInstance().submit(new PayActivityLog(playerIdx, consume, PayActivityEnum.WeeklyGift));
		}
		if (consume != null && consume.getCount() > 0 && ActivityTypeEnum.ATE_MonthlyGift == activity.getType()) {
			LogService.getInstance().submit(new PayActivityLog(playerIdx, consume, PayActivityEnum.MonthlyGift));
		}
	}

	private RewardSourceEnum getRewardSourceEnum(long activityId, ActivityTypeEnum type) {
		if (activityId == LocalActivityId.DailyGift) {
			return RewardSourceEnum.RSE_DailyGift;
		} else if (activityId == LocalActivityId.WeeklyGift) {
			return RewardSourceEnum.RSE_WeeklyGift;
		} else if (activityId == LocalActivityId.MonthlyGift) {
			return RewardSourceEnum.RSE_MonthlyGift;
		} else if (type == ActivityTypeEnum.ATE_FestivalBoss) {
			return RewardSourceEnum.RSE_MonthlyGift;
		} else if (type == ActivityTypeEnum.ATE_RichMan) {
			return RewardSourceEnum.RSE_RichMan;
		}
		return RewardSourceEnum.RSE_BuyItem_Activity;
	}

	/**
	 * 新手限时任务
	 *
	 * @param target
	 * @param index
	 * @param activityId
	 * @return
	 */
	private RetCodeEnum claimTimeLimitActivityReward(targetsystemEntity target, int index, long activityId) {

		playerEntity player = playerCache.getByIdx(target.getLinkplayeridx());
		if (player == null) {
			return RetCodeEnum.RCE_UnknownError;
		}
		if (player.alreadyClaimed(activityId, index)) {
			return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
		}

		RetCodeEnum codeEnum = SyncExecuteFunction.executeFunction(target, t -> {
			TimeLimitActivityTaskObject taskCfg = TimeLimitActivityTask.getById(index);
			if (taskCfg == null) {
				return RetCodeEnum.RCE_Target_MissionCfgIdNotExist;
			}

			DB_TimeLimitActivity.Builder challengeBuilder = target.getDb_Builder().getSpecialInfoBuilder().getTimeLimitActivitiesBuilder();
			// 领取不限制时间
//            long currentTime = GlobalTick.getInstance().getCurrentTime();
//            if (taskCfg.getEndtime() != -1 && (challengeBuilder.getStartTime() + taskCfg.getEndtime() * TimeUtil.MS_IN_A_DAY) < currentTime) {
//                return RetCodeEnum.RCE_Activity_MissionOutOfTime;
//            }

			TargetMission targetMission = challengeBuilder.getMissionProMap().get(index);
			if (targetMission == null || targetMission.getStatus() == MissionStatusEnum.MSE_UnFinished) {
				return RetCodeEnum.RCE_Target_MissionUnfinished;
			}

			if (targetMission.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
				return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
			}

			TargetMission newMission = targetMission.toBuilder().setStatus(MissionStatusEnum.MSE_FinishedAndClaim).build();
			challengeBuilder.putMissionPro(targetMission.getCfgId(), newMission);
			target.sendRefreshActivityMission(target.buildRefreshMission(TimeLimitActivity.getTaskLinkActivityId(index), newMission));

			RewardManager.getInstance().doRewardByList(target.getLinkplayeridx(), RewardUtil.parseRewardIntArrayToRewardList(taskCfg.getReward()), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_NewBeeChallenge), true);
			return RetCodeEnum.RCE_Success;
		});
		if (codeEnum == RetCodeEnum.RCE_Success) {
			player.increasePlayerRewardRecord(activityId, index);
		}
		return codeEnum;
	}

	/**
	 * 在线活动(累计在线,总在线)
	 *
	 * @param target
	 * @param index
	 * @param activityId
	 * @return
	 */
	private RetCodeEnum claimOnlineTimeActivityReward(targetsystemEntity target, int index, long activityId) {
		playerEntity player = playerCache.getByIdx(target.getLinkplayeridx());
		if (player == null) {
			return RetCodeEnum.RCE_UnknownError;
		}
		if (player.alreadyClaimed(activityId, index)) {
			return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
		}
		RetCodeEnum codeEnum = SyncExecuteFunction.executeFunction(target, t -> {
			if (!ActivityManager.activityIsOpen(activityId, target.getLinkplayeridx())) {
				return RetCodeEnum.RCE_Activity_NotOpen;
			}
			TimeLimitActivityTaskObject taskCfg = TimeLimitActivityTask.getById(index);
			if (taskCfg == null) {
				return RetCodeEnum.RCE_Target_MissionCfgIdNotExist;
			}
			DB_TimeLimitActivity.Builder challengeBuilder = target.getDb_Builder().getSpecialInfoBuilder().getTimeLimitActivitiesBuilder();
			TargetMission targetMission = challengeBuilder.getMissionProMap().get(index);
			if (targetMission != null && targetMission.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
				return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
			}
			int currentTarget = activityId == ActivityTypeEnum.ATE_CumuOnline_VALUE ? player.getCumuOnline() : player.getCurrentOnline();
			if (currentTarget < taskCfg.getTargetcount()) {
				player.sendRefreshOnlineTime(activityId);
				return RetCodeEnum.RCE_Target_MissionUnfinished;
			}

			TargetMission newMission = TargetMission.newBuilder().setCfgId(taskCfg.getId()).setProgress(taskCfg.getTargetcount()).setStatus(MissionStatusEnum.MSE_FinishedAndClaim).build();
			challengeBuilder.putMissionPro(newMission.getCfgId(), newMission);
			target.sendRefreshActivityMission(target.buildRefreshMission(TimeLimitActivity.getTaskLinkActivityId(index), newMission));

			RewardManager.getInstance().doRewardByList(target.getLinkplayeridx(), RewardUtil.parseRewardIntArrayToRewardList(taskCfg.getReward()), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_OnlineTime), true);
			return RetCodeEnum.RCE_Success;
		});
		if (codeEnum == RetCodeEnum.RCE_Success) {
			player.increasePlayerRewardRecord(activityId, index);
		}
		return codeEnum;
	}

	private RetCodeEnum claimGeneralReward(String playerIdx, ServerActivity serverActivity, targetsystemEntity target, int index) {
		List<Reward> all = new ArrayList<>();
		playerEntity player = playerCache.getByIdx(target.getLinkplayeridx());
		if (player == null) {
			return RetCodeEnum.RCE_UnknownError;
		}
		if (player.alreadyClaimed(serverActivity.getActivityId(), index)) {
			return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
		}
		RetCodeEnum code = SyncExecuteFunction.executeFunction(target, t -> {
			ServerSubMission subMissionsCfg = serverActivity.getMissionsMap().get(index);
			if (subMissionsCfg == null) {
				return RetCodeEnum.RCE_ErrorParam;
			}

			TargetMission.Builder dbActivityPro = target.getDBActivityProBuilder(serverActivity.getActivityId(), index);
			if (subMissionsCfg.getEndTimestamp() != -1 && GlobalTick.getInstance().getCurrentTime() > subMissionsCfg.getEndTimestamp()) {
				return RetCodeEnum.RCE_Activity_MissionIsExpire;
			}

			if (dbActivityPro.getStatus() != MissionStatusEnum.MSE_Finished) {
				return RetCodeEnum.RCE_Activity_MissionCanNotClaim;
			}

			if (subMissionsCfg.getRewardCount() > 0) {
				all.addAll(subMissionsCfg.getRewardList());
			}
			if (subMissionsCfg.getRandomTimes() > 0 && subMissionsCfg.getRandomsCount() > 0) {
				all.addAll(RewardUtil.drawMustRandomReward(subMissionsCfg.getRandomsList(), subMissionsCfg.getRandomTimes()));
			}

			dbActivityPro.setStatus(MissionStatusEnum.MSE_FinishedAndClaim);
			target.putDBActivityMissionPro(serverActivity.getActivityId(), dbActivityPro.build());

			target.sendRefreshActivityMission(target.buildRefreshMission(serverActivity.getActivityId(), dbActivityPro.build()));
			return RetCodeEnum.RCE_Success;
		});

		if (code == RetCodeEnum.RCE_Success && !all.isEmpty()) {
			player.increasePlayerRewardRecord(serverActivity.getActivityId(), index);

			return RewardManager.getInstance().doRewardByList(playerIdx, all, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Activity), true) ? RetCodeEnum.RCE_Success : RetCodeEnum.RCE_UnknownError;
		}

		return code;
	}

	private RetCodeEnum claimExchangeReward(String playerIdx, ServerActivity serverActivity, targetsystemEntity target, int index, List<ExSlotCondition> conditionList, int exTimes) {
		ServerExMission exMissionsCfg = serverActivity.getExMissionMap().get(index);
		if (exMissionsCfg == null || exTimes <= 0) {
			return RetCodeEnum.RCE_ErrorParam;
		}
		playerEntity player = playerCache.getByIdx(target.getLinkplayeridx());
		if (player == null) {
			return RetCodeEnum.RCE_UnknownError;
		}
		if (!player.canClaim(serverActivity.getActivityId(), index, exTimes, exMissionsCfg.getExchangeLimit())) {
			return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
		}
		RetCodeEnum success = SyncExecuteFunction.executeFunction(target, t -> {
			Builder dbActivityPro = target.getDBActivityProBuilder(serverActivity.getActivityId(), index);
			if (exMissionsCfg.getEndTimestamp() != -1 && GlobalTick.getInstance().getCurrentTime() > exMissionsCfg.getEndTimestamp()) {
				return RetCodeEnum.RCE_Activity_MissionIsExpire;
			}

			if (dbActivityPro.getStatus() != MissionStatusEnum.MSE_UnFinished) {
				return RetCodeEnum.RCE_Activity_ExTimesLimit;
			}

			if ((dbActivityPro.getProgress() + exTimes) > exMissionsCfg.getExchangeLimit()) {
				return RetCodeEnum.RCE_Activity_ExTimesLimit;
			}
			return RetCodeEnum.RCE_Success;
		});

		if (success != RetCodeEnum.RCE_Success) {
			return success;
		}

		if (!satisfyAddition(playerIdx, exMissionsCfg, conditionList, exTimes)) {
			return RetCodeEnum.RCE_Activity_DissatisfyAddition;
		}

		List<Reward> rewards = RewardUtil.multiReward(exMissionsCfg.getRewardsList(), exTimes);
		RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Activity, exTimes), true);

		SyncExecuteFunction.executeConsumer(target, t -> {
			Builder dbActivityPro = target.getDBActivityProBuilder(serverActivity.getActivityId(), index);
			dbActivityPro.setProgress(dbActivityPro.getProgress() + exTimes);
			if (exMissionsCfg.getExchangeLimit() != -1) {
				if (dbActivityPro.getProgress() > exMissionsCfg.getExchangeLimit()) {
					dbActivityPro.setProgress(exMissionsCfg.getExchangeLimit());
					dbActivityPro.setStatus(MissionStatusEnum.MSE_Finished);
				}
			}
			TargetMission build = dbActivityPro.build();
			target.putDBActivityMissionPro(serverActivity.getActivityId(), build);
		});
		player.addPlayerRewardRecord(serverActivity.getActivityId(), index, exTimes);

		return RetCodeEnum.RCE_Success;
	}

	/**
	 * 判断是否满足兑换条件,并移除对应的兑换物
	 *
	 * @param playerIdx
	 * @param exMissionsCfg
	 * @param conditionList
	 * @return
	 */
	private boolean satisfyAddition(String playerIdx, ServerExMission exMissionsCfg, List<ExSlotCondition> conditionList, int exTimes) {
		if (exMissionsCfg.getExSlotsCount() != conditionList.size()) {
			return false;
		}

		if (exTimes > 1 && canNotMultiExchange(exMissionsCfg)) {
			return false;
		}

		// 用于保存移除的物品
		List<Consume> consumeList = new ArrayList<>();
		final Set<String> petIdxSet = new HashSet<>();
		final Set<String> runeIdxSet = new HashSet<>();

		for (int i = 0; i < exMissionsCfg.getExSlotsCount(); i++) {
			ExchangeSlot exSlots = exMissionsCfg.getExSlots(i);
			ExSlotCondition exSlotCondition = conditionList.get(i);
			if (exSlotCondition == null) {
				return false;
			}

			ApposeAddition apposeAddition = null;
			for (ApposeAddition addition : exSlots.getApposeAdditionList()) {
				if (addition.getIndex() == exSlotCondition.getAdditionIndex()) {
					apposeAddition = addition;
					break;
				}
			}

			if (!satisfy(playerIdx, apposeAddition, exSlotCondition, consumeList, petIdxSet, runeIdxSet)) {
				return false;
			}
		}

		// 多次兑换加倍
		if (exTimes > 1) {
			consumeList = ConsumeUtil.multiConsume(consumeList, exTimes);
		}

		return ConsumeManager.getInstance().consumeMaterial(playerIdx, consumeList, new ArrayList<>(petIdxSet), new ArrayList<>(runeIdxSet), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Activity));
	}

	private static final Set<RewardTypeEnum> UNSUPPORTED_MULTI_EXCHANGE_TYPE;

	static {
		Set<RewardTypeEnum> tempSet = new HashSet<>();

		tempSet.add(RewardTypeEnum.RTE_Pet);
		tempSet.add(RewardTypeEnum.RTE_Rune);

		UNSUPPORTED_MULTI_EXCHANGE_TYPE = Collections.unmodifiableSet(tempSet);
	}

	private static boolean unsupportedMultiExchangeType(RewardTypeEnum rewardType) {
		if (rewardType == null) {
			return true;
		}
		return UNSUPPORTED_MULTI_EXCHANGE_TYPE.contains(rewardType);
	}

	/**
	 * 判断是否支持同时兑换多个, 消耗的材料为宠物和符文时 不支持同时兑换多个
	 *
	 * @return
	 */
	private boolean canMultiExchange(ServerExMission exMissionsCfg) {
		if (exMissionsCfg == null) {
			return false;
		}

		for (ExchangeSlot slot : exMissionsCfg.getExSlotsList()) {
			for (ApposeAddition apposeAddition : slot.getApposeAdditionList()) {
				if (unsupportedMultiExchangeType(apposeAddition.getType())) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean canNotMultiExchange(ServerExMission exMissionsCfg) {
		return !canMultiExchange(exMissionsCfg);
	}

	/**
	 * 是否满足条件
	 *
	 * @param playerIdx
	 * @param apposeAddition
	 * @param exSlotCondition
	 * @param consumeList
	 * @param petIdxSet
	 * @param runeIdxSet
	 * @return
	 */
	private boolean satisfy(String playerIdx, ApposeAddition apposeAddition, ExSlotCondition exSlotCondition, List<Consume> consumeList, Set<String> petIdxSet, Set<String> runeIdxSet) {

		if (playerIdx == null || apposeAddition == null || exSlotCondition == null || consumeList == null || petIdxSet == null || runeIdxSet == null) {
			return false;
		}

		switch (apposeAddition.getType()) {
		case RTE_Diamond:
		case RTE_Gold:
		case RTE_Coupon:
		case RTE_HolyWater:
			consumeList.add(ConsumeUtil.parseConsume(apposeAddition.getTypeValue(), 0, apposeAddition.getCount()));
			break;
		case RTE_Item:
			if (!itemSatisfy(apposeAddition.getAdditionList(), exSlotCondition.getItemId())) {
				return false;
			}
			consumeList.add(ConsumeUtil.parseConsume(apposeAddition.getTypeValue(), exSlotCondition.getItemId(), apposeAddition.getCount()));
			break;
		case RTE_Pet:
			// 使用同一个宠物兑换或者不满足兑换条件
			if (petIdxSet.contains(exSlotCondition.getPetIdx()) || !petSatisfy(playerIdx, apposeAddition.getAdditionList(), exSlotCondition.getPetIdx())) {
				return false;
			}
			petIdxSet.add(exSlotCondition.getPetIdx());
			break;
		case RTE_Rune:
			if (runeIdxSet.contains(exSlotCondition.getRuneIdx()) || !runeSatisfy(playerIdx, apposeAddition.getAdditionList(), exSlotCondition.getRuneIdx())) {
				return false;
			}
			runeIdxSet.add(exSlotCondition.getRuneIdx());
			break;
		case RTE_PetFragment:
			if (!PetFragmentSatisfy(apposeAddition.getAdditionList(), exSlotCondition.getPetFragmentId())) {
				return false;
			}
			consumeList.add(ConsumeUtil.parseConsume(apposeAddition.getTypeValue(), exSlotCondition.getPetFragmentId(), apposeAddition.getCount()));
			break;
		default:
			LogUtil.error("unsupported addition reward type");
			return false;
		}
		return true;
	}

	private boolean PetFragmentSatisfy(List<Addition> additionList, int petFragmentId) {
		if (additionList.isEmpty()) {
			return false;
		}
		for (Addition addition : additionList) {
			switch (addition.getAdditionType()) {
			case AET_Id:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), petFragmentId)) {
					return false;
				}
				break;
			case AET_Quality:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), PetFragmentConfig.getQualityByCfgId(petFragmentId))) {
					return false;
				}
				break;
			default:
				LogUtil.error("unsupported, addition type " + addition.getAdditionType());
				return false;
			}
		}
		return true;
	}

	private boolean itemSatisfy(List<Addition> additionList, int itemId) {
		if (additionList.isEmpty()) {
			return false;
		}
		for (Addition addition : additionList) {
			switch (addition.getAdditionType()) {
			case AET_Id:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), itemId)) {
					return false;
				}
				break;
			default:
				LogUtil.error("unsupported, addition type " + addition.getAdditionType());
				return false;
			}
		}

		return true;
	}

	/**
	 * 满足addition的所有条件
	 *
	 * @param additionList
	 * @return
	 */
	private boolean petSatisfy(String playerIdx, List<Addition> additionList, String petIdx) {
		if (additionList.isEmpty()) {
			return false;
		}

		// 首先判断宠物的图鉴是否满足条件
		Pet pet = model.pet.dbCache.petCache.getInstance().getPetById(playerIdx, petIdx);
		if (pet == null || petCache.petStatusCheck(pet) != RetCodeEnum.RCE_Success) {
			return false;
		}

		for (Addition addition : additionList) {
			switch (addition.getAdditionType()) {
			case AET_Id:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), pet.getPetBookId())) {
					return false;
				}
				break;
			case AET_Quality:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), PetBaseProperties.getQualityByPetId(pet.getPetBookId()))) {
					return false;
				}
				break;
			case AET_PetRace:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), PetBaseProperties.getRaceByPetId(pet.getPetBookId()))) {
					return false;
				}
				break;
			case AET_Level:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), pet.getPetLvl())) {
					return false;
				}
				break;
			case AET_Rarity:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), pet.getPetRarity())) {
					return false;
				}
				break;
			case AET_Awake:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), pet.getPetUpLvl())) {
					return false;
				}
				break;
			case AET_Class:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), PetBaseProperties.getClass(pet.getPetBookId()))) {
					return false;
				}
				break;
			default:
				LogUtil.error("unsupported additionType = " + addition.getAdditionType());
				break;
			}
		}
		return true;
	}

	private boolean runeSatisfy(String playerIdx, List<Addition> additionList, String runeIdx) {
		if (additionList.isEmpty()) {
			return false;
		}
		Rune rune = petruneCache.getInstance().getPlayerRune(playerIdx, runeIdx);
		if (rune == null || !petruneCache.runeCanRemove(playerIdx, rune)) {
			return false;
		}

		for (Addition addition : additionList) {
			switch (addition.getAdditionType()) {
			case AET_Id:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), rune.getRuneBookId())) {
					return false;
				}
				break;
			case AET_Quality:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), PetRuneProperties.getByRuneid(rune.getRuneBookId()).getRunerarity())) {
					return false;
				}
				break;
			case AET_Level:
				if (GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), rune.getRuneLvl())) {
					return false;
				}
				break;
			default:
				LogUtil.error("unsupported additionType = " + addition.getAdditionType());
				break;
			}
		}
		return true;
	}

	/**
	 * 检查对应的IdxList是否为空,是否含有重复的idx,size是否等于需要的count
	 *
	 * @return
	 */
	public static boolean checkIdxList(List<String> list, int needCount) {
		if (list == null || needCount <= 0) {
			return false;
		}

		Set<String> set = new HashSet<>(list);
		if (set.size() != list.size() || set.size() != needCount) {
			return false;
		}
		return true;
	}

	private RetCodeEnum missionCanClaim(TargetMission mission) {
		if (mission == null || mission.getStatus() == MissionStatusEnum.MSE_UnFinished) {
			return RetCodeEnum.RCE_Activity_MissionCanNotClaim;
		}

		if (mission.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
			return RetCodeEnum.RCE_Target_MissionAlreadyClaim;
		}
		return RetCodeEnum.RCE_Success;
	}

	@Override
	public EnumFunction belongFunction() {
		return EnumFunction.Activity;
	}

	@Override
	public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
		RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
		gsChn.send(MsgIdEnum.SC_ClaimActivityReward_VALUE, SC_ClaimActivityReward.newBuilder().setRetCode(retCode));
	}
}
