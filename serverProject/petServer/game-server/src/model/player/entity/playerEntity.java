/**
 * created by tool DAOGenerate
 */
package model.player.entity;

import cfg.AdsConfig;
import cfg.AdsConfigObject;
import cfg.ArenaDan;
import cfg.ArenaDanObject;
import cfg.ArtifactConfig;
import cfg.ArtifactConfigObject;
import cfg.ArtifactEnhancePointConfig;
import cfg.ArtifactEnhancePointConfigObject;
import cfg.ArtifactMapExpConfig;
import cfg.ArtifactStarConfig;
import cfg.ArtifactStarConfigObject;
import cfg.DrawCard;
import cfg.DrawCardAdvanced;
import cfg.DrawCardAdvancedObject;
import cfg.FunctionOpenLvConfig;
import cfg.GameConfig;
import cfg.GameConfigObject;
import cfg.Head;
import cfg.HeadBorder;
import cfg.HeadBorderObject;
import cfg.LinkConfig;
import cfg.LinkConfigObject;
import cfg.MailTemplateUsed;
import cfg.MainLineNode;
import cfg.MainLineNodeObject;
import cfg.MistLootPackCarryConfig;
import cfg.MistLootPackCarryConfigObject;
import cfg.MistMoveEffectConfig;
import cfg.MistMoveEffectConfigObject;
import cfg.MonsterDifficulty;
import cfg.MonsterDifficultyObject;
import cfg.MonthlyCardConfig;
import cfg.MonthlyCardConfigObject;
import cfg.NewTitleSytemConfig;
import cfg.NewTitleSytemConfigObject;
import cfg.PetBaseProperties;
import cfg.PetCollectExpCfg;
import cfg.PetCollectLvCfg;
import cfg.PlayerLevelConfig;
import cfg.PlayerLevelConfigObject;
import cfg.PrivilegedCardCfg;
import cfg.ResourceCopy;
import cfg.ResourceCopyObject;
import cfg.ResourceRecycleCfg;
import cfg.ServerStringRes;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import com.google.protobuf.ProtocolStringList;
import common.GameConst;
import common.GameConst.Ban;
import common.GameConst.ChatRetCode;
import common.GameConst.EventType;
import common.GameConst.ServerStringConst;
import common.GlobalData;
import common.HttpRequestUtil;
import common.IdGenerator;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.activity.petAvoidance.PetAvoidanceGameManager;
import model.arena.ArenaManager;
import model.arena.dbCache.arenaCache;
import model.barrage.BarrageManager;
import model.bravechallenge.dbCache.bravechallengeCache;
import model.consume.ConsumeUtil;
import model.cp.CpCopyManger;
import model.cp.CpTeamManger;
import model.cp.CpTeamMatchManger;
import model.crossarena.CrossArenaManager;
import model.crossarenapvp.CrossArenaPvpManager;
import model.drawCard.DrawCardManager;
import model.drawCard.DrawCardUtil;
import model.magicthron.MagicThronManager;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.mistforest.MistForestManager;
import model.obj.BaseObj;
import model.patrol.dbCache.patrolCache;
import model.patrol.entity.patrolEntity;
import model.pet.dbCache.petCache;
import model.pet.entity.FightPowerCalculate;
import model.pet.entity.petEntity;
import model.petfragment.dbCache.service.PetFragmentServiceImpl;
import model.player.dbCache.playerCache;
import model.player.playerConstant;
import model.player.util.FriendUtil;
import model.ranking.RankingManager;
import model.redpoint.RedPointManager;
import model.redpoint.RedPointOptionEnum;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.stoneRift.StoneRiftWorldMapManager;
import model.stoneRift.dbCache.stoneriftCache;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.timer.TimerConst.TimerIdx;
import model.timer.dbCache.timerCache;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import platform.PlatformManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.DailyDateLog;
import platform.logs.entity.LogOutLog;
import platform.logs.entity.MistPlayTimeLog;
import platform.logs.entity.OnLineTimeLog;
import platform.logs.entity.PlayerLvLog;
import platform.logs.entity.PlayerVipLog;
import platform.logs.statistics.ArtifactStatistics;
import protocol.Activity;
import protocol.Activity.EnumRankingType;
import protocol.Battle.PlayerBaseInfo;
import protocol.Battle.SkillBattleDict;
import protocol.Collection.SC_CollectionUpdate;
import protocol.Comment.CommentTypeEnum;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.Common.LanguageEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.Common.SC_RefreashCurrency;
import protocol.Common.SC_Tips;
import protocol.DrawCard.EnumDrawCardType;
import protocol.DrawCard.HighPoolReward;
import protocol.DrawCard.SC_ClaimDrawCardInfo;
import protocol.DrawCard.SC_RefreshCardExp;
import protocol.DrawCard.SelectedPetIndex;
import protocol.Friend.FriednStateEnum;
import protocol.Friend.SC_UpdateFriendState;
import protocol.LoginProto.ClientData;
import protocol.LoginProto.SC_KickOut;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.SC_AddCollectionExp_VALUE;
import protocol.PlayerInfo.MistMoveEffectInfo;
import protocol.PlayerInfo.SC_ShowMoveEffectInfo;
import protocol.RedPointIdEnum.RedPointId;
import static protocol.RedPointIdEnum.RedPointId.RP_HONORWALL_VALUE;

import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.GhostBusterRankData;
import protocol.MistForest.GhostBusterRecordData;
import protocol.MistForest.MazeBuyGoodsTimes;
import protocol.MistForest.SC_GhostBusterTotalScore;
import protocol.MistForest.SC_PlayerGhostBusterRecord;
import protocol.MistForest.SC_UpdateEliteMonsterRewardTimes;
import protocol.MistForest.SC_UpdateMistBaseInfo;
import protocol.MistForest.SC_UpdateMistCarryInfo;
import protocol.MistForest.SC_UpdateMistLootPackInfo;
import protocol.MistForest.SC_UpdateMistStamina;
import protocol.MistForest.SC_UpdateMistTicket;
import protocol.MonthCard;
import protocol.PetMessage;
import protocol.PlayerBase;
import protocol.PlayerDB;
import protocol.PlayerDB.DB_BanInfo;
import protocol.PlayerDB.DB_DrawCardCurOdds;
import protocol.PlayerDB.DB_DrawCardData;
import protocol.PlayerDB.DB_FriendInfo;
import protocol.PlayerDB.DB_GhostBusterData;
import protocol.PlayerDB.DB_HighCard;
import protocol.PlayerDB.DB_MonsterDifficultyInfo;
import protocol.PlayerDB.DB_OwnedFriendInfo;
import protocol.PlayerDB.DB_PlayerData;
import protocol.PlayerDB.DB_PlayerData.Builder;
import protocol.PlayerDB.DB_ResourceCopy;
import protocol.PlayerDB.DB_ResourceCopyDict;
import protocol.PlayerDB.DB_SelectedPet;
import protocol.PlayerDB.GlobalAddition;
import protocol.PlayerInfo;
import protocol.PlayerInfo.Artifact;
import protocol.PlayerInfo.ArtifactEnhancePoint;
import protocol.PlayerInfo.AvatarBorderInfo;
import protocol.PlayerInfo.DisplayPet;
import protocol.PlayerInfo.NewTitle;
import protocol.PlayerInfo.NewTitleInfo;
import protocol.PlayerInfo.PlayerSkill;
import protocol.PlayerInfo.SC_AddAvatar;
import protocol.PlayerInfo.SC_AddAvatarBorder;
import protocol.PlayerInfo.SC_AddNewTitle;
import protocol.PlayerInfo.SC_ArtifactUpdate;
import protocol.PlayerInfo.SC_GlobalAddition;
import protocol.PlayerInfo.SC_NewTitleExpire;
import protocol.PlayerInfo.SC_PlayerBaseInfo;
import protocol.PlayerInfo.SC_RefreshGoldExchangeInfo;
import protocol.PlayerInfo.SC_RefreshPlayerLv;
import protocol.PlayerInfo.SC_RefreshTodayReportTimes;
import protocol.PlayerInfo.SC_RefreshVipLv;
import protocol.PlayerInfo.SC_RemoveAvatarBorder;
import protocol.PlayerInfo.SC_TotalAdsInfo;
import protocol.PlayerInfo.SC_UpdatePushSetting;
import protocol.PlayerInfo.SC_UpdateSkill;
import protocol.PlayerInfo.SC_UpdateTitle;
import protocol.Recharge.SC_PlayerCurRecharge;
import protocol.ResourceCopy.ResCopy;
import protocol.ResourceCopy.ResourceCopyTypeEnum;
import protocol.ResourceCopy.SC_ClaimResCopy;
import protocol.ResourceCopy.SC_RefreshResCopy;
import protocol.ResourceRecycle;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_CS_ClearEliteMonsterRewardTimes;
import protocol.ServerTransfer.GS_CS_ReconnectMistForest;
import protocol.ServerTransfer.GS_CS_UpdateHolyWater;
import protocol.ServerTransfer.GS_CS_UpdateMistStamina;
import protocol.ServerTransfer.PlayerOffline;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TheWar.SC_UpdateBuyBackTimes;
import protocol.TheWar.SC_UpdateBuyStamiaTimes;
import protocol.TransServerCommon.PlayerMistServerInfo;
import server.event.Event;
import server.event.EventManager;
import server.handler.monthCard.MonthCardUtil;
import server.handler.resRecycle.ResourceRecycleHelper;
import util.CollectionUtil;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.MapUtil;
import util.TimeUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")

@Getter
@Setter
public class playerEntity extends BaseObj {

    public String getClassType() {
        return "playerEntity";
    }

    /**
     *
     */
    private String idx;

    /**
     *
     */
    private int shortid;

    /**
     *
     */
    private String userid;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private int avatar;

    /**
     *
     */
    private int level;

    /**
     *
     */
    private int experience;

    /**
     *
     */
    private int vip;

    /**
     *
     */
    private int vipexperience;

    /**
     *
     */
    private long gold;

    /**
     *
     */
    private int diamond;

    /**
     * 点券
     */
    private int coupon;

    /**
     * 月卡到期时间
     */
    private Date monthcardexpiretime;

    /**
     *
     */
    private Date createtime;

    /**
     *
     */
    private Date logintime;

    /**
     *
     */
    private Date logouttime;

    /**
     *
     */
    private Date updatetime;

    /**
     * DB_PlayerData
     */
    private byte[] playerdata;

    /**
     * 每日领奖记录
     */
    private volatile long dailyrewardrecord;

    /**
     * 一次性奖励记录
     */
    private volatile long oncerewardreward;

    /**
     * 性别 0女 1男
     */
    private int sex;

    /**
     * <activityId,<奖励id,当前领取次数>
     */
    private Map<Long, Map<Integer, Integer>> claimedMap = new ConcurrentHashMap<>();


    public String getBaseIdx() {
        return idx;
    }

    /**
     * ==========================================================
     */

    @Override
    public void putToCache() {
        playerCache.put(this);
    }

    public ClientData.Builder getClientData() {
        return clientData;
    }

    private ClientData.Builder clientData;

    private DB_PlayerData.Builder db_data;

    private long updateFriendHelpTime;

    private String ip;

    private int curChatState = ChatRetCode.LV_NOT_ENOUGH;

    /**
     * 上次进入战斗时间
     */
    private long lastEnterFightTime;

    private long lastEnterMistTime;

    public DB_PlayerData.Builder getDb_data() {
        if (db_data == null) {
            this.db_data = getDBPlayerData();
        }
        return db_data;
    }


    private DB_PlayerData.Builder getDBPlayerData() {
        if (this.playerdata != null) {
            synchronized (this) {
                if (this.db_data != null) {
                    return this.db_data;
                }
                Builder builder;
                try {
                    builder = DB_PlayerData.parseFrom(this.playerdata).toBuilder();
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                    return null;
                }
                initClaimMap(builder);
                return builder;
            }
        } else {
            return DB_PlayerData.newBuilder();
        }

    }

    private void initClaimMap(Builder builder) {
        claimedMap.clear();
        List<PlayerDB.DB_ClaimRewardEntry> claimEntryList = builder.getClaimEntryList();
        for (PlayerDB.DB_ClaimRewardEntry entry : claimEntryList) {
            Map<Integer, Integer> recordMap = new ConcurrentHashMap<>();
            protocol.Common.IntMap record = entry.getRecord();
            for (int i = 0; i < record.getKeysCount(); i++) {
                recordMap.put(record.getKeys(i), record.getValues(i));
            }
            claimedMap.put(entry.getActivityId(), recordMap);
        }
        builder.clearClaimEntry();
    }

    @Override
    public void transformDBData() {
        this.playerdata = getDb_data().build().toByteArray();
    }

    public playerEntity() {
        this.playerdata = DB_PlayerData.newBuilder().build().toByteArray();
//        this.battleController = new BattleController(this);
    }

//    public BattleController getBattleController() {
//        return battleController;
//    }
//
//    public void setBattleController(BattleController battleController) {
//        this.battleController = battleController;
//    }

    public boolean functionUnLock(EnumFunction function) {
        return getDb_data().getUnlockFunctionList().contains(function);
    }

    public List<EnumFunction> queryCanUnlockFunctionByKeyNode(int keyNode) {
        List<EnumFunction> functions = FunctionOpenLvConfig.queryCanUnlockFunctionByKeyNode(keyNode);
        functions.removeAll(getDb_data().getUnlockFunctionList());
        return functions;
    }

    public void unLockFunctionByMissionComplete(List<Integer> missionIds) {
        if (CollectionUtils.isEmpty(missionIds)) {
            return;
        }
        List<EnumFunction> functions = FunctionOpenLvConfig.queryCanUnlockFunctionByMission(missionIds);
        doUnlockFunctions(functions, true);
    }

    private void unlockFunctionPlayerLvUp(int curLv) {
        List<EnumFunction> functions = FunctionOpenLvConfig.queryCanUnlockFunctionByLvUp(curLv);
        doUnlockFunctions(functions, true);
    }

    public void doUnlockFunctions(List<EnumFunction> functions, boolean sendMsg) {
        if (CollectionUtils.isEmpty(functions)) {
            return;
        }
        //remove already unlock function
        functions.removeAll(getDb_data().getUnlockFunctionList());
        if (CollectionUtils.isEmpty(functions)) {
            return;
        }
        //用这个消息推新增功能是因为客户端需要
        for (EnumFunction function : functions) {
            unlockFunction(function);
        }
        if (sendMsg) {
            sendUnlockFunctionMsg(functions);
        }
    }

