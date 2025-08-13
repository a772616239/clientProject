package model.ranking;

import cfg.RankConfig;
import cfg.RankConfigObject;
import common.HttpRequestUtil;
import common.SyncExecuteFunction;
import common.entity.RankingQuerySingleResult;
import common.tick.GlobalTick;
import common.tick.Tickable;
import helper.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import model.activity.ActivityManager;
import model.ranking.ranking.AbilityRanking;
import model.ranking.ranking.AbstractRanking;
import model.ranking.ranking.AbyssPetAbilityRanking;
import model.ranking.ranking.ActivityBossDamageRanking;
import model.ranking.ranking.arena.ArenaDanAllServerRanking;
import model.ranking.ranking.arena.ArenaDanRanking;
import model.ranking.ranking.arena.ArenaGainScoreRanking;
import model.ranking.ranking.ConsumeCouponRanking;
import model.ranking.ranking.DemonDescendsScoreRanking;
import model.ranking.ranking.FestivalBossRanking;
import model.ranking.ranking.GloryRoadRanking;
import model.ranking.ranking.HellPetAbilityRanking;
import model.ranking.ranking.MainlineRanking;
import model.ranking.ranking.MatchArenaCrossRanking;
import model.ranking.ranking.MatchArenaLocalRanking;
import model.ranking.ranking.MineScoreRanking;
import model.ranking.ranking.NaturePetAbilityRanking;
import model.ranking.ranking.NewForeignInvasionRanking;
import model.ranking.ranking.PetAbilityRanking;
import model.ranking.ranking.PlayerLevelRanking;
import model.ranking.ranking.RankingTargetManager;
import model.ranking.ranking.RechargeCouponRanking;
import model.ranking.ranking.RichManRanking;
import model.ranking.ranking.SpireRanking;
import model.ranking.ranking.TargetRewardRanking;
import model.ranking.ranking.Team1Ranking;
import model.ranking.ranking.TheWarRanking;
import model.ranking.ranking.WildPetAbilityRanking;
import model.ranking.ranking.PetAvoidanceRanking;
import model.ranking.ranking.arena.ArenaLocalDanRanking;
import model.ranking.ranking.crossarena.CrossArenaDuelRanking;
import model.ranking.ranking.crossarena.CrossArenaScoreRanking;
import model.ranking.ranking.crossarena.CrossArenaSerialWinRanking;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Activity;
import protocol.Activity.EnumRankingType;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB.DB_DemonDescendsActivityInfo;
import server.handler.ranking.RankingEntranceDto;
import util.EventUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/12/17
 */
public class RankingManager implements Tickable {
    private static RankingManager instance;

    public static RankingManager getInstance() {
        if (instance == null) {
            synchronized (RankingManager.class) {
                if (instance == null) {
                    instance = new RankingManager();
                }
            }
        }
        return instance;
    }

    private RankingManager() {
    }

    /**
     * 用于保存排行榜数据
     */
    private final Map<String, AbstractRanking> rankingMap = new ConcurrentHashMap<>();

    public boolean init() {
        initShowClientRanking();
        for (EnumRankingType rankingType : showClientRanking) {
            //创建排行榜
            AbstractRanking ranking = getRanking(rankingType, RankingUtils.getRankingTypeDefaultName(rankingType));
            if (ranking==null){
                continue;
            }
            //处理老数据
            if (ranking.needSettleOldData(ranking)) {
                ranking.doSettleOldData();
            }
        }

        initRanking();
        return true;
    }

    private void initRanking() {
        getRanking(EnumRankingType.ERT_MatchArena_Local,
                RankingUtils.getRankingTypeDefaultName(EnumRankingType.ERT_MatchArena_Local));

        getRanking(EnumRankingType.ERT_MatchArena_Cross,
                RankingUtils.getRankingTypeDefaultName(EnumRankingType.ERT_MatchArena_Cross));
    }

    private void initShowClientRanking() {
        showClientRanking.clear();
        List<RankConfigObject> collect = RankConfig._ix_rankid.values().stream()
                .sorted(Comparator.comparingInt(RankConfigObject::getSort)).collect(Collectors.toList());

        for (RankConfigObject rankCfg : collect) {
            EnumRankingType rankingType = EnumRankingType.forNumber(rankCfg.getRankid());
            if (rankingType != null && !showClientRanking.contains(rankingType)) {
                showClientRanking.add(rankingType);
            }
        }
    }

