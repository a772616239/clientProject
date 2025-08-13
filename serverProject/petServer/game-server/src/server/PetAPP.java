package server;

import cfg.FightMake;
import cfg.PetBaseProperties;
import cfg.PetFragmentConfig;
import cfg.PetGemConfig;
import cfg.PetRuneBlessRatingCfg;
import cfg.PetRuneProperties;
import cfg.RankRewardTargetConfig;
import clazz.PackageUtil;
import common.GameConst.ServerState;
import common.GlobalData;
import common.GlobalThread;
import common.IdGenerator;
import common.JedisUtil;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import common.load.Sysload;
import common.tick.GlobalTick;
import db.entity.BaseEntity;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import model.FunctionManager;
import model.activity.ActivityManager;
import model.activity.PointCopyManager;
import model.activity.ScratchLotteryManager;
import model.activity.TimeRuleManager;
import model.activity.petAvoidance.PetAvoidanceGameManager;
import model.ancientCall.AncientCallManager;
import model.ancientCall.PetTransferManager;
import model.arena.ArenaManager;
import model.barrage.BarrageManager;
import model.battle.BattleManager;
import model.bosstower.BossTowerManager;
import model.cacheprocess.baseUapteCacheL;
import model.comment.dbCache.commentCache;
import model.cp.CpCopySettleManager;
import model.cp.CpTeamManger;
import model.crazyDuel.CrazyDuelDataUpdateManager;
import model.crazyDuel.CrazyDuelManager;
import model.crossarena.CrossArenaManager;
import model.crossarenapvp.CrossArenaPvpManager;
import model.drawCard.DrawCardManager;
import model.farmmine.FarmMineManager;
import model.foreignInvasion.newVersion.NewForeignInvasionManager;
import model.gameplay.dbCache.gameplayCache;
import model.gloryroad.GloryRoadManager;
import model.itembag.BlindBoxManager;
import model.magicthron.MagicThronManager;
import model.mainLine.manager.MainLineManager;
import model.matcharena.MatchArenaManager;
import model.mistforest.MistForestManager;
import model.mistforest.MistTimeLimitMissionManager;
import model.offerreward.OfferRewardManager;
import model.pet.HelpPetManager;
import model.pet.PetManager;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.petmission.entity.PetMissionHelper;
import model.petrune.PetRuneManager;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.RankingManager;
import model.ranking.ranking.RankingTargetManager;
import model.rollcard.RollCardManager;
import model.shop.StoreManager;
import model.stoneRift.StoneRiftManager;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import model.thewar.TheWarManager;
import model.timer.dbCache.timerCache;
import model.training.TrainingManager;
import model.wordFilter.WordFilterManager;
import platform.PlatformManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.statistics.StatisticsManager;
import platform.purchase.PurchaseManager;
import protocol.Common;
import protocol.PetMessage;
import protocol.PrepareWar;
import protocol.RetCodeId.RetCodeEnum;
import server.event.EventManager;
import server.http.HttpServer;
import server.net.GameServerBootstrap;
import util.LogUtil;

import static protocol.PrepareWar.TeamNumEnum.TNE_Arena_Attack_1;

public class PetAPP {

    static final Runtime runtime = Runtime.getRuntime();

    public static int serverState = ServerState.ServerClosed;

    public static void main(String[] args) throws IllegalArgumentException {
        long l1 = System.currentTimeMillis();
        if (!initServer()) {
            System.exit(0);
            return;
        }
        long l2 = System.currentTimeMillis();
        serverState = ServerState.ServerRunning;
        LogUtil.info("===================start server use time = " + (l2 - l1));
        if (ServerConfig.getInstance().isSingleRun()) {
            LogUtil.warn("===========single rune game server===========");
        }

        GlobalTick.getInstance().startUpdateTick();
    }