    private void sendUnlockFunctionMsg(List<EnumFunction> functions) {
        SC_RefreshPlayerLv.Builder msg = PlayerInfo.SC_RefreshPlayerLv.newBuilder().addAllAddFunctions(functions);
        msg.setNewExp(getExperience());
        msg.setNewLevel(getLevel());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_RefreshPlayerLv_VALUE, msg);
    }

    private void unlockFunction(EnumFunction function) {
        if (getDb_data().getUnlockFunctionList().contains(function)) {
            return;
        }
        getDb_data().addUnlockFunction(function);

        switch (function) {
            case CourageTrial:
                //勇气者试炼功能解锁时立即初始化难度
                bravechallengeCache.getInstance().initPoint(getIdx());
                break;
            case ResCopy:
                checkResCopy();
                break;
            case WishingWell:
                unLockWishWell();
                break;
            case AutoOnHook:
                unlockAutoOnHook();
                break;
            case StoneRift:
                unLockStoneRift();
                break;
            case EF_RankingEntrance:
                RedPointManager.getInstance().sendRedPoint(getIdx(), RP_HONORWALL_VALUE, RedPointOptionEnum.CHECK);
                break;
            default:
        }
        EventUtil.triggerUpdateTargetProgress(getIdx(), TargetTypeEnum.TEE_Function_Unlock, 1, function.getNumber());
    }

    private void unLockStoneRift() {
        stoneriftCache.getInstance().createNewEntity(getIdx());
    }

    private void unlockAutoOnHook() {
        mainlineEntity mainlineEntity = mainlineCache.getByIdx(getIdx());
        if (mainlineEntity == null) {
            return;
        }
        mainlineEntity.unlockAutoOnHook();
    }

    private void unLockWishWell() {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(getIdx());
        Event unlockWishWellEvent = Event.valueOf(EventType.ET_UnlockWishWell, GameUtil.getDefaultEventSource(), target);
        EventManager.getInstance().dispatchEvent(unlockWishWellEvent);
    }

    public void addExperience(int experience) {
        if (experience <= 0) {
            return;
        }

        int beforeAdd = getLevel();

        int level = getLevel();
        int exp = getExperience() + experience;
        while (true) {
            int needExp = PlayerLevelConfig.getInstance().getLvUpExp(level);
            if (exp < needExp || level == PlayerLevelConfig.maxLevel) {
                break;
            }
            exp -= needExp;
            level++;
        }
        setLevel(level);
        setExperience(exp);

        //触发等级提升解锁事件
        lvUp(beforeAdd, level);
        sendRefreshPlayerLvMsg();
    }

    /**
     * 等级提升
     *
     * @param beforeAdd 增加之前等级
     * @param curLv     增加之后等级
     */
    public void lvUp(int beforeAdd, int curLv) {
        if (curLv <= beforeAdd) {
            return;
        }
        EventUtil.triggerLevelUpEvent(getIdx(), beforeAdd, curLv);
        checkResCopy();
        LogService.getInstance().submit(new PlayerLvLog(this, beforeAdd));

        //解锁功能
        unlockFunctionPlayerLvUp(curLv);

        //更新聊天权限(在解锁功能之后执行)
        updateChatFunctionOpen();

        //更新活动排行榜
        RankingManager.getInstance().updatePlayerRankingScore(getIdx(), EnumRankingType.ERT_PlayerLevel, level, 0);

        //玩家appsflyer等级更新
        HttpRequestUtil.platformAppsflyerLevel(this);

        CrossArenaManager.getInstance().playerUPLv(getIdx());
    }


    public void initNewbiePlayer() {
        GameConfigObject gameCfg = GameConfig.getById(GameConst.CONFIG_ID);
        if (gameCfg != null) {
            setAvatar(gameCfg.getDefaultavatarid());
            Builder db_data = getDb_data();
            if (db_data != null) {
                addAvatar(Collections.singleton(gameCfg.getDefaultavatarid()));
            }
            setLevel(gameCfg.getDefaultlv());

            getDb_data().getMistForestDataBuilder().setStamina(gameCfg.getInitmiststamina());
            if (gameCfg.getInitmistcarrypack() != null && gameCfg.getInitmistcarrypack().length > 0) {
                for (int i = 0; i < gameCfg.getInitmistcarrypack().length; i++) {
                    int[] initPackData = gameCfg.getInitmistcarrypack()[i];
                    if (initPackData == null || initPackData.length < 2) {
                        continue;
                    }
                    getDb_data().getMistForestDataBuilder().putMistCarryRewards(initPackData[0], initPackData[1]);
                }
            }

            VIPConfigObject vipCfg = VIPConfig.getById(getVip());
            if (vipCfg != null) {
                getDb_data().getGhostBusterDataBuilder().setFreeTickets(vipCfg.getGhostbusterfreecount());
            }
            initDefaultArtifactAndSkills();

            //初始化金币兑换产出
            int defaultRate = getNodeGoldOutPutRate(gameCfg.getGoldexdefaultnode());
            if (defaultRate != -1) {
                getDb_data().getGoldExchangeBuilder().setOutputRate(defaultRate);
            }

            //初始化普通抽卡最高品质奖池
            DrawCardAdvancedObject drawCardAdvance = DrawCardAdvanced.randomAdvance();
            if (drawCardAdvance != null) {
                getDb_data().getDrawCardBuilder().getCommonAdvanceInfoBuilder().setAdvanceId(drawCardAdvance.getId());
            }

            addAvatarBorder(Collections.singleton(HeadBorder.getDefaultHeadBorder()));
            getDb_data().setCurAvatarBorder(HeadBorder.getDefaultHeadBorder());
        }
        setShortid(IdGenerator.getInstance().generateShortId());
        //设置抽卡默认选中优先使用卷
        getDb_data().getDrawCardBuilder().setUseItemFirst(true);

        updateDailyDataUpdateTime();
        updateWeeklyDataUpdateTime();

        updateAdsBonusData(false);

        putToCache();
        LogUtil.info("init newbie player finished id=" + getIdx() + ",userId=" + getUserid());
        //初始化默认功能(必须在player放入缓存后,不然其他entity在创建时找不到Player)
        initDefaultFunction();
        PetFragmentServiceImpl.getInstance().getFragmentByPlayer(getIdx());
    }

    private void initDefaultFunction() {
        List<Common.EnumFunction> functions = FunctionOpenLvConfig.queryDefaultFunction();
        doUnlockFunctions(functions, false);
    }

    public Artifact getArtifactById(int artifactId) {
        for (Artifact artifact : getDb_data().getArtifactList()) {
            if (artifact.getArtifactId() == artifactId) {
                return artifact;
            }
        }
        return null;
    }

    public int getArtifactEnhanceLv(int artifactId) {
        Artifact artifact = getArtifactById(artifactId);
        if (artifact == null) {
            return 0;
        }
        return artifact.getEnhancePointList().stream().mapToInt(ArtifactEnhancePoint::getPointLevel).min().orElse(1);
    }


    private void initDefaultArtifactAndSkills() {
        for (int[] artifact : GameConfig.getById(GameConst.CONFIG_ID).getDefaultaritifact()) {
            if (artifact.length > 1) {
                int artifactId = artifact[0];
                ArtifactConfigObject config = ArtifactConfig.getByKey(artifactId);
                if (config == null) {
                    return;
                }
                PlayerSkill.Builder skill = PlayerSkill.newBuilder().setSkillLv(artifact[1]).setSkillCfgId(config.getPlayerskillid());
                Artifact.Builder artifactBuilder = Artifact.newBuilder().setArtifactId(artifactId).setPlayerSkill(skill);
                getDb_data().addArtifact(artifactBuilder);

                //激活神器
                EventUtil.triggerUpdateTargetProgress(getIdx(), TargetTypeEnum.TEE_Artifact_Unlock, 1, artifactId);
                collectArtifactExp(artifactId, 0, 1);
                ArtifactStatistics.getInstance().addActive(artifactId);
                ArtifactStatistics.getInstance().addStarLv(artifactId, 1);
            }
        }
        refreshAllPetPropertyAddition(true);
        sendGlobalAdditionMsg();
    }

    /**
     * 重新计算群体加成及战力加成
     * 该处只计算神器相关加成
     */
    public void refreshAllPetPropertyAddition(boolean sendUpdate) {
        PlayerDB.GlobalAddition.Builder globalAddition = getDb_data().getGlobalAdditionBuilder();

        //神器部分
        Map<Integer, Integer> map = calculateArtifactAddition();
        globalAddition.clearArtifactAddition().putAllArtifactAddition(map);
        globalAddition.setArtifactAbilityAddition(calculateAbilityAddition(map));

//        getDb_data().putAllPetPropertyAddition(map);
//        getDb_data().setPetAbilityAddition(calculateAbilityAddition(map));
//        if (sendUpdate) {
//            sendGlobalAdditionMsg();
//        }
        calculateTotalGlobalAddition(sendUpdate);
    }

    /**
     * 此处计算所有的额外加成
     *
     * @param sendUpdate
     */
    public void calculateTotalGlobalAddition(boolean sendUpdate) {
        GlobalAddition globalAddition = getDb_data().getGlobalAddition();
        petEntity pet = petCache.getInstance().getEntityByPlayer(getIdx());
        Map<Integer, Integer> petCollectionAddition = pet == null ? Collections.emptyMap() :
                PetCollectLvCfg.getInstance().getAdditionMap(getDb_data().getCollectionBuilder().getCollectionLv());

        Map<Integer, Integer> mergeResult =
                CollectionUtil.mergeMap(Integer::sum, globalAddition.getArtifactAdditionMap(),
                        globalAddition.getNewTitleAdditionMap(),
                        petCollectionAddition);

        if (MapUtils.isEmpty(mergeResult)) {
            return;
        }

        getDb_data().clearPetPropertyAddition().putAllPetPropertyAddition(mergeResult);
        getDb_data().clearPetAbilityAddition().setPetAbilityAddition(calculateAbilityAddition(mergeResult));

        if (sendUpdate) {
            sendGlobalAdditionMsg();
        }
    }

    private Map<Integer, Integer> calculateArtifactAddition() {
        Map<Integer, Integer> map = new HashMap<>();
        //计算神器部分
        for (Artifact artifact : getDb_data().getArtifactList()) {
            ArtifactStarConfigObject starUpConfig = ArtifactStarConfig.getByArtifactIdAndStar(artifact.getArtifactId(), artifact.getPlayerSkill().getSkillLv());
            if (starUpConfig != null) {
                MapUtil.add2IntMapValue(map, starUpConfig.getCumuproperty());
            }
            ArtifactEnhancePoint enhancePoint = ArtifactConfig.getCurEnhancePoint(artifact);
            if (enhancePoint == null) {
                continue;
            }
            ArtifactEnhancePointConfigObject enhanceConfig = ArtifactEnhancePointConfig.getByPointAndLv(enhancePoint.getPointId(), enhancePoint.getPointLevel());
            if (enhanceConfig != null) {
                MapUtil.add2IntMapValue(map, enhanceConfig.getCumuproperty());
            }
        }
        return map;
    }

    public void sendGlobalAdditionMsg() {
        SC_GlobalAddition.Builder msg = SC_GlobalAddition.newBuilder();
        msg.setPetAbility(getDb_data().getPetAbilityAddition());
        msg.addAllPropertyKey(getDb_data().getPetPropertyAdditionMap().keySet());
        msg.addAllPropertyValue(getDb_data().getPetPropertyAdditionMap().values());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_GlobalAddition_VALUE, msg);
    }

    private long calculateAbilityAddition(Map<Integer, Integer> map) {
        FightPowerCalculate fightPowerCalculate = new FightPowerCalculate(map);
        return fightPowerCalculate.calculateAdditionAbility();
    }


    public boolean isOnline() {
        GameServerTcpChannel channel = GlobalData.getInstance().getOnlinePlayerChannel(idx);
        return channel != null && channel.channel.isActive();
    }

    public void onPlayerLogin(boolean isResume, boolean isNewPlayer) {
        LogUtil.info("player login id=" + getIdx() + ",userId=" + getUserid() + ",ip=" + getIp() + ",isResume=" + isResume);
        // TODO login logic
        long currentTime = GlobalTick.getInstance().getCurrentTime();

        saveRcentLoginDiff(currentTime);
        //累积登陆天数
        settleCumuLoginDays(currentTime);

        sendPlayerBaseInfo(isNewPlayer);
        sendGlobalAdditionMsg();
        setLogintime(new Date(currentTime));

        updateStatusToFriend(FriednStateEnum.FST_OnLine);

        //设置在线时长开始时间
        getDb_data().setLastSettleOnlineTime(currentTime);

        //更新聊天权限
        setCurChatState(chatOpenState());

        // 迷雾森林重连，须在baseinfo发送后处理
        PlayerMistServerInfo serverInfo = CrossServerManager.getInstance().getMistForestPlayerServerInfo(getIdx());
        if (serverInfo != null) {
            GS_CS_ReconnectMistForest.Builder builder = GS_CS_ReconnectMistForest.newBuilder();
            builder.setPlayerIdx(getIdx());
            if (!CrossServerManager.getInstance().sendMsgToMistForest(getIdx(), MsgIdEnum.GS_CS_ReconnectMistForest_VALUE, builder, true)) {
                if (lastEnterMistTime > 0 && serverInfo.getMistRule() == EnumMistRuleKind.EMRK_Common) {
                    setLastEnterMistTime(0);
                    int mistLevel = 0;
                    targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(getIdx());
                    if (targetEntity != null) {
                        mistLevel = targetEntity.getDb_Builder().getMistTaskData().getCurEnterLevel();
                    }
                    LogService.getInstance().submit(new MistPlayTimeLog(getIdx(), mistLevel, getDb_data().getMistForestData().getStamina(), false));
                }
                CrossServerManager.getInstance().removeMistForestPlayer(getIdx());
            }
        }
        if (!isResume) {
            PetAvoidanceGameManager.getInstance().onPlayerLogin(getIdx());
        }

    }

    private void saveRcentLoginDiff(long currentTime) {
        if (logintime == null) {
            return;
        }
        getDb_data().setRecentLoginDiff(currentTime - logintime.getTime());
    }

    private void settleCumuLoginDays(long currentTime) {
        int cumuLoginDays = getDb_data().getCumuLoginDays();
        if (cumuLoginDays == 0) {
            getDb_data().setCumuLoginDays(cumuLoginDays + 1);
            return;
        }
        if (logintime != null && !TimeUtil.isOneDay(currentTime, logintime.getTime())) {
            getDb_data().setCumuLoginDays(cumuLoginDays + 1);
        }
    }

    public void onPlayerLogout() {
        LogUtil.info("player logout id=" + getIdx() + ",userId=" + getUserid());
        // TODO logout logic

        leaveMistForest();
        leaveTheWar();
        updateOnlineTime();
        leavePatrol();
        leaveWatchBarrage();
        leaveCpTeamFunction();
        leaveStoneRiftWorldMap();

        CrossArenaManager.getInstance().onPlayerLogout(getIdx());
        CrossArenaPvpManager.getInstance().outRoom(getIdx());

        EventUtil.unlockObjEvent(EventType.ET_BATTLE_PLAYER_LEAVE, getIdx(), false);

        setLogouttime(new Date(GlobalTick.getInstance().getCurrentTime()));
        GlobalData.getInstance().removeOnlinePlayer(getIdx());

        HttpRequestUtil.antiLogOut(this);
        updateStatusToFriend(FriednStateEnum.FST_OffLine);

        LogService.getInstance().submit(new OnLineTimeLog(this));
        LogService.getInstance().submit(new LogOutLog(this));
        RedPointManager.getInstance().onPlayerLogout(getIdx());
    }

    private void leaveStoneRiftWorldMap() {
        StoneRiftWorldMapManager.getInstance().leaveRiftWorldMap(getIdx());
    }

    private void leaveCpTeamFunction() {
        CpTeamManger.getInstance().cancelMatchPlayer(getIdx());
        CpCopyManger.getInstance().logoutCpTeamCopy(getIdx());
        CpTeamMatchManger.getInstance().playerLeaveScene(getIdx());
    }

    private void leaveWatchBarrage() {
        BarrageManager.getInstance().leaveWatch(getIdx());
    }

    private void leavePatrol() {
        patrolEntity patrolEntity = patrolCache.getInstance().getCacheByPlayer(getIdx());
        if (patrolEntity != null) {
            patrolEntity.playerLeavePatrol();
        }
    }

    private void updateOnlineTime() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        int timeAfterLogin = (int) (currentTime - logintime.getTime()) / 1000;

        getDb_data().setCumuOnline(getDb_data().getCumuOnline() + timeAfterLogin);

        getDb_data().setTodayOnline(getDb_data().getTodayOnline() + getTodayThisLoginOnline());

    }

    public void leaveMistForest() {
        if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(getIdx()) > 0) {
            PlayerOffline.Builder builder = PlayerOffline.newBuilder();
            builder.setPlayerIdx(getIdx());
            CrossServerManager.getInstance().sendMsgToMistForest(
                    getIdx(), MsgIdEnum.PlayerOffline_VALUE, builder, false);
//            if (getBattleController().getBattleId() <= 0) {
//                CrossServerManager.getInstance().removeMistForestPlayer(getIdx());
//            }

        }
        if (lastEnterMistTime > 0) {
            setLastEnterMistTime(0);
            int mistLevel = 0;
            targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(getIdx());
            if (targetEntity != null) {
                mistLevel = targetEntity.getDb_Builder().getMistTaskData().getCurEnterLevel();
            }
            LogService.getInstance().submit(new MistPlayTimeLog(getIdx(), mistLevel, getDb_data().getMistForestData().getStamina(), false));
        }
    }

    public void settleMistCarryReward() {
        for (int i = EnumMistRuleKind.EMRK_Common_VALUE; i <= EnumMistRuleKind.EMRK_GhostBuster_VALUE; ++i) {
            settleMistCarryRewardByRule(i);
        }
    }

    public void settleMistCarryRewardByRule(int rule) {
        PlayerLevelConfigObject plyLvCfg = PlayerLevelConfig.getByLevel(getLevel());
        if (plyLvCfg == null) {
            return;
        }
        Map<Integer, Integer> readOnlyCarryRewardMap = null;
        Map<Integer, Integer> readOnlyDailyCarryRewardMap = new HashMap<>();
        Integer dailyGainCountObj;

        switch (rule) {
            case EnumMistRuleKind.EMRK_Common_VALUE: {
                readOnlyCarryRewardMap = getDb_data().getMistForestData().getMistCarryRewardsMap();
                readOnlyDailyCarryRewardMap.putAll(getDb_data().getMistForestData().getMistDailyGainRewardsMap());
                break;
            }
            case EnumMistRuleKind.EMRK_Maze_VALUE: {
                readOnlyCarryRewardMap = getDb_data().getMazeData().getMistMazeCarryRewardsMap();
                readOnlyDailyCarryRewardMap.putAll(getDb_data().getMazeData().getMistMazeDailyGainRewardsMap());
                break;
            }
            case EnumMistRuleKind.EMRK_GhostBuster_VALUE: {
                readOnlyCarryRewardMap = getDb_data().getGhostBusterData().getMistGhostCarryRewardsMap();
                readOnlyDailyCarryRewardMap.putAll(getDb_data().getGhostBusterData().getMistGhostDailyGainRewardsMap());
                break;
            }
            default:
                break;
        }
        if (readOnlyCarryRewardMap == null || readOnlyCarryRewardMap.isEmpty()) {
            return;
        }

        int realGainCount;
        int dailyGainCount;

        MistLootPackCarryConfigObject config;
        List<Reward> rewardList = null;
        Reward.Builder reward = Reward.newBuilder();
        Map<Integer, Integer> onlyUseInMistMap = null;
        for (Entry<Integer, Integer> entry : readOnlyCarryRewardMap.entrySet()) {
            config = MistLootPackCarryConfig.getById(entry.getKey());
            if (config == null) {
                continue;
            }
            if (config.getOnlyuseinmist()) {
                if (onlyUseInMistMap == null) {
                    onlyUseInMistMap = new HashMap<>();
                }
                onlyUseInMistMap.put(entry.getKey(), entry.getValue());
                continue;
            }

            realGainCount = entry.getValue();
            dailyGainCountObj = readOnlyDailyCarryRewardMap.get(entry.getKey());

            if (config.getId() == GameConst.FriendPointMistCfgId) {
                dailyGainCount = getDb_data().getFriendInfo().getTodayGainFriendItemCount();
                realGainCount = Math.min(FriendUtil.getFriendItemGainLimit(getIdx()) - dailyGainCount, realGainCount);
                dailyGainCount += realGainCount;
            } else {
                int limit = 0;
                if (config.getId() == GameConst.MistRefinedStoneCfgId) {
                    limit = plyLvCfg.getLimitByRule(EnumMistRuleKind.EMRK_Common_VALUE);
                } else {
                    limit = config.getLimitByRule(rule);
                }
                if (limit > 0) {
                    if (dailyGainCountObj != null) {
                        realGainCount = Math.min(limit - dailyGainCountObj, realGainCount);
                        dailyGainCount = dailyGainCountObj + realGainCount;
                    } else {
                        realGainCount = Math.min(limit, realGainCount);
                        dailyGainCount = realGainCount;
                    }
                } else {
                    dailyGainCount = 0;
                }
            }

            if (realGainCount <= 0) {
                continue;
            }

            reward.clear();
            reward.setRewardTypeValue(config.getRewardtype()).setId(config.getRewardid()).setCount(realGainCount);
            if (rewardList == null) {
                rewardList = new ArrayList<>();
            }
            rewardList.add(reward.build());

            if (dailyGainCount > 0) {
                readOnlyDailyCarryRewardMap.put(entry.getKey(), dailyGainCount);
                if (config.getId() == GameConst.FriendPointMistCfgId) {
                    getDb_data().getFriendInfoBuilder().setTodayGainFriendItemCount(dailyGainCount);
                }
            }
        }

        RewardSourceEnum resource = RewardSourceEnum.RSE_MistForest;
        switch (rule) {
            case EnumMistRuleKind.EMRK_Common_VALUE: {
                getDb_data().getMistForestDataBuilder().putAllMistDailyGainRewards(readOnlyDailyCarryRewardMap);
                getDb_data().getMistForestDataBuilder().clearMistCarryRewards();
                if (onlyUseInMistMap != null) {
                    getDb_data().getMistForestDataBuilder().putAllMistCarryRewards(onlyUseInMistMap);
                }
                break;
            }
            case EnumMistRuleKind.EMRK_Maze_VALUE: {
                getDb_data().getMazeDataBuilder().putAllMistMazeDailyGainRewards(readOnlyDailyCarryRewardMap);
                getDb_data().getMazeDataBuilder().clearMistMazeCarryRewards();
                if (onlyUseInMistMap != null) {
                    getDb_data().getMistForestDataBuilder().putAllMistCarryRewards(onlyUseInMistMap);
                }
                resource = RewardSourceEnum.RSE_MistMaze;
                break;
            }
            case EnumMistRuleKind.EMRK_GhostBuster_VALUE: {
                getDb_data().getGhostBusterDataBuilder().putAllMistGhostDailyGainRewards(readOnlyDailyCarryRewardMap);
                getDb_data().getGhostBusterDataBuilder().clearMistGhostCarryRewards();
                if (onlyUseInMistMap != null) {
                    getDb_data().getMistForestDataBuilder().putAllMistCarryRewards(onlyUseInMistMap);
                }
                resource = RewardSourceEnum.RSE_MistGhostBuster;
                break;
            }
            default:
                break;
        }
        sendMistCarryRewardInfoByRule(rule);
        sendMistDailyRewardInfoByRule(rule);
        if (CollectionUtils.isNotEmpty(rewardList)) {
            RewardManager.getInstance().doRewardByList(getIdx(), rewardList, ReasonManager.getInstance().borrowReason(resource), false);
        }
    }

    public void leaveTheWar() {
        if (!CrossServerManager.getInstance().isPlayerInTheWarNow(this)) {
            return;
        }
        PlayerOffline.Builder builder = PlayerOffline.newBuilder();
        builder.setPlayerIdx(getIdx());
        CrossServerManager.getInstance().sendMsgToWarRoom(getDb_data().getTheWarRoomIdx(), MsgIdEnum.PlayerOffline_VALUE, builder);

        CrossServerManager.getInstance().removeTheWarPlayer(this);
    }

    public void sendPlayerBaseInfo(boolean isNewPlayer) {
        SC_PlayerBaseInfo.Builder builder = SC_PlayerBaseInfo.newBuilder();
        builder.setPlayerId(getBaseIdx());
        builder.setShortId(getShortid());
        builder.setPlayerName(getName());
        builder.setAvatar(getAvatar());
        builder.setLevel(getLevel());
        builder.setExp(getExperience());
        builder.setVipLv(getVip());
        builder.setVipExp(getVipexperience());
        builder.setDiamond(getDiamond());
        builder.setGold(getGold());
        builder.setCoupon(getCoupon());
        builder.setHolyWater(getDb_data().getTheWarData().getHolyWater());
        builder.setNewPlayer(isNewPlayer);
        builder.setSex(this.sex);

        DB_PlayerData.Builder dbPlayerData = getDb_data();
        if (dbPlayerData != null) {
            builder.setFinishedWatchCG(dbPlayerData.getFinishedWatchCG());
            builder.addAllOwnedAvatar(dbPlayerData.getAvatarListList());

            Map<Integer, String> displayPetMap = dbPlayerData.getDisplayPetMap();
            for (Entry<Integer, String> entry : displayPetMap.entrySet()) {
                DisplayPet.Builder display = DisplayPet.newBuilder();
                display.setPosition(entry.getKey());
                display.setLinkPetIdx(entry.getValue());
                builder.addDisplayPet(display);
            }

            builder.setNextRenameTime(dbPlayerData.getNextRenameTime());
            builder.setTransferInfo(dbPlayerData.getAncientAltarBuilder().getPetTransferBuilder());
            //金币兑换
            builder.setGoldExchange(dbPlayerData.getGoldExchange());
            builder.addAllClaimedVipGift(dbPlayerData.getClaimedVipGiftList());

            builder.setCurAvatarBorder(dbPlayerData.getCurAvatarBorder());
            if (builder.getCurAvatarBorder() == playerConstant.AvatarBorderWithRank) {
                builder.setCurAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(getIdx()));
            }
            builder.addAllAvatarBorders(dbPlayerData.getAvatarBordersList());

            //玩家技能
            builder.addAllSkillList(getPlayerSkills());
            builder.addAllArtifact(dbPlayerData.getArtifactList());
            builder.setTodayReportTimes(dbPlayerData.getTodayReportTimes());
            builder.setPushOpen(dbPlayerData.getPushOpen());
            builder.setVipExpBuyTime(dbPlayerData.getVipExpBuyTime());
            builder.addAllFunctionUnlockAnimation(dbPlayerData.getFunctionUnlockAnimationList());

            //新称号系统
            builder.setNewTitle(dbPlayerData.getNewTitle());
        }

        builder.setTotalAbility(petCache.getInstance().totalAbility(getIdx()));
        builder.setTheWarMapName(getDb_data().getTheWarRoomIdx());
        builder.setTitleId(getTitleId());
        builder.setLastSettleWarTime(getDb_data().getTheWarData().getLastSettleTime());
        builder.addAllUnlockFunctions(getDb_data().getUnlockFunctionList());
        builder.addAllPrivilegedCards(getPrivilegedCardList());
        builder.setAutoFreePet(petCache.getInstance().playerSettingAutoFree(getIdx()));
        builder.setRarityReset(petCache.getInstance().playerRarityRest(getIdx()));
        builder.setCrossAreaScienceId(CrossArenaManager.getInstance().findPlayerMaxSceneId(idx));
        builder.setHonorLv(CrossArenaManager.getInstance().findPlayerGradeLv(idx));
        builder.setClosePlot(mainlineCache.getInstance().isClosePlot(getIdx()));
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_PlayerBaseInfo_VALUE, builder);
    }

    private List<Integer> getPrivilegedCardList() {
        return getDb_data().getRechargeCards().getPrivilegedCardList().stream().map(PlayerDB.DB_PrivilegedCard::getCarId).collect(Collectors.toList());
    }

    /**
     * 称号,暂无称号系统,以竞技场段位为称号
     *
     * @return
     */
    public int getTitleId() {
        int playerDan = arenaCache.getInstance().getPlayerDan(getIdx());
        ArenaDanObject arenaDanCfg = ArenaDan.getById(playerDan);
        return arenaDanCfg == null ? 0 : arenaDanCfg.getTitleid();
    }

    public List<PlayerSkill> getPlayerSkills() {
        if (CollectionUtils.isEmpty(db_data.getArtifactList())) {
            return Collections.emptyList();
        }
        return db_data.getArtifactList().stream().map(Artifact::getPlayerSkill).collect(Collectors.toList());
    }

    /**
     * 消耗钻石或者金币
     *
     * @param rewardEnum  RewardTypeEnum.RTE_Diamond or RewardTypeEnum.RTE_Gold
     * @param removeCount 消耗的数量
     */
    public boolean consumeCurrency(RewardTypeEnum rewardEnum, int removeCount, Reason reason) {
        if (rewardEnum == null || removeCount < 0) {
            LogUtil.warn("remove params count <= 0");
            return false;
        }

        if (!currencyIsEnough(rewardEnum, removeCount)) {
            LogUtil.info("Player[" + getIdx() + "] Currency is not enough, type=" + rewardEnum + ",count=" + getCurrencyCount(rewardEnum));
            return false;
        }

        long beforeAdd = getCurrencyCount(rewardEnum);
        if (rewardEnum == RewardTypeEnum.RTE_Diamond) {
            setDiamond(getDiamond() - removeCount);

            if (removeCount <= 0) {
                LogUtil.error("ConsumeDiamond removeCount is zero, reason=" + reason.getSourceEnum());
            }
            //目标：累积花费钻石
            EventUtil.triggerUpdateTargetProgress(getIdx(), TargetTypeEnum.TTE_CumuConsumeDiamond, removeCount, 0);
        } else if (rewardEnum == RewardTypeEnum.RTE_Gold) {
            setGold(getGold() - removeCount);
        } else if (rewardEnum == RewardTypeEnum.RTE_Coupon) {
            setCoupon(getCoupon() - removeCount);
            EventUtil.triggerUpdateTargetProgress(getIdx(), TargetTypeEnum.TTE_CumuConsumeCoupon, removeCount, 0);
            EventUtil.updateIncrRankingScore(getIdx(),EnumRankingType.ERT_ConsumeCoupon,removeCount);
        } else if (rewardEnum == RewardTypeEnum.RTE_HolyWater) {
            getDb_data().getTheWarDataBuilder().setHolyWater(getDb_data().getTheWarData().getHolyWater() - removeCount);
            if (!StringHelper.isNull(getDb_data().getTheWarRoomIdx())) {
                GS_CS_UpdateHolyWater.Builder toCSBuilder = GS_CS_UpdateHolyWater.newBuilder();
                toCSBuilder.setNewHolyWater(getDb_data().getTheWarData().getHolyWater());
                CrossServerManager.getInstance().sendMsgToWarRoom(getDb_data().getTheWarRoomIdx(), MsgIdEnum.GS_CS_UpdateHolyWater_VALUE, toCSBuilder);
            }
        } else {
            LogUtil.warn("unsupported currencyType");
        }

        sendCurrencyRefreshMsg(rewardEnum);
        LogService.getInstance().submit(new DailyDateLog(getIdx(), true, rewardEnum, 0, beforeAdd,
                removeCount, getCurrencyCount(rewardEnum), reason));
        return true;
    }

    public long getCurrencyCount(RewardTypeEnum type) {
        if (type == RewardTypeEnum.RTE_Gold) {
            return getGold();
        } else if (type == RewardTypeEnum.RTE_Diamond) {
            return getDiamond();
        } else if (type == RewardTypeEnum.RTE_Coupon) {
            return getCoupon();
        } else if (type == RewardTypeEnum.RTE_HolyWater) {
            return getDb_data().getTheWarData().getHolyWater();
        }
        return 0;
    }

    /**
     * 增加对应货币
     *
     * @param rewardType
     * @param addCount
     */
    public void addCurrency(RewardTypeEnum rewardType, int addCount, Reason reason) {
        if (rewardType == null || addCount <= 0) {
            return;
        }

        DB_PlayerData.Builder dbPlayerData = getDb_data();
        if (dbPlayerData == null) {
            LogUtil.error("playerIdx[" + getIdx() + "] DBData is null");
            return;
        }

        long beforeCount = 0;
        switch (rewardType) {
            case RTE_Gold:
                beforeCount = getGold();
                setGold(GameUtil.sumLong(getGold(), addCount));
                break;
            case RTE_Diamond:
                beforeCount = getDiamond();
                setDiamond(GameUtil.sumInt(getDiamond(), addCount));
                break;
            case RTE_Coupon:
                beforeCount = getCoupon();
                setCoupon(GameUtil.sumInt(getCoupon(), addCount));
                break;
            case RTE_HolyWater:
                beforeCount = getDb_data().getTheWarData().getHolyWater();
                int newCount = GameUtil.sumInt(getDb_data().getTheWarData().getHolyWater(), addCount);
                getDb_data().getTheWarDataBuilder().setHolyWater(newCount);
                if (!StringHelper.isNull(getDb_data().getTheWarRoomIdx())) {
                    GS_CS_UpdateHolyWater.Builder toCSBuilder = GS_CS_UpdateHolyWater.newBuilder();
                    toCSBuilder.setNewHolyWater(newCount);
                    CrossServerManager.getInstance().sendMsgToWarRoom(getDb_data().getTheWarRoomIdx(), MsgIdEnum.GS_CS_UpdateHolyWater_VALUE, toCSBuilder);
                }
                break;
            default:
                break;
        }
        sendCurrencyRefreshMsg(rewardType);
        LogService.getInstance().submit(new DailyDateLog(getIdx(), false, rewardType, 0, beforeCount,
                addCount, getCurrencyCount(rewardType), reason));
    }

    /**
     * 发送刷新货币(消息
     *
     * @param rewardType
     */
    public void sendCurrencyRefreshMsg(RewardTypeEnum rewardType) {
        if (rewardType == null) {
            return;
        }

        SC_RefreashCurrency.Builder refreshMsg = SC_RefreashCurrency.newBuilder();
        refreshMsg.setType(rewardType);
        switch (rewardType) {
            case RTE_Gold:
                refreshMsg.setNewCount(getGold());
                break;
            case RTE_Diamond:
                refreshMsg.setNewCount(getDiamond());
                break;
            case RTE_Coupon:
                refreshMsg.setNewCount(getCoupon());
                break;
            case RTE_HolyWater:
                refreshMsg.setNewCount(getDb_data().getTheWarData().getHolyWater());
                break;
            default:
                return;
        }
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_RefreashCurrency_VALUE, refreshMsg);
    }

    /**
     * 检查对应货币是否足够
     *
     * @param currencyType 货币类型
     * @param needCount    需要的数量
     * @return
     */
    public boolean currencyIsEnough(RewardTypeEnum currencyType, int needCount) {
        if (currencyType == null) {
            return false;
        }

        DB_PlayerData.Builder dbPlayerData = getDb_data();
        if (dbPlayerData == null) {
            LogUtil.error("playerIdx[" + getIdx() + "] dbData is null");
            return false;
        }

        return getCurrencyCount(currencyType) >= needCount;
    }

    public PlayerBaseInfo.Builder getBattleBaseData() {
        PlayerBaseInfo.Builder builder = PlayerBaseInfo.newBuilder();
        builder.setPlayerId(getIdx());
        builder.setPlayerName(getName());
        builder.setLevel(getLevel());
        builder.setAvatar(getAvatar());
        builder.setVipLevel(getVip());
        builder.setAvatarBorder(getDb_data().getCurAvatarBorder());
        if (builder.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            builder.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(getIdx()));
        }
        builder.setTitleId(getTitleId());
        builder.setNewTitleId(getCurEquipNewTitleId());
        builder.setHonorLv(CrossArenaManager.getInstance().findPlayerGradeLv(getIdx()));
        return builder;
    }

    public void onTick(long curTime) {
//        getBattleController().onTick(curTime);
        if (isOnline()) {
            recoverStamina(curTime);
        }
        removeExpireAvatarBorder();
        removeExpireNewTitle();
        removeExpireMistMoveEffect();
    }

    /**
     * * 更新玩家聊天功能开放情况
     */
    private void updateChatFunctionOpen() {
        int newState = chatOpenState();
        LogUtil.debug("model.player.entity.playerEntity.updateChatFunctionOpen, playerIdx:" + getIdx()
                + "new state:" + newState + "cur state:" + curChatState);
        if (newState != curChatState) {
            int beforeState = curChatState;
            curChatState = newState;
            if (!HttpRequestUtil.updatePlayerChatAuthority(this)) {
                LogUtil.error("playerEntity.updateChatFunctionOpen, update chat authority failed, playerIdx=" + getIdx());
                //更新失败将状态更新回原状态
                curChatState = beforeState;
            }
        }
    }

    /**
     * 经验值可以一直增长,不收最大经验值限制
     *
     * @param vipExp
     */
    public void addVipExp(int vipExp) {
        if (vipExp <= 0) {
            return;
        }

        int beforeAdd = getVip();
        int newVipExp = getVipexperience() + vipExp;
        setVipexperience(newVipExp);

        int maxExp = VIPConfig.getMaxExp();
        if (newVipExp >= maxExp) {
            setVip(VIPConfig.maxVipLv);
        } else {
            int vipLv = getVip();

            for (; vipLv < VIPConfig.maxVipLv; vipLv++) {
                if (newVipExp < VIPConfig.getVipExpByVipLv(vipLv + 1)) {
                    break;
                }
            }
            setVip(vipLv);
        }

        if (getVip() > beforeAdd) {
            LogService.getInstance().submit(new PlayerVipLog(this));
            Event event = Event.valueOf(EventType.ET_VipLvUp, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
            event.pushParam(getIdx(), beforeAdd, getVip());
            EventManager.getInstance().dispatchEvent(event);

            //增加普通抽卡特殊次数
            DB_DrawCardData.Builder drawCardBuilder = getDb_data().getDrawCardBuilder();
            for (int i = beforeAdd + 1; i <= getVip(); i++) {
                VIPConfigObject vipCfg = VIPConfig.getById(i);
                if (vipCfg == null) {
                    continue;
                }
                drawCardBuilder.setCommonRemainSpecialTimes(drawCardBuilder.getCommonRemainSpecialTimes() + vipCfg.getCommondrawspecialtimes());
            }
        }

        sendRefreshVipLvMsg();
    }

    private void sendRefreshVipLvMsg() {
        SC_RefreshVipLv.Builder builder = SC_RefreshVipLv.newBuilder();
        builder.setNewVipLv(getVip());
        builder.setNewVipExp(getVipexperience());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_RefreshVipLv_VALUE, builder);
    }

    /**
     * @param avatarCfgIdList
     * @return 返回重复添加的头像ID
     */
    public List<Integer> addAvatar(Collection<Integer> avatarCfgIdList) {

        List<Integer> repeatedAdd = new ArrayList<>();
        SC_AddAvatar.Builder addBuilder = SC_AddAvatar.newBuilder();

        Builder db_data = getDb_data();
        if (db_data == null) {
            LogUtil.info("playerIdx[" + getIdx() + "] dbData is null");
            return repeatedAdd;
        }

        for (Integer cfgId : avatarCfgIdList) {
            if (Head.getById(cfgId) == null) {
                continue;
            }

            if (!db_data.getAvatarListList().contains(cfgId)) {
                db_data.addAvatarList(cfgId);
                addBuilder.addAvatarId(cfgId);
            } else {
                repeatedAdd.add(cfgId);
            }
        }

        if (addBuilder.getAvatarIdCount() > 0) {
            GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_AddAvatar_VALUE, addBuilder);
        }

        return repeatedAdd;
    }

    /**
     * @param avatarBorderList
     */
    public void addAvatarBorder(Collection<Integer> avatarBorderList) {
        SC_AddAvatarBorder.Builder msgBuilder = SC_AddAvatarBorder.newBuilder();

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        for (Integer cfgId : avatarBorderList) {
            HeadBorderObject borderCfg = HeadBorder.getById(cfgId);
            if (borderCfg == null) {
                continue;
            }

            AvatarBorderInfo.Builder borderBuilder = null;
            boolean needInvokeAdd = false;
            for (AvatarBorderInfo.Builder avatarBorderInfo : getDb_data().getAvatarBordersBuilderList()) {
                if (avatarBorderInfo.getAvatarBorderId() == cfgId) {
                    borderBuilder = avatarBorderInfo;
                    break;
                }
            }
            if (borderBuilder == null) {
                borderBuilder = AvatarBorderInfo.newBuilder().setAvatarBorderId(cfgId);
                needInvokeAdd = true;
            }

            long newExpireTime = borderCfg.getExpiretime() == -1 ? -1 : currentTime + TimeUtil.MS_IN_A_DAY * borderCfg.getExpiretime();
            if (borderBuilder.getExpireTime() != newExpireTime) {
                borderBuilder.setExpireTime(newExpireTime);

                if (needInvokeAdd) {
                    getDb_data().addAvatarBorders(borderBuilder);
                }
                msgBuilder.addAvatarBorders(borderBuilder);
            }
        }

        sortAvatarBorders();

        if (msgBuilder.getAvatarBordersCount() > 0) {
            GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_AddAvatarBorder_VALUE, msgBuilder);
        }
    }

    public void sortAvatarBorders() {
        List<AvatarBorderInfo> sortList = new ArrayList<>(getDb_data().getAvatarBordersList());
        //将-1排序至数组最后
        sortList.sort((e1, e2) -> {
            if (e1.getExpireTime() == -1 && e2.getExpireTime() == -1) {
                return 0;
            }
            if (e1.getExpireTime() == -1) {
                return 1;
            }
            if (e2.getExpireTime() == -1) {
                return -1;
            }
            return Long.compare(e1.getExpireTime(), e2.getExpireTime());
        });

        getDb_data().clearAvatarBorders();
        getDb_data().addAllAvatarBorders(sortList);

//        LogUtil.info("model.player.entity.playerEntity.sortAvatarBorders, sort size:" + sortList.size()
//                + ", cur db size:" + getDb_data().getAvatarBordersCount());
    }

    /**
     * 线上数据的兼容处理
     */
    private boolean avatarBorderSorted = false;

    private void removeExpireAvatarBorder() {
        if (!isOnline() || getDb_data().getAvatarBordersCount() <= 0) {
            return;
        }

        //线上数据的兼容处理
        if (!avatarBorderSorted) {
            sortAvatarBorders();
            this.avatarBorderSorted = true;
        }

        AvatarBorderInfo firstAvatarBorderInfo = getDb_data().getAvatarBorders(0);
        if (firstAvatarBorderInfo.getExpireTime() != -1
                && firstAvatarBorderInfo.getExpireTime() < GlobalTick.getInstance().getCurrentTime()) {
            getDb_data().removeAvatarBorders(0);

            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Expire);
            EventUtil.triggerAddMailEvent(getIdx(), MailTemplateUsed.getById(GameConst.CONFIG_ID).getAvatarborderexpire(),
                    Collections.emptyList(), reason, HeadBorder.getName(firstAvatarBorderInfo.getAvatarBorderId(), getLanguage()));

            if (getDb_data().getCurAvatarBorder() == firstAvatarBorderInfo.getAvatarBorderId()) {
                getDb_data().setCurAvatarBorder(HeadBorder.getDefaultHeadBorder());
            }

            SC_RemoveAvatarBorder.Builder msgBuilder = SC_RemoveAvatarBorder.newBuilder();
            msgBuilder.addAvatarBorders(firstAvatarBorderInfo.getAvatarBorderId());
            msgBuilder.setCurAvatarBorder(getDb_data().getCurAvatarBorder());
            GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_RemoveAvatarBorder_VALUE, msgBuilder);
        }
    }

    public void updateDailyData(boolean sendMsg) {
        updateDailyDataUpdateTime();

        updateMistForestData(sendMsg);
        updateFriendData();
        updateExGoldData(sendMsg);
        updateResCopyData(sendMsg);
        updateMonthCardDataAndDoReward(sendMsg);
        updatePrivilegedCard(sendMsg);
        updateCallTimes();
        updateDrawTimes();
        updateAdsBonusData(sendMsg);
        updateDailyOnlineTime();
        updateReportData();
        updateVipExpBuyTIme();
        updateRecharge();
        updateTheWarData(sendMsg);
        updateClaimRewardRecord();
        clearDailyClaimRecord();
        getDb_data().clearMathArenaLeiTaiGuess();
        updateOfferReward();
    }


    private void clearDailyClaimRecord() {
        clearLocalActivityClaimRecord();
        clearPlatformActivityClaimRecord();

    }

    private void updateOfferReward() {
        getDb_data().getOfferRewardPrepareBuilder().setTodayFirst(0).setDayFight(0);
    }

    private void clearPlatformActivityClaimRecord() {
        for (Activity.ActivityTypeEnum activityTypeEnum : ActivityManager.getDailyRestPlatActivity()) {
            protocol.Server.ServerActivity activity = ActivityManager.getInstance().findOneRecentActivityByType(activityTypeEnum);
            if (activity != null) {
                claimedMap.remove(activity.getActivityId());
            }
        }
    }

    private void clearLocalActivityClaimRecord() {
        for (Integer localActivityId : ActivityUtil.LocalActivityId.getDailyResetActivityIds()) {
            claimedMap.remove(localActivityId.longValue());
        }
    }

    private void updateClaimRewardRecord() {
        dailyrewardrecord = 0L;
    }


    private void updateDailyDataUpdateTime() {
        long nextTriggerTime = timerCache.getInstance().getTimerNextTriggerTime(TimerIdx.TI_RESET_DAILY_DATE);
        getDb_data().setNextUpdateDailyDataTime(nextTriggerTime);
        LogUtil.debug("playerEntity.updateDailyDataUpdateTime, player:" + getIdx() + " next update time:" + nextTriggerTime);
    }

    public boolean needUpdateDailyData() {
        boolean needUpdate = GlobalTick.getInstance().getCurrentTime() >= getDb_data().getNextUpdateDailyDataTime();
        LogUtil.debug("playerEntity.needUpdateDailyData, player:" + getIdx() + " next update daily time:"
                + getDb_data().getNextUpdateDailyDataTime() + ", need update:" + needUpdate);
        return needUpdate;
    }

    private void updateRecharge() {
        getDb_data().clearTodayRecharge();
    }

    private void updateVipExpBuyTIme() {
        getDb_data().clearVipExpBuyTime();
    }

    private void updateReportData() {
        getDb_data().clearTodayReportTimes();
    }

    private void updateDailyOnlineTime() {
        getDb_data().clearTodayOnline();
    }

    private void updateDrawTimes() {
        getDb_data().getDrawCardBuilder().clearTodayDrawCount();
    }

    private void updateTheWarData(boolean sendMsg) {
        getDb_data().getTheWarDataBuilder().setDailyBuyBackCount(0);
        getDb_data().getTheWarDataBuilder().setDailyBuyStaminaCount(0);

        if (sendMsg) {
            sendTheWarBuyBackTimes();
            sendTheWarBuyStaminaTimes();
        }
    }


    /**
     * 清除召唤计数
     */
    private void updateCallTimes() {
        getDb_data().clearTodayCallTimes();
    }

    public void updateMonthCardDataAndDoReward(boolean sendMsg) {
        List<PlayerDB.DB_MonthCardInfo> monthCardListList = getDb_data().getRechargeCards().getMonthCardListList();
        if (CollectionUtils.isEmpty(monthCardListList)) {
            return;
        }
        List<PlayerDB.DB_MonthCardInfo> cardInfos = new ArrayList<>();
        monthCardListList.forEach(card -> {
            if (card.getRemainDays() - 1 > 0) {
                //更新剩余使用次数
                cardInfos.add(PlayerDB.DB_MonthCardInfo.newBuilder(card).setCarId(card.getCarId()).setRemainDays(card.getRemainDays() - 1).build());
            }
            if (card.getRemainDays() > 0) {
                LogUtil.debug("玩家[" + getIdx() + "]每日月卡奖励发放开始,cardId[" + card.getCarId() + "],remainDay:[" + (card.getRemainDays() - 1) + "]");
                //发送奖励
                MonthCardUtil.doMonthCardDailyReward(getIdx(), card.getCarId(), card.getRemainDays() - 1);
            }
        });
        getDb_data().getRechargeCardsBuilder().clearMonthCardList().addAllMonthCardList(cardInfos);

        if (sendMsg) {
            sendMonthCardUpdate();
        }
    }

    public void sendMonthCardUpdate() {
        MonthCard.SC_UpdateMonthCard.Builder msg = MonthCard.SC_UpdateMonthCard.newBuilder();
        List<PlayerDB.DB_MonthCardInfo> cardInfoInDb = getDb_data().getRechargeCards().getMonthCardListList();
        for (MonthlyCardConfigObject config : MonthlyCardConfig._ix_id.values()) {
            if (config.getId() <= 0) {
                continue;
            }
            Optional<PlayerDB.DB_MonthCardInfo> any = cardInfoInDb.stream().filter(cardInfo -> cardInfo.getCarId() == config.getId()).findAny();
            int remainDays = any.map(PlayerDB.DB_MonthCardInfo::getRemainDays).orElse(0);
            msg.addCarList(MonthCard.MonthCardItem.newBuilder().setCardId(config.getId()).setRemainDays(remainDays));
        }
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdateMonthCard_VALUE, msg);
    }

    public void updatePrivilegedCard(boolean sendMsg) {
        List<PlayerDB.DB_PrivilegedCard> dbRechargeCards = getDb_data().getRechargeCards().getPrivilegedCardList();
        if (CollectionUtils.isEmpty(dbRechargeCards)) {
            return;
        }
        List<PlayerDB.DB_PrivilegedCard> cardInfos = new ArrayList<>();
        dbRechargeCards.forEach(card -> {
            if (card.getRemainDays() - 1 > 0) {
                //更新剩余使用次数
                cardInfos.add(PlayerDB.DB_PrivilegedCard.newBuilder(card)
                        .setCarId(card.getCarId()).setRemainDays(card.getRemainDays() - 1).build());
            }
        });
        getDb_data().getRechargeCardsBuilder().clearPrivilegedCard().addAllPrivilegedCard(cardInfos);

        if (sendMsg) {
            sendRechargeCardUpdate();
        }
    }

    public void sendRechargeCardUpdate() {
        MonthCard.SC_UpdatePrivilegedCard.Builder msg = MonthCard.SC_UpdatePrivilegedCard.newBuilder();
        for (PlayerDB.DB_PrivilegedCard cardInfo : getDb_data().getRechargeCardsBuilder().getPrivilegedCardList()) {
            msg.addCard(MonthCard.PrivilegedCard.newBuilder().setCardId(cardInfo.getCarId()).setRemainDays(cardInfo.getRemainDays()));
        }
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdatePrivilegedCard_VALUE, msg);
    }

    public void updateAdsBonusData(boolean send) {
        AdsConfigObject cfgObj = AdsConfig.getById(GameConst.CONFIG_ID);
        if (cfgObj == null) {
            return;
        }
//        if (!targetsystemCache.getInstance().firstRechargeNotActive(getIdx())) {
//            return;
//        }
        getDb_data().getAdsBonusDataBuilder().setFreeGiftTimes(cfgObj.getFreeadsgifttimes());

        getDb_data().getAdsBonusDataBuilder().setFreeWheelBonusTimes(cfgObj.getWheelbonustimes());
        getDb_data().getAdsBonusDataBuilder().setRemainWatchBonusTimes(cfgObj.getWatchwheeladstimes());
        if (send) {
            sendTotalAdsInfo();
        }
    }

    public void updateWeeklyData(boolean sendMsg) {
        updateWeeklyDataUpdateTime();
    }

    public boolean needUpdateWeeklyData() {
        boolean needUpdate = GlobalTick.getInstance().getCurrentTime() >= getDb_data().getNextUpdateWeeklyDataTime();
        LogUtil.debug("playerEntity.needUpdateWeeklyData, player:" + getIdx() + " next update weekly time:"
                + getDb_data().getNextUpdateDailyDataTime() + ", need update:" + needUpdate);
        return needUpdate;
    }

    private void updateWeeklyDataUpdateTime() {
        long nextTriggerTime = timerCache.getInstance().getTimerNextTriggerTime(TimerIdx.TI_RESET_WEEK_DATE);
        getDb_data().setNextUpdateWeeklyDataTime(nextTriggerTime);
        LogUtil.debug("playerEntity.updateWeeklyDataUpdateTime, playerIdx:" + getIdx() + ", next update weekly time:" + nextTriggerTime);
    }


    private void updateResCopyData(boolean sendMsg) {
        Builder dbBuilder = getDb_data();
        for (DB_ResourceCopy.Builder dbResourceCopy : dbBuilder.getResCopyDataBuilder().getResourceCopyDataBuilderList()) {
            dbResourceCopy.clearBuyTimes().clearChallengeTimes();
        }

        if (sendMsg) {
            List<ResCopy> copies = buildResCopies();
            if (CollectionUtils.isEmpty(copies)) {
                return;
            }

            SC_ClaimResCopy.Builder builder = SC_ClaimResCopy.newBuilder();
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            builder.addAllResCopyData(copies);
            GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_ClaimResCopy_VALUE, builder);
        }
    }

    /**
     * 清除玩家金币兑换次数
     */
    private void updateExGoldData(boolean sendMsg) {
        DB_PlayerData.Builder db_dataBuilder = getDb_data();
        if (db_dataBuilder == null) {
            return;
        }
        db_dataBuilder.getGoldExchangeBuilder().clearGoldExTimes();
        //重置玩家当日收益
        int newOutput = getNodeGoldOutPutRate(mainlineCache.getInstance().getCurOnHookNode(getIdx()));
        if (newOutput == -1) {
            LogUtil.error("playerEntity.updateExGoldData, init player new gold output failed, playerId:" + getIdx());
        } else if (newOutput > db_dataBuilder.getGoldExchange().getOutputRate()) {
            db_dataBuilder.getGoldExchangeBuilder().setOutputRate(newOutput);
        }

        if (sendMsg) {
            refreshGoldExchangeInfo();
        }
    }

    public void refreshGoldExchangeInfo() {
        SC_RefreshGoldExchangeInfo.Builder builder = SC_RefreshGoldExchangeInfo.newBuilder();
        builder.setNewInfo(getDb_data().getGoldExchange());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_RefreshGoldExchangeInfo_VALUE, builder);
    }

    private int getNodeGoldOutPutRate(int nodeId) {
        MainLineNodeObject nodeCfg = MainLineNode.getById(nodeId);
        if (nodeCfg != null) {
            int[][] output = nodeCfg.getOnhookresourceoutput();
            for (int[] ints : output) {
                if (ints.length < 3) {
                    continue;
                }

                if (ints[0] == RewardTypeEnum.RTE_Gold_VALUE) {
                    return ints[2];
                }
            }
        }
        return -1;
    }

    /**
     * 更新每日好友数据
     */
    private void updateFriendData() {
        DB_PlayerData.Builder db_dataBuilder = getDb_data();
        if (db_dataBuilder == null) {
            return;
        }

        DB_FriendInfo.Builder friendInfoBuilder = db_dataBuilder.getFriendInfoBuilder();
        //清空当日获取数量
        friendInfoBuilder.clearTodayGainFriendItemCount();
        //清除昨天送友情点记录
        friendInfoBuilder.clearSendFriendshipPoint();
        //移除昨日收点记录
        Map<String, Boolean> recvPointMap = friendInfoBuilder.getRecvPointMap();
        if (recvPointMap == null || recvPointMap.size() <= 0) {
            return;
        }

        List<String> removeStr = new ArrayList<>();
        for (Entry<String, Boolean> entry : recvPointMap.entrySet()) {
            //删除已经领取的友情点
            if (Boolean.FALSE.equals(entry.getValue())) {
                removeStr.add(entry.getKey());
            }
        }

        if (!removeStr.isEmpty()) {
            for (String removeKey : removeStr) {
                friendInfoBuilder.removeRecvPoint(removeKey);
            }
        }
    }

    public void updateMistForestData(boolean sendMsg) {
        getDb_data().getMistForestDataBuilder().clearMistDailyGainRewards();
        getDb_data().getMistForestDataBuilder().clearDailyButStaminaTimes();
        getDb_data().getMistForestDataBuilder().clearEliteMonsterRewardTimes();
        getDb_data().getGhostBusterDataBuilder().clearMistGhostDailyGainRewards();

        VIPConfigObject vipConfig = VIPConfig.getById(getVip());
        int freshNum = vipConfig == null ? 0 : vipConfig.getGhostbusterfreecount();
        int oldFreeTicket = getDb_data().getGhostBusterDataBuilder().getFreeTickets();
        getDb_data().getGhostBusterDataBuilder().setFreeTickets(Integer.max(oldFreeTicket, freshNum));
        if (sendMsg) {
            sendMistDailyRewardInfoByRule(EnumMistRuleKind.EMRK_Common_VALUE);
            sendMistDailyRewardInfoByRule(EnumMistRuleKind.EMRK_GhostBuster_VALUE);
            sendMistFreeTickets();
        }

        if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(getIdx()) > 0) {
            GS_CS_ClearEliteMonsterRewardTimes.Builder builder = GS_CS_ClearEliteMonsterRewardTimes.newBuilder();
            builder.setPlayerIdx(getIdx());
            CrossServerManager.getInstance().sendMsgToMistForest(getIdx(), MsgIdEnum.GS_CS_ClearEliteMonsterRewardTimes_VALUE, builder, true);

            if (isOnline()) {
                SC_UpdateEliteMonsterRewardTimes.Builder builder1 = SC_UpdateEliteMonsterRewardTimes.newBuilder();
                builder1.setRewardTimes(GameConfig.getById(GameConst.CONFIG_ID).getDailyelitemonsterrewradtimes());
                GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdateEliteMonsterRewardTimes_VALUE, builder1);
            }
        }

        //迷雾深林宝箱不消失