    /**
     * 更新玩家分数,rankingType 的主要作用是排行榜不存在时,用于创建新排行榜的依据
     *
     * @param playerIdx    玩家Id
     * @param rankingName  排名
     * @param primaryScore 主分数(全量)
     * @param subScore     副分数(全量)
     */
    public void updatePlayerRankingScore(String playerIdx, EnumRankingType rankingType, String rankingName,
                                         long primaryScore, long subScore) {
        if (StringUtils.isEmpty(playerIdx) || StringUtils.isEmpty(rankingName) || rankingType == null) {
            return;
        }

        AbstractRanking ranking = getRanking(rankingType, rankingName);
        ranking.updatePlayerRankingScore(playerIdx, primaryScore, subScore);

        if (ranking instanceof TargetRewardRanking) {
            RankingTargetManager.getInstance().updateRankTarget(rankingType.getNumber(), playerIdx, primaryScore);
        }
    }

    public void updatePlayerRankingScore(String playerIdx, EnumRankingType rankingType, long primaryScore) {
        updatePlayerRankingScore(playerIdx, rankingType, RankingUtils.getRankingTypeDefaultName(rankingType), primaryScore);
    }

    public void triggerTargetRankingTarget(String playerIdx, EnumRankingType rankingType, long primaryScore) {
        if (StringUtils.isEmpty(playerIdx) || rankingType == null) {
            return;
        }

        AbstractRanking ranking = getRanking(rankingType, RankingUtils.getRankingTypeDefaultName(rankingType));
        ranking.updatePlayerRankingScore(playerIdx, primaryScore, GlobalTick.getInstance().getCurrentTime());

        if (ranking instanceof TargetRewardRanking) {
            RankingTargetManager.getInstance().updateRankTarget(rankingType.getNumber(), playerIdx, primaryScore);
        }
    }

    /**
     * 更新玩家分数
     *
     * @param playerIdx    玩家Id
     * @param rankingName  排名
     * @param primaryScore 主分数(全量)
     */
    public void updatePlayerRankingScore(String playerIdx, EnumRankingType rankingType, String rankingName, long primaryScore) {
        updatePlayerRankingScore(playerIdx, rankingType, rankingName, primaryScore, 0);
    }

    public void updatePlayerRankingScore(String playerIdx, EnumRankingType rankingType, long primaryScore, int subScore) {
        updatePlayerRankingScore(playerIdx, rankingType, RankingUtils.getRankingTypeDefaultName(rankingType), primaryScore, subScore);
    }

    /**
     * 清除玩家排行榜数据
     * @param playerIdx
     * @param rankingName
     */
    public void clearPlayerRanking(String playerIdx, String rankingName) {
        AbstractRanking ranking = getRanking(rankingName);
        if (ranking != null) {
            ranking.clearRankingMember(playerIdx);
        }
    }

    public AbstractRanking getRanking(String rankingName) {
        if (StringUtils.isEmpty(rankingName)) {
            return null;
        }
        AbstractRanking ranking = this.rankingMap.get(rankingName);
        if (ranking == null) {
            LogUtil.error("RankingManager.getRanking, ranking is not exist, ranking name:" + rankingName);
        }
        return ranking;
    }


    /**
     * 根据指定rankingName获取,当排行榜不存在时会更具rankingType创建
     *
     * @return
     */
    public AbstractRanking getRanking(EnumRankingType rankingType, String rankingName) {
        if (rankingType == null || StringUtils.isEmpty(rankingName)) {
            return null;
        }

        AbstractRanking ranking = this.rankingMap.get(rankingName);
        if (ranking == null) {
            ranking = createNewRanking(rankingType, rankingName);

            if (ranking != null) {
                this.rankingMap.put(rankingName, ranking);
            } else {
                LogUtil.error("RankingManager.getRanking, can not create new ranking, ranking type:" + rankingType);
            }
        }
        return ranking;
    }

    /**
     * 根据排行榜类型创建的对应管理的排行榜
     */
    private static final Map<EnumRankingType, Supplier<AbstractRanking>> RANKING_SUPPLIER_MAP;

    private static final List<EnumRankingType> showClientRanking = new ArrayList<>();

