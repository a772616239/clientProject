package server.event;

import cfg.GameConfig;
import cfg.HelpPetCfg;
import cfg.MainLineCheckPoint;
import cfg.MainLineCheckPointObject;
import cfg.MainLineNode;
import cfg.MainLineNodeObject;
import cfg.PayRewardConfig;
import cfg.ResourceCopy;
import cfg.ResourceCopyObject;
import cfg.ShuraArenaConfig;
import cfg.ShuraArenaConfigObject;
import cfg.TeamPosition;
import cfg.TeamPositionObject;
import cfg.TeamsConfig;
import cfg.TeamsConfigObject;
import cfg.TheWarMapConfig;
import cfg.TheWarMapConfigObject;
import cfg.TimeLimitActivity;
import cfg.TimeLimitActivityObject;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import com.bowlong.sql.AtomicInt;
import common.GameConst;
import common.GameConst.EventType;
import common.GameConst.RankingName;
import common.GameConst.StarTreasureConstant;
import common.GameConst.WarPetUpdate;
import common.GlobalData;
import common.HttpRequestUtil;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import datatool.StringHelper;
import entity.UpdateActivityDropCount;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.activity.petAvoidance.PetAvoidanceGameData;
import model.activity.petAvoidance.PetAvoidanceGameManager;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.battle.BattleManager;
import model.bosstower.BossTowerManager;
import model.bosstower.dbCache.bosstowerCache;
import model.bosstower.entity.bosstowerEntity;
import model.bravechallenge.dbCache.bravechallengeCache;
import model.bravechallenge.entity.bravechallengeEntity;
import model.comment.dbCache.commentCache;
import model.cp.CpTeamManger;
import model.crazyDuel.CrazyDuelManager;
import model.crossarena.CrossArenaManager;
import model.crossarena.dbCache.playercrossarenaCache;
import model.crossarena.entity.playercrossarenaEntity;
import model.crossarenapvp.CrossArenaPvpManager;
import model.foreignInvasion.dbCache.foreigninvasionCache;
import model.foreignInvasion.newVersion.NewForeignInvasionManager;
import model.gameplay.dbCache.gameplayCache;
import model.gloryroad.GloryRoadManager;
import model.gloryroad.entity.gloryroadEntity;
import model.inscription.dbCache.petinscriptionCache;
import model.inscription.petinscriptionEntity;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.magicthron.MagicThronManager;
import model.magicthron.dbcache.magicthronCache;
import model.magicthron.entity.magicthronEntity;
import model.mailbox.entity.mailboxEntity;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.mainLine.util.MainLineUtil;
import model.matcharena.MatchArenaManager;
import model.matcharena.dbCache.matcharenaCache;
import model.matcharena.entity.matcharenaEntity;
import model.mistforest.MistForestManager;
import model.obj.BaseObj;
import model.patrol.dbCache.patrolCache;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.patrol.entity.PatrolTree;
import model.patrol.entity.patrolEntity;
import model.pet.dbCache.petCache;
import model.pet.entity.FightPowerCalculate;
import model.pet.entity.petEntity;
import model.petfragment.dbCache.service.PetFragmentServiceImpl;
import model.petfragment.entity.petfragmentEntity;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import model.petmission.dbCache.petmissionCache;
import model.petmission.entity.petmissionEntity;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import model.ranking.ranking.AbstractRanking;
import model.redpoint.RedPointManager;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.rollcard.RollCardManager;
import model.shop.dbCache.shopCache;
import model.shop.entity.shopEntity;
import model.stoneRift.StoneRiftCfgManager;
import model.stoneRift.StoneRiftWorldMapManager;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.stoneriftEntity;
import model.targetsystem.TargetSystemUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.team.dbCache.teamCache;
import model.team.entity.TeamsDB;
import model.team.entity.teamEntity;
import model.team.util.TeamsUtil;
import model.thewar.TheWarManager;
import model.timer.TimerConst.TimerTargetType;
import model.training.TrainingManager;
import model.training.dbCache.trainingCache;
import model.training.entity.trainingEntity;
import model.warpServer.battleServer.BattleServerManager;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import platform.PlatformManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.MistPlayTimeLog;
import platform.purchase.PurchaseManager;
import protocol.Activity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.EnumRankingType;
import protocol.Activity.SC_ActivityBossUpdate;
import protocol.Activity.SC_PetAvoidanceEnd;
import protocol.Arena.ArenaRecord;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Comment.CommentTypeEnum;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.Common.SC_UpdateDailyData;
import protocol.EndlessSpire.SC_RefreashSpireLv;
import protocol.LoginProto.ClientData;
import protocol.MagicThron;
import protocol.MagicThronDB.DB_MagicThron;
import protocol.MagicThronDB.MagicBattleRecord;
import protocol.Mail.MailStatusEnum;
import protocol.MailDB.DB_MailInfo;
import protocol.MainLine.MainLineProgress;
import protocol.MainLineDB.DB_MainLine.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.SC_KickOutFromMistForest;
import protocol.MistForest.SC_MazeItemCollectCount;
import protocol.PetMessage.Pet;
import protocol.PlayerDB.DB_PlayerData;
import protocol.PlayerDB.DB_ResourceCopy;
import protocol.PlayerDB.EndlessSpireInfo;
import protocol.PlayerInfo.DisplayPet;
import protocol.PlayerInfo.SC_RefreshDisplayPet;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.ResourceCopy.SC_RefreshResCopy;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.DB_TimerParam;
import protocol.Server.ServerActivity;
import protocol.ServerTransfer.GS_CS_ClearRewardAndRankRecord;
import protocol.ServerTransfer.GS_CS_RemoveWarPetData;
import protocol.ServerTransfer.GS_CS_UpdatePlayerBaseAdditions;
import protocol.ServerTransfer.GS_CS_UpdateWarPetData;
import protocol.Shop.ShopTypeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TargetSystem.TimeLimitGiftType;
import protocol.TargetSystemDB;
import protocol.TargetSystemDB.DB_StarTreasureActivity;
import protocol.TransServerCommon.MistActivityBossPlayerData;
import protocol.TransServerCommon.PlayerMistServerInfo;
import server.event.sub.CrossArenaGradeAdd;
import server.handler.resRecycle.ResourceRecycleManager;
import util.ArrayUtil;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static protocol.MessageId.MsgIdEnum.SC_PlayerRecoverScene_VALUE;

public class EventListener {
    public static boolean listenEvent() {
        int result = 1;
        result &= listenEvent(EventType.ET_Login, new LoginEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_Logout, new LogoutEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AddCurrency, new AddCurrencyHandler()) ? 1 : 0;

//        result &= listenEvent(EventType.ET_ReduceCurrency, new ReduceCurrencyEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_RemoveItem, new RemoveItemEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_AddItem, new AddItemEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AddAvatar, new AddAvatarEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_AddAvatarBorder, new AddAvatarBorderEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_UnlockTeamAndPosition, new UnlockTeamAndPositionEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_RemoveDisplayPet, new RemoveDisplayPetEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AddPet, new AddPetEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AddPetRune, new AddPetRuneEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_AddPetFragment, new AddPetFragmentEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_ResetRuneStatus, new ResetRuneStatusEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_SetPetMissionStatus, new SetPetMissionStatusEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_ResetPetMissionStatus, new ResetPetMissionStatusEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AddMail, new AddMailEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AddVIPExp, new AddVIPExpEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_AddExp, new AddExpEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_MainLineBattleSettle, new MainLineBattleSettleEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_UnlockMainLine, new UnlockMainLineEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_VipLvUp, new VipLvUpEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_RemoveTeamDeadPet, new RemoveTeamDeadPetHandler()) ? 1 : 0;

        // 目标系统
        result &= listenEvent(EventType.ET_UpdateTargetProgress, new UpdateTargetProgressHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_ClearAllMistTargetProgress, new ClearMistTargetProgressHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_UpdateBossActivityTime, new UpdateBossActivityTimeHandler()) ? 1 : 0;

        // 迷雾森林
        result &= listenEvent(EventType.ET_MistForestSeasonEnd, new MistForestSeasonEndEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_ClearMistItem, new ClearMistItemHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_MistForestServerClosed, new MistForestServerCloseHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_MistBossActivityBegin, new MistBossActivityBeginHandler()) ? 1 : 0;

        // 矿区争夺
//		result &= listenEvent(EventType.ET_MineFightServerClosed, new MineFightServerCloseHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_LockPetTeam, new LockPetTeamHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_UPDATE_ACTIVITY_DROP_COUNT, new updateActivityDropCountHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_TimerInvoke, new TimerInvokeHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_UPDATE_UNLOCK_MIST_LV, new updateUnlockMistLvHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_RECORD_ARENA_BATTLE, new RecordArenaBattleHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_CLEAR_TEAM, new ClearTeamHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_REMOVE_PET_FROM_TEAMS, new RemovePetFromTeamsHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_UPDATE_PET_TEAM_STATE, new UpdatePetTeamState()) ? 1 : 0;

        result &= listenEvent(EventType.ET_ENDLESS_SPIRE_BATTLE_SETTLE, new EndlessSpireBattleSettle()) ? 1 : 0;
//        result &= listenEvent(EventType.ET_FOREIGN_INVASION_BATTLE_SETTLE, new ForeignInvasionBattleSettle()) ? 1 : 0;
        result &= listenEvent(EventType.ET_RESOURCE_COPY_BATTLE_SETTLE, new ResourceCopyBattleSettle()) ? 1 : 0;
        result &= listenEvent(EventType.ET_PATROL_BATTLE_SETTLE, new PatrolBattleSettleHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_BATTLE_PLAYER_LEAVE, new BattlePlayerLeave()) ? 1 : 0;

        result &= listenEvent(EventType.ET_PLAYER_RECHARGE_ACTIVITY, new RechargeActivityHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AUTO_REFRESH_SHOP, new AutoRefreshShop()) ? 1 : 0;

        result &= listenEvent(EventType.ET_Add_Limit_Purchase_Recharge_Id, new AddLimitPurchaseRecharge()) ? 1 : 0;

        result &= listenEvent(EventType.ET_ReCreateMonsterDiff, new ReCreateMonsterHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_CLEAR_ALL_PLAYER_ACTIVITY_INFO, new ClearAllPlayerActivityInfo()) ? 1 : 0;

        result &= listenEvent(EventType.ET_TIME_LIMIT_GIFT, new AddTimeLimitGiftHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_ADD_REPORT_TIMES, new AddReportTimesHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_BAN, new BanHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_SHIELD_COMMENT, new ShieldCommentHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_TheWar_UpdateWarRoomIdx, new UpdateWarRoomIdxHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_WarPet_Update, new WarPetUpdateHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_MIST_CLEAR_ALL_TIME_LIMIT_PROGRESS, new ClearAllPlayerMistTimeLimitMissionProgress()) ? 1 : 0;

        result &= listenEvent(EventType.ET_UnlockWishWell, new UnlockWishWellHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AllPetAdditionUpdate, new AllPetAdditionUpdateHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AddPetGem, new AddPetGemEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_ResetPetGemStatus, new ResetPetGemStatusHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_UpdatePlayerRecharge, new UpdatePlayerRechargeHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_UnlockTimeLimitActivity, new UnlockTimeLimitActivityHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_NewForeignInvasionSendPlayerBuildingInfo, new NewForeignInvasionPlayerBuildingInfoHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_ClearMistTimeLimitMission, new ClearMistTimeLimitHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_UpdatePetMissionLvUpPro, new UpdatePetMissionLvUpProHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_ClearAllPlayerTheWarSeasonMissionPro, new ClearAllPlayerTheWarSeasonMissionProHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_AddPointCopyScore, new AddPointCopyScoreHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_DailyUpdatePlayerAllFunction, new DailyUpdatePlayerAllFunctionHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_WeeklyUpdatePlayerAllFunction, new WeeklyUpdatePlayerAllFunctionHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_UpdateRanking, new UpdateRankingHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_UpdatePatrolMissionSwitch, new UpdatePatrolMissionSwitchHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AddNewTitle, new AddNewTitleHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_ClearClaimRecordOnPlayer, new ClearClaimRecordOnPlayerHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AddGloryRoadQuizRecord, new AddGloryRoadQuizRecordHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_GloryRoadBattleResult, new GloryRoadBattleResultHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_RefreshMazeData, new RefreshMazeDataHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_SettleMazeActivity, new SettleMazeActivityHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_CollectMazeItem, new CollectMazeItemHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_AddInscription, new AddPetInscriptionEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_RefreshPetData, new RefreshPetDataHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_TRAIN_BATTLE_SETTLE, new TrainBattleSettleHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TRAIN_RANK_SETTLE, new TrainRankSettleHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_UnLockFunction, new UnLockFunctionHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_CoupTeamUpdate, new CoupTeamUpdateHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TrainItemAdd, new TrainItemAddHandler()) ? 1 : 0;
//		result &= listenEvent(EventType.ET_MAGICTHRON_PVE, new MagicThronPveHandler()) ? 1 : 0;
//		result &= listenEvent(EventType.ET_MAGICTHRON_PVP, new MagicThronPvpHandler()) ? 1 : 0;
//		result &= listenEvent(EventType.ET_MAGICTHRON_BOSS, new MagicThronBossHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_ClearHelpPet, new ClearHelpPetHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_CollectArtifactExp, new CollectArtifactExpHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_CollectPet, new CollectPetHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_MagicThronRecord, new MagicThronRecordHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_RemoveInscription, new RemoveInscriptionHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_OpenTraining, new OpenTraingHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_OfferRewardFight, new OfferRewardFight()) ? 1 : 0;
        result &= listenEvent(EventType.ET_CROSSARENAEVENT_BATTLE_SETTLE, new CrossArenaEventBattleEnd()) ? 1 : 0;
        result &= listenEvent(EventType.ET_AddCrossArenaGrade, new CrossArenaGradeAdd()) ? 1 : 0;
        result &= listenEvent(EventType.ET_CrossArenaBoss, new CrossArenaBoss()) ? 1 : 0;
        result &= listenEvent(EventType.ET_CompleteStoneRiftMission, new CompleteStoneRiftMissionHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_UpdateStoneRiftAchievement, new UpdateStoneRiftAchievementHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_UpdateCrossArenaWeeklyTask, new UpdateCrossArenaWeeklyTaskHandler()) ? 1 : 0;
		result &= listenEvent(EventType.ET_UpdateIncrRankingScore, new UpdateIncrRankingScoreHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_AddStoneRiftFactoryExp, new AddStoneRiftFactoryExpHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_UpdateStarCount, new UpdateStarCountHandler()) ? 1 : 0;              ////充值获得获得星元
        result &= listenEvent(EventType.ET_PetAvoidanceGameTimeOver, new PetAvoidanceGameTimeOverHandler()) ? 1: 0;
        result &= listenEvent(EventType.ET_AddMoveEffect, new AddMistMoveEffectHandler()) ? 1: 0;

        return result == 1;
    }

