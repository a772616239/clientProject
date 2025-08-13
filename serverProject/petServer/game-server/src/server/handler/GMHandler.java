package server.handler;

import cfg.BossTowerConfig;
import cfg.BossTowerConfigObject;
import cfg.CrossArenaEvent;
import cfg.CrossArenaEventObject;
import cfg.CrossArenaScene;
import cfg.CumuSignIn;
import cfg.CumuSignInObject;
import cfg.DrawCard;
import cfg.DrawCardAdvanced;
import cfg.DrawCardAdvancedObject;
import cfg.EndlessSpireConfig;
import cfg.EndlessSpireConfigObject;
import cfg.ForeignInvasionParamConfig;
import cfg.GameConfig;
import cfg.GameConfigObject;
import cfg.HeadBorder;
import cfg.InscriptionCfg;
import cfg.InscriptionCfgObject;
import cfg.Item;
import cfg.ItemCard;
import cfg.ItemCardObject;
import cfg.ItemObject;
import cfg.KeyNodeConfig;
import cfg.KeyNodeConfigObject;
import cfg.MainLineCheckPoint;
import cfg.MainLineCheckPointObject;
import cfg.MainLineNode;
import cfg.MainLineNodeObject;
import cfg.MissionObject;
import cfg.PatrolConfig;
import cfg.PatrolConfigObject;
import cfg.PatrolMap;
import cfg.PatrolMapObject;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetFragmentConfig;
import cfg.PetFragmentConfigObject;
import cfg.PetGemConfig;
import cfg.PetGemConfigObject;
import cfg.PetMissionLevel;
import cfg.PetMissionLevelObject;
import cfg.PetRarityConfig;
import cfg.PetRarityConfigObject;
import cfg.PetRuneExp;
import cfg.PetRuneProperties;
import cfg.PetRunePropertiesObject;
import cfg.PlayerLevelConfig;
import cfg.Recharge;
import cfg.RechargeObject;
import cfg.RechargeProduct;
import cfg.RechargeProductObject;
import cfg.ServerStringRes;
import cfg.StoneRiftScience;
import cfg.StoneRiftScienceObject;
import cfg.TrainingMap;
import cfg.TrainingMapObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GameConst.EventType;
import common.GameConst.RankingName;
import common.GameConst.RedisKey;
import common.GlobalData;
import static common.JedisUtil.jedis;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import datatool.StringHelper;
import db.entity.BaseEntity;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import model.FunctionManager;
import model.activity.ActivityManager;
import model.activity.DailyFirstRechargeManage;
import model.activity.ScratchLotteryManager;
import model.activity.entity.Lottery;
import model.activity.petAvoidance.PetAvoidanceGameManager;
import model.activity.petAvoidance.ScoreValidator;
import model.ancientCall.AncientCallManager;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.bosstower.BossTowerManager;
import model.bosstower.dbCache.bosstowerCache;
import model.bosstower.entity.bosstowerEntity;
import model.bravechallenge.dbCache.bravechallengeCache;
import model.bravechallenge.entity.bravechallengeEntity;
import model.comment.dbCache.commentCache;
import model.comment.entity.commentEntity;
import model.cp.CpCopyManger;
import model.cp.CpTeamCache;
import model.cp.CpTeamManger;
import model.cp.entity.CpCopyMap;
import model.crazyDuel.CrazyDuelManager;
import model.crossarena.CrossArenaHonorManager;
import model.crossarena.CrossArenaManager;
import model.crossarena.CrossArenaRankManager;
import model.crossarena.CrossArenaTopManager;
import model.crossarena.CrossArenaUtil;
import model.crossarena.dbCache.playercrossarenaCache;
import model.crossarena.entity.playercrossarenaEntity;
import model.crossarenapvp.CrossArenaPvpManager;
import model.drawCard.DrawCardManager;
import model.drawCard.OddsRandom;
import model.farmmine.FarmMineManager;
import model.foreignInvasion.newVersion.NewForeignInvasionManager;
import model.itembag.ItemConst.ItemType;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.magicthron.MagicThronManager;
import model.magicthron.dbcache.magicthronCache;
import model.mailbox.dbCache.mailboxCache;
import model.mailbox.entity.mailboxEntity;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.matcharena.MatchArenaLTManager;
import model.matcharena.MatchArenaUtil;
import model.matcharena.dbCache.matcharenaCache;
import model.matcharena.entity.matcharenaEntity;
import model.mission.MissionManager;
import model.offerreward.OfferRewardManager;
import model.patrol.dbCache.patrolCache;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.patrol.entity.patrolEntity;
import model.pet.PetManager;
import model.pet.StrongestPetManager;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.petfragment.dbCache.service.PetFragmentServiceImpl;
import model.petmission.dbCache.petmissionCache;
import model.petmission.entity.petmissionEntity;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.FriendUtil;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.rollcard.RollCardManager;
import model.shop.dbCache.shopCache;
import model.shop.entity.PlayerShopInfo;
import model.shop.entity.shopEntity;
import model.stoneRift.StoneRiftWorldMapManager;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbStoneRiftFactory;
import model.stoneRift.stoneriftEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import model.thewar.TheWarManager;
import model.training.TrainingManager;
import model.training.dbCache.trainingCache;
import model.training.entity.trainingEntity;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import platform.PlatformManager;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.purchase.PurchaseManager;
import protocol.Activity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.EnumRankingType;
import protocol.Activity.ItemCardData;
import protocol.Activity.PayActivityBonus;
import protocol.Activity.SC_ActivityBossUpdate;
import protocol.Activity.SC_PetAvoidanceUpdate;
import protocol.Activity.SC_RefreshItemCard;
import protocol.Activity.WishStateEnum;
import protocol.Activity.WishingWellItem;
import protocol.BossTower.EnumBossTowerDifficult;
import protocol.Chat.SC_SystemChat;
import protocol.Comment.CommentTypeEnum;
import protocol.Comment.EnumReportType;
import protocol.CommentDB.CommentDbData;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.Common.SC_Marquee;
import protocol.Common.SC_Tips;
import protocol.CrayzeDuel;
import protocol.CrossArena;
import protocol.CrossArenaPvp.CrossArenaPvpRoom;
import protocol.DrawCard.EnumDrawCardType;
import protocol.EndlessSpire.SC_RefreashSpireLv;
import protocol.GM;
import protocol.GM.CS_GM;
import protocol.GM.GM_Result;
import protocol.GM.SC_GM;
import protocol.Gameplay.SC_BossKilledBarrage;
import protocol.MainLine.MainLineProgress;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.SC_PetAvoidanceUpdate_VALUE;
import protocol.PetMessage;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Rune;
import protocol.PlayerDB;
import protocol.PlayerDB.CommonAdvanceInfo;
import protocol.PlayerDB.DB_DrawCardData;
import protocol.PlayerDB.DB_OwnedFriendInfo;
import protocol.PlayerDB.DB_PlayerData.Builder;
import protocol.PlayerInfo.Artifact;
import protocol.PlayerInfo.AvatarBorderInfo;
import protocol.PlayerInfo.NewTitleInfo;
import protocol.PrepareWar.PositionPetMap;
import protocol.PrepareWar.SkillMap;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamPetPositionEnum;
import protocol.PrepareWar.TeamSkillPositionEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server;
import protocol.Server.ServerActivity;
import protocol.ServerTransfer.GS_CS_ArenaGm;
import protocol.ServerTransfer.GS_CS_TransGMCommand;
import protocol.ServerTransfer.TheWarGmType;
import protocol.Shop.ShopTypeEnum;
import protocol.TargetSystem;
import protocol.TargetSystem.TargetMission;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TargetSystemDB;
import protocol.TargetSystemDB.DB_CumuSignIn;
import protocol.TargetSystemDB.DB_Feats;
import protocol.TargetSystemDB.DB_HadesActivityInfo;
import protocol.TargetSystemDB.DB_ItemCard;
import protocol.TargetSystemDB.DB_NoviceCredit;
import protocol.TargetSystemDB.DB_SpecialActivity;
import protocol.TargetSystemDB.DB_TargetSystem;
import protocol.TargetSystemDB.DB_WishingWell;
import protocol.TargetSystemDB.PayActivityRecord;
import protocol.TheWarDefine.Position;
import protocol.TrainingDB.TrainDBMap;
import protocol.TransServerCommon.GS_CS_AddPetEnergyGmCmd;
import protocol.TransServerCommon.GS_CS_AddWarDpGmCmd;
import protocol.TransServerCommon.GS_CS_AddWarGoldGmCmd;
import protocol.TransServerCommon.GS_CS_AddWarItemGmCmd;
import protocol.TransServerCommon.GS_CS_OccupyGirdGmCmd;
import protocol.TransServerCommon.GS_CS_SetGridPropGmCmd;
import server.event.Event;
import server.event.EventManager;
import server.handler.resRecycle.ResourceRecycleManager;
import server.http.entity.PlatformPurchaseData;
import util.ArrayUtil;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.MapUtil;
import util.RandomUtil;
import util.TimeUtil;

@MsgId(msgId = MsgIdEnum.CS_GM_VALUE)
public class GMHandler extends AbstractBaseHandler<CS_GM> {

    /**
     * 方法名全部使用全小写保存 <FunctionName,
     * <p>
     * 使用方法名为GM名
     */
    private static final Map<String, Method> GM_FUNCTION_MAP = new ConcurrentHashMap<>();