//        itembagEntity bag = itembagCache.getInstance().getItemBagByPlayerIdx(getIdx());
//        if (bag != null) {
//            Event event = Event.valueOf(GameConst.EventType.ET_ClearMistItem, this, bag);
//            EventManager.getInstance().dispatchEvent(event);
//        }
    }

    public void sendMistBaseData() {
//        if (!isOnline()) {
//            return;
//        }
        SC_UpdateMistBaseInfo.Builder updateBuilder = SC_UpdateMistBaseInfo.newBuilder();
        updateBuilder.setStamina(getDb_data().getMistForestData().getStamina());
        updateBuilder.setDailyBuyStaminaTimes(getDb_data().getMistForestData().getDailyButStaminaTimes());
        updateBuilder.addAllMistItemData(getDb_data().getMistForestData().getMistItemDataList());
        updateBuilder.setSeasonStartTime(MistForestManager.getInstance().getSeasonStartTime());
        updateBuilder.setSeasonEndTime(MistForestManager.getInstance().getSeasonEndTime());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdateMistBaseInfo_VALUE, updateBuilder);
    }

    public void sendMistCarryRewardInfoByRule(int mistRule) {
        SC_UpdateMistLootPackInfo.Builder builder = SC_UpdateMistLootPackInfo.newBuilder();
        builder.setRuleValue(mistRule);
        builder.setFullUpdate(true);
        switch (mistRule) {
            case EnumMistRuleKind.EMRK_Common_VALUE: {
                for (Entry<Integer, Integer> entry : getDb_data().getMistForestDataBuilder().getMistCarryRewardsMap().entrySet()) {
                    builder.getItemDictBuilder().addCarryRewardId(entry.getKey()).addCount(entry.getValue());
                }
                break;
            }
            case EnumMistRuleKind.EMRK_Maze_VALUE: {
                for (Entry<Integer, Integer> entry : getDb_data().getMazeDataBuilder().getMistMazeCarryRewardsMap().entrySet()) {
                    builder.getItemDictBuilder().addCarryRewardId(entry.getKey()).addCount(entry.getValue());
                }
                break;
            }
            case EnumMistRuleKind.EMRK_GhostBuster_VALUE: {
                for (Entry<Integer, Integer> entry : getDb_data().getGhostBusterDataBuilder().getMistGhostCarryRewardsMap().entrySet()) {
                    builder.getItemDictBuilder().addCarryRewardId(entry.getKey()).addCount(entry.getValue());
                }
                break;
            }
        }
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdateMistLootPackInfo_VALUE, builder);
    }

    public void sendMistDailyRewardInfoByRule(int mistRule) {
        SC_UpdateMistCarryInfo.Builder builder = SC_UpdateMistCarryInfo.newBuilder();
        builder.setRuleValue(mistRule);
        builder.setFullUpdate(true);
        Integer dailyCountObj = 0;
        for (MistLootPackCarryConfigObject cfg : MistLootPackCarryConfig._ix_id.values()) {
            int limit = cfg.getLimitByRule(mistRule);
            if (limit == 0) {
                continue;
            }
            if (limit < 0) {
                PlayerLevelConfigObject plyLvCfg = PlayerLevelConfig.getByLevel(getLevel());
                if (plyLvCfg == null) {
                    limit = 0;
                } else {
                    limit = plyLvCfg.getLimitByRule(mistRule);
                }
            }
            switch (mistRule) {
                case EnumMistRuleKind.EMRK_Common_VALUE: {
                    dailyCountObj = getDb_data().getMistForestDataBuilder().getMistDailyGainRewardsMap().get(cfg.getId());
                    break;
                }
                case EnumMistRuleKind.EMRK_Maze_VALUE: {
                    dailyCountObj = getDb_data().getMazeDataBuilder().getMistMazeDailyGainRewardsMap().get(cfg.getId());
                    break;
                }
                case EnumMistRuleKind.EMRK_GhostBuster_VALUE: {
                    dailyCountObj = getDb_data().getGhostBusterDataBuilder().getMistGhostDailyGainRewardsMap().get(cfg.getId());
                    break;
                }
            }
            int dailyCount = dailyCountObj != null ? dailyCountObj : 0 + getMistCarryRewardCount(mistRule, cfg.getId());
            builder.getCarryInfoDictBuilder().addCarryRewardId(cfg.getId()).addCarryCount(dailyCount).addCarryLimit(limit);
        }
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdateMistCarryInfo_VALUE, builder);
    }

    public int getMistCarryRewardCount(int mistRule, int rewardId) {
        Map<Integer, Integer> readMap = null;
        switch (mistRule) {
            case EnumMistRuleKind.EMRK_Common_VALUE: {
                readMap = getDb_data().getMistForestDataBuilder().getMistCarryRewardsMap();
                break;
            }
            case EnumMistRuleKind.EMRK_Maze_VALUE: {
                readMap = getDb_data().getMazeDataBuilder().getMistMazeCarryRewardsMap();
                break;
            }
            case EnumMistRuleKind.EMRK_GhostBuster_VALUE: {
                readMap = getDb_data().getGhostBusterDataBuilder().getMistGhostCarryRewardsMap();
                break;
            }
            default:
                break;
        }
        Integer countObj = readMap.get(rewardId);
        return countObj != null ? countObj : 0;
    }

    public int getMistDailyRewardCount(int mistRule, int rewardId) {
        Map<Integer, Integer> readMap = null;
        switch (mistRule) {
            case EnumMistRuleKind.EMRK_Common_VALUE: {
                readMap = getDb_data().getMistForestDataBuilder().getMistDailyGainRewardsMap();
                break;
            }
            case EnumMistRuleKind.EMRK_Maze_VALUE: {
                readMap = getDb_data().getMazeDataBuilder().getMistMazeDailyGainRewardsMap();
                break;
            }
            case EnumMistRuleKind.EMRK_GhostBuster_VALUE: {
                readMap = getDb_data().getGhostBusterDataBuilder().getMistGhostDailyGainRewardsMap();
                break;
            }
            default:
                break;
        }
        Integer countObj = readMap.get(rewardId);
        return countObj != null ? countObj : 0;
    }

    public int getMistDailyConfigLimit(int mistRule, int rewardId) {
        MistLootPackCarryConfigObject config = MistLootPackCarryConfig.getById(rewardId);
        if (config == null) {
            return 0;
        }
        int limit = config.getLimitByRule(mistRule);
        if (limit < 0) {
            PlayerLevelConfigObject plyLvCfg = PlayerLevelConfig.getByLevel(getLevel());
            if (plyLvCfg == null) {
                return 0;
            }
            limit = plyLvCfg.getLimitByRule(mistRule);
        }
        return limit;
    }

    public void addMistMoveEffect(List<Integer> cfgIdList) {
        if (CollectionUtils.isEmpty(cfgIdList)) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        SC_ShowMoveEffectInfo.Builder builder = SC_ShowMoveEffectInfo.newBuilder();
        for (Integer cfgId : cfgIdList) {
            MistMoveEffectConfigObject cfg = MistMoveEffectConfig.getById(cfgId);
            if (cfg == null) {
                LogUtil.error("player[{}] add MistMoveEffect cfg error,cfgid={}", getIdx(), cfgId);
                continue;
            }
            MistMoveEffectInfo.Builder newEffect = null;
            if (getDb_data().getMistForestData().getMoveEffectInfoList().contains(cfgId)) {
                if (cfg.getExpiretime() <= 0) {
                    continue;
                }
                // 有过期时间的,更新过期时间
                for (MistMoveEffectInfo.Builder effect : getDb_data().getMistForestDataBuilder().getMoveEffectInfoBuilderList()) {
                    if (effect.getMoveEffectId() == cfgId) {
                        effect.setExpireTime(curTime + cfg.getExpiretime() * TimeUtil.MS_IN_A_HOUR);
                        newEffect = effect;
                        break;
                    }
                }
            }
            if (newEffect == null) {
                newEffect = MistMoveEffectInfo.newBuilder();
                newEffect.setMoveEffectId(cfgId);
                if (cfg.getExpiretime() > 0) {
                    newEffect.setExpireTime(curTime + cfg.getExpiretime() * TimeUtil.MS_IN_A_HOUR);
                }
                getDb_data().getMistForestDataBuilder().addMoveEffectInfo(newEffect);
            }
            builder.addMoveEffectInfo(newEffect);
        }
        if (builder.getMoveEffectInfoCount() > 0) {
            GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_ShowMoveEffectInfo_VALUE, builder);
        }
    }

    protected void removeExpireMistMoveEffect() {
        if (!isOnline()) {
            return;
        }
        List<Integer> removeList = null;
        long curTime = GlobalTick.getInstance().getCurrentTime();
        for (int i = 0; i < getDb_data().getMistForestDataBuilder().getMoveEffectInfoCount(); i++) {
            MistMoveEffectInfo.Builder effect = getDb_data().getMistForestDataBuilder().getMoveEffectInfoBuilder(i);
            if (effect.getExpireTime() <= 0) {
                continue;
            }
            if (effect.getExpireTime() <= curTime) {
                if (removeList == null) {
                    removeList = new ArrayList<>();
                }
                removeList.add(i);
                if (effect.getMoveEffectId() == getDb_data().getMistForestData().getCurMistEffectId()) {
                    getDb_data().getMistForestDataBuilder().setCurMistEffectId(0);
                }
            }
        }
        if (!CollectionUtils.isEmpty(removeList)) {
            for (Integer removeIndex : removeList) {
                getDb_data().getMistForestDataBuilder().removeMoveEffectInfo(removeIndex);
            }
            showAllMoveEffect(curTime);
        }
    }

    public void showAllMoveEffect(long curTime) {
        SC_ShowMoveEffectInfo.Builder builder = SC_ShowMoveEffectInfo.newBuilder();
        builder.setShowAll(true);
        builder.setCurMoveEffectId(getDb_data().getMistForestData().getCurMistEffectId());
        for (MistMoveEffectInfo effect : getDb_data().getMistForestData().getMoveEffectInfoList()) {
            if (effect.getExpireTime() <= 0 || effect.getExpireTime() > curTime) {
                builder.addMoveEffectInfo(effect);
            }
        }
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_ShowMoveEffectInfo_VALUE, builder);
    }

    public void addMistStamina(int addCount, boolean beyondLimit) {
        int newVal;
        int oldVal = getDb_data().getMistForestDataBuilder().getStamina();
        if (beyondLimit) {
            newVal = oldVal + addCount;
        } else {
            int limit = GameConfig.getById(GameConst.CONFIG_ID).getMiststaminamaxnum();
            newVal = Integer.min(oldVal + addCount, limit);
        }
        getDb_data().getMistForestDataBuilder().setStamina(newVal);
        if (oldVal != newVal) {
            updateMistStanima();
        }
    }

    public boolean removeMistStamina(int removeCount) {
        int stamina;
        int oldVal = getDb_data().getMistForestDataBuilder().getStamina();
        if (oldVal < removeCount) {
            removeCount = oldVal;
        }
        stamina = oldVal - removeCount;
        getDb_data().getMistForestDataBuilder().setStamina(stamina);
        if (oldVal != stamina) {
            EventUtil.triggerUpdateTargetProgress(getIdx(), TargetTypeEnum.TTE_Mist_ConsumeMistStamina, removeCount, 0);
            updateMistStanima();
        }
        return true;
    }

    protected void recoverStamina(long curTime) {
        if (!getDb_data().getMistForestData().getFirstEnterMistFlag()) {
            return;
        }
        int stamina = getDb_data().getMistForestData().getStamina();
        long lastRecoverTime = getDb_data().getMistForestData().getLastRecoverStaminaTime();
        if (stamina >= GameConfig.getById(GameConst.CONFIG_ID).getMiststaminamaxnum()) {
            if (lastRecoverTime > 0) {
                getDb_data().getMistForestDataBuilder().setLastRecoverStaminaTime(0);
            }
            return;
        }
        int unitRecoverStanima = GameConfig.getById(GameConst.CONFIG_ID).getMistrecoverstamina();
        long interval = GameConfig.getById(GameConst.CONFIG_ID).getMistrecoverstaminainterval() * TimeUtil.MS_IN_A_MIN;
        if (0 == lastRecoverTime) {
            getDb_data().getMistForestDataBuilder().setLastRecoverStaminaTime(curTime + interval);
        } else {
            long deltaTime = curTime - lastRecoverTime;
            if (deltaTime <= 0) {
                return;
            }
            int recoverRate = (int) (deltaTime / interval);
            if (recoverRate <= 0) {
                return;
            }
            int realRecoverStanima = recoverRate * unitRecoverStanima;
            addMistStamina(realRecoverStanima, false);
            getDb_data().getMistForestDataBuilder().setLastRecoverStaminaTime(curTime + interval - deltaTime % interval);
            LogUtil.info("Player[{}] name={}, recover stamina newStamina={},lastRecoverTime={}", getIdx(), getName(), getDb_data().getMistForestData().getStamina(), getDb_data().getMistForestData().getLastRecoverStaminaTime());
        }
    }

    public void updateMistStanima() {
        int stamina = getDb_data().getMistForestData().getStamina();
        SC_UpdateMistStamina.Builder builder = SC_UpdateMistStamina.newBuilder();
        builder.setStamina(stamina);
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdateMistStamina_VALUE, builder);

        PlayerMistServerInfo mistSvrData = CrossServerManager.getInstance().getMistForestPlayerServerInfo(getIdx());
        if (mistSvrData != null && mistSvrData.getMistRule() == EnumMistRuleKind.EMRK_Common) {
            GS_CS_UpdateMistStamina.Builder updateBuilder = GS_CS_UpdateMistStamina.newBuilder();
            updateBuilder.setPlayerIdx(getIdx());
            updateBuilder.setNewValue(stamina);
            CrossServerManager.getInstance().sendMsgToMistForest(getIdx(), MsgIdEnum.GS_CS_UpdateMistStamina_VALUE, updateBuilder, true);
        }
    }

    public int getMazeBuyGoodsTimes(int goodsId) {
        MazeBuyGoodsTimes.Builder buyTimesData = getDb_data().getMazeDataBuilder().getBuyGoodsTimesBuilder();
        for (int i = 0; i < buyTimesData.getGoodsIdCount(); i++) {
            int tmpId = buyTimesData.getGoodsId(i);
            if (tmpId == goodsId) {
                return buyTimesData.getBuyTimes(i);
            }
        }
        return 0;
    }

    public void sendMistFreeTickets() {
        SC_UpdateMistTicket.Builder builder = SC_UpdateMistTicket.newBuilder();
        builder.addRuleType(EnumMistRuleKind.EMRK_GhostBuster);
        builder.addFreeTicket(getDb_data().getGhostBusterDataBuilder().getFreeTickets());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdateMistTicket_VALUE, builder);
    }

    public void addForceExitGhostScoreRecord() {
        DB_GhostBusterData.Builder dbBulder = getDb_data().getGhostBusterDataBuilder();
        if (dbBulder.getRecentRecordsCount() >= GameConfig.getById(GameConst.CONFIG_ID).getMaxghostrecordcount()) {
            dbBulder.removeRecentRecords(0);
        }
        GhostBusterRecordData.Builder newRecord = GhostBusterRecordData.newBuilder();
        newRecord.setFightTime(GlobalTick.getInstance().getCurrentTime());
        dbBulder.addRecentRecords(newRecord);

        SC_PlayerGhostBusterRecord.Builder builder = SC_PlayerGhostBusterRecord.newBuilder();
        builder.setHighestRecord(getDb_data().getGhostBusterDataBuilder().getHighestRecord());
        builder.addAllRecentRecords(getDb_data().getGhostBusterDataBuilder().getRecentRecordsList());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_PlayerGhostBusterRecord_VALUE, builder);
    }

    public void addGhostScoreRecord(GhostBusterRankData rankData, long settleTime) {
        DB_GhostBusterData.Builder dbBulder = getDb_data().getGhostBusterDataBuilder();
        if (dbBulder.getHighestRecord().getScore() < rankData.getScore()) {
            dbBulder.getHighestRecordBuilder().setScore(rankData.getScore());
            dbBulder.getHighestRecordBuilder().setRank(rankData.getRank());
            dbBulder.getHighestRecordBuilder().setFightTime(settleTime);

        }
        if (rankData.getScore() > 0) {
            dbBulder.setTotalGainScore(dbBulder.getTotalGainScore() + rankData.getScore());
            SC_GhostBusterTotalScore.Builder builder = SC_GhostBusterTotalScore.newBuilder();
            builder.setGhoseTotalScore(getDb_data().getGhostBusterDataBuilder().getTotalGainScore());
            GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_GhostBusterTotalScore_VALUE, builder);

            EventUtil.triggerUpdateTargetProgress(getIdx(), TargetTypeEnum.TEE_GhostBuster_CumuGainScore, rankData.getScore(), 0);
        }
        if (dbBulder.getRecentRecordsCount() >= GameConfig.getById(GameConst.CONFIG_ID).getMaxghostrecordcount()) {
            dbBulder.removeRecentRecords(0);
        }
        GhostBusterRecordData.Builder newRecord = GhostBusterRecordData.newBuilder();
        newRecord.setRank(rankData.getRank());
        newRecord.setScore(rankData.getScore());
        newRecord.setFightTime(settleTime);
        dbBulder.addRecentRecords(newRecord);

        SC_PlayerGhostBusterRecord.Builder builder = SC_PlayerGhostBusterRecord.newBuilder();
        builder.setHighestRecord(getDb_data().getGhostBusterDataBuilder().getHighestRecord());
        builder.addAllRecentRecords(getDb_data().getGhostBusterDataBuilder().getRecentRecordsList());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_PlayerGhostBusterRecord_VALUE, builder);
    }

    public void sendRefreshPlayerLvMsg() {
        SC_RefreshPlayerLv.Builder builder = SC_RefreshPlayerLv.newBuilder();
        builder.setNewExp(getExperience());
        builder.setNewLevel(getLevel());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_RefreshPlayerLv_VALUE, builder);
    }

    public void checkResCopy() {
        Builder db_data = getDb_data();
        if (db_data == null) {
            LogUtil.error("playerIdx =" + getIdx() + ", dbData is null");
            return;
        }

        DB_ResourceCopyDict.Builder resCopyDataBuilder = db_data.getResCopyDataBuilder();
        Set<Integer> allResCopyType = ResourceCopy.getInstance().getAllResCopyType();

        for (Integer type : allResCopyType) {
            DB_ResourceCopy.Builder resBuilder = getResourceCopyData(type);
            boolean exist = true;
            if (resBuilder == null) {
                resBuilder = DB_ResourceCopy.newBuilder();
                exist = false;
            }

            Collection<ResourceCopyObject> allByType = ResourceCopy.getInstance().getAllByType(type);
            if (allByType == null || allByType.isEmpty()) {
                continue;
            }

            for (ResourceCopyObject obj : allByType) {
                if (resBuilder.getUnlockProgressList().contains(obj.getTypepassindex())
                        || resBuilder.getProgressList().contains(obj.getTypepassindex())) {
                    continue;
                }

                if (obj.getUnlocklv() <= getLevel()) {
                    resBuilder.addUnlockProgress(obj.getTypepassindex());
                    EventUtil.triggerUpdateTargetProgress(getIdx(), TargetTypeEnum.TTE_ResCopy_UnlockLv, obj.getAfterid(), 0);
                }
            }
            if (!exist) {
                resCopyDataBuilder.addResourceType(type);
                resCopyDataBuilder.addResourceCopyData(resBuilder);
            }
        }

        List<ResCopy> copies = buildResCopies();
        if (copies == null || copies.isEmpty()) {
            return;
        }
        SC_RefreshResCopy.Builder refreshList = SC_RefreshResCopy.newBuilder();
        refreshList.addAllCopies(copies);
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_RefreshResCopy_VALUE, refreshList);
    }

    /**
     * 构建资源副本消息
     */
    public ResCopy buildResCopy(int resType) {
        Builder db_data = getDb_data();
        if (db_data == null) {
            LogUtil.error("playerIdx[" + getIdx() + "] db data is null");
            return null;
        }
        DB_ResourceCopy.Builder resourceCopy = getResourceCopyData(resType);
        if (resourceCopy == null) {
            return null;
        }

        ResCopy.Builder builder = ResCopy.newBuilder();
        builder.setType(ResourceCopyTypeEnum.forNumber(resType));
        builder.addAllProgress(resourceCopy.getProgressList());
        builder.addAllUnlockProgress(resourceCopy.getUnlockProgressList());
        builder.setChallengeTimes(resourceCopy.getChallengeTimes());
        builder.setBuyTimes(resourceCopy.getBuyTimes());

        return builder.build();
    }

    public DB_ResourceCopy.Builder getResourceCopyData(int resType) {
        DB_ResourceCopy.Builder resourceCopy = null;
        DB_ResourceCopyDict.Builder resCopyDataBuilder = db_data.getResCopyDataBuilder();
        for (int i = 0; i < resCopyDataBuilder.getResourceTypeCount(); i++) {
            int type = resCopyDataBuilder.getResourceType(i);
            if (type == resType) {
                resourceCopy = resCopyDataBuilder.getResourceCopyDataBuilder(i);
                break;
            }
        }
        return resourceCopy;
    }

    /**
     * 构建资源副本消息
     */
    public List<ResCopy> buildResCopies() {
        Set<Integer> allResCopyType = ResourceCopy.getInstance().getAllResCopyType();
        if (allResCopyType == null || allResCopyType.isEmpty()) {
            return null;
        }

        List<ResCopy> copies = new ArrayList<>();
        for (Integer type : allResCopyType) {
            ResCopy resCopy = buildResCopy(type);
            if (resCopy != null) {
                copies.add(resCopy);
            }
        }
        return copies;
    }

    public boolean isFriend(String targetPlayerIdx) {
        if (targetPlayerIdx == null) {
            return false;
        }

        Builder db_data = getDb_data();
        if (db_data == null) {
            return false;
        }

        return db_data.getFriendInfo().getOwnedMap().containsKey(targetPlayerIdx);
    }

