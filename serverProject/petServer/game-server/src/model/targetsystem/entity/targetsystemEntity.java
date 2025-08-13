/**
 * created by tool DAOGenerate
 */
package model.targetsystem.entity;

import cfg.Achievement;
import cfg.AchievementObject;
import cfg.ArenaConfig;
import cfg.ArenaConfigObject;
import cfg.CrossArenaCfg;
import cfg.CrossArenaCfgObject;
import cfg.DailyMission;
import cfg.DailyMissionObject;
import cfg.DemonDescendsConfig;
import cfg.DemonDescendsConfigObject;
import cfg.GameConfig;
import cfg.GameConfigObject;
import cfg.GrowthTrack;
import cfg.HadesConfig;
import cfg.MailTemplateUsed;
import cfg.MatchArenaLT;
import cfg.MatchArenaLTObject;
import cfg.Mission;
import cfg.MissionObject;
import cfg.MistNewbieTaskConfig;
import cfg.MistNewbieTaskConfigObject;
import cfg.MistSeasonMission;
import cfg.MistSeasonMissionObject;
import cfg.MistWordMapInfoConfig;
import cfg.MistWordMapInfoConfigObject;
import cfg.MistWorldMapConfig;
import cfg.MistWorldMapConfigObject;
import cfg.NeeBeeGiftActivityCfg;
import cfg.NeeBeeGiftActivityCfgObject;
import cfg.NewBeeGiftCfg;
import cfg.NewBeeGiftCfgObject;
import cfg.NoviceTask;
import cfg.NoviceTaskObject;
import cfg.PopupMission;
import cfg.PopupMissionObject;
import cfg.RuneTreasure;
import cfg.ServerStringRes;
import cfg.TimeLimitActivity;
import cfg.TimeLimitActivityObject;
import cfg.TimeLimitActivityTask;
import cfg.TimeLimitActivityTaskObject;
import cfg.TimeLimitGiftConfig;
import cfg.TimeLimitGiftConfigObject;
import cfg.TrainingMap;
import cfg.TrainingMapObject;
import cfg.WishWellActivityConfig;
import cfg.WishWellActivityConfigObject;
import cfg.WishWellConfig;
import cfg.WishWellConfigObject;
import common.GameConst;
import common.GlobalData;
import common.IdGenerator;
import common.tick.GlobalTick;
import entity.UpdateActivityDropCount;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.activity.ActivityUtil.LocalActivityId;
import model.activity.DailyFirstRechargeManage;
import model.activity.PointCopyManager;
import model.consume.ConsumeManager;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.mission.MissionManager;
import model.mistforest.MistConst;
import model.mistforest.MistForestManager;
import model.mistforest.MistTimeLimitMissionManager;
import model.obj.BaseObj;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.shop.StoreManager;
import model.stoneRift.StoneRiftCfgManager;
import model.targetsystem.TargetSystemUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.thewar.TheWarManager;
import model.training.dbCache.trainingCache;
import model.training.entity.trainingEntity;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.MultiValueMap;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.DailyDateLog;
import platform.purchase.DirectGiftPurchaseHandler;
import protocol.Activity;
import protocol.Activity.ActivityTime;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.ClientActivity;
import protocol.Activity.ClientSubBuyItem;
import protocol.Activity.ClientSubExchange;
import protocol.Activity.ClientSubMission;
import protocol.Activity.CycleTypeEnum;
import protocol.Activity.Cycle_Day;
import protocol.Activity.Cycle_TimeLimit;
import protocol.Activity.DayDayRecharge;
import protocol.Activity.DemonDescendsRechargeInfo;
import protocol.Activity.EnumClientActivityTabType;
import protocol.Activity.EnumRankingType;
import protocol.Activity.GeneralActivityTemplate;
import protocol.Activity.ItemCardData;
import protocol.Activity.PayActivityStateEnum;
import protocol.Activity.RechargeType;
import protocol.Activity.RefreshActivity;
import protocol.Activity.RewardList;
import protocol.Activity.SC_ClaimDemonDescendsInfo;
import protocol.Activity.SC_ClaimHadesInfo;
import protocol.Activity.SC_ClaimPointCopyInfo;
import protocol.Activity.SC_NewActivity;
import protocol.Activity.SC_NewBeeGiftUpdate;
import protocol.Activity.SC_PetAvoidanceUpdate;
import protocol.Activity.SC_RefreshActivity;
import protocol.Activity.SC_RefreshItemCard;
import protocol.Activity.SC_RefreshNovicePro;
import protocol.Activity.SC_RefreshPointCopyScore;
import protocol.Activity.SC_UpdateDayDayRechargeInfo;
import protocol.Activity.SC_UpdateRuneTreasureDailyMissionPro;
import protocol.Activity.SC_WishWellUpdate;
import protocol.Activity.WishStateEnum;
import protocol.Activity.WishingWellItem;
import protocol.Arena.SC_RefreshArenaMissionPro;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.LanguageEnum;
import protocol.Common.MissionStatusEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.SC_AddBusinessPopup_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_AddCompleteShopMission_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_BusinessPopupInit_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_BusinessUpdate_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_FestivalBossInfoUpdate_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_TotalActivityGoodsInfo_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_TotalStageRewardInfo_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdateActivityGoodsInfo_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdateRichManInfo_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdateStageRewardInfo_VALUE;
import protocol.MistForest.EnumMistRuleKind;
import protocol.Patrol;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server;
import protocol.Server.ActivityDayDayRecharge;
import protocol.Server.ServerActivity;
import protocol.Server.ServerExMission;
import protocol.Server.ServerSubMission;
import protocol.ServerTransfer.GS_CS_NewbieTaskCreateObj;
import protocol.Shop;
import protocol.TargetSystem;
import protocol.TargetSystem.AchievmentPro;
import protocol.TargetSystem.FeatsInfo;
import protocol.TargetSystem.SC_ClaimMistTimeLimitMission;
import protocol.TargetSystem.SC_ClearDailyMission;
import protocol.TargetSystem.SC_GetFeatsInfo;
import protocol.TargetSystem.SC_MistMazeActivityMission;
import protocol.TargetSystem.SC_NewTimeLimitGift;
import protocol.TargetSystem.SC_RefreashDailyMissionPro;
import protocol.TargetSystem.SC_RefreashMistSeasonMissionPro;
import protocol.TargetSystem.SC_RefreshAchievement;
import protocol.TargetSystem.SC_RefreshGrowTrackProgress;
import protocol.TargetSystem.SC_RefreshMistTimeLimitMissionProgress;
import protocol.TargetSystem.SC_TrainUpdateTask;
import protocol.TargetSystem.SC_UpdateMistNewbieTask;
import protocol.TargetSystem.SC_UpdateMistSweepTask;
import protocol.TargetSystem.SC_UpdateMistTargetMission;
import protocol.TargetSystem.TargetMission;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TargetSystem.TimeLimitGiftType;
import protocol.TargetSystemDB;
import protocol.TargetSystemDB.DB_Activity;
import protocol.TargetSystemDB.DB_ActivityRanking;
import protocol.TargetSystemDB.DB_DayDayRecharge;
import protocol.TargetSystemDB.DB_DemonDescendsActivityInfo;
import protocol.TargetSystemDB.DB_Feats;
import protocol.TargetSystemDB.DB_GrowthTrack;
import protocol.TargetSystemDB.DB_HadesActivityInfo;
import protocol.TargetSystemDB.DB_ItemCard;
import protocol.TargetSystemDB.DB_LotteryCurOdds;
import protocol.TargetSystemDB.DB_MistGhostActivity;
import protocol.TargetSystemDB.DB_MistMazeActivity;
import protocol.TargetSystemDB.DB_MistTimeLimitMission;
import protocol.TargetSystemDB.DB_NeeBeeGift;
import protocol.TargetSystemDB.DB_NoviceCredit;
import protocol.TargetSystemDB.DB_PetAvoidance;
import protocol.TargetSystemDB.DB_PointCopy;
import protocol.TargetSystemDB.DB_RuneTreasureInfo;
import protocol.TargetSystemDB.DB_ScratchLottery;
import protocol.TargetSystemDB.DB_TargetSystem;
import protocol.TargetSystemDB.DB_TargetSystem.Builder;
import protocol.TargetSystemDB.DB_TheWarSeasonMission;
import protocol.TargetSystemDB.DB_TimeLimitActivity;
import protocol.TargetSystemDB.DB_TimeLimitGiftItem;
import protocol.TargetSystemDB.MistSweepTaskDbData;
import protocol.TargetSystemDB.MistUnlockTaskDbData;
import protocol.TargetSystemDB.PayActivityRecord;
import protocol.TargetSystemDB.TrainingMapTaskData;
import util.ArrayUtil;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.MapUtil;
import util.RandomUtil;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static protocol.MessageId.MsgIdEnum.SC_AddBusinessPopup_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_AddCompleteShopMission_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_BusinessPopupInit_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_BusinessUpdate_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_FestivalBossInfoUpdate_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetAvoidanceUpdate_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_TotalActivityGoodsInfo_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_TotalStageRewardInfo_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdateActivityGoodsInfo_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdateRichManInfo_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdateStageRewardInfo_VALUE;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class targetsystemEntity extends BaseObj {

	@Override
	public String getClassType() {
		return "targetsystemEntity";
	}

	/**
	 *
	 */
	private String idx;

	/**
	 *
	 */
	private String linkplayeridx;

	/**
	 *
	 */
	private byte[] targetdata;

	/**
	 * 获得
	 */
	public String getIdx() {
		return idx;
	}

	/**
	 * 设置
	 */
	public void setIdx(String idx) {
		this.idx = idx;
	}

	/**
	 * 获得
	 */
	public String getLinkplayeridx() {
		return linkplayeridx;
	}

	/**
	 * 设置
	 */
	public void setLinkplayeridx(String linkplayeridx) {
		this.linkplayeridx = linkplayeridx;
	}

	/**
	 * 获得
	 */
	public byte[] getTargetdata() {
		return targetdata;
	}

	/**
	 * 设置
	 */
	public void setTargetdata(byte[] targetdata) {
		this.targetdata = targetdata;
	}

	@Override
	public String getBaseIdx() {
		return idx;
	}

	private targetsystemEntity() {

	}

	/**
	 * ========================================================================
	 */

	@Override
	public void putToCache() {
		targetsystemCache.put(this);
	}

	public targetsystemEntity(String playerIdx) {
		this.idx = IdGenerator.getInstance().generateId();
		this.linkplayeridx = playerIdx;
	}

	private DB_TargetSystem.Builder db_data;

	public DB_TargetSystem.Builder getDb_Builder() {
		if (db_data == null) {
			synchronized (this) {
				if (db_data != null) {
					return db_data;
				}
				db_data = getDBTarget();
			}
		}
		return db_data;
	}

	private DB_TargetSystem.Builder getDBTarget() {
		try {
			if (this.targetdata != null) {
				return DB_TargetSystem.parseFrom(targetdata).toBuilder();
			} else {
				return DB_TargetSystem.newBuilder();
			}
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
			return null;
		}
	}

	@Override
	public void transformDBData() {
		this.targetdata = getDb_Builder().build().toByteArray();
	}

	private Map<Integer, Integer> playerBusinessPopupMap = new ConcurrentHashMap<>();

	public void doDailyMission(TargetTypeEnum typeEnum, int addPro, int param) {
		Builder dbBuilder = getDb_Builder();
		if (dbBuilder == null) {
			return;
		}

		List<DailyMissionObject> missionCfgList = DailyMission.getInstance().getDailyMissionCfgListByType(typeEnum);
		if (missionCfgList == null || missionCfgList.isEmpty()) {
			return;
		}

		// 修改过的mission进度
		List<TargetMission> modifyMissionList = new ArrayList<>();
		for (DailyMissionObject dailyMissionCfg : missionCfgList) {
			TargetMission targetMission = addMissionPro(dailyMissionCfg.getId(), TargetTypeEnum.forNumber(dailyMissionCfg.getMissiontype()), dailyMissionCfg.getAddtion(), dailyMissionCfg.getTargetcount(), typeEnum, addPro, param, dbBuilder.getDailyMissionMap().get(dailyMissionCfg.getId()));

			if (missionUpdate(dbBuilder.getDailyMissionMap().get(dailyMissionCfg.getId()), targetMission)) {
				modifyMissionList.add(targetMission);
				dbBuilder.putDailyMission(targetMission.getCfgId(), targetMission);
			}
		}

		if (!modifyMissionList.isEmpty()) {
			sendRefreshDailyMissionMsg(modifyMissionList);
		}

		addDailyMissionCountPro(typeEnum, modifyMissionList, dbBuilder);
	}

	private void addDailyMissionCountPro(TargetTypeEnum typeEnum, List<TargetMission> modifyMissionList, Builder dbBuilder) {
		if (TargetTypeEnum.TTE_FinishedDailyMission == typeEnum) {
			return;
		}

		int addFinishCount = (int) modifyMissionList.stream().filter(e -> e.getStatus() == MissionStatusEnum.MSE_Finished).count();

		if (addFinishCount <= 0) {
			return;
		}
		doDailyMission(TargetTypeEnum.TTE_FinishedDailyMission, addFinishCount, 0);
	}

	/**
	 * 每日任务解锁的任务数
	 *
	 * @return
	 */
	private long dailyMissionUnlockMissionCount() {
		return DailyMission._ix_id.values().stream().filter(e -> {
			if (e.getId() <= 0 || e.getMissiontype() == TargetTypeEnum.TTE_FinishedDailyMission_VALUE) {
				return false;
			}
			return TargetSystemUtil.targetMissionIsUnlock(getLinkplayeridx(), TargetTypeEnum.forNumber(e.getMissiontype()));
		}).count();
	}

	public void sendRefreshDailyMissionMsg(List<TargetMission> missionList) {
		if (missionList == null || missionList.isEmpty()) {
			return;
		}
		SC_RefreashDailyMissionPro.Builder builder = SC_RefreashDailyMissionPro.newBuilder();
		builder.addAllMissionPro(missionList);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreashDailyMissionPro_VALUE, builder);
	}

	public void sendRefreshDailyMissionMsg(TargetMission missionList) {
		if (missionList == null) {
			return;
		}
		SC_RefreashDailyMissionPro.Builder builder = SC_RefreashDailyMissionPro.newBuilder();
		builder.addMissionPro(missionList);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreashDailyMissionPro_VALUE, builder);
	}

	/**
	 * 用于每日任务刷新后重置任务
	 */
	public void sendClearDailyMissionMsg() {
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_ClearDailyMission_VALUE, SC_ClearDailyMission.newBuilder());
	}

	public void doAchievement(TargetTypeEnum typeEnum, int addPro, int param) {
		List<AchievementObject> cfgList = Achievement.getInstance().getByType(typeEnum);
		if (cfgList == null || cfgList.isEmpty()) {
			return;
		}
		Builder db_builder = getDb_Builder();
		if (db_builder == null) {
			return;
		}

		if (typeEnum == TargetTypeEnum.TTE_CumuLogin && GlobalTick.getInstance().getCurrentTime() > db_builder.getNextUpdateAchivmentCumuLogInTime()) {
			return;
		}

		List<AchievmentPro.Builder> modifyList = new ArrayList<>();
		for (AchievementObject value : cfgList) {
			AchievmentPro.Builder achievementPro = getAchievementPro(value.getId());

			// 判断类型附件条件是否满足条件
			if (!additionIsSatisfy(typeEnum, value.getAddtiomcondition(), param)) {
				continue;
			}

			// 成就的最大目标数
			int maxTargetCount = Achievement.getInstance().getMaxTargetCount(value.getId());
			if (achievementPro.getCurPro() >= maxTargetCount) {
				continue;
			}

			// 判断任务是增量还是全量
			if (isIncrementType(typeEnum)) {
				achievementPro.setCurPro(achievementPro.getCurPro() + addPro);
			} else {
				achievementPro.setCurPro(addPro);
			}

			if (achievementPro.getCurPro() >= maxTargetCount) {
				achievementPro.setCurPro(maxTargetCount);
			}

			putAchievementPro(achievementPro);
			modifyList.add(achievementPro);
		}

		if (!modifyList.isEmpty()) {
			sendRefreshAchievementMsg(modifyList);
			if (typeEnum == TargetTypeEnum.TTE_CumuLogin) {
				db_builder.setNextUpdateAchivmentCumuLogInTime(TimeUtil.getNextDayResetTime(GlobalTick.getInstance().getCurrentTime()));
			}
		}
	}

	public AchievmentPro.Builder getAchievementPro(int cfgId) {
		Builder db_builder = getDb_Builder();
		if (db_builder == null) {
			return AchievmentPro.newBuilder().setCfgId(cfgId);
		}
		AchievmentPro targetMission = db_builder.getAchievementMap().get(cfgId);
		if (targetMission == null) {
			return AchievmentPro.newBuilder().setCfgId(cfgId);
		}
		return targetMission.toBuilder();
	}

	public void putAchievementPro(AchievmentPro.Builder builder) {
		if (builder == null) {
			return;
		}
		Builder db_builder = getDb_Builder();
		if (db_builder == null) {
			return;
		}
		db_builder.putAchievement(builder.getCfgId(), builder.build());
	}

	private void sendRefreshAchievementMsg(List<AchievmentPro.Builder> modifyTargetList) {
		if (modifyTargetList == null || modifyTargetList.isEmpty()) {
			return;
		}

		SC_RefreshAchievement.Builder builder = SC_RefreshAchievement.newBuilder();
		for (AchievmentPro.Builder builder1 : modifyTargetList) {
			builder.addMissionPro(builder1);
		}
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreshAchievement_VALUE, builder);
	}

	/**
	 * 判断一个活动是否可以添加进度
	 *
	 * @param activity
	 * @return
	 */
	public static boolean activityCanAddProgress(ServerActivity activity) {
		if (activity == null) {
			return false;

		}

		long beginTime = activity.getBeginTime();
		long endTime = activity.getEndTime();
		long currentTime = GlobalTick.getInstance().getCurrentTime();
		return (beginTime == -1 || beginTime <= currentTime) && (endTime == -1 || currentTime <= endTime);
	}

	private void doActivity(TargetTypeEnum type, int addPro, int param) {
		Collection<ServerActivity> allGeneralActivities = ActivityManager.getInstance().getAllGeneralActivities();
		if (allGeneralActivities == null || allGeneralActivities.isEmpty()) {
			return;
		}

		List<RefreshActivity> modifyList = new ArrayList<>();
		for (ServerActivity activityCfg : allGeneralActivities) {
			if (!activityCanAddProgress(activityCfg) || activityCfg.getMissionsCount() <= 0) {
				continue;
			}
			if (GeneralActivityTemplate.GAT_ApocalypseBlessing == activityCfg.getTemplate()) {
				continue;
			}
			if (ActivityTypeEnum.ATE_MistGhostBuster == activityCfg.getType()) {
				continue;
			}

			// 如果是累积登陆或者签到类型任务,
			long activityNextCanUpdateTime = getActivityNextCanUpdateTime(activityCfg.getActivityId());
			if (type == TargetTypeEnum.TTE_CumuLogin && activityNextCanUpdateTime != 0 && GlobalTick.getInstance().getCurrentTime() < activityNextCanUpdateTime) {
				continue;
			}

			Map<Integer, ServerSubMission> subMissionsList = activityCfg.getMissionsMap();
			long currentTime = GlobalTick.getInstance().getCurrentTime();
			for (ServerSubMission activitySubCfg : subMissionsList.values()) {
				if (!(activitySubCfg.getEndTimestamp() == -1 || currentTime <= activitySubCfg.getEndTimestamp())) {
					continue;
				}

				TargetMission beforeActivityPro = getDBActivityPro(activityCfg.getActivityId(), activitySubCfg.getIndex());
				TargetMission afterTargetMission = addMissionPro(activitySubCfg.getIndex(), activitySubCfg.getSubType(), activitySubCfg.getAdditon(), activitySubCfg.getTarget(), type, addPro, param, beforeActivityPro);

				if (!missionUpdate(beforeActivityPro, afterTargetMission)) {
					continue;
				}

				// 持久化
				putDBActivityMissionPro(activityCfg.getActivityId(), afterTargetMission);
				// 添加到刷新列表
				modifyList.add(buildRefreshMission(activityCfg.getActivityId(), afterTargetMission));
			}

			if (type == TargetTypeEnum.TTE_CumuLogin) {
				setActivityNextCanUpdateTime(activityCfg.getActivityId());
			}
		}

		if (!modifyList.isEmpty()) {
			sendRefreshActivityMissionByList(modifyList);
		}
	}

	public long getActivityNextCanUpdateTime(long activityId) {
		DB_Activity.Builder dbActivityBuilder = getDBActivityBuilder(activityId);
		if (dbActivityBuilder == null) {
			return 0;
		}
		return dbActivityBuilder.getNextCanUpdateCumuLogTime();
	}

	public void setActivityNextCanUpdateTime(long activityId) {
		DB_Activity.Builder dbActivityBuilder = getDBActivityBuilder(activityId);
		if (dbActivityBuilder == null) {
			return;
		}
		dbActivityBuilder.setNextCanUpdateCumuLogTime(TimeUtil.getNextDayResetTime(GlobalTick.getInstance().getCurrentTime()));
		getDb_Builder().putActivities(activityId,dbActivityBuilder.build());
	}

	public void sendRefreshActivityMissionByList(List<RefreshActivity> list) {
		if (list == null) {
			return;
		}
		SC_RefreshActivity.Builder builder = SC_RefreshActivity.newBuilder();
		builder.addAllRefresh(list);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreshActivity_VALUE, builder);
	}

	public void sendRefreshActivityMission(RefreshActivity activity) {
		if (activity == null) {
			return;
		}

		SC_RefreshActivity.Builder builder = SC_RefreshActivity.newBuilder();
		builder.addRefresh(activity);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreshActivity_VALUE, builder);
	}

	public TargetMission getDBActivityPro(long activityId, int index) {
		DB_Activity.Builder dbActivityBuilder = getDBActivityBuilder(activityId);

		Map<Integer, TargetMission> missionProMap = dbActivityBuilder.getMissionProMap();
		if (missionProMap == null) {
			return TargetMission.newBuilder().setCfgId(index).build();
		}
		return missionProMap.get(index);
	}

	public void putDBActivityMissionPro(long activityId, TargetMission mission) {
		if (mission == null) {
			return;
		}

		DB_Activity.Builder dbActivityBuilder = getDBActivityBuilder(activityId);
		dbActivityBuilder.putMissionPro(mission.getCfgId(), mission);
		putDBActivityBuilder(activityId, dbActivityBuilder);
	}

	public TargetMission.Builder getDBActivityProBuilder(long activityId, int index) {
		TargetMission dbActivityPro = getDBActivityPro(activityId, index);
		if (dbActivityPro == null) {
			return TargetMission.newBuilder().setCfgId(index);
		}
		return dbActivityPro.toBuilder();
	}

	/**
	 * 使用此方法获取活动配置后请调用targetsystemEntity#putDBActivityBuilder使更改生效
	 *
	 * @param activityId
	 * @return
	 */
	public DB_Activity.Builder getDBActivityBuilder(long activityId) {
		Builder db_builder = getDb_Builder();
		if (db_builder == null) {
			return DB_Activity.newBuilder();
		}
		Map<Long, DB_Activity> activitiesMap = db_builder.getActivitiesMap();
		if (activitiesMap == null || activitiesMap.isEmpty()) {
			return DB_Activity.newBuilder();
		}
		DB_Activity db_activity = activitiesMap.get(activityId);

		if (db_activity == null) {
			return DB_Activity.newBuilder();
		}

		return db_activity.toBuilder();
	}

	public void putDBActivityBuilder(long activityId, DB_Activity.Builder builder) {
		if (builder == null) {
			return;
		}

		Builder db_builder = getDb_Builder();
		if (db_builder == null) {
			return;
		}
		db_builder.putActivities(activityId, builder.build());
	}

	public RefreshActivity buildRefreshMission(long activityId, TargetMission builder) {
		RefreshActivity.Builder newBuilder = RefreshActivity.newBuilder();
		if (builder == null) {
			return newBuilder.build();
		}
		newBuilder.setActivityId(activityId);
		newBuilder.setIndex(builder.getCfgId());
		newBuilder.setNewPro(builder.getProgress());
		newBuilder.setNewStatus(builder.getStatus());
		return newBuilder.build();
	}

	public void doMistSeasonTask(TargetTypeEnum typeEnum, int addPro, int param) {
		Builder dbBuilder = getDb_Builder();
		if (dbBuilder == null) {
			return;
		}

		List<MistSeasonMissionObject> missionCfgList = MistSeasonMission.getInstance().getMistSeasonMissionCfgListByType(typeEnum);
		if (missionCfgList == null || missionCfgList.isEmpty()) {
			return;
		}

		// 修改过的mission进度
		List<TargetMission> modifyMissionList = null;
		Map<Integer, TargetMission> mistSeasonMissionMap = dbBuilder.getMistSeasonTaskMap();
		for (MistSeasonMissionObject mistTaskCfg : missionCfgList) {
			assert mistTaskCfg.getMissiontype() != TargetTypeEnum.TTE_MistSeasonTask_KillBossCount_VALUE;
			TargetMission targetMission = addMissionPro(mistTaskCfg.getId(), TargetTypeEnum.forNumber(mistTaskCfg.getMissiontype()), mistTaskCfg.getAddtion(), mistTaskCfg.getTargetcount(), typeEnum, addPro, param, mistSeasonMissionMap.get(mistTaskCfg.getId()));

			if (missionUpdate(mistSeasonMissionMap.get(mistTaskCfg.getId()), targetMission)) {
				if (modifyMissionList == null) {
					modifyMissionList = new ArrayList<>();
				}
				modifyMissionList.add(targetMission);
				dbBuilder.putMistSeasonTask(targetMission.getCfgId(), targetMission);
			}
		}

		if (modifyMissionList != null && !modifyMissionList.isEmpty()) {
			sendMistSeasonTaskMsg(modifyMissionList);
		}
	}

	public void sendMistSeasonTaskMsg(List<TargetMission> missionList) {
		if (missionList == null || missionList.isEmpty()) {
			return;
		}
//        if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(getLinkplayeridx()) != null) {
//            return;
//        }
		SC_RefreashMistSeasonMissionPro.Builder builder = SC_RefreashMistSeasonMissionPro.newBuilder();
		builder.addAllMistSeasonTask(missionList);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreashMistSeasonMissionPro_VALUE, builder);
	}

	public void sendMistSeasonTaskMsg(TargetMission missionList) {
		if (missionList == null) {
			return;
		}
//        if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(getLinkplayeridx()) != null) {
//            return;
//        }
		SC_RefreashMistSeasonMissionPro.Builder builder = SC_RefreashMistSeasonMissionPro.newBuilder();
		builder.addMistSeasonTask(missionList);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreashMistSeasonMissionPro_VALUE, builder);
	}

	public void clearMistSeasonTaskData() {
		getDb_Builder().clearMistSeasonTask();
	}

	/**
	 * 更新每日需要刷新的数据
	 */
	public void updateDailyData(boolean sendMsg) {

		LogUtil.debug("player:{} start update targetsystemEntity daily data", getLinkplayeridx());

		getDb_Builder().clearDailyMission();
		getDb_Builder().clearTodayDoLoginTargetPro();

		checkAndSendWishWellReward(sendMsg);

		updateActivityDailyData(sendMsg);

		// 每日更新boss挑战次数不用通知客户端
		getDb_Builder().getSpecialInfoBuilder().getActivityBossBuilder().clearTimes().clearBuyTimes();

		resetFeatsInfo(sendMsg);

		resetTimeLimitGiftTriggerTimes();

		resetArenaDailyMission();

		resetDemonDescendsDailyMission();

		resetHadesTreasureDailyMission();

		resetDirectPurchaseGiftRecord(sendMsg);

		resetRuneTreasureDailyMission();

		resetRichManDailyItem();

		resetBusinessPopup();

		resetFestivalBoss();

		resetItemCard();
		clearRollCard();
		LogUtil.info("player:{} update  targetsystemEntity daily data,finished resetItemCard", getLinkplayeridx());
		resetMatchArenaTaskMission();
		resetCrossArenaTaskMission();
		clearShareRecord();
		resetPetAvoidance(sendMsg);

		LogUtil.info("update player:{} updateDailyData finished", getLinkplayeridx());
	}

	private void resetPetAvoidance(boolean sendMsg) {
		ServerActivity activity = ActivityManager.getInstance().getActivityByType(ActivityTypeEnum.ATE_PetAvoidance);
		if (!ActivityUtil.activityInOpen(activity)) {
			LogUtil.debug("resetPetAvoidance activity closed");
			return;
		}
		DB_PetAvoidance.Builder petAvoidanceBuilder = getDb_Builder().getSpecialInfoBuilder().getPetAvoidanceBuilder();
		if (petAvoidanceBuilder.getChallengedTimes() > 0) {
			petAvoidanceBuilder.clearChallengedTimes();
			LogUtil.info("reset player:{} PetAvoidance daily challenged times ", getIdx());
			if (sendMsg) {
				SC_PetAvoidanceUpdate.Builder refreshData = SC_PetAvoidanceUpdate.newBuilder();
				refreshData.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
				refreshData.setTimes(petAvoidanceBuilder.getChallengedTimes());
				refreshData.setTimesLimit(activity.getPetAvoidance().getDailyChallengeTimes());
				GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_PetAvoidanceUpdate_VALUE, refreshData);
			}
		}
	}

	private void clearShareRecord() {
		getDb_Builder().clearActivityShareTimes();
	}

	private void resetFestivalBoss() {
		for (Entry<Long, TargetSystemDB.DB_FestivalBoss> entry : getDb_Builder().getFestivalBossInfoMap().entrySet()) {
			if (entry.getValue().getTodayChallengeTimes() > 0) {
				getDb_Builder().putFestivalBossInfo(entry.getKey(), entry.getValue().toBuilder().clearTodayChallengeTimes().build());
			}
		}
	}

	public void checkAndSendWishWellReward(boolean sendMsg) {
		int sendMailHour = GameConfig.getById(GameConst.CONFIG_ID).getWishwellsendmailtime();

		long sendMailTime = sendMailHour * TimeUtil.MS_IN_A_HOUR;

		TargetSystemDB.DB_WishingWell.Builder wishingWellBuilder = getDb_Builder().getSpecialInfoBuilder().getWishingWellBuilder();

		for (WishingWellItem wish : wishingWellBuilder.getWishMapMap().values()) {
			if (WishStateEnum.WSE_UnClaim != wish.getState()) {
				continue;
			}
			if (wish.getClaimTime() + sendMailTime < GlobalTick.getInstance().getCurrentTime()) {
				doWishMailReward(wish);
				WishingWellItem.Builder update = wish.toBuilder().setState(WishStateEnum.WSE_Claimed);
				wishingWellBuilder.putWishMap(wish.getWishIndex(), update.build());
				if (sendMsg) {
					sendWishUpdate(update);
				}
			}

		}
	}

	private void doWishMailReward(WishingWellItem wish) {

		WishWellConfigObject config = WishWellConfig.getById(wish.getWishIndex());

		if (config == null) {
			LogUtil.error("player:{} doWishMailReward error by WishWellConfig is null by wishId:{}", getLinkplayeridx(), wish.getWishIndex());
			return;
		}
		List<Reward> rewardList = RewardUtil.getRewardsByRewardId(config.getRewardoptions());

		if (rewardList == null || rewardList.size() <= wish.getRewardIndex()) {
			LogUtil.error("player:{} doWishMailReward error,case by wishReward length not enough", getLinkplayeridx(), wish.getRewardIndex());
			return;
		}
		Reward reward = rewardList.get(wish.getRewardIndex());
		Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_WishingWell);
		EventUtil.triggerAddMailEvent(getLinkplayeridx(), MailTemplateUsed.getById(GameConst.CONFIG_ID).getWishwell(), Collections.singletonList(reward), reason);
		LogUtil.info("player:{} doWishMailReward,wishIndex:{} ,reward:{}", getLinkplayeridx(), wish.getRewardIndex(), reward);

	}

	private void resetCrossArenaTaskMission() {
		Map<Integer, TargetMission> task = getDb_Builder().getCrossArenaInfoMap();
		TargetSystem.SC_RefCrossArenaTaskMission.Builder refreshBuilder = TargetSystem.SC_RefCrossArenaTaskMission.newBuilder();
		for (Map.Entry<Integer, TargetMission> ent : task.entrySet()) {
			TargetMission.Builder msg = ent.getValue().toBuilder();
			msg.setStatusValue(0);
			msg.setProgress(0);
			refreshBuilder.addMission(msg);
		}
		getDb_Builder().clearCrossArenaInfo();
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefCrossArenaTaskMission_VALUE, refreshBuilder);
	}

	private void resetMatchArenaTaskMission() {
		Map<Integer, TargetMission> task = getDb_Builder().getMatchArenaInfoMap();
		TargetSystem.SC_RefMatchArenaTaskMission.Builder refreshBuilder = TargetSystem.SC_RefMatchArenaTaskMission.newBuilder();
		for (Map.Entry<Integer, TargetMission> ent : task.entrySet()) {
			TargetMission.Builder msg = ent.getValue().toBuilder();
			msg.setStatusValue(0);
			msg.setProgress(0);
			refreshBuilder.addMission(msg);
		}
		getDb_Builder().clearMatchArenaInfo();
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefMatchArenaTaskMission_VALUE, refreshBuilder);
	}

	private void resetRichManDailyItem() {
		if (CollectionUtils.isEmpty(ActivityManager.getInstance().getOpenActivitiesByType(ActivityTypeEnum.ATE_RichMan))) {
			LogUtil.debug("resetRichManDailyItem activity closed");
			return;
		}
		if (getDb_Builder().getSpecialInfoBuilder().getRichManBuilder().getClaimDailyItem()) {
			LogUtil.info("reset player:{} richMan daily free item status", getLinkplayeridx());
			getDb_Builder().getSpecialInfoBuilder().getRichManBuilder().setClaimDailyItem(false);
			if (GlobalData.getInstance().checkPlayerOnline(getLinkplayeridx())) {
				sendBeforeRichManInfo();
			}
		}
	}

	private void resetDirectPurchaseGiftRecord(boolean sendMsg) {
		TargetSystemDB.DB_SpecialActivity.Builder specialInfoBuilder = getDb_Builder().getSpecialInfoBuilder();
		if (specialInfoBuilder.getDirectPurchaseGiftBuyRecordCount() <= 0) {
			return;
		}
		for (Long giftId : getDb_Builder().getSpecialInfo().getDirectPurchaseGiftBuyRecordMap().keySet()) {
			Activity.DirectPurchaseGift gift = DirectGiftPurchaseHandler.getInstance().queryGiftByGiftId(giftId);
			if (gift == null || gift.getDailyReset()) {
				specialInfoBuilder.removeDirectPurchaseGiftBuyRecord(giftId);
				if (sendMsg) {
					sendDirectGiftPurchaseReset(giftId);
				}
			}
		}
	}

	private void sendDirectGiftPurchaseReset(Long giftId) {
		Activity.SC_UpdatePurchaseGiftInfo.Builder msg = Activity.SC_UpdatePurchaseGiftInfo.newBuilder();
		Activity.DirectPurchaseGift directPurchaseGift = DirectGiftPurchaseHandler.getInstance().queryGiftByGiftId(giftId);
		int total = directPurchaseGift == null ? 0 : directPurchaseGift.getLimitBuy();
		msg.setGiftId(giftId);
		msg.setLimitBuy(total);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_UpdatePurchaseGiftInfo_VALUE, msg);
	}

	private void resetTimeLimitGiftTriggerTimes() {
		getDb_Builder().getTimeLimitGiftInfoBuilder().clearCurTarget().clearTodayTriggerTimes();
		Map<Integer, DB_TimeLimitGiftItem> giftsMap = getDb_Builder().getTimeLimitGiftInfo().getGiftsMap();
		for (DB_TimeLimitGiftItem gift : giftsMap.values()) {
			if (gift.getTriggerTime() != 0) {
				getDb_Builder().getTimeLimitGiftInfoBuilder().putGifts(gift.getId(), gift.toBuilder().setTriggerTime(0).build());
			}
		}
	}

	private void updateActivityDailyData(boolean sendMsg) {
		clearExchangeActivityDropInfo();

		clearBuyActivityInfo(sendMsg);

		clearDailyOnlineActivity();

		updateDayDayRechargeDailyData(sendMsg);

		clearDailyFirstRechargeData(sendMsg);

		if (sendMsg) {
			GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_ClearDailyMission_VALUE, SC_ClearDailyMission.newBuilder());
		}
	}

	public void clearDailyFirstRechargeData(boolean playerOnline) {
		TargetSystemDB.DB_DailyFirstRecharge dailyFirstRecharge = getDb_Builder().getSpecialInfo().getDailyFirstRecharge();
		if (dailyFirstRecharge.getState() == PayActivityStateEnum.PAS_NotActive) {
			return;
		}
		if (dailyFirstRecharge.getState() == PayActivityStateEnum.PAS_SignOn) {
			EventUtil.triggerAddMailEvent(getLinkplayeridx(), MailTemplateUsed.getById(GameConst.CONFIG_ID).getDailyfirstrechagerewards(),
					DailyFirstRechargeManage.getInstance().queryDailyReward(GlobalTick.getInstance().getCurrentTime() - TimeUtil.MS_IN_A_DAY),
					ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DailyFirstPayRecharge));
		}
		getDb_Builder().getSpecialInfoBuilder().getDailyFirstRechargeBuilder().setState(PayActivityStateEnum.PAS_NotActive);
		if (playerOnline && !ActivityManager.getInstance().activityIsEnd(LocalActivityId.DailyFirstRecharge)) {
			sendDailyFirstRechargeUpdate();
		}

	}

	public void updateDayDayRechargeDailyData(boolean playerOnline) {

		List<ServerActivity> activities = ActivityManager.getInstance().getOpenActivitiesByType(ActivityTypeEnum.ATE_DayDayRecharge);
		if (CollectionUtils.isEmpty(activities)) {
			return;
		}
		ActivityDayDayRecharge activityConfig = activities.get(0).getDayDayRecharge();

		boolean sendMsg = false;

		DB_DayDayRecharge db_dayDayRecharge = getDb_Builder().getSpecialInfo().getDayDayRecharge();

		int rechargeRewardIndex = Math.max(db_dayDayRecharge.getClaimedIndexList().stream().max(Integer::compareTo).orElse(-1), db_dayDayRecharge.getCanClaimIndexList().stream().max(Integer::compareTo).orElse(-1));

		if (rechargeRewardIndex < activityConfig.getRechargeRewardsCount() - 2 && db_dayDayRecharge.getShowRewardIndex() != rechargeRewardIndex + 1) {
			getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder().setShowRewardIndex(rechargeRewardIndex + 1);
			sendMsg = true;
		}
		getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder().setTriggerDailyRechargeReward(false);

		int freeIndex = db_dayDayRecharge.getCurFreeIndex();

		if (freeIndex < activityConfig.getFreeRewardsCount() - 1) {
			getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder().clearClaimTodayFree().setCurFreeIndex(++freeIndex);
			sendMsg = true;
		} else if (!getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder().getClaimTodayFree()) {
			getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder().setClaimTodayFree(true);
			sendMsg = true;
		}

		if (playerOnline && sendMsg) {
			sendDayDayRechargeUpdate();
		}

	}

	private void clearDailyOnlineActivity() {
		if (!ActivityManager.activityIsOpen(LocalActivityId.DailyOnline, getLinkplayeridx())) {
			return;
		}
		TimeLimitActivityObject config = TimeLimitActivity.getById(LocalActivityId.DailyOnline);
		if (config == null) {
			return;
		}
		int[] taskList = config.getTasklist();
		Map<Integer, TargetMission> missionProMap = getDb_Builder().getSpecialInfo().getTimeLimitActivities().getMissionProMap();
		if (taskList.length < 1 || MapUtils.isEmpty(missionProMap)) {
			return;
		}
		Map<Integer, TargetMission> collect = missionProMap.values().stream().filter(mission -> !ArrayUtil.intArrayContain(taskList, mission.getCfgId())).collect(Collectors.toMap(TargetMission::getCfgId, a -> a));
		getDb_Builder().getSpecialInfoBuilder().getTimeLimitActivitiesBuilder().clearMissionPro().putAllMissionPro(collect);

		if (GlobalData.getInstance().checkPlayerOnline(getLinkplayeridx())) {

			GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_ResetActivity_VALUE, Activity.SC_ResetActivity.newBuilder().setActivityId(LocalActivityId.DailyOnline));
		}
	}

	private void clearBuyActivityInfo(boolean playerOnline) {
		List<Integer> cycleClearActivityIds = LocalActivityId.getCycleClearActivityIds();
		for (Integer activityId : cycleClearActivityIds) {
			DB_Activity.Builder dbActivityBuilder = getDBActivityBuilder(activityId);
			if (GlobalTick.getInstance().getCurrentTime() < dbActivityBuilder.getNextCanUpdateCumuLogTime()) {
				continue;
			}
			long nextResetTime = TimeUtil.getNextDaysResetTime(GlobalTick.getInstance().getCurrentTime(), getCycleDaysByActivityId(activityId));
			DB_Activity.Builder newActivity = dbActivityBuilder.clear().setNextCanUpdateCumuLogTime(nextResetTime);
			db_data.putActivities(activityId, newActivity.build());

			EventUtil.triggerClearBuyRecordOnPlayer(getLinkplayeridx(), activityId);
			if (playerOnline) {
				GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_ResetActivity_VALUE, Activity.SC_ResetActivity.newBuilder().setActivityId(activityId).setNextResetTime(nextResetTime));
			}
		}
	}

	private int getCycleDaysByActivityId(long activityId) {
		if (LocalActivityId.DailyGift == activityId) {
			return 1;
		} else if (LocalActivityId.WeeklyGift == activityId) {
			return 7;
		} else if (LocalActivityId.MonthlyGift == activityId) {
			return 30;
		}
		return 1;
	}

	private void clearExchangeActivityDropInfo() {
		Collection<ServerActivity> allExchangeActivity = ActivityManager.getInstance().getActivitiesByType(ActivityTypeEnum.ATE_Exchange);
		if (!CollectionUtils.isEmpty(allExchangeActivity)) {
			for (ServerActivity serverActivity : allExchangeActivity) {
				DB_Activity.Builder dbActivityBuilder = getDBActivityBuilder(serverActivity.getActivityId());
				dbActivityBuilder.clearDropItemInfo();
				putDBActivityBuilder(serverActivity.getActivityId(), dbActivityBuilder);
			}
		}
	}

	private void resetFeatsInfo(boolean sendMsg) {
		boolean change = false;
		for (Entry<Integer, DB_Feats> ent : getDb_Builder().getFeatsInfosMap().entrySet()) {
			if (ent.getValue().getResetTime() > GlobalTick.getInstance().getCurrentTime()) {
				continue;
			}
			if (ent.getKey() == GameConst.FEAT_TYPE_WUJIN) {// 无尽尖塔不重置
				continue;
			}
			change = true;
			/*DB_Feats.Builder builder = ent.getValue().toBuilder();
			builder.clear();*/
			initFeatsInfo(ent.getKey());
			itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(getLinkplayeridx());
			if (itemBag == null) {
				LogUtil.error("targetSystemEntity resetFeatsInfo failed by itemBag empty,playerId[{}]", getLinkplayeridx());
				return;
			}
			int gongXunId = getGongXunId(ent.getKey());
			if (gongXunId == -1) {
				continue;
			}
			// 功勋实际上不应该是物品,策划为了背包内容多些,就放到背包了
			itemBag.clearItem(Collections.singleton(gongXunId), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Expire));
		}
		if (sendMsg && change) {
			sendFeats();
		}
	}

	/**
	 * 全量类型枚举值
	 */
	private static final Set<TargetTypeEnum> WHOLE_TYPE = new HashSet<>();

	static {
		WHOLE_TYPE.add(TargetTypeEnum.TTE_PlayerLvReach);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_RuneIntensifyLv);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_PassMianLineChapter);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_MistLevel);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_MistContinuousKillPlayer);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_PassSpireLv);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_Pet_SpecifyPetLvUpReach);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_Pet_SpecifyPetStarUpReach);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_Pet_SpecifyPetAwakeUpReach);
		WHOLE_TYPE.add(TargetTypeEnum.TEE_Arena_DanReach);
		WHOLE_TYPE.add(TargetTypeEnum.TEE_BossTower_UnlockLvReach);
		WHOLE_TYPE.add(TargetTypeEnum.TEE_Player_RechargeCoupon);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_TheWar_KillMonsterCount);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_TheWar_JobTileLvReach);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_CrossArena_GRADELvReach);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_CrossArena_SCENEIDReach);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_CrossArena_COTWin);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_CrazyDuel_CompleteBattle);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_Collection_CumuLvUp);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_PlayerFriendReach);
		WHOLE_TYPE.add(TargetTypeEnum.TTE_TrainScore);
	}

	/**
	 * 返回该成就类型是否是增量类型
	 *
	 * @param typeEnum
	 */
	public static boolean isIncrementType(TargetTypeEnum typeEnum) {
		if (typeEnum == null) {
			return true;
		}
		if (WHOLE_TYPE.contains(typeEnum)) {
			return false;
		}
		return true;
	}

	/**
	 * 此方法只获取通用活动,封装进度
	 *
	 * @return
	 */
	public List<ClientActivity> getActivities() {
		List<ClientActivity> result = new ArrayList<>();

		List<ServerActivity> allActivities = ActivityManager.getInstance().getAllActivities();
		if (!GameUtil.collectionIsEmpty(allActivities)) {
			List<ClientActivity> clientActivities = parseToClientActivities(allActivities);
			if (!GameUtil.collectionIsEmpty(clientActivities)) {
				result.addAll(clientActivities);
			}
		}

		// 获取限时活动
		List<ClientActivity> timeLimitActivities = getTimeLimitActivities();
		if (CollectionUtils.isNotEmpty(timeLimitActivities)) {
			result.addAll(timeLimitActivities);
		}

		// 许愿池活动
		ClientActivity wishingWellActivity = getPlayerWishWellActivity();
		if (wishingWellActivity != null) {
			result.add(wishingWellActivity);
		}

		return result;
	}

	private List<ClientActivity> getTimeLimitActivities() {
		return buildTimeLimitClientActivityList(TimeLimitActivity._ix_id.values());
	}

	private List<ClientActivity> buildTimeLimitClientActivityList(Collection<TimeLimitActivityObject> timeLimitActivityCfgList) {
		if (CollectionUtils.isEmpty(timeLimitActivityCfgList)) {
			return null;
		}

		// 检查限时任务开始时间
		long currentTime = GlobalTick.getInstance().getCurrentTime();
		DB_TimeLimitActivity.Builder timeLimitBuilder = getDb_Builder().getSpecialInfoBuilder().getTimeLimitActivitiesBuilder();
		if (timeLimitBuilder.getStartTime() == 0) {
			timeLimitBuilder.setStartTime(currentTime);
		}

		long startTime = timeLimitBuilder.getStartTime();
		LanguageEnum languageEnum = PlayerUtil.queryPlayerLanguage(getLinkplayeridx());

		List<ClientActivity> result = new ArrayList<>();
		int playerLv = PlayerUtil.queryPlayerLv(getLinkplayeridx());
		for (TimeLimitActivityObject activityConfig : timeLimitActivityCfgList) {
			// 未开启
			if (activityConfig.getId() <= 0 || activityConfig.getShowlv() > playerLv) {
				continue;
			}

			if (activityFinished(activityConfig)) {
				continue;
			}

			// 时间未结束
			long endTime = TimeUtil.calculateActivityEndTime(startTime, activityConfig.getEnddistime());
			if (activityConfig.getEnddistime() != -1 && endTime <= currentTime) {
				LogUtil.debug("time limit activity, id :" + activityConfig.getId() + " is over, playerIdx:" + getLinkplayeridx() + ", start time:" + startTime);
				continue;
			}

			ClientActivity.Builder builder = ClientActivity.newBuilder();
			builder.setActicityId(activityConfig.getId());
			builder.setActivityType(getActivityTypeEnum(activityConfig.getId()));
			if (activityConfig.getTitle() > 0) {
				builder.setTitle(ServerStringRes.getContentByLanguage(activityConfig.getTitle(), languageEnum));
			}
			if (activityConfig.getDesc() > 0) {
				builder.setDesc(ServerStringRes.getContentByLanguage(activityConfig.getDesc(), languageEnum));
			}
			if (activityConfig.getDetail() > 0) {
				builder.setDetail(ServerStringRes.getContentByLanguage(activityConfig.getDetail(), languageEnum));
			}
			String picture = activityConfig.getPicture();
			if (picture != null) {
				builder.setPictureName(picture);
			}
			builder.setTabTypeValue(activityConfig.getTabtype());

			ActivityTime.Builder timeBuilder = ActivityTime.newBuilder();
			timeBuilder.setTimeType(CycleTypeEnum.CTE_TimeLimit);
			timeBuilder.setTimeContent(Cycle_TimeLimit.newBuilder().setBeginTimestamp(startTime).setEndTimestamp(endTime).build().toByteString());
			builder.setCycleTime(timeBuilder);
			builder.setRedDotTypeValue(activityConfig.getReddottype());
			builder.setTemplate(getGeneralActivityTemplate(activityConfig.getId()));

			combineMissionList(timeLimitBuilder, startTime, languageEnum, activityConfig, builder);
			combineOnlineTimeActivitySpecial(activityConfig, builder);

			result.add(builder.build());
		}
		return result;
	}

	private boolean activityFinished(TimeLimitActivityObject activityConfig) {
		// 七日签到任务全部领取直接跳过
		if (activityConfig.getId() == LocalActivityId.SEVEN_DAYS_SIGN_IN) {
			return sevenDaysSignInFinished();
		}
		// 0元购完成跳过
		if (activityConfig.getId() == LocalActivityId.ZeroCostPurchase) {
			return zeroCostPurchaseFinished();
		}
		return false;
	}

	private boolean zeroCostPurchaseFinished() {
		Map<Integer, Activity.ZeroCostPurchaseItem> zeroCostPurchaseMap = getDb_Builder().getSpecialInfo().getZeroCostPurchase().getZeroCostPurchaseMap();
		return zeroCostPurchaseMap.size() >= 2 && zeroCostPurchaseMap.values().stream().allMatch(e -> e.getClaimStatus() == Activity.ActivityClaimStatusEnum.ACS_Complete);
	}

	private ActivityTypeEnum getActivityTypeEnum(long activityId) {
		if (activityId == LocalActivityId.ZeroCostPurchase) {
			return ActivityTypeEnum.ATE_ZeroCostPurchase;
		}
		return ActivityTypeEnum.ATE_General;
	}

	private void combineMissionList(DB_TimeLimitActivity.Builder timeLimitBuilder, long startTime, LanguageEnum languageEnum, TimeLimitActivityObject activityConfig, ClientActivity.Builder builder) {

		for (int taskId : activityConfig.getTasklist()) {
			TimeLimitActivityTaskObject task = TimeLimitActivityTask.getById(taskId);
			if (task == null) {
				LogUtil.error("can not find time limit task, task id:" + taskId + ", activity id:" + activityConfig.getId());
				continue;
			}

			ClientSubMission.Builder taskBuilder = ClientSubMission.newBuilder();
			taskBuilder.setIndex(task.getId());
			taskBuilder.setDesc(getActivityDesc(languageEnum, activityConfig, task));
			taskBuilder.setTarget(task.getTargetcount());
			taskBuilder.setTargetType(TargetTypeEnum.forNumber(task.getMissiontype()));
			TargetMission targetMission = timeLimitBuilder.getMissionProMap().get(task.getId());
			if (targetMission != null) {
				taskBuilder.setPlayerPro(targetMission.getProgress());
				taskBuilder.setStatus(targetMission.getStatus());
			}
			taskBuilder.setEndTimestamp(TimeUtil.calculateActivityEndTime(startTime, task.getEndtime()));
			taskBuilder.addAllReward(RewardUtil.parseRewardIntArrayToRewardList(task.getReward()));

			builder.addMissionLists(taskBuilder);
		}
	}

	private GeneralActivityTemplate getGeneralActivityTemplate(int activityId) {

		switch (activityId) {
		case LocalActivityId.SEVEN_DAYS_SIGN_IN:
			return GeneralActivityTemplate.GAT_SevenDaysSignIn;
		case LocalActivityId.DailyOnline:
			return GeneralActivityTemplate.GAT_DailyOnline;
		case LocalActivityId.CumuOnline:
			return GeneralActivityTemplate.GAT_CumuOnline;
		case LocalActivityId.CumuRecharge:
			return GeneralActivityTemplate.GAT_CumuRecharge;
		}
		return GeneralActivityTemplate.GAT_Null;

	}

	private String getActivityDesc(LanguageEnum languageEnum, TimeLimitActivityObject activityConfig, TimeLimitActivityTaskObject task) {
		String contentByLanguage;
		if (onLineActivity(activityConfig.getId())) {
			long second = task.getTargetcount();
			long hours = second / 3600;// 转换小时数
			second = second % 3600;// 剩余秒数
			long minutes = second / 60;// 转换分钟
			contentByLanguage = ServerStringRes.getContentByLanguage(task.getMissiondesc(), languageEnum, hours, minutes);
		} else {
			contentByLanguage = ServerStringRes.getContentByLanguage(task.getMissiondesc(), languageEnum, task.getTargetcount());
		}
		return contentByLanguage;
	}

	private void combineOnlineTimeActivitySpecial(TimeLimitActivityObject activityConfig, ClientActivity.Builder builder) {
		if (!onLineActivity(activityConfig.getId())) {
			return;
		}
		int onlineTime = 0;
		playerEntity player = playerCache.getByIdx(getLinkplayeridx());
		if (player == null) {
			return;
		}
		if (activityConfig.getId() == LocalActivityId.CumuOnline) {
			onlineTime = player.getDb_data().getCumuOnline();
		}
		if (activityConfig.getId() == LocalActivityId.DailyOnline) {
			onlineTime = player.getDb_data().getTodayOnline();
		}
		// 累计在线需要传给前端累计在线时间,当前时间
		builder.addOtherParams(GlobalTick.getInstance().getCurrentTime() + "").addOtherParams(onlineTime + "");
		builder.getMissionListsBuilderList().forEach(e -> e.setNotShowPro(true));
	}

	private boolean onLineActivity(long activityId) {
		return LocalActivityId.CumuOnline == activityId || LocalActivityId.DailyOnline == activityId;
	}

	private ClientActivity getPlayerWishWellActivity() {
		Builder db_builder = getDb_Builder();
		WishWellActivityConfigObject config = WishWellActivityConfig.getById(GameConst.CONFIG_ID);
		if (config == null) {
			LogUtil.error("WishWellActivityConfig  config is null");
			return null;
		}

		long currentTime = GlobalTick.getInstance().getCurrentTime();

		if (TimeUtil.parseTime(config.getOverdistime()) < currentTime || TimeUtil.parseTime(config.getStartdistime()) > currentTime) {
			LogUtil.debug("许愿池活动未开始");
			return null;
		}

		protocol.TargetSystemDB.DB_WishingWell.Builder wishingWellBuilder = db_builder.getSpecialInfoBuilder().getWishingWellBuilder();

		if ((wishingWellBuilder.getWishMapCount() > 0 && wishingWellBuilder.getWishMapMap().values().stream().allMatch(e -> WishStateEnum.WSE_Claimed == e.getState())) || (wishingWellBuilder.getEndTime() != 0 && wishingWellBuilder.getEndTime() < GlobalTick.getInstance().getCurrentTime())) {
			return null;
		}
		LanguageEnum languageEnum = PlayerUtil.queryPlayerLanguage(getLinkplayeridx());
		ClientActivity.Builder builder = ClientActivity.newBuilder();
		builder.setActicityId(LocalActivityId.WishingWell);
		if (config.getTitle() > 0) {
			builder.setTitle(ServerStringRes.getContentByLanguage(config.getTitle(), languageEnum));
		}
		if (config.getDesc() > 0) {
			builder.setDesc(ServerStringRes.getContentByLanguage(config.getDesc(), languageEnum));
		}
		builder.setTabType(EnumClientActivityTabType.ECATT_Independent);
		String picture = config.getPicture();
		if (picture != null) {
			builder.setPictureName(picture);
		}
		builder.setCycleTime(getWishTimeBuilder(wishingWellBuilder.getStartTime(), wishingWellBuilder.getEndTime()));
		builder.setActivityType(ActivityTypeEnum.ATE_WishWell);
		if (config.getHelp() > 0) {
			builder.setDetail(ServerStringRes.getContentByLanguage(config.getHelp(), languageEnum));
		}

		return builder.build();
	}

	/**
	 * 解析并过滤不在展示时间内活动
	 *
	 * @param activities
	 * @return
	 */
	private List<ClientActivity> parseToClientActivities(List<ServerActivity> activities) {
		if (GameUtil.collectionIsEmpty(activities)) {
			return null;
		}

		List<ClientActivity> result = new ArrayList<>();
		LanguageEnum language = PlayerUtil.queryPlayerLanguage(getLinkplayeridx());
		for (ServerActivity activity : activities) {
			if (!ActivityUtil.activityNeedDis(activity)) {
				continue;
			}
			result.add(parseToClientActivity(activity, language));
		}
		return result;
	}

	/**
	 * 解析活动
	 *
	 * @param serverActivity
	 * @param language
	 * @return
	 */
	private ClientActivity parseToClientActivity(ServerActivity serverActivity, LanguageEnum language) {
		ClientActivity.Builder builder = ClientActivity.newBuilder();
		builder.setActicityId(serverActivity.getActivityId());
		builder.setActivityType(getClientActivityType(serverActivity));
		builder.setTitle(GameUtil.getLanguageStr(serverActivity.getTitle(), language));
		builder.setDesc(GameUtil.getLanguageStr(serverActivity.getDesc(), language));
		builder.setDetail(GameUtil.getLanguageStr(serverActivity.getDetail(), language));
		builder.setPictureName(serverActivity.getPictureName());
		builder.setTemplate(serverActivity.getTemplate());
		builder.setTag(serverActivity.getTag());
		if (serverActivity.getTemplate() != null) {
			builder.setTemplate(serverActivity.getTemplate());
		}
		builder.setTabTypeValue(serverActivity.getTabTypeValue());
		builder.setRedDotType(serverActivity.getRedDotType());

		DB_Activity.Builder dbActivity = getDBActivityBuilder(serverActivity.getActivityId());

		builder.setCycleTime(getTimeBuilder(serverActivity, dbActivity));

		builder.setNextResetTime(getActivityNextResetTime(serverActivity.getType(), dbActivity));

		builder.setRebateRate(serverActivity.getRebateRate());

		combineRankingReward(serverActivity, builder);

		combineMission(serverActivity, language, builder);
		combineExMission(serverActivity, builder);

		combineBuyMission(serverActivity, builder);

		combineDemonDescentsRandom(serverActivity, builder);

		combineDayDayRecharge(serverActivity, builder, language);

		combineGhostBusterMission(serverActivity, language, builder);

		builder.addAllRuneTreasurePool(serverActivity.getRuneTreasurePoolList());
		builder.addAllStageRewards(serverActivity.getStageRewardsMap().values());
		builder.addAllDisplayRewards(serverActivity.getDisplayRewardsList());

		getDb_Builder().putActivities(serverActivity.getActivityId(), dbActivity.build());
		return builder.build();
	}

	private long getActivityNextResetTime(ActivityTypeEnum type, DB_Activity.Builder serverActivity) {
		if (type != ActivityTypeEnum.ATE_DailyGift && type != ActivityTypeEnum.ATE_WeeklyGift && type != ActivityTypeEnum.ATE_MonthlyGift) {
			return -1;
		}
		return serverActivity.getNextCanUpdateCumuLogTime();

	}

	/**
	 * 关联抓鬼任务
	 */
	private void combineGhostBusterMission(ServerActivity serverActivity, LanguageEnum language, ClientActivity.Builder builder) {
		if (serverActivity.getMissionsCount() <= 0
				|| ActivityTypeEnum.ATE_MistGhostBuster != serverActivity.getType()) {
			return;
		}
		DB_MistGhostActivity.Builder ghostMissionBuilder = getDb_Builder().getSpecialInfoBuilder().getGhostActivityMissionBuilder();
		for (ServerSubMission value : serverActivity.getMissionsMap().values()) {
			ClientSubMission.Builder subMission = ClientSubMission.newBuilder();
			subMission.setIndex(value.getIndex());
			subMission.setTarget(value.getTarget());
			subMission.setDesc(GameUtil.getLanguageStr(value.getDesc(), language));
			subMission.addAllReward(value.getRewardList());
			subMission.addAllRandoms(value.getRandomsList());
			subMission.setEndTimestamp(value.getEndTimestamp());
			subMission.setTargetType(value.getSubType());

			TargetMission.Builder dbActivityPro = null;
			for (int i = 0; i < ghostMissionBuilder.getMissionProCount(); i++) {
				if (ghostMissionBuilder.getMissionPro(i).getCfgId() == value.getIndex()) {
					dbActivityPro = ghostMissionBuilder.getMissionProBuilder(i);
					break;
				}
			}
			if (dbActivityPro == null) {
				dbActivityPro = TargetMission.newBuilder().setCfgId(value.getIndex());
			}
			subMission.setPlayerPro(dbActivityPro.getProgress());
			subMission.setStatus(dbActivityPro.getStatus());

			builder.addMissionLists(subMission.build());
		}
		Cycle_Day.Builder builder1 = Cycle_Day.newBuilder();
		builder1.setStartOfDay((int) (serverActivity.getDailyBeginTime()));
		builder1.setEndOfDay((int) (serverActivity.getDailyEndTime()));
		builder.getCycleTimeBuilder().setTimeType(CycleTypeEnum.CTE_CycleDay).setTimeContent(builder1.build().toByteString());
	}

	/**
	 * 关联魔灵降临
	 */
	private void combineDemonDescentsRandom(ServerActivity serverActivity, ClientActivity.Builder builder) {
		if (serverActivity.getDemonDescentsRandomCount() <= 0) {
			return;
		}

		builder.addAllDemonDescentsRandom(serverActivity.getDemonDescentsRandomList());
	}

	private void combineDayDayRecharge(ServerActivity serverActivity, ClientActivity.Builder clientActivity, LanguageEnum language) {
		ActivityDayDayRecharge activityDayDayRecharge = serverActivity.getDayDayRecharge();
		if (activityDayDayRecharge.getRechargeRewardsCount() <= 0) {
			return;
		}
		DB_DayDayRecharge.Builder db_dayDayRecharge = getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder();
		if (db_dayDayRecharge.getStartTime() == 0) {
			// 初始化
			getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder().setStartTime(GlobalTick.getInstance().getCurrentTime());
			playerEntity player = playerCache.getByIdx(getLinkplayeridx());
			if (player != null && player.getDb_data().getTodayRecharge() > 0) {
				triggerDayDayRechargeReward(player.getDb_data().getTodayRecharge(), false);
				// 这里需要重新get拿最新值
				db_dayDayRecharge = getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder();
			}
		}
		DayDayRecharge.Builder dayDayRechargeBuilder = clientActivity.getDayDayRechargeBuilder();
		dayDayRechargeBuilder.setClaimTodayFree(db_dayDayRecharge.getClaimTodayFree());
		dayDayRechargeBuilder.setShowRewardIndex(db_dayDayRecharge.getShowRewardIndex());
		dayDayRechargeBuilder.addAllCanClaimRechargeIndex(db_dayDayRecharge.getCanClaimIndexList());
		dayDayRechargeBuilder.addAllClaimedRechargeIndex(db_dayDayRecharge.getClaimedIndexList());
		dayDayRechargeBuilder.addAllRewardWorth(activityDayDayRecharge.getRewardWorthList());
		dayDayRechargeBuilder.addAllRechargeReward(activityDayDayRecharge.getRechargeRewardsList());
		dayDayRechargeBuilder.setDailyRechargeNeed(activityDayDayRecharge.getDailyTarget());
		dayDayRechargeBuilder.setAdMessage(GameUtil.getLanguageStr(activityDayDayRecharge.getAdMessage(), language));
		dayDayRechargeBuilder.setTodayRecharge(playerCache.getInstance().queryTodayRecharge(getLinkplayeridx()));
	}

	private ActivityTime.Builder getTimeBuilder(ServerActivity serverActivity, DB_Activity.Builder dbActivity) {
		ActivityTypeEnum type = serverActivity.getType();
		if (type != ActivityTypeEnum.ATE_DailyGift && type != ActivityTypeEnum.ATE_WeeklyGift && type != ActivityTypeEnum.ATE_MonthlyGift) {
			return getTimeBuilderByServerActivity(serverActivity);
		}

		return getTimeBuilderByTimeCycleActivity(dbActivity, serverActivity.getActivityId());
	}

	private ActivityTime.Builder getTimeBuilderByTimeCycleActivity(DB_Activity.Builder dbActivity, long activityId) {
		int cycleDays = getCycleDaysByActivityId(activityId);

		ActivityTime.Builder timeBuilder = ActivityTime.newBuilder();
		long nextRefresh = dbActivity.getNextCanUpdateCumuLogTime();
		if (nextRefresh <= 0) {
			nextRefresh = TimeUtil.getNextDaysResetTime(GlobalTick.getInstance().getCurrentTime(), cycleDays);
			dbActivity.setNextCanUpdateCumuLogTime(nextRefresh);
		}
		timeBuilder.setTimeType(CycleTypeEnum.CTE_TimeLimit);
		timeBuilder.setTimeContent(Cycle_TimeLimit.newBuilder().setBeginTimestamp(nextRefresh - cycleDays * TimeUtil.MS_IN_A_DAY).setEndTimestamp(nextRefresh).build().toByteString());
		return timeBuilder;
	}

	private ActivityTime.Builder getTimeBuilderByServerActivity(ServerActivity serverActivity) {
		ActivityTime.Builder timeBuilder = ActivityTime.newBuilder();
		timeBuilder.setTimeType(CycleTypeEnum.CTE_TimeLimit);
		timeBuilder.setTimeContent(Cycle_TimeLimit.newBuilder().setBeginTimestamp(serverActivity.getBeginTime()).setEndTimestamp(serverActivity.getEndTime()).build().toByteString());
		return timeBuilder;
	}

	private ActivityTypeEnum getClientActivityType(ServerActivity serverActivity) {
		ActivityTypeEnum type = serverActivity.getType();
		if (ActivityTypeEnum.ATE_DailyGift == type || ActivityTypeEnum.ATE_WeeklyGift == type || ActivityTypeEnum.ATE_MonthlyGift == type) {
			return ActivityTypeEnum.ATE_BuyItem;
		}
		return type;
	}

	private void combineRankingReward(ServerActivity serverActivity, ClientActivity.Builder builder) {
//        if (ActivityTypeEnum.ATE_Ranking != serverActivity.getType()) {
//            return;
//        }
		builder.setRankingType(serverActivity.getRankingType());
		if (serverActivity.getRankingRewardCount() > 0) {
			builder.addAllRankingReward(serverActivity.getRankingRewardList());
		}
	}

	private void combineBuyMission(ServerActivity serverActivity, ClientActivity.Builder builder) {
		if (serverActivity.getBuyMissionCount() <= 0) {
			return;
		}
		Map<Integer, Server.ServerBuyMission> exMissionMap = serverActivity.getBuyMissionMap();
		for (Server.ServerBuyMission value : exMissionMap.values()) {

			builder.addBuyItemLists(serverBuyMission2ClientBuyMission(serverActivity, value));
		}
	}

	public ClientSubBuyItem.Builder serverBuyMission2ClientBuyMission(ServerActivity serverActivity, Server.ServerBuyMission value) {
		ClientSubBuyItem.Builder item = ClientSubBuyItem.newBuilder();
		item.setIndex(value.getIndex());
		item.setLimitBuy(value.getLimitBuy());
		item.setEndTimestamp(value.getEndTimestamp());
		item.setPrice(value.getPrice());
		item.setDiscount(value.getDiscount());
		item.addAllRewards(value.getRewardsList());
		item.setTitle(GameUtil.getLanguageStr(value.getTitle(), PlayerUtil.queryPlayerLanguage(getLinkplayeridx())));
		item.setSpeicalType(value.getSpecialType());
		item.setAlreadyBuyTimes(getActivityItemBuyTimes(serverActivity, value.getIndex()));
		return item;
	}

	private void combineExMission(ServerActivity serverActivity, ClientActivity.Builder builder) {
		if (serverActivity.getExMissionCount() <= 0) {
			return;
		}
		Map<Integer, ServerExMission> exMissionMap = serverActivity.getExMissionMap();
		for (ServerExMission value : exMissionMap.values()) {
			ClientSubExchange.Builder exMission = ClientSubExchange.newBuilder();
			exMission.setIndex(value.getIndex());
			exMission.setExchangeLimit(value.getExchangeLimit());
			exMission.setEndTimestamp(value.getEndTimestamp());
			exMission.addAllExchangeTarget(value.getRewardsList());
			exMission.addAllSlots(value.getExSlotsList());
			exMission.setVisualFlag(value.getVisualFlag());

			TargetMission.Builder dbActivityPro = getDBActivityProBuilder(serverActivity.getActivityId(), value.getIndex());
			exMission.setExchangeLimitPlayerPro(dbActivityPro.getProgress());
			exMission.setStatus(dbActivityPro.getStatus());

			builder.addExchangeLists(exMission.build());
		}
	}

	private void combineMission(ServerActivity serverActivity, LanguageEnum language, ClientActivity.Builder builder) {
		if (serverActivity.getMissionsCount() <= 0 || serverActivity.getType() == ActivityTypeEnum.ATE_MistGhostBuster) {
			return;
		}
		Map<Integer, ServerSubMission> missionsMap = serverActivity.getMissionsMap();
		for (ServerSubMission value : missionsMap.values()) {
			ClientSubMission.Builder subMission = ClientSubMission.newBuilder();
			subMission.setIndex(value.getIndex());
			subMission.setTarget(value.getTarget());
			subMission.setDesc(GameUtil.getLanguageStr(value.getDesc(), language));
			subMission.addAllReward(value.getRewardList());
			subMission.addAllRandoms(value.getRandomsList());
			subMission.setEndTimestamp(value.getEndTimestamp());
			subMission.setTargetType(value.getSubType());

			TargetMission.Builder dbActivityPro = getDBActivityProBuilder(serverActivity.getActivityId(), value.getIndex());
			subMission.setPlayerPro(dbActivityPro.getProgress());
			subMission.setStatus(dbActivityPro.getStatus());

			builder.addMissionLists(subMission.build());
		}
	}

	private CycleTypeEnum getTypeByActivity(ServerActivity serverActivity) {
		if (serverActivity == null) {
			return CycleTypeEnum.UNRECOGNIZED;
		}
		switch (serverActivity.getType()) {
		case ATE_DailyGift:
			return CycleTypeEnum.CTE_CycleDay;
		case ATE_WeeklyGift:
			return CycleTypeEnum.CTE_Week;
		case ATE_MonthlyGift:
			return CycleTypeEnum.CTE_Month;
		default:
			return CycleTypeEnum.CTE_TimeLimit;

		}
	}

	public void doTargetPro(TargetTypeEnum typeEnum, int addPro, int param) {
		if (typeEnum == null || typeEnum == TargetTypeEnum.TTE_NULL || addPro <= 0) {
			return;
		}
		//累计登陆每天只能触发一次  //todo 如果当日触发活动后再触发登录就没法触发活动的登录任务了,现在来不及改了,临时处理
/*		if (TargetTypeEnum.TTE_CumuLogin == typeEnum) {
			if (getDb_Builder().getTodayDoLoginTargetPro()) {
				return;
			}
			getDb_Builder().setTodayDoLoginTargetPro(true);
		}*/

		// 迷雾深林阶乘大于等于1000不处理
		if (typeEnum == TargetTypeEnum.TTE_MistLevel && addPro >= 1000) {
			return;
		}
		doKeyNodeMission(typeEnum, addPro, param);
		doDailyMission(typeEnum, addPro, param);
		doAchievement(typeEnum, addPro, param);
		doActivity(typeEnum, addPro, param);
		doSpecialActivity(typeEnum, addPro, param);
		doMistSeasonTask(typeEnum, addPro, param);
		doGrowthTrack(typeEnum, addPro, param);
		doArenaMission(typeEnum, addPro, param);
		doDemonDescends(typeEnum, addPro, param);
		doHadesTreasure(typeEnum, addPro, param);
		doRuneTreasure(typeEnum, addPro, param);
		doStoneRiftMission(typeEnum, addPro, param);
		doStoneRiftAchievement(typeEnum, addPro, param);
//        doTheWarSeasonMission(typeEnum, addPro, param);
		doPatrolTask(typeEnum, addPro, param);
		doBusinessPopup(typeEnum, addPro, param);
		doMistTask(typeEnum, addPro, param);
		doStoreMission(typeEnum, addPro, param);
		doMatchArenaTaskMission(typeEnum, addPro, param);
		doCrossArenaTaskMission(typeEnum, addPro, param);
		doTrainingMapTask(typeEnum, addPro, param);
	}

	private void doTrainingMapTask(TargetTypeEnum typeEnum, int addPro, int param) {
		if (param <= 0) {
			return;
		}
		trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(getLinkplayeridx());
		if (cache == null || cache.getInfoDB().containsEndMap(param)) {
			return;
		}
		TrainingMapObject mapCfg = TrainingMap.getByMapid(param);
		if (mapCfg == null) {
			return;
		}
		if (mapCfg.getScoretasklist() == null || mapCfg.getScoretasklist().length < 1) {
			return;
		}
		TrainingMapTaskData.Builder mapTaskData = null;
		for (TrainingMapTaskData.Builder mapData : getDb_Builder().getTrainingTaskDataBuilder().getMapTaskDataBuilderList()) {
			if (mapData.getMapId() == param) {
				mapTaskData = mapData;
				break;
			}
		}
		MissionObject mission;
		List<TargetMission> updateList = null;
		for (Integer missionId : mapCfg.getScoretasklist()) {
			mission = Mission.getById(missionId);
			if (mission == null) {
				continue;
			}
			if (mission.getMissiontype() != typeEnum.getNumber()) {
				continue;
			}
			if (mission.getAddtion() > 0 && mission.getAddtion() != param) {
				continue;
			}
			boolean existFlag = false;
			if (mapTaskData == null) {
				mapTaskData = TrainingMapTaskData.newBuilder();
				mapTaskData.setMapId(param);
				getDb_Builder().getTrainingTaskDataBuilder().addMapTaskData(mapTaskData);
				existFlag = true;
			}

			TargetMission.Builder targetBuilder = null;
			if (!existFlag) {
				for (TargetMission.Builder tmpTaskData : mapTaskData.getTrainTaskBuilderList()) {
					if (tmpTaskData.getCfgId() == missionId) {
						targetBuilder = tmpTaskData;
						break;
					}
				}
			}
			boolean initFlag = false;
			if (targetBuilder == null) {
				targetBuilder = TargetMission.newBuilder().setCfgId(mission.getId());
				initFlag = true;
			} else if (targetBuilder.getStatus() != MissionStatusEnum.MSE_UnFinished) {
				continue;
			}
			int newProgress;
			if (isIncrementType(typeEnum)) {
				newProgress = targetBuilder.getProgress() + addPro;
			} else {
				newProgress = addPro;
			}
			targetBuilder.setProgress(newProgress);
			if (newProgress >= mission.getTargetcount()) {
				targetBuilder.setProgress(mission.getTargetcount());
				targetBuilder.setStatus(MissionStatusEnum.MSE_Finished);
				if (updateList == null) {
					updateList = new ArrayList<>();
				}
				updateList.add(targetBuilder.build());
			}
			if (initFlag) {
				mapTaskData.addTrainTask(targetBuilder);
			}
		}

		if (updateList != null) {
			SC_TrainUpdateTask.Builder builder = SC_TrainUpdateTask.newBuilder().setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
			builder.addAllTasks(updateList);
			GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_TrainUpdateTask_VALUE, builder);
		}
	}

	private void doStoreMission(TargetTypeEnum typeEnum, int addPro, int param) {
		List<MissionObject> missions = StoreManager.getInstance().getStoreMission().get(typeEnum.getNumber());
		if (CollectionUtils.isEmpty(missions)) {
			return;
		}
		List<Integer> completeMissionIds = new ArrayList<>();
		for (MissionObject missionCfg : missions) {

			TargetMission beforeMission = getDb_Builder().getStoreMissionMap().get(missionCfg.getId());
			TargetMission afterTargetMission = addMissionPro(missionCfg.getId(), typeEnum, missionCfg.getAddtion(), missionCfg.getTargetcount(), typeEnum, addPro, param, beforeMission);

			if (missionUpdate(beforeMission, afterTargetMission)) {
				getDb_Builder().putStoreMission(missionCfg.getId(), afterTargetMission);
				if (MissionStatusEnum.MSE_Finished == afterTargetMission.getStatus()) {
					completeMissionIds.add(missionCfg.getId());
				}
			}
		}
		if (!CollectionUtils.isEmpty(completeMissionIds)) {
			sendAddCompleteShopMission(completeMissionIds);
		}

	}

	private void doStoneRiftAchievement(TargetTypeEnum typeEnum, int addPro, int param) {
		List<MissionObject> missions = StoneRiftCfgManager.getInstance().getAchievementMissions();
		if (CollectionUtils.isEmpty(missions)) {
			return;
		}
		List<TargetMission> updateMissions = new ArrayList<>();
		for (MissionObject missionCfg : missions) {
            if (missionCfg.getMissiontype() != typeEnum.getNumber()) {
                continue;
            }
			TargetMission beforeMission = getDb_Builder().getStoneRiftAchievementMap().get(missionCfg.getId());
			TargetMission afterTargetMission = addMissionPro(missionCfg.getId(), typeEnum, missionCfg.getAddtion(), missionCfg.getTargetcount(), typeEnum, addPro, param, beforeMission);

			if (missionUpdate(beforeMission, afterTargetMission)) {
				getDb_Builder().putStoneRiftAchievement(missionCfg.getId(), afterTargetMission);
				updateMissions.add(afterTargetMission);
			}
		}
		if (CollectionUtils.isNotEmpty(updateMissions)) {
			EventUtil.triggerUpdateStoneRiftAchievement(getLinkplayeridx(), updateMissions);
		}

	}
	private void doStoneRiftMission(TargetTypeEnum typeEnum, int addPro, int param) {
		List<MissionObject> missions = StoneRiftCfgManager.getInstance().getUnlockMissions();
		if (CollectionUtils.isEmpty(missions)) {
			return;
		}
		boolean change = false;
		for (MissionObject missionCfg : missions) {
            if (missionCfg.getMissiontype() != typeEnum.getNumber()) {
                continue;
            }

			TargetMission beforeMission = getDb_Builder().getStoneRiftMissionMap().get(missionCfg.getId());
			TargetMission afterTargetMission = addMissionPro(missionCfg.getId(), typeEnum, missionCfg.getAddtion(), missionCfg.getTargetcount(), typeEnum, addPro, param, beforeMission);

			if (missionUpdate(beforeMission, afterTargetMission)) {
				change = true;
				getDb_Builder().putStoneRiftMission(missionCfg.getId(), afterTargetMission);
			}
		}
		if (!change&&PlayerUtil.queryFunctionLock(getIdx(),EnumFunction.StoneRift)) {
			return;
		}
		triggerStoneRiftUnlock();
	}

	private void triggerStoneRiftUnlock() {
		List<Integer> completeMissions = getDb_Builder().getStoneRiftMissionMap().values()
				.stream().filter(e -> MissionStatusEnum.MSE_Finished == e.getStatus())
				.map(TargetMission::getCfgId).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(completeMissions)) {
			EventUtil.triggerCompleteStoneRiftUnlockMission(getLinkplayeridx(), completeMissions);
		}
	}


	public Set<Integer> getCompleteMissionIds() {
		return getDb_Builder().getStoreMissionMap().values().stream().filter(e -> e.getStatus() == Common.MissionStatusEnum.MSE_Finished).map(TargetMission::getCfgId).collect(Collectors.toSet());
	}

	private void sendAddCompleteShopMission(List<Integer> completeMissionIds) {
		Shop.SC_AddCompleteShopMission.Builder msg = Shop.SC_AddCompleteShopMission.newBuilder();
		msg.addAllMissionId(completeMissionIds);
		GlobalData.getInstance().sendMsg(linkplayeridx, SC_AddCompleteShopMission_VALUE, msg);
	}


	public MistSweepTaskDbData.Builder getCurSweepMistTaskData() {
		int curEnterLevel = getDb_Builder().getMistTaskDataBuilder().getCurEnterLevel();
		if (curEnterLevel <= 1 && !isFinishedMistNewbieTask()) {
			return null;
		}

		if (getDb_Builder().getMistTaskDataBuilder().getSweepTaskDbDataBuilder().getMistLevel() != curEnterLevel) {
			return null;
		}
		return getDb_Builder().getMistTaskDataBuilder().getSweepTaskDbDataBuilder();
	}

	public MistUnlockTaskDbData.Builder getCurUnlockTaskData() {
		int curEnterLevel = getDb_Builder().getMistTaskDataBuilder().getCurEnterLevel();
		for (MistUnlockTaskDbData.Builder taskBuilder : getDb_Builder().getMistTaskDataBuilder().getUnlockTaskDbDataBuilderList()) {
			if (taskBuilder.getMistLevel() == curEnterLevel) {
				return taskBuilder;
			}
		}
		return null;
	}

	public MistUnlockTaskDbData.Builder initUnlockTaskData() {
		MissionObject missionCfg;
		MistUnlockTaskDbData.Builder curTaskBuilder = null;
		MistWorldMapConfigObject mapCfg;
		boolean bAdd = getDb_Builder().getMistTaskDataBuilder().getUnlockTaskDbDataCount() <= 0;
		for (MistWordMapInfoConfigObject cfg : MistWordMapInfoConfig._ix_mapid.values()) {
			mapCfg = MistWorldMapConfig.getByMapid(cfg.getMapid());
			if (mapCfg == null) {
				continue;
			}
			MistUnlockTaskDbData.Builder taskBuilder = MistUnlockTaskDbData.newBuilder();
			taskBuilder.setMistLevel(mapCfg.getLevel());
			for (int i = 0; i < cfg.getUnlockcondition().length; i++) {
				int cfgId = cfg.getUnlockcondition()[i];
				missionCfg = Mission.getById(cfgId);
				if (missionCfg == null) {
					continue;
				}
				TargetMission.Builder target = TargetMission.newBuilder();
				target.setCfgId(cfgId);
				taskBuilder.addUnlockMissions(target);
			}
			if (MistWorldMapConfig.getInstance().getDefaultCommonMistLevel() == mapCfg.getLevel()) {
				curTaskBuilder = taskBuilder;
			}
			if (bAdd) {
                getDb_Builder().getMistTaskDataBuilder().addUnlockTaskDbData(taskBuilder);
            }
		}
		return curTaskBuilder;
	}

	public MistSweepTaskDbData.Builder initNewLevelSweepTaskData(int enterLevel) {
		MistSweepTaskDbData.Builder curTaskBuilder = null;
		MistWorldMapConfigObject mapCfg;
		for (MistWordMapInfoConfigObject cfg : MistWordMapInfoConfig._ix_mapid.values()) {
			mapCfg = MistWorldMapConfig.getByMapid(cfg.getMapid());
			if (mapCfg == null) {
				continue;
			}
			if (mapCfg.getLevel() != enterLevel) {
				continue;
			}
			curTaskBuilder = MistSweepTaskDbData.newBuilder();
			curTaskBuilder.setMistLevel(enterLevel);
			if (cfg.getSweepmissions() == null || cfg.getSweepmissions().length < 0) {
				curTaskBuilder.setSweepMissionState(MissionStatusEnum.MSE_FinishedAndClaim);
			} else {
				for (int i = 0; i < cfg.getSweepmissions().length; i++) {
					if (cfg.getSweepmissions()[i] == null ||cfg.getSweepmissions()[i].length < 2) {
						continue;
					}
					if (Mission.getById(cfg.getSweepmissions()[i][0]) == null) {
						continue;
					}
					if (curTaskBuilder.getCurTargetBuilder().getCfgId() <= 0) {
						curTaskBuilder.getCurTargetBuilder().setCfgId(cfg.getSweepmissions()[i][0]);
						curTaskBuilder.setCurTaskIndex(i);
					} else if (cfg.getSweepmissions()[i][1] > 0) {
						curTaskBuilder.addExtTaskData(TargetMission.newBuilder().setCfgId(cfg.getSweepmissions()[i][0]));
					}
				}
			}
			getDb_Builder().getMistTaskDataBuilder().setSweepTaskDbData(curTaskBuilder);
			break;
		}
		return curTaskBuilder;
	}

	public void sendMistSweepTaskData() {
		SC_UpdateMistSweepTask.Builder builder = SC_UpdateMistSweepTask.newBuilder();
		int curEnterLevel = getDb_Builder().getMistTaskData().getCurEnterLevel();
		int curEnterMapId = MistWorldMapConfig.getInstance().getMapIdByRuleAndLevel(EnumMistRuleKind.EMRK_Common_VALUE, curEnterLevel);
		builder.setCurEnterMistMapId(curEnterMapId);
		MistSweepTaskDbData.Builder curTaskBuilder = getCurSweepMistTaskData();
		if (curTaskBuilder != null) {
			builder.setRewardMissionState(curTaskBuilder.getSweepMissionState());
			builder.setCurIndex(curTaskBuilder.getCurTaskIndex());
			builder.setCurSweepMission(curTaskBuilder.getCurTarget());
		}
		MistWordMapInfoConfigObject cfg = MistWordMapInfoConfig.getByMapid(curEnterMapId);
		if (cfg != null && cfg.getSweepmissions() != null) {
			builder.setTotalSize(cfg.getSweepmissions().length);
		}
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_UpdateMistSweepTask_VALUE, builder);
	}

	public void updateMistTargetMissionWithLevelCheck(int newMistLevel) {
		if (getDb_Builder().getMistTaskData().getTargetMissionDb().getMistLevel() < newMistLevel) {
			settleMistTargetMissionReward(newMistLevel);
		}
		updateMistTargetMission();
	}

	public void updateMistTargetMission() {
		SC_UpdateMistTargetMission.Builder builder = SC_UpdateMistTargetMission.newBuilder();
		for (TargetMission targetMission : getDb_Builder().getMistTaskData().getTargetMissionDb().getTargetMissionList()) {
			builder.addTargetMission(targetMission);
		}
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_UpdateMistTargetMission_VALUE, builder);
	}

	public boolean isFinishedMistNewbieTask() {
		return getDb_Builder().getMistTaskData().getCurNewbieTask().getCfgId() > 0 && (getDb_Builder().getMistTaskData().getCurNewbieTask().getStatus() == MissionStatusEnum.MSE_Finished
				|| getDb_Builder().getMistTaskData().getCurNewbieTask().getStatus() == MissionStatusEnum.MSE_FinishedAndClaim);
	}

	public void updateMistNewbieTask() {
		if (getDb_Builder().getMistTaskData().getCurEnterLevel() > MistWorldMapConfig.getInstance().getDefaultCommonMistLevel() || isFinishedMistNewbieTask()) {
			return;
		}
		SC_UpdateMistNewbieTask.Builder builder = SC_UpdateMistNewbieTask.newBuilder();
		builder.setHasAcceptedTask(getDb_Builder().getMistTaskData().getCurNewbieTask().getCfgId() > 0);
		builder.setTaskData(getDb_Builder().getMistTaskData().getCurNewbieTask());
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_UpdateMistNewbieTask_VALUE, builder);
	}

	protected void settleMistTargetMissionReward(int newMistLevel) {
		if (getDb_Builder().getMistTaskData().getTargetMissionDb().getMistLevel() == newMistLevel) {
			return;
		}
		playerEntity player = playerCache.getByIdx(getLinkplayeridx());
		if (player == null) {
			return;
		}
		getDb_Builder().getMistTaskDataBuilder().getTargetMissionDbBuilder().setMistLevel(newMistLevel);
		MissionObject missionCfg;
		List<Reward> rewardList = null;
		for (TargetMission.Builder targetBuilder : getDb_Builder().getMistTaskDataBuilder().getTargetMissionDbBuilder().getTargetMissionBuilderList()) {
			if (targetBuilder.getStatus() != MissionStatusEnum.MSE_Finished) {
				continue;
			}
			missionCfg = Mission.getById(targetBuilder.getCfgId());
			if (missionCfg == null) {
				continue;
			}
			List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward());
			if (!CollectionUtils.isEmpty(rewards)) {
				if (rewardList == null) {
					rewardList = new ArrayList<>();
				}
				rewardList.addAll(rewards);
			}
			targetBuilder.setStatus(MissionStatusEnum.MSE_FinishedAndClaim);
		}
		getDb_Builder().getMistTaskDataBuilder().getTargetMissionDbBuilder().clearTargetMission();
		if (rewardList != null) {
			Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistForest);
			EventUtil.triggerAddMailEvent(player.getIdx(), MailTemplateUsed.getById(GameConst.CONFIG_ID).getMisttargetmission(), rewardList, reason);
		}
	}

	protected boolean isMistTargetType(TargetTypeEnum typeEnum) {
		switch (typeEnum.getNumber()) {
			case TargetTypeEnum.TTE_MistLevel_VALUE:
			case TargetTypeEnum.TTE_MistContinuousKillPlayer_VALUE:
			case TargetTypeEnum.TTE_Mist_CumuEnterMist_VALUE:
			case TargetTypeEnum.TTE_Mist_CumuKillMonster_VALUE:
			case TargetTypeEnum.TTE_MistSeasonTask_GainBagCount_VALUE:
			case TargetTypeEnum.TTE_MistSeasonTask_OpenBoxCount_VALUE:
			case TargetTypeEnum.TTE_MistSeasonTask_UseItemCount_VALUE:
			case TargetTypeEnum.TTE_MistSeasonTask_KillBossCount_VALUE:
			case TargetTypeEnum.TTE_Mist_CumuFormATeam_VALUE:
			return true;
		}
		return typeEnum.getNumber() >= TargetTypeEnum.TTE_Mist_PickUpMistBox_VALUE && typeEnum.getNumber() <= TargetTypeEnum.TTE_Mist_Tmp_VALUE;
	}

	public void doMistNewbieTask(TargetTypeEnum typeEnum, int addPro, int param) {
		if (typeEnum == null || addPro <= 0) {
			return;
		}
		if (!isMistTargetType(typeEnum)) {
			return;
		}
		if (getDb_Builder().getMistTaskData().getCurEnterLevel() > MistWorldMapConfig.getInstance().getDefaultCommonMistLevel()) {
			return;
		}

		TargetMission.Builder curTaskBuilder = getDb_Builder().getMistTaskDataBuilder().getCurNewbieTaskBuilder();
		if (curTaskBuilder.getCfgId() <= 0) {
			return;
		}
		if (curTaskBuilder.getStatus() != MissionStatusEnum.MSE_UnFinished) {
			return;
		}
		MistNewbieTaskConfigObject cfg = MistNewbieTaskConfig.getById(curTaskBuilder.getCfgId());
		if (cfg == null) {
			return;
		}
		MissionObject missionCfg = Mission.getById(cfg.getMissionid());
		if (missionCfg == null) {
			return;
		}
		if (missionCfg.getMissiontype() != typeEnum.getNumber()) {
			return;
		}

		if (missionCfg.getAddtion() > 0 && param != missionCfg.getAddtion()) {
			return;
		}
		int newProgress;
		if (isIncrementType(typeEnum)) {
			newProgress = curTaskBuilder.getProgress() + addPro;
		} else {
			newProgress = addPro;
		}
		curTaskBuilder.setProgress(newProgress);
		if (newProgress >= missionCfg.getTargetcount()) {
			MistNewbieTaskConfigObject nextTaskCfg = MistNewbieTaskConfig.getById(curTaskBuilder.getCfgId() + 1); // 按顺序
			if (nextTaskCfg != null) {
				curTaskBuilder.clear();
				curTaskBuilder.setCfgId(nextTaskCfg.getId());

				if (nextTaskCfg.getUnittype() > 0) {
					GS_CS_NewbieTaskCreateObj.Builder transBuilder = GS_CS_NewbieTaskCreateObj.newBuilder();
					transBuilder.setPlayerIdx(getLinkplayeridx());
					transBuilder.setNewbieTaskId(nextTaskCfg.getId());
					CrossServerManager.getInstance().sendMsgToMistForest(getLinkplayeridx(), MsgIdEnum.GS_CS_NewbieTaskCreateObj_VALUE, transBuilder, true);
				}
			} else {
				curTaskBuilder.setProgress(missionCfg.getTargetcount());
				curTaskBuilder.setStatus(MissionStatusEnum.MSE_Finished);
			}
		}
		SC_UpdateMistNewbieTask.Builder builder = SC_UpdateMistNewbieTask.newBuilder();
		builder.setHasAcceptedTask(true);
		builder.setTaskData(curTaskBuilder);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_UpdateMistNewbieTask_VALUE, builder);
	}

	public void doMistTargetTask(TargetTypeEnum typeEnum, int addPro, int param) {
		if (typeEnum == null || addPro <= 0) {
			return;
		}
		if (!isMistTargetType(typeEnum)) {
			return;
		}

		int curMistLevel = getDb_Builder().getMistTaskDataBuilder().getTargetMissionDbBuilder().getMistLevel();
		MistWorldMapConfigObject mapCfg = MistWorldMapConfig.getInstance().getCfgByRuleAndLevel(EnumMistRuleKind.EMRK_Common_VALUE, curMistLevel);
		if (mapCfg == null) {
			return;
		}
		MistWordMapInfoConfigObject mapInfoCfg = MistWordMapInfoConfig.getByMapid(mapCfg.getMapid());
		if (mapInfoCfg == null || mapInfoCfg.getTargetmission() == null) {
			return;
		}
		MissionObject missionCfg;
		List<TargetMission> updateTaskList = null;
		for (int i = 0; i < mapInfoCfg.getTargetmission().length; i++) {
			missionCfg = Mission.getById(mapInfoCfg.getTargetmission()[i]);
			if (missionCfg == null) {
				continue;
			}
			if (missionCfg.getMissiontype() != typeEnum.getNumber()) {
				continue;
			}
			if (missionCfg.getAddtion() > 0 && param != missionCfg.getAddtion()) {
				continue;
			}
			boolean initFlag = false;
			TargetMission.Builder targetBuilder = null;
			for (TargetMission.Builder mission : getDb_Builder().getMistTaskDataBuilder().getTargetMissionDbBuilder().getTargetMissionBuilderList()) {
				if (mission.getCfgId() == missionCfg.getId()) {
					targetBuilder = mission;
					initFlag = true;
					break;
				}
			}
			if (targetBuilder == null) {
				targetBuilder = TargetMission.newBuilder().setCfgId(missionCfg.getId());
			} else if (targetBuilder.getStatus() != MissionStatusEnum.MSE_UnFinished) {
				continue;
			}
			int newProgress;
			if (isIncrementType(typeEnum)) {
				newProgress = targetBuilder.getProgress() + addPro;
			} else {
				newProgress = addPro;
			}
			targetBuilder.setProgress(newProgress);
			if (newProgress >= missionCfg.getTargetcount()) {
				targetBuilder.setProgress(missionCfg.getTargetcount());
				targetBuilder.setStatus(MissionStatusEnum.MSE_Finished);
			}
			if (!initFlag) {
				getDb_Builder().getMistTaskDataBuilder().getTargetMissionDbBuilder().addTargetMission(targetBuilder);
			}
			if (updateTaskList == null) {
				updateTaskList = new ArrayList<>();
			}
			updateTaskList.add(targetBuilder.build());
		}
		if (!CollectionUtils.isEmpty(updateTaskList)) {
			SC_UpdateMistTargetMission.Builder builder = SC_UpdateMistTargetMission.newBuilder();
			builder.addAllTargetMission(updateTaskList);
			GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_UpdateMistTargetMission_VALUE, builder);
		}
	}

	private void doMistUnlockTask(TargetTypeEnum typeEnum, int addPro, int param) {
		if (typeEnum == null || addPro <= 0) {
			return;
		}
		MissionObject missionCfg;
		MistUnlockTaskDbData.Builder curUnlockTaskData = getCurUnlockTaskData();
		if (curUnlockTaskData == null) {
			curUnlockTaskData = initUnlockTaskData();
			if (curUnlockTaskData == null) {
				LogUtil.error("MistTask init failed, playerIdx=" + getLinkplayeridx());
				return;
			}
		}

		int curEnterLevel = getDb_Builder().getMistTaskDataBuilder().getCurEnterLevel();
		for (MistUnlockTaskDbData.Builder taskBuilder : getDb_Builder().getMistTaskDataBuilder().getUnlockTaskDbDataBuilderList()) {
		    if (curEnterLevel > taskBuilder.getMistLevel()) {
		    	continue;
			}
		    if (taskBuilder.getUnlockMissionState() == MissionStatusEnum.MSE_Finished) {
		    	continue;
			}
		    boolean taskAllFinished = true;
			for (TargetMission.Builder target : taskBuilder.getUnlockMissionsBuilderList()) {
				missionCfg = Mission.getById(target.getCfgId());
				if (missionCfg == null) {
					continue;
				}
				if (missionCfg.getMissiontype() == typeEnum.getNumber() && (missionCfg.getAddtion() == 0 || (missionCfg.getAddtion() > 0 && param == missionCfg.getAddtion()))) {
					int newProgress;
					if (isIncrementType(typeEnum)) {
						newProgress = target.getProgress() + addPro;
					} else {
						newProgress = addPro;
					}
					target.setProgress(newProgress);
					if (newProgress >= missionCfg.getTargetcount()) {
						target.setProgress(missionCfg.getTargetcount());
						target.setStatus(MissionStatusEnum.MSE_Finished);
					}
				}
				if (target.getStatus() == MissionStatusEnum.MSE_UnFinished) {
					taskAllFinished = false;
				}
			}
			if (taskAllFinished) {
				taskBuilder.setUnlockMissionState(MissionStatusEnum.MSE_Finished);
				if (curEnterLevel < taskBuilder.getMistLevel()) {
					curEnterLevel = taskBuilder.getMistLevel();
				}
				getDb_Builder().getMistTaskDataBuilder().setCurEnterLevel(curEnterLevel);
                sendMistSweepTaskData();
			}
		}
	}

	private void doMistSweepTask(TargetTypeEnum typeEnum, int addPro, int param) {
		if (typeEnum == null || addPro <= 0) {
			return;
		}
		int curEnterLevel = getDb_Builder().getMistTaskData().getCurEnterLevel();
		if (curEnterLevel <= MistWorldMapConfig.getInstance().getDefaultCommonMistLevel() && !isFinishedMistNewbieTask()) {
			return;
		}
		MistWorldMapConfigObject mapCfg = MistWorldMapConfig.getInstance().getCfgByRuleAndLevel(EnumMistRuleKind.EMRK_Common_VALUE, curEnterLevel);
		if (mapCfg == null) {
			return;
		}
		MistWordMapInfoConfigObject cfg = MistWordMapInfoConfig.getByMapid(mapCfg.getMapid());
		if (cfg == null) {
			return;
		}
		boolean updateFlag = false;
		MistSweepTaskDbData.Builder curTaskBuilder = getCurSweepMistTaskData();
		if (curTaskBuilder == null) {
			curTaskBuilder = initNewLevelSweepTaskData(curEnterLevel);
			updateFlag = true;
		}
		if (curTaskBuilder == null || curTaskBuilder.getSweepMissionState() != MissionStatusEnum.MSE_UnFinished) {
			return;
		}

		// 先处理累积的任务
		MissionObject missionCfg = null;
		for (TargetMission.Builder extTaskBuilder : curTaskBuilder.getExtTaskDataBuilderList()) {
		    missionCfg = Mission.getById(extTaskBuilder.getCfgId());
		    if (missionCfg == null) {
		    	continue;
			}
		    if (extTaskBuilder.getStatus() != MissionStatusEnum.MSE_UnFinished) {
		    	continue;
			}
			if (missionCfg.getMissiontype() == typeEnum.getNumber() && (missionCfg.getAddtion() == 0 || (missionCfg.getAddtion() > 0 && param == missionCfg.getAddtion()))) {
				int newProgress;
				if (isIncrementType(typeEnum)) {
					newProgress = extTaskBuilder.getProgress() + addPro;
				} else {
					newProgress = addPro;
				}
				extTaskBuilder.setProgress(newProgress);
				if (newProgress >= missionCfg.getTargetcount()) {
					extTaskBuilder.setProgress(missionCfg.getTargetcount());
					extTaskBuilder.setStatus(MissionStatusEnum.MSE_Finished);
				}
			}
		}


		boolean nextTaskFlag = false; // 判断是否需要更新到下一个任务
		int curIndex = curTaskBuilder.getCurTaskIndex();
		TargetMission.Builder curTarget = curTaskBuilder.getCurTargetBuilder();
		missionCfg = Mission.getById(curTarget.getCfgId());
		if (missionCfg != null) {
			if (curTarget.getStatus() == MissionStatusEnum.MSE_UnFinished) {
				if (missionCfg.getMissiontype() == typeEnum.getNumber() && (missionCfg.getAddtion() == 0 || (missionCfg.getAddtion() > 0 && param == missionCfg.getAddtion()))) {
					int newProgress;
					if (isIncrementType(typeEnum)) {
						newProgress = curTarget.getProgress() + addPro;
					} else {
						newProgress = addPro;
					}
					curTarget.setProgress(newProgress);
					if (newProgress >= missionCfg.getTargetcount()) {
						curTarget.setProgress(missionCfg.getTargetcount());
						curTarget.setStatus(MissionStatusEnum.MSE_Finished);
					}
					updateFlag = true;
				}
			}
		} else {
			nextTaskFlag = true;
		}

		// 当前任务完成或任务配置错误直接更新到下一任务
		if (nextTaskFlag || curTarget.getStatus() == MissionStatusEnum.MSE_Finished || curTarget.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
			List<Reward> rewards = null;
			if (curTarget.getStatus() == MissionStatusEnum.MSE_Finished) {
				rewards = RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward());
			}

			if (++curIndex >= cfg.getSweepmissions().length) {
				curTaskBuilder.setSweepMissionState(MissionStatusEnum.MSE_Finished);
			} else {
				for (int i = curIndex; i < cfg.getSweepmissions().length; i++) {
					if (cfg.getSweepmissions()[i] == null || cfg.getSweepmissions()[i].length < 2) {
						continue;
					}
					missionCfg = Mission.getById(cfg.getSweepmissions()[i][0]);
					if (missionCfg == null) {
						continue;
					}

					TargetMission targetMission = null;
					for (TargetMission extTarget : curTaskBuilder.getExtTaskDataList()) {
					    if (extTarget.getCfgId() == missionCfg.getId()) {
							targetMission = extTarget;
					    	break;
						}
					}
					if (targetMission != null && targetMission.getStatus() == MissionStatusEnum.MSE_Finished) {
						List<Reward> tmpRewards = RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward());
						if (tmpRewards != null) {
							if (rewards == null) {
								rewards = tmpRewards;
							} else {
								rewards.addAll(tmpRewards);
							}
						}
						continue;
					}
					curTaskBuilder.setCurTaskIndex(i);
					curTaskBuilder.clearCurTarget();
					if (targetMission != null) {
						curTaskBuilder.setCurTarget(targetMission);
					} else {
						curTaskBuilder.getCurTargetBuilder().setCfgId(missionCfg.getId());
					}
					break;
				}
			}
			if (curTaskBuilder.getCurTaskIndex() >= cfg.getSweepmissions().length) {
				curTaskBuilder.setSweepMissionState(MissionStatusEnum.MSE_Finished);
			}
			updateFlag = true;
			if (rewards != null) {
				Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistForest);
				RewardManager.getInstance().doRewardByList(getLinkplayeridx(), rewards, reason, true);
			}
		}

		if (updateFlag) {
			sendMistSweepTaskData();
		}
	}

	private void doMistTask(TargetTypeEnum typeEnum, int addPro, int param) {
		doMistUnlockTask(typeEnum, addPro, param);
		doMistNewbieTask(typeEnum, addPro, param);
		doMistSweepTask(typeEnum, addPro, param); // 扫荡任务需在解锁任务和新手任务之后
		doMistTargetTask(typeEnum, addPro, param);
	}

	private void doKeyNodeMission(TargetTypeEnum typeEnum, int addPro, int param) {
		List<MissionObject> allMissions = MissionManager.getInstance().getKeyNodeMissionsByMissionType(typeEnum.getNumber());

		if (CollectionUtils.isEmpty(allMissions)) {
			return;
		}
		int curKeyNode = PlayerUtil.queryPlayerKeyNodeId(getLinkplayeridx());
		List<Integer> keyNodeMission = MissionManager.getInstance().getKeyNodeMissionsByMissionKeyNode(curKeyNode);
		List<TargetMission> modifyMissionList = new ArrayList<>();
		List<TargetMission> needPushToClientMission = new ArrayList<>();

		for (MissionObject missionCfg : allMissions) {

			TargetMission beforeMission = getDb_Builder().getKeyNodeMissionMap().get(missionCfg.getId());
			TargetMission afterTargetMission = addMissionPro(missionCfg.getId(), typeEnum, missionCfg.getAddtion(), missionCfg.getTargetcount(), typeEnum, addPro, param, beforeMission);

			if (missionUpdate(beforeMission, afterTargetMission)) {
				modifyMissionList.add(afterTargetMission);
				getDb_Builder().putKeyNodeMission(missionCfg.getId(), afterTargetMission);
				if (keyNodeMission.contains(missionCfg.getId())) {
					needPushToClientMission.add(afterTargetMission);
				}
			}
		}
		if (CollectionUtils.isNotEmpty(modifyMissionList)) {
			unlockFunctionByKeyNodeMissionComplete(getLinkplayeridx(), modifyMissionList);
		}
		if (CollectionUtils.isNotEmpty(needPushToClientMission)) {
			sendKeyNodeMissionUpdate(needPushToClientMission);
		}
	}

	private void unlockFunctionByKeyNodeMissionComplete(String playerIdx, List<TargetMission> modifyMissionList) {
		List<Integer> missionIds = modifyMissionList.stream().filter(e -> MissionStatusEnum.MSE_Finished == e.getStatus()).map(TargetMission::getCfgId).collect(Collectors.toList());
		playerEntity player = playerCache.getByIdx(playerIdx);
		if (player == null || CollectionUtils.isEmpty(missionIds)) {
			return;
		}
		player.unLockFunctionByMissionComplete(missionIds);
	}

	public void sendKeyNodeMissionUpdate(List<TargetMission> modifyMissionList) {
		if (CollectionUtils.isEmpty(modifyMissionList)) {
			return;
		}
		mainlineEntity mainline = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(getLinkplayeridx());
		if (mainline == null) {
			return;
		}
		mainline.sendKeyNodeMissionUpdate(modifyMissionList);
	}

	private void doBusinessPopup(TargetTypeEnum typeEnum, int addPro, int param) {
		if (typeEnum == null || addPro <= 0) {
			return;
		}
		PopupMissionObject mission = PopupMission.getInstance().findOne(typeEnum.getNumber(), addPro, param);
		if (mission == null) {
			return;
		}

		if (!canTriggerBusinessPopup(mission, addPro, param)) {
			return;
		}
		TargetSystem.BusinessPopupItem popupItem = buildNewPopup(mission);
		// save to db
		saveBusinessPopupRecord(popupItem);
		// send popup
		sendBusinessPopup(popupItem);
	}

	private void doPatrolTask(TargetTypeEnum typeEnum, int addPro, int param) {
		TargetSystemDB.DB_PatrolMission patrolMission = getDb_Builder().getPatrolMission();
		TargetMission before = patrolMission.getMission();
		if (before.getStatus() != MissionStatusEnum.MSE_UnFinished) {
			return;
		}
		if (patrolMission.getEndTime() > 0 && getPatrolMissionEndTime() < GlobalTick.getInstance().getCurrentTime()) {
			return;
		}
		MissionObject missionCfg = Mission.getById(before.getCfgId());
		if (missionCfg == null || missionCfg.getMissiontype() != typeEnum.getNumber()) {
			return;
		}

		TargetMission after = addMissionPro(missionCfg, typeEnum, addPro, param, before);
		if (missionUpdate(before, after)) {
			getDb_Builder().getPatrolMissionBuilder().setMission(after);
			sendPatrolMissionUpdate();
		}
	}

	public void sendPatrolMissionUpdate() {
		TargetSystemDB.DB_PatrolMission patrolMission = getDb_Builder().getPatrolMission();
		Patrol.SC_PatrolMissionUpdate.Builder msg = Patrol.SC_PatrolMissionUpdate.newBuilder().setEndTime(getPatrolMissionEndTime()).setUpdateMission(patrolMission.getMission());
		if (patrolMission.getMission().getStatus() == MissionStatusEnum.MSE_Finished) {
			msg.setRewardUp(patrolMission.getRewardUp());
		}

		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_PatrolMissionUpdate_VALUE, msg);
	}

	public long getPatrolMissionEndTime() {
		TargetSystemDB.DB_PatrolMission patrolMission = getDb_Builder().getPatrolMission();
		if (patrolMission.getPauseTime() > 0) {
			return GlobalTick.getInstance().getCurrentTime() - patrolMission.getPauseTime() + patrolMission.getEndTime();
		}
		return patrolMission.getEndTime();
	}

	/**
	 * ===============================竞技场任务 start==================================
	 */

	private void resetArenaDailyMission() {
		ArenaConfigObject arenaCfg = ArenaConfig.getById(GameConst.CONFIG_ID);
		if (arenaCfg == null) {
			return;
		}

		List<Reward> rewards = new ArrayList<>();
		for (int i : arenaCfg.getDailymission()) {
			MissionObject missionCfg = Mission.getById(i);
			if (missionCfg == null) {
				continue;
			}

			TargetMission missionPro = getArenaMission(i);
			// 完成自动领取
			if (missionPro != null && missionPro.getStatus() == MissionStatusEnum.MSE_Finished) {
				List<Reward> rewardList = RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward());
				if (CollectionUtils.isNotEmpty(rewardList)) {
					rewards.addAll(rewardList);
				}
			}

			// 移除进度
			getDb_Builder().getArenaMissionBuilder().removeMissions(i);
		}

		if (CollectionUtils.isNotEmpty(rewards)) {
			Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ArenaMission);
			EventUtil.triggerAddMailEvent(getLinkplayeridx(), MailTemplateUsed.getById(GameConst.CONFIG_ID).getArenamissionreissue(), rewards, reason);
		}
	}

	/**
	 * 获取竞技场任务
	 *
	 * @param index
	 * @return
	 */
	public TargetMission getArenaMission(int index) {
		return getDb_Builder().getArenaMission().getMissionsMap().get(index);
	}

	public void putArenaMission(TargetMission newMission) {
		if (newMission == null) {
			return;
		}

		getDb_Builder().getArenaMissionBuilder().putMissions(newMission.getCfgId(), newMission);
	}

	private List<MissionObject> getArenaMissionByType(TargetTypeEnum target) {
		List<MissionObject> result = new ArrayList<>();
		if (target == null || target == TargetTypeEnum.TTE_NULL) {
			return result;
		}

		ArenaConfigObject arenaCfg = ArenaConfig.getById(GameConst.CONFIG_ID);
		if (arenaCfg == null) {
			return result;
		}

		for (int i : arenaCfg.getDailymission()) {
			MissionObject missionCfg = Mission.getById(i);
			if (missionCfg != null && missionCfg.getMissiontype() == target.getNumber()) {
				result.add(missionCfg);
			}
		}

		for (int i : arenaCfg.getChallengemission()) {
			MissionObject missionCfg = Mission.getById(i);
			if (missionCfg != null && missionCfg.getMissiontype() == target.getNumber()) {
				result.add(missionCfg);
			}
		}
		return result;
	}

	private void doArenaMission(TargetTypeEnum typeEnum, int addPro, int param) {
		List<MissionObject> list = getArenaMissionByType(typeEnum);
		if (CollectionUtils.isEmpty(list)) {
			return;
		}

		List<TargetMission> refresh = new ArrayList<>();
		for (MissionObject mission : list) {
			TargetMission beforeActivityPro = getArenaMission(mission.getId());
			TargetMission afterTargetMission = addMissionPro(mission, typeEnum, addPro, param, beforeActivityPro);

			if (missionUpdate(beforeActivityPro, afterTargetMission)) {
				refresh.add(afterTargetMission);
				putArenaMission(afterTargetMission);
			}
		}

		if (CollectionUtils.isNotEmpty(list)) {
			SC_RefreshArenaMissionPro.Builder refreshBuilder = SC_RefreshArenaMissionPro.newBuilder();
			refreshBuilder.addAllMission(refresh);
			GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreshArenaMissionPro_VALUE, refreshBuilder);
		}
	}

	/**
	 * ===============================竞技场任务 end==================================
	 */

	private void doSpecialActivity(TargetTypeEnum typeEnum, int addPro, int param) {
		doNovice(typeEnum, addPro, param);
		doTimeLimitActivity(typeEnum, addPro, param);
		doMistTimeLimitMission(typeEnum, addPro, param);
		doApocalypseBlessingMission(typeEnum, addPro, param);
		doMistMazeMission(typeEnum, addPro, param);
		doMistGhostBusterMission(typeEnum, addPro, param);
	}

	private void doMistGhostBusterMission(TargetTypeEnum typeEnum, int addPro, int param) {
		ServerActivity activity = ActivityManager.getInstance().getActivityByType(ActivityTypeEnum.ATE_MistGhostBuster);
		if (activity == null) {
			return;
		}

		if (!activityCanAddProgress(activity) || activity.getMissionsCount() <= 0) {
			return;
		}
		long currentTime = GlobalTick.getInstance().getCurrentTime();
		List<RefreshActivity> modifyList = new ArrayList<>();

		DB_MistGhostActivity.Builder ghostMissionBuilder = getDb_Builder().getSpecialInfoBuilder().getGhostActivityMissionBuilder();
		for (ServerSubMission activitySubCfg : activity.getMissionsMap().values()) {
			if (!(activitySubCfg.getEndTimestamp() == -1 || currentTime <= activitySubCfg.getEndTimestamp())) {
				continue;
			}
			if (activitySubCfg.getSubType() != typeEnum) {
				continue;
			}
			if (activitySubCfg.getAdditon() > 0 && activitySubCfg.getAdditon() != param) {
				continue;
			}

			int i = 0;
			boolean existTarget = false;
			TargetMission.Builder tmpTarget;
			TargetMission.Builder targetMission = null;
			for (; i < ghostMissionBuilder.getMissionProCount(); i++) {
				tmpTarget = ghostMissionBuilder.getMissionProBuilder(i);
				if (tmpTarget.getCfgId() == activitySubCfg.getIndex()) {
					targetMission = tmpTarget;
					existTarget = true;
					break;
				}
			}
			if (targetMission == null) {
				targetMission = TargetMission.newBuilder();
				targetMission.setCfgId(activitySubCfg.getIndex());
			} else if (targetMission.getStatus() != MissionStatusEnum.MSE_UnFinished) {
				continue;
			}

			int progress = targetMission.getProgress() + addPro;
			if (progress >= activitySubCfg.getTarget()) {
				progress = activitySubCfg.getTarget();
				targetMission.setStatus(MissionStatusEnum.MSE_Finished);
			}
			targetMission.setProgress(progress);
			if (!existTarget) {
				ghostMissionBuilder.addMissionPro(targetMission);
			} else {
				ghostMissionBuilder.setMissionPro(i, targetMission);
			}

			// 添加到刷新列表
			modifyList.add(buildRefreshMission(activity.getActivityId(), targetMission.build()));
		}

		if (!modifyList.isEmpty()) {
			sendRefreshActivityMissionByList(modifyList);
		}
	}

	public void sendMistMazeMission() {
		SC_MistMazeActivityMission.Builder builder = SC_MistMazeActivityMission.newBuilder();
		builder.addAllMissionPro(getDb_Builder().getSpecialInfoBuilder().getMazeActivityMissionBuilder().getMissionProList());
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_MistMazeActivityMission_VALUE, builder);
	}

	private void doMistMazeMission(TargetTypeEnum typeEnum, int addPro, int param) {
		if (!MistForestManager.getInstance().getMazeManager().isOpen()) {
			return;
		}
		if (addPro <= 0) {
			return;
		}
		GameConfigObject cfg = GameConfig.getById(GameConst.CONFIG_ID);
		if (cfg == null) {
			return;
		}
		int[] missionList = cfg.getMistmazeseasonmission();
		if (missionList == null || missionList.length <= 0) {
			return;
		}
		boolean needUpdate = false;
		MissionObject mission;
		DB_MistMazeActivity.Builder mazeMissionBuilder = getDb_Builder().getSpecialInfoBuilder().getMazeActivityMissionBuilder();
		for (int i = 0; i < missionList.length; i++) {
			mission = Mission.getById(missionList[i]);
			if (mission == null) {
				continue;
			}
			if (mission.getMissiontype() != typeEnum.getNumber()) {
				continue;
			}
			if (mission.getAddtion() > 0 && mission.getAddtion() != param) {
				continue;
			}
			needUpdate = true;
			boolean initFlag = false;
			TargetMission.Builder tmpTarget;
			TargetMission.Builder targetMission = null;
			int j = 0;
			for (; j < mazeMissionBuilder.getMissionProCount(); j++) {
				tmpTarget = mazeMissionBuilder.getMissionProBuilder(j);
				if (tmpTarget.getCfgId() == missionList[i]) {
					targetMission = tmpTarget;
					initFlag = true;
					break;
				}
			}
			if (targetMission == null) {
				targetMission = TargetMission.newBuilder();
				targetMission.setCfgId(mission.getId());
			} else if (targetMission.getStatus() != MissionStatusEnum.MSE_UnFinished) {
				continue;
			}
			int progress = targetMission.getProgress() + addPro;
			if (progress >= mission.getTargetcount()) {
				progress = mission.getTargetcount();
				targetMission.setStatus(MissionStatusEnum.MSE_Finished);
			}
			targetMission.setProgress(progress);
			if (!initFlag) {
				mazeMissionBuilder.addMissionPro(targetMission);
			} else {
				mazeMissionBuilder.setMissionPro(j, targetMission);
			}
		}

		if (needUpdate) {
			sendMistMazeMission();
		}
	}

	private void doApocalypseBlessingMission(TargetTypeEnum typeEnum, int addPro, int param) {
		ServerActivity activity = ActivityManager.getInstance().findGeneraActivityByTemplate(GeneralActivityTemplate.GAT_ApocalypseBlessing);
		if (activity == null || GameUtil.outOfScope(activity.getBeginTime(), activity.getEndTime(), GlobalTick.getInstance().getCurrentTime())) {
			return;
		}
		TargetSystemDB.DB_ApocalypseBlessing bless = getDb_Builder().getSpecialInfo().getBless();
		boolean modify = false;
		int beforePro = bless.getCurPro();
		Map<Integer, TargetMission> missionProMap = bless.getMissionProMap();
		List<RefreshActivity> modifiedMission = new ArrayList<>();
		for (ServerSubMission mission : activity.getMissionsMap().values()) {
			if (mission.getSubType() != typeEnum) {
				continue;
			}
			TargetMission missionPro = missionProMap.get(mission.getIndex());
			if (missionPro != null && MissionStatusEnum.MSE_FinishedAndClaim == missionPro.getStatus()) {
				continue;
			}
			// 如果是累积登陆或者签到类型任务,
			if (typeEnum == TargetTypeEnum.TTE_CumuLogin && GlobalTick.getInstance().getCurrentTime() < bless.getNextCanUpdateCumuLoginTime()) {
				return;
			}
			TargetSystemDB.DB_ApocalypseBlessing.Builder blessBuilder = getDb_Builder().getSpecialInfoBuilder().getBlessBuilder();
			TargetMission targetMission = addMissionPro(mission.getIndex(), typeEnum, mission.getAdditon(), mission.getTarget(), typeEnum, addPro, param, missionPro);

			if (MissionStatusEnum.MSE_Finished == targetMission.getStatus()) {
				targetMission = targetMission.toBuilder().setStatus(MissionStatusEnum.MSE_FinishedAndClaim).build();
			}
			if (missionUpdate(missionPro, targetMission)) {
				modifiedMission.add(buildRefreshMission(activity.getActivityId(), targetMission));
				blessBuilder.putMissionPro(targetMission.getCfgId(), targetMission);
			}
			if (MissionStatusEnum.MSE_FinishedAndClaim == targetMission.getStatus()) {
				blessBuilder.setCurPro(blessBuilder.getCurPro() + mission.getRewardList().get(0).getCount());
				modify = true;
				if (typeEnum == TargetTypeEnum.TTE_CumuLogin) {
					blessBuilder.setNextCanUpdateCumuLoginTime(TimeUtil.getNextDayResetTime(GlobalTick.getInstance().getCurrentTime()));
				}
			}

		}

		if (modify) {
			TargetTypeEnum blessType = TargetTypeEnum.TEE_ApocalypseBlessing_CumuGainScore;
			TargetSystemDB.DB_ApocalypseBlessing.Builder blessBuilder = getDb_Builder().getSpecialInfoBuilder().getBlessBuilder();
			int addTarget = blessBuilder.getCurPro() - beforePro;
			for (ServerSubMission mission : activity.getMissionsMap().values()) {
				if (blessType != mission.getSubType()) {
					continue;
				}
				TargetMission missionPro = missionProMap.get(mission.getIndex());
				if (missionPro != null && MissionStatusEnum.MSE_UnFinished != missionPro.getStatus()) {
					continue;
				}
				TargetMission targetMission = addMissionPro(mission.getIndex(), blessType, mission.getAdditon(), mission.getTarget(), blessType, addTarget, param, missionPro);

				if (missionUpdate(missionPro, targetMission)) {
					modifiedMission.add(buildRefreshMission(activity.getActivityId(), targetMission));
					blessBuilder.putMissionPro(targetMission.getCfgId(), targetMission);
				}
			}
			int nowPro = bless.getCurPro() + addTarget;
			getDb_Builder().getSpecialInfoBuilder().getBlessBuilder().setCurPro(bless.getCurPro() + addTarget);
			sendApocalypseBlessingUpdate(nowPro);
		}

		if (!modifiedMission.isEmpty()) {
			sendRefreshActivityMissionByList(modifiedMission);
		}
	}

	private void sendApocalypseBlessingUpdate(int nowPro) {
		Activity.SC_ApocalypseBlessingUpdate.Builder msg = Activity.SC_ApocalypseBlessingUpdate.newBuilder();
		msg.setCurPro(nowPro);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_ApocalypseBlessingUpdate_VALUE, msg);
	}

	/**
	 * ==================================迷雾深林限时任务 start
	 * ==================================
	 */
	public DB_MistTimeLimitMission.Builder getMistTimeLimitMissionBuilder() {
		DB_MistTimeLimitMission.Builder builder = getDb_Builder().getSpecialInfoBuilder().getMistTimeLimitMissionBuilder();

		// 设置玩家当前所在楼层
		int playerCurMistLv = MistConst.getPlayerBelongMistLv(getLinkplayeridx());
		if (builder.getCurMistLv() == 0 || !MistTimeLimitMissionManager.getInstance().inSameTimeLimitActivity(builder.getCurMistLv(), playerCurMistLv)) {
			LogUtil.info("targetsystemEntity.getMistTimeLimitMissionBuilder, player:" + getLinkplayeridx() + " cur mist lv:" + playerCurMistLv + ", is not equals cur mission lv:" + builder.getCurMistLv() + ", rest mission progress");
			builder.clear();
			builder.setCurMistLv(MistConst.getPlayerBelongMistLv(getLinkplayeridx()));
		}

		if (builder.getCurMission().getCfgId() == 0 || builder.getCurMission().getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
			int curMissionId = builder.getCurMission().getCfgId();

			// 进行下一个任务
			int newMissionId = MistTimeLimitMissionManager.getInstance().getNextMissionId(getLinkplayeridx(), curMissionId);
			if (newMissionId == -1) {
				LogUtil.error("targetsystemEntity.doMistTimeLimitMission, can not find next mission id, cur mission id:" + curMissionId);
			} else if (newMissionId == curMissionId) {
				LogUtil.info("targetsystemEntity.doMistTimeLimitMission, player have finished all mission, cur mission" + curMissionId);
			} else {
				if (curMissionId != 0 && !builder.getAlreadyFinishedAndClaimedList().contains(curMissionId)) {
					builder.addAlreadyFinishedAndClaimed(curMissionId);
				}
				builder.setCurMission(TargetMission.newBuilder().setCfgId(newMissionId).build());
			}
		}
		return builder;
	}

	private void doMistTimeLimitMission(TargetTypeEnum typeEnum, int addPro, int param) {
		if (!MistTimeLimitMissionManager.getInstance().isOpen()) {
//            LogUtil.debug("model.targetsystem.entity.targetsystemEntity.doMistTimeLimitMission， time limit mission is not open");
			return;
		}

		DB_MistTimeLimitMission.Builder builder = getMistTimeLimitMissionBuilder();
		MissionObject missionCfg = Mission.getById(builder.getCurMission().getCfgId());
		if (missionCfg == null || missionCfg.getMissiontype() != typeEnum.getNumber()) {
			return;
		}
		TargetMission newPro = addMissionPro(missionCfg, typeEnum, addPro, param, builder.getCurMission());
		if (missionUpdate(builder.getCurMission(), newPro)) {
			// 完成自动领取
			if (newPro.getStatus() == MissionStatusEnum.MSE_Finished) {
				List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward());
				Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Mist_TimeLimitMission);
				RewardManager.getInstance().doRewardByList(getLinkplayeridx(), rewards, reason, true);

				// 更新状态
				newPro = newPro.toBuilder().setStatus(MissionStatusEnum.MSE_FinishedAndClaim).build();
			}

			builder.setCurMission(newPro);

			// 刷新进度
			SC_RefreshMistTimeLimitMissionProgress.Builder refresh = SC_RefreshMistTimeLimitMissionProgress.newBuilder().setNewPro(newPro).addAllAlreadyFinishedAndClaimed(builder.getAlreadyFinishedAndClaimedList());
			GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreshMistTimeLimitMissionProgress_VALUE, refresh);
		}
	}

	public void sendMistTimeLimitMissionMsg() {
		if (!GlobalData.getInstance().checkPlayerOnline(getLinkplayeridx())) {
			return;
		}

		SC_ClaimMistTimeLimitMission.Builder resultBuilder = SC_ClaimMistTimeLimitMission.newBuilder();
		DB_MistTimeLimitMission.Builder builder = getMistTimeLimitMissionBuilder();
		resultBuilder.setCurMission(builder.getCurMission());
		resultBuilder.addAllAlreadyFinishedAndClaimed(builder.getAlreadyFinishedAndClaimedList());
		resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_ClaimMistTimeLimitMission_VALUE, resultBuilder);
	}

	/**
	 * ==================================迷雾深林限时任务
	 * end==================================
	 */

	private void doTimeLimitActivity(TargetTypeEnum typeEnum, int addPro, int param) {
		List<TimeLimitActivityTaskObject> result = TimeLimitActivityTask.getByType(typeEnum);
		if (CollectionUtils.isEmpty(result)) {
			return;
		}

		DB_TimeLimitActivity.Builder timeLimitBuilder = getDb_Builder().getSpecialInfoBuilder().getTimeLimitActivitiesBuilder();
		// 如果是累积登陆类型活动
		if (typeEnum == TargetTypeEnum.TTE_CumuLogin && timeLimitBuilder.getNextCanUpdateTime() != 0 && GlobalTick.getInstance().getCurrentTime() < timeLimitBuilder.getNextCanUpdateTime()) {
			return;
		}

		long currentTime = GlobalTick.getInstance().getCurrentTime();
		long startTime = timeLimitBuilder.getStartTime();

		int playerLv = PlayerUtil.queryPlayerLv(getLinkplayeridx());

		List<RefreshActivity> modifiedMission = new ArrayList<>();
		for (TimeLimitActivityTaskObject taskObject : result) {
			TimeLimitActivityObject linkActivity = TimeLimitActivity.getTaskLinkActivity(taskObject.getId());
			if (linkActivity == null || playerLv < linkActivity.getOpenlv()) {
				continue;
			}

			// 任务已经过期不加进度
			if (taskObject.getEndtime() != -1 && startTime + TimeUtil.MS_IN_A_DAY * taskObject.getEndtime() < currentTime) {
				continue;
			}

			TargetMission missionPro = timeLimitBuilder.getMissionProMap().get(taskObject.getId());
			TargetMission targetMission = addMissionPro(taskObject.getId(), TargetTypeEnum.forNumber(taskObject.getMissiontype()), taskObject.getAddtion(), taskObject.getTargetcount(), typeEnum, addPro, param, missionPro);

			if (missionUpdate(missionPro, targetMission)) {
				modifiedMission.add(buildRefreshMission(TimeLimitActivity.getTaskLinkActivityId(taskObject.getId()), targetMission));
				timeLimitBuilder.putMissionPro(targetMission.getCfgId(), targetMission);
			}
		}

		if (!modifiedMission.isEmpty()) {
			sendRefreshActivityMissionByList(modifiedMission);

			// 更新累积登陆时间
			if (typeEnum == TargetTypeEnum.TTE_CumuLogin) {
				timeLimitBuilder.setNextCanUpdateTime(TimeUtil.getNextDayResetTime(GlobalTick.getInstance().getCurrentTime()));
			}
		}
	}

	/**
	 * 新号积分
	 */
	private void doNovice(TargetTypeEnum typeEnum, int addPro, int param) {
		if (!noviceIsValid(1)) {
			return;
		}

		Builder db_builder = getDb_Builder();
		if (db_builder == null) {
			return;
		}

		DB_NoviceCredit.Builder noviceBuilder = db_builder.getSpecialInfoBuilder().getNoviceBuilder();

		// 如果是累积登陆类型活动
		if (typeEnum == TargetTypeEnum.TTE_CumuLogin && GlobalTick.getInstance().getCurrentTime() < noviceBuilder.getNextCanUpdateTime()) {
			return;
		}

		List<TargetMission> modify = new ArrayList<>();
		Map<Integer, TargetMission> missionProMap = noviceBuilder.getMissionProMap();
		for (NoviceTaskObject value : NoviceTask._ix_id.values()) {
			TargetMission targetMission = addMissionPro(value.getId(), TargetTypeEnum.forNumber(value.getMissiontype()), value.getAddtion(), value.getTargetcount(), typeEnum, addPro, param, missionProMap.get(value.getId()));

			if (missionUpdate(missionProMap.get(value.getId()), targetMission)) {
				modify.add(targetMission);
				noviceBuilder.putMissionPro(targetMission.getCfgId(), targetMission);
			}
		}

		if (!modify.isEmpty()) {
			SC_RefreshNovicePro.Builder newBuilder = SC_RefreshNovicePro.newBuilder();
			newBuilder.addAllNewPro(modify);
			newBuilder.setCurPoint(noviceBuilder.getCurPoint());
			newBuilder.addAllClaimedReward(noviceBuilder.getClaimRewardList());
			GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreshNovicePro_VALUE, newBuilder);

			// 更新累积登陆时间
			if (TargetTypeEnum.TTE_CumuLogin == typeEnum) {
				noviceBuilder.setNextCanUpdateTime(TimeUtil.getNextDayResetTime(GlobalTick.getInstance().getCurrentTime()));
			}
		}
	}

	/**
	 * 进度发生改变且状态变变为 为完成
	 *
	 * @param before
	 * @param after
	 * @return
	 */
	private boolean missionUpdate(TargetMission before, TargetMission after) {
		if (after == null) {
			return false;
		}

		if (before == null) {
			return true;
		}

		if (before.getProgress() != after.getProgress() || before.getStatus() != after.getStatus()) {
			return true;
		}

		return false;
	}

	/**
	 * 添加任务进度, 累登类型任务请提前判断是否可以更新进度
	 *
	 * @param cfgId          当前更新进度的配置Id,用于防止TargetMission为空的情况
	 * @param cfgType        任务配置的target类型
	 * @param cfgAddition    任务的附加条件（无附加条件填0）
	 * @param cfgTargetCount 任务的目标个数
	 * @param addType        正在处理的任务类型
	 * @param addPro         添加的进度
	 * @param param          参数
	 * @param targetMission  已经存在的任务进度
	 * @return
	 */
	private TargetMission addMissionPro(int cfgId, TargetTypeEnum cfgType, int cfgAddition, int cfgTargetCount, TargetTypeEnum addType, int addPro, int param, TargetMission targetMission) {

		if (cfgType != addType || addType == null || addType == TargetTypeEnum.TTE_NULL || cfgTargetCount <= 0 || addPro <= 0) {
			return targetMission;
		}

		TargetMission.Builder builder;
		if (targetMission == null) {
			builder = TargetMission.newBuilder().setCfgId(cfgId);
		} else {
			// 当前进度已完成直接返回
			if (targetMission.getStatus() != MissionStatusEnum.MSE_UnFinished) {
				return targetMission;
			}
			builder = targetMission.toBuilder();
		}

		// 判断类型附件条件是否满足条件
		if (!additionIsSatisfy(addType, cfgAddition, param)) {
			return builder.build();
		}

		// 判断任务是增量还是全量
		if (isIncrementType(addType)) {
			builder.setProgress(builder.getProgress() + addPro);
		} else {
			// 全量更新时小于当前进度不更新
			if (addPro > builder.getProgress()) {
				builder.setProgress(addPro);
			}
		}

		if (builder.getProgress() >= cfgTargetCount) {
//            LogUtil.debug("cfgId = " + cfgId + ",cur progress =" + builder.getProgress()
//                    + ", target pro =" + cfgTargetCount + ", finished this mission");
			builder.setProgress(cfgTargetCount);
			builder.setStatus(MissionStatusEnum.MSE_Finished);
		} else {
			if (needNotAddProgress(cfgType, cfgAddition)) {
				builder.clearProgress();
			}
		}
		return builder.build();
	}

	/**
	 * 不需要处理进度
	 * <p>
	 * 如果是竞技场段位达到且配置有额外条件 如果当前进度不满足目标次数 竞技场段位为全量更新 不处理进度
	 *
	 * @param typeEnum
	 * @param cfgAddition
	 * @return
	 */
	private boolean needNotAddProgress(TargetTypeEnum typeEnum, int cfgAddition) {
		if (typeEnum == TargetTypeEnum.TEE_Arena_DanReach && cfgAddition >= 0) {
			return true;
		}
		return false;
	}

	/**
	 * 添加任务进度, 累登类型任务请提前判断是否可以更新进度
	 *
	 * @param missionCfg 任务配置
	 * @param typeEnum   正在处理的任务类型
	 * @param addPro     添加的进度
	 * @param param      参数
	 * @param curPro     当前已经存在的进度
	 * @return
	 */
	private TargetMission addMissionPro(MissionObject missionCfg, TargetTypeEnum typeEnum, int addPro, int param, TargetMission curPro) {
		if (missionCfg == null || typeEnum == null || missionCfg.getMissiontype() != typeEnum.getNumber() || addPro <= 0) {
			return curPro;
		}
		return addMissionPro(missionCfg.getId(), TargetTypeEnum.forNumber(missionCfg.getMissiontype()), missionCfg.getAddtion(), missionCfg.getTargetcount(), typeEnum, addPro, param, curPro);
	}

	/**
	 * 额外条件是否满足
	 *
	 * @param addType
	 * @param cfgAddition
	 * @param param       传入的target额外条件
	 * @return
	 */
	private boolean additionIsSatisfy(TargetTypeEnum addType, int cfgAddition, int param) {
		// 该类型不支持额外条件,或者支持额外条件但是未配置则满足
		if (!supportedAddition(addType) || cfgAddition == 0) {
			return true;
		}

		// 参数小于0 不满足
		if (param < 0) {
			return false;
		}

		// 判断附加类型是使用大于等于还是小于
		boolean satisfy = false;
		int compareType = getTargetTypeAdditionCompareType(addType);
		if (CompareType.GRATER_OR_EQUAL == compareType) {
			satisfy = param >= cfgAddition;
		} else if (CompareType.EQUAL == compareType) {
			satisfy = param == cfgAddition;
		} else if (CompareType.LESS_OR_EQUAL == compareType) {
			satisfy = param > 0 && param <= cfgAddition;
		}
//        if (!satisfy) {
//            LogUtil.debug("model.targetsystem.entity.targetsystemEntity.additionIsSatisfy, not satisfy, type = "
//                    + addType + ", cfgAddition = " + cfgAddition + ", param = " + param);
//        }
		return satisfy;
	}

	public void initFeats() {
		initFeatsInfo(GameConst.FEAT_TYPE_HUOYUE);// 活跃
		initFeatsInfo(GameConst.FEAT_TYPE_WUJIN);// 无尽
		initFeatsInfo(GameConst.FEAT_TYPE_XUKONG);// 虚空
	}

	public void initFeatsInfo(int type) {
		long restTime = 0;

		if (type == GameConst.FEAT_TYPE_HUOYUE) {
			restTime = TimeUtil.getNextDaysResetTime(GlobalTick.getInstance().getCurrentTime(), GameConfig.getById(GameConst.CONFIG_ID).getFeatesrestdays());
		} else if (type == GameConst.FEAT_TYPE_WUJIN) {
			restTime = TimeUtil.getNextDaysResetTime(GlobalTick.getInstance().getCurrentTime(), GameConfig.getById(GameConst.CONFIG_ID).getEndlessfeatsdays());
		} else {
			restTime = TimeUtil.getNextDaysResetTime(GlobalTick.getInstance().getCurrentTime(), GameConfig.getById(GameConst.CONFIG_ID).getPatrolfeatsdays());
		}
		DB_Feats dbFeats = getDb_Builder().getFeatsInfosMap().getOrDefault(type, DB_Feats.getDefaultInstance());
		//DB_Feats db_Feats = getDb_Builder().getFeatsInfosMap().get(type);
	//	DB_Feats.Builder builder = null;
/*		if (db_Feats == null) {
			builder = DB_Feats.newBuilder();
		} else {
			builder = db_Feats.toBuilder();
		}*/
		DB_Feats.Builder builder = dbFeats.toBuilder().setResetTime(restTime);
		getDb_Builder().putFeatsInfos(type, builder.build());
	}

	public void sendFeats() {
		SC_GetFeatsInfo.Builder result = SC_GetFeatsInfo.newBuilder();
		if (toResultBuilder(result, GameConst.FEAT_TYPE_HUOYUE) != null) {
			result.addInfo(toResultBuilder(result, GameConst.FEAT_TYPE_HUOYUE));
		}
		if (toResultBuilder(result, GameConst.FEAT_TYPE_WUJIN) != null) {
			result.addInfo(toResultBuilder(result, GameConst.FEAT_TYPE_WUJIN));
		}
		if (toResultBuilder(result, GameConst.FEAT_TYPE_XUKONG) != null) {
			result.addInfo(toResultBuilder(result, GameConst.FEAT_TYPE_XUKONG));
		}
		result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_GetFeatsInfo_VALUE, result);
	}


	private FeatsInfo toResultBuilder(protocol.TargetSystem.SC_GetFeatsInfo.Builder resultBuilder, int type) {
		FeatsInfo.Builder builder = FeatsInfo.newBuilder();
		protocol.TargetSystemDB.DB_Feats featsInfo = getDb_Builder().getFeatsInfosMap().get(type);
		builder.setType(type);
		if (featsInfo == null) {
			return builder.build();
		}
		if (featsInfo.getResetTime() < GlobalTick.getInstance().getCurrentTime()) {
			return null;
		}
		builder.setFeatsType(featsInfo.getFeatsType());
		builder.addAllClaimedAdvancedReward(featsInfo.getClaimedAdvanceRewardList());
		builder.addAllClaimedBasicReward(featsInfo.getClaimedBasicRewardList());
		builder.setResetTime(featsInfo.getResetTime());
		return builder.build();
	}

	public void sendWishingWellInfo() {
		protocol.Activity.SC_GetWishWellInfo.Builder result = protocol.Activity.SC_GetWishWellInfo.newBuilder();
		protocol.TargetSystemDB.DB_WishingWell wishingWell = getDb_Builder().getSpecialInfo().getWishingWell();
		result.addAllWishList(wishingWell.getWishMapMap().values());
		result.setRetCode(GameUtil.buildRetCode(protocol.RetCodeId.RetCodeEnum.RCE_Success));
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_GetWishWellInfo_VALUE, result);
	}

	public void sendGrowFundInfo() {
		Activity.SC_GrowthFundInfo.Builder result = Activity.SC_GrowthFundInfo.newBuilder();
		TargetSystemDB.DB_GrowthFund growthFund = getDb_Builder().getSpecialInfo().getGrowthFund();
		result.setBuy(growthFund.getBuy());
		result.addAllClaimedIdList(growthFund.getClaimedIdListList());
		result.setRetCode(GameUtil.buildRetCode(protocol.RetCodeId.RetCodeEnum.RCE_Success));
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_GrowthFundInfo_VALUE, result);
	}

	/**
	 * 额外条件满足条件是大于或者等于的集合
	 */
	private static final Set<TargetTypeEnum> ADDITION_GRATER_OR_EQUAL;

	static {
		Set<TargetTypeEnum> tempSet = new HashSet<>();
		tempSet.add(TargetTypeEnum.TTE_Patrol_FightAboveGreed);
		tempSet.add(TargetTypeEnum.TEE_Patrol_SpecialGreedFinish);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_Common);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_WarGold);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_HolyWater);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_DpResource);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuStationTroops_BossGrid);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_Common);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_WarGold);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_HolyWater);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_DpResource);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_BossGrid);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_WarGold);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_HolyWater);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_DpResource);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_BossGrid);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid);
		tempSet.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_Common);
		tempSet.add(TargetTypeEnum.TEE_Player_RechargeCoupon);
		tempSet.add(TargetTypeEnum.TTE_CumuConsumeCoupon);
		tempSet.add(TargetTypeEnum.TEE_ApocalypseBlessing_CumuGainScore);
		tempSet.add(TargetTypeEnum.TTE_Collection_CumuLvUp);
		tempSet.add(TargetTypeEnum.TTE_Train_CumuComplete);
		tempSet.add(TargetTypeEnum.TTE_HonorWall_CumuClaimReward);
		tempSet.add(TargetTypeEnum.TTE_Pet_DischargeOrRestore);
		tempSet.add(TargetTypeEnum.TTE_StoneRift_ClaimRes);
		tempSet.add(TargetTypeEnum.TTE_CrossArenaHonor1002);

		ADDITION_GRATER_OR_EQUAL = Collections.unmodifiableSet(tempSet);
	}

	/**
	 * 判断能使用附加条件的任务类型的附件条件比较方式
	 *
	 * @param addType
	 */
	private int getTargetTypeAdditionCompareType(TargetTypeEnum addType) {
		if (ADDITION_GRATER_OR_EQUAL.contains(addType)) {
			return CompareType.GRATER_OR_EQUAL;
		}
		if (addType == TargetTypeEnum.TEE_Arena_DanReach) {
			return CompareType.LESS_OR_EQUAL;
		}
		return CompareType.EQUAL;
	}

	/**
	 * 新号积分活动是否在有效期中
	 *
	 * @param type 1,是否可以更新进度，2，是否可以领取奖励
	 * @return true 在， false 不在
	 */
	public boolean noviceIsValid(int type) {
		Builder db_builder = getDb_Builder();
		if (db_builder == null) {
			return true;
		}
		DB_NoviceCredit.Builder noviceBuilder = db_builder.getSpecialInfoBuilder().getNoviceBuilder();
		if (noviceBuilder.getStartTime() == 0) {
			noviceBuilder.setStartTime(GlobalTick.getInstance().getCurrentTime());
		}

		if (type == 1) {
			return timeInScope(noviceBuilder.getStartTime(), 0, NoviceTask.getInstance().getMaxOpenTime());
		} else if (type == 2) {
			return timeInScope(noviceBuilder.getStartTime(), 0, NoviceTask.getInstance().getMaxDisTime());
		}
		return false;
	}

	/**
	 * 当前时间是否在领取时间内,新手积分专用
	 *
	 * @param startTime
	 * @param openDay
	 * @param closeDay
	 * @return
	 */
	public static boolean timeInScope(long startTime, int openDay, int closeDay) {
		long currentTime = GlobalTick.getInstance().getCurrentTime();
		long nextResetTime = TimeUtil.getNextDayResetTime(startTime);
		long curDay = 1;
		if (nextResetTime <= currentTime) {
			long divide = (currentTime - nextResetTime) / TimeUtil.MS_IN_A_DAY;
			long remainder = (currentTime - nextResetTime) % TimeUtil.MS_IN_A_DAY;
			curDay += remainder > 0 ? ++divide : divide;
		}

		return openDay <= curDay && curDay <= closeDay;
	}

	/**
	 * 获取与玩家相关的特殊活动
	 *
	 * @return
	 */
	public List<ClientActivity> getPlayerSpecialActivity() {
		List<ClientActivity> result = new ArrayList<>();

		// 新手积分
		if (noviceIsValid(2)) {
			ClientActivity novice = ClientActivity.newBuilder().setActivityType(ActivityTypeEnum.ATE_NoviceCredit).setTabType(EnumClientActivityTabType.ECATT_Independent).build();
			result.add(novice);
		}

		// 首充活动暂时屏蔽
		if (isShowPayActivity()) {
			ClientActivity firstPay = ClientActivity.newBuilder().setActivityType(ActivityTypeEnum.ATE_FirstPay).setTabType(EnumClientActivityTabType.ECATT_Independent).build();
			result.add(firstPay);
		}
		/**
		 * 新手礼包屏蔽
		 */
		/* combineNeeBeeActivity(result); */

		return result;
	}

	private void combineNeeBeeActivity(List<ClientActivity> result) {
		// 新手礼包活动
		ClientActivity newBeeGiftClientActivity = getNewBeeGiftClientActivity();
		if (newBeeGiftClientActivity != null) {
			result.add(newBeeGiftClientActivity);
		}
	}

	private ClientActivity getNewBeeGiftClientActivity() {
		DB_NeeBeeGift newBeeGift = getDb_Builder().getSpecialInfo().getNewBeeGift();
		if (newBeeGift.getEndTime() > 0 && newBeeGift.getEndTime() < GlobalTick.getInstance().getCurrentTime()) {
			return null;
		}
		if (newBeeGift.getGiftsCount() <= 0) {
			initNewBeeGift();
		}
		ClientActivity.Builder builder = ClientActivity.newBuilder();
		builder.setActivityType(ActivityTypeEnum.ATE_NewBeeGift);
		Cycle_TimeLimit.Builder cycle = Cycle_TimeLimit.newBuilder();
		cycle.setBeginTimestamp(GlobalTick.getInstance().getCurrentTime());
		cycle.setEndTimestamp(getDb_Builder().getSpecialInfo().getNewBeeGift().getEndTime());

		ActivityTime.Builder timeBuilder = ActivityTime.newBuilder();
		timeBuilder.setTimeType(CycleTypeEnum.CTE_TimeLimit);
		timeBuilder.setTimeContent(cycle.build().toByteString());
		builder.setCycleTime(timeBuilder);
		return builder.build();
	}

	private void initNewBeeGift() {
		DB_NeeBeeGift.Builder builder = getDb_Builder().getSpecialInfoBuilder().getNewBeeGiftBuilder();
		NeeBeeGiftActivityCfgObject activity = NeeBeeGiftActivityCfg.getById(LocalActivityId.NewBeeGift);
		long endTime = TimeUtil.calculateActivityEndTime(GlobalTick.getInstance().getCurrentTime(), activity.getEnddistime());
		builder.setEndTime(endTime);
		for (NewBeeGiftCfgObject gift : NewBeeGiftCfg._ix_id.values()) {
			builder.putGifts(gift.getId(), 0);
		}
	}

	/**
	 * 七日奖励是否已经领取完毕
	 *
	 * @return
	 */
	private boolean sevenDaysSignInFinished() {
		TimeLimitActivityObject activityCfg = TimeLimitActivity.getById(LocalActivityId.SEVEN_DAYS_SIGN_IN);
		if (activityCfg == null) {
			return true;
		}

		DB_TimeLimitActivity.Builder dbBuilder = getDb_Builder().getSpecialInfoBuilder().getTimeLimitActivitiesBuilder();
		// 有任务未完成且有任务未领取
		for (int taskId : activityCfg.getTasklist()) {
			TargetMission mission = dbBuilder.getMissionProMap().get(taskId);
			if (mission == null || mission.getStatus() != MissionStatusEnum.MSE_FinishedAndClaim) {
				return false;
			}
		}

		return true;
	}

	private boolean isShowPayActivity() {
		return !firstPayFinish() || !cumuPayFinish();
	}

	private boolean firstPayFinish() {
		Map<Integer, PayActivityRecord> payActivityRecordMap = getDb_Builder().getPayActivityRecordMap();
		if (MapUtils.isNotEmpty(payActivityRecordMap) && payActivityRecordMap.get(RechargeType.RT_FirstPay_VALUE) != null) {
			return payActivityRecordMap.get(RechargeType.RT_FirstPay_VALUE).getState() == PayActivityStateEnum.BSE_Finish_VALUE;
		}
		return false;
	}

	private boolean cumuPayFinish() {
		Map<Integer, PayActivityRecord> payActivityRecordMap = getDb_Builder().getPayActivityRecordMap();
		if (MapUtils.isNotEmpty(payActivityRecordMap) && payActivityRecordMap.get(RechargeType.RT_SignlePay_VALUE) != null) {
			return payActivityRecordMap.get(RechargeType.RT_SignlePay_VALUE).getState() == PayActivityStateEnum.BSE_Finish_VALUE;
		}
		return false;
	}

	public void onPlayerLogIn() {
		doTargetPro(TargetTypeEnum.TTE_CumuLogin, 1, 0);
		sendBusinessPopupMsgInit();
		sendItemCard();
	}

	/**
	 * 支持额外条件的枚举类型
	 */
	private static final Set<TargetTypeEnum> SUPPORT_ADDITION_TYPE = new HashSet<>();

	static {
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuAwakePet);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuFinishedPetEntrust);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuPetLevelReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuGainPet);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuGainRune);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuOpenMistBox);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuPassGoldResCopy);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuPassCrystalResCopy);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuPassSoulResCopy);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuPassRuneResCopy);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuCompoundPet);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuOccupyMine);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuPetAwakeRech);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuPassAwakeResCopy);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CumuPetTransfer);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_MistSeasonTask_OpenBoxCount);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_MistSeasonTask_UseItemCount);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_Pet_CumuGainSpecifyPet);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_Pet_CumuPetStarReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_Pet_SpecifyPetLvUpReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_Pet_SpecifyPetStarUpReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_Pet_SpecifyPetAwakeUpReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_Patrol_SpecialGreedFinish);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_Arena_DanReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_Function_Unlock);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_Gem_CumuGain);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_Gem_LvReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_Artifact_Unlock);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_Artifact_LvReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_Artifact_StarReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_PetAwake_AttackReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_PetAwake_DefenseReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_PetAwake_HpReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_BossTower_DefeatBoss);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_Mist_CumuKillMonster);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_Patrol_FightAboveGreed);

		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuCollectTech);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuComposeTech);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuGainTech);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_Common);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_WarGold);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_HolyWater);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_DpResource);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuStationTroops_BossGrid);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_Common);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_WarGold);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_HolyWater);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_DpResource);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_BossGrid);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_WarGold);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_HolyWater);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_DpResource);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_BossGrid);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_Common);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TEE_Item_CumuCollect);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_TrainScore);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_StoneRift_FactoryLevelReach);
		SUPPORT_ADDITION_TYPE.add(TargetTypeEnum.TTE_CrossArena_YSerialWinXTime);
	}

	/**
	 * 是否支持额外条件
	 *
	 * @param typeEnum
	 * @return
	 */
	public static boolean supportedAddition(TargetTypeEnum typeEnum) {
		if (typeEnum == null) {
			return false;
		}

		return SUPPORT_ADDITION_TYPE.contains(typeEnum);
	}

	/**
	 * ========================积分副本 start======================================
	 */

	/**
	 * 检查积分副本是否需要清空当前积分
	 */
	public void checkPointCopyExpire() {
		Builder db_builder = getDb_Builder();
		if (db_builder == null) {
			return;
		}

		DB_PointCopy.Builder pointCopy = db_builder.getSpecialInfoBuilder().getPointCopyBuilder();

		// 是否需要清空积分
		int cfgId = PointCopyManager.getInstance().getCfgId();
		if (pointCopy.getCurCfgId() != cfgId) {
			pointCopy.clear();
			pointCopy.setCurCfgId(cfgId);
		}

		if (pointCopy.getUnlockBattleIdCount() <= 0) {
			pointCopy.addUnlockBattleId(PointCopyManager.getDefaultUnlockFightId(cfgId));
		}
	}

	public void sendPointCopyInfo() {
		checkPointCopyExpire();
		DB_PointCopy.Builder pointCopy = getDb_Builder().getSpecialInfoBuilder().getPointCopyBuilder();
		SC_ClaimPointCopyInfo.Builder resultBuilder = SC_ClaimPointCopyInfo.newBuilder();
		resultBuilder.setPoint(pointCopy.getCurPoint());
		resultBuilder.addAllUnlockBattleId(pointCopy.getUnlockBattleIdList());
		resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
		resultBuilder.addAllClaimedIndex(pointCopy.getClaimRewardMissionIdList());
		resultBuilder.addAllCanSweepId(pointCopy.getCanSweepIdList());
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_ClaimPointCopyInfo_VALUE, resultBuilder);
	}

	public void addPointCopyScore(int addScore, Reason reason) {
		DB_PointCopy.Builder builder = getDb_Builder().getSpecialInfoBuilder().getPointCopyBuilder();
		int before = builder.getCurPoint();
		int newScore = before + addScore;
		builder.setCurPoint(newScore);

		sendPointCopyNewScore(newScore);

		// 目标：累积获得x积分副本积分
		EventUtil.triggerUpdateTargetProgress(getLinkplayeridx(), TargetTypeEnum.TEE_PointCopy_CumuPoint, addScore, 0);

		LogService.getInstance().submit(new DailyDateLog(getLinkplayeridx(), false, RewardTypeEnum.RTE_PointInstance, 0, before, addScore, newScore, reason));
	}

	public void sendPointCopyNewScore(int newScore) {
		SC_RefreshPointCopyScore.Builder builder = SC_RefreshPointCopyScore.newBuilder();
		builder.setNewPoint(newScore);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreshPointCopyScore_VALUE, builder);
	}

	/**
	 * ========================积分副本 end======================================
	 */

	public void clearActivitiesData(ServerActivity activity) {
		ActivityTypeEnum typeEnum = activity.getType();
		if (typeEnum == ActivityTypeEnum.ATE_Ranking) {
			getDb_Builder().removeActivityRanking(activity.getActivityId());
		} else if (typeEnum == ActivityTypeEnum.ATE_DemonDescends) {
			getDb_Builder().removeDemonDescendsInfo(activity.getActivityId());
		} else if (typeEnum == ActivityTypeEnum.ATE_DayDayRecharge) {
			settleDayDayRechargeActivity(activity);
			getDb_Builder().getSpecialInfoBuilder().clearDayDayRecharge();
		} else if (typeEnum == ActivityTypeEnum.ATE_HadesTreasure) {
			getDb_Builder().removeHadesInfo(activity.getActivityId());
		} else if (ActivityTypeEnum.ATE_DirectPurchaseGift == typeEnum) {
			getDb_Builder().getSpecialInfoBuilder().clearDirectPurchaseGiftBuyRecord();
		} else if (GeneralActivityTemplate.GAT_ApocalypseBlessing == activity.getTemplate()) {
			getDb_Builder().getSpecialInfoBuilder().clearBless();
		} else if (typeEnum == ActivityTypeEnum.ATE_RichMan) {
			getDb_Builder().getSpecialInfoBuilder().clearRichMan();
		} else if (typeEnum == ActivityTypeEnum.ATE_MistGhostBuster) {
			settleGhostBusterMissionActivity(activity);
			getDb_Builder().getSpecialInfoBuilder().clearGhostActivityMission();
		} else if (typeEnum == ActivityTypeEnum.ATE_PetAvoidance) {
			getDb_Builder().getSpecialInfoBuilder().clearPetAvoidance();
		} else if (typeEnum == ActivityTypeEnum.ATE_StarTreasure) {
			this.clearStarTreasureData(activity);
			getDb_Builder().getSpecialInfoBuilder().clearStarTreasureActivity();
		} else {
			getDb_Builder().removeActivities(activity.getActivityId());
		}
	}

	private void settleGhostBusterMissionActivity(ServerActivity activity) {
		List<Reward> rewards = null;
		DB_MistGhostActivity.Builder ghostActivity = getDb_Builder().getSpecialInfoBuilder().getGhostActivityMissionBuilder();
		for (TargetMission.Builder mission : ghostActivity.getMissionProBuilderList()) {
			if (mission.getStatus() == MissionStatusEnum.MSE_Finished) {
				ServerSubMission missionCfg = activity.getMissionsMap().get(mission.getCfgId());
				if (missionCfg == null) {
					continue;
				}
				if (rewards == null) {
					rewards = new ArrayList<>();
				}
				if (missionCfg.getRewardCount() > 0) {
					rewards.addAll(missionCfg.getRewardList());
				}
				if (missionCfg.getRandomTimes() > 0 && missionCfg.getRandomsCount() > 0) {
					rewards.addAll(RewardUtil.drawMustRandomReward(missionCfg.getRandomsList(), missionCfg.getRandomTimes()));
				}
			}
		}
		RewardUtil.mergeReward(rewards);
		EventUtil.triggerAddMailEvent(getLinkplayeridx(), MailTemplateUsed.getById(GameConst.CONFIG_ID).getGhostmission(), rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistGhostBuster));
	}

	private void clearStarTreasureData(ServerActivity activity){
		long haveCount = ConsumeManager.getInstance().getConsumItemCount(this.linkplayeridx, activity.getStarTreasure().getCostItem());

		Consume.Builder consumeBuilder = activity.getStarTreasure().getCostItem().toBuilder();
		consumeBuilder.setCount((int)haveCount);

		Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_System_Recovery,"系统回收");
		ConsumeManager.getInstance().consumeMaterial(this.linkplayeridx, consumeBuilder.build(), reason);
	}

	private void settleDayDayRechargeActivity(ServerActivity activity) {
		DB_DayDayRecharge dayDayRecharge = getDb_Builder().getSpecialInfo().getDayDayRecharge();
		if (dayDayRecharge.getCanClaimIndexCount() <= 0) {
			return;
		}
		List<Integer> canClaimIndexList = dayDayRecharge.getCanClaimIndexList();
		List<RewardList> rechargeRewardsList = activity.getDayDayRecharge().getRechargeRewardsList();
		List<Reward> rewards = new ArrayList<>();
		canClaimIndexList.stream().map(rechargeRewardsList::get).forEach(e -> rewards.addAll(e.getRewardList()));
		RewardUtil.mergeReward(rewards);
		EventUtil.triggerAddMailEvent(getLinkplayeridx(), MailTemplateUsed.getById(GameConst.CONFIG_ID).getDaydayrechargeactivitysettle(), rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DayDayRecharge_Settle));
	}

	public int getAlreadyGetDropCount(long activityId, int itemId) {
		DB_Activity.Builder dbActivityBuilder = getDBActivityBuilder(activityId);
		Integer count = dbActivityBuilder.getDropItemInfoMap().get(itemId);
		return count == null ? 0 : count;
	}

	public void updateDropItemCount(List<UpdateActivityDropCount> updateList) {
		if (updateList == null || updateList.isEmpty()) {
			return;
		}

		for (UpdateActivityDropCount value : updateList) {
			DB_Activity.Builder dbActivityBuilder = getDBActivityBuilder(value.getActivityId());
			Integer oldCount = dbActivityBuilder.getDropItemInfoMap().get(value.getItemId());
			if (oldCount == null) {
				dbActivityBuilder.putDropItemInfo(value.getItemId(), value.getCount());
			} else {
				dbActivityBuilder.putDropItemInfo(value.getItemId(), value.getCount() + oldCount);
			}
			putDBActivityBuilder(value.getActivityId(), dbActivityBuilder);
		}
	}

	public void sendNewActivity(List<ServerActivity> serverActivities) {
		if (GameUtil.collectionIsEmpty(serverActivities)) {
			return;
		}

		List<ClientActivity> clientActivities = parseToClientActivities(serverActivities);
		if (clientActivities != null) {
			SC_NewActivity.Builder result = SC_NewActivity.newBuilder();
			result.addAllActivitys(clientActivities);
			GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_NewActivity_VALUE, result);
		}
	}

	/**
	 * 获取排行榜活动builder
	 *
	 * @param activityId
	 * @return
	 */
	private DB_ActivityRanking.Builder getActivityRankingBuilder(long activityId) {
		Map<Long, DB_ActivityRanking> rankingMap = getDb_Builder().getActivityRankingMap();
		DB_ActivityRanking.Builder result;
		DB_ActivityRanking activityRanking = rankingMap.get(activityId);
		if (activityRanking == null) {
			result = DB_ActivityRanking.newBuilder().setActivityId(activityId);
		} else {
			result = activityRanking.toBuilder();
		}
		return result;
	}

	private void putActivityRankingBuilder(DB_ActivityRanking.Builder builder) {
		if (builder == null) {
			return;
		}
		getDb_Builder().putActivityRanking(builder.getActivityId(), builder.build());
	}

	public int addActivityRankingScore(long activityId, EnumRankingType rankingType, int addScore) {
		addScore = Math.max(addScore, 0);
		int newScore = addScore;
		DB_ActivityRanking.Builder rankingBuilder = getActivityRankingBuilder(activityId);
		if (rankingType == EnumRankingType.ERT_ArenaGainScore) {
			rankingBuilder.setArenaScoreGain(rankingBuilder.getArenaScoreGain() + addScore);
			newScore = rankingBuilder.getArenaScoreGain();
		} else if (rankingType == EnumRankingType.ERT_MineScore) {
			rankingBuilder.setMineScore(rankingBuilder.getMineScore() + addScore);
			newScore = rankingBuilder.getMineScore();
		}
		putActivityRankingBuilder(rankingBuilder);

		return newScore;
	}

	public int getLotteryOdds(int quality) {
		List<DB_LotteryCurOdds> oddsList = getDb_Builder().getSpecialInfo().getScratchLottery().getCurOddsList();
		for (DB_LotteryCurOdds curOdds : oddsList) {
			if (curOdds.getQuality() == quality) {
				return curOdds.getCurOdds();
			}
		}
		return 0;
	}

	public void setLotteryOdds(int quality, int newOdds) {
		DB_ScratchLottery.Builder lotteryBuilder = getDb_Builder().getSpecialInfoBuilder().getScratchLotteryBuilder();
		for (DB_LotteryCurOdds.Builder curOdds : lotteryBuilder.getCurOddsBuilderList()) {
			if (curOdds.getQuality() == quality) {
				curOdds.setCurOdds(newOdds);
				return;
			}
		}
		lotteryBuilder.addCurOdds(DB_LotteryCurOdds.newBuilder().setQuality(quality).setCurOdds(newOdds));
	}

	public void triggerTimeLimitGift(TimeLimitGiftType giftType, int curTarget) {
		if (TimeLimitGiftType.TLG_LosingStreak == giftType) {
			curTarget = getDb_Builder().getTimeLimitGiftInfo().getCurTarget() + 1;
			getDb_Builder().getTimeLimitGiftInfoBuilder().setCurTarget(curTarget);
		}
		TimeLimitGiftConfigObject config = TimeLimitGiftConfig.findMaxIdByTypeAndTarget(giftType.getNumber(), curTarget);
		if (config == null) {
			return;
		}
		// 额外触发概率
		int exProperty = TimeLimitGiftType.TLG_LosingStreak == giftType ? curTarget * config.getExprobability() : 0;
		if (!canTrigger(config, exProperty)) {
			return;
		}
		long expireTime = GlobalTick.getInstance().getCurrentTime() + config.getExpiretime() * 1000;

		increaseGiftTodayTotalTriggerTimes();

		upsertGiftToDB(config, expireTime);

		// 触发连败礼包后充值连败次数
		if (giftType == TimeLimitGiftType.TLG_LosingStreak) {
			getDb_Builder().getTimeLimitGiftInfoBuilder().setCurTarget(0);
		}

		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_NewTimeLimitGift_VALUE, SC_NewTimeLimitGift.newBuilder().setId(config.getId()).setExpireTime(expireTime));
	}

	private void upsertGiftToDB(TimeLimitGiftConfigObject config, long expireTime) {
		getDb_Builder().getTimeLimitGiftInfoBuilder().putGifts(config.getId(), DB_TimeLimitGiftItem.newBuilder().setId(config.getId()).setLimitTime(expireTime).setTriggerTime(getTriggerTimes(config.getId()) + 1).setBuy(false).build());
	}

	private void increaseGiftTodayTotalTriggerTimes() {
		getDb_Builder().getTimeLimitGiftInfoBuilder().setTodayTriggerTimes(getDb_Builder().getTimeLimitGiftInfo().getTodayTriggerTimes() + 1);
	}

	private boolean canTrigger(TimeLimitGiftConfigObject config, int exProperty) {
		if (config.getMusthave()) {
			// 必现礼包仅能出现一次
			return getDb_Builder().getTimeLimitGiftInfo().getGiftsMap().get(config.getId()) == null;
		}
		if (getDb_Builder().getTimeLimitGiftInfo().getTodayTriggerTimes() >= GameConfig.getById(GameConst.CONFIG_ID).getDailytimelimitgiftnum()) {
			return false;
		}
		if (config.getTriggerlimit() > 0) {
			DB_TimeLimitGiftItem db_timeLimitGift = getDb_Builder().getTimeLimitGiftInfo().getGiftsMap().get(config.getId());
			if (db_timeLimitGift != null && db_timeLimitGift.getTriggerTime() >= config.getTriggerlimit()) {
				return false;
			}
		}
		if (config.getLevellimit() > 0) {
			playerEntity player = playerCache.getByIdx(getLinkplayeridx());
			if (player == null || player.getLevel() <= config.getLevellimit()) {
				return false;
			}
		}
		return RandomUtil.getRandom1000() < config.getProbability() + exProperty;
	}

	private int getTriggerTimes(int id) {
		DB_TimeLimitGiftItem db_timeLimitGift = getDb_Builder().getTimeLimitGiftInfo().getGiftsMap().get(id);
		return db_timeLimitGift == null ? 0 : db_timeLimitGift.getTriggerTime();
	}

	public void sendWishUpdate() {
		SC_WishWellUpdate.Builder msg = SC_WishWellUpdate.newBuilder();
		for (WishingWellItem wish : getDb_Builder().getSpecialInfo().getWishingWell().getWishMapMap().values()) {
			msg.addWish(wish);
		}
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_WishWellUpdate_VALUE, msg);
	}

	private void sendWishUpdate(ActivityTime.Builder timeBuilder) {
		SC_WishWellUpdate.Builder msg = SC_WishWellUpdate.newBuilder();
		for (WishingWellItem wish : getDb_Builder().getSpecialInfo().getWishingWell().getWishMapMap().values()) {
			msg.addWish(wish);
		}
		if (timeBuilder != null) {
			msg.setTimeInfo(timeBuilder);
		}
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_WishWellUpdate_VALUE, msg);
	}

	public void sendWishUpdate(WishingWellItem.Builder wish) {
		SC_WishWellUpdate.Builder msg = SC_WishWellUpdate.newBuilder();
		msg.addWish(wish);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_WishWellUpdate_VALUE, msg);
	}

	public void sendNeeBeeGiftUpdate(int giftId) {
		NewBeeGiftCfgObject config = NewBeeGiftCfg.getById(giftId);
		Integer buyTimes = getDb_Builder().getSpecialInfo().getNewBeeGift().getGiftsMap().get(giftId);
		if (config != null && buyTimes != null) {
			SC_NewBeeGiftUpdate.Builder builder = SC_NewBeeGiftUpdate.newBuilder().setGiftId(giftId).setSoldOut(buyTimes >= config.getLimit());

			GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_NewBeeGiftUpdate_VALUE, builder);

		}
	}

	public void unLockWishWell() {
		WishWellActivityConfigObject config = WishWellActivityConfig.getById(GameConst.CONFIG_ID);
		if (config == null) {
			LogUtil.error("WishWellActivityConfig  config is null");
			return;
		}

		long currentTime = GlobalTick.getInstance().getCurrentTime();
		long overDisTime = TimeUtil.parseTime(config.getOverdistime());

		if (overDisTime < currentTime || TimeUtil.parseTime(config.getStartdistime()) > currentTime) {
			LogUtil.debug("许愿池活动未开始");
			return;
		}

		protocol.TargetSystemDB.DB_WishingWell.Builder wishingWellBuilder = getDb_Builder().getSpecialInfoBuilder().getWishingWellBuilder();
		if (wishingWellBuilder.getWishMapCount() > 0) {
			LogUtil.error("许愿池已解锁,玩家idx:{}", getLinkplayeridx());
			return;
		}
		wishingWellBuilder.setStartTime(currentTime);
		wishingWellBuilder.setEndTime(Math.min(overDisTime, TimeUtil.calculateActivityEndTime(wishingWellBuilder.getStartTime(), config.getDuration())));
		for (WishWellConfigObject wishConfig : WishWellConfig._ix_id.values()) {
			if (wishConfig.getId() <= 0) {
				continue;
			}

			wishingWellBuilder.putWishMap(wishConfig.getId(), WishingWellItem.newBuilder().setWishIndex(wishConfig.getId()).setState(WishStateEnum.WSE_UnChoose).setWishTime(currentTime + (wishConfig.getStartday() - 1) * TimeUtil.MS_IN_A_DAY).setClaimTime(currentTime + (wishConfig.getClaimday() - 1) * TimeUtil.MS_IN_A_DAY).build());

		}
		ActivityTime.Builder timeBuilder = getWishTimeBuilder(wishingWellBuilder.getStartTime(), wishingWellBuilder.getEndTime());
		sendWishUpdate(timeBuilder);
	}

	private ActivityTime.Builder getWishTimeBuilder(long startTime, long endTime) {
		Cycle_TimeLimit.Builder cycleBuilder = Cycle_TimeLimit.newBuilder();
		cycleBuilder.setBeginTimestamp(startTime);
		cycleBuilder.setEndTimestamp(endTime);
		ActivityTime.Builder timeBuilder = ActivityTime.newBuilder();
		timeBuilder.setTimeType(CycleTypeEnum.CTE_TimeLimit);
		timeBuilder.setTimeContent(cycleBuilder.build().toByteString());
		return timeBuilder;
	}

	public int getRankingActivityScore(long activityId, EnumRankingType rankingType) {
		DB_ActivityRanking activityRanking = getDb_Builder().getActivityRankingMap().get(activityId);
		if (activityRanking == null) {
			return 0;
		}

		if (rankingType == EnumRankingType.ERT_ArenaGainScore) {
			return activityRanking.getArenaScoreGain();
		} else if (rankingType == EnumRankingType.ERT_MineScore) {
			return activityRanking.getMineScore();
		}
		return 0;
	}

	// =================================魔灵降临
	// start===========================================

	public DB_DemonDescendsActivityInfo.Builder getDemonDescendsInfoBuilder(long activityId) {
		DB_DemonDescendsActivityInfo descendsActivityInfo = getDb_Builder().getDemonDescendsInfoMap().get(activityId);
		return descendsActivityInfo == null ? DB_DemonDescendsActivityInfo.newBuilder().setActivityId(activityId) : descendsActivityInfo.toBuilder();
	}

	public void putDemonDescendsInfoBuilder(DB_DemonDescendsActivityInfo.Builder builder) {
		if (builder == null) {
			return;
		}

		getDb_Builder().putDemonDescendsInfo(builder.getActivityId(), builder.build());
	}

	public void refreshDemonDescendsActivityInfo(long activityId) {
		SC_ClaimDemonDescendsInfo.Builder resultBuilder = SC_ClaimDemonDescendsInfo.newBuilder();
		DB_DemonDescendsActivityInfo.Builder demonBuilder = getDemonDescendsInfoBuilder(activityId);
		resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
		if (demonBuilder != null) {
			resultBuilder.setRecharge(demonBuilder.getRecharge());
			resultBuilder.setAlreadyBugCount(demonBuilder.getAlreadyBugCount());
			resultBuilder.addAllDailyMissionPro(demonBuilder.getDailyMissionProMap().values());
			resultBuilder.setScore(demonBuilder.getScore());
		}
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_ClaimDemonDescendsInfo_VALUE, resultBuilder);
	}

	private void resetDemonDescendsDailyMission() {
		HashSet<Long> activityIdSet = new HashSet<>(getDb_Builder().getDemonDescendsInfoMap().keySet());
		for (Long activityId : activityIdSet) {
			DB_DemonDescendsActivityInfo.Builder infoBuilder = getDemonDescendsInfoBuilder(activityId);
			infoBuilder.clearDailyMissionPro();
			putDemonDescendsInfoBuilder(infoBuilder);
		}
	}

	public TargetMission getDemonDescendsMissionPro(long activityId, int missionId) {
		DB_DemonDescendsActivityInfo.Builder infoBuilder = getDemonDescendsInfoBuilder(activityId);
		if (infoBuilder.getDailyMissionProCount() >= 0) {
			return infoBuilder.getDailyMissionProMap().get(missionId);
		}
		return null;
	}

	public void putDemonDescendsMissionPro(long activityId, TargetMission mission) {
		DB_DemonDescendsActivityInfo.Builder infoBuilder = getDemonDescendsInfoBuilder(activityId);
		infoBuilder.putDailyMissionPro(mission.getCfgId(), mission);
		putDemonDescendsInfoBuilder(infoBuilder);
	}

	private void doDemonDescends(TargetTypeEnum typeEnum, int addPro, int param) {
		List<Long> activityIdList = ActivityManager.getInstance().getOpenActivitiesIdByType(ActivityTypeEnum.ATE_DemonDescends);
		if (CollectionUtils.isEmpty(activityIdList)) {
			return;
		}

		Set<Long> needRefreshId = new HashSet<>();
		DemonDescendsConfigObject descendsConfig = DemonDescendsConfig.getById(GameConst.CONFIG_ID);
		for (Long activityId : activityIdList) {
			DB_DemonDescendsActivityInfo.Builder infoBuilder = getDemonDescendsInfoBuilder(activityId);
			// 添加充值进度
			if (typeEnum == TargetTypeEnum.TEE_Player_CumuRechargeCoupon) {
				DemonDescendsRechargeInfo.Builder rechargeBuilder = infoBuilder.getRechargeBuilder();
				int newAmount = rechargeBuilder.getRemainAmount() + addPro;
				// 可获得的奖励
				int canGetItemCount = newAmount / descendsConfig.getRechargerewardneedcoupon();
				// 剩余的充值数目
				int remainAmount = newAmount % descendsConfig.getRechargerewardneedcoupon();

				rechargeBuilder.setCanClaimItemCount(rechargeBuilder.getCanClaimItemCount() + canGetItemCount);
				rechargeBuilder.setRemainAmount(remainAmount);

				putDemonDescendsInfoBuilder(infoBuilder);

				needRefreshId.add(activityId);
			}

			for (int missionId : descendsConfig.getDailymission()) {
				MissionObject mission = Mission.getById(missionId);
				if (mission == null) {
					LogUtil.error("model.targetsystem.entity.targetsystemEntity.doDemonDescends, daily mission is not exist, mission id:" + mission);
					continue;
				}

				TargetMission oldPro = getDemonDescendsMissionPro(activityId, missionId);
				TargetMission newPro = addMissionPro(mission, typeEnum, addPro, param, getDemonDescendsMissionPro(activityId, missionId));
				if (missionUpdate(oldPro, newPro)) {
					putDemonDescendsMissionPro(activityId, newPro);
					needRefreshId.add(activityId);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(needRefreshId)) {
			for (Long aLong : needRefreshId) {
				refreshDemonDescendsActivityInfo(aLong);
			}
		}
	}

	// =================================魔灵降临
	// end===========================================

	// =================================哈迪斯的宝藏
	// start=========================================
	public DB_HadesActivityInfo.Builder getHadesActivityInfoBuilder(long activityId) {
		DB_HadesActivityInfo.Builder result;
		DB_HadesActivityInfo activityInfo = getDb_Builder().getHadesInfoMap().get(activityId);
		if (activityInfo == null) {
			int defaultTimes = HadesConfig.getById(GameConst.CONFIG_ID).getDefaulttimes();
			result = DB_HadesActivityInfo.newBuilder().setActivityId(activityId).setRemainTimes(defaultTimes);
		} else {
			result = activityInfo.toBuilder();
		}
		return result;
	}

	public void putHadesActivityInfoBuilder(DB_HadesActivityInfo.Builder builder) {
		if (builder == null) {
			return;
		}

		getDb_Builder().putHadesInfo(builder.getActivityId(), builder.build());
	}

	public void sendHadesActivityInfo(long activityId) {
		SC_ClaimHadesInfo.Builder builder = SC_ClaimHadesInfo.newBuilder();
		builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
		DB_HadesActivityInfo.Builder infoBuilder = getHadesActivityInfoBuilder(activityId);
		builder.setRemainTimes(infoBuilder.getRemainTimes());
		builder.setAlreadyWorshipTimes(infoBuilder.getAlreadyWorshipTimes());
		builder.addAllMissionStatus(infoBuilder.getMissionStatusMap().values());
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_ClaimHadesInfo_VALUE, builder);
	}

	public TargetMission getHadesMissionPro(long activityId, int index) {
		DB_HadesActivityInfo.Builder infoBuilder = getHadesActivityInfoBuilder(activityId);
		return infoBuilder.getMissionStatusMap().get(index);
	}

	public void putHadesMissionPro(long activityId, TargetMission newPro) {
		if (newPro == null) {
			return;
		}
		DB_HadesActivityInfo.Builder infoBuilder = getHadesActivityInfoBuilder(activityId);
		infoBuilder.putMissionStatus(newPro.getCfgId(), newPro);
		putHadesActivityInfoBuilder(infoBuilder);
	}

	private void doHadesTreasure(TargetTypeEnum typeEnum, int addPro, int param) {
		List<MissionObject> missionList = HadesConfig.getDailyMissionCfgByType(typeEnum);
		if (CollectionUtils.isEmpty(missionList)) {
			return;
		}

		List<Long> activityIds = ActivityManager.getInstance().getOpenActivitiesIdByType(ActivityTypeEnum.ATE_HadesTreasure);
		if (CollectionUtils.isEmpty(activityIds)) {
			return;
		}

		Set<Long> needRefresh = new HashSet<>();
		for (Long activityId : activityIds) {
			for (MissionObject missionCfg : missionList) {
				TargetMission before = getHadesMissionPro(activityId, missionCfg.getId());
				TargetMission after = addMissionPro(missionCfg, typeEnum, addPro, param, before);
				if (missionUpdate(before, after)) {
					putHadesMissionPro(activityId, after);
					needRefresh.add(activityId);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(needRefresh)) {
			for (Long refresh : needRefresh) {
				sendHadesActivityInfo(refresh);
			}
		}
	}

	private void resetHadesTreasureDailyMission() {
		HashSet<Long> longs = new HashSet<>(getDb_Builder().getHadesInfoMap().keySet());
		for (Long aLong : longs) {
			DB_HadesActivityInfo.Builder infoBuilder = getHadesActivityInfoBuilder(aLong);
			infoBuilder.clearMissionStatus();
			putHadesActivityInfoBuilder(infoBuilder);
		}
	}
	// =================================哈迪斯的宝藏
	// end===========================================

	// =================================天天充值
	// start=========================================
	public void sendDayDayRechargeUpdate() {
		SC_UpdateDayDayRechargeInfo.Builder result = SC_UpdateDayDayRechargeInfo.newBuilder();
		DB_DayDayRecharge.Builder dayDayRecharge = getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder();
		result.addAllClaimedRechargeIndex(dayDayRecharge.getClaimedIndexList());
		result.addAllCanClaimedRechargeIndex(dayDayRecharge.getCanClaimIndexList());
		result.setClaimTodayFree(dayDayRecharge.getClaimTodayFree());
		result.setShowRewardIndex(dayDayRecharge.getShowRewardIndex());
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_UpdateDayDayRechargeInfo_VALUE, result);
	}

	public void triggerDayDayRechargeReward(int nowRecharge, boolean sendMsg) {
		List<ServerActivity> activities = ActivityManager.getInstance().getOpenActivitiesByType(ActivityTypeEnum.ATE_DayDayRecharge);
		if (CollectionUtils.isEmpty(activities)) {
			return;
		}
		ServerActivity serverActivity = activities.get(0);
		if (!canTriggerDailyRecharge(activities.get(0), nowRecharge)) {
			return;
		}
		LogUtil.info("playerIdx:{} begin triggerDayDayRechargeReward ,nowRecharge:{}", getLinkplayeridx(), nowRecharge);
		DB_DayDayRecharge.Builder dayDayRechargeBuilder = getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder();
		List<Integer> canClaimIndexList = dayDayRechargeBuilder.getCanClaimIndexList();
		//最大可领取索引
		int index1 = canClaimIndexList.stream().max(Integer::compareTo).orElse(-1);
		//最大已领取索引
		int index2 = dayDayRechargeBuilder.getClaimedIndexList().stream().max(Integer::compareTo).orElse(-1);
		//下一个可开放的奖励索引
		int openRewardIndex = Math.max(index1, index2) + 1;
		int rechargeRewardCount = serverActivity.getDayDayRecharge().getRechargeRewardsCount();
		if (openRewardIndex >= rechargeRewardCount) {
			LogUtil.info("playerIdx:{} had trigger all DayDayRechargeRewards ", getLinkplayeridx());
			return;
		}
		getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder().setTriggerDailyRechargeReward(true);
		dayDayRechargeBuilder.addCanClaimIndex(openRewardIndex);
		LogUtil.info("playerIdx:{} trigger triggerDayDayRechargeReward reward,reward index:{} ", getLinkplayeridx(), openRewardIndex);
		if (triggerDayDayRechargeBigReward(canClaimIndexList, openRewardIndex, rechargeRewardCount)) {
			LogUtil.info("playerIdx:{} trigger triggerDayDayRechargeReward big reward ", getLinkplayeridx());
			dayDayRechargeBuilder.addCanClaimIndex(openRewardIndex + 1);
		}
		if (sendMsg) {
			sendDayDayRechargeUpdate();
		}
	}

	private boolean canTriggerDailyRecharge(ServerActivity serverActivity, int nowRecharge) {
		boolean triggerDailyRechargeReward = getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder().getTriggerDailyRechargeReward();
		if (triggerDailyRechargeReward) {
			return false;
		}
		int rechargeNeed = serverActivity.getDayDayRecharge().getDailyTarget();
		return rechargeNeed <= nowRecharge;
	}

	private boolean triggerDayDayRechargeBigReward(List<Integer> canClaimIndexList, int openRewardIndex, int rechargeRewardCount) {
		return openRewardIndex == rechargeRewardCount - 2 && !canClaimIndexList.contains(openRewardIndex + 1);
	}

	public void sendTimeLimitActivity(Collection<TimeLimitActivityObject> cfgList) {
		if (CollectionUtils.isEmpty(cfgList)) {
			return;
		}

		List<ClientActivity> clientActivities = buildTimeLimitClientActivityList(cfgList);
		if (CollectionUtils.isNotEmpty(clientActivities)) {
			SC_NewActivity.Builder builder = SC_NewActivity.newBuilder();
			builder.addAllActivitys(clientActivities);
			GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_NewActivity_VALUE, builder);
		}
	}

	public void sendZeroCostPurchaseUpdate(int cfgId) {
		Activity.ZeroCostPurchaseItem zeroCostPurchaseItem = getDb_Builder().getSpecialInfo().getZeroCostPurchase().getZeroCostPurchaseMap().get(cfgId);
		if (zeroCostPurchaseItem == null) {
			return;
		}
		Activity.SC_ZeroCostPurchaseUpdate.Builder msg = Activity.SC_ZeroCostPurchaseUpdate.newBuilder().setItem(zeroCostPurchaseItem);

		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_ZeroCostPurchaseUpdate_VALUE, msg);
	}

	public void sendDirectGiftPurchaseUpdate(long giftId) {
		Activity.SC_UpdatePurchaseGiftInfo.Builder msg = Activity.SC_UpdatePurchaseGiftInfo.newBuilder();

		msg.setGiftId(giftId);
		msg.setLimitBuy(getDirectGiftRemainBuyTimes(giftId));

		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_UpdatePurchaseGiftInfo_VALUE, msg);
	}

	private int getDirectGiftRemainBuyTimes(long giftId) {
		Integer buyTimes = getDb_Builder().getSpecialInfo().getDirectPurchaseGiftBuyRecordMap().get(giftId);
		Activity.DirectPurchaseGift directPurchaseGift = DirectGiftPurchaseHandler.getInstance().queryGiftByGiftId(giftId);
		int total = directPurchaseGift == null ? 0 : directPurchaseGift.getLimitBuy();
		int remain;
		if (buyTimes == null) {
			return total;
		}
		remain = total - buyTimes;
		return Math.max(remain, 0);
	}

	// =================================符文密藏
	// start===========================================

	public DB_RuneTreasureInfo.Builder getDbRuneTreasureInfoBuilder(long activityId) {
		DB_RuneTreasureInfo treasureInfo = getDb_Builder().getRuneTreasureInfoMap().get(activityId);
		return treasureInfo != null ? treasureInfo.toBuilder() : DB_RuneTreasureInfo.newBuilder().setActivityId(activityId);
	}

	public void putRuneTreasureInfoBuilder(DB_RuneTreasureInfo.Builder builder) {
		if (builder == null) {
			return;
		}

		getDb_Builder().putRuneTreasureInfo(builder.getActivityId(), builder.build());
	}

	public void resetRuneTreasureDailyMission() {
		for (Long activityId : getDb_Builder().getRuneTreasureInfoMap().keySet()) {
			DB_RuneTreasureInfo.Builder builder = getDbRuneTreasureInfoBuilder(activityId);
			builder.clearDailyMissionPro();
			putRuneTreasureInfoBuilder(builder);
		}
	}

	private void doRuneTreasure(TargetTypeEnum typeEnum, int addPro, int param) {
		List<MissionObject> missionList = RuneTreasure.getDailyMissionCfgByType(typeEnum);
		if (CollectionUtils.isEmpty(missionList)) {
			return;
		}

		List<Long> activityIds = ActivityManager.getInstance().getOpenActivitiesIdByType(ActivityTypeEnum.ATE_RuneTreasure);
		if (CollectionUtils.isEmpty(activityIds)) {
			return;
		}

		MultiValueMap<Long, TargetMission> needRefresh = new MultiValueMap<>();
		for (Long activityId : activityIds) {
			DB_RuneTreasureInfo.Builder treasureInfoBuilder = getDbRuneTreasureInfoBuilder(activityId);
			for (MissionObject missionCfg : missionList) {
				TargetMission before = treasureInfoBuilder.getDailyMissionProMap().get(missionCfg.getId());
				TargetMission after = addMissionPro(missionCfg, typeEnum, addPro, param, before);
				if (missionUpdate(before, after)) {
					treasureInfoBuilder.putDailyMissionPro(missionCfg.getId(), after);
					needRefresh.put(activityId, after);
				}
			}
			putRuneTreasureInfoBuilder(treasureInfoBuilder);
		}

		if (MapUtils.isNotEmpty(needRefresh)) {
			for (Long activityId : needRefresh.keySet()) {
				sendRuneTreasureMissionPro(activityId, needRefresh.getCollection(activityId));
			}
		}
	}

	private void sendRuneTreasureMissionPro(long activityId, Collection<TargetMission> newPro) {
		SC_UpdateRuneTreasureDailyMissionPro.Builder builder = SC_UpdateRuneTreasureDailyMissionPro.newBuilder();
		builder.setActivityId(activityId);
		if (CollectionUtils.isNotEmpty(newPro)) {
			builder.addAllNewPro(newPro);
		}
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_UpdateRuneTreasureDailyMissionPro_VALUE, builder);
	}

	// =================================符文密藏
	// end===========================================
	// =================================远征任务
	// start=========================================
	private void doTheWarSeasonMission(TargetTypeEnum typeEnum, int addPro, int param) {
		if (!TheWarManager.getInstance().open()) {
			LogUtil.debug("targetsystemEntity.doTheWarSeasonMission, the war is not open");
			return;
		}
		List<MissionObject> missions = TheWarManager.getInstance().getSeasonMissionsByTargetType(typeEnum);
		if (CollectionUtils.isEmpty(missions)) {
			return;
		}

		DB_TheWarSeasonMission.Builder theWarBuilder = getDb_Builder().getSpecialInfoBuilder().getTheWarSeasonMissionBuilder();
		for (MissionObject missionCfg : missions) {
			TargetMission before = theWarBuilder.getMissionProMap().get(missionCfg.getId());
			TargetMission after = addMissionPro(missionCfg, typeEnum, addPro, param, before);
			if (missionUpdate(before, after)) {
				theWarBuilder.putMissionPro(missionCfg.getId(), after);

				if (missionCfg.getId() == theWarBuilder.getCurMissionId()) {
					sendUpdateTheWarMissionMsg();
				}
			}
		}
	}

	public TargetMission getTheWarCurMission() {
		DB_TheWarSeasonMission.Builder theWarMissionBuilder = getDb_Builder().getSpecialInfoBuilder().getTheWarSeasonMissionBuilder();
		if (theWarMissionBuilder.getCurMissionId() == 0) {
			int nextMissionId = TheWarManager.getInstance().getSeasonMissionNextMissionId(theWarMissionBuilder.getCurMissionId());
			if (nextMissionId == -1 || nextMissionId == theWarMissionBuilder.getCurMissionId()) {
				LogUtil.error("targetsystemEntity.sendUpdateTheWarMissionMsg, can not get the war season mission next id, curId:" + theWarMissionBuilder.getCurMissionId());
				return null;
			} else {
				theWarMissionBuilder.setCurMissionId(nextMissionId);
			}
		}

		TargetMission mission = theWarMissionBuilder.getMissionProMap().get(theWarMissionBuilder.getCurMissionId());
		if (mission == null) {
			mission = TargetMission.newBuilder().setCfgId(theWarMissionBuilder.getCurMissionId()).build();
		}

		return mission;
	}

	public void sendUpdateTheWarMissionMsg() {
		TargetMission mission = getTheWarCurMission();
		if (mission == null) {
			return;
		}
//        SC_UpdateTheWarMission.Builder builder = SC_UpdateTheWarMission.newBuilder().setCurMission(mission);
//        GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_UpdateTheWarMission_VALUE, builder);
	}

	private void sendDailyFirstRechargeUpdate(TargetSystemDB.DB_DailyFirstRecharge.Builder recharge) {
		Activity.SC_UpdateDailyFirstRecharge.Builder msg = Activity.SC_UpdateDailyFirstRecharge.newBuilder();
		msg.setExploreTime(recharge.getExploreTimes());
		msg.addAllEarnedReward(recharge.getEarnedRewardList());
		msg.setRechargeDays(recharge.getRechargeDays());
		msg.setState(recharge.getState());
		msg.addAllDailyReward(DailyFirstRechargeManage.getInstance().queryDailyReward());
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_UpdateDailyFirstRecharge_VALUE, msg);
	}

	public void sendDailyFirstRechargeUpdate() {
		Activity.SC_UpdateDailyFirstRecharge.Builder msg = Activity.SC_UpdateDailyFirstRecharge.newBuilder();
		TargetSystemDB.DB_DailyFirstRecharge recharge = getDb_Builder().getSpecialInfo().getDailyFirstRecharge();
		msg.setExploreTime(recharge.getExploreTimes());
		msg.addAllEarnedReward(recharge.getEarnedRewardList());
		msg.setRechargeDays(recharge.getRechargeDays());
		msg.setState(recharge.getState());
		msg.addAllDailyReward(DailyFirstRechargeManage.getInstance().queryDailyReward());
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_UpdateDailyFirstRecharge_VALUE, msg);
	}

	public void triggerDailyFirstRecharge(int todayLastCumuRecharge) {
		if (todayLastCumuRecharge == 0 && DailyFirstRechargeManage.getInstance().activityOpen()) {
			LogUtil.info("playerId:{} triggerDailyFirstRecharge success:{}", getLinkplayeridx());
			activeTodayDailyFirstRecharge();
		}
	}

	private void activeTodayDailyFirstRecharge() {
		TargetSystemDB.DB_DailyFirstRecharge.Builder dailyFirstRechargeBuilder = getDb_Builder().getSpecialInfoBuilder().getDailyFirstRechargeBuilder();
		if (dailyFirstRechargeBuilder.getState() == PayActivityStateEnum.PAS_NotActive) {
			dailyFirstRechargeBuilder.setState(PayActivityStateEnum.PAS_SignOn);
		}
		dailyFirstRechargeBuilder.setRechargeDays(DailyFirstRechargeManage.getInstance().increaseRechargeDays(dailyFirstRechargeBuilder.getRechargeDays()));
		dailyFirstRechargeBuilder.setExploreTimes(DailyFirstRechargeManage.getInstance().increaseExploreTimes(dailyFirstRechargeBuilder.getExploreTimes(), dailyFirstRechargeBuilder.getRechargeDays()));
		sendDailyFirstRechargeUpdate(dailyFirstRechargeBuilder);
	}

	public void updatePatrolMissionSwitch(boolean pause) {
		TargetSystemDB.DB_PatrolMission.Builder patrolMission = getDb_Builder().getPatrolMissionBuilder();
		if (patrolMission.getEndTime() <= 0 || MissionStatusEnum.MSE_Finished == patrolMission.getMission().getStatus()) {
			return;
		}
		if (pause && patrolMission.getPauseTime() == 0) {
			patrolMission.setPauseTime(GlobalTick.getInstance().getCurrentTime());
		}
		if (!pause && patrolMission.getPauseTime() > 0) {
			long pauseDuringTime = GlobalTick.getInstance().getCurrentTime() - patrolMission.getPauseTime();
			patrolMission.setEndTime(patrolMission.getEndTime() + pauseDuringTime).clearPauseTime();
		}
		if (GlobalData.getInstance().checkPlayerOnline(getLinkplayeridx())) {
			sendPatrolMissionUpdate();
		}
	}

	// =================================远征任务
	// end===========================================
	// =================================成长轨迹
	// start===========================================
	private void doGrowthTrack(TargetTypeEnum typeEnum, int addPro, int param) {
		DB_GrowthTrack.Builder trackBuilder = getGrowthTrackBuilder();

		long currentTime = GlobalTick.getInstance().getCurrentTime();
		if (typeEnum == TargetTypeEnum.TTE_CumuLogin) {
			if (!GrowthTrack.containLoginMission(trackBuilder.getCurMissionGroupIdsList()) || currentTime < trackBuilder.getNextCanUpdateCumuLoginTime()) {
				return;
			}
		}

		List<MissionObject> missions = GrowthTrack.getSatisfyMissions(typeEnum);
		if (CollectionUtils.isEmpty(missions)) {
			return;
		}

		List<TargetMission> modifyList = new ArrayList<>();
		for (MissionObject mission : missions) {
			if (mission == null) {
				LogUtil.warn("model.targetsystem.entity.targetsystemEntity.doGrowthTrack, mission is null, targetType:" + typeEnum);
				continue;
			}
			TargetMission beforeActivityPro = trackBuilder.getMissionsMap().get(mission.getId());
			TargetMission afterTargetMission = addMissionPro(mission, typeEnum, addPro, param, beforeActivityPro);

			if (missionUpdate(beforeActivityPro, afterTargetMission)) {
				trackBuilder.putMissions(afterTargetMission.getCfgId(), afterTargetMission);
				//主线闯关都是相同的任务,发多了客户端卡
				if (modifyList.size() < 8) {
					modifyList.add(afterTargetMission);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(modifyList)) {
			sendRefreshGrowthTrackProgressMsg(modifyList);

			if (typeEnum == TargetTypeEnum.TTE_CumuLogin) {
				trackBuilder.setNextCanUpdateCumuLoginTime(TimeUtil.getNextDayResetTime(currentTime));
			}
		}
	}

	public void sendRefreshGrowthTrackProgressMsg(List<TargetMission> modifyList) {
		if (CollectionUtils.isEmpty(modifyList) || !PlayerUtil.queryFunctionUnlock(getLinkplayeridx(), EnumFunction.GrowthTrack)) {
			return;
		}

		SC_RefreshGrowTrackProgress.Builder builder = SC_RefreshGrowTrackProgress.newBuilder();
		builder.addAllMissions(modifyList);
		builder.addAllCurMissionGroupIds(getGrowthTrackBuilder().getCurMissionGroupIdsList());
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreshGrowTrackProgress_VALUE, builder);
	}

	public DB_GrowthTrack.Builder getGrowthTrackBuilder() {
		DB_GrowthTrack.Builder growthTrackBuilder = getDb_Builder().getGrowthTrackBuilder();
		if (growthTrackBuilder.getCurMissionGroupIdsCount() <= 0) {
			growthTrackBuilder.addAllCurMissionGroupIds(GrowthTrack.getDefaultMissionGroupIdsSet());

		}
		return growthTrackBuilder;
	}

	public boolean activeAdvanceFeats(int type) {
		DB_Feats db_Feats = getDb_Builder().getFeatsInfosMap().get(type);
		if (db_Feats == null) {
			return false;
		}
		return db_Feats.getFeatsType() == 1;
	}

	// =================================成长轨迹
	// start===========================================

	// ================================大富翁 start
	// ===============================================

	public void sendRichManInfoUpdate() {
		Activity.SC_UpdateRichManInfo.Builder msg = Activity.SC_UpdateRichManInfo.newBuilder();
		TargetSystemDB.DB_RichMan richMan = getDb_Builder().getSpecialInfo().getRichMan();
		msg.setCycle(richMan.getCycle());
		msg.setCurPoint(richMan.getCurPoint());
		msg.setDischargeRebate(richMan.getDischargeRebate());
		msg.setDoubleReward(richMan.getDoubleReward());
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_UpdateRichManInfo_VALUE, msg);
	}

	// ===============================大富翁 end
	// =======================================

	// =================================活动通用(大富翁活动版本新增)
	// start===========================================

	/**
	 * 发送玩家所有活动购买相关记录
	 */
	public void sendTotalActivityGoodsInfo() {
		Activity.SC_TotalActivityGoodsInfo.Builder sendMsg = Activity.SC_TotalActivityGoodsInfo.newBuilder();
		for (TargetSystemDB.DB_ActivityBuyInfo buyInfo : getDb_Builder().getActivityBuyInfoList()) {
			Activity.ActivityGoodsBuyInfo.newBuilder().setActivityId(buyInfo.getActivityId()).setBuyRecord(buyInfo.getBuyRecord());
		}
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_TotalActivityGoodsInfo_VALUE, sendMsg);
	}

	/**
	 * 与客户端同步活动购买相关记录
	 */
	public void sendUpdateActivityGoodsInfo(long activityId) {
		Activity.SC_UpdateActivityGoodsInfo.Builder sendMsg = Activity.SC_UpdateActivityGoodsInfo.newBuilder();
		Activity.ActivityGoodsBuyInfo.Builder builder = Activity.ActivityGoodsBuyInfo.newBuilder().setActivityId(activityId);
		Optional<TargetSystemDB.DB_ActivityBuyInfo> dbRecord = getDb_Builder().getActivityBuyInfoList().stream().filter(e -> e.getActivityId() == activityId).findFirst();
		dbRecord.ifPresent(activityBuyMission -> builder.setBuyRecord(activityBuyMission.getBuyRecord()));
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_UpdateActivityGoodsInfo_VALUE, sendMsg);
	}

	/**
	 * 与客户端活同步连续领取相关(如大富翁宝箱奖励)
	 */
	public void sendUpdateStageRewardInfo(long activityId) {
		Activity.SC_UpdateStageRewardInfo.Builder sendMsg = Activity.SC_UpdateStageRewardInfo.newBuilder();
		Activity.StageRewardInfo.Builder stageInfo = Activity.StageRewardInfo.newBuilder();
		Optional<TargetSystemDB.DB_StageRewardClaimInfo> dbRecord = getDb_Builder().getStageRewardClaimInfoList().stream().filter(e -> e.getActivityId() == activityId).findFirst();
		dbRecord.ifPresent(dbStageInfo -> stageInfo.setActivityId(activityId).addAllClaimedIndex(dbRecord.get().getClaimedIndexList()));
		sendMsg.setInfos(stageInfo);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_UpdateStageRewardInfo_VALUE, sendMsg);
	}

	/**
	 * 发送玩家所有连续奖励领取相关记录(如大富翁宝箱奖励)
	 */
	public void sendTotalStageRewardInfo() {
		Activity.SC_TotalStageRewardInfo.Builder sendMsg = Activity.SC_TotalStageRewardInfo.newBuilder();
		for (TargetSystemDB.DB_StageRewardClaimInfo dbRecord : getDb_Builder().getStageRewardClaimInfoList()) {
			Activity.StageRewardInfo.Builder stageInfo = Activity.StageRewardInfo.newBuilder();
			stageInfo.setActivityId(dbRecord.getActivityId()).addAllClaimedIndex(dbRecord.getClaimedIndexList());
			sendMsg.addStageRewardInfo(stageInfo);
		}
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_TotalStageRewardInfo_VALUE, sendMsg);
	}

	public void updateStageRewardTarget(long activityId, int newValue) {
		TargetSystemDB.DB_StageRewardClaimInfo.Builder builder = pullOneDbStageReward(activityId);
		builder.setCurTarget(newValue);
		getDb_Builder().addStageRewardClaimInfo(builder);
	}

	public TargetSystemDB.DB_StageRewardClaimInfo.Builder pullOneDbStageReward(long activityId) {
		TargetSystemDB.DB_StageRewardClaimInfo record;
		for (int i = 0; i < getDb_Builder().getStageRewardClaimInfoBuilderList().size(); i++) {
			record = getDb_Builder().getStageRewardClaimInfo(i);
			if (record.getActivityId() == activityId) {
				getDb_Builder().removeStageRewardClaimInfo(i);
				return record.toBuilder();
			}
		}
		return TargetSystemDB.DB_StageRewardClaimInfo.newBuilder().setActivityId(activityId);
	}

	public TargetSystemDB.DB_ActivityBuyInfo getActivityBuyPro(long activityId) {
		List<TargetSystemDB.DB_ActivityBuyInfo> InfoList = getDb_Builder().getActivityBuyInfoList();
		for (TargetSystemDB.DB_ActivityBuyInfo db_activityBuyInfo : InfoList) {
			if (db_activityBuyInfo.getActivityId() == activityId) {
				return db_activityBuyInfo;
			}
		}
		return TargetSystemDB.DB_ActivityBuyInfo.getDefaultInstance();
	}

	public int getActivityItemBuyTimes(ServerActivity serverActivity, int index) {
		if (serverActivity.getType() == ActivityTypeEnum.ATE_RuneTreasure) {
			TargetMission targetMission = getDbRuneTreasureInfoBuilder(serverActivity.getActivityId()).getBuyMissionProMap().get(index);
			return targetMission == null ? 0 : targetMission.getProgress();
		}
		/*
		 * //这里用list存储只是避开使用proto Map if
		 * (serverActivity.getType()==ActivityTypeEnum.ATE_RichMan){ return
		 * getActivityItemBuyTimesFromListData(serverActivity.getActivityId(),index); }
		 */
		// 老活动走的map
		return getActivityItemBuyTimesFromMapData(serverActivity, index);
	}

	private int getActivityItemBuyTimesFromMapData(ServerActivity serverActivity, int index) {
		TargetMission dbActivityPro = getDBActivityPro(serverActivity.getActivityId(), index);
		return dbActivityPro == null ? 0 : dbActivityPro.getProgress();
	}

	private int getActivityItemBuyTimesFromListData(long activityId, int index) {
		TargetSystemDB.DB_ActivityBuyInfo activityBuyPro = getActivityBuyPro(activityId);
		Common.IntMap buyRecord = activityBuyPro.getBuyRecord();
		for (int i = 0; i < buyRecord.getKeysList().size(); i++) {
			if (buyRecord.getKeysList().get(i) == index) {
				return buyRecord.getValues(i);
			}

		}
		return 0;
	}

	public void clearActivityBuyMissionPro(long activityId, boolean sendMsg) {
		DB_Activity.Builder builder = getDBActivityBuilder(activityId).clearMissionPro();
		getDb_Builder().putActivities(activityId, builder.build());
		playerEntity player = playerCache.getByIdx(getLinkplayeridx());
		if (player != null) {
			player.clearActivitiesData(activityId);
		}
		if (sendMsg) {
			sendRefreshActivityMission(buildRefreshMission(activityId, TargetMission.getDefaultInstance()));
		}

	}

	public int queryRichManRebate() {
		return getDb_Builder().getSpecialInfo().getRichMan().getDischargeRebate();
	}

	public void sendBeforeRichManInfo() {
		Activity.SC_BeforeRichManEnter.Builder msg = Activity.SC_BeforeRichManEnter.newBuilder();
		TargetSystemDB.DB_RichMan.Builder db_richMan = getDb_Builder().getSpecialInfoBuilder().getRichManBuilder();
		// 没有每日奖励了,恒定为true 要不然客户端红点有问题
		msg.setClaimDailyReward(true);
		msg.setDischargeRebate(db_richMan.getDischargeRebate());
		msg.setCycle(db_richMan.getCycle());
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_BeforeRichManEnter_VALUE, msg);
	}

	// =================================活动通用
	// end===========================================

	// =================================

	public void sendBusinessPopupUpdate(TargetSystem.BusinessPopupItem item) {
		TargetSystem.SC_BusinessUpdate.Builder msg = TargetSystem.SC_BusinessUpdate.newBuilder().setPopup(item);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_BusinessUpdate_VALUE, msg);

	}

	public void sendBusinessPopupMsgInit() {
		TargetSystem.SC_BusinessPopupInit.Builder msg = TargetSystem.SC_BusinessPopupInit.newBuilder();
		List<TargetSystem.BusinessPopupItem> db_businessPopups = getDb_Builder().getDbBusinessPopupBuilder().getBusinessItemsList();
		List<TargetSystem.BusinessPopupItem> activeBusinessPopup = db_businessPopups.stream().filter(e -> e.getExpireTime() > GlobalTick.getInstance().getCurrentTime()).collect(Collectors.toList());
		if (db_businessPopups.size() != activeBusinessPopup.size()) {
			getDb_Builder().getDbBusinessPopupBuilder().clearBusinessItems().addAllBusinessItems(activeBusinessPopup);
		}
		msg.addAllPopups(activeBusinessPopup);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_BusinessPopupInit_VALUE, msg);
	}

	public TargetSystem.BusinessPopupItem.Builder getOneBusinessPopup(int missionId) {
		return getDb_Builder().getDbBusinessPopupBuilder().getBusinessItemsBuilderList().stream().filter(item ->
				item.getCfgId() == missionId && item.getExpireTime() > GlobalTick.getInstance().getCurrentTime()).min(
						Comparator.comparingLong(TargetSystem.BusinessPopupItem.Builder::getExpireTime)).orElse(null);

	}

	private void sendBusinessPopup(TargetSystem.BusinessPopupItem popupItem) {
		TargetSystem.SC_AddBusinessPopup.Builder msg = TargetSystem.SC_AddBusinessPopup.newBuilder().setPopup(popupItem);
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_AddBusinessPopup_VALUE, msg);
	}

	private TargetSystem.BusinessPopupItem buildNewPopup(PopupMissionObject mission) {
		TargetSystem.BusinessPopupItem.Builder builder = TargetSystem.BusinessPopupItem.newBuilder();
		builder.setPopupId(IdGenerator.getInstance().generateIdNum());
		builder.setCfgId(mission.getId());
		builder.setExpireTime(GlobalTick.getInstance().getCurrentTime() + mission.getLimittime() * TimeUtil.MS_IN_A_S);
		return builder.build();
	}

	private void resetBusinessPopup() {
		todayTotalBusinessPopupCount = 0;
		playerBusinessPopupMap.clear();
		popupMissionProMap.clear();
	}

	private void saveBusinessPopupRecord(TargetSystem.BusinessPopupItem popupItem) {
		TargetSystemDB.DB_BusinessPopup.Builder dbBusinessPopupBuilder = getDb_Builder().getDbBusinessPopupBuilder();
		todayTotalBusinessPopupCount++;
		dbBusinessPopupBuilder.addBusinessItems(popupItem);
		MapUtil.add2IntMapValue(playerBusinessPopupMap, popupItem.getCfgId(), 1);
		// 完成后把任务移除,重新计数,因为有些任务会触发多次
		popupMissionProMap.remove(popupItem.getCfgId());
	}

	private int getTotalTriggerBusinessPopupCount() {
		return todayTotalBusinessPopupCount;
	}

	private int todayTotalBusinessPopupCount = 0;

	private final Map<Integer, TargetMission> popupMissionProMap = new ConcurrentHashMap<>();

	private boolean canTriggerBusinessPopup(PopupMissionObject mission, int addPro, int param) {
		if (getTotalTriggerBusinessPopupCount() >= GameConfig.getById(GameConst.CONFIG_ID).getPopupdailylimit()) {
			return false;
		}
		int dailyLimitTrigger;
		if ((dailyLimitTrigger = mission.getDailytriggerlimit()) != -1 && dailyLimitTrigger <= getTodayPopupCount(mission)) {
			return false;
		}

		TargetMission beforePro = getPopupMissionPro(mission.getId());

		TargetMission afterPro = addMissionPro(mission.getId(), TargetTypeEnum.forNumber(mission.getMissiontype()), mission.getAddition(), mission.getTarget(), TargetTypeEnum.forNumber(mission.getMissiontype()), addPro, param, beforePro);

		popupMissionProMap.put(mission.getId(), afterPro);

		if (MissionStatusEnum.MSE_Finished != afterPro.getStatus()) {
			return false;
		}

		playerEntity player = playerCache.getByIdx(getLinkplayeridx());
		if (player == null) {
			return false;
		}
		if (mission.getViplv() > player.getVip()) {
			return false;
		}

		return true;
	}

	private TargetMission getPopupMissionPro(int missionId) {
		TargetMission target = popupMissionProMap.get(missionId);
		if (target == null) {
			target = TargetMission.newBuilder().setCfgId(missionId).build();
		}
		return target;
	}

	private int getTodayPopupCount(PopupMissionObject mission) {
		Integer result = playerBusinessPopupMap.get(mission.getId());
		return result == null ? 0 : result;
	}

	// =================================活动通用
	// end===========================================

	// =================================竞技场排位赛擂台赛
	// start===========================================
	private void doMatchArenaTaskMission(TargetTypeEnum typeEnum, int addPro, int param) {
		List<MissionObject> list = new ArrayList<>();
		if (typeEnum == null || typeEnum == TargetTypeEnum.TTE_NULL) {
			return;
		}
		MatchArenaLTObject arenaCfg = MatchArenaLT.getById(GameConst.CONFIG_ID);
		if (arenaCfg == null) {
			return;
		}
		for (int i : arenaCfg.getMissionlist()) {
			MissionObject missionCfg = Mission.getById(i);
			if (missionCfg != null && missionCfg.getMissiontype() == typeEnum.getNumber()) {
				list.add(missionCfg);
			}
		}
		List<TargetMission> refresh = new ArrayList<>();
		for (MissionObject mission : list) {
			TargetMission beforeActivityPro = getDb_Builder().getMatchArenaInfoMap().get(mission.getId());
			TargetMission afterTargetMission = addMissionPro(mission, typeEnum, addPro, param, beforeActivityPro);
			if (missionUpdate(beforeActivityPro, afterTargetMission)) {
				refresh.add(afterTargetMission);
				getDb_Builder().putMatchArenaInfo(afterTargetMission.getCfgId(), afterTargetMission);
			}
		}

		if (CollectionUtils.isNotEmpty(refresh)) {
			TargetSystem.SC_RefMatchArenaTaskMission.Builder refreshBuilder = TargetSystem.SC_RefMatchArenaTaskMission.newBuilder();
			refreshBuilder.addAllMission(refresh);
			GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefMatchArenaTaskMission_VALUE, refreshBuilder);
		}
	}

	private void doCrossArenaTaskMission(TargetTypeEnum typeEnum, int addPro, int param) {
		List<MissionObject> list = new ArrayList<>();
		if (typeEnum == null || typeEnum == TargetTypeEnum.TTE_NULL) {
			return;
		}
		CrossArenaCfgObject arenaCfg = CrossArenaCfg.getById(GameConst.CONFIG_ID);
		if (arenaCfg == null) {
			return;
		}
		for (int i : arenaCfg.getMission()) {
			MissionObject missionCfg = Mission.getById(i);
			if (missionCfg != null && missionCfg.getMissiontype() == typeEnum.getNumber()) {
				list.add(missionCfg);
			}
		}
		List<TargetMission> refresh = new ArrayList<>();
		for (MissionObject mission : list) {
			TargetMission beforeActivityPro = getDb_Builder().getCrossArenaInfoMap().get(mission.getId());
			TargetMission afterTargetMission = addMissionPro(mission, typeEnum, addPro, param, beforeActivityPro);
			if (missionUpdate(beforeActivityPro, afterTargetMission)) {
				refresh.add(afterTargetMission);
				getDb_Builder().putCrossArenaInfo(afterTargetMission.getCfgId(), afterTargetMission);
			}
		}

		if (CollectionUtils.isNotEmpty(refresh)) {
			TargetSystem.SC_RefCrossArenaTaskMission.Builder refreshBuilder = TargetSystem.SC_RefCrossArenaTaskMission.newBuilder();
			refreshBuilder.addAllMission(refresh);
			GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefCrossArenaTaskMission_VALUE, refreshBuilder);
		}
	}

	// =================================竞技场排位赛擂台赛
	// end===========================================

	public void sendFestivalBossInfoUpdate(String playerIdx,long activityId) {
		Activity.SC_FestivalBossInfoUpdate.Builder msg = Activity.SC_FestivalBossInfoUpdate.newBuilder();
		TargetSystemDB.DB_FestivalBoss db_festivalBoss = getDb_Builder().getFestivalBossInfoMap().get(activityId);
		msg.setPlayerScore(db_festivalBoss.getCumeScore());
		msg.setAlreadyChallengeTimes(db_festivalBoss.getTodayChallengeTimes());
		msg.setPresentTime(db_festivalBoss.getPresentTimes());
		msg.setLastDamage(db_festivalBoss.getLastDamage());
		msg.setAlreadyChallengeTimes(db_festivalBoss.getTodayChallengeTimes());
		GlobalData.getInstance().sendMsg(playerIdx,SC_FestivalBossInfoUpdate_VALUE,msg);

	}

	private void sendItemCard() {
		SC_RefreshItemCard.Builder builder = SC_RefreshItemCard.newBuilder();

		for (Entry<Integer, DB_ItemCard> ent : getDb_Builder().getItemCardMap().entrySet()) {
			ItemCardData.Builder b = ItemCardData.newBuilder();
//			b.setHave(ent.getValue().getHave());
			b.setIndex(ent.getKey());
			b.setToday(ent.getValue().getToday());
			TimeUtil.getNextDaysStamp(System.currentTimeMillis(), ent.getValue().getHave());
			b.setEndtime(TimeUtil.getNextDaysStamp(System.currentTimeMillis(), ent.getValue().getHave()));
			builder.addItemCard(b);
		}
		GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreshItemCard_VALUE, builder);
	}

	private void resetItemCard() {
		Map<Integer, DB_ItemCard> map = getDb_Builder().getItemCardMap();
		Map<Integer, DB_ItemCard> newMap = new HashMap<>();
//	TargetSystem.SC_RefCrossArenaTaskMission.Builder refreshBuilder = TargetSystem.SC_RefCrossArenaTaskMission.newBuilder();
		for (Map.Entry<Integer, DB_ItemCard> ent : map.entrySet()) {

			DB_ItemCard.Builder builder = ent.getValue().toBuilder();
			if (builder.getToday() == 0) {// TODO 邮件代领
			} else {
				builder.setToday(0);
			}

			int have = builder.getHave();
			have--;
			if (have < 0) {// 道具卡结束

			} else {
				newMap.put(ent.getKey(), builder.setHave(have).build());
			}
		}
		getDb_Builder().clearItemCard();
		getDb_Builder().putAllItemCard(newMap);
//	GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefCrossArenaTaskMission_VALUE, refreshBuilder);
	}

	public int getGongXunId(int type) {
		switch (type) {
		case GameConst.FEAT_TYPE_HUOYUE:
			if (GameConfig.getById(GameConst.CONFIG_ID).getFeatidlist().length >= 1) {
				return GameConfig.getById(GameConst.CONFIG_ID).getFeatidlist()[0];
			} else {
				return -1;
			}
		case GameConst.FEAT_TYPE_WUJIN:
			if (GameConfig.getById(GameConst.CONFIG_ID).getFeatidlist().length >= 2) {
				return GameConfig.getById(GameConst.CONFIG_ID).getFeatidlist()[1];
			} else {
				return -1;
			}
		case GameConst.FEAT_TYPE_XUKONG:
			if (GameConfig.getById(GameConst.CONFIG_ID).getFeatidlist().length >= 3) {
				return GameConfig.getById(GameConst.CONFIG_ID).getFeatidlist()[2];
			} else {
				return -1;
			}
		default:
			return -1;
		}
	}

	private void clearRollCard() {
		getDb_Builder().getRollGodTempBuilder().clearDaily();
	}

	/**
	 * 更新增量类排行榜（如统计活动期间消耗，充值金额等）
	 * @param rankingType
	 * @param addVal
	 */
	public void updateIncrRankingScore(EnumRankingType rankingType, int addVal) {
		List<ServerActivity> activities = ActivityManager.getInstance().findInOpenRankingActivity(rankingType);
		for (ServerActivity activity : activities) {
			Long cumnVal = getDb_Builder().getRankingTargetMap().getOrDefault(activity.getActivityId(), 0L);
			long finalVal = cumnVal + addVal;
			getDb_Builder().putRankingTarget(activity.getActivityId(), finalVal);
			RankingManager.getInstance().updatePlayerRankingScore(getLinkplayeridx(), rankingType, RankingUtils.getActivityRankingName(activity), finalVal);
		}
	}
}

class CompareType {
	/**
	 * 大于等于
	 */
	public static final int GRATER_OR_EQUAL = 1;
	/**
	 * 等于
	 */
	public static final int EQUAL = 2;

	/**
	 * 小于等于
	 */
	public static final int LESS_OR_EQUAL = 3;
}