    static {
        Map<EnumRankingType, Supplier<AbstractRanking>> tempMap = new HashMap<>();
        tempMap.put(EnumRankingType.ERT_Ability, AbilityRanking::new);
        tempMap.put(EnumRankingType.ERT_PetAbility, PetAbilityRanking::new);
        tempMap.put(EnumRankingType.ERT_PlayerLevel, PlayerLevelRanking::new);
        tempMap.put(EnumRankingType.ERT_MainLine, MainlineRanking::new);
        tempMap.put(EnumRankingType.ERT_Spire, SpireRanking::new);
        tempMap.put(EnumRankingType.ERT_ArenaScoreLocal, ArenaDanRanking::new);
        tempMap.put(EnumRankingType.ERT_ArenaScoreCross, ArenaDanAllServerRanking::new);
        tempMap.put(EnumRankingType.ERT_ArenaGainScore, ArenaGainScoreRanking::new);
        tempMap.put(EnumRankingType.ERT_ArenaScoreLocalDan, ArenaLocalDanRanking::new);
        tempMap.put(EnumRankingType.ERT_MineScore, MineScoreRanking::new);
        tempMap.put(EnumRankingType.ERT_ActivityBoss_Damage, ActivityBossDamageRanking::new);
        tempMap.put(EnumRankingType.ERT_DemonDescendsScore, DemonDescendsScoreRanking::new);
        tempMap.put(EnumRankingType.ERT_NewForeignInvasion, NewForeignInvasionRanking::new);
        tempMap.put(EnumRankingType.ERT_TheWar_KillMonster, TheWarRanking::new);
        tempMap.put(EnumRankingType.ERT_AbyssPet, AbyssPetAbilityRanking::new);
        tempMap.put(EnumRankingType.ERT_HellPet, HellPetAbilityRanking::new);
        tempMap.put(EnumRankingType.ERT_NaturePet, NaturePetAbilityRanking::new);
        tempMap.put(EnumRankingType.ERT_Team1Ability, Team1Ranking::new);
        tempMap.put(EnumRankingType.ERT_WildPet, WildPetAbilityRanking::new);
        tempMap.put(EnumRankingType.ERT_GloryRoad, GloryRoadRanking::new);
        tempMap.put(EnumRankingType.ERT_RichMan, RichManRanking::new);
        tempMap.put(EnumRankingType.ERT_FestivalBoss, FestivalBossRanking::new);
        tempMap.put(EnumRankingType.ERT_MatchArena_Local, MatchArenaLocalRanking::new);
        tempMap.put(EnumRankingType.ERT_MatchArena_Cross, MatchArenaCrossRanking::new);
        tempMap.put(EnumRankingType.ERT_Lt_Duel, CrossArenaDuelRanking::new);
        tempMap.put(EnumRankingType.ERT_Lt_Score, CrossArenaScoreRanking::new);
        tempMap.put(EnumRankingType.ERT_Lt_SerialWin, CrossArenaSerialWinRanking::new);
        tempMap.put(EnumRankingType.ERT_Recharge, RechargeCouponRanking::new);
        tempMap.put(EnumRankingType.ERT_ConsumeCoupon, ConsumeCouponRanking::new);
        tempMap.put(EnumRankingType.ERT_PetAvoidance, PetAvoidanceRanking::new);
        RANKING_SUPPLIER_MAP = Collections.unmodifiableMap(tempMap);
    }

    /**
     * @param rankingType
     * @param rankingName
     * @return
     */
    private AbstractRanking createNewRanking(EnumRankingType rankingType, String rankingName) {
        if (rankingType == null || StringUtils.isEmpty(rankingName)) {
            return null;
        }

        Supplier<AbstractRanking> supplier = RANKING_SUPPLIER_MAP.get(rankingType);
        if (supplier == null) {
            LogUtil.error("RankingManager.createNewRanking, ranking type:" + rankingType + ", not have match supplier");
            return null;
        }

        AbstractRanking ranking = supplier.get();
        ranking.setRankingName(rankingName);
        ranking.setRankingType(rankingType);
        ranking.init();
        ranking.updateRanking();
        return ranking;
    }

    public List<RankingQuerySingleResult> getRankingTotalInfo(EnumRankingType rankingType, String rankingName) {
        AbstractRanking ranking = getRanking(rankingType, rankingName);
        if (ranking == null) {
            return null;
        }
        return ranking.getRankingTotalInfoList();
    }