    static {
        Method[] methods = GMHandler.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(GmFunction.class)) {
                GM_FUNCTION_MAP.put(method.getName().toLowerCase(), method);
            }
        }
    }

    @Override
    protected CS_GM parse(byte[] bytes) throws Exception {
        return CS_GM.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GM req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_GM.Builder builder = SC_GM.newBuilder();
        // 判断GM是否开启
        if (ServerConfig.getInstance().isGM() && doGM(playerIdx, req.getStr())) {
            builder.setResult(GM.GM_Result.GMR_Success);
        } else {
            builder.setResult(GM_Result.GNR_ErrorParam);
        }
        gsChn.send(MsgIdEnum.SC_GM_VALUE, builder);
    }

    private boolean doGM(String playerIdx, String params) {
        if (StringUtils.isBlank(playerIdx) || params == null) {
            return false;
        }

        // GM格式 参数名+参数
        String[] reqParamsArray = params.split("\\|");
        LogUtil.info("server.handler.GMHandler.doGM, playerIdx:" + playerIdx + "gm params: " + params);
        if (reqParamsArray.length < 1) {
            return false;
        }
        String functionName = reqParamsArray[0];

        if ("arenaDan".equalsIgnoreCase(functionName) || "arenaScore".equalsIgnoreCase(functionName)) {
            sendGmToArena(playerIdx, params);
            return true;
        } else {
            Method method = GM_FUNCTION_MAP.get(functionName.toLowerCase());
            if (method == null) {
                LogUtil.error("server.handler.GMHandler.doGM, function name is not exist, fuName=" + functionName.toLowerCase());
                return false;
            }

            Class<?>[] paramsTypes = method.getParameterTypes();
            // reqParamsArray = functionName + params
            // paramType = playerIdx + params
            if (paramsTypes.length != reqParamsArray.length) {
                LogUtil.error("server.handler.GMHandler.doGM, function params size miss match, functionName:" + functionName);
                return false;
            }

            Object[] paramArr = new Object[reqParamsArray.length];
            // 将第一个参数设置PlayerIdx
            paramArr[0] = playerIdx;
            for (int i = 1; i < reqParamsArray.length; i++) {
                Object type = castToType(reqParamsArray[i], paramsTypes[i]);
                if (type == null) {
                    return false;
                }
                paramArr[i] = type;
            }

            try {
                return (boolean) method.invoke(this, paramArr);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LogUtil.printStackTrace(e);
            }
            return false;
        }
    }


    @GmFunction
    private boolean petSpecify(String playerIdx, int bookId, int lvl, int petRarity, int upLvl, int amount) {
        PetBasePropertiesObject object = PetBaseProperties.getByPetid(bookId);
        if (object != null && object.getPetfinished() == 1) {
            petCache.getInstance().petSpecify(playerIdx, bookId, lvl, petRarity, upLvl, amount);
        }
        return true;
    }

    @GmFunction
    private boolean recycleYesterdayDayRes(String playerIdx) {
        ResourceRecycleManager.getInstance().recyclePlayerResource(playerIdx);
        return true;
    }

    @GmFunction
    private boolean initWorldMapData(String playerIdx) {
        for (BaseEntity value : petCache.getInstance()._ix_id.values()) {
            petEntity petEntity = (petEntity) value;
            SyncExecuteFunction.executeConsumer(petEntity, entity -> {
                Collection<Pet> pets = entity.peekAllPetByUnModify();
                int maxPetLv = pets.stream().mapToInt(Pet::getPetLvl).max().orElse(0);
                entity.getDbPetsBuilder().setPetMaxLvHis(maxPetLv);
            });
        }
        return true;
    }


    @GmFunction
    private boolean ltWeeklySettle(String playerIdx) {
        playercrossarenaEntity entity = playercrossarenaCache.getByIdx(playerIdx);
        SyncExecuteFunction.executeConsumer(entity, playercrossarenaEntity::updateWeeklyData);
        return true;
    }

    @GmFunction
    private boolean addGrade(String playerIdx, int exp) {
        playercrossarenaEntity entity = playercrossarenaCache.getByIdx(playerIdx);
        SyncExecuteFunction.executeConsumer(entity, playercrossarenaEntity -> {
            CrossArenaManager.getInstance().addGrade(playercrossarenaEntity, exp);
        });
        return true;
    }

    @GmFunction
    private boolean triggerCrossEvent(String playerIdx, int eventId) {
        playercrossarenaEntity entity = playercrossarenaCache.getByIdx(playerIdx);
        if (entity == null) {
            return false;
        }
        if (entity.getDataMsg().containsEventCur(eventId)) {
            return false;
        }
        CrossArenaEventObject eventc = CrossArenaEvent.getById(eventId);
        if (eventc == null) {
            return false;
        }
        long expire = eventc.getCoutime() > 0 ? System.currentTimeMillis() + eventc.getCoutime() * 60000L : 0L;

        SyncExecuteFunction.executeConsumer(entity, p -> {
            p.getDataMsg().putEventCur(eventId, expire);
        });
        CrossArenaManager.getInstance().sendPlayerEvents(playerIdx);
        return true;
    }

    @GmFunction
    private boolean clearEpisode(String playerIdx) {
        mainlineEntity mainline = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (mainline == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(mainline, entity -> {
            entity.getDBBuilder().clearEpisodeProgress();
        });
        return true;
    }

    @GmFunction
    private boolean crazySetting(String playerIdx) {
        List<CrayzeDuel.CrazyDuelBuffSetting> crazyDuelBuffSettings = CrazyDuelManager.getInstance().initOrRefreshBuffSetting(null);
        for (int i = 0; i < 20; i++) {
            crazyDuelBuffSettings = CrazyDuelManager.getInstance().initOrRefreshBuffSetting(crazyDuelBuffSettings);
        }
        return true;
    }

    @GmFunction
    private boolean addFriendNum(String playerIdx) {
        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_PlayerFriendReach, 1, 0);
        return true;
    }


    @GmFunction
    private boolean unLockAllFunction(String playerIdx) {
        List<EnumFunction> functions = new ArrayList<>();
        for (EnumFunction value : EnumFunction.values()) {
            if (value == EnumFunction.EF_GloryRoad) {
                continue;
            }
            if (value != EnumFunction.UNRECOGNIZED && value.getNumber() > 0) {
                functions.add(value);
            }
        }
        EventUtil.triggerUnlockFunction(playerIdx, functions);
        return true;
    }

    @GmFunction
    private boolean rankStrongestPet(String playerIdx) {
        StrongestPetManager.getInstance().rankStrongestPet();
        return true;
    }

    @GmFunction
    private boolean refreshKeyNode(String playerIdx) {
        for (BaseEntity value : mainlineCache.getInstance()._ix_id.values()) {
            mainlineEntity mainline = (mainlineEntity) value;
            SyncExecuteFunction.executeConsumer(mainline, e -> {
                if (mainline.getDBBuilder().getKeyNodeId() == 0) {
                    mainline.getDBBuilder().setKeyNodeId(1);
                }
            });

        }
        return true;
    }

    @GmFunction
    private boolean featsPassDay(String playerIdx, int days) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        long leadTime = TimeUtil.MS_IN_A_DAY * days;
        SyncExecuteFunction.executeConsumer(target, e -> {
            Map<Integer, DB_Feats> featsInfosMap = target.getDb_Builder().getFeatsInfosMap();
            for (Entry<Integer, DB_Feats> ent : featsInfosMap.entrySet()) {
                long resetTime = Math.max(ent.getValue().getResetTime() - leadTime, TimeUtil.getNextDayResetTime(GlobalTick.getInstance().getCurrentTime()));
                DB_Feats.Builder setResetTime = ent.getValue().toBuilder().setResetTime(resetTime);
                target.getDb_Builder().putFeatsInfos(ent.getKey(), setResetTime.build());
            }
        });
        target.sendFeats();
        return true;
    }

    @GmFunction
    private boolean doMonthCard(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            player.updateMonthCardDataAndDoReward(true);
        });

        return true;
    }

    @GmFunction
    private boolean removeRedisKey(String playerIdx, String key) {
        jedis.del(key);
        return true;
    }

    @GmFunction
    private boolean clearCrayDuelData(String playerIdx) {
        return true;
    }

    @GmFunction
    private boolean petMax(String playerIdx, int petCfgId) {
        PetBasePropertiesObject object = PetBaseProperties.getByPetid(petCfgId);
        if (object != null && object.getPetfinished() == 1) {
            addPetMax(playerIdx, PetBaseProperties.getByPetid(petCfgId));
        }
        return true;
    }

    @GmFunction
    private boolean addAllPet(String playerIdx) {
        PetBaseProperties._ix_petid.values().forEach(petBasePropertiesObject -> {
            // 只添加完成的宠物
            if (petBasePropertiesObject.getPetfinished() == 1) {
                addPetMax(playerIdx, petBasePropertiesObject);
            }
        });
        return true;
    }

    @GmFunction
    private boolean allInitialPet(String playerIdx) {
        PetBaseProperties._ix_petid.values().forEach(config -> {
            petCache.getInstance().petSpecify(playerIdx, config.getPetid(), 1, config.getStartrarity(), 0, 1);
        });
        return true;
    }

    //gm doreward|3|1331|100|180
    @GmFunction
    private boolean doReward(String playerIdx, int type, int id, int num, int reason) {
        Reward reward = RewardUtil.parseReward(type, id, num);
        if (reward == null) {
            return false;
        }
        Reason reason1 = ReasonManager.getInstance().borrowReason(RewardSourceEnum.forNumber(reason));
        RewardManager.getInstance().doReward(playerIdx, reward, reason1, true);
        return true;
    }

    @GmFunction
    private boolean PetRune(String playerIdx) {
        Map<Integer, Integer> bookIdMap = new HashMap<>();
        Map<Integer, PetRunePropertiesObject> runeCfg = PetRuneProperties._ix_runeid;
        runeCfg.forEach((integer, petRunePropertiesObject) -> bookIdMap.put(integer, 1));
        List<Reward> rewards = mapToRewardList(RewardTypeEnum.RTE_Rune, bookIdMap);
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), true);

        return true;
    }

    @GmFunction
    private boolean inscription(String playerIdx) {
        Map<Integer, Integer> cfgIdMap = new HashMap<>();
        Map<Integer, InscriptionCfgObject> runeCfg = InscriptionCfg._ix_id;
        runeCfg.forEach((integer, InscriptionCfgObject) -> cfgIdMap.put(integer, 1));
        List<Reward> rewards = mapToRewardList(RewardTypeEnum.RTE_Inscription, cfgIdMap);
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), true);

        return true;
    }

    @GmFunction
    private boolean clearPopupMission(String playerIdx) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SyncExecuteFunction.executeConsumer(target, e -> {
            target.getDb_Builder().getDbBusinessPopupBuilder().clearBusinessItems();
        });
        target.sendBusinessPopupMsgInit();
        return true;
    }

    @GmFunction
    private boolean PetRuneSpecial(String playerIdx, int cfgId, int num) {
        Map<Integer, Integer> bookIdMap = new HashMap<>();
        PetRunePropertiesObject config = PetRuneProperties._ix_runeid.get(cfgId);
        if (config == null) {
            return false;
        }
        bookIdMap.put(cfgId, num);
        List<Reward> rewards = mapToRewardList(RewardTypeEnum.RTE_Rune, bookIdMap);
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), true);

        return true;
    }

    @GmFunction
    private boolean petAllGem(String playerIdx) {
        Map<Integer, Integer> gemMap = PetGemConfig._ix_id.values().parallelStream().filter(e -> e.getLv() == 1 && e.getStar() == 1).collect(Collectors.toMap(PetGemConfigObject::getId, a -> 1));

        List<Reward> rewards = mapToRewardList(RewardTypeEnum.RTE_Gem, gemMap);
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), true);
        return true;
    }

    private List<Reward> mapToRewardList(RewardTypeEnum type, Map<Integer, Integer> gemMap) {
        if (MapUtils.isEmpty(gemMap)) {
            return Collections.emptyList();
        }
        List<Reward> rewards = new ArrayList<>();
        for (Entry<Integer, Integer> entry : gemMap.entrySet()) {
            rewards.add(Reward.newBuilder().setId(entry.getKey()).setCount(entry.getValue()).setRewardType(type).build());
        }
        return rewards;
    }

    @GmFunction
    private boolean PetGem(String playerIdx, int cfgId, int num) {
        Map<Integer, Integer> gemMap = new HashMap<>();
        gemMap.put(cfgId, num);
        List<Reward> rewards = mapToRewardList(RewardTypeEnum.RTE_Gem, gemMap);
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), true);

        return true;
    }

    @GmFunction
    private boolean clearRune(String playerIdx) {
        petruneEntity entityByPlayer = petruneCache.getInstance().getEntityByPlayer(playerIdx);
        if (entityByPlayer != null) {
            SyncExecuteFunction.executeConsumer(entityByPlayer, e -> {
                entityByPlayer.getRuneListBuilder().clearRune();
            });
        }
        return true;
    }

    @GmFunction
    private boolean unEquipAllRune(String playerIdx) {
        petruneEntity entityByPlayer = petruneCache.getInstance().getEntityByPlayer(playerIdx);
        if (entityByPlayer != null) {
            SyncExecuteFunction.executeConsumer(entityByPlayer, e -> {
                Collection<Rune> strings = entityByPlayer.getRuneListBuilder().getRuneMap().values();
                entityByPlayer.unEquipAllRune(strings, true, true);
            });
        }
        return true;
    }

    @GmFunction
    private boolean novicePassDay(String playerIdx, int days) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return false;
        }

        SyncExecuteFunction.executeConsumer(target, e -> {
            DB_NoviceCredit.Builder noviceBuilder = target.getDb_Builder().getSpecialInfoBuilder().getNoviceBuilder();
            noviceBuilder.setStartTime(noviceBuilder.getStartTime() - days * TimeUtil.MS_IN_A_DAY);
        });
        Activity.SC_NewActivity.Builder result = Activity.SC_NewActivity.newBuilder();
        Activity.ClientActivity novice = Activity.ClientActivity.newBuilder().setActivityType(Activity.ActivityTypeEnum.ATE_NoviceCredit).setTabType(Activity.EnumClientActivityTabType.ECATT_Independent).build();
        result.addActivitys(novice);
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_NewActivity_VALUE, result);
        return true;
    }

    @GmFunction
    private boolean petFragment(String playerIdx) {
        Map<Integer, Integer> fragmentMap = PetFragmentConfig._ix_id.values().stream().filter(config -> config.getId() != 0 && config.getPetid() == 0).collect(Collectors.toMap(PetFragmentConfigObject::getId, a -> 300));

        PetFragmentServiceImpl.getInstance().playerObtainFragment(playerIdx, fragmentMap, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM));

        return true;
    }

    @GmFunction
    private boolean crazyDuelWeeklyRest(String playerIdx) {
        CrazyDuelManager.getInstance().updateWeeklyData();
        return true;
    }

    @GmFunction
    private boolean clearCrossArenaRanking(String playerIdx) {
        for (Integer sceneId : CrossArenaScene._ix_id.keySet()) {
            RankingManager.getInstance().clearRanking(Activity.EnumRankingType.ERT_Lt_SerialWin, RankingUtils.getLtSerialWinRankName(sceneId));
        }
        CrossArenaRankManager.getInstance().moveRedisDataToPlatform();

        return true;
    }


    @GmFunction
    private boolean addLoginDay(String playerIdx, int day) {
        for (int i = 0; i < day; i++) {
            targetsystemEntity targetsystem = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
            if (targetsystem == null) {
                return false;
            }
            SyncExecuteFunction.executeConsumer(targetsystem, entity -> {
                for (Entry<Long, TargetSystemDB.DB_Activity> entry : entity.getDb_Builder().getActivitiesMap().entrySet()) {
                    entity.getDb_Builder().putActivities(entry.getKey(), entry.getValue().toBuilder().clearNextCanUpdateCumuLogTime().build());
                }
                entity.getDb_Builder().getSpecialInfoBuilder().getTimeLimitActivitiesBuilder().setNextCanUpdateTime(0);
            });
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_CumuLogin, day, 0);
        }
        return true;
    }

    @GmFunction
    private boolean addCollectionExp(String playerIdx, int exp) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            PlayerDB.DB_Collection.Builder collectionBuilder = player.getDb_data().getCollectionBuilder();
            collectionBuilder.setCollectionExp(collectionBuilder.getCollectionExp() + exp);
        });
        player.sendPetCollectionUpdate();
        return true;
    }

    @GmFunction
    private boolean resetLevel1Pet(String playerIdx) {
        for (BaseEntity entity : petCache.getInstance()._ix_id.values()) {
            petEntity playerPetEntity = ((petEntity) entity);
            SyncExecuteFunction.executeConsumer(playerPetEntity, e -> {
                for (PetMessage.Pet pet : ((petEntity) entity).peekAllPetByUnModify()) {
                    if (pet.getPetLvl() == 1) {
                        PetMessage.PetProperties level1NewPetProperty = PetManager.getInstance().getLevel1NewPetProperty(pet.getPetBookId(), pet.getPetRarity());
                        if (level1NewPetProperty != null) {
                            PetMessage.Pet.Builder builder = pet.toBuilder().setPetProperty(level1NewPetProperty);
                            playerPetEntity.putPet(builder.build());
                        }
                    }

                }
            });
        }
        return true;
    }

    @GmFunction
    private boolean refreshRuneData(String playerIdx) {
        for (BaseEntity value : petruneCache.getInstance()._ix_id.values()) {
            petruneEntity petruneEntity = (petruneEntity) value;
            SyncExecuteFunction.executeConsumer(petruneEntity, e -> {
                for (Rune rune : petruneEntity.getRuneListBuilder().getRuneMap().values()) {
                    Rune.Builder newRune = rune.toBuilder();
                    PetRuneExp.refreshBaseProperty(newRune);
                    petruneCache.getInstance().refreshPropertyByConfig(newRune);
                    petruneEntity.putRune(newRune.build());
                }
            });
        }
        return true;
    }

    @GmFunction
    private boolean refreshArtifactData(String playerIdx) {
        for (BaseEntity entity : playerCache.getInstance()._ix_id.values()) {
            playerEntity player = (playerEntity) entity;
            SyncExecuteFunction.executeConsumer(player, e -> {
                player.refreshAllPetPropertyAddition(false);
            });
        }
        return true;
    }

    @GmFunction
    private boolean petFragmentSpecify(String playerIdx, int fragmentId, int count) {
        PetFragmentConfigObject config = PetFragmentConfig._ix_id.get(fragmentId);
        if (config == null) {
            return false;
        }
        Map<Integer, Integer> fragmentMap = new HashMap<>(16);
        fragmentMap.put(fragmentId, count);
        PetFragmentServiceImpl.getInstance().playerObtainFragment(playerIdx, fragmentMap, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM));
        return true;
    }

    @GmFunction
    private boolean addMail(String playerIdx, int templateId) {
        EventUtil.triggerAddMailEvent(playerIdx, templateId, null, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), (String[]) null);
        return true;
    }

    @GmFunction
    private boolean addMailByNum(String playerIdx, int templateId, int num) {
        if (num <= 0) {
            return false;
        }
        for (int i = 0; i < num; i++) {
            addMail(playerIdx, templateId);
        }
        return true;
    }

    @GmFunction
    private boolean addItem(String playerIdx, int itemCfgId, int itemCount) {
        if (itemCfgId <= 0 || itemCount <= 0) {
            return false;
        }
        itembagEntity itemBagByPlayerIdx = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBagByPlayerIdx == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(itemBagByPlayerIdx, e -> {
            itemBagByPlayerIdx.addItem(itemCfgId, itemCount, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), true);
        });
        return true;
    }

    @GmFunction
    private boolean clearItem(String playerIdx) {
        itembagEntity itemBagByPlayerIdx = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBagByPlayerIdx == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(itemBagByPlayerIdx, e -> {
            Set<Integer> cfgIdSet = itemBagByPlayerIdx.getDb_data().getItemsMap().keySet();
            itemBagByPlayerIdx.getDb_data().clearItems();
            itemBagByPlayerIdx.sendRefreshItemMsgBySet(cfgIdSet);
        });
        return true;
    }

    @GmFunction
    private boolean clearItemOpenLimit(String playerIdx) {
        itembagEntity itemBagByPlayerIdx = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBagByPlayerIdx == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(itemBagByPlayerIdx, e -> {
            itemBagByPlayerIdx.getDb_data().clearItemUseCount();
        });
        return true;
    }

    @GmFunction
    private boolean wishPassDay(String playerIdx, int days) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        long leadTime = TimeUtil.MS_IN_A_DAY * days;
        SyncExecuteFunction.executeConsumer(target, e -> {
            DB_WishingWell.Builder wishWell = target.getDb_Builder().getSpecialInfoBuilder().getWishingWellBuilder();
            Map<Integer, WishingWellItem> wishMapMap = wishWell.getWishMapMap();
            for (WishingWellItem wish : wishMapMap.values()) {
                WishingWellItem.Builder builder = wish.toBuilder().setWishTime(wish.getWishTime() - leadTime).setClaimTime(wish.getClaimTime() - leadTime);
                wishWell.putWishMap(wish.getWishIndex(), builder.build());
            }
            wishWell.setStartTime(wishWell.getStartTime() - leadTime);
            target.checkAndSendWishWellReward(false);
        });
        target.sendWishUpdate();
        return true;
    }

    @GmFunction
    private boolean resetWishWell(String playerIdx) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SyncExecuteFunction.executeConsumer(target, e -> {
            DB_WishingWell.Builder wishWell = target.getDb_Builder().getSpecialInfoBuilder().getWishingWellBuilder();
            Map<Integer, WishingWellItem> wishMapMap = wishWell.getWishMapMap();
            long leadTime = wishMapMap.get(1).getWishTime() - TimeUtil.getTodayStamp(GlobalTick.getInstance().getCurrentTime());
            for (WishingWellItem wish : wishMapMap.values()) {
                WishingWellItem.Builder builder = wish.toBuilder().setWishTime(wish.getWishTime() - leadTime).setClaimTime(wish.getClaimTime() - leadTime).setRewardIndex(0).setState(WishStateEnum.WSE_UnChoose);
                wishWell.putWishMap(wish.getWishIndex(), builder.build());
            }
            wishWell.setStartTime(wishWell.getStartTime() - leadTime);
        });
        target.sendWishUpdate();
        return true;
    }

   /* @GmFunction
    private boolean pushMsg(String playerIdx, int msg) {
        switch (msg) {
            case 1:
                MistPvPPushManage.getInstance().executePush();
                break;
            case 2:
                MineDoublePushManage.getInstance().executePush();
                break;
            case 3:
                ForeignInvasionPushManage.getInstance().executePush();
                break;
            case 5:
                OnHookPushManage.getInstance().executePush();
                break;
            case 6:
                OfflinePushManage.getInstance().executePush();
                break;

        }
        return true;
    }*/

    @GmFunction
    private boolean addGold(String playerIdx, int addGoldCount) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null || addGoldCount <= 0) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            player.addCurrency(RewardTypeEnum.RTE_Gold, addGoldCount, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM));
        });
        return true;
    }

    @GmFunction
    private boolean recharge(String playerIdx, int rechargeId) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        RechargeObject config = Recharge.getById(rechargeId);
        if (config == null) {
            return false;
        }
        RechargeProductObject product = RechargeProduct.getById(config.getProductid());
        if (product == null) {
            return false;
        }

        PlatformPurchaseData platformPurchaseData = new PlatformPurchaseData();
        platformPurchaseData.setProductCode(product.getIosproductid());
        platformPurchaseData.setUserId(player.getUserid());
        return PurchaseManager.getInstance().settlePurchaseByPlatformPurchaseData(platformPurchaseData);
    }

    @GmFunction
    private boolean purchase(String playerIdx, String productId) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        boolean exist = RechargeProduct._ix_id.values().stream().anyMatch(config -> config.getGoogleproductid().equals(productId)
                || config.getIosproductid().equals(productId) || config.getHyzproductid().equals(productId));

        if (!exist) {
            return false;
        }
        PlatformPurchaseData platformPurchaseData = new PlatformPurchaseData();
        platformPurchaseData.setProductCode(productId);
        platformPurchaseData.setUserId(player.getUserid());
        return PurchaseManager.getInstance().settlePurchaseByPlatformPurchaseData(platformPurchaseData);
    }

    @GmFunction
    private boolean directGift(String playerIdx, int giftIndex) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        Server.ServerActivity activity = ActivityManager.getInstance().findOneRecentActivityByType(Activity.ActivityTypeEnum.ATE_DirectPurchaseGift);
        if (activity == null) {
            return false;
        }
        if (activity.getDirectPurchaseGiftCount() <= giftIndex) {
            return false;
        }
        Activity.DirectPurchaseGift gift = activity.getDirectPurchaseGift(giftIndex);
        RechargeProductObject cfg = RechargeProduct.getById(gift.getRechargeProductId());
        if (cfg == null) {
            return false;
        }
        PlatformPurchaseData platformPurchaseData = new PlatformPurchaseData();
        platformPurchaseData.setProductCode(cfg.getIosproductid());
        platformPurchaseData.setUserId(player.getUserid());
        return PurchaseManager.getInstance().settlePurchaseByPlatformPurchaseData(platformPurchaseData);
    }

    @GmFunction
    private boolean petMissionLv(String playerIdx, int level) {
        PetMissionLevelObject config = PetMissionLevel.getByMissionlv(level);
        if (config == null || level <= 0) {
            return false;
        }
        petmissionEntity entity = petmissionCache.getInstance().getEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(entity, e -> {
            protocol.PetDB.SerializablePetMission.Builder missionListBuilder = entity.getMissionListBuilder();
            missionListBuilder.setMissionLv(level).clearUpLvPro();
            entity.sendPetMissionUpProUpdate(missionListBuilder.getUpLvProMap());
        });
        return true;
    }

    @GmFunction
    private boolean addFeats(String playerIdx, int addFeatsCount) {
        if (addFeatsCount <= 0) {
            return false;
        }
        itembagEntity itemBagByPlayerIdx = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBagByPlayerIdx == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(itemBagByPlayerIdx, e -> {
            itemBagByPlayerIdx.addItem(GameConst.ITEM_ID_FEATS, addFeatsCount, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), true);
        });
        return true;
    }

    @GmFunction
    private boolean addDiamond(String playerIdx, int addDiamondCount) {
        if (addDiamondCount <= 0) {
            return false;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            player.addCurrency(RewardTypeEnum.RTE_Diamond, addDiamondCount, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM));
            player.sendCurrencyRefreshMsg(RewardTypeEnum.RTE_Diamond);
        });
        return true;
    }

    @GmFunction
    private boolean addExp(String playerIdx, int addExpCount) {
        if (addExpCount < 0) {
            return false;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            player.addExperience(addExpCount);
        });

        return true;
    }

    @GmFunction
    private boolean clearSpire(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            Builder db_data = player.getDb_data();
            if (db_data != null) {
                db_data.clearEndlessSpireInfo();
            }
        });
        return true;
    }

    @GmFunction
    private boolean systemChat(String playerIdx, String info) {
        SC_SystemChat.Builder systemBuilder = SC_SystemChat.newBuilder();
        systemBuilder.setInfo(info);
        GlobalData.getInstance().sendMsgToAllOnlinePlayer(MsgIdEnum.SC_SystemChat, systemBuilder);
        return true;
    }

    @GmFunction
    private boolean addVipExp(String playerIdx, int vipExp) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            player.addVipExp(vipExp);
        });
        return true;
    }

    @GmFunction
    private boolean clearMainLine(String playerIdx) {
        mainlineEntity mainLine_1 = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (mainLine_1 == null) {
            LogUtil.error("playerIdx" + playerIdx + "] mainlineEntity is null");
            return false;
        }
        SyncExecuteFunction.executeConsumer(mainLine_1, e -> {
            mainLine_1.clearDBData();
            mainLine_1.sendRefreshMainLineMsg();
        });
        return true;
    }

    @GmFunction
    private boolean clearAvatar(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            Builder db_data = player.getDb_data();
            if (db_data != null) {
                db_data.clearAvatarList();
                player.sendPlayerBaseInfo(false);
            }
        });
        return true;
    }

    @GmFunction
    private boolean addAvatarBorder(String playerIdx, int avatarBorder) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (HeadBorder.getById(avatarBorder) == null || player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, ply -> {
            player.addAvatarBorder(Collections.singleton(avatarBorder));
        });
        return true;
    }

    @GmFunction
    private boolean clearAvatarBorder(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, ply -> {
            Builder db_data = ply.getDb_data();
            if (db_data != null) {
                db_data.setCurAvatarBorder(0);
                db_data.clearAvatarBorders();
                ply.sendPlayerBaseInfo(false);
            }
        });
        return true;
    }

    @GmFunction
    private boolean clearLv(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            GameConfigObject gameCfg = GameConfig.getById(GameConst.CONFIG_ID);
            if (gameCfg != null) {
                player.setLevel(gameCfg.getDefaultlv());
                player.setExperience(0);
                player.sendRefreshPlayerLvMsg();
            }
        });
        return true;
    }

    @GmFunction
    private boolean clearRenameInterval(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            Builder db_data = player.getDb_data();
            if (db_data != null) {
                db_data.clearNextRenameTime();
            }
        });
        return true;
    }

    @GmFunction
    private boolean setMistPermitLevel(String playerIdx, int permitLv) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(target, entity -> entity.getDb_Builder().getMistTaskDataBuilder().setCurEnterLevel(permitLv));
        return true;
    }

    @GmFunction
    private boolean resetMistNewbieTask(String playerIdx) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return false;
        }
        TargetMission.Builder targetBuilder = target.getDb_Builder().getMistTaskDataBuilder().getCurNewbieTaskBuilder();
        if (targetBuilder == null) {
            return false;
        }

        SyncExecuteFunction.executeConsumer(target, entity -> entity.getDb_Builder().getMistTaskDataBuilder().getCurNewbieTaskBuilder().clear());
        return true;
    }

    @GmFunction
    private boolean addMistStamina(String playerIdx, int addVal) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> entity.addMistStamina(addVal, true));
        return true;
    }

    @GmFunction
    private boolean clearMistSweepTask(String playerIdx) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(target, entity -> entity.getDb_Builder().getMistTaskDataBuilder().clearSweepTaskDbData());
        return true;
    }

    @GmFunction
    private boolean addMistMoveEffect(String playerIdx, int effectId) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> entity.addMistMoveEffect(Collections.singletonList(effectId)));
        return true;
    }

    @GmFunction
    private boolean resetPosition(String playerIdx) {
        teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (entity == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(entity, team -> {
            entity.getDB_Builder().setUnlockPosition(0);
        });

        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (teamEntity != null) {
            Event event = Event.valueOf(EventType.ET_UnlockTeamAndPosition, GameUtil.getDefaultEventSource(), teamEntity);
            event.pushParam(PlayerUtil.queryPlayerLv(playerIdx));
            EventManager.getInstance().dispatchEvent(event);
        }

        return true;
    }

//    @GmFunction
//    private boolean forInvBossDamage(String playerIdx, int damage) {
//        ForeignInvasionManager.getInstance().addBossDamageCount(playerIdx, damage, "");
//        return true;
//    }
//
//    @GmFunction
//    private boolean forInvKillMonster(String playerIdx, int killCount) {
//        for (int i = 0; i < killCount; i++) {
//            ForeignInvasionManager.getInstance().KillOneMonster();
//        }
//        return true;
//    }

    /**
     * 外敌入侵跑马灯
     *
     * @param playerIdx
     * @param count
     * @return
     */
    @GmFunction
    private boolean forInvMarquee(String playerIdx, int count) {
        SC_BossKilledBarrage.Builder builder = SC_BossKilledBarrage.newBuilder();
        builder.setPlayerIdx(playerIdx);
        String msg = ServerStringRes.getContentByLanguage(ForeignInvasionParamConfig.getById(GameConst.CONFIG_ID).getBosskilledbarrageid(), PlayerUtil.queryPlayerLanguage(playerIdx), PlayerUtil.queryPlayerName(playerIdx), new Random().nextInt());
        builder.setStr(msg);
        for (int i = 0; i < count; i++) {
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_BossKilledBarrage_VALUE, builder);
            builder.setPlayerIdx("");
        }
        return true;
    }

    /**
     * 跳关并获得奖励
     *
     * @param playerIdx
     * @param targetPoint
     * @return
     */
    @GmFunction
    private boolean passMainReward(String playerIdx, int targetPoint) {
        MainLineCheckPointObject mainLineCfg = MainLineCheckPoint.getById(targetPoint);
        if (mainLineCfg == null) {
            return false;
        }
        mainlineEntity mainLine = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (mainLine == null) {
            LogUtil.error("playerIdx" + playerIdx + "] mainlineEntity is null");
            return false;
        }
        // 获取更改前的节点
        int curCheckPoint = mainLine.getDBBuilder().getMainLinePro().getCurCheckPoint();
        MainLineCheckPointObject curPointCfg = MainLineCheckPoint.getById(curCheckPoint);
        int beforeNode = curPointCfg != null ? ArrayUtil.getMinInt(curPointCfg.getNodelist(), 0) : 0;

        // 跳关
        fastPassMainLine(playerIdx, targetPoint);

        // 获取跳关后的节点
        int afterNode = ArrayUtil.getMinInt(mainLineCfg.getNodelist(), beforeNode);
        // 发放奖励
        List<Reward> rewards = new ArrayList<>();
        for (int i = beforeNode; i < afterNode; i++) {
            MainLineNodeObject nodeCfg = MainLineNode.getById(i);
            if (nodeCfg == null) {
                continue;
            }

            List<Reward> fightReward = RewardUtil.getRewardsByFightMakeId(nodeCfg.getFightmakeid());
            if (fightReward != null) {
                rewards.addAll(fightReward);
            }
        }
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), true);
        return true;
    }

    @GmFunction
    private boolean fastPassMainLine(String playerIdx, int targetPoint) {
        MainLineCheckPointObject mainLineCfg = MainLineCheckPoint.getById(targetPoint);
        if (mainLineCfg == null) {
            return false;
        }

        mainlineEntity mainLine_1 = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (mainLine_1 == null) {
            LogUtil.error("playerIdx" + playerIdx + "] mainlineEntity is null");
            return false;

        }
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);

        SyncExecuteFunction.executeConsumer(target, e -> {
            completeKeyNodeMissions(playerIdx, mainLine_1.getDBBuilder().getKeyNodeId());
        });

        SyncExecuteFunction.executeConsumer(mainLine_1, e -> {
            MainLineProgress.Builder mainLineProBuilder = mainLine_1.getDBBuilder().getMainLineProBuilder();
            mainLineProBuilder.setAlreadyPassed(targetPoint);
            mainLineProBuilder.setCurCheckPoint(targetPoint);
            mainLineProBuilder.clearLastTransferNode();
            mainLine_1.getDBBuilder().setKeyNodeId(findCurKeyNodeIdByCheckPoint(mainLineCfg));
            mainLine_1.checkPointUnlock();
            mainLine_1.sendRefreshMainLineMsg();
            mainLine_1.sendKeyNodeMissions();
            for (int checkPoint = 1; checkPoint <= targetPoint; checkPoint++) {
                mainLine_1.unlockEpisode(checkPoint);
            }
        });
        RankingManager.getInstance().updatePlayerRankingScore(playerIdx, EnumRankingType.ERT_MainLine, RankingName.RN_MainLinePassed, targetPoint);
        //   RankingManager.getInstance().updatePlayerRankingScore(playerIdx, EnumRankingType.ERT_MainLine, RankingName.RN_MainLinePassed, mainlineCache.getInstance().getCurOnHookNode(playerIdx));
        return true;
    }

    private void completeKeyNodeMissions(String playerIdx, int keyNodeId) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return;
        }
        for (int i = 0; i < keyNodeId; i++) {
            List<MissionObject> missions = MissionManager.getInstance().getKeyNodeMissionObjectsByMissionKeyNode(i);
            if (CollectionUtils.isEmpty(missions)) {
                continue;
            }

            for (MissionObject missionCfg : missions) {
                TargetSystem.TargetMission mission = TargetSystem.TargetMission.newBuilder().setCfgId(missionCfg.getId()).setProgress(missionCfg.getTargetcount()).setStatus(Common.MissionStatusEnum.MSE_Finished).build();
                target.getDb_Builder().putKeyNodeMission(missionCfg.getId(), mission);
            }
        }
    }

    private int findCurKeyNodeIdByCheckPoint(MainLineCheckPointObject targetPoint) {
        for (KeyNodeConfigObject cfg : KeyNodeConfig._ix_id.values()) {
            int keyNodeId = cfg.getMainlinenodeid();
            int targetNode = targetPoint.getNodelist()[0];
            if (keyNodeId > targetNode) {
                return cfg.getId();
            }
        }
        return KeyNodeConfig._ix_id.keySet().stream().max(Integer::compareTo).orElse(0);
    }

    @GmFunction
    private boolean clearDailyMission(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                DB_TargetSystem.Builder db_data = entity.getDb_Builder();
                if (db_data != null) {
                    db_data.clearDailyMission();
                    db_data.clearNextResetMissionTime();
                    entity.sendClearDailyMissionMsg();
                }
            });
        }
        return true;
    }

    @GmFunction
    private boolean clearAchievement(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                DB_TargetSystem.Builder db_data = entity.getDb_Builder();
                if (db_data != null) {
                    db_data.clearAchievement();
                }
            });
        }
        return true;
    }

    @GmFunction
    private boolean resetAllPlayerTeam(String playerIdx) {
        for (BaseEntity value : teamCache.getInstance()._ix_id.values()) {
            teamEntity teams = (teamEntity) value;
            Map<Integer, Team> teamsMap = teams.getDB_Builder().getTeamsMap();
            boolean b1 = teamsMap.values().stream().anyMatch(e -> e.getTeamNum() == TeamNumEnum.TNE_Team_3);
            boolean b2 = teamsMap.values().stream().noneMatch(e -> e.getTeamNum() == TeamNumEnum.TNE_Team_1);
            if (b1 && b2) {
                SyncExecuteFunction.executeConsumer(teams, t -> {
                    for (int i = 1; i < 4; i++) {
                        Team team1 = new Team();
                        team1.setTeamNum(TeamNumEnum.forNumber(i));
                        teams.getDB_Builder().putTeams(i, team1);
                    }
                });
            }
            teams.sendTeamsInfo();
        }
        return true;
    }

    @GmFunction
    private boolean clearDiamond(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            player.setDiamond(0);
            player.sendCurrencyRefreshMsg(RewardTypeEnum.RTE_Diamond);
        });
        return true;
    }

    @GmFunction
    private boolean clearGold(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, e -> {
            player.setGold(0);
            player.sendCurrencyRefreshMsg(RewardTypeEnum.RTE_Gold);
        });
        return true;
    }

    @GmFunction
    private boolean updatePlayerRankScore(String playerIdx, int rankType, int score1, int score2) {
        EnumRankingType enumRank = EnumRankingType.forNumber(rankType);
        RankingManager.getInstance().updatePlayerRankingScore(playerIdx, enumRank, score1, score2);
        return true;
    }

    private boolean clearResCopy(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> {
            Builder db_data = player.getDb_data();
            db_data.clearResCopyData();
            player.checkResCopy();
        });
        return true;
    }

    @GmFunction
    private boolean clearActivity(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                DB_TargetSystem.Builder db_builder = entity.getDb_Builder();
                db_builder.clearActivities();
            });
        }
        return true;
    }