    public static boolean listenEvent(int eventId, EventHandler handler) {
        return EventManager.getInstance().listenEvent(eventId, handler);
    }
}

final class LoginEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.checkParamSize(4)) {
                String playerIdx = event.getParam(0);
                boolean isResume = event.getParam(1);
                ClientData clientData = event.getParam(2);
                boolean isNewPlayer = event.getParam(3);

                recoverPlayerLogoutScene(playerIdx);

                playerEntity player = playerCache.getByIdx(playerIdx);
                if (player != null) {
                    if (clientData != null) {
                        SyncExecuteFunction.executeConsumer(player, entity -> {
                            entity.setClientData(clientData.toBuilder());
                            entity.getDb_data().setLanguage(clientData.getLanguage());
                        }); // 需要提前处理,否中迷雾森林奖励日志没渠道等信息
                    }

                    // 迷雾森林结算逻辑在每日刷新之前
                    MistForestManager.getInstance().onPlayerLogin(player);
                    if (SyncExecuteFunction.executePredicate(player, p -> player.needUpdateDailyData())) {
                        DailyUpdatePlayerAllFunctionHandler.dailyUpdatePlayerAllFunction(playerIdx, false);
                    }

                    if (SyncExecuteFunction.executePredicate(player, p -> player.needUpdateWeeklyData())) {
                        WeeklyUpdatePlayerAllFunctionHandler.weeklyUpdatePlayerAllFunction(playerIdx, false);
                    }
                    if (isResume) {
                        BattleServerManager.getInstance().quitWatch(playerIdx);
                    }
                    SyncExecuteFunction.executeConsumer(player, e -> player.onPlayerLogin(isResume, isNewPlayer));
                }

                // 其他模块的login
//                ForeignInvasionManager.getInstance().onPlayerLogin(playerIdx);
                NewForeignInvasionManager.getInstance().onPlayerLogIn(playerIdx);
                targetsystemCache.getInstance().onPlayerLogin(playerIdx);
                PlatformManager.getInstance().onPlayerLogIn(playerIdx);
                BattleManager.getInstance().onPlayerLogin(playerIdx, isResume);
                PurchaseManager.getInstance().onPlayerLogin(playerIdx);
                TheWarManager.getInstance().onPlayerLogin(playerIdx);
                itembagCache.getInstance().onPlayerLogin(playerIdx);
                GloryRoadManager.getInstance().onPlayerLogin(playerIdx, false);
                MatchArenaManager.getInstance().onPlayerLogin(playerIdx);
                TrainingManager.getInstance().onPlayerLogIn(playerIdx);
                CrossArenaManager.getInstance().onPlayerLogIn(playerIdx,isResume);
                CrossArenaPvpManager.getInstance().onPlayerLogIn(playerIdx);
                teamCache.getInstance().onPlayerLogIn(playerIdx);
				RedPointManager.getInstance().onPlayerLogIn(playerIdx, isResume);