    public List<RankingQuerySingleResult> getRankingTotalInfo(EnumRankingType rankingType) {
        return getRankingTotalInfo(rankingType, RankingUtils.getRankingTypeDefaultName(rankingType));
    }

    public void clearRanking(EnumRankingType rankingType, String rankingName) {
        AbstractRanking ranking = getRanking(rankingType, rankingName);
        if (ranking == null) {
            return;
        }
        if (!ranking.clearRanking()) {
            LogUtil.error("RankingManager.clearRanking, clear ranking name failed, rankingName:" + rankingName);
        }
    }

    public void clearRanking(EnumRankingType rankingType) {
        clearRanking(rankingType, RankingUtils.getRankingTypeDefaultName(rankingType));
    }

    /**
     * 根据排名获取排行榜信息   1 ~
     *
     * @param rankingNum
     */
    public RankingQuerySingleResult getPlayerInfoByRanking(EnumRankingType rankingType, String rankingName, int rankingNum) {
        if (rankingNum <= 0) {
            return null;
        }
        AbstractRanking ranking = getRanking(rankingType, rankingName);
        if (ranking == null) {
            return null;
        }
        return ranking.getPlayerInfoByRanking(rankingNum);
    }

    /**
     * 根据排名获取排行榜信息   1 ~
     *
     * @param rankingNum
     */
    public RankingQuerySingleResult getPlayerInfoByRanking(EnumRankingType rankingType, int rankingNum) {
        return getPlayerInfoByRanking(rankingType, RankingUtils.getRankingTypeDefaultName(rankingType), rankingNum);
    }

    /**
     * 直接更新排行榜不管是否更新间隔
     */
    public void directUpdateRanking(EnumRankingType rankingType, String rankingName) {
        AbstractRanking ranking = getRanking(rankingType, rankingName);
        if (ranking != null) {
            ranking.updateRanking();
        }
    }

    @Override
    public void onTick() {
        for (AbstractRanking value : rankingMap.values()) {
            if (value.needUpdate()) {
                EventUtil.updateRanking(value);
            }
          /*  if(value.needSendNewRankData()){
                value.sendNewRankData();
            }*/
        }
    }

    public RankingQuerySingleResult getPlayerRankingResult(String rankingName, String playerIdx) {
        AbstractRanking ranking = getRanking(rankingName);
        return ranking == null ? null : ranking.queryPlayerRankingResult(playerIdx);
    }

    public void stopUpdatePlayerScore(String rankingName) {
        AbstractRanking ranking = getRanking(rankingName);
        if (ranking == null) {
            return;
        }
        synchronized (ranking) {
            ranking.stopUpdatePlayerScore();
        }
    }

    public void startUpdatePlayerScore(String rankingName) {
        AbstractRanking ranking = getRanking(rankingName);
        if (ranking == null) {
            return;
        }
        synchronized (ranking) {
            ranking.startUpdatePlayerScore();
        }
    }

    public int queryPlayerIntScore(String rankingName, String playerIdx) {
        RankingQuerySingleResult result = queryPlayerRankingResult(rankingName, playerIdx);
        return result == null ? 0 : result.getIntPrimaryScore();
    }

    public int queryPlayerRanking(String rankingName, String playerIdx) {
        RankingQuerySingleResult result = queryPlayerRankingResult(rankingName, playerIdx);
        return result == null ? -1 : result.getRanking();
    }

    public RankingQuerySingleResult queryPlayerRankingResult(String rankingName, String playerIdx) {
        AbstractRanking ranking = getRanking(rankingName);
        return ranking == null ? null : ranking.queryPlayerRankingResult(playerIdx);
    }

    /**
     * @param senderType  以什么类型的send发送排行榜消息
     * @param rankingType 发送的目标排行榜类型
     * @param rankingName 发送的目标排行榜类型
     * @param playerIdx   需要发送到的玩家
     */
    public void sendRankingInfoToPlayer(EnumRankingSenderType senderType, EnumRankingType rankingType, String rankingName, String playerIdx) {
        AbstractRanking ranking = getRanking(rankingType, rankingName);
        if (ranking == null) {
            return;
        }
        ranking.sendRankingMsgToPlayer(senderType, playerIdx);
    }