//    @GmFunction
//    private boolean queryForInvRanking(String playerIdx) {
//        ForeignInvasionManager.getInstance().updateRankingInfo();
//        return true;
//    }

//    @GmFunction
//    private boolean clearRanking(String playerIdx, String rankingName) {
//        HttpRequestUtil.asyncClearRanking(rankingName, ServerConfig.getInstance().getServer());
//        return true;
//    }

    @GmFunction
    private boolean clearAllRanking(String playerIdx) {
        for (EnumRankingType value : EnumRankingType.values()) {
            RankingManager.getInstance().clearRanking(value);
        }
        return true;
    }

    @GmFunction
    private boolean clearNovice(String playerIdx) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target != null) {
            SyncExecuteFunction.executeConsumer(target, t -> {
                DB_TargetSystem.Builder db_builder = target.getDb_Builder();
                if (db_builder != null) {
                    DB_SpecialActivity.Builder specialInfoBuilder = db_builder.getSpecialInfoBuilder();
                    specialInfoBuilder.clearNovice();
                }
            });
        }
        return true;
    }

    @GmFunction
    private boolean noviceAddScore(String playerIdx, int add) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target != null) {
            SyncExecuteFunction.executeConsumer(target, t -> {
                DB_TargetSystem.Builder db_builder = target.getDb_Builder();
                if (db_builder != null) {
                    DB_NoviceCredit.Builder noviceBuilder = db_builder.getSpecialInfoBuilder().getNoviceBuilder();
                    noviceBuilder.setCurPoint(noviceBuilder.getCurPoint() + add);
                }
            });
        }
        return true;
    }

    @GmFunction
    private boolean noviceNextDay(String playerIdx) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target != null) {
            SyncExecuteFunction.executeConsumer(target, t -> {
                DB_TargetSystem.Builder db_builder = target.getDb_Builder();
                if (db_builder != null) {
                    DB_NoviceCredit.Builder noviceBuilder = db_builder.getSpecialInfoBuilder().getNoviceBuilder();
                    noviceBuilder.setStartTime(noviceBuilder.getStartTime() - TimeUtil.MS_IN_A_DAY);
                    noviceBuilder.setNextCanUpdateTime(noviceBuilder.getNextCanUpdateTime() - TimeUtil.MS_IN_A_DAY);
                }
            });
        }
        return true;
    }

    @GmFunction
    private boolean clearPointCopy(String playerIdx) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target != null) {
            SyncExecuteFunction.executeConsumer(target, t -> {
                target.getDb_Builder().getSpecialInfoBuilder().clearPointCopy();
            });
        }
        return true;
    }

    @GmFunction
    private boolean addCoupon(String playerIdx, int addCount) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, p -> {
            player.addCurrency(RewardTypeEnum.RTE_Coupon, addCount, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM));
        });
        return true;
    }

    @GmFunction
    private boolean res2(String playerIdx) {
        return res(playerIdx);
    }

    @GmFunction
    private boolean res(String playerIdx) {
        final int addCount = 999;
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player != null) {
            SyncExecuteFunction.executeConsumer(player, p -> {
                player.addCurrency(RewardTypeEnum.RTE_Gold, addCount, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM));
                player.addCurrency(RewardTypeEnum.RTE_Diamond, addCount, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM));
                player.addCurrency(RewardTypeEnum.RTE_Coupon, addCount, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM));
            });
        }

        itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBag != null) {
            Map<Integer, Integer> addItem = new HashMap<>();
            SyncExecuteFunction.executeConsumer(itemBag, e -> {
                Map<Integer, ItemObject> ix_id = Item._ix_id;
                if (ix_id != null) {
                    for (ItemObject value : ix_id.values()) {
                        if (value.getSpecialtype() != ItemType.ONLY_USE_FOR_DISPLAY) {
                            addItem.put(value.getId(), addCount);
                        }
                    }

                    itemBag.addItem(addItem, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM));
                }
            });
        }
        return true;
    }

    @GmFunction
    private boolean clearQuick(String playerIdx) {
        mainlineEntity mainLineEntityByPlayerIdx = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (mainLineEntityByPlayerIdx != null) {
            SyncExecuteFunction.executeConsumer(mainLineEntityByPlayerIdx, t -> {
                mainLineEntityByPlayerIdx.updateDailyData(true);
            });
        }
        return true;
    }

    @GmFunction
    private boolean clearPlatMarquee(String playerIdx) {
        PlatformManager.getInstance().clearMarquee();
        return true;
    }

    @GmFunction
    private boolean clearPlatMail(String playerIdx) {
        PlatformManager.getInstance().clearMail();
        return true;
    }

    @GmFunction
    private boolean addMineBonusBuff(String playerIdx, int cfgId) {
//        playermineEntity playerMine = playermineCache.getInstance().getMineByPlayerIdx(playerIdx);
//        if (playerMine == null) {
//            return false;
//        }
//        MineGiftBaseCfgObject mineCfg = MineGiftBaseCfg.getByCfgid(cfgId);
//        if (mineCfg == null) {
//            return false;
//        }
//        SyncExecuteFunction.executeConsumer(playerMine, entity -> {
//            MineGiftEffect.Builder newEffect = MineGiftEffect.newBuilder();
//            newEffect.setCfgId(mineCfg.getCfgid());
//            newEffect.setExpireTimeStamp(mineCfg.getEffecttime() * 1000 + GlobalTick.getInstance().getCurrentTime());
//            entity.getMineFightData().addMineGiftEffect(newEffect);
//            entity.sendMineEffectInfo();
//        });
        return true;
    }

    @GmFunction
    private boolean clearGoldExchange(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> {
            Builder db_data = player.getDb_data();
            db_data.getGoldExchangeBuilder().clearGoldExTimes();
            player.refreshGoldExchangeInfo();
        });
        return true;
    }

    @GmFunction
    private boolean clearBrave(String playerIdx) {
        bravechallengeEntity entityByPlayer = bravechallengeCache.getInstance().getEntityByPlayer(playerIdx);
        if (entityByPlayer != null) {
            SyncExecuteFunction.executeConsumer(entityByPlayer, e -> {
                entityByPlayer.updateDailyDate(true);
            });
        }
        return true;
    }

    @GmFunction
    private boolean clearBossTime(String playerIdx) {
        targetsystemEntity targetSystem = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (targetSystem != null) {
            SyncExecuteFunction.executeConsumer(targetSystem, entity -> entity.getDb_Builder().getSpecialInfoBuilder().getActivityBossBuilder().setTimes(0));
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ActivityBossUpdate_VALUE, SC_ActivityBossUpdate.newBuilder().setRetCode(RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success).build()).setTimes(targetSystem.getDb_Builder().getSpecialInfo().getActivityBoss().getTimes()));
        }
        return true;
    }

    @GmFunction
    private boolean addDrawCardExp(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, p -> {
            player.addDrawCardExp(10000);
        });
        return true;
    }

    /**
     * 抽卡统计
     *
     * @param playerIdx
     * @return
     */
    @GmFunction
    private boolean draw(String playerIdx, String cardType, int count, boolean mustDrawCorePet) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (count <= 0 || player == null) {
            return false;
        }

        Collection<OddsRandom> result = null;
        if ("friend".equalsIgnoreCase(cardType)) {
            result = DrawCardManager.getInstance().drawCardByType(EnumDrawCardType.EDCT_FRIEND, playerIdx, count, mustDrawCorePet);
        } else if ("common".equalsIgnoreCase(cardType)) {
            result = new ArrayList<>();
            for (int i = 0; i < count / 10; i++) {
                // 设置玩家消耗金额
                SyncExecuteFunction.executeConsumer(player, p -> {
                    DB_DrawCardData.Builder builder = player.getDb_data().getDrawCardBuilder();
                    int consumeCount = 10 * DrawCard.getById(GameConst.CONFIG_ID).getCommondrawcarddiamond();
                    builder.setDrawCardConsume(builder.getDrawCardConsume() + consumeCount);
                });
                result.addAll(DrawCardManager.getInstance().drawCardByType(EnumDrawCardType.EDCT_COMMON, playerIdx, 10, mustDrawCorePet));
            }
        } else if ("high".equalsIgnoreCase(cardType)) {
            result = new ArrayList<>();
            for (int i = 0; i < count / 10; i++) {
                result.addAll(DrawCardManager.getInstance().drawHighCard(playerIdx, mustDrawCorePet));
            }
        }

        if (GameUtil.collectionIsEmpty(result)) {
            return false;
        }

        List<Reward> rewards = result.stream().map(e -> RewardUtil.parseReward(e.getRewards())).collect(Collectors.toList());
        List<Reward> mergeRewards = RewardUtil.mergeReward(rewards);

        JSONArray array = new JSONArray();
        for (Reward mergeReward : mergeRewards) {
            JSONObject object = new JSONObject();
            object.put("type", mergeReward.getRewardTypeValue());
            object.put("id", mergeReward.getId());
            object.put("count", mergeReward.getCount());
            array.add(object);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_Tips_VALUE, SC_Tips.newBuilder().setMsg(array.toJSONString()));
        return true;
    }

    @GmFunction
    private boolean clearDrawCard(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }

        SyncExecuteFunction.executeConsumer(player, p -> {
            player.getDb_data().clearDrawCard();

        });
        return true;
    }

    @GmFunction
    private boolean addFriend(String playerIdx, int targetShortId) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        playerEntity target = playerCache.getInstance().getPlayerByShortId(targetShortId);
        if (player == null || target == null || Objects.equals(playerIdx, target.getIdx())) {
            return false;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();

        SyncExecuteFunction.executeConsumer(player, p -> {
            player.getDb_data().getFriendInfoBuilder().putOwned(target.getIdx(), DB_OwnedFriendInfo.newBuilder().setAddTime(currentTime).build());
            //目标：拥有好友个数
            EventUtil.triggerUpdateTargetProgress(player.getIdx(), TargetSystem.TargetTypeEnum.TTE_PlayerFriendReach, player.getDb_data().getFriendInfo().getOwnedCount(), 0);
        });


        FriendUtil.sendAddFriendMsg(player, target, currentTime);
        return true;
    }

    @GmFunction
    private boolean quickOnHookByHour(String playerIdx, int onHookHour) {
        mainlineEntity mainlineEntity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);

        long onHookTime = TimeUtil.MS_IN_A_HOUR * onHookHour;

        List<Reward> totalReward = mainlineEntity.calculateOnHookReward(onHookTime);

        if (CollectionUtils.isEmpty(totalReward)) {
            return false;
        }

        RewardManager.getInstance().doRewardByList(playerIdx, totalReward, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), true);
        return true;

    }

    @GmFunction
    private boolean quickOnHook(String playerIdx, int onHookHour) {
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return false;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.getDBBuilder().getOnHookIncomeBuilder().clearGainReward();
            long currentTime = GlobalTick.getInstance().getCurrentTime();
            long startTime = currentTime - onHookHour * TimeUtil.MS_IN_A_HOUR;
            entity.getDBBuilder().getOnHookIncomeBuilder().setStartOnHookTime(startTime);
            entity.getDBBuilder().getOnHookIncomeBuilder().setLastSettleTime(startTime);
        });
        return true;
    }

    /**
     * 多场景使用"," 分割
     *
     * @param playerIdx
     * @param priority
     * @param scenes
     * @return
     */
    @GmFunction
    private boolean marquee(String playerIdx, int priority, String scenes) {
        SC_Marquee.Builder builder = SC_Marquee.newBuilder();
        builder.setInfo("跑马灯测试" + TimeUtil.formatStamp(GlobalTick.getInstance().getCurrentTime()));
        builder.setCycleCount(5);
        builder.setPriorityValue(priority);

        String[] sceneStr = scenes.split("\\,");
        for (int i = 2; i < sceneStr.length; i++) {
            builder.addScenesValue(StringHelper.stringToInt(sceneStr[i], 0));
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_Marquee_VALUE, builder);
        return true;
    }

    @GmFunction
    private boolean lottery(String playerIdx, int times) {
        Map<Integer, Integer> qualityCount = new HashMap<>();

        for (int i = 0; i < times; i++) {
            Lottery lottery = ScratchLotteryManager.getInstance().randomLottery(playerIdx);
            List<Reward> rewards = ScratchLotteryManager.getInstance().settleLottery(lottery);

            if (CollectionUtils.isNotEmpty(rewards)) {
                for (Reward reward : rewards) {
                    if (reward.getRewardType() == RewardTypeEnum.RTE_PetFragment) {
                        int quality = PetFragmentConfig.getQualityByCfgId(reward.getId());
                        int oldCount = qualityCount.computeIfAbsent(quality, e -> 0);
                        qualityCount.put(quality, oldCount + 1);
                    }
                }
            }
        }

        JSONArray array = new JSONArray();
        for (Entry<Integer, Integer> entry : qualityCount.entrySet()) {
            JSONObject object = new JSONObject();
            object.put("quality", entry.getKey());
            object.put("count", entry.getValue());
            array.add(object);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_Tips_VALUE, SC_Tips.newBuilder().setMsg(array.toJSONString()));
        return true;
    }

    @GmFunction
    private boolean clearArena(String playerIdx) {
        arenaEntity entity = arenaCache.getInstance().getEntity(playerIdx);
        if (entity == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(entity, e -> e.getDbBuilder().clear());
        return true;
    }


    @GmFunction
    private boolean bossTowerSkip(String playerIdx, int bossTowerId) {
        bosstowerEntity entity = bosstowerCache.getInstance().getEntity(playerIdx);
        BossTowerConfigObject cfg1 = BossTowerConfig.getById(bossTowerId);
        if (cfg1 == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(entity, et -> {
            for (int i = entity.getDbBuilder().getMaxCfgId() + 1; i <= bossTowerId; i++) {
                BossTowerConfigObject cfg = BossTowerConfig.getById(i);
                if (cfg == null) {
                    continue;
                }
                entity.addBattleTimes(i, cfg.getUnbeatablefightmakeid(), 3, true);
            }
        });
        return true;
    }

    @GmFunction
    private boolean clearMail(String playerIdx) {
        mailboxEntity playerMail = mailboxCache.getInstance().getMailBoxByPlayerIdx(playerIdx);
        if (playerMail != null) {
            SyncExecuteFunction.executeConsumer(playerMail, pl -> {
                playerMail.getDBBuilder().clearMails();
            });
        }
        return true;
    }

    @GmFunction
    private boolean updateSex(String playerIdx, int sex) {
        if (sex != 0 && sex != 1) {
            sex = 1;
        }
        playerEntity playerEntity = playerCache.getByIdx(playerIdx);
        if (playerEntity != null) {
            int finalSex = sex;
            SyncExecuteFunction.executeConsumer(playerEntity, pl -> {
                playerEntity.setSex(finalSex);
            });
            playerEntity.sendPlayerBaseInfo(false);
        }
        return true;
    }

    @GmFunction
    private boolean jsCpCopyTime(String playerIdx, int min) {
        CpTeamCache cache = CpTeamCache.getInstance();
        CpCopyMap map = cache.findPlayerCopyMapInfo(playerIdx);
        if (map == null) {
            return false;
        }

        Long time = cache.loadPlayerMapExpire(map.getMapId());
        if (time == null) {
            return false;
        }
        long expire = time - TimeUtil.MS_IN_A_MIN * min;
        cache.savePlayerMapExpire(map.getMapId(), expire);
        CpCopyManger.getInstance().sendCopyInit(playerIdx);
        return true;
    }

    @GmFunction
    private boolean setMaxScene(String playerIdx, int scene) {
        CrossArenaManager.getInstance().savePlayerDBInfo(playerIdx, CrossArena.CrossArenaDBKey.LT_MAXSCENEID, scene, CrossArenaUtil.DbChangeRep);
        CrossArenaManager.getInstance().sendMainPanelInfo(playerIdx);
        return true;
    }

    /**
     * 用于验证祭坛内某个类型是否含有其他类型的宠物
     *
     * @param playerIdx
     * @param type
     * @param count
     * @return
     */
    @GmFunction
    private boolean altarType(String playerIdx, int type, int count) {
        List<Reward> rewards = AncientCallManager.getInstance().callAncient(playerIdx, type, count);
        Map<Integer, Integer> petTypeCount = new HashMap<>();
        for (Reward reward : rewards) {
            int typeById = PetBaseProperties.getTypeById(PetFragmentConfig.getLinkPetId(reward.getId()));
            int oldValue = petTypeCount.computeIfAbsent(typeById, e -> 0);
            petTypeCount.put(typeById, oldValue + 1);
        }

        JSONArray array = new JSONArray();
        for (Entry<Integer, Integer> entry : petTypeCount.entrySet()) {
            JSONObject object = new JSONObject();
            object.put("type", entry.getKey());
            object.put("count", entry.getValue());
            array.add(object);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_Tips_VALUE, SC_Tips.newBuilder().setMsg(array.toJSONString()));
        return true;
    }

    /**
     * 用于验证祭坛内某个类型中品质产出
     *
     * @param playerIdx
     * @param type
     * @param count
     * @return
     */
    @GmFunction
    private boolean altarDraw(String playerIdx, int type, int count) {
        List<Reward> rewards = AncientCallManager.getInstance().callAncient(playerIdx, type, count);
        Map<Integer, Integer> petQualityCount = new HashMap<>();
        for (Reward reward : rewards) {
            int qualityByCfgId = PetFragmentConfig.getQualityByCfgId(reward.getId());
            int oldValue = petQualityCount.computeIfAbsent(qualityByCfgId, e -> 0);
            petQualityCount.put(qualityByCfgId, oldValue + 1);
        }

        JSONArray array = new JSONArray();
        for (Entry<Integer, Integer> entry : petQualityCount.entrySet()) {
            JSONObject object = new JSONObject();
            object.put("quality", entry.getKey());
            object.put("count", entry.getValue());
            array.add(object);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_Tips_VALUE, SC_Tips.newBuilder().setMsg(array.toJSONString()));
        return true;
    }

    /**
     * 用于验证祭坛内某个类型中品质产出
     *
     * @param playerIdx
     * @param type
     * @param count
     * @return
     */
    @GmFunction
    private boolean assertDraw(String playerIdx, int type, int count) {
        List<Reward> rewards = AncientCallManager.getInstance().callAncient(playerIdx, type, count);
        Map<Integer, Integer> petQualityCount = new HashMap<>();
        for (Reward reward : rewards) {
            MapUtil.add2IntMapValue(petQualityCount, reward.getId(), reward.getCount());
        }

        JSONArray array = new JSONArray();
        for (Entry<Integer, Integer> entry : petQualityCount.entrySet()) {
            JSONObject object = new JSONObject();
            object.put("fragmentId", entry.getKey());
            object.put("count", entry.getValue());
            array.add(object);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_Tips_VALUE, SC_Tips.newBuilder().setMsg(array.toJSONString()));
        return true;
    }

    @GmFunction
    private boolean spireLv(String playerIdx, int newLv) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, p -> {
            p.getDb_data().getEndlessSpireInfoBuilder().setMaxSpireLv(newLv);
            SC_RefreashSpireLv.Builder builder = SC_RefreashSpireLv.newBuilder().setNewLv(newLv);
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RefreashSpireLv_VALUE, builder);
        });
        RankingManager.getInstance().updatePlayerRankingScore(player.getIdx(), EnumRankingType.ERT_Spire, RankingName.RN_EndlessSpire, player.getDb_data().getEndlessSpireInfoBuilder().getMaxSpireLv());
        return true;
    }

    /**
     * 爬塔和发放奖励
     *
     * @param playerIdx
     * @param newLv
     * @return
     */
    @GmFunction
    private boolean spireLvReward(String playerIdx, int newLv) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, p -> {
            int oldLv = p.getDb_data().getEndlessSpireInfo().getMaxSpireLv();
            if (newLv <= oldLv) {
                return;
            }
            p.getDb_data().getEndlessSpireInfoBuilder().setMaxSpireLv(newLv);

            List<Reward> rewards = new ArrayList<>();
            for (int i = oldLv + 1; i < newLv; i++) {
                EndlessSpireConfigObject cfg = EndlessSpireConfig.getBySpirelv(i);
                if (cfg != null) {
                    List<Reward> rewardList = RewardUtil.getRewardsByFightMakeId(cfg.getMonsterteamid());
                    if (CollectionUtils.isEmpty(rewardList)) {
                        LogUtil.error("Spire fight link rewards is null, spireLv:" + cfg.getSpirelv());
                    } else {
                        rewards.addAll(rewardList);
                    }
                }
            }

            RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), true);
        });
        return true;
    }

    @GmFunction
    private boolean resetAdsData(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, p -> p.updateAdsBonusData(true));
        return true;
    }

    @GmFunction
    private boolean selectedPet(String playerIdx, int petId, int index) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, p -> {
            player.setDrawCardSelectedPet(petId, index);
        });
        return true;
    }

    private void addPetMax(String playerIdx, PetBasePropertiesObject petBasePropertiesObject) {
        if (petBasePropertiesObject == null) {
            return;
        }
        PetRarityConfigObject rarityConfig = PetRarityConfig.getByRarityAndPropertyModel(petBasePropertiesObject.getMaxrarity(), petBasePropertiesObject.getPropertymodel());
        int maxRarity = petBasePropertiesObject.getMaxrarity();
        int maxPetLv = rarityConfig == null ? 1 : rarityConfig.getMaxlvl();
        petCache.getInstance().petSpecify(playerIdx, petBasePropertiesObject.getPetid(), maxPetLv, maxRarity, 15, 1);
    }

    private void sendGmToArena(String playerIdx, String params) {
        if (StringUtils.isEmpty(playerIdx) || StringUtils.isEmpty(params)) {
            return;
        }
        GS_CS_ArenaGm.Builder builder = GS_CS_ArenaGm.newBuilder();
        builder.setGmParams(params);
        builder.setPlayerIdx(playerIdx);
        CrossServerManager.getInstance().sendMsgToArena(playerIdx, MsgIdEnum.GS_CS_ArenaGm_VALUE, builder, false);
    }

    @GmFunction
    private boolean petRandom(String playerIdx) {
        for (int j = 0; j < 10; j++) {
            petCache.getInstance().playerObtainPet(playerIdx, RandomUtil.getRandomAvailablePet(), 1, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), true);
        }
        return true;
    }

    @GmFunction
    private boolean removeOneDayResCycle(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, p -> {
            PlayerDB.DB_ResourceRecycle.Builder resourceRecycle = player.getDb_data().getResourceRecycleBuilder();
            for (PlayerDB.DB_ResourceRecycleItem.Builder builder : resourceRecycle.getFunctionRecycleBuilderList()) {
                int recycleDays = builder.getRecycleInfoCount();
                if (recycleDays > 0) {
                    builder.removeRecycleInfo(recycleDays - 1);
                }

            }
        });
        player.sendResourceRecycleInfo();
        return true;
    }

    @GmFunction
    private boolean pet(String playerIdx, int petCfgId, int count) {
        petCache.getInstance().playerObtainPet(playerIdx, petCfgId, count, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), true);
        return true;
    }

    /**
     * 清除所有评论的屏蔽
     *
     * @param playerIdx
     * @return
     */
    @GmFunction
    private boolean clearCommentBan(String playerIdx) {
        for (BaseEntity value : commentCache.getInstance().getAll().values()) {
            if (!(value instanceof commentEntity)) {
                continue;
            }
            commentEntity entity = (commentEntity) value;
            SyncExecuteFunction.executeConsumer(entity, e -> entity.clearTotalBan());
        }

        commentCache.getInstance().initReportComment();
        return true;
    }

    /**
     * 以当前账号评论所有的评论点
     *
     * @param playerIdx
     * @return
     */
    @GmFunction
    private boolean commentAll(String playerIdx) {
        String commentBaseContent = PlayerUtil.queryPlayerName(playerIdx);
        for (Integer petBookId : PetBaseProperties._ix_petid.keySet()) {
            addComment(playerIdx, CommentTypeEnum.CTE_Pet_VALUE, petBookId, commentBaseContent);
        }

        for (BossTowerConfigObject cfg : BossTowerConfig._ix_id.values()) {
            addComment(playerIdx, CommentTypeEnum.CTE_BossTower_VALUE, cfg.getFightmakeid(), commentBaseContent);
            addComment(playerIdx, CommentTypeEnum.CTE_BossTower_VALUE, cfg.getDifficultfightmakeid(), commentBaseContent);
            addComment(playerIdx, CommentTypeEnum.CTE_BossTower_VALUE, cfg.getUnbeatablefightmakeid(), commentBaseContent);
        }
        return true;
    }

    private void addComment(String playerIdx, int type, int linkId, String baseContent) {
        if (StringUtils.isBlank(playerIdx) || StringUtils.isBlank(baseContent)) {
            return;
        }
        commentEntity entity = commentCache.getInstance().getEntity(type, linkId);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> entity.addComment(playerIdx, baseContent + new Random().nextInt()));
        }
    }

    /**
     * 随机举报评论
     *
     * @param playerIdx
     * @return
     */
    @GmFunction
    private boolean randomReport(String playerIdx) {
        int successReport = 5;
        finish:
        {
            for (BaseEntity value : commentCache.getInstance().getAll().values()) {
                if (!(value instanceof commentEntity)) {
                    continue;
                }

                commentEntity entity = (commentEntity) value;
                for (Long aLong : entity.getUnreportedCommentId()) {
                    if (RetCodeEnum.RCE_Success == SyncExecuteFunction.executeFunction(entity, e -> entity.addReported(aLong, playerIdx, EnumReportType.ERT_IllegalSpeech, ""))) {
                        successReport--;
                    }

                    if (successReport <= 0) {
                        break finish;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 随机自动处理评论(仅限测试用)
     *
     * @param playerIdx
     * @return
     */
    @GmFunction
    private boolean randomAutoDeal(String playerIdx) {
        int successReport = 5;
        finish:
        {
            for (BaseEntity value : commentCache.getInstance().getAll().values()) {
                if (!(value instanceof commentEntity)) {
                    continue;
                }

                commentEntity entity = (commentEntity) value;
                for (Long aLong : entity.getAllCommentId()) {
                    boolean dealResult = SyncExecuteFunction.executePredicate(entity, e -> {
                        CommentDbData.Builder builder = entity.getCommentBuilderById(aLong);
                        if (builder == null || builder.getBaned() || builder.getReportsCount() <= 0) {
                            return false;
                        }
                        entity.autoDeal(builder);
                        return true;
                    });

                    if (dealResult) {
                        successReport--;
                    }

                    if (successReport <= 0) {
                        break finish;
                    }
                }
            }
        }
        return true;
    }

    @GmFunction
    private boolean newPet(String playerIdx) {
        HashMap<Integer, Integer> petCountMap = new HashMap<>();
        petCountMap.put(4007, 1);
        petCountMap.put(4014, 1);
        petCountMap.put(4019, 1);
        petCountMap.put(4026, 1);
        petCountMap.put(4028, 1);
        petCountMap.put(4032, 1);
        petCountMap.put(4036, 1);
        petCountMap.put(4037, 1);

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM);
        petCache.getInstance().playerObtainPets(playerIdx, petCountMap, reason);
        return true;
    }

    @GmFunction
    private boolean randomAdvance(String playerIdx, int randomCount) {
        Map<Integer, Integer> randomRecord = new HashMap<>();
        for (int i = 0; i < randomCount; i++) {
            DrawCardAdvancedObject object = DrawCardAdvanced.randomAdvance();
            if (object != null) {
                Integer oldValue = randomRecord.get(object.getId());
                randomRecord.put(object.getId(), oldValue == null ? 1 : oldValue + 1);
            }
        }
        return true;
    }

    @GmFunction
    private boolean bossTowerBuff(String playerIdx, int difficulty, int buffId_1, int buffId_2) {
        EnumBossTowerDifficult difficult = EnumBossTowerDifficult.forNumber(difficulty);
        if (difficult == null || difficult == EnumBossTowerDifficult.EBS_Null) {
            return false;
        }
        return BossTowerManager.getInstance().setBuff(difficult, buffId_1, buffId_2);
    }

    @GmFunction
    private boolean enterWarRoom(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        TheWarManager.getInstance().enterWarRoom(player, false);
        return true;
    }

    @GmFunction
    private boolean settleWarRoom(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> entity.getDb_data().setTheWarRoomIdx(""));
        int serverIndex = CrossServerManager.getInstance().getSvrIndexByWarRoomIdx(roomIdx);
        if (serverIndex <= 0) {
            return false;
        }
        TheWarManager.getInstance().settleTheWar();
        String roomSvrIndexStr = jedis.hget(RedisKey.TheWarRoomServerIndex, roomIdx);
        int roomSvrIndex = StringHelper.stringToInt(roomSvrIndexStr, 0);
        if (serverIndex == roomSvrIndex) {
            jedis.hdel(RedisKey.TheWarAvailableJoinRoomInfo, roomIdx);
            jedis.hdel(RedisKey.TheWarRoomServerIndex, roomIdx);
            jedis.hdel(RedisKey.TheWarRoomData.getBytes(), roomIdx.getBytes());
            jedis.del((RedisKey.TheWarPlayerData + roomIdx).getBytes());
            jedis.del((RedisKey.TheWarGridData + roomIdx).getBytes());
        }
        GS_CS_TransGMCommand.Builder builder = GS_CS_TransGMCommand.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setGmCmdType(TheWarGmType.TWGT_SettleWarRoom);
        return CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_TransGMCommand_VALUE, builder);
    }

    @GmFunction
    private boolean setGridProp(String playerIdx, int posX, int posY, int propType, long propVal) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            return false;
        }
        GS_CS_TransGMCommand.Builder builder = GS_CS_TransGMCommand.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setGmCmdType(TheWarGmType.TWGT_SettGridProperty);
        GS_CS_SetGridPropGmCmd.Builder builder1 = GS_CS_SetGridPropGmCmd.newBuilder();
        builder1.setPos(Position.newBuilder().setX(posX).setY(posY));
        builder1.setPropTypeValue(propType);
        builder1.setPropValue(propVal);
        builder.setGmParams(builder1.build().toByteString());
        return CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_TransGMCommand_VALUE, builder);
    }

    @GmFunction
    private boolean addWarGold(String playerIdx, int addVal) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            return false;
        }
        GS_CS_TransGMCommand.Builder builder = GS_CS_TransGMCommand.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setGmCmdType(TheWarGmType.TWGT_AddWarGold);
        GS_CS_AddWarGoldGmCmd.Builder builder1 = GS_CS_AddWarGoldGmCmd.newBuilder();
        builder1.setAddVal(addVal);
        builder.setGmParams(builder1.build().toByteString());
        return CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_TransGMCommand_VALUE, builder);
    }

    @GmFunction
    private boolean addWarDp(String playerIdx, int addVal) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            return false;
        }
        GS_CS_TransGMCommand.Builder builder = GS_CS_TransGMCommand.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setGmCmdType(TheWarGmType.TWGT_AddWarDp);
        GS_CS_AddWarDpGmCmd.Builder builder1 = GS_CS_AddWarDpGmCmd.newBuilder();
        builder1.setAddVal(addVal);
        builder.setGmParams(builder1.build().toByteString());
        return CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_TransGMCommand_VALUE, builder);
    }

    @GmFunction
    private boolean addWarItem(String playerIdx, int itemCfgId, int itemNum) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            return false;
        }
        GS_CS_TransGMCommand.Builder builder = GS_CS_TransGMCommand.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setGmCmdType(TheWarGmType.TWGT_AddWarItem);
        GS_CS_AddWarItemGmCmd.Builder builder1 = GS_CS_AddWarItemGmCmd.newBuilder();
        builder1.setItemCfgId(itemCfgId);
        builder1.setItemNum(itemNum);
        builder.setGmParams(builder1.build().toByteString());
        return CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_TransGMCommand_VALUE, builder);
    }

    @GmFunction
    private boolean addHolyWater(String playerIdx, int addVal) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> entity.addCurrency(RewardTypeEnum.RTE_HolyWater, addVal, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM)));
        return true;
    }

    @GmFunction
    private boolean promoteJobTile(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            return false;
        }
        GS_CS_TransGMCommand.Builder builder = GS_CS_TransGMCommand.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setGmCmdType(TheWarGmType.TWGT_PromoteJobTile);
        return CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_TransGMCommand_VALUE, builder);
    }

    @GmFunction
    private boolean addPetEnergy(String playerIdx, int addVal) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            return false;
        }
        GS_CS_TransGMCommand.Builder builder = GS_CS_TransGMCommand.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setGmCmdType(TheWarGmType.TWGT_AddPetEnergy);
        GS_CS_AddPetEnergyGmCmd.Builder builder1 = GS_CS_AddPetEnergyGmCmd.newBuilder();
        builder1.setAddVal(addVal);
        builder.setGmParams(builder1.build().toByteString());
        return CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_TransGMCommand_VALUE, builder);
    }

    @GmFunction
    private boolean occupyGrid(String playerIdx, int posX, int posY) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            return false;
        }
        GS_CS_TransGMCommand.Builder builder = GS_CS_TransGMCommand.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setGmCmdType(TheWarGmType.TWGT_OccupyGrid);
        GS_CS_OccupyGirdGmCmd.Builder builder1 = GS_CS_OccupyGirdGmCmd.newBuilder();
        builder1.getOccupyGirdBuilder().setX(posX).setY(posY);
        builder.setGmParams(builder1.build().toByteString());
        return CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_TransGMCommand_VALUE, builder);
    }

    @GmFunction
    private boolean finishCurWarTask(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            return false;
        }
        GS_CS_TransGMCommand.Builder builder = GS_CS_TransGMCommand.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setGmCmdType(TheWarGmType.TWGT_FinishCurTask);
        return CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_TransGMCommand_VALUE, builder);
    }

    @GmFunction
    private boolean braveNextDay(String playerIdx) {
        bravechallengeEntity entity = bravechallengeCache.getInstance().getEntityByPlayer(playerIdx);
        if (entity == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(entity, e -> entity.updateDailyDate(true));
        return true;
    }

    @GmFunction
    private boolean level(String playerIdx, int newLv) {
        int level = Math.min(Math.max(0, newLv), PlayerLevelConfig.maxLevel);
        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                int before = entity.getLevel();
                entity.setLevel(level);
                entity.sendRefreshPlayerLvMsg();
                entity.lvUp(before, level);
            });
        }
        return true;
    }

    @GmFunction
    private boolean reloadPatrol(String playerIdx, int patrolCfg, int mapId) {
        patrolEntity patrolExist = patrolCache.getInstance().getCacheByPlayer(playerIdx);
        PatrolConfigObject cfg = PatrolConfig.getById(patrolCfg);
        if (cfg == null) {
            return false;
        }
        PatrolMapObject mapConfig = PatrolMap.getByMapid(mapId);
        if (mapConfig == null) {
            return false;
        }
        PatrolServiceImpl.getInstance().upsertPatrolEntity(playerIdx, patrolExist, patrolCfg, mapId);
        return true;
    }

    /**
     * 清空技能等级
     *
     * @param playerIdx
     * @return
     */
    @GmFunction
    private boolean clearPlayerSkillLv(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }

        SyncExecuteFunction.executeConsumer(player, e -> {
            for (Artifact.Builder newArtifact : player.getDb_data().getArtifactBuilderList()) {
                newArtifact.getPlayerSkillBuilder().setSkillLv(1);
            }
        });

        return true;
    }

    @GmFunction
    private boolean skipNewBeeGuide(String playerIdx) {
        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                // 先将玩家踢下线
                entity.kickOut(RetCodeEnum.RCE_KickOut_SkipNewBeeGuide);
                entity.getDb_data().getNewBeeInfoBuilder().clearPlayerNewbeeStep();
                entity.getDb_data().getNewBeeInfoBuilder().addPlayerNewbeeStep(-1);
            });
        }
        return true;
    }

    /**
     * 签到天数设置
     *
     * @param playerIdx
     * @param days
     * @return
     */
    @GmFunction
    private boolean signInDays(String playerIdx, int days) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null || days < 0) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_CumuSignIn.Builder signBuilder = entity.getDb_Builder().getSpecialInfoBuilder().getSignInBuilder();
            signBuilder.setCumuDays(days);
        });
        CumuSignInObject cfg = CumuSignIn.getByDays(days);
        if (cfg != null) {
            List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(cfg.getRewards());
            RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CumuSignIn), true);
        }
        return true;
    }

    /**
     * 清空远古召唤
     *
     * @param playerIdx
     * @return
     */
    @GmFunction
    private boolean clearAncient(String playerIdx) {
        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                entity.getDb_data().clearAncientAltar();
            });
        }
        return true;
    }

    /**
     * 清空商店手动刷新次数
     *
     * @param playerIdx
     * @return
     */
    @GmFunction
    private boolean clearShopManualTimes(String playerIdx) {
        shopEntity entity = shopCache.getInstance().getEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                for (Entry<Integer, PlayerShopInfo> entry : entity.getDbBuilder().getShopInfo().entrySet()) {
                    PlayerShopInfo infoBuilder = entry.getValue().clearManualRefreshTimes();
                    entity.putPlayerShopInfo(ShopTypeEnum.forNumber(entry.getKey()), infoBuilder);
                }
            });
        }
        return true;
    }

    @GmFunction
    private boolean clearMistTimeLimitMissionPro(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                entity.getDb_Builder().getSpecialInfoBuilder().clearMistTimeLimitMission();
            });
        }
        return true;
    }

    @GmFunction
    private boolean clearArenaMission(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                e.getDb_Builder().clearArenaMission();
            });
        }
        return true;
    }

    @GmFunction
    private boolean fistPayActivityPassDay(String playerIdx) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(target, e -> {
            Map<Integer, protocol.TargetSystemDB.PayActivityRecord> map = target.getDb_Builder().getPayActivityRecordMap();
            for (Entry<Integer, PayActivityRecord> entry : map.entrySet()) {
                if (entry.getValue().getBonusCount() > 0) {
                    PayActivityRecord.Builder record = entry.getValue().toBuilder();
                    for (PayActivityBonus.Builder builder : record.getBonusBuilderList()) {
                        if (builder.getClaimTimestamp() > 0) {
                            builder.setClaimTimestamp(builder.getClaimTimestamp() - TimeUtil.MS_IN_A_DAY);
                        }
                    }
                    target.getDb_Builder().putPayActivityRecord(entry.getKey(), record.build());
                }
            }
        });
        targetsystemCache.getInstance().sendRechargeActivityShow(playerIdx);
        return true;

    }

    @GmFunction
    private boolean dayDayRechargeNextDay(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);

        if (player != null) {
            SyncExecuteFunction.executeConsumer(player, e -> {
                player.getDb_data().clearTodayRecharge();
            });
            player.sendPlayerCurRecharge();
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                entity.updateDayDayRechargeDailyData(true);

            });
        }
        return true;
    }

    @GmFunction
    private boolean zeroCostNextDay(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                Map<Integer, Activity.ZeroCostPurchaseItem> zeroCostPurchaseMap = entity.getDb_Builder().getSpecialInfoBuilder().getZeroCostPurchaseBuilder().getZeroCostPurchaseMap();
                for (Activity.ZeroCostPurchaseItem item : zeroCostPurchaseMap.values()) {
                    Activity.ZeroCostPurchaseItem newItem = item.toBuilder().setNextClaimTime(item.getNextClaimTime() - TimeUtil.MS_IN_A_DAY).build();
                    entity.getDb_Builder().getSpecialInfoBuilder().getZeroCostPurchaseBuilder().putZeroCostPurchase(newItem.getPurchaseCfg(), newItem);
                }
            });
            for (Integer cfgId : entity.getDb_Builder().getSpecialInfo().getZeroCostPurchase().getZeroCostPurchaseMap().keySet()) {
                entity.sendZeroCostPurchaseUpdate(cfgId);
            }
        }
        return true;
    }

    @GmFunction
    private boolean addHadesTimes(String playerIdx, int times) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                for (Long aLong : entity.getDb_Builder().getHadesInfoMap().keySet()) {
                    DB_HadesActivityInfo.Builder infoBuilder = entity.getHadesActivityInfoBuilder(aLong);
                    infoBuilder.setRemainTimes(infoBuilder.getRemainTimes() + times);
                    entity.putHadesActivityInfoBuilder(infoBuilder);
                    entity.sendHadesActivityInfo(aLong);
                }
            });
        }
        return true;
    }

    @GmFunction
    private boolean clearTimeLimitSignIn(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                e.getDb_Builder().getSpecialInfoBuilder().getTimeLimitActivitiesBuilder().clearNextCanUpdateTime();
            });
        }
        return true;
    }

    /**
     * 重置每日数据,谨慎操作,重置全服
     *
     * @param playerIdx
     * @return
     */
    @GmFunction
    private boolean updateDailyData(String playerIdx) {
        EventUtil.unlockObjEvent(EventType.ET_DailyUpdatePlayerAllFunction, Collections.singletonList(playerIdx), true);
        return true;
    }

    @GmFunction
    private boolean updateWeeklyData(String playerIdx) {
        EventUtil.unlockObjEvent(EventType.ET_WeeklyUpdatePlayerAllFunction, Collections.singletonList(playerIdx));
        return true;
    }

    @GmFunction
    private boolean shopAutoRefresh(String playerIdx, int shopId) {
        shopEntity entity = shopCache.getInstance().getEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> entity.autoRefresh(ShopTypeEnum.forNumber(shopId)));
        }
        return true;
    }

    @GmFunction
    private boolean forInv(String playerIdx, int buildingId, int killCount) {
        NewForeignInvasionManager.getInstance().killMonster(playerIdx, buildingId, killCount);
        return true;
    }

    @GmFunction
    private boolean clearTheWarMission(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                entity.getDb_Builder().getSpecialInfoBuilder().clearTheWarSeasonMission();
                entity.sendUpdateTheWarMissionMsg();
            });
        }
        return true;
    }

    @GmFunction
    private boolean dailyFirstRechargeNextDay(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                entity.clearDailyFirstRechargeData(true);
            });
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player != null) {
            SyncExecuteFunction.executeConsumer(player, e -> {
                player.getDb_data().clearTodayRecharge();
            });
            player.sendPlayerCurRecharge();
        }
        return true;
    }

    @GmFunction
    private boolean dailyFirstRechargeAddDays(String playerIdx, int days) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                for (int i = 0; i < days; i++) {
                    protocol.TargetSystemDB.DB_DailyFirstRecharge.Builder dailyFirstRechargeBuilder = entity.getDb_Builder().getSpecialInfoBuilder().getDailyFirstRechargeBuilder();
                    dailyFirstRechargeBuilder.setRechargeDays(DailyFirstRechargeManage.getInstance().increaseRechargeDays(dailyFirstRechargeBuilder.getRechargeDays()));
                    dailyFirstRechargeBuilder.setExploreTimes(DailyFirstRechargeManage.getInstance().increaseExploreTimes(dailyFirstRechargeBuilder.getExploreTimes(), dailyFirstRechargeBuilder.getRechargeDays()));
                }
            });
            entity.sendDailyFirstRechargeUpdate();
        }
        return true;
    }

    @GmFunction
    private boolean clearGrowthTrack(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                entity.getGrowthTrackBuilder().clear();
            });
        }
        return true;
    }

    @GmFunction
    private boolean closeFunction(String playerIdx, int functionNum) {
        return FunctionManager.getInstance().closeFunction(EnumFunction.forNumber(functionNum));
    }

    @GmFunction
    private boolean openFunction(String playerIdx, int functionNum) {
        return FunctionManager.getInstance().openFunction(EnumFunction.forNumber(functionNum));
    }

    @GmFunction
    private boolean advance(String playerIdx, int advanceId) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        DrawCardAdvancedObject advanceCfg = DrawCardAdvanced.getById(advanceId);
        if (advanceCfg == null) {
            return false;
        }
        if (player != null) {
            SyncExecuteFunction.executeConsumer(player, p -> {
                CommonAdvanceInfo.Builder advanceBuilder = player.getDb_data().getDrawCardBuilder().getCommonAdvanceInfoBuilder();
                advanceBuilder.clear();
                advanceBuilder.setAdvanceId(advanceId);

                // 清除保底
                player.getDb_data().getDrawCardBuilder().clearCommonMustDrawCount();
            });
        }
        return true;
    }

    @GmFunction
    private boolean addNewTitle(String playerIdx, int newTitleId) {
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM);
        Reward reward = Reward.newBuilder().setRewardType(RewardTypeEnum.RTE_NewTitleSystem).setId(newTitleId).setCount(1).build();
        RewardManager.getInstance().doReward(playerIdx, reward, reason, true);
        return true;
    }

    @GmFunction
    private boolean refreshAllPlayerAddition(String playerIdx) {
        for (BaseEntity entity : playerCache.getInstance()._ix_id.values()) {
            playerEntity player = ((playerEntity) entity);
            SyncExecuteFunction.executeConsumer(player, p -> {
                player.refreshAllPetPropertyAddition(false);
            });
        }

        return true;
    }

    @GmFunction
    private boolean clearNewTitle(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player != null) {
            SyncExecuteFunction.executeConsumer(player, e -> {
                player.getDb_data().clearNewTitle();
            });
        }
        return true;
    }

    /**
     * 减少所有头像框的有效时间 不操作没有过期时间的头像框
     *
     * @param playerIdx
     * @return
     */
    @GmFunction
    private boolean avatarBorderReduceTime(String playerIdx, int days) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player != null) {
            SyncExecuteFunction.executeConsumer(player, e -> {
                for (AvatarBorderInfo.Builder builder : player.getDb_data().getAvatarBordersBuilderList()) {
                    if (builder.getExpireTime() == -1) {
                        continue;
                    }
                    builder.setExpireTime(builder.getExpireTime() - TimeUtil.MS_IN_A_DAY * days);
                }
            });
        }
        return true;
    }

    @GmFunction
    private boolean newTitleReduceTime(String playerIdx, int days) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player != null) {
            SyncExecuteFunction.executeConsumer(player, e -> {
                for (NewTitleInfo.Builder builder : player.getDb_data().getNewTitleBuilder().getInfoBuilderList()) {
                    if (builder.getExpireStamp() == -1) {
                        continue;
                    }
                    builder.setExpireStamp(builder.getExpireStamp() - TimeUtil.MS_IN_A_DAY * days);
                }
            });
        }
        return true;
    }

    @GmFunction
    private boolean updateRank(String playerIdx, int rank) {
        RankingManager.getInstance().directUpdateRanking(EnumRankingType.forNumber(rank), RankingUtils.getRankingTypeDefaultName(EnumRankingType.forNumber(rank)));
        return true;
    }

    @GmFunction
    private boolean matchArenaScore(String playerIdx, int newScore) {
        matcharenaEntity entity = matcharenaCache.getInstance().getEntity(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                entity.getDbBuilder().getRankMatchArenaBuilder().setScore(newScore);
                entity.getDbBuilder().getRankMatchArenaBuilder().setDan(MatchArenaUtil.getScoreDan(newScore));
            });
            entity.updateScoreToRedis(newScore);
            MatchArenaUtil.updateRanking(playerIdx, newScore);
        }
        return true;
    }

    @GmFunction
    private boolean clearMatchLimit(String playerIdx) {
        jedis.hdel(RedisKey.MatchArenaPlayerMatchInfo.getBytes(StandardCharsets.UTF_8), playerIdx.getBytes(StandardCharsets.UTF_8));

        return true;
    }

    @GmFunction
    private boolean template(String playerIdx) {
        return true;
    }

    @GmFunction
    private boolean trainingOpen(String playerIdx, int guanka) {
        TrainingManager.getInstance().openTrain(playerIdx, guanka);
        return true;
    }

    @GmFunction
    private boolean trainingRefMap(String playerIdx, int guanka, int time) {
        TrainingManager.getInstance().resetTrain(playerIdx, guanka, time);
        return true;
    }


    @GmFunction
    private boolean trainingUnlock(String playerIdx, int mapId) {
        TrainingMapObject byMapid = TrainingMap.getByMapid(mapId);
        if (byMapid == null) {
            return false;
        }
        TrainingManager.getInstance().openTrain(playerIdx, byMapid);
        return true;
    }

    @GmFunction
    private boolean trainingJoin(String playerIdx, int mapId) {
        TrainingManager.getInstance().joinTrain(playerIdx, mapId);
        return true;
    }

    @GmFunction
    private boolean trainingGo(String playerIdx, int mapId, int pointId) {
        TrainingManager.getInstance().going(playerIdx, mapId, pointId, 0, false, false, 0);
        return true;
    }

    @GmFunction
    private boolean addTrainLuckyCard(String playerIdx, int cardId) {
        trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerIdx);
        if (cache == null) {
            return false;
        }
        TrainDBMap.Builder curMap = cache.getCurTrainMap();
        if (curMap == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(cache, entity -> TrainingManager.getInstance().addCard(curMap, cardId));
        return true;
    }

    @GmFunction
    private boolean trainingTest(String playerIdx) {

        boolean test = true;
        if (test) {

            GameConfig.getById(GameConst.CONFIG_ID).getMagicthron_rankall();
            return true;

//			List<Battle.BattlePetData> petDataList = teamCache.getInstance().buildBattlePetData(playerIdx, TeamNumEnum.TNE_Team_1, BattleSubTypeEnum.BSTE_magicthron);
//			if (GameUtil.collectionIsEmpty(petDataList)) {
//				return false;
//			}
//			// 检查玩家信息是否正确
//			Battle.PlayerBaseInfo.Builder playerInfo = BattleUtil.buildPlayerBattleBaseInfo(playerIdx);
//			if (playerInfo == null) {
//				return false;
//			}
//			playerInfo.setPower(teamCache.getInstance().getTeamFightAbility(playerIdx, TeamNumEnum.TNE_Team_1));
//			Battle.BattlePlayerInfo.Builder battlePlayerInfo = Battle.BattlePlayerInfo.newBuilder();
//			battlePlayerInfo.setCamp(0);
//			battlePlayerInfo.addAllPetList(petDataList);
//			battlePlayerInfo.setPlayerInfo(playerInfo);
//			battlePlayerInfo.setIsAuto(false);
//			
//			return true;
        }

        String roomId = "";
        int count = 2;

        String id1 = playerCache.getInstance().getIdxByName("得体之伊冯娜");
        String id2 = playerCache.getInstance().getIdxByName("诚恳の芬克");

        List<String> playerIds = new ArrayList<>();
        playerIds.add(id1);
        playerIds.add(id2);
        for (String playerId : playerIds) {
            boolean create = false;
//			if(count > 5) {
//				break;
//			}
            if (count % 2 == 0) {
                create = true;
            }
            petEntity entity = petCache.getByIdx(playerId);
            Pet pet = petCache.getInstance().getMaxAbilityPet(playerId);
            List<PositionPetMap> pets = new ArrayList<>();
            List<SkillMap> skills = new ArrayList<>();
            skills.add(SkillMap.newBuilder().setSkillCfgId(1).setSkillPosition(TeamSkillPositionEnum.TSPE_Position_1).build());
            pets.add(PositionPetMap.newBuilder().setPosition(TeamPetPositionEnum.TPPE_Position_1).setPetIdx(pet.getId()).build());
            if (create) {
                CrossArenaPvpRoom room = CrossArenaPvpManager.getInstance().addOne(playerId, -1, 100, new ArrayList<>(), "", pets, skills);
                if (room != null) {
                    roomId = room.getId();
                }
            } else {
                CrossArenaPvpManager.getInstance().join(playerId, roomId, 0, pets, skills);
            }
            CrossArenaPvpManager.getInstance().ready(playerId, roomId, 0);
            count++;
        }

        return true;
    }

    @GmFunction
    private boolean trainingclear(String playerIdx, int mapId) {
        TrainingManager.getInstance().clearTrain(playerIdx, mapId);

//		if (mapId == 1) {
//			MagicThronManager.getInstance().magicThronPanel(playerIdx);
//		} else if (mapId == 2) {
//			CS_EnterFight.Builder b = CS_EnterFight.newBuilder();
//			b.addParamList("0");
//			b.addParamList("1");
//			b.addParamList("0");
//			b.setSkipBattle(false);
//			b.setType(BattleSubTypeEnum.BSTE_magicthron);
//			BattleManager.getInstance().enterPveBattle(playerIdx, b.build());
//		}
        return true;
    }

    @GmFunction
    private boolean addTrainScore(String playerIdx, int score) {
        trainingEntity cache = trainingCache.getInstance().getCacheByPlayer(playerIdx);
        if (cache == null) {
            return false;
        }

        SyncExecuteFunction.executeConsumer(cache, entity -> {
            TrainDBMap.Builder curMap = entity.getCurTrainMap();
            if (curMap == null) {
                return;
            }
            curMap.setJifenRefTime(GlobalTick.getInstance().getCurrentTime());
            int newScore = curMap.getJifen() + score;
            curMap.setJifen(newScore);
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_TrainScore, newScore, curMap.getMapId());
        });
        return true;
    }

    @GmFunction
    private boolean feattime(String playerIdx, int time) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        SyncExecuteFunction.executeConsumer(entity, e -> {
            for (Entry<Integer, DB_Feats> ent : entity.getDb_Builder().getFeatsInfosMap().entrySet()) {
                protocol.TargetSystemDB.DB_Feats.Builder setResetTime = ent.getValue().toBuilder().setResetTime(currentTime + (time * 10));
                entity.getDb_Builder().putFeatsInfos(ent.getKey(), setResetTime.build());
            }
        });
        entity.sendFeats();
        entity.updateDailyData(true);
        return true;
    }

    @GmFunction
    private boolean matchArenaLeitaiOpen(String playerIdx) {
        MatchArenaLTManager.getInstance().openLT(playerIdx, true);
        return true;
    }

    @GmFunction
    private boolean gmChangeHourCycle(String playerIdx, int loop, int auctime) {
        FarmMineManager.getInstance().gmChangeHourCycle(loop, auctime);
        return true;
    }

    @GmFunction
    private boolean gmChangeGiveTime(String playerIdx, int n) {
        FarmMineManager.getInstance().gmAddGiveTime(playerIdx, n);
        return true;
    }

    @GmFunction
    private boolean gmAddRandomPlayerMine(String playerIdx, int n) {
        FarmMineManager.getInstance().gmAddRandomPlayerMine(n);
        return true;
    }

    @GmFunction
    private boolean mfwzDailyUpdate(String playerIdx) {
        magicthronCache.getInstance().updateDailyData();
        return true;
    }

    @GmFunction
    private boolean useProtectCard(String playerIdx) {
        CrossArenaManager.getInstance().useProtectCard(playerIdx);
        return true;
    }

    @GmFunction
    private boolean serverWeeklyUpdate(String playerIdx) {
        synchronized (this) {
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
        }
        return true;
    }

    @GmFunction
    private boolean refreshCrazyDuelTeams(String playerIdx) {
        magicthronCache.getInstance().updateDailyData();
        return true;
    }

    @GmFunction
    private boolean upLtWeekTask(String playerIdx, int type, int value) {
        playercrossarenaEntity entity = playercrossarenaCache.getByIdx(playerIdx);
        SyncExecuteFunction.executeConsumer(entity, e -> {
            CrossArenaManager.getInstance().updateWeekTask(e, type, CrossArenaUtil.DbChangeAdd, value);
        });
        return true;
    }

    @GmFunction
    private boolean quitLt(String playerIdx) {
        CrossArenaManager.getInstance().quitAll(playerIdx, true);
        return true;
    }

    @GmFunction
    private boolean stoneWorldMapUpdate(String playerIdx) {
        StoneRiftWorldMapManager.getInstance().dailyRefresh();
        return true;
    }

    @GmFunction
    private boolean stoneStudyAllScience(String playerIdx) {
        stoneriftEntity entity = stoneriftCache.getByIdx(playerIdx);
        if (entity == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(entity, ex -> {
            Map<Integer, Integer> skillLvMap = entity.getDB_Builder().getDbScience().getSkillLvMap();
            for (StoneRiftScienceObject cfg : StoneRiftScience._ix_id.values()) {
                Integer integer = skillLvMap.get(cfg.getId());
                int lv = integer == null ? 0 : integer;
                for (int i = lv; i < cfg.getMaxlevel(); i++) {
                    entity.studyScience(cfg);
                }
            }
        });
        return true;
    }

    @GmFunction
    private boolean addStoneRiftExp(String playerIdx, int exp) {
        stoneriftEntity entity = stoneriftCache.getByIdx(playerIdx);
        if (entity == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(entity, ex -> {
            entity.addStoneRiftExp(exp);
        });
        return true;
    }

    @GmFunction
    private boolean stoneSettleReward(String playerIdx) {
        stoneriftEntity entity = stoneriftCache.getByIdx(playerIdx);
        if (entity == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(entity, ex -> {
            entity.settleReward();
            for (DbStoneRiftFactory factory : entity.getDB_Builder().getFactoryMap().values()) {
                factory.setNextCanClaimTime(GlobalTick.getInstance().getCurrentTime());
            }
            entity.resetNextCanClaimTime();
        });
        return true;
    }

    @GmFunction
    private boolean gmCrossReset(String playerIdx) {
        CrossArenaManager.getInstance().gmReset();
        return true;
    }


    @GmFunction
    private boolean gmCrossArenaTest(String playerIdx, int n) {
        CrossArenaManager.getInstance().gmTest(playerIdx, n);
        return true;
    }

    @GmFunction
    private boolean gmCrossArenaTen(String playerIdx, int n) {
        if (n == 1) {
            CrossArenaManager.getInstance().openLT10(System.currentTimeMillis() + 3600000L, true);
        } else {
            CrossArenaManager.getInstance().closeLT10();
        }
        return true;
    }

    @GmFunction
    private boolean gmCrossArenaSetGradeLv(String playerIdx, int n) {
        CrossArenaManager.getInstance().gmSetGradeLv(playerIdx, n);
        return true;
    }

    @GmFunction
    private boolean gmCrossArenaUpdateDay(String playerIdx) {
        CrossArenaManager.getInstance().gmMailUpdateDay(playerIdx);
        return true;
    }

    @GmFunction
    private boolean gmCrossArenaAddGrade(String playerIdx, int n) {
        CrossArenaManager.getInstance().gmAddGrade(playerIdx, n);
        return true;
    }

    @GmFunction
    private boolean gmCrossArenaTopOpen(String playerIdx, int n) {
        CrossArenaTopManager.getInstance().openGM(n);
        return true;
    }

    @GmFunction
    private boolean gmCrossArenaClearMobai(String playerIdx) {
        CrossArenaHonorManager.getInstance().clearMobaiGM(playerIdx);
        return true;
    }

    @GmFunction
    private boolean gmOpenJTRQ(String playerIdx, int n) {
        NewForeignInvasionManager.getInstance().gmOpen(n);
        return true;
    }

    @GmFunction
    private boolean gongxun(String playerIdx, int type) {

        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (target == null || player == null) {
            return false;
        }
        DB_Feats db_Feats = target.getDb_Builder().getFeatsInfosMap().get(type);
        protocol.TargetSystemDB.DB_Feats.Builder builder = db_Feats.toBuilder().setFeatsType(1);
        target.getDb_Builder().putFeatsInfos(type, builder.build());
        return true;
    }

    @GmFunction
    private boolean itemcard(String playerIdx, int n) {
        ItemCardObject itemCardConfig = ItemCard.getById(n);
        if (itemCardConfig == null) {
            LogUtil.error("ItemCardHandler,itemCardConfig == null,playerIdx:{}", playerIdx);
            return false;
        }
        targetsystemEntity tarsystem = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (tarsystem == null) {
            LogUtil.error("ItemCardHandler,targetsystemEntity == null,playerIdx:{}", playerIdx);
            return false;
        }
        Map<Integer, DB_ItemCard> itemCardMap = tarsystem.getDb_Builder().getItemCardMap();
        if (itemCardMap.containsKey(n)) {
            LogUtil.error("ItemCardHandler,repeated buy,playerIdx:{}", playerIdx);
            return false;
        }

        SyncExecuteFunction.executeConsumer(tarsystem, entity -> {
            DB_ItemCard.Builder builder = DB_ItemCard.newBuilder();
            // 第一天自动领
            builder.setHave(itemCardConfig.getLimitday() - 1);
            builder.setToday(1);
            List<Reward> rwList = new ArrayList<>();
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ItemCard_day);
            List<Reward> rewardsByRewardId = RewardUtil.getRewardsByRewardId(itemCardConfig.getReward());
            if (rewardsByRewardId != null) {
                rwList.addAll(RewardUtil.getRewardsByRewardId(itemCardConfig.getReward()));
            }
            List<Reward> rewardsByRewardId2 = RewardUtil.getRewardsByRewardId(itemCardConfig.getBuyreward());
            if (rewardsByRewardId2 != null) {
                rwList.addAll(RewardUtil.getRewardsByRewardId(itemCardConfig.getBuyreward()));
            }
            RewardManager.getInstance().doRewardByList(playerIdx, rwList, reason, true);
            tarsystem.getDb_Builder().putItemCard(itemCardConfig.getId(), builder.build());
        });
        SC_RefreshItemCard.Builder builder = SC_RefreshItemCard.newBuilder();

        for (Entry<Integer, DB_ItemCard> ent : tarsystem.getDb_Builder().getItemCardMap().entrySet()) {
            ItemCardData.Builder b = ItemCardData.newBuilder();
//			b.setHave(ent.getValue().getHave());
            b.setIndex(ent.getKey());
            b.setToday(ent.getValue().getToday());
            b.setEndtime(TimeUtil.getNextDaysStamp(System.currentTimeMillis(), ent.getValue().getHave()));
            builder.addItemCard(b);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RefreshItemCard_VALUE, builder);
        return true;
    }

    @GmFunction
    private boolean clearAllOfferData(String playerIdx) {
        OfferRewardManager.getInstance().gmClearAllTask();
        return true;
    }

    @GmFunction
    private boolean clearStar(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.getDb_Builder().getSpecialInfoBuilder().clearStarTreasureActivity();
        });

        long count = itembagCache.getInstance().getPlayerItemCount(playerIdx, 63010);
        if (count > 0) {
            itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
            if (itemBag == null) {
                return false;
            }
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM);
            SyncExecuteFunction.executeFunction(itemBag, bagEntity -> itemBag.removeItem(63010, count, reason, true));
        }

        return true;
    }

    @GmFunction
    private boolean clearStarRedis(String playerIdx) {
        jedis.del(RedisKey.getStarTreasureRecordKey());
        return true;
    }

    @GmFunction
    private boolean petAvoidReset(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        entity.getDb_Builder().getSpecialInfoBuilder().getPetAvoidanceBuilder().setChallengedTimes(0);

        SC_PetAvoidanceUpdate.Builder refreshData = SC_PetAvoidanceUpdate.newBuilder();
        refreshData.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        refreshData.setTimes(entity.getDb_Builder().getSpecialInfo().getPetAvoidance().getChallengedTimes());
        ServerActivity activityByType = ActivityManager.getInstance().getActivityByType(ActivityTypeEnum.ATE_PetAvoidance);
        if (activityByType != null) {
            refreshData.setTimesLimit(activityByType.getPetAvoidance().getDailyChallengeTimes());
        }
        GlobalData.getInstance().sendMsg(playerIdx, SC_PetAvoidanceUpdate_VALUE, refreshData);

        LogUtil.info("重置 魔灵大躲避挑战次数为：" + refreshData.getTimes() + " timesLimit:" + refreshData.getTimesLimit());
        return true;
    }

    @GmFunction
    private boolean petAvoidScoreCheck(String playerIdx, int score, int gameTime) {
        ServerActivity activity = ActivityManager.getInstance().getActivityByType(ActivityTypeEnum.ATE_PetAvoidance);

        ScoreValidator scoreValidator = PetAvoidanceGameManager.getInstance().getScoreValidator();
        int durationTime = activity.getPetAvoidance().getDurationTime();
        int realScore = scoreValidator.makeRightScore(score, gameTime, durationTime);
        LogUtil.info("GMHandler.petAvoidScoreCheck success playerIdx={} gameTime={} maxTime={} cliScore={} finalScore={}",
                playerIdx, gameTime, durationTime, score, realScore);
        return true;
    }

    private static Object castToType(String param, Class<?> target) {
        if (param == null || target == null || target == String.class) {
            return param;
        }

        try {
            if (target == int.class || target == Integer.class) {
                return Integer.parseInt(param);
            } else if (target == long.class || target == Long.class) {
                return Long.parseLong(param);
            } else if (target == boolean.class || target == Boolean.class) {
                return Boolean.parseBoolean(param);
            }
        } catch (NumberFormatException e) {
            LogUtil.printStackTrace(e);
        }

        return null;
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}

/**
 * 标注该方法为Gm方法 方法名为参数名,
 *
 * @author huhan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface GmFunction {
}