                // http 模块防止阻塞, 放到最后执行
                if (player != null && !isResume) {
                    HttpRequestUtil.antiLogIn(player);

                    HttpRequestUtil.platformAppsflyerLogin(player);
                    if (isNewPlayer) {
                        HttpRequestUtil.reportNewPlayerData(player);
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

	/**
	 * 玩家登录/重连 告知客户端重新回到某个的游戏场景
	 *
	 * @param playerIdx
	 */
	private void recoverPlayerLogoutScene(String playerIdx) {
		Common.SC_PlayerRecoverScene.Builder msg = Common.SC_PlayerRecoverScene.newBuilder();

        if (CpTeamManger.getInstance().playerInMatchTeam(playerIdx)) {
            msg.setScene(Common.RecoverSceneEnum.RSE_CP_Team);
        } else if (CpTeamManger.getInstance().playerInCpCopy(playerIdx)) {
            msg.setScene(Common.RecoverSceneEnum.RSE_CP_Copy);
        }
        GlobalData.getInstance().sendMsg(playerIdx, SC_PlayerRecoverScene_VALUE, msg);
    }
}

final class LogoutEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof playerEntity) {
                playerEntity player = (playerEntity) obj;
                player.onPlayerLogout();
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddCurrencyHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof playerEntity && event.checkParamSize(3)) {
                playerEntity player = (playerEntity) obj;
                RewardTypeEnum rewardType = event.getParam(0);
                int addCount = event.getParam(1);
                Reason reason = event.getParam(2);
                player.addCurrency(rewardType, addCount, reason);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

//final class ReduceCurrencyEventHandler implements EventHandler {
//    @Override
//    public boolean onEvent(BaseObj obj, Event event) {
//        try {
//            if (obj instanceof playerEntity && event.checkParamSize(2)) {
//                playerEntity player = (playerEntity) obj;
//                String playerIdx = player.getIdx();
//                LogUtil.info("playerIdx[" + playerIdx + "],ReduceCurrencysEventHandler");
//                RewardTypeEnum rewardType = event.getParam(0);
//                int removeCount = event.getParam(1);
//
//                if (rewardType == null || removeCount <= 0) {
//                    LogUtil.info("params error");
//                    return false;
//                }
//
//                if (!player.currencyIsEnough(rewardType, removeCount)) {
//                    LogUtil.info("currency is not enough, need Currency,type: " + rewardType + ",needCount: " + removeCount);
//                    return false;
//                }
//
//                DB_PlayerData.Builder db_dataBuilder = player.getDb_data();
//                if (db_dataBuilder == null) {
//                    LogUtil.error("playerIdx[" + playerIdx + "] DB_Data is null");
//                    return false;
//                }
//
//                if (rewardType == RewardTypeEnum.RTE_Gold) {
//                    player.setGold(player.getGold() - removeCount);
//                } else if (rewardType == RewardTypeEnum.RTE_Diamond) {
//                    player.setDiamond(player.getDiamond() - removeCount);
//                }
//                player.sendCurrencyRefreshMsg(rewardType);
//                return true;
//            }
//            return false;
//        } catch (Exception e) {
//            LogUtil.printStackTrace(e);
//            return false;
//        }
//    }
//}

final class RemoveItemEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof itembagEntity && event.checkParamSize(2)) {
                itembagEntity itemBag = (itembagEntity) obj;
                Map<Integer, Integer> removeItemMap = event.getParam(0);
                Reason reason = event.getParam(1);
                return itemBag.removeItemByMap(removeItemMap, reason);
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddItemEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof itembagEntity && event.checkParamSize(2)) {
                itembagEntity itemBag = (itembagEntity) obj;
                String playerIdx = itemBag.getLinkplayeridx();
                LogUtil.debug("playerIdx[" + playerIdx + "] AddItemEventHandler");
                Map<Integer, Integer> addItemMap = event.getParam(0);
                Reason reason = event.getParam(1);
                itemBag.addItem(addItemMap, reason);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddAvatarEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof playerEntity && event.checkParamSize(1)) {
                playerEntity player = (playerEntity) obj;
                Collection<Integer> avatarCfgIdList = event.getParam(0);

                List<Integer> repeated = player.addAvatar(avatarCfgIdList);
                if (repeated != null && !repeated.isEmpty()) {
                    // TODO 重复获得补偿?
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddAvatarBorderEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof playerEntity && event.checkParamSize(1)) {
                playerEntity player = (playerEntity) obj;
                Collection<Integer> avatarCfgIdList = event.getParam(0);

                player.addAvatarBorder(avatarCfgIdList);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

/**
 * 等级解锁小队和位置
 */
final class UnlockTeamAndPositionEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof teamEntity) {
                teamEntity teams = (teamEntity) obj;
                int level = PlayerUtil.queryPlayerLv(teams.getLinkplayeridx());

                TeamsDB dbTeams = teams.getDB_Builder();
                if (dbTeams == null) {
                    LogUtil.error("UnlockTeamAndPositionEventHandler error, dbTeams is null");
                    return false;
                }

                // 是否发生改变
                boolean changedHappen = false;

                int unlockTeam = dbTeams.getUnlockTeams();
                int buyTeamCount = dbTeams.getBuyTeamCount();

                int unlockPosition = dbTeams.getUnlockPosition();

                int nextUnlockTeam = unlockTeam - buyTeamCount + 1;
                int nextUnlockPosition = unlockPosition + 1;

                for (; ; nextUnlockTeam++) {
                    TeamsConfigObject teamCfg = TeamsConfig.getByTeamid(nextUnlockTeam);
                    if (teamCfg != null) {
                        if (teamCfg.getUnlockneedlv() <= level) {
                            teams.unlockOneTeam(false);
                            changedHappen = true;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }

                for (; ; nextUnlockPosition++) {
                    TeamPositionObject byPositionId = TeamPosition.getByPositionid(nextUnlockPosition);
                    if (byPositionId != null) {
                        if (byPositionId.getUnlocklv() <= level) {
                            teams.unlockOnePosition();
                            changedHappen = true;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }

                if (changedHappen) {
                    // 更新魔晶宠物编队
                    teams.updateCoupTeam();

                    teams.sendTeamsInfo();
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

/**
 * 当有宠物放生或者移除时调用此事件，同时移除在玩家面板上展示的宠物
 */
final class RemoveDisplayPetEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof playerEntity && event.checkParamSize(1)) {
                playerEntity player = (playerEntity) obj;
                List<String> petIdx = event.getParam(0);
                if (petIdx == null || petIdx.size() < 1) {
                    return true;
                }

                DB_PlayerData.Builder db_dataBuilder = player.getDb_data();
                if (db_dataBuilder == null) {
                    LogUtil.error("playerIdx[" + player.getIdx() + "] dbData is null");
                    return false;
                }

                Set<Integer> removeKey = new HashSet<>();
                for (Entry<Integer, String> entry : db_dataBuilder.getDisplayPetMap().entrySet()) {
                    for (String idx : petIdx) {
                        if (idx.equalsIgnoreCase(entry.getValue())) {
                            removeKey.add(entry.getKey());
                        }
                    }
                }

                if (removeKey.size() <= 0) {
                    return true;
                }

                for (Integer integer : removeKey) {
                    db_dataBuilder.removeDisplayPet(integer);
                }

                SC_RefreshDisplayPet.Builder refreashBuilder = SC_RefreshDisplayPet.newBuilder();

                for (Entry<Integer, String> entry : db_dataBuilder.getDisplayPetMap().entrySet()) {
                    DisplayPet.Builder display = DisplayPet.newBuilder();
                    display.setPosition(entry.getKey());
                    display.setLinkPetIdx(entry.getValue());
                    refreashBuilder.addDisplayPet(display);
                }
                GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_RefreshDisplayPet_VALUE, refreashBuilder);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddPetEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof petEntity && event.checkParamSize(2)) {
            petEntity entity = (petEntity) obj;
            Map<Integer, Integer> petAddMap = event.getParam(0);
            Reason source = event.getParam(1);
            petCache.getInstance().playerObtainPets(entity.getPlayeridx(), petAddMap, source);
            return true;
        }
        return false;
    }
}

final class AddPetRuneEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof petruneEntity) {
            petruneEntity runeCache = (petruneEntity) obj;
            Map<Integer, Integer> runeMap = event.getParam(0);
            Reason reason = event.getParam(1);
            petruneCache.getInstance().playerObtainRune(runeCache, runeMap, reason);
            return true;
        }
        return false;
    }
}

final class AddPetFragmentEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof petfragmentEntity && event.checkParamSize(2)) {
            petfragmentEntity fragmentCache = (petfragmentEntity) obj;
            Map<Integer, Integer> fragmentMap = event.getParam(0);
            Reason reason = event.getParam(1);
            PetFragmentServiceImpl.getInstance().playerObtainFragment(fragmentCache.getPlayeridx(), fragmentMap, reason);
            return true;
        }
        return false;
    }
}

final class AddMailEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof mailboxEntity && event.checkParamSize(2)) {
            try {
                mailboxEntity mailbox = (mailboxEntity) obj;
                DB_MailInfo.Builder mail = event.getParam(0);
                Reason source = event.getParam(1);

                checkRewards(mailbox, mail, source);

                if (mail.getRewardsCount() > 0) {
                    mail.setMailStatus(MailStatusEnum.MSE_AttachmentUnRead);
                } else {
                    mail.setMailStatus(MailStatusEnum.MSE_NoAttachmentUnread);
                }
                mailbox.addMail(mail.build(), source);
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }

    private void checkRewards(mailboxEntity entity, DB_MailInfo.Builder mail, Reason reason) {
        if (entity == null || mail == null || mail.getRewardsCount() <= 0) {
            return;
        }

        List<Reward> filterReward = new ArrayList<>();
        for (Reward reward : mail.getRewardsList()) {
            if (reward == null || reward.getCount() <= 0) {
                LogUtil.info("server.event.AddMailEventHandler, playerIdx:" + entity.getLinkplayeridx() + ", mail template:" + mail.getMailTemplateId() + ", reward:" + RewardUtil.toJsonStr(Collections.singletonList(reward)) + ", is null or count is <= 0, reason:" + reason);
            } else {
                filterReward.add(reward);
            }
        }

        if (mail.getRewardsCount() != filterReward.size()) {
            LogUtil.warn("server.event.AddMailEventHandler, find error rewards, playerIdx:" + entity.getLinkplayeridx() + ", mail template:" + mail.getMailTemplateId() + ", before:" + RewardUtil.toJsonStr(mail.getRewardsList()) + ", after:" + RewardUtil.toJsonStr(filterReward) + ", reason:" + reason);

            mail.clearRewards();
            mail.addAllRewards(filterReward);
        }
    }
}