//    /**
//     * 剔除已经是好友的Idx
//     *
//     * @param targetList
//     * @return
//     */
//    public List<String> excludeAlreadyFriend(List<String> targetList) {
//        Builder db_data = getDb_data();
//        if (db_data == null) {
//            return targetList;
//        }
//
//        Map<String, DB_OwnedFrienfInfo> ownedMap = db_data.getFriendInfo().getOwnedMap();
//        if (ownedMap == null) {
//            return targetList;
//        }
//
//        List<String> resultList = new ArrayList<>();
//        for (String idx : targetList) {
//            if (!ownedMap.containsKey(idx)) {
//                resultList.add(idx);
//            }
//        }
//        return resultList;
//    }

    /**
     * 世界聊天功能开放,    1:封号  2:禁言
     * ChatRetCode
     */
    private int chatOpenState() {
        //禁言
        Builder db_data = getDb_data();
        if (db_data != null) {
            Map<Integer, DB_BanInfo> bannedInfosMap = db_data.getBannedInfosMap();
            if (bannedInfosMap.containsKey(Ban.CHAT)) {
                DB_BanInfo db_banInfo = bannedInfosMap.get(Ban.CHAT);
                if (GlobalTick.getInstance().getCurrentTime() <= db_banInfo.getEndTime()) {
                    return ChatRetCode.BANNED_TO_POST;
                }
            }
        }

        //功能开放
        if (!functionUnLock(EnumFunction.WordChat)) {
            return ChatRetCode.LV_NOT_ENOUGH;
        }
        return ChatRetCode.OPEN;
    }

    /**
     * 聊天权限描述
     */
    public String getChatStateMsg() {
        int curState = getCurChatState();
        LanguageEnum language = getLanguage();
        if (curState == ChatRetCode.LV_NOT_ENOUGH) {
            return ServerStringRes.getContentByLanguage(ServerStringConst.LV_NOT_ENOUGH, language,
                    FunctionOpenLvConfig.getOpenLv(EnumFunction.WordChat));
        } else if (curState == ChatRetCode.BANNED_TO_POST) {
            Builder db_data = getDb_data();
            if (db_data != null) {
                DB_BanInfo db_banInfo = db_data.getBannedInfosMap().get(Ban.CHAT);
                if (db_banInfo != null) {
                    return PlatformManager.getInstance().getBanMsg(db_banInfo.getMsgId(), language);
                }
            }
        }
        return "";
    }

    public LanguageEnum getLanguage() {
        LanguageEnum language = getDb_data().getLanguage();
        if (LanguageEnum.UNRECOGNIZED == language) {
            return LanguageEnum.LE_SimpleChinese;
        }
        return language;
    }

    public String getNetType() {
        ClientData.Builder clientData = getClientData();
        if (clientData == null) {
            return "";
        }

        return clientData.getIsWify() ? "WIFI" : "移动";
    }

    public int getClientSourceId() {
        ClientData.Builder clientData = getClientData();
        if (clientData == null) {
            return 0;
        }

        return clientData.getSourceId();
    }

    /**
     * 踢出原因
     *
     * @param codeEnum
     */
    public void kickOut(RetCodeEnum codeEnum) {
        try {
            if (!isOnline()) {
                return;
            }
            SC_KickOut.Builder builder = SC_KickOut.newBuilder();
            builder.setRetCode(GameUtil.buildRetCode(codeEnum));
            GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_KickOut_VALUE, builder);

            //关闭socket
            GlobalData.getInstance().closeChannel(getIdx());
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }

//        Event event = Event.valueOf(EventType.ET_Logout, GameUtil.getDefaultEventSource(), this);
//        EventManager.getInstance().dispatchEvent(event);
    }

    /**
     * @param type    1:封号  2:禁言  3:禁止评论
     * @param endTime
     * @param msgId   详细信息
     */
    public void ban(int type, long endTime, long msgId) {
        Builder db_data = getDb_data();
        if (db_data == null) {
            LogUtil.error("playerIdx [" + getIdx() + "] db data is null");
            return;
        }

        if (endTime <= GlobalTick.getInstance().getCurrentTime()) {
            return;
        }

        DB_BanInfo.Builder builder = DB_BanInfo.newBuilder();
        builder.setType(type);
        builder.setEndTime(endTime);
        builder.setMsgId(msgId);
        db_data.putBannedInfos(type, builder.build());

        if (type == Ban.ROLE) {
            kickOut(RetCodeEnum.RCE_KickOut_Banned);
        } else if (type == Ban.CHAT) {
            updateChatFunctionOpen();
        } else if (type == Ban.COMMENT) {
            //禁止评论,屏蔽玩家所有评论
            EventUtil.shieldComment(Arrays.asList(CommentTypeEnum.values()), getIdx());
        }

        //在线推送封禁tips
        if (isOnline()) {
            String banTips = PlatformManager.getInstance().getBanMsg(msgId, getLanguage());
            if (StringUtils.isNotBlank(banTips)) {
                SC_Tips.Builder tipsBuilder = SC_Tips.newBuilder();
                tipsBuilder.setMsg(banTips);
                GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_Tips_VALUE, tipsBuilder);
            }
        }
        LogUtil.info("player [" + getName() + "] ban, type = " + type + ", endTime = " + endTime);
    }

    public void cancelBan(int type) {
        Builder db_data = getDb_data();
        if (db_data == null) {
            return;
        }
        db_data.removeBannedInfos(type);
        updateChatFunctionOpen();
        LogUtil.info("player [" + getIdx() + "] cancelBan");
    }

    public long getBanMsgId(int type) {
        Builder db_data = getDb_data();
        if (db_data == null) {
            return 0;
        }
        DB_BanInfo db_banInfo = db_data.getBannedInfosMap().get(type);
        if (db_banInfo == null) {
            return 0;
        }
        return db_banInfo.getMsgId();
    }

    public boolean isRoleBaned() {
        return isBaned(Ban.ROLE);
    }

    public boolean isBaned(int banType) {
        Builder db_data = getDb_data();
        if (db_data != null) {
            Map<Integer, DB_BanInfo> bannedInfosMap = db_data.getBannedInfosMap();
            if (bannedInfosMap.containsKey(banType)) {
                DB_BanInfo db_banInfo = bannedInfosMap.get(banType);
                if (GlobalTick.getInstance().getCurrentTime() <= db_banInfo.getEndTime()) {
                    return true;
                }
            }
        }
        return false;
    }

    public RetCodeEnum canLogIn() {
        if (isRoleBaned()) {
            return RetCodeEnum.RCE_KickOut_Banned;
        }
        if (getLogintime() != null) {
            long lastLoginTime = getLogintime().getTime();
            if (lastLoginTime > 0 && GlobalTick.getInstance().getCurrentTime() - lastLoginTime <= TimeUtil.MS_IN_A_S) {
                return RetCodeEnum.RCE_Login_LoginTooFast;
            }
        }
        return RetCodeEnum.RCE_Success;
    }

    public void updateStatusToFriend(FriednStateEnum state) {
        if (state == null || state == FriednStateEnum.FST_Null) {
            return;
        }

        SC_UpdateFriendState.Builder builder = SC_UpdateFriendState.newBuilder();
        builder.setNewState(state);
        builder.setPlayerIdx(getIdx());
        Builder db_data = getDb_data();
        if (db_data != null) {
            Map<String, DB_OwnedFriendInfo> ownedMap = db_data.getFriendInfoBuilder().getOwnedMap();
            for (String idx : ownedMap.keySet()) {
                if (GlobalData.getInstance().checkPlayerOnline(idx)) {
                    GlobalData.getInstance().sendMsg(idx, MsgIdEnum.SC_UpdateFriendState_VALUE, builder);
                }
            }
        }
    }

    public List<Integer> getSkillIds() {
        return getDb_data().getArtifactList().stream().map(artifact -> artifact.getPlayerSkill().getSkillCfgId()).collect(Collectors.toList());
    }

    /**
     *=================================抽卡  start===================================================
     */

    /**
     * @return 本次获得的奖励
     */
    public DB_HighCard drawHighCard() {
        Builder db_data = getDb_data();
        if (db_data == null || getHighNoGainCount() <= 0) {
            return null;
        }

        List<DB_HighCard.Builder> unclaimed = new ArrayList<>();
        List<DB_HighCard.Builder> alreadyClaimed = new ArrayList<>();
        List<DB_HighCard.Builder> highCardsBuilderList = db_data.getDrawCardBuilder().getHighCardsBuilderList();
        int totalOdds = 0;
//        boolean containRed = false;
        for (DB_HighCard.Builder builder : highCardsBuilderList) {
            if (!builder.getClaimed()) {
                totalOdds += builder.getOdds();
                unclaimed.add(builder);
//
//                if (builder.getQuality() == DrawCardManager.HIGHEST_QUALITY) {
//                    containRed = true;
//                }
            } else {
                alreadyClaimed.add(builder);
            }
        }

//        DB_HighCard.Builder result = null;
//        if (containRed && canDirectGetRed(alreadyClaimed)) {
//            for (DB_HighCard.Builder builder : unclaimed) {
//                if (builder.getQuality() == DrawCardManager.HIGHEST_QUALITY) {
////                    builder.setClaimed(true);
//                    result = builder;
//                    break;
//                }
//            }
//        } else {
//        result = randomHighCard(unclaimed, totalOdds);
//        }

        DB_HighCard.Builder result = randomHighCard(unclaimed, totalOdds);

        if (result != null) {
            //修改特殊处理次数红卡概率
            increaseSpecialTimesRedQuality(result.getQuality());
            LogUtil.debug("model.player.entity.playerEntity.drawHighCard, player Idx:" + getIdx() + ", random get reward, detail:" + result);
            //设置当前抽取的index
            getDb_data().getDrawCardBuilder().setCurHighDrawIndex(result.getIndex());
            return result.build();
        }

        return null;
    }

    private void increaseSpecialTimesRedQuality(int curGetQuality) {
        if (DrawCardManager.HIGHEST_QUALITY == curGetQuality) {
            return;
        }

        //是否是特殊处理轮次
        int[] dealConfig = DrawCardUtil.getHighSpecialDealConfig(getDb_data().getDrawCard().getHighOpenedTimes());
        if (dealConfig == null) {
            return;
        }

        DB_DrawCardData.Builder cardBuilder = getDb_data().getDrawCardBuilder();
        cardBuilder.setCurHighPoolSpecialDealRedQuality(cardBuilder.getCurHighPoolSpecialDealRedQuality() + dealConfig[2]);
    }

    /**
     * 是否可以直接获得红色品质概率
     * 当一次转盘出现多张红卡时,特殊处理轮次规则只使用于第一张红卡，后续红卡随机规则按照原概率进行
     *
     * @return
     */
    private boolean canDirectGetRed(List<DB_HighCard.Builder> claimed) {
        //是否已经出过红卡
        if (CollectionUtils.isNotEmpty(claimed)) {
            for (DB_HighCard.Builder builder : claimed) {
                if (builder.getQuality() == DrawCardManager.HIGHEST_QUALITY) {
                    return false;
                }
            }
        }

        //是否是特殊处理轮次
        int curTimes = getDb_data().getDrawCard().getHighOpenedTimes();
        int[] dealConfig = DrawCardUtil.getHighSpecialDealConfig(curTimes);
        if (dealConfig == null) {
            return false;
        }

        //是否可以获得红色品质
        int playerOdds = getDb_data().getDrawCard().getCurHighPoolSpecialDealRedQuality();
        int random = new Random().nextInt(100);

        if (playerOdds > random) {
            LogUtil.info("playerEntity.canDirectGetRed, playerIdx:" + getIdx() + ", can direct get red card, playerOdds:"
                    + playerOdds + " ,random:" + random);
            return true;
        } else {
            return false;
        }
    }

    private DB_HighCard.Builder randomHighCard(List<DB_HighCard.Builder> list, int totalOdds) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Random random = new Random();
        if (totalOdds <= 0) {
            DB_HighCard.Builder builder = list.get(random.nextInt(list.size()));
            builder.setClaimed(true);
            return builder;
        } else {
            int curNum = 0;
            int thisOdds = random.nextInt(totalOdds);
            for (DB_HighCard.Builder builder : list) {
                if ((curNum += builder.getOdds()) >= thisOdds) {
                    builder.setClaimed(true);
                    return builder;
                }
            }
        }
        return null;
    }

    /**
     * 本轮高级抽卡是否已经抽取完毕
     *
     * @return
     */
    public int getHighNoGainCount() {
        Builder db_data = getDb_data();
        if (db_data == null) {
            return 0;
        }

        int noGain = 0;
        List<DB_HighCard> highCardsList = db_data.getDrawCard().getHighCardsList();
        for (DB_HighCard db_highCard : highCardsList) {
            if (!db_highCard.getClaimed()) {
                noGain++;
            }
        }
        return noGain;
    }

    public List<HighPoolReward> getHighCardPool() {
        List<HighPoolReward> result = new ArrayList<>();
        Builder db_data = getDb_data();
        if (db_data == null) {
            return result;
        }

        List<DB_HighCard> highCardsList = db_data.getDrawCard().getHighCardsList();
        for (DB_HighCard db_highCard : highCardsList) {
            HighPoolReward.Builder builder = HighPoolReward.newBuilder();
            builder.setIndex(db_highCard.getIndex());
            builder.setReward(db_highCard.getReward());
            builder.setQuality(db_highCard.getQuality());
            builder.setClaimed(db_highCard.getClaimed());
            result.add(builder.build());
        }
        return result;
    }

    public void sendDrawCardInfo() {
        SC_ClaimDrawCardInfo.Builder resultBuilder = SC_ClaimDrawCardInfo.newBuilder();
        resultBuilder.addAllRewards(getHighCardPool());
        resultBuilder.setCurExp(getDb_data().getDrawCard().getCumulateExp());
        resultBuilder.setUseItemFirst(getDb_data().getDrawCard().getUseItemFirst());
        resultBuilder.setHighOpenTimes(getDb_data().getDrawCard().getHighOpenedTimes());
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setTodayDrawCount(getDb_data().getDrawCard().getTodayDrawCount());
        resultBuilder.setCommonMustDrawTimes(getDb_data().getDrawCard().getCommonMustDrawCount());
        resultBuilder.setCurHighDrawTimes(getDb_data().getDrawCard().getCurHighDrawTimes());
        resultBuilder.setCurHighDrawIndex(getDb_data().getDrawCard().getCurHighDrawIndex());
        resultBuilder.setCommonCardFreeTime(getDb_data().getDrawCard().getNextCommonCardFreeTime());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_ClaimDrawCardInfo_VALUE, resultBuilder);
    }

    /**
     * 添加抽卡经验值
     *
     * @param addExp
     */
    public void addDrawCardExp(int addExp) {
        DB_DrawCardData.Builder drawCardBuilder = getDb_data().getDrawCardBuilder();
        drawCardBuilder.setCumulateExp(drawCardBuilder.getCumulateExp() + addExp);

        SC_RefreshCardExp.Builder builder = SC_RefreshCardExp.newBuilder();
        builder.setNewExp(drawCardBuilder.getCumulateExp());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_RefreshCardExp_VALUE, builder);
    }

    /**
     * 获取指定奖次和品质的当前概率
     *
     * @param type
     * @param quality
     * @return
     */
    private DB_DrawCardCurOdds.Builder getDrawCardCurOddsBuilder(EnumDrawCardType type, int quality) {
        if (type == null || type == EnumDrawCardType.EDCT_NULL) {
            return null;
        }
        DB_DrawCardData.Builder drawCardBuilder = getDb_data().getDrawCardBuilder();
        List<DB_DrawCardCurOdds.Builder> builderList = drawCardBuilder.getDrawCardCurOddsBuilderList();
        for (DB_DrawCardCurOdds.Builder builder : builderList) {
            if (Objects.equals(type, builder.getType()) && Objects.equals(quality, builder.getQuality())) {
                return builder;
            }
        }
        return null;
    }

    /**
     * 清空高级抽卡信息
     */
    public void clearHighPoolData() {
        getDb_data().getDrawCardBuilder().clearHighCards();
        getDb_data().getDrawCardBuilder().clearCurHighDrawTimes();
        getDb_data().getDrawCardBuilder().clearCurHighDrawIndex();
    }

    public int getDrawCardCurOdds(EnumDrawCardType type, int quality) {
        DB_DrawCardCurOdds.Builder builder = getDrawCardCurOddsBuilder(type, quality);
        if (builder != null) {
            return builder.getCurOdds();
        }
        return 0;
    }

    public void setDrawCardOdds(EnumDrawCardType type, int quality, int newOdds) {
        if (type == null || type == EnumDrawCardType.EDCT_NULL) {
            return;
        }

        DB_DrawCardCurOdds.Builder builder = getDrawCardCurOddsBuilder(type, quality);
        if (builder != null) {
            builder.setCurOdds(newOdds);
        } else {
            DB_DrawCardCurOdds.Builder newBuilder = DB_DrawCardCurOdds.newBuilder()
                    .setType(type)
                    .setQuality(quality)
                    .setCurOdds(newOdds);
            getDb_data().getDrawCardBuilder().addDrawCardCurOdds(newBuilder);
        }
    }

    public DB_HighCard getCurHighDraw() {
        DB_DrawCardData.Builder builder = getDb_data().getDrawCardBuilder();
        int curHighDrawIndex = builder.getCurHighDrawIndex();
        for (DB_HighCard db_highCard : builder.getHighCardsList()) {
            if (db_highCard.getIndex() == curHighDrawIndex) {
                return db_highCard;
            }
        }
        return null;
    }

    public void setHighClaimed() {
        DB_DrawCardData.Builder drawCardBuilder = getDb_data().getDrawCardBuilder();
        for (DB_HighCard.Builder builder : drawCardBuilder.getHighCardsBuilderList()) {
            if (builder.getIndex() == drawCardBuilder.getCurHighDrawIndex()) {
                builder.setClaimed(true);
                drawCardBuilder.clearCurHighDrawIndex();
                break;
            }
        }
    }

    public void increaseHighDrawTimes() {
        DB_DrawCardData.Builder drawCardBuilder = getDb_data().getDrawCardBuilder();
        drawCardBuilder.setCurHighDrawTimes(drawCardBuilder.getCurHighDrawTimes() + 1);
    }

    /**
     * =================================抽卡 end===================================================
     */

    public DB_MonsterDifficultyInfo.Builder getMonsterDiffByFunction(EnumFunction function) {
        if (function == null || function == EnumFunction.NullFuntion) {
            return null;
        }

        List<DB_MonsterDifficultyInfo.Builder> builderList = getDb_data().getMonsterDiffBuilderList();

        DB_MonsterDifficultyInfo.Builder resultBuilder = null;
        for (DB_MonsterDifficultyInfo.Builder builder : builderList) {
            if (builder.getFunction() == function) {
                resultBuilder = builder;
            }
        }

        if (resultBuilder == null) {
            resultBuilder = createNewMonsterDiff(function);
            if (resultBuilder == null) {
                LogUtil.error("playerEntity.getMonsterDiffByFunction, create new monster diff failed");
                return null;
            }

            getDb_data().addMonsterDiff(resultBuilder);
        }
        return resultBuilder;
    }

    private DB_MonsterDifficultyInfo.Builder createNewMonsterDiff(EnumFunction function) {
        if (function == null || function == EnumFunction.NullFuntion) {
            return null;
        }
        return createNewMonsterDiff(function, MonsterDifficulty.getByPlayerIdx(getIdx()));
    }

    public DB_MonsterDifficultyInfo.Builder createNewMonsterDiff(EnumFunction function, MonsterDifficultyObject diffCfg) {
        if (function == null || function == EnumFunction.NullFuntion || diffCfg == null) {
            return null;
        }

        DB_MonsterDifficultyInfo.Builder resultBuilder = DB_MonsterDifficultyInfo.newBuilder();
        resultBuilder.setFunction(function);
        resultBuilder.setLevel(GameUtil.randomInScope(diffCfg.getMonsterlevelscope()));
        return resultBuilder;
    }

    private void putMonsterDiff(DB_MonsterDifficultyInfo.Builder newBuilder) {
        if (newBuilder == null) {
            return;
        }
        List<DB_MonsterDifficultyInfo.Builder> builderList = getDb_data().getMonsterDiffBuilderList();

        boolean contain = false;
        for (DB_MonsterDifficultyInfo.Builder builder : builderList) {
            if (builder.getFunction() == newBuilder.getFunction()) {
                contain = true;
                builder.setLevel(newBuilder.getLevel());
                break;
            }
        }

        if (!contain) {
            getDb_data().addMonsterDiff(newBuilder);
        }
    }

    public void reCreateMonsterDiff(EnumFunction function, int nodeId) {
        putMonsterDiff(createNewMonsterDiff(function, MonsterDifficulty.getById(nodeId)));
    }

    // 广告
    public void sendTotalAdsInfo() {
        SC_TotalAdsInfo.Builder builder = buildAdsInfo();
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_TotalAdsInfo_VALUE, builder);
    }

    public SC_TotalAdsInfo.Builder buildAdsInfo() {
        SC_TotalAdsInfo.Builder builder = SC_TotalAdsInfo.newBuilder();
        long curTime = GlobalTick.getInstance().getCurrentTime();
        long starDisplayFreeGiftTime = AdsConfig.getById(GameConst.CONFIG_ID).getFreeadsdisplaytime() * TimeUtil.MS_IN_A_MIN;
        long starDisplayWheelBonusTime = AdsConfig.getById(GameConst.CONFIG_ID).getWheeladsdisplaytime() * TimeUtil.MS_IN_A_MIN;
        if (curTime - getCreatetime().getTime() >= starDisplayFreeGiftTime) {
            builder.setFreeGiftTimes(getDb_data().getAdsBonusData().getFreeGiftTimes());
        }
        if (curTime - getCreatetime().getTime() >= starDisplayWheelBonusTime) {
            builder.setFreeWheelBonusTimes(getDb_data().getAdsBonusData().getFreeWheelBonusTimes());
            builder.setWatchWheelBonusAdsTimes(getDb_data().getAdsBonusData().getRemainWatchBonusTimes());
        }
        return builder;
    }


    public int getTodayThisLoginOnline() {
        return (int) ((GlobalTick.getInstance().getCurrentTime() - Math.max(TimeUtil.getNextDaysResetTime(GlobalTick.getInstance().getCurrentTime(), 0), logintime.getTime())) / 1000);
    }

    public int getCurrentOnline() {
        return getDb_data().getTodayOnline() + getTodayThisLoginOnline();
    }

    public int getCumuOnline() {
        if (logintime == null) {
            return 0;
        }
        long lastLoginTime = logintime.getTime();
        if (lastLoginTime <= 0) {
            return 0;
        }
        int cumuOnline = getDb_data().getCumuOnline();
        return cumuOnline + (int) ((GlobalTick.getInstance().getCurrentTime() - lastLoginTime) / 1000);
    }

    public void sendRefreshOnlineTime(long activityId) {
        if (activityId != ActivityUtil.LocalActivityId.CumuOnline && activityId != ActivityUtil.LocalActivityId.DailyOnline) {
            return;
        }
        Activity.SC_RefreshCumuOnline.Builder builder = Activity.SC_RefreshCumuOnline.newBuilder();
        builder.setCumuOnlineTime(activityId == ActivityUtil.LocalActivityId.CumuOnline ? getCumuOnline() : getCurrentOnline());
        builder.setCurrentTime(GlobalTick.getInstance().getCurrentTime());
        builder.setId(activityId);
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_RefreshCumuOnline_VALUE, builder);
    }

    public int getSkillLv(int skillId) {
        for (Artifact artifact : getDb_data().getArtifactList()) {
            if (artifact.getPlayerSkill().getSkillCfgId() == skillId) {
                return artifact.getPlayerSkill().getSkillLv();
            }
        }
        return 0;
    }

    public void sendPlayerSkillUpdate(int skillId) {
        int skillLv = getSkillLv(skillId);
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdateSkill_VALUE, SC_UpdateSkill.newBuilder().setSkillId(skillId).setStarLv(skillLv).setSkillLv(skillLv));
    }

    public List<SkillBattleDict> getSkillBattleDict(Collection<Integer> values) {
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        List<SkillBattleDict> skills = new ArrayList<>();
        for (Integer skillId : values) {
            skills.add(SkillBattleDict.newBuilder().setSkillId(skillId).setSkillLv(getSkillLv(skillId)).build());
        }
        return skills;
    }


    public DB_SelectedPet.Builder getDbSelectedBuilder(int petType) {
        for (int i = 0; i < getDb_data().getSelectedPetBuilder().getPetDataCount(); i++) {
            int type = getDb_data().getSelectedPetBuilder().getPetType(i);
            if (type == petType) {
                return getDb_data().getSelectedPetBuilder().getPetDataBuilder(i);
            }
        }
        return null;
    }

    /**
     * 设置玩家抽卡自选宠物
     *
     * @param petCfgId
     * @param setIndex
     * @return
     */
    public RetCodeEnum setDrawCardSelectedPet(int petCfgId, int setIndex) {
        if (PetBaseProperties.getByPetid(petCfgId) == null || setIndex < 0
                || setIndex >= DrawCard.getById(GameConst.CONFIG_ID).getSelectedeachpettypelimit()) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        //去重
        cancelSelectedPet(petCfgId);

        boolean exist = true;
        int petType = PetBaseProperties.getTypeById(petCfgId);
        DB_SelectedPet.Builder selectedPet = getDbSelectedBuilder(petType);
        if (selectedPet == null) {
            selectedPet = DB_SelectedPet.newBuilder();
            exist = false;
        }
        selectedPet.addSelectPetData(SelectedPetIndex.newBuilder().setPetId(petCfgId).setIndex(setIndex));

        if (!exist) {
            getDb_data().getSelectedPetBuilder().addPetType(petType);
            getDb_data().getSelectedPetBuilder().addPetData(selectedPet);
        }

        return RetCodeEnum.RCE_Success;
    }


    public boolean activeMonthCard(int cardId) {
        return getDb_data().getRechargeCards().getMonthCardListList().stream().anyMatch(card ->
                card.getCarId() == cardId && card.getRemainDays() > 0);
    }


   /* public void activeOneMonthCard(int cardId, Reason reason) {
        List<protocol.PlayerDB.MonthCardInfo> monthCardListList = getDb_data().getMonthCardListList();
        protocol.PlayerDB.MonthCardInfo cardInfo = monthCardListList.stream().filter(card -> card.getCarId() == cardId).findAny().orElse(null);
        if (cardInfo != null && cardInfo.getRemainDays() > 0) {
            LogUtil.error("BuyMonthCardHandler,player monthly card repeated buy,playerIdx:{},cardId:{},remainDays:{}", getIdx(), cardId, cardInfo.getRemainDays());
            return;
        }
        MonthlyCardConfigObject config = MonthlyCardConfig.getById(cardId);
        if (config == null) {
            LogUtil.error("playerIdx:{},activeOneMonthCard,cant`t find MonthlyCardConfig by monthCardId type:{}", getIdx(), cardId);
            return;
        }
        List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(config.getInstantrewards());
        MonthCard.SC_BuyMonthCard.Builder result = MonthCard.SC_BuyMonthCard.newBuilder();
        //添加玩家月卡信息
        List<protocol.PlayerDB.MonthCardInfo> newCardList = getDb_data().getMonthCardListList().stream()
                .filter(card -> cardId != card.getCarId()).collect(Collectors.toList());

        protocol.PlayerDB.MonthCardInfo.Builder card = buildNewCardInfo(cardId);
        newCardList.add(card.build());
        getDb_data().clearMonthCardList().addAllMonthCardList(newCardList);

        //新卡立马获得邮件奖励和购买奖励
        RewardManager.getInstance().doRewardByList(getIdx(), rewards, reason, true);
        MonthCardUtil.doMonthCardDailyReward(getIdx(), cardId, card.getRemainDays());

        //推送购买消息
        result.setRemainDays(card.getRemainDays());
        result.setRetCode(GameUtil.buildRetCode(protocol.RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_BuyMonthCard_VALUE, result);
    }

    private protocol.PlayerDB.MonthCardInfo.Builder buildNewCardInfo(int buyCardId) {
        protocol.PlayerDB.MonthCardInfo.Builder card = protocol.PlayerDB.MonthCardInfo.newBuilder();
        card.setCarId(buyCardId);
        //购买的时候就会消耗一次
        card.setRemainDays(GameConst.ONE_MONTH_CARD_USE_DAY - 1);
        return card;
    }*/


    /**
     * 取消自选宠物
     *
     * @param petCfgId
     * @return
     */
    public RetCodeEnum cancelSelectedPet(int petCfgId) {
        if (PetBaseProperties.getByPetid(petCfgId) == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        int petType = PetBaseProperties.getTypeById(petCfgId);
        DB_SelectedPet.Builder selectedPet = null;
        for (int i = 0; i < getDb_data().getSelectedPetBuilder().getPetDataCount(); i++) {
            int type = getDb_data().getSelectedPetBuilder().getPetType(i);
            if (type == petType) {
                selectedPet = getDb_data().getSelectedPetBuilder().getPetDataBuilder(i);
                break;
            }
        }
        if (selectedPet == null) {
            selectedPet = DB_SelectedPet.newBuilder();
            getDb_data().getSelectedPetBuilder().addPetType(petType);
            getDb_data().getSelectedPetBuilder().addPetData(selectedPet);
        } else {
            for (int i = 0; i < selectedPet.getSelectPetDataCount(); i++) {
                SelectedPetIndex petData = selectedPet.getSelectPetData(i);
                if (petData.getPetId() == petCfgId) {
                    selectedPet.removeSelectPetData(i);
                    break;
                }
            }
        }
        return RetCodeEnum.RCE_Success;
    }

    public void addReportTimes(int times) {
        if (times <= 0) {
            return;
        }
        getDb_data().setTodayReportTimes(getDb_data().getTodayReportTimes() + times);

        SC_RefreshTodayReportTimes.Builder builder = SC_RefreshTodayReportTimes.newBuilder();
        builder.setNewTimes(getDb_data().getTodayReportTimes());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_RefreshTodayReportTimes_VALUE, builder);
    }

    public void sendUpdatePushSetting() {
        SC_UpdatePushSetting.Builder msg = SC_UpdatePushSetting.newBuilder().setOpenPush(getDb_data().getPushOpen());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdatePushSetting_VALUE, msg);
    }

    public void sendArtifactUpdate(int artifactId) {
        SC_ArtifactUpdate.Builder msg = SC_ArtifactUpdate.newBuilder();
        for (Artifact artifact : getDb_data().getArtifactList()) {
            if (artifact.getArtifactId() == artifactId) {
                msg.setArtifactUpdate(artifact);
                break;
            }
        }
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_ArtifactUpdate_VALUE, msg);
    }

    /**
     * ===========================无尽尖塔  start==============================
     */


    public void sendPlayerCurRecharge() {
        SC_PlayerCurRecharge.Builder msg = SC_PlayerCurRecharge.newBuilder().setTodayRecharge(getDb_data().getTodayRecharge());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_PlayerCurRecharge_VALUE, msg);
    }

    /**
     * ===========================无尽尖塔  end==============================
     */

    public void sendRefreshTitleMsg() {
        SC_UpdateTitle.Builder builder = SC_UpdateTitle.newBuilder();
        builder.setNewTitleId(getTitleId());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdateTitle_VALUE, builder);
    }

    public void sendTheWarBuyBackTimes() {
        SC_UpdateBuyBackTimes.Builder builder = SC_UpdateBuyBackTimes.newBuilder();
        builder.setTimes(getDb_data().getTheWarData().getDailyBuyBackCount());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdateBuyBackTimes_VALUE, builder);
    }

    public void sendTheWarBuyStaminaTimes() {
        SC_UpdateBuyStamiaTimes.Builder builder = SC_UpdateBuyStamiaTimes.newBuilder();
        builder.setTimes(getDb_data().getTheWarData().getDailyBuyStaminaCount());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_UpdateBuyStamiaTimes_VALUE, builder);
    }

    public List<PlayerBase.SimpleArtifact> getSimpleArtifact() {
        List<PlayerBase.SimpleArtifact> artifacts = new ArrayList<>();
        for (protocol.PlayerInfo.Artifact artifact : getDb_data().getArtifactList()) {
            PlayerBase.SimpleArtifact.Builder simpleArtifact = PlayerBase.SimpleArtifact.newBuilder();
            simpleArtifact.setArtifactId(artifact.getArtifactId()).setStarLv(artifact.getPlayerSkill().getSkillLv()).setEnhanceLv(getArtifactEnhanceLv(artifact));
            artifacts.add(simpleArtifact.build());
        }
        return artifacts;
    }

    private int getArtifactEnhanceLv(Artifact artifact) {
        int enhanceLv = 1;
        if (artifact.getEnhancePointCount() < GameConst.Artifact_Enhance_Point_Num) {
            return enhanceLv;
        }
        int max = artifact.getEnhancePointList().stream().mapToInt(ArtifactEnhancePoint::getPointLevel).max().orElse(0);
        int min = artifact.getEnhancePointList().stream().mapToInt(ArtifactEnhancePoint::getPointLevel).min().orElse(0);
        return max == min ? max + 1 : max;
    }


    /**
     * ======================新称号系统 start===============
     */
    public void addNewTitles(List<Integer> newTitleList, Reason reason) {
        if (CollectionUtils.isEmpty(newTitleList)) {
            return;
        }
        NewTitle.Builder newTitleBuilder = getDb_data().getNewTitleBuilder();
        long currentTime = GlobalTick.getInstance().getCurrentTime();

        List<NewTitleInfo> refresh = new ArrayList<>();
        for (Integer newTitleId : newTitleList) {
            NewTitleSytemConfigObject titleConfig = NewTitleSytemConfig.getById(newTitleId);
            if (titleConfig == null) {
                LogUtil.error("playerEntity.addNewTitles, title cfg is not exist, cfg id:" + newTitleId);
                continue;
            }

            NewTitleInfo.Builder newBuilder = null;
            boolean needInvokeAdd = false;
            for (NewTitleInfo.Builder newTitleInfo : newTitleBuilder.getInfoBuilderList()) {
                if (newTitleInfo.getCfgId() == newTitleId) {
                    newBuilder = newTitleInfo;
                    break;
                }
            }
            if (newBuilder == null) {
                newBuilder = NewTitleInfo.newBuilder().setCfgId(newTitleId);
                needInvokeAdd = true;
            }

            long newExpireTime = titleConfig.getLimittime() == -1 ?
                    -1 : currentTime + titleConfig.getLimittime() * TimeUtil.MS_IN_A_S;

            if (newBuilder.getExpireStamp() != newExpireTime) {
                newBuilder.setExpireStamp(newExpireTime);

                refresh.add(newBuilder.build());

                if (needInvokeAdd) {
                    newTitleBuilder.addInfo(newBuilder);
                }
            }
        }

        sortNewTitle();

        if (CollectionUtils.isNotEmpty(refresh)) {
            sendAddNewTitleMsg(refresh);

            refreshNewTitleAddition();
        }
    }

    private void sortNewTitle() {
        NewTitle.Builder titleBuilder = getDb_data().getNewTitleBuilder();

        List<NewTitleInfo> sortedList = new ArrayList<>(titleBuilder.getInfoList());
        sortedList.sort((e1, e2) -> {
            if (e1.getExpireStamp() == -1 && e2.getExpireStamp() == -1) {
                return 0;
            }
            if (e1.getExpireStamp() == -1) {
                return 1;
            }
            if (e2.getExpireStamp() == -1) {
                return -1;
            }
            return Long.compare(e1.getExpireStamp(), e2.getExpireStamp());
        });

        titleBuilder.clearInfo();
        titleBuilder.addAllInfo(sortedList);
    }

    public void sendAddNewTitleMsg(List<NewTitleInfo> newTitleInfos) {
        if (CollectionUtils.isEmpty(newTitleInfos)) {
            return;
        }
        SC_AddNewTitle.Builder builder = SC_AddNewTitle.newBuilder().addAllNewTitles(newTitleInfos);
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_AddNewTitle_VALUE, builder);
    }

    public void sendNewTitleExpire(List<Integer> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        SC_NewTitleExpire.Builder builder = SC_NewTitleExpire.newBuilder().addAllTitleIds(list);
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_NewTitleExpire_VALUE, builder);
    }

    private void removeExpireNewTitle() {
        if (!isOnline() || getDb_data().getNewTitleBuilder().getInfoCount() <= 0) {
            return;
        }
        NewTitle.Builder titleBuilder = getDb_data().getNewTitleBuilder();

        NewTitleInfo.Builder firstInfo = titleBuilder.getInfoBuilder(0);
        if (firstInfo.getExpireStamp() != -1
                && firstInfo.getExpireStamp() <= GlobalTick.getInstance().getCurrentTime()) {
            titleBuilder.removeInfo(0);

            if (firstInfo.getCfgId() == titleBuilder.getCurEquip()) {
                titleBuilder.clearCurEquip();
            }

            List<Integer> singletonList = Collections.singletonList(firstInfo.getCfgId());
            sendNewTitleExpire(singletonList);
            addNewTitleExpireMail(singletonList);

            refreshNewTitleAddition();
        }
    }

    private void addNewTitleExpireMail(List<Integer> expireTitleList) {
        if (CollectionUtils.isEmpty(expireTitleList)) {
            return;
        }

        String totalExpireName = expireTitleList.stream()
                .distinct()
                .map(e -> {
                    NewTitleSytemConfigObject titleCfg = NewTitleSytemConfig.getById(e);
                    return titleCfg == null ?
                            "" : ServerStringRes.getContentByLanguage(titleCfg.getServername(), getLanguage());
                })
                .reduce((e1, e2) -> e1 + "," + e2)
                .orElse("");

        int mailTemplate = MailTemplateUsed.getById(GameConst.CONFIG_ID).getNewtitleexpire();
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Expire);
        EventUtil.triggerAddMailEvent(getIdx(), mailTemplate, Collections.emptyList(), reason, totalExpireName);
    }

    public int getCurEquipNewTitleId() {
        return getDb_data().getNewTitle().getCurEquip();
    }

    /**
     * 此处只计算称号相关加成
     */
    private void refreshNewTitleAddition() {
        Map<Integer, Integer> additionMap = calculateNewTitleAddition();

        GlobalAddition.Builder globalAdditionBuilder = getDb_data().getGlobalAdditionBuilder();

        //刷新战力变化
        EventUtil.triggerAllPetAdditionUpdate(getIdx(), globalAdditionBuilder.getNewTitleAdditionMap(), additionMap, 2);

        globalAdditionBuilder.clearNewTitleAddition();
        globalAdditionBuilder.putAllNewTitleAddition(additionMap);

        globalAdditionBuilder.clearNewTitleAbilityAddition();
        globalAdditionBuilder.setNewTitleAbilityAddition(calculateAbilityAddition(additionMap));

        calculateTotalGlobalAddition(isOnline());
    }

    private Map<Integer, Integer> calculateNewTitleAddition() {
        Map<Integer, Integer> result = new HashMap<>();
        getDb_data().getNewTitle().getInfoList().forEach(e -> {
            NewTitleSytemConfigObject newTitleConfig = NewTitleSytemConfig.getById(e.getCfgId());
            if (newTitleConfig == null) {
                return;
            }
            for (int[] ints : newTitleConfig.getAddproperty()) {
                if (ints.length < 2) {
                    continue;
                }
                result.compute(ints[0], (key, oldVal) -> oldVal == null ? ints[1] : ints[1] + oldVal);
            }
        });
        return result;
    }

    public List<Integer> getPlayerAllTitleIds() {
        return getDb_data().getNewTitle().getInfoList().stream().map(NewTitleInfo::getCfgId).collect(Collectors.toList());
    }


    /**
     * ======================新称号系统 end===============
     */


    /**
     * ======================活动奖励领取保底 start===============
     */


    public boolean alreadyClaimed(long activityId, int index) {
        return !canClaim(activityId, index, 1, 1);
    }


    /**
     * @param activityId    活动id
     * @param index         奖励索引
     * @param curClaimCount 当前领取次数
     * @param total         总次数(-1无限制)
     * @return
     */
    public boolean canClaim(long activityId, int index, int curClaimCount, int total) {
        if (total == -1) {
            return true;
        }
        Map<Integer, Integer> map = claimedMap.computeIfAbsent(activityId, a -> new ConcurrentHashMap<>());
        return (int) ObjectUtils.defaultIfNull(map.get(index), 0) <= (total - curClaimCount);
    }


    public void addPlayerRewardRecord(Long activityId, int index, int addCount) {
        LogUtil.info("playerId:{} addPlayerRewardRecord  on playerEntity by activityId:{},index:{},addCount:{}", getIdx(), activityId, index, addCount);
        SyncExecuteFunction.executeConsumer(this, p -> {
            Map<Integer, Integer> map = claimedMap.computeIfAbsent(activityId, a -> new ConcurrentHashMap<>());
            Integer record = map.computeIfAbsent(index, a -> 0);
            map.put(index, record + addCount);
        });
    }


    public void increasePlayerRewardRecord(long activityId, int index) {
        addPlayerRewardRecord(activityId, index, 1);
    }


    public void clearActivitiesData(long activityId) {
        claimedMap.remove(activityId);
    }

    public void updateDBClaimMap() {
        getDb_data().clearClaimEntry();

        for (Entry<Long, Map<Integer, Integer>> claimEntry : claimedMap.entrySet()) {
            getDb_data().addClaimEntry(buildClaimEntry(claimEntry));
        }
    }

    private PlayerDB.DB_ClaimRewardEntry.Builder buildClaimEntry(Entry<Long, Map<Integer, Integer>> claimEntry) {
        return PlayerDB.DB_ClaimRewardEntry.newBuilder().setActivityId(claimEntry.getKey()).setRecord(map2PatrolIntMap(claimEntry.getValue()));
    }

    private protocol.Common.IntMap.Builder map2PatrolIntMap(Map<Integer, Integer> map) {
        return protocol.Common.IntMap.newBuilder().addAllKeys(map.keySet()).addAllValues(map.values());
    }
    /**
     * ======================活动奖励领取保底 end===============
     */

    /**
     * ======================资源回收 start===============
     */


    public int calculateResourceRecycleOfflineDay() {
        long loginTime = getLogintime() == null ? 0 : getLogintime().getTime();
        long logoutTime = getLogouttime() == null ? 0 : getLogouttime().getTime();
        if (loginTime > logoutTime) {
            return 0;
        }
        return (int) ((TimeUtil.getTodayStamp(logoutTime) - TimeUtil.getTodayStamp(loginTime)) / TimeUtil.MS_IN_A_DAY);
    }

    public void addResRecycle(EnumFunction function, List<Reward> rewards) {
        PlayerDB.DB_ResourceRecycle.Builder ResourceRecycle = getDb_data().getResourceRecycleBuilder();
        PlayerDB.DB_ResourceRecycleItem.Builder builder = pullOneResourceRecycleItem(function);

        boolean gteMaxRecycleDays = builder.getRecycleInfoCount() >= GameConfig.getById(GameConst.CONFIG_ID).getResmaxrecycledays();
        if (gteMaxRecycleDays) {
            replaceResRecycleNewestReward(function, rewards, builder);
        } else {
            addResReward2Db(function, rewards, builder);
        }
        ResourceRecycle.addFunctionRecycle(builder);
    }

    private void replaceResRecycleNewestReward(EnumFunction function, List<Reward> rewards, PlayerDB.DB_ResourceRecycleItem.Builder builder) {
        //移除首位最早保存的奖励
        builder.removeRecycleInfo(0);
        //加入最新奖励到末端
        builder.addRecycleInfo(buildDB_OnceResourceCycleInfo(function, rewards));
    }

    private PlayerDB.DB_OnceResourceCycleInfo.Builder buildDB_OnceResourceCycleInfo(EnumFunction function, List<Reward> rewards) {
        PlayerDB.DB_OnceResourceCycleInfo.Builder info = PlayerDB.DB_OnceResourceCycleInfo.newBuilder();
        if (CollectionUtils.isEmpty(rewards)) {
            return info;
        }
        info.addReward(RewardUtil.rewards2RewardList(rewards));
        Common.Consume baseConsume = ResourceRecycleCfg.getInstance().getBaseConsume(function);
        Common.Consume advancedConsume = ResourceRecycleCfg.getInstance().getAdvancedConsume(function);
        if (baseConsume != null) {
            info.addBaseConsume(baseConsume);
        }
        if (advancedConsume != null) {
            info.addAdvancedConsume(advancedConsume);
        }
        return info;
    }


    private void addResReward2Db(EnumFunction function, List<Reward> rewards, PlayerDB.DB_ResourceRecycleItem.Builder builder) {
        builder.addRecycleInfo(buildDB_OnceResourceCycleInfo(function, rewards));
    }


    private PlayerDB.DB_ResourceRecycleItem.Builder pullOneResourceRecycleItem(EnumFunction function) {
        PlayerDB.DB_ResourceRecycleItem.Builder result;
        for (int i = 0; i < getDb_data().getResourceRecycle().getFunctionRecycleList().size(); i++) {
            result = getDb_data().getResourceRecycleBuilder().getFunctionRecycleBuilder(i);
            if (function == result.getFunction()) {
                getDb_data().getResourceRecycleBuilder().removeFunctionRecycle(i);
                return result;
            }

        }

        return PlayerDB.DB_ResourceRecycleItem.newBuilder().setFunction(function);
    }


    /**
     * 查询玩家资源副本最大可扫荡的层数
     *
     * @param resType
     * @return 层数 0层表未解锁
     */
    public int queryCanMaxSweepProgress(int resType) {
        PlayerDB.DB_ResourceCopy.Builder resourceCopy = getResourceCopyData(resType);
        if (resourceCopy == null || CollectionUtils.isEmpty(resourceCopy.getProgressList())) {
            return 0;
        }
        return resourceCopy.getProgressList().stream().max(Integer::compareTo).orElse(0);
    }

    public Common.Consume getResourceCycleConsume(int claimType) {
        if (claimType != 1 && claimType != 0) {
            return null;
        }
        return mergeResourceCycleConsume(claimType);


    }

    private Common.Consume mergeResourceCycleConsume(int claimType) {
        PlayerDB.DB_ResourceRecycle.Builder source = getDb_data().getResourceRecycleBuilder();

        List<Common.Consume> consumes = new ArrayList<>();
        for (PlayerDB.DB_ResourceRecycleItem item : source.getFunctionRecycleList()) {
            for (PlayerDB.DB_OnceResourceCycleInfo info : item.getRecycleInfoList()) {
                if (GameConst.ResourceCycleClaimType.base == claimType) {
                    consumes.addAll(info.getBaseConsumeList());
                }
                if (GameConst.ResourceCycleClaimType.advanced == claimType) {
                    consumes.addAll(info.getAdvancedConsumeList());
                }
            }
        }
        consumes = ConsumeUtil.mergeConsume(consumes);
        if (CollectionUtils.isEmpty(consumes)) {
            return null;
        }
        return consumes.get(0);
    }

    public List<Reward> queryResourceRecycleFullRewards() {
        PlayerDB.DB_ResourceRecycle ResourceRecycle = getDb_data().getResourceRecycle();

        List<Common.Reward> rewards = new ArrayList<>();
        for (PlayerDB.DB_ResourceRecycleItem item : ResourceRecycle.getFunctionRecycleList()) {
            rewards.addAll(ResourceRecycleHelper.parseResourceReward(item));
        }
        return rewards;
    }

    public void sendResourceRecycleInfo() {
        ResourceRecycle.SC_ClaimResourceRecycleInfo.Builder msg = ResourceRecycle.SC_ClaimResourceRecycleInfo.newBuilder();
        setClientRewards(msg);

        setBaseConsume(msg);

        setAdvancedConsume(msg);

        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_ClaimResourceRecycleInfo_VALUE, msg);
    }

    private void setAdvancedConsume(ResourceRecycle.SC_ClaimResourceRecycleInfo.Builder msg) {
        Common.Consume advancedConsume = getResourceCycleConsume(GameConst.ResourceCycleClaimType.advanced);
        if (advancedConsume != null) {
            msg.setAdvancedConsume(advancedConsume);
        }
    }

    private void setBaseConsume(ResourceRecycle.SC_ClaimResourceRecycleInfo.Builder msg) {
        Common.Consume baseConsume = getResourceCycleConsume(GameConst.ResourceCycleClaimType.base);
        if (baseConsume != null) {
            msg.setBaseConsume(baseConsume);
        }
    }

    private void setClientRewards(ResourceRecycle.SC_ClaimResourceRecycleInfo.Builder msg) {
        msg.addAllRewards(getResourceCycleFullReward());
    }

    public List<protocol.ResourceRecycle.ClientResourceReward> getResourceCycleFullReward() {
        List<protocol.ResourceRecycle.ClientResourceReward> clientRewards = new ArrayList<>();
        protocol.ResourceRecycle.ClientResourceReward.Builder clientReward;
        List<Common.Reward> fullRewards;
        for (PlayerDB.DB_ResourceRecycleItem item : getDb_data().getResourceRecycleBuilder().getFunctionRecycleList()) {
            clientReward = protocol.ResourceRecycle.ClientResourceReward.newBuilder();
            fullRewards = ResourceRecycleHelper.parseResourceReward(item);
            if (org.springframework.util.CollectionUtils.isEmpty(fullRewards)) {
                continue;
            }
            clientReward.addAllRewards(fullRewards);
            clientReward.setFunction(item.getFunction());
            clientRewards.add(clientReward.build());
        }
        return clientRewards;
    }

    /**
     * ======================资源回收 end===============
     */


    /**
     * ======================图鉴 start===============
     */

    public int calculateCanClaimCollectionExp(protocol.Collection.CollectionType collectionType) {
        PlayerDB.DB_Collection.Builder collection = getDb_data().getCollectionBuilder();
        switch (collectionType) {
            case CT_PET:
                ProtocolStringList expIdList = collection.getCanClaimedPetExpIdList();
                return PetCollectExpCfg.getInstance().calculateCanClaimCollectionExp(expIdList);
            case CT_Artifact:
                return collection.getCanClaimArtifactExpMap().values()
                        .stream().mapToInt(Integer::intValue).sum();
            case CT_Link:
                return collection.getCanClaimLinkExpMap().values()
                        .stream().mapToInt(Integer::intValue).sum();
            default:
                return 0;
        }
    }

    public List<protocol.Collection.ArtifactExp> getCanClaimArtifactExp() {
        Map<Integer, Integer> expMap = getDb_data().getCollection().getCanClaimArtifactExpMap();
        if (MapUtils.isEmpty(expMap)) {
            return Collections.emptyList();
        }
        List<protocol.Collection.ArtifactExp> result = new ArrayList<>();
        expMap.forEach((k, v) -> result.add(protocol.Collection.ArtifactExp.newBuilder()
                .setCfgId(k).setExp(v).build()));
        return result;
    }

    public List<protocol.Collection.LinkExp> getLinkExp() {
        Map<Integer, Integer> expMap = getDb_data().getCollection().getCanClaimLinkExpMap();
        if (MapUtils.isEmpty(expMap)) {
            return Collections.emptyList();
        }
        List<protocol.Collection.LinkExp> result = new ArrayList<>();
        expMap.forEach((k, v) -> result.add(protocol.Collection.LinkExp.newBuilder()
                .setCfgId(k).setExp(v).build()));
        return result;
    }

    public void settleCollectionExp() {
        PlayerDB.DB_Collection.Builder db_collection = getDb_data().getCollectionBuilder();
        int beforeLv = db_collection.getCollectionLv();
        int collectionExp = db_collection.getCollectionExp();
        int nowLv = beforeLv;
        int upLvCostEp;
        while (collectionExp >= (upLvCostEp = PetCollectLvCfg.getInstance().getUpLvExp(nowLv))) {
            collectionExp -= upLvCostEp;
            nowLv++;
        }
        db_collection.setCollectionLv(nowLv).setCollectionExp(collectionExp);
        if (nowLv != beforeLv) {
            EventUtil.triggerAllPetAdditionUpdate(getIdx(),
                    PetCollectLvCfg.getInstance().getAdditionMap(beforeLv),
                    PetCollectLvCfg.getInstance().getAdditionMap(nowLv), 3);
            calculateTotalGlobalAddition(true);

            EventUtil.triggerUpdateTargetProgress(getIdx(), TargetTypeEnum.TTE_Collection_CumuLvUp, nowLv, 0);
        }
        LogUtil.info("player:{} Collection lv up,beforeLv:{},now lv:{},now exp:{}", idx, beforeLv, nowLv, collectionExp);
    }

    /**
     * 推送宠物收集进度更新
     *
     * @param newPetBookIds 新增宠物bookIds
     * @param addLinkIds    新增链接ids
     */
    public void sendPetCollectionUpdate(List<Integer> newPetBookIds, List<Integer> addLinkIds) {
        SC_CollectionUpdate.Builder result = SC_CollectionUpdate.newBuilder();
        PlayerDB.DB_Collection.Builder db_collection = getDb_data().getCollectionBuilder();
        if (!CollectionUtils.isEmpty(newPetBookIds)) {
            result.addAllNewPetBookIds(newPetBookIds);
        }
        if (!CollectionUtils.isEmpty(addLinkIds)) {
            result.addAllLinkIds(addLinkIds);
        }
        result.setCollection(petCache.queryCollectionCount(getIdx()));
        result.setCollectLv(db_collection.getCollectionLv());
        result.setCurExp(db_collection.getCollectionExp());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_CollectionUpdate_VALUE, result);
    }

    public void sendPetCollectionUpdate() {
        SC_CollectionUpdate.Builder result = SC_CollectionUpdate.newBuilder();
        PlayerDB.DB_Collection.Builder db_collection = getDb_data().getCollectionBuilder();
        result.setCollection(petCache.queryCollectionCount(getIdx()));
        result.setCollectLv(db_collection.getCollectionLv());
        result.setCurExp(db_collection.getCollectionExp());
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_CollectionUpdate_VALUE, result);
    }

    public void sendAddCollectionExp(protocol.Collection.ArtifactExp.Builder artifactExp, List<Integer> addLinkId) {
        protocol.Collection.SC_AddCollectionExp.Builder msg = protocol.Collection.SC_AddCollectionExp.newBuilder();
        if (artifactExp != null) {
            msg.addArtifactExp(artifactExp);
        }
        List<protocol.Collection.LinkExp> linkExps = LinkConfig.convertToLinkExp(addLinkId);
        if (!CollectionUtils.isEmpty(linkExps)) {
            msg.addAllLinkExp(linkExps);
        }
        GlobalData.getInstance().sendMsg(getIdx(), SC_AddCollectionExp_VALUE, msg);
    }


    public void collectPetExp(Collection<PetMessage.Pet> pets, boolean oldData) {
        Map<Integer, Set<Integer>> rarityBookIdsMap = petList2rarityBookIdsMap(pets);
        PlayerDB.DB_Collection.Builder collectionBuilder = getDb_data().getCollectionBuilder();
        Set<String> addCfgIds = rarityBookIdsMap2CollectExpIdList(rarityBookIdsMap, oldData);
        collectionBuilder.getActivePetExpIdList().forEach(addCfgIds::remove);
        if (CollectionUtils.isEmpty(addCfgIds)) {
            return;
        }
        collectionBuilder.addAllActivePetExpId(addCfgIds);
        collectionBuilder.addAllCanClaimedPetExpId(addCfgIds);
        sendPetCollectExpUpdate(addCfgIds);
        RedPointManager.getInstance().sendRedPoint(getIdx(), RedPointId.ALBUM_PET_VALUE, RedPointOptionEnum.ADD);
    }


    public void collectPetExpAndLink(List<PetMessage.Pet> pets) {
        collectNewPet(pets);
        collectPetExp(pets, false);
        unlockLink(pets);
    }

    private void collectNewPet(List<PetMessage.Pet> pets) {
        PlayerDB.DB_Collection.Builder collectionBuilder = getDb_data().getCollectionBuilder();
		boolean addRedPoint = false;
        for (PetMessage.Pet pet : pets) {
            if (!collectionBuilder.getCfgIdList().contains(pet.getPetBookId())) {
                collectionBuilder.addCfgId(pet.getPetBookId());
				addRedPoint = true;
            }
        }
		if (addRedPoint) {
        	RedPointManager.getInstance().sendRedPoint(getIdx(), RedPointId.ALBUM_PET_VALUE, RedPointOptionEnum.ADD);
		}
    }

    public void collectPetExp(Collection<PetMessage.Pet> pets) {
        collectPetExp(pets, false);
    }

    private void sendPetCollectExpUpdate(Set<String> addCfgIds) {
        protocol.Collection.SC_AddCollectionExp.Builder msg = protocol.Collection.SC_AddCollectionExp.newBuilder();
        msg.addAllCollectionExp(collectionCfgIds2CollectionExpList(addCfgIds));
        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_AddCollectionExp_VALUE, msg);
    }

    public static List<protocol.Collection.CollectionPetExp> collectionCfgIds2CollectionExpList(Collection<String> addCfgIds) {
        if (org.springframework.util.CollectionUtils.isEmpty(addCfgIds)) {
            return Collections.emptyList();
        }
        List<protocol.Collection.CollectionPetExp> result = new ArrayList<>();
        for (String addCfgId : addCfgIds) {
            protocol.Collection.CollectionPetExp addExp = PetCollectExpCfg.getCollectExp(addCfgId);
            if (addExp != null) {
                result.add(addExp);
            }
        }
        return result;
    }

    private Set<String> rarityBookIdsMap2CollectExpIdList(Map<Integer, Set<Integer>> rarityBookIdsMap, boolean oldData) {
        if (org.springframework.util.CollectionUtils.isEmpty(rarityBookIdsMap)) {
            return Collections.emptySet();
        }
        Set<String> expIds = new HashSet<>();
        for (Entry<Integer, Set<Integer>> setEntry : rarityBookIdsMap.entrySet()) {
            Integer petRarity = setEntry.getKey();
            if (petRarity < PetCollectExpCfg.getInstance().getStartRarity()) {
                continue;
            }
            if (oldData) {
                //处理老数据要把宠物当前品质和之前品质都要加起来与宠物BookId组合
                combineOldPetCollectionIds(expIds, setEntry, petRarity);
            } else {
                //玩家获取新宠物只需要算当前品质当前宠物BookId
                for (Integer bookId : setEntry.getValue()) {
                    expIds.add(PetCollectExpCfg.rarityBookId2CollectCfgId(petRarity, bookId));
                }
            }
        }
        return expIds;
    }

    private void combineOldPetCollectionIds(Set<String> expIds, Entry<Integer, Set<Integer>> setEntry, Integer petRarity) {
        for (Integer collectionRarity : PetCollectExpCfg.getInstance().getCanClaimRarity()) {
            if (collectionRarity > petRarity) {
                continue;
            }
            for (Integer bookId : setEntry.getValue()) {
                if (PetBaseProperties.getStartRarityById(bookId) > collectionRarity) {
                    continue;
                }
                expIds.add(PetCollectExpCfg.rarityBookId2CollectCfgId(collectionRarity, bookId));
            }
        }
    }

    private Map<Integer, Set<Integer>> petList2rarityBookIdsMap(Collection<PetMessage.Pet> pets) {
        if (org.springframework.util.CollectionUtils.isEmpty(pets)) {
            return Collections.emptyMap();
        }
        Map<Integer, Set<Integer>> rarityBookIdsMap = new HashMap<>();
        for (PetMessage.Pet pet : pets) {
            Set<Integer> rarityBookIds = rarityBookIdsMap.computeIfAbsent(pet.getPetRarity(), a -> new HashSet<>());
            rarityBookIds.add(pet.getPetBookId());
        }
        return rarityBookIdsMap;
    }

    private void unlockLink(List<PetMessage.Pet> petList) {
        petEntity petEntity = petCache.getInstance().getEntityByPlayer(getIdx());
        if (petEntity == null) {
            return;
        }

        Set<Integer> petBookIds = petList.stream().map(PetMessage.Pet::getPetBookId).collect(Collectors.toSet());

        PlayerDB.DB_Collection.Builder collectionBuilder = getDb_data().getCollectionBuilder();
        List<Integer> activeLinkIdList = collectionBuilder.getCollectedLinkIdList();
        List<Integer> addLinkId = new ArrayList<>();
        for (LinkConfigObject cfg : LinkConfig._ix_id.values()) {
            if (canActiveLink(cfg, petBookIds, collectionBuilder.getCfgIdList(), activeLinkIdList)) {
                addLinkId.add(cfg.getId());
                getDb_data().getCollectionBuilder().putCanClaimLinkExp(cfg.getId(), cfg.getExp());
            }
        }
        if (!CollectionUtils.isEmpty(addLinkId)) {
            getDb_data().getCollectionBuilder().addAllCollectedLinkId(addLinkId);
            sendAddLinkId(addLinkId);
            sendPetCollectionUpdate(null, addLinkId);
            RedPointManager.getInstance().sendRedPoint(getIdx(), RedPointId.ALBUM_CHAIN_VALUE, RedPointOptionEnum.ADD);
        }
    }


    private void sendAddLinkId(List<Integer> addLinkId) {
        playerEntity player = playerCache.getByIdx(idx);
        if (player != null) {
            player.sendAddCollectionExp(null, addLinkId);
        }
    }

    private boolean canActiveLink(LinkConfigObject cfg, Collection<Integer> newPetBookIds, List<Integer> playerPets, List<Integer> activeLinkIdList) {
        if (activeLinkIdList.contains(cfg.getId())) {
            return false;
        }
        for (Integer petBookId : newPetBookIds) {
            if (ArrayUtils.contains(cfg.getNeedpet(), petBookId)
                    && Arrays.stream(cfg.getNeedpet()).allMatch(playerPets::contains)) {
                return true;
            }
        }
        return false;
    }