    protected static boolean initServer() {
        try {
            // load ServerConfig
            if (!Sysload.sysInitConfig()) {
                LogUtil.error("init server config failed");
                return false;
            }

            //load Game
            Sysload.onServerStart();

            ServerConfig config = ServerConfig.getInstance();

            LogUtil.debug("total server config :" + config.toString());

            if (!afterAllCfgInit()) {
                LogUtil.error("*****afterCfgInit init failed*****");
                return false;
            }

            if (!JedisUtil.init()) {
                LogUtil.error("*****JedisUtil init failed*****");
                return false;
            }

            if (!GlobalThread.getInstance().init(config.getThreadCount())) {
                LogUtil.error("*****GlobalThread init failed*****");
                return false;
            }

            if (!FunctionManager.getInstance().init()) {
                LogUtil.error("****FunctionManager IS ERROR*********");
                return false;
            }

            //开始globalTick
            GlobalTick.getInstance().start();

            if (!EventManager.getInstance().init()) {
                LogUtil.error("*****EventManager init failed*****");
                return false;
            }

            if (!IdGenerator.getInstance().init(config.getServer())) {
                LogUtil.error("IdGenerator init error");
                return false;
            }

//            if (!ForeignInvasionManager.getInstance().init()) {
//                LogUtil.error("ForeignInvasionManager init error");
//                return false;
//            }


            if (!RankingTargetManager.getInstance().init()) {
                LogUtil.error("****RankingTargetManager IS ERROR*********");
                return false;
            }

            if (!settleRuneCfgRemove()) {
                return false;
            }
            if (!settlePetRemove()) {
                return false;
            }

            if (!PetGemConfig.getInstance().initGemData()) {
                LogUtil.error("PetGemConfig init error");
                return false;
            }

            if (!PetRuneManager.getInstance().init()) {
                LogUtil.error("PetRuneManager init error");
                return false;
            }


            if (!PetManager.getInstance().init()) {
                LogUtil.error("PetManager init error");
                return false;
            }

            if (!NewForeignInvasionManager.getInstance().init()) {
                LogUtil.error("ForeignInvasionManager init error");
                return false;
            }

            if (!WordFilterManager.getInstance().init()) {
                LogUtil.error("WordFilterManager init error");
                return false;
            }

            if (!DrawCardManager.getInstance().init()) {
                LogUtil.error("DrawCardManager init error");
                return false;
            }

            if (!MainLineManager.getInstance().init()) {
                LogUtil.error("MainLineCheckPoint cfg error");
                return false;
            }

            if (!MistForestManager.getInstance().init()) {
                LogUtil.error("MistForest init error");
                return false;
            }

            if (!AncientCallManager.getInstance().init() || !PetTransferManager.getInstance().init()) {
                LogUtil.error("AncientCallManager or PetTransferManager init error");
                return false;
            }

            if (!ActivityManager.getInstance().init()) {
                LogUtil.error("ActivityManager init error");
                return false;
            }

            if (!PetAvoidanceGameManager.getInstance().init()) {
                LogUtil.error("PetAvoidanceGameManager init error");
                return false;
            }

            if (!PointCopyManager.getInstance().init()) {
                LogUtil.error("PointCopyManager init error");
                return false;
            }

            if (!PlatformManager.getInstance().init()) {
                LogUtil.error("PlatformMailManager init error");
                return false;
            }

            // 初始化战斗地图战斗力阈值
            if (!FightMake.getInstance().afterInit()) {
                LogUtil.error("FightMake afterInit error");
                return false;
            }

            //初始化平台日志服务
            if (!LogService.getInstance().init()) {
                LogUtil.error("LogService init error");
                return false;
            }

            //初始化平台日志服务
            if (!StatisticsManager.getInstance().init()) {
                LogUtil.error("LogService init error");
                return false;
            }

            if (!PurchaseManager.getInstance().init()) {
                LogUtil.error("PurchaseManager init error");
                return false;
            }

            if (!RankingManager.getInstance().init()) {
                LogUtil.error("RankingManager init error");
                return false;
            }

            if (!TrainingManager.getInstance().init()) {
                LogUtil.error("RankingManager init error");
                return false;
            }
            if (!MagicThronManager.getInstance().init()) {
                LogUtil.error("MagicThronManager init error");
                return false;
            }
            if (!OfferRewardManager.getInstance().init()) {
                LogUtil.error("OfferRewardManager init error");
                return false;
            }

            if (!FarmMineManager.getInstance().init()) {
                LogUtil.error("FarmMineManager init error");
                return false;
            }

            //初始化定时器
            if (!timerCache.getInstance().init()) {
                LogUtil.error("timerCache init error");
                return false;
            }

//            //初始化竞技场
            if (!ArenaManager.getInstance().init()) {
                LogUtil.error("ArenaManager init error");
                return false;
            }
//            //初始化机器人
//            RobotManager.getInstance().init();

            //初始化刮刮乐
            if (!ScratchLotteryManager.getInstance().init()) {
                LogUtil.error("****ScratchLotteryManager init error*********");
                return false;
            }

            if (!BlindBoxManager.getInstance().init()) {
                LogUtil.error("****BlindBoxManager init error*********");
                return false;
            }

            if (!StoreManager.getInstance().init()) {
                LogUtil.error("StoreManager init error*********");
                return false;
            }

            if (!BattleManager.getInstance().init()) {
                LogUtil.error("****battle Manager init error*********");
                return false;
            }

            if (!BossTowerManager.getInstance().init()) {
                LogUtil.error("****BossTower Manager init error*********");
                return false;
            }
            if (!StoneRiftManager.getInstance().init()) {
                LogUtil.error("****StoneRiftManager init error*********");
                return false;
            }

            if (!MistTimeLimitMissionManager.getInstance().init()) {
                LogUtil.error("****MistTimeLimitMission Manager init error*********");
                return false;
            }

            TheWarManager.getInstance().init();

            // ServerBoot 初始化完成后开启
            if (!GameServerBootstrap.start(config, false, true)) {
                LogUtil.error("****APP SERVER START IS ERROR*********");
                return false;
            }

            if (!PetMissionHelper.init()) {
                LogUtil.error("****PetMissionHelper IS ERROR*********");
                return false;
            }

            if (!GloryRoadManager.getInstance().init()) {
                LogUtil.error("****GloryRoadManager IS ERROR*********");
                return false;
            }

            if (!MatchArenaManager.getInstance().init()) {
                LogUtil.error("****MatchArenaManager IS ERROR*********");
                return false;
            }

            if (!CrossArenaManager.getInstance().init()) {
                LogUtil.error("****CrossArenaManager IS ERROR*********");
                return false;
            }

            if (!HelpPetManager.getInstance().init()) {
                LogUtil.error("****HelpPetManager IS ERROR*********");
                return false;
            }

            if (!BarrageManager.getInstance().init()) {
                LogUtil.error("****BarrageManager IS ERROR*********");
                return false;
            }

            if (!CpTeamManger.getInstance().init()) {
                LogUtil.error("****CpBroadcastManager IS ERROR*********");
                return false;
            }

            if (!CpCopySettleManager.getInstance().init()) {
                LogUtil.error("****CpCopySettleManager IS ERROR*********");
                return false;
            }


            if (!CrazyDuelManager.getInstance().init()){
                LogUtil.error("****CrazyDuelManager IS ERROR*********");
                return false;
            }
            if (!CrazyDuelDataUpdateManager.getInstance().init()){
                LogUtil.error("****CrazyDuelDataUpdateManager IS ERROR*********");
                return false;
            }

            if (!TimeRuleManager.getInstance().init()) {
                LogUtil.error("****TimeRuleManager IS ERROR*********");
                return false;
            }
            if (!CrossArenaPvpManager.getInstance().init()) {
            	LogUtil.error("****CrossArenaPvpManager IS ERROR*********");
            	return false;
            }
            if (!RollCardManager.getInstance().init()) {
            	LogUtil.error("****RollCardManager IS ERROR*********");
            	return false;
            }

            //初始化被举报的评论
            commentCache.getInstance().initReportComment();

            HttpServer.getInstance().run();

            //重新计算所有玩家的宠物属性
            petCache.getInstance().reCalculateAllPetProperties();

            if (!RankingManager.getInstance().init()) {
                LogUtil.error("RankingManager init error");
                return false;
            }

            addShutDownHook();
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

    private static boolean settlePetRemove() {
        for (BaseEntity value : petCache.getInstance()._ix_id.values()) {
            petEntity petEntity = (petEntity) value;
            SyncExecuteFunction.executeConsumer(petEntity, entity -> {
                for (PetMessage.Pet pet : petEntity.peekAllPetByUnModify()) {
                    if (PetBaseProperties.getByPetid(pet.getPetBookId()) == null) {
                        petEntity.removePets(Collections.singletonList(pet.getId()), ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_GM));
                    }
                }
            });
        }
        return true;
    }

    /**
     * 在所有excel生成的Config init方法执行完后调用
     *
     * @return
     */
    private static boolean afterAllCfgInit() {
        if (!RankRewardTargetConfig.getInstance().afterAllCfgInit()) {
            LogUtil.error("RankRewardTargetConfig afterAllCfgInit failed");
            return false;
        }
        if (!PetFragmentConfig.getInstance().afterAllCfgInit()) {
            LogUtil.error("PetFragmentConfig afterAllCfgInit init failed");
            return false;
        }

        if (!PetRuneBlessRatingCfg.getInstance().initTotalRatingAdditionMap()) {
            LogUtil.error("PetRuneBlessRatingCfg afterAllCfgInit init failed");
            return false;
        }


        return true;
    }

    private static boolean settleRuneCfgRemove() {
        for (BaseEntity value : petruneCache.getInstance()._ix_id.values()) {
            petruneEntity runeEntity = (petruneEntity) value;

            SyncExecuteFunction.executeConsumer(runeEntity, entity -> {


                ArrayList<PetMessage.Rune> runes = new ArrayList<>(runeEntity.getRuneListBuilder().getRuneMap().values());
                for (PetMessage.Rune rune : runes) {
                    if (PetRuneProperties.getByRuneid(rune.getRuneBookId()) == null) {
                        runeEntity.removeRune(rune);
                    }
                }
            });
        }
        return true;
    }

    public static void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LogUtil.info("GameServer ready to close");
                serverState = ServerState.ServerClosing;
                kickOutAllPlayer();
                GlobalTick.getInstance().closeTick();
                LogService.getInstance().close();
                gameplayCache.getInstance().update();
                BattleManager.getInstance().stop();
                GlobalThread.getInstance().getExecutor().shutdown();
                if (GlobalThread.getInstance().getExecutor().awaitTermination(10, TimeUnit.SECONDS)) {
                    GlobalThread.getInstance().getExecutor().shutdownNow();
                }
            } catch (InterruptedException e) {
                LogUtil.printStackTrace(e);
            }
            shutDownAllModel();
            LogUtil.info("GameServer close finished");
        }));
    }

    private static void kickOutAllPlayer() {
        for (String playerIdx : GlobalData.getInstance().getAllOnlinePlayerIdx()) {
            playerEntity player = playerCache.getByIdx(playerIdx);
            if (player == null || !player.isOnline()) {
                continue;
            }
            player.kickOut(RetCodeEnum.RCE_KickOut_ServerClose);
            LogUtil.info("Server Close kick out player=" + playerIdx);
        }

        //延迟,防止客户端未接受到kickOut消息
//        try {
//            Thread.sleep(5000);
//        } catch (Exception e) {
//            LogUtil.printStackTrace(e);
//            LogUtil.warn("kickOutAllPlayer, try sleep failed");
//        }

    }

    protected static void shutDownAllModel() {
        List<Class<?>> classList = PackageUtil.getClasses("model");
        if (classList == null || classList.isEmpty()) {
            LogUtil.error("Gameserver shutdown Model is null");
            return;
        }
        try {
            for (Class<?> clazz : classList) {
                if (Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }
                if (!clazz.getPackage().getName().endsWith("cache")) {
                    continue;
                }
                if (!clazz.getName().endsWith("CacheL")) {
                    continue;
                }
                Method getInstance = clazz.getMethod("getInstance");
                if (getInstance == null) {
                    continue;
                }
                baseUapteCacheL instance = (baseUapteCacheL) getInstance.invoke(null);
                if (instance == null) {
                    continue;
                }
                LogUtil.info("server shut save instance=" + instance.getClass().getSimpleName());
                instance.dealUpdateCache();
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}