final class ResetRuneStatusEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof petruneEntity && event.checkParamSize(1)) {
            try {
                petruneCache.getInstance().resetRuneStatus(((petruneEntity) obj).getPlayeridx(), event.getParam(0));
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class SetPetMissionStatusEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof petEntity) {
            try {
                ((petEntity) obj).updatePetMissionStatus(event.getParam(0), true);
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class ResetPetMissionStatusEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof petEntity) {
            try {
                ((petEntity) obj).updatePetMissionStatus(event.getParam(0), false);
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class AddVIPExpEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof playerEntity && event.checkParamSize(1)) {
            try {
                int addVipExp = event.getParam(0);
                playerEntity player = (playerEntity) obj;
                player.addVipExp(addVipExp);
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class AddExpEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof playerEntity && event.checkParamSize(1)) {
            try {
                int addExp = event.getParam(0);
                playerEntity player = (playerEntity) obj;
                player.addExperience(addExp);
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class MainLineBattleSettleEventHandler implements EventHandler {
    /**
     * 歧路型找上一个节点计数
     */
    private AtomicInt findCount = new AtomicInt();

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof mainlineEntity && event.checkParamSize(3)) {
            mainlineEntity entity = (mainlineEntity) obj;
           return SyncExecuteFunction.executeFunction(entity, mainline -> {
                int nodeId = event.getParam(0);
                int winnerCamp = event.getParam(1);
                long teamAbility = event.getParam(2);

                Builder db_data = entity.getDBBuilder();
                if (db_data == null) {
                    LogUtil.error("playerIdx[" + entity.getLinkplayeridx() + "] mainLineDbData is null");
                    return true;
                }

                MainLineProgress.Builder mainLineProBuilder = db_data.getMainLineProBuilder();
                int curCheckPoint = mainLineProBuilder.getCurCheckPoint();

                MainLineCheckPointObject checkPointCfg = MainLineCheckPoint.getById(curCheckPoint);
                if (checkPointCfg == null) {
                    LogUtil.error("MainLineCfg[" + curCheckPoint + "] is null");
                    return true;
                }

                // 如果未胜利,检查前一个关卡是否是密码型关卡,是且密码输入错误,返回到上一个关卡
                if (winnerCamp != 1) {
                    MainLineCheckPointObject prePointCfg = MainLineCheckPoint.getById(checkPointCfg.getBeforecheckpoint());
                    if (prePointCfg != null && prePointCfg.getType() == 1) {
                        if (!entity.pswIsRight(checkPointCfg.getBeforecheckpoint())) {
                            entity.fallbackPoint(checkPointCfg.getBeforecheckpoint());
                        }
                    }
                    entity.sendRefreshMainLineMsg();
                    return true;
                }

                if (needClearHelpPet(nodeId)) {
                    EventUtil.clearHelpPet(entity.getLinkplayeridx(), Common.EnumFunction.MainLine);
                }

                MainLineNodeObject curNodeCfg = MainLineNode.getById(nodeId);
                if (curNodeCfg == null || !ArrayUtil.intArrayContain(checkPointCfg.getNodelist(), nodeId)) {
                    LogUtil.error("CurCheckPoint do not contain node id or nodeIdCfg is null, checkId = " + curCheckPoint + ", nodeId = " + nodeId);
                    return true;
                }

                // 添加进度
                entity.addProgress(nodeId);
//            //添加当前玩家到最近通关,以节点保存
//            mainlinerecentpassCache.getInstance().addRecentPassed(nodeId, entity.getLinkplayeridx());
                // 更新排行榜
                entity.updateRanking(nodeId);
                // 目标：通过主线关卡节点
                EventUtil.triggerUpdateTargetProgress(entity.getLinkplayeridx(), TargetTypeEnum.TTE_PassMianLineChapter, nodeId, 0);
                passNodeUnlockFunction(entity.getLinkplayeridx(), nodeId);

                mainLineProBuilder.setLastOperationtNode(nodeId);
                // 普通关卡胜利就通关, 特殊关卡击杀boss,或者完成所有节点
                if (checkPointCfg.getType() == 0 || curNodeCfg.getNodetype() == 2 || MainLineUtil.isFinished(checkPointCfg.getNodelist(), mainLineProBuilder.getProgressList())) {
                    doCurCheckPointRemainReward(entity.getLinkplayeridx(), checkPointCfg, mainLineProBuilder.getProgressList());
                    entity.passCurCheckPoint();
                } else {
                    // 歧路型到达末尾
                    if (checkPointCfg.getType() == 3 && (curNodeCfg.getAfternodeid() == null || curNodeCfg.getAfternodeid().length <= 0)) {
                        findCount.set(0);
                        int lastBranchRoad = getLastBranchRoad(mainLineProBuilder.getProgressList(), checkPointCfg.getNodelist(), curNodeCfg);
                        MainLineNodeObject lastCfg = MainLineNode.getById(lastBranchRoad);
                        if (lastCfg != null) {
                            int[] ints = MainLineUtil.removeAlreadyPassed(lastCfg.getAfternodeid(), mainLineProBuilder.getProgressList());
                            entity.addUnlockNode(ints, true);
                            // 设置到上次操作节点到歧路型分叉点
                            mainLineProBuilder.setLastOperationtNode(lastBranchRoad);
                        } else {
                            LogUtil.error("MainLineBattleSettleEventHandler.onEvent, can not find branch road node" + "， curNode = " + nodeId + "], recently pass Cur point");
                            entity.passCurCheckPoint();
                        }
                    } else if (checkPointCfg.getType() == 4) {
                        // 更新解锁关卡
                        entity.addUnlockNode(curNodeCfg.getAfternodeid(), true);

                        // 更新关卡进度
                        int lastTransferNode = mainLineProBuilder.getLastTransferNode();
                        if (ArrayUtil.intArrayContain(checkPointCfg.getNodelist(), lastTransferNode) && !mainLineProBuilder.getProgressList().contains(lastTransferNode)) {
                            mainLineProBuilder.addProgress(lastTransferNode);
                        }
                        mainLineProBuilder.clearLastTransferNode();
                    } else if (checkPointCfg.getType() == 5) {
                        entity.addUnlockNode(curNodeCfg.getAfternodeid(), false, nodeId);
                    } else {
                        entity.addUnlockNode(curNodeCfg.getAfternodeid(), true);
                    }
                }
                // 切换挂机节点
                if (PlayerUtil.queryFunctionUnlock(entity.getLinkplayeridx(), Common.EnumFunction.AutoOnHook) && curNodeCfg.getOnhookable()) {
                    entity.changeOnHookNode(nodeId);
                }
                entity.unlockEpisode(nodeId);
                // 发送关卡进度
                entity.sendRefreshMainLineMsg();
                EventUtil.triggerTimeLimitGift(entity.getLinkplayeridx(), TimeLimitGiftType.TLG_MainLine, mainlineCache.getInstance().getCurOnHookNode(entity.getLinkplayeridx()));
                // 训练营解锁
                EventUtil.openTraining(entity.getLinkplayeridx(), nodeId);

               if (teamAbility > 0) {
                   entity.updateMaxPassAbility(teamAbility);
               }

                LogUtil.info("MainLineBattleSettleEventHandler finish,player:{} cur mainline node:{}", entity.getLinkplayeridx(), db_data.getMainLinePro().getCurCheckPoint());
                return true;
            });
        }
        return false;
    }

    private void passNodeUnlockFunction(String playerIdx, int nodeId) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        List<Common.EnumFunction> functions = player.queryCanUnlockFunctionByKeyNode(nodeId);
        if (CollectionUtils.isNotEmpty(functions)) {
            EventUtil.triggerUnlockFunction(playerIdx, functions);
        }
    }

    private boolean needClearHelpPet(int nodeId) {
        return HelpPetCfg._ix_id.values().stream().anyMatch(e -> e.getMainlinenode() == nodeId);
    }

    /**
     * 查找上一个未通过完毕的节点
     *
     * @param proList
     * @param curNodeCfg
     * @return -1 未找到
     */
    private int getLastBranchRoad(List<Integer> proList, int[] curPointNodeList, MainLineNodeObject curNodeCfg) {
        // 寻找次数计数,防止死循环
        if (findCount.incrementAndGet() > 5) {
            return -1;
        }

        if (proList == null || curPointNodeList == null || curNodeCfg == null) {
            return -1;
        }

        MainLineNodeObject lastNode = MainLineNode.getById(curNodeCfg.getParam());
        if (lastNode == null || !ArrayUtil.intArrayContain(curPointNodeList, lastNode.getId())) {
            return -1;
        }

        if (!MainLineUtil.intContainAll(proList, lastNode.getAfternodeid())) {
            return lastNode.getId();
        } else {
            return getLastBranchRoad(proList, curPointNodeList, lastNode);
        }
    }

    /**
     * 主线闯关中，当玩家通过当前特殊关卡时，补发当前关卡未通过节店关卡的奖励
     *
     * @param playerIdx
     * @param curPointCfg
     * @param progressList
     */
    private void doCurCheckPointRemainReward(String playerIdx, MainLineCheckPointObject curPointCfg, List<Integer> progressList) {
        // 普通关卡不做此项
        if (curPointCfg == null || curPointCfg.getType() == 0 || playerIdx == null || progressList == null) {
            return;
        }

        int[] nodeList = curPointCfg.getNodelist();
        if (nodeList == null) {
            return;
        }

        List<Reward> allRewards = new ArrayList<>();
        for (int i = 0; i < nodeList.length; i++) {
            if (progressList.contains(nodeList[i])) {
                continue;
            }

            MainLineNodeObject nodeCfg = MainLineNode.getById(nodeList[i]);
            if (nodeCfg == null) {
                LogUtil.error("MainLieNodeCfg is null, nodeId = " + nodeList[i]);
                continue;
            }

            if (nodeCfg.getNodetype() == 1 || nodeCfg.getNodetype() == 2) {
                List<Reward> rewardsByFightMakeId = RewardUtil.getRewardsByFightMakeId(nodeCfg.getFightmakeid());
                if (rewardsByFightMakeId != null) {
                    allRewards.addAll(rewardsByFightMakeId);
                }
            }
        }

        List<Reward> rewards = RewardUtil.mergeReward(allRewards);
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MainLineCheckPoint, "闯关"), true);
    }
}

final class UnlockMainLineEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof mainlineEntity) {
            mainlineEntity entity = (mainlineEntity) obj;
            if (entity.checkPointUnlock()) {
                entity.sendRefreshMainLineMsg();
            }
            return true;
        }
        return false;
    }
}

final class VipLvUpEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (event.checkParamSize(3)) {
            String playerIdx = event.getParam(0);
            int beforeAdd = event.getParam(1);
            int afterAdd = event.getParam(2);
            int finalAfterAdd = beforeAdd;

            for (int i = beforeAdd + 1; i <= afterAdd; i++) {
                VIPConfigObject vipCfg = VIPConfig.getById(i);
                if (vipCfg == null) {
                    continue;
                }
                finalAfterAdd = i;
                // 扩容背包
                if (!model.pet.dbCache.petCache.getInstance().vipBagEnlarge(playerIdx, i) || !petruneCache.getInstance().bagEnlarge(playerIdx, i)) {
                    LogUtil.error("playerIdx[" + playerIdx + "] vip enlarge capacity error");
                }
            }
            // 扩展挑战次数
            PatrolServiceImpl.getInstance().vipLevelUp(playerIdx, beforeAdd, finalAfterAdd);

            // 拓展迷雾森林入场次数
            MistForestManager.getInstance().addFreeTickByVipLevelUp(playerIdx, beforeAdd, finalAfterAdd);

            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TEE_VipLvReach, finalAfterAdd, 0);
            return true;
        }
        return false;
    }
}

final class RemoveTeamDeadPetHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof teamEntity && event.checkParamSize(1)) {
                teamEntity entity = (teamEntity) obj;
                TeamTypeEnum teamType = event.getParam(0);