/*    public void settleOldData() {
        LogUtil.info("playerId:{} settle petEntity old data");
        collectPetExp(peekAllPetByUnModify(), true);
    }*/

    public void collectArtifactExp(int artifactId, int lastSkillLv, int nowSkillLv) {
        int starUpExp = ArtifactMapExpConfig.getStarUpExp(lastSkillLv, nowSkillLv);
        if (starUpExp <= 0) {
            return;
        }
        PlayerDB.DB_Collection.Builder collectionBuilder = getDb_data().getCollectionBuilder();
        Integer beforeExp = collectionBuilder.getCanClaimArtifactExpMap().get(artifactId);

        int curExp = beforeExp == null ? starUpExp : starUpExp + beforeExp;
        collectionBuilder.putCanClaimArtifactExp(artifactId, curExp);

        protocol.Collection.ArtifactExp.Builder artifactExp = protocol.Collection.ArtifactExp.newBuilder()
                .setCfgId(artifactId).setExp(curExp);

        sendAddCollectionExp(artifactExp, null);

        RedPointManager.getInstance().sendRedPoint(getIdx(), RedPointId.ALBUM_ARTIFACT_VALUE, RedPointOptionEnum.ADD);
    }


    /**
     * ======================图鉴 end===============
     */

    /**
     * 查询好友id
     *
     * @return
     */
    public Set<String> getFriendIds() {
        return getDb_data().getFriendInfo().getOwnedMap().keySet();
    }

    public long queryOfflineTime() {
        if (isOnline() || getLogouttime() == null) {
            return 0L;
        }
        return GlobalTick.getInstance().getCurrentTime() - getLogouttime().getTime();
    }

    /**
     * 查询玩家特权卡增加的功能次数
     *
     * @param function
     * @return
     */
    public int queryPrivilegedCardNum(MonthCard.PrivilegedCardFunction function) {
        List<PlayerDB.DB_PrivilegedCard> cardList = getDb_data().getRechargeCards().getPrivilegedCardList();
        return PrivilegedCardCfg.getInstance().queryPrivilegedNum(cardList, function);
    }

    /**
     * 1.连续登录一周以上,2.登录总时长＞5h，并且两次登录间距小于48h。以上条件满足一个即可
     *
     * @return
     */
    public boolean canRecommend() {
        return getDb_data().getCumuLoginDays() > 7
                || (getCumuOnline() > 5 * TimeUtil.MS_IN_A_HOUR / 1000 && getDb_data().getRecentLoginDiff() != 0 && getDb_data().getRecentLoginDiff() < 48 * TimeUtil.MS_IN_A_HOUR);
    }

    public int getBorderId() {
        return getDb_data().getCurAvatarBorder();
    }

}