    public void sendRankingInfoToPlayer(EnumRankingSenderType senderType, EnumRankingType rankingType, String playerIdx) {
        sendRankingInfoToPlayer(senderType, rankingType, RankingUtils.getRankingTypeDefaultName(rankingType), playerIdx);
    }


    /**
     * 添加活动分数
     *
     * @param playerIdx
     * @param rankingType
     * @param addScore
     */
    public void addActivityRankingScore(String playerIdx, EnumRankingType rankingType, int addScore) {
        if (addScore <= 0 || !RankingUtils.isSeparateStorageActivityRanking(rankingType)) {
            return;
        }
        List<ServerActivity> activities = ActivityManager.getInstance().getActivitiesByRankingType(rankingType);
        if (CollectionUtils.isEmpty(activities)) {
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return;
        }

        for (ServerActivity activity : activities) {
            int newScore = SyncExecuteFunction.executeFunction(entity, e -> {
                if (rankingType == EnumRankingType.ERT_DemonDescendsScore) {
                    DB_DemonDescendsActivityInfo.Builder demonDescends = entity.getDemonDescendsInfoBuilder(activity.getActivityId());
                    demonDescends.setScore(demonDescends.getScore() + addScore);
                    return demonDescends.getScore();
                } else {
                    return entity.addActivityRankingScore(activity.getActivityId(), rankingType, addScore);
                }
            });

            updatePlayerRankingScore(playerIdx, rankingType, RankingUtils.getActivityRankingName(activity), newScore);
        }
    }

    public void removeActivityRanking(ServerActivity remove) {
        removeRanking(RankingUtils.getActivityRankingName(remove));
    }

    /**
     * 只有活动对应的排行榜需要移除
     *
     * @param rankingName
     */
    private void removeRanking(String rankingName) {
        if (StringUtils.isEmpty(rankingName) || !RankingUtils.needClearRanking(rankingName)) {
            LogUtil.info("RankingManager.removeRanking, ranking need not clear, rankingName:" + rankingName);
            return;
        }
        AbstractRanking remove = this.rankingMap.remove(rankingName);
        if (remove != null) {
            remove.clearRanking();
        }
        LogUtil.info("RankingManager.removeRanking, remove ranking success, rankingName:" + rankingName);
    }


    public Activity.RankingEntrance getOnePlayerRankingEntrance(String playerIdx, EnumRankingType rankType) {
        AbstractRanking abstractRanking = getRanking(rankType, RankingUtils.getRankingTypeDefaultName(rankType));
        if (abstractRanking == null) {
            return Activity.RankingEntrance.getDefaultInstance();
        }

        RankingEntranceDto clientRankEntranceInfo = abstractRanking.getClientRankEntranceInfo();
        Activity.RankingEntrance.Builder result = Activity.RankingEntrance.newBuilder();
        result.setRankType(abstractRanking.getRankingType());
        if (clientRankEntranceInfo != null) {
            result.setPlayerName(StringUtils.defaultIfNull(clientRankEntranceInfo.getPlayerName(), ""));
            result.setPlayerAvatar(clientRankEntranceInfo.getPlayerAvatar());
            result.setAvatarBorder(clientRankEntranceInfo.getAvatarBorder());
            result.setAvatarBorderRank(clientRankEntranceInfo.getAvatarBorderRank());
            result.setRankingScore(clientRankEntranceInfo.getRankingScore());
            result.setPetBookId(clientRankEntranceInfo.getPetBookId());
            result.setCanClaimReward(canClaimRankingTargetReward(clientRankEntranceInfo, playerIdx));
            result.setTitleId(clientRankEntranceInfo.getTitleId());
        }
        return result.build();
    }

    private boolean canClaimRankingTargetReward(RankingEntranceDto clientRankEntranceInfo, String playerIdx) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return false;
        }
        return !target.getDb_Builder().getClaimedRankTargetRewardList().containsAll(clientRankEntranceInfo.getCanClaimRankTargetIds());
    }

    public List<Activity.RankingEntrance> getPlayerAllRankingEntrance(String playerIdx) {
        List<Activity.RankingEntrance> result = new ArrayList<>();
        for (EnumRankingType enumRankingType : showClientRanking) {
            Activity.RankingEntrance entrance = getOnePlayerRankingEntrance(playerIdx, enumRankingType);
            if (entrance != Activity.RankingEntrance.getDefaultInstance()) {
                result.add(entrance);
            }
        }
        return result;

    }
}