                entity.removeDeadPets(teamType);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class UpdateTargetProgressHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof targetsystemEntity && event.checkParamSize(3)) {
                targetsystemEntity entity = (targetsystemEntity) obj;
                TargetTypeEnum typeEnum = event.getParam(0);
                int progress = event.getParam(1);
                int param = event.getParam(2);
                entity.doTargetPro(typeEnum, progress, param);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ClearMistTargetProgressHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            targetsystemCache.getInstance().clearAllPlayerMistSessionTaskData();
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class UpdateBossActivityTimeHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof targetsystemEntity && event.checkParamSize(1)) {
                targetsystemEntity entity = (targetsystemEntity) obj;
                int times = event.getParam(0);
                int oldTimes = entity.getDb_Builder().getSpecialInfo().getActivityBoss().getTimes();
                if (times > 0) {
                    times += oldTimes;
                } else {
                    times = 0;
                }
                entity.getDb_Builder().getSpecialInfoBuilder().getActivityBossBuilder().setTimes(times);

                SC_ActivityBossUpdate.Builder builder = SC_ActivityBossUpdate.newBuilder();
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
                builder.setTimes(times);
                GlobalData.getInstance().sendMsg(entity.getLinkplayeridx(), MsgIdEnum.SC_ActivityBossUpdate_VALUE, builder);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class MistForestSeasonEndEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof playerEntity) {
            try {
                playerEntity player = (playerEntity) obj;
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class ClearMistItemHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof itembagEntity) {
                itembagEntity itemBag = (itembagEntity) obj;
                itemBag.clearAllMistItem();
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class MistForestServerCloseHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof playerEntity) {
                playerEntity player = (playerEntity) obj;
                SC_KickOutFromMistForest.Builder builder = SC_KickOutFromMistForest.newBuilder();
                GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_KickOutFromMistForest_VALUE, builder);
                if (player.getLastEnterMistTime() > 0) {
                    PlayerMistServerInfo serverInfo = CrossServerManager.getInstance().getMistForestPlayerServerInfo(player.getIdx());
                    boolean leaveCommonRule = serverInfo != null && serverInfo.getMistRule() == EnumMistRuleKind.EMRK_Common;
                    if (leaveCommonRule) {
                        player.setLastEnterMistTime(0);
                        int mistLevel = 0;
                        targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(player.getIdx());
                        if (targetEntity != null) {
                            mistLevel = targetEntity.getDb_Builder().getMistTaskData().getCurEnterLevel();
                        }
                        LogService.getInstance().submit(new MistPlayTimeLog(player.getIdx(), mistLevel, player.getDb_data().getMistForestData().getStamina(), false));
                    }
                }
                CrossServerManager.getInstance().removeMistForestPlayer(player.getIdx());
                player.settleMistCarryReward();

                BattleManager.getInstance().onOwnerLeave(player.getIdx(), true);
//                player.getBattleController().onOwnerLeave(true);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class MistBossActivityBeginHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.checkParamSize(1)) {
                List<MistActivityBossPlayerData> playerList = event.getParam(0);
                playerEntity player;
                for (MistActivityBossPlayerData playerData : playerList) {
                    player = playerCache.getByIdx(playerData.getPlayerId());
                    if (player == null || CrossServerManager.getInstance().getMistForestPlayerServerInfo(player.getIdx()) == null) {
                        continue;
                    }
                    GS_CS_ClearRewardAndRankRecord.Builder builder = GS_CS_ClearRewardAndRankRecord.newBuilder().setPlayerIdx(player.getIdx());
                    CrossServerManager.getInstance().sendMsgToMistForest(player.getIdx(), MsgIdEnum.GS_CS_ClearRewardAndRankRecord_VALUE, builder, true);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class LockPetTeamHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof teamEntity && event.checkParamSize(2)) {
                teamEntity team = (teamEntity) obj;
                int teamNum = event.getParam(0);
                boolean lock = event.getParam(1);
                TeamNumEnum teamNumEnum = TeamNumEnum.forNumber(teamNum);
                team.lockTeam(teamNumEnum, lock);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class updateActivityDropCountHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof targetsystemEntity && event.checkParamSize(1)) {
                targetsystemEntity target = (targetsystemEntity) obj;
                List<UpdateActivityDropCount> paramList = event.getParam(0);
                target.updateDropItemCount(paramList);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class TimerInvokeHandler implements EventHandler {

//    private static final Set<UpdateDailyData> UPDATE_DAILY_DATA_SET = new ConcurrentSet<>();
//
//    static {
//        List<Class<UpdateDailyData>> list = ClassUtil.getClassByInterface(".model", UpdateDailyData.class);
//        for (Class<UpdateDailyData> aClass : list) {
//            try {
//                Method method = aClass.getMethod("getInstance");
//                UpdateDailyData instance = (UpdateDailyData) method.invoke(aClass);
//                UPDATE_DAILY_DATA_SET.add(instance);
//                LogUtil.info("success add to UPDATE_DAILY_DATA_SET, name:" + aClass.getSimpleName());
//            } catch (Exception e) {
//                LogUtil.printStackTrace(e);
//            }
//        }
//        LogUtil.info("server.event.TimerInvokeHandler, init UPDATE_DAILY_DATA_SET finish, size:" + UPDATE_DAILY_DATA_SET.size());
//    }

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(2)) {
                LogUtil.error("TimerInvokeHandler, param size is not null");
                return false;
            }

            int targetType = event.getParam(0);
            DB_TimerParam.Builder paramBuilder = event.getParam(1);

            if (targetType == TimerTargetType.TT_RESET_DAILY_DATA) {

                LogUtil.info("Reset dailyTimer onEvent!");
                // 资源回收优先放到前面,需要判断资源副本,巡逻队,勇气试炼,boss塔昨天的状态,如果这些被重置了回收数据就可能不正确
                ResourceRecycleManager.getInstance().updateDailyData();

                ActivityManager.getInstance().updateDailyData();
                BossTowerManager.getInstance().updateDailyData();
                CrossArenaManager.getInstance().updateDailyData();
                StoneRiftWorldMapManager.getInstance().dailyRefresh();

                Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
                if (CollectionUtils.isNotEmpty(allOnlinePlayerIdx)) {
                    EventUtil.unlockObjEvent(EventType.ET_DailyUpdatePlayerAllFunction, allOnlinePlayerIdx, true);
                }

            } else if (targetType == TimerTargetType.TT_RESET_WEEK_DATA) {
                LogUtil.info("Reset weeklyTimer onEvent!");
//                playerCache.getInstance().updateWeekData();

				Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
				if (CollectionUtils.isNotEmpty(allOnlinePlayerIdx)) {
					EventUtil.unlockObjEvent(EventType.ET_WeeklyUpdatePlayerAllFunction, allOnlinePlayerIdx);
				}
                CpTeamManger.getInstance().updateWeeklyData();
                CrazyDuelManager.getInstance().updateWeeklyData();
				MagicThronManager.getInstance().updateWeek();
				RollCardManager.getInstance().updateWeek();

            } else if (targetType == TimerTargetType.TT_UPDATE_GAME_PLAY) {
                LogUtil.info("Reset gamePlayTimer onEvent!");
                gameplayCache.getInstance().update();

            } else if (targetType == TimerTargetType.TT_UPDATE_LOG_DAILY_DATA) {
                LogUtil.info("update log daily  onEvent!");
                LogService.getInstance().updateDailyData();

            } else {
                LogUtil.error("unSupported timer target type , value = " + targetType);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class updateUnlockMistLvHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!(obj instanceof playerEntity) || !event.checkParamSize(1)) {
                LogUtil.error("TimerInvokeHandler, param size is not null");
                return false;
            }

            playerEntity player = (playerEntity) obj;
            DB_PlayerData.Builder db_data = player.getDb_data();
            if (db_data == null) {
                return false;
            }

//            int newLv = event.getParam(0);
//            int permitLevel = db_data.getMistForestData().getPermitLevel();
//            if (newLv <= permitLevel) {
//                return true;
//            }
//            db_data.getMistForestDataBuilder().setPermitLevel(event.getParam(0));
            player.sendMistBaseData();

            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class RecordArenaBattleHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!(obj instanceof arenaEntity) || !event.checkParamSize(1)) {
                LogUtil.error("recordArenaBattleHandler, param size is not null");
                return false;
            }

            arenaEntity entity = (arenaEntity) obj;
            ArenaRecord record = event.getParam(0);
            entity.addBattleRecord(record);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ClearTeamHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!(obj instanceof teamEntity) || !event.checkParamSize(2)) {
                LogUtil.error("ClearTeamHandler, param size is not null");
                return false;
            }

            teamEntity entity = (teamEntity) obj;
            entity.clearTeam(event.getParam(0), event.getParam(1));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class RemovePetFromTeamsHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!(obj instanceof teamEntity) || !event.checkParamSize(1)) {
                LogUtil.error("ClearTeamHandler, param size is not null");
                return false;
            }

            teamEntity entity = (teamEntity) obj;
            entity.removePetFromTeam(event.getParam(0));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class UpdatePetTeamState implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!(obj instanceof petEntity) || !event.checkParamSize(3)) {
                LogUtil.error("ClearTeamHandler, param size is not null");
                return false;
            }

            petEntity entity = (petEntity) obj;
            entity.updatePetTeamStatus(event.getParam(0), event.getParam(1), event.getParam(2));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}

final class EndlessSpireBattleSettle implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!(obj instanceof playerEntity)) {
                return false;
            }

            long currentTime = GlobalTick.getInstance().getCurrentTime();
            playerEntity player = (playerEntity) obj;
            EndlessSpireInfo.Builder endlessBuilder = player.getDb_data().getEndlessSpireInfoBuilder();
            endlessBuilder.setMaxSpireLv(endlessBuilder.getMaxSpireLv() + 1);
            endlessBuilder.setLastPassTime(currentTime);

            // 更新到客户端
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_RefreashSpireLv_VALUE, SC_RefreashSpireLv.newBuilder().setNewLv(endlessBuilder.getMaxSpireLv()));

            RankingManager.getInstance().updatePlayerRankingScore(player.getIdx(), EnumRankingType.ERT_Spire, RankingName.RN_EndlessSpire, endlessBuilder.getMaxSpireLv());

            // 目标：无尽尖塔通关多少层
            EventUtil.triggerUpdateTargetProgress(player.getIdx(), TargetTypeEnum.TTE_PassSpireLv, endlessBuilder.getMaxSpireLv(), 0);

            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

//final class ForeignInvasionBattleSettle implements EventHandler {
//    @Override
//    public boolean onEvent(BaseObj obj, Event event) {
//        try {
//            if (!event.checkParamSize(3)) {
//                return false;
//            }
//
//            String playerIdx = event.getParam(0);
//            CS_BattleResult resultData = event.getParam(1);
//            String monsterIdx = event.getParam(2);
//
//            ForeignInvasionStatusEnum status = ForeignInvasionManager.getInstance().getStatus();
//            if (status == ForeignInvasionStatusEnum.FISE_FirstStage && resultData.getWinnerCamp() == 1) {
//                ForeignInvasionManager.getInstance().addIntegralAddition(playerIdx);
//                ForeignInvasionManager.getInstance().KillOneMonster();
//
//                //目标：累积击杀外敌入侵小怪
//                EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuKillForInvMonster, 1, 0);
//            }
//
//            if (status == ForeignInvasionStatusEnum.FISE_SecondStage) {
//                long damage = BattleUtil.getFightParamsValue(resultData.getFightParamsList(), FightParamTypeEnum.FPTE_PM_BossDamage);
//                ForeignInvasionManager.getInstance().addBossDamageCount(playerIdx, damage, monsterIdx);
//                ForeignInvasionManager.getInstance().recreateBossClone(playerIdx);
//
//                //目标：累积击杀外敌入侵boss
//                if (resultData.getWinnerCamp() == 1) {
//                    EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuKillForInvBoss, 1, 0);
//                }
//            }
//            return true;
//        } catch (Exception e) {
//            LogUtil.printStackTrace(e);
//            return false;
//        }
//    }
//}

final class ResourceCopyBattleSettle implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!(obj instanceof playerEntity) || !event.checkParamSize(2)) {
                return false;
            }

            playerEntity player = (playerEntity) obj;
            int copyType = event.getParam(0);
            int copyIndex = event.getParam(1);

            ResourceCopyObject copyCfg = ResourceCopy.getInstance().getCopyCfgByTypeAndIndex(copyType, copyIndex);
            if (copyCfg == null) {
                return false;
            }

            DB_ResourceCopy.Builder resourceCopy = player.getResourceCopyData(copyType);
            if (resourceCopy == null) {
                return false;
            }

            resourceCopy.addProgress(copyIndex);

            // 因为proto返回的是不可更改的数组
            ArrayList<Integer> ints = new ArrayList<>(resourceCopy.getUnlockProgressList());
            ints.remove(Integer.valueOf(copyIndex));
            resourceCopy.clearUnlockProgress();
            resourceCopy.addAllUnlockProgress(ints);

            resourceCopy.setChallengeTimes(resourceCopy.getChallengeTimes() + 1);

            SC_RefreshResCopy.Builder refreshBuilder = SC_RefreshResCopy.newBuilder();
            refreshBuilder.addCopies(player.buildResCopy(copyType));
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_RefreshResCopy_VALUE, refreshBuilder);

            // 目标：累积通过x次x级副本
            TargetTypeEnum targetTypeEnum = TargetSystemUtil.getTargetTypeByResCopyTypeNum(copyType);
            EventUtil.triggerUpdateTargetProgress(player.getIdx(), targetTypeEnum, 1, copyIndex);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class PatrolBattleSettleHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(4)) {
                LogUtil.error("PatrolBattleSettleHandler error, param size is not enough");
                return false;
            }
            String playerId = event.getParam(0);
            int battleResult = event.getParam(1);
            List<Reward> rewardList = event.getParam(2);
            PatrolTree location = event.getParam(3);

            PatrolServiceImpl.getInstance().battleSettle(playerId, battleResult, rewardList, location);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class BattlePlayerLeave implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(2)) {
                LogUtil.error("PatrolBattleSettleHandler error, param size is not enough");
                return false;
            }

            String playerId = event.getParam(0);
            boolean settlePvp = event.getParam(1);

            BattleManager.getInstance().onOwnerLeave(playerId, settlePvp);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class RechargeActivityHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!(obj instanceof targetsystemEntity) || !event.checkParamSize(3)) {
                LogUtil.error("ClearTeamHandler, rechargeValue size is not null");
                return false;
            }
            String playerId = event.getParam(0);
            int rechargeValue = event.getParam(1);
            int todayRecharge = event.getParam(2);
            targetsystemEntity entity = (targetsystemEntity) obj;
            TargetSystemDB.DB_TargetSystem.Builder db_builder = entity.getDb_Builder();
            int currRechargeCoupon = db_builder.getCumuRechargeCoupon();

            LogUtil.info("playerId recharge info,this time recharge:{},before this recharge,todayRecharge:{},cumuRechargeCoupon:{}", rechargeValue, todayRecharge, currRechargeCoupon);

            boolean firstRechargeNotActive = targetsystemCache.getInstance().firstRechargeNotActive(playerId);
            boolean cumuRechargeActive = targetsystemCache.getInstance().cumuRechargeActive(playerId);
            if (firstRechargeNotActive) {
                // 激活首充奖励
                LogUtil.info("playerId:{} active first recharge reward", playerId);
                targetsystemCache.getInstance().initRechargeRecord(entity.getIdx(), Activity.RechargeType.RT_FirstPay_VALUE);
            }

            // 当前累充值
            int curCumuRecharge = currRechargeCoupon + rechargeValue;
            // 更新累充数值
            db_builder.setCumuRechargeCoupon(curCumuRecharge);
            LogUtil.info("update player curCumuRecharge,playerIdx:{},curCumuRecharge:{}", playerId, curCumuRecharge);

            // 累充未激活情况下判断是否激活累充
            if (!cumuRechargeActive) {
                int diamondNeed = PayRewardConfig.getById(Activity.RechargeType.RT_SignlePay_VALUE).getDiamondneed();
                if (diamondNeed <= curCumuRecharge) {
                    LogUtil.info("playerId:{} active single recharge Rewards, active diamond need:{},cur recharge:{}", playerId, diamondNeed, rechargeValue);
                    // 激活累充奖励
                    targetsystemCache.getInstance().initRechargeRecord(entity.getIdx(), Activity.RechargeType.RT_SignlePay_VALUE);
                }
            }

            // 首充或者累充(更新充值状态)前更新充值活动
            if (firstRechargeNotActive || !cumuRechargeActive) {
                targetsystemCache.getInstance().sendRechargeActivityShow(playerId);
            }

            entity.triggerDayDayRechargeReward(todayRecharge + rechargeValue, true);
            entity.triggerDailyFirstRecharge(todayRecharge);
            entity.updateIncrRankingScore(EnumRankingType.ERT_Recharge,rechargeValue);

            // 目标：累计充值x点券
            EventUtil.triggerUpdateTargetProgress(playerId, protocol.TargetSystem.TargetTypeEnum.TEE_Player_CumuRechargeCoupon, rechargeValue, 0);

            // 单笔充值
            EventUtil.triggerUpdateTargetProgress(playerId, protocol.TargetSystem.TargetTypeEnum.TEE_Player_RechargeCoupon, rechargeValue, 0);

            EventUtil.triggerUpdatePlayerRecharge(playerId, rechargeValue);

            //充值获得星元
            EventUtil.triggerUpdatePlayerStar(playerId,rechargeValue);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AutoRefreshShop implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(1)) {
                LogUtil.error("AutoRefreshShop, params size is not match, paramSize = " + (event.getParams() == null ? 0 : event.getParams().size()));
                return false;
            }

            ShopTypeEnum shopType = event.getParam(0);
            shopCache.getInstance().autoRefresh(shopType);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddLimitPurchaseRecharge implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(1)) {
                LogUtil.error("AddLimitPurchaseRecharge, params size is not match, paramSize = " + (event.getParams() == null ? 0 : event.getParams().size()));
                return false;
            }
            int rechargeId = event.getParam(0);
            targetsystemEntity cache = (targetsystemEntity) obj;
            cache.getDb_Builder().addLimitPurchaseRechargeIds(rechargeId);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ReCreateMonsterHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!(obj instanceof playerEntity) || !event.checkParamSize(2)) {
                LogUtil.error("ReCreateMonsterHandler, error params, size is empty or obj not match");
                return false;
            }

            playerEntity player = (playerEntity) obj;
            player.reCreateMonsterDiff(event.getParam(0), event.getParam(1));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ClearAllPlayerActivityInfo implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(1)) {
                LogUtil.error("ClearAllPlayerActivityInfo, params is empty");
                return false;
            }

            ServerActivity activity = event.getParam(0);

            targetsystemCache.getInstance().clearAllPlayerActivitiesData(activity);
            playerCache.getInstance().clearAllPlayerActivityData(activity.getActivityId());
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddTimeLimitGiftHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!(obj instanceof targetsystemEntity) || !event.checkParamSize(2)) {
                LogUtil.error("AddTimeLimitGiftHandler, params is empty");
                return false;
            }
            TimeLimitGiftType giftType = event.getParam(0);
            int curTarget = event.getParam(1);
            targetsystemEntity target = (targetsystemEntity) obj;
            target.triggerTimeLimitGift(giftType, curTarget);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddReportTimesHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!(obj instanceof playerEntity) || !event.checkParamSize(1)) {
                LogUtil.error("AddTimeLimitGiftHandler, params is empty");
                return false;
            }

            playerEntity entity = (playerEntity) obj;
            entity.addReportTimes(event.getParam(0));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class BanHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(4)) {
                LogUtil.error("BanHandler, params is empty");
                return false;
            }

            List<String> playerIdx = event.getParam(0);
            int banType = event.getParam(1);
            long endTime = event.getParam(2);
            String banMsg = event.getParam(3);

            long msgId = PlatformManager.getInstance().addBanMsg(banMsg);

            for (String idx : playerIdx) {
                playerEntity entity = playerCache.getByIdx(idx);
                if (entity == null) {
                    continue;
                }

                SyncExecuteFunction.executeConsumer(entity, e -> entity.ban(banType, endTime, msgId));
            }

            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ShieldCommentHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(2)) {
                LogUtil.error("ShieldCommentHandler, params is empty");
                return false;
            }

            List<CommentTypeEnum> commentType = event.getParam(0);
            String playerIdx = event.getParam(1);

            commentCache.getInstance().shieldCommentByType(commentType, playerIdx);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class UpdateWarRoomIdxHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(2)) {
                LogUtil.error("UpdateWarRoomIdxHandler, params size is error");
                return false;
            }
            boolean bAdd = event.getParam(0);
            Map<String, String> playerRooms = event.getParam(1);
            if (bAdd) {
                playerEntity player;
                for (Entry<String, String> entry : playerRooms.entrySet()) {
                    player = playerCache.getByIdx(entry.getKey());
                    if (player == null) {
                        continue;
                    }
                    SyncExecuteFunction.executeConsumer(player, entity -> entity.getDb_data().setTheWarRoomIdx(entry.getValue()));
                }
                LogUtil.info("UpdateWarRoomIdxHandler, update player warRoomIdx finished");
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class WarPetUpdateHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof playerEntity && event.checkParamSize(2)) {
                playerEntity player = (playerEntity) obj;
                String petIdx = event.getParam(0);
                int warUpdateType = event.getParam(1);
                String roomIdx = player.getDb_data().getTheWarRoomIdx();
                if (StringHelper.isNull(roomIdx)) {
                    return true;
                }

                if (warUpdateType == WarPetUpdate.MODIFY) {
                    GS_CS_UpdateWarPetData.Builder builder = GS_CS_UpdateWarPetData.newBuilder();
                    builder.setPlayerIdx(player.getIdx());
                    TheWarMapConfigObject mapCfg = TheWarMapConfig.getByMapname(TheWarManager.getInstance().getMapName());
                    if (mapCfg == null) {
                        return true;
                    }
                    Pet pet = petCache.getInstance().buildReviseLevelPet(player.getIdx(), petIdx, mapCfg.getPetverifylevel());
                    if (pet == null) {
                        return true;
                    }
                    List<Pet> petList = new ArrayList<>();
                    petList.add(pet);
                    List<BattlePetData> petBattleData = petCache.getInstance().buildPlayerPetBattleData(player.getIdx(), petList, BattleSubTypeEnum.BSTE_TheWar);
                    if (CollectionUtils.isEmpty(petBattleData)) {
                        return true;
                    }
                    builder.setPetData(petBattleData.get(0));
                    CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_UpdateWarPetData_VALUE, builder);
                } else if (WarPetUpdate.REMOVE == warUpdateType) {
                    GS_CS_RemoveWarPetData.Builder builder = GS_CS_RemoveWarPetData.newBuilder();
                    builder.setPlayerIdx(player.getIdx());
                    builder.setRemovePetIdx(petIdx);
                    CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_RemoveWarPetData_VALUE, builder);
                }

            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ClearAllPlayerMistTimeLimitMissionProgress implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            targetsystemCache.getInstance().clearAllPlayerMistTimeLimitMissionProgress();
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class UnlockWishWellHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof targetsystemEntity) {
                ((targetsystemEntity) obj).unLockWishWell();
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AllPetAdditionUpdateHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.checkParamSize(4)) {
                String playerId = event.getParam(0);
                // 之前加成
                Map<Integer, Integer> lastAddition = event.getParam(1);
                // 当前加成
                Map<Integer, Integer> nowAddition = event.getParam(2);
                int type = event.getParam(3);
                // 通知客户端全体战力更新
                FightPowerCalculate lastAbilityAddition = new FightPowerCalculate(lastAddition);
                FightPowerCalculate newAbilityAddition = new FightPowerCalculate(nowAddition);
                long abilityUpdate = newAbilityAddition.calculateAdditionAbility() - lastAbilityAddition.calculateAdditionAbility();
                petCache.getInstance().sendAllPetAbilityUpdate(playerId, abilityUpdate, type);

                teamCache.settleAllPetUpdate(playerId);

                playerEntity player = playerCache.getByIdx(playerId);
                if (player != null) {
                    GS_CS_UpdatePlayerBaseAdditions.Builder builder = GS_CS_UpdatePlayerBaseAdditions.newBuilder();
                    builder.setPlayerIdx(playerId);
                    builder.putAllPlayerBaseAdditions(nowAddition);
                    CrossServerManager.getInstance().sendMsgToWarRoom(player.getDb_data().getTheWarRoomIdx(), MsgIdEnum.GS_CS_UpdatePlayerBaseAdditions_VALUE, builder);
                }

            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddPetGemEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof petgemEntity) {
            petgemEntity runeCache = (petgemEntity) obj;
            Map<Integer, Integer> runeMap = event.getParam(0);
            Reason reason = event.getParam(1);
            petgemCache.getInstance().playerObtainGem(runeCache, runeMap, reason);
            return true;
        }
        return false;
    }
}

final class ResetPetGemStatusHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof petgemEntity && event.checkParamSize(1)) {
            try {
                petgemCache.getInstance().resetPetGemStatus(((petgemEntity) obj).getPlayeridx(), event.getParam(0));
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class UpdatePlayerRechargeHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof playerEntity && event.checkParamSize(1)) {
            try {
                DB_PlayerData.Builder player = ((playerEntity) obj).getDb_data();
                player.setTodayRecharge(player.getTodayRecharge() + (int) event.getParam(0));
                ((playerEntity) obj).sendPlayerCurRecharge();
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class UnlockTimeLimitActivityHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof targetsystemEntity && event.checkParamSize(2)) {
            try {
                targetsystemEntity entity = (targetsystemEntity) obj;
                int beforeLv = event.getParam(0);
                int afterLv = event.getParam(1);

                List<TimeLimitActivityObject> needUpdateToClient = new ArrayList<>();
                for (TimeLimitActivityObject value : TimeLimitActivity._ix_id.values()) {
                    if (beforeLv < value.getShowlv() && afterLv >= value.getShowlv()) {
                        needUpdateToClient.add(value);
                    }
                }

                if (CollectionUtils.isNotEmpty(needUpdateToClient)) {
                    entity.sendTimeLimitActivity(needUpdateToClient);
                }

                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class NewForeignInvasionPlayerBuildingInfoHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (event.checkParamSize(1)) {
            try {
                Collection<String> needSendPlayer = event.getParam(0);

                foreigninvasionCache.getInstance().sendPlayerBuildingInfo(needSendPlayer);
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class ClearMistTimeLimitHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof targetsystemEntity) {
            try {
                targetsystemEntity entity = (targetsystemEntity) obj;
                entity.getDb_Builder().getSpecialInfoBuilder().clearMistTimeLimitMission();
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class UpdatePetMissionLvUpProHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof petmissionEntity) {
            if (!event.checkParamSize(1)) {
                return false;
            }
            try {
                petmissionEntity entity = (petmissionEntity) obj;
                Map<Integer, Integer> missionPro = event.getParam(0);
                entity.updatePetMissionLvUpPro(missionPro);
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class ClearAllPlayerTheWarSeasonMissionProHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        targetsystemCache.getInstance().clearAllPlayerTheWarSeasonMissionPro();
        return true;
    }
}

final class AddPointCopyScoreHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof targetsystemEntity && event.checkParamSize(2)) {
            try {
                targetsystemEntity entity = (targetsystemEntity) obj;
                int addScore = event.getParam(0);
                Reason reason = event.getParam(1);

                entity.addPointCopyScore(addScore, reason);
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class DailyUpdatePlayerAllFunctionHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (event.checkParamSize(2)) {
            try {
                Collection<String> playerIdxList = event.getParam(0);
                if (CollectionUtils.isEmpty(playerIdxList)) {
                    return true;
                }
                boolean sendMsg = event.getParam(1);

                playerIdxList.forEach(e -> dailyUpdatePlayerAllFunction(e, sendMsg));
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }

    public static void dailyUpdatePlayerAllFunction(String playerIdx, boolean sendMsg) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("DailyUpdatePlayerAllFunctionHandler.dailyUpdatePlayerAllFunction, playerIdx:" + playerIdx + ", is not exist");
            return;
        }

        SyncExecuteFunction.executeConsumer(player, p -> player.updateDailyData(sendMsg));

        arenaEntity arena = arenaCache.getInstance().getEntity(playerIdx);
        if (arena != null) {
            SyncExecuteFunction.executeConsumer(arena, a -> arena.updateDailyData());
        }

        bosstowerEntity bossTower = bosstowerCache.getInstance().getEntity(playerIdx);
        if (bossTower != null) {
            SyncExecuteFunction.executeConsumer(bossTower, b -> bossTower.updateDailyData(sendMsg));
        }

        bravechallengeEntity braveChallenge = bravechallengeCache.getInstance().getEntityByPlayer(playerIdx);
        if (braveChallenge != null) {
            SyncExecuteFunction.executeConsumer(braveChallenge, b -> braveChallenge.updateDailyDate(sendMsg));
        }

        mainlineEntity mainLine = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (mainLine != null) {
            SyncExecuteFunction.executeConsumer(mainLine, m -> mainLine.updateDailyData(sendMsg));
        }

        petmissionEntity petMission = petmissionCache.getInstance().getEntityByPlayerIdx(playerIdx);
        if (petMission != null) {
            SyncExecuteFunction.executeConsumer(petMission, p -> petMission.updateDailyData(sendMsg));
        }

        shopEntity shop = shopCache.getInstance().getEntityByPlayerIdx(playerIdx);
        if (shop != null) {
            SyncExecuteFunction.executeConsumer(shop, s -> shop.updateDailyData());
        }

        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target != null) {
            SyncExecuteFunction.executeConsumer(target, t -> target.updateDailyData(sendMsg));
        }

        patrolEntity patrol = patrolCache.getInstance().getCacheByPlayer(playerIdx);
        if (patrol != null) {
            SyncExecuteFunction.executeConsumer(patrol, t -> patrol.updateDailyData());
        }

        itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBag != null) {
            SyncExecuteFunction.executeConsumer(itemBag, t -> itemBag.updateDailyData(sendMsg));
        }

        trainingEntity trainingInfo = trainingCache.getInstance().getCacheByPlayer(playerIdx);
        if (trainingInfo != null) {
            SyncExecuteFunction.executeConsumer(trainingInfo, t -> trainingInfo.updateDailyData());
        }

		playercrossarenaEntity crossarenaInfo = playercrossarenaCache.getByIdx(playerIdx);
		if (crossarenaInfo != null) {
			SyncExecuteFunction.executeConsumer(crossarenaInfo, t -> crossarenaInfo.updateDailyData());
		}

        stoneriftEntity stoneriftEntity = stoneriftCache.getByIdx(playerIdx);
        if (stoneriftEntity != null) {
            SyncExecuteFunction.executeConsumer(crossarenaInfo, t -> stoneriftEntity.updateDailyData());
        }

        magicthronEntity magicthron = magicthronCache.getByIdx(playerIdx);
        if (magicthron != null) {
            SyncExecuteFunction.executeConsumer(magicthron, t -> magicthron.updateDailyData());
        }

		if (sendMsg) {
			GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_UpdateDailyData_VALUE, SC_UpdateDailyData.newBuilder());
		}
		LogUtil.debug("DailyUpdatePlayerAllFunctionHandler.dailyUpdatePlayerAllFunction, update player:" + playerIdx + ", daily data finished");
	}
}

final class WeeklyUpdatePlayerAllFunctionHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (event.checkParamSize(2)) {
            try {
                Collection<String> playerIdxList = event.getParam(0);
                if (CollectionUtils.isEmpty(playerIdxList)) {
                    return true;
                }
                boolean sendMsg = event.getParam(1);

                playerIdxList.forEach(e -> weeklyUpdatePlayerAllFunction(e, sendMsg));
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }

    public static void weeklyUpdatePlayerAllFunction(String playerIdx, boolean sendMsg) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("UpdatePlayerAllFunctionHandler.updatePlayerAllFunction, playerIdx:" + playerIdx + ", is not exist");
            return;
        }

        matcharenaEntity matchArena = matcharenaCache.getInstance().getEntity(playerIdx);
        if (matchArena != null) {
            SyncExecuteFunction.executeConsumer(matchArena, m -> matchArena.updateWeeklyData(sendMsg));
        }

        SyncExecuteFunction.executeConsumer(player, p -> player.updateWeeklyData(sendMsg));
        playercrossarenaEntity crossarenaInfo = playercrossarenaCache.getByIdx(playerIdx);
        if (null != crossarenaInfo) {
            SyncExecuteFunction.executeConsumer(crossarenaInfo, playercrossarenaEntity::updateWeeklyData);
        }

        itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (null != itemBag) {
            SyncExecuteFunction.executeConsumer(itemBag, itembagEntity::updateWeeklyData);
        }


        LogUtil.debug("WeeklyUpdatePlayerAllFunctionHandler.weeklyUpdatePlayerAllFunction, update player:" + playerIdx + ", weekly data finished");
    }
}

final class UpdateRankingHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof AbstractRanking) {
            try {
                AbstractRanking ranking = (AbstractRanking) obj;
                ranking.updateRanking();
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class UpdatePatrolMissionSwitchHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof targetsystemEntity) {
            if (event.checkParamSize(1)) {
                try {
                    boolean pause = event.getParam(0);
                    targetsystemEntity target = (targetsystemEntity) obj;
                    target.updatePatrolMissionSwitch(pause);
                    return true;
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                }
            }
        }
        return false;
    }
}

final class AddNewTitleHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof playerEntity && event.checkParamSize(2)) {
            try {
                playerEntity player = (playerEntity) obj;
                List<Integer> newTitleList = event.getParam(0);
                Reason reason = event.getParam(1);
                player.addNewTitles(newTitleList, reason);
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class ClearClaimRecordOnPlayerHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof playerEntity && event.checkParamSize(1)) {
            try {
                playerEntity player = (playerEntity) obj;
                long activityId = event.getParam(0);
                player.clearActivitiesData(activityId);
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class AddGloryRoadQuizRecordHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof gloryroadEntity && event.checkParamSize(1)) {
            try {
                gloryroadEntity entity = (gloryroadEntity) obj;
                entity.addQuizRecord(event.getParam(0));
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class GloryRoadBattleResultHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (event.checkParamSize(4)) {
            String playerIdx_1 = event.getParam(0);
            String playerIdx_2 = event.getParam(1);
            int player1BattleResult = event.getParam(2);
            String linkBattleRecordId = event.getParam(3);
            GloryRoadManager.getInstance().settleBattle(playerIdx_1, playerIdx_2, player1BattleResult, linkBattleRecordId);
            return true;
        }
        return false;
    }
}

final class RefreshMazeDataHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        MistForestManager.getInstance().getMazeManager().refreshMazePlayerData();
        return true;
    }
}

final class SettleMazeActivityHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        MistForestManager.getInstance().getMazeManager().settleMazeActivity();
        return true;
    }
}

final class CollectMazeItemHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof playerEntity && event.checkParamSize(1)) {
            try {
                playerEntity entity = (playerEntity) obj;
                int itemCount = event.getParam(0);
                int collectCount = entity.getDb_data().getMazeDataBuilder().getMazeItemCollectCount() + itemCount;
                entity.getDb_data().getMazeDataBuilder().setMazeItemCollectCount(collectCount);

                SC_MazeItemCollectCount.Builder builder = SC_MazeItemCollectCount.newBuilder();
                builder.setCollectCount(collectCount);
                GlobalData.getInstance().sendMsg(entity.getIdx(), MsgIdEnum.SC_MazeItemCollectCount_VALUE, builder);
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
        return false;
    }
}

final class AddPetInscriptionEventHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof petinscriptionEntity) {
            petinscriptionEntity entity = (petinscriptionEntity) obj;
            Map<Integer, Integer> runeMap = event.getParam(0);
            Reason reason = event.getParam(1);
            petinscriptionCache.getInstance().playerObtainInscription(entity, runeMap, reason);
            return true;
        }
        return false;
    }
}

final class RefreshPetDataHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        if (obj instanceof petEntity) {
            petEntity entity = (petEntity) obj;
            String petId = event.getParam(0);
            Reason reason = event.getParam(1);
            Pet pet = entity.getPetById(petId);
            if (pet == null) {
                return false;
            }
            entity.refreshPetPropertyAndPut(pet.toBuilder(), reason, true);
            return true;
        }
        return false;
    }
}

final class TrainBattleSettleHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(4)) {
                LogUtil.error("PatrolBattleSettleHandler error, param size is not enough");
                return false;
            }
            String playerId = event.getParam(0);
            int battleResult = event.getParam(1);
            int mapId = event.getParam(2);
            int pointId = event.getParam(3);
            List<Reward> rewardList = event.getParam(4);
            int hprate = event.getParam(5);

            TrainingManager.getInstance().battleSettle(playerId, battleResult, rewardList, mapId, pointId, hprate);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class TrainRankSettleHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(2)) {
                LogUtil.error("TrainRankSettleHandler error, param size is not enough");
                return false;
            }
            String playerId = event.getParam(0);
            int mapId = event.getParam(1);

            TrainingManager.getInstance().endTrain(playerId, mapId);
            TeamsUtil.updateTeamInfoTrain(playerId);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class UnLockFunctionHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(1)) {
                LogUtil.error("TrainRankSettleHandler error, param size is not enough");
                return false;
            }
            playerEntity player = (playerEntity) obj;
            List<Common.EnumFunction> functions = event.getParam(0);
            player.doUnlockFunctions(functions, true);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class CoupTeamUpdateHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            teamEntity teamEntity = (teamEntity) obj;
            teamEntity.updateCoupTeam();
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class TrainItemAddHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            playerEntity player = (playerEntity) obj;
            Map<Integer, Integer> itemMap = event.getParam(0);
            TrainingManager.getInstance().addTrainItem(player.getIdx(), itemMap);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ClearHelpPetHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(1)) {
                LogUtil.error("ClearHelpPetHandler error, param size is not enough");
                return false;
            }
            Common.EnumFunction function = event.getParam(0);
            petEntity petEntity = (petEntity) obj;
            petEntity.clearHelpPet(function);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}

final class CollectArtifactExpHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(3)) {
                LogUtil.error("CollectArtifactExpHandler error, param size is not enough");
                return false;
            }
            playerEntity player = (playerEntity) obj;

            int artifactId = event.getParam(0);
            int lastSkillLv = event.getParam(1);
            int nowSkillLv = event.getParam(2);
            player.collectArtifactExp(artifactId, lastSkillLv, nowSkillLv);

            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}

final class CollectPetHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(1)) {
                LogUtil.error("CollectPetHandlerHandler error, param size is not enough");
                return false;
            }
            playerEntity player = (playerEntity) obj;
            player.collectPetExpAndLink(event.getParam(0));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}

final class RemoveInscriptionHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(1)) {
                LogUtil.error("CollectPetHandlerHandler error, param size is not enough");
                return false;
            }
            petinscriptionEntity entity = (petinscriptionEntity) obj;
            entity.removeByIds(event.getParam(0));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}

final class MagicThronRecordHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {

            String playerId = event.getParam(0);
            int areaId = event.getParam(1);
            int difficult = event.getParam(2);
            long battleId = event.getParam(3);
            long time = event.getParam(4);
            long power = event.getParam(5);

            magicthronEntity player = magicthronCache.getByIdx(playerId);
            if (player == null) {
                return false;
            }
            MagicBattleRecord.Builder b = MagicBattleRecord.newBuilder();
            b.setBattleId(String.valueOf(battleId));
            b.setAreaId(areaId);
            b.setDifficult(difficult);
            b.setTime(time);
            b.setPower(power);

            SyncExecuteFunction.executeFunction(player, e -> {
                DB_MagicThron.Builder magicThronBuilder = player.getInfoDB();
                magicThronBuilder.addRecord(b.build());
                if (magicThronBuilder.getRecordList().size() > GameConfig.getById(GameConst.CONFIG_ID).getMagicrecordsize()) {
                    List<MagicBattleRecord> list = new ArrayList<>();

                    int max = GameConfig.getById(GameConst.CONFIG_ID).getMagicrecordsize();
                    int over = magicThronBuilder.getRecordList().size() - max;

                    for (int i = 0; i < magicThronBuilder.getRecordList().size(); i++) {
                        if (over > 0) {
                            over--;
                            MagicThronManager.getInstance().deleteBattleNum(magicThronBuilder.getRecordList().get(i).getBattleId());
                            continue;
                        }
                        list.add(magicThronBuilder.getRecordList().get(i));
                    }
                    magicThronBuilder.clearRecord();
                    magicThronBuilder.addAllRecord(list);
                }

                return null;
            });
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}


final class OpenTraingHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            String playerId = event.getParam(0);
            int id = event.getParam(1);
            TrainingManager.getInstance().finishMainTask(playerId, id);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}

final class CrossArenaEventBattleEnd implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(3)) {
                LogUtil.error("PatrolBattleSettleHandler error, param size is not enough");
                return false;
            }
            String playerId = event.getParam(0);
            int battleResult = event.getParam(1);
            int eventId = event.getParam(2);
            CrossArenaManager.getInstance().battleSettle(playerId, battleResult, eventId);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class OfferRewardFight implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            String playerId = event.getParam(0);
            String id = event.getParam(1);
            boolean win = event.getParam(2);
            int grade = Integer.valueOf(event.getParam(3));
//            OfferRewardManager.getInstance().fightEnd(playerId, win, id, grade);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}

final class CrossArenaBoss implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            String playerId = event.getParam(0);
            CrossArenaManager.getInstance().changeStageId(playerId);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}

final class CompleteStoneRiftMissionHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(1)) {
                LogUtil.error("CompleteStoneRiftMissionHandler error, param size is not enough");
                return false;
            }
            ((stoneriftEntity) obj).unlockFactoryByCompleteMission(event.getParam(0));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}
final class UpdateStoneRiftAchievementHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(1)) {
                LogUtil.error("UpdateStoneRiftAchievementHandler error, param size is not enough");
                return false;
            }
            ((stoneriftEntity) obj).updateStoneRiftAchievement(event.getParam(0));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}

final class UpdateCrossArenaWeeklyTaskHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(2)) {
                LogUtil.error("UpdateCrossArenaWeeklyTaskHandler error, param size is not enough");
                return false;
            }
            playercrossarenaEntity entity  = (playercrossarenaEntity)(obj);
            CrossArenaManager.getInstance().updateWeeklyTaskWithLock(entity,event.getParam(0),event.getParam(1));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}

final class UpdateIncrRankingScoreHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(2)) {
                LogUtil.error("UpdateIncrRankingScoreHandler error, param size is not equals 2");
                return false;
            }
            targetsystemEntity entity = (targetsystemEntity) (obj);
            entity.updateIncrRankingScore(event.getParam(0), event.getParam(1));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

}


final class AddStoneRiftFactoryExpHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(2)) {
                LogUtil.error("AddStoneRiftFactoryExpHandler error, param size is not equals 2");
                return false;
            }
            stoneriftEntity entity = (stoneriftEntity) (obj);
            entity.addStoneRiftExp(StoneRiftCfgManager.getInstance().getGainExp(event.getParam(0), event.getParam(1)));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class UpdateStarCountHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(2)) {
                LogUtil.error("UpdateStarCountHandler error, param size is not enough");
                return false;
            }
            String playerIdx = event.getParam(0);
            int magicStoneCount = event.getParam(1);

            if(magicStoneCount <= 0){
                LogUtil.error("UpdateStarCountHandler error, magicStoneCount={}",magicStoneCount);
                return false;
            }

            ServerActivity activity = ActivityManager.getInstance().getActivityByType(ActivityTypeEnum.ATE_StarTreasure);
            if(activity == null){
                return false;
            }
            if(!ActivityUtil.activityInOpen(activity)){
                return false;
            }

            targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
            DB_StarTreasureActivity.Builder dbStarTreasureBuilder = entity.getDb_Builder()
                    .getSpecialInfoBuilder().getStarTreasureActivityBuilder();

            int gainedCount = dbStarTreasureBuilder.getCurrentGainStar();
            int limitCount = activity.getStarTreasure().getLimitGainStarCount();
            if(gainedCount >= limitCount){
                return false;
            }
            int addCount = (magicStoneCount/StarTreasureConstant.CHARGE_ADD_STAR_UNIT)* activity.getStarTreasure().getPerTenGainStar();
            if(addCount <= 0){
                return false;
            }

            int newCount = gainedCount+addCount;
            if(newCount > limitCount){
                newCount = limitCount;
            }

            addCount = newCount - gainedCount;

            Consume consume = activity.getStarTreasure().getCostItem();

            Reward.Builder rewardItem = Reward.newBuilder();
            rewardItem.setRewardType(consume.getRewardType());
            rewardItem.setId(consume.getId());
            rewardItem.setCount(addCount);

            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Recharge,"星空宝藏活动");
            boolean addResult = RewardManager.getInstance().doReward(playerIdx,rewardItem.build(),reason,true);

            if(addResult){
                final int setCount = newCount;
                SyncExecuteFunction.executeConsumer(entity, e -> {
                    entity.getDb_Builder()
                            .getSpecialInfoBuilder().getStarTreasureActivityBuilder().setCurrentGainStar(setCount);
                });
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class PetAvoidanceGameTimeOverHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(1)) {
                LogUtil.error("UpdateCrossArenaWeeklyTaskHandler error, param size is not enough");
                return false;
            }
            PetAvoidanceGameData petAvoidanceGameData = event.getParam(0);

            SC_PetAvoidanceEnd.Builder clBuilder = SC_PetAvoidanceEnd.newBuilder();

            PetAvoidanceGameManager.getInstance().settle(petAvoidanceGameData, clBuilder);
            clBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.REC_petAvoidance_Timeout));
            GlobalData.getInstance().sendMsg(petAvoidanceGameData.getPlayerIdx(), MsgIdEnum.SC_PetAvoidanceEnd_VALUE, clBuilder);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddMistMoveEffectHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!(obj instanceof playerEntity)) {
                return false;
            }
            if (!event.checkParamSize(2)) {
                LogUtil.error("UpdateCrossArenaWeeklyTaskHandler error, param size is not enough");
                return false;
            }
            playerEntity player = (playerEntity) obj;
            List<Integer> cfgIdList = event.getParam(0);
            player.addMistMoveEffect(cfgIdList);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

