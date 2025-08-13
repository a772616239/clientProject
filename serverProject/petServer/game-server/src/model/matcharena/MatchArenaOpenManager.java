package model.matcharena;

import cfg.MatchArenaConfig;
import cfg.MatchArenaConfigObject;
import cfg.MatchArenaDanInheritanceConfig;
import cfg.MatchArenaDanInheritanceConfigObject;
import cfg.MatchArenaSeasonConfig;
import cfg.MatchArenaSeasonConfigObject;
import cfg.RankRewardRangeConfig;
import cfg.RankRewardRangeConfigObject;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GlobalData;
import static common.JedisUtil.jedis;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import common.tick.Tickable;
import db.entity.BaseEntity;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import model.gameplay.dbCache.gameplayCache;
import model.gameplay.entity.gameplayEntity;
import model.matcharena.dbCache.matcharenaCache;
import model.matcharena.entity.matcharenaEntity;
import model.ranking.RankingManager;
import model.ranking.settle.MailRankingSettleHandler;
import model.ranking.settle.RankingRewards;
import model.ranking.settle.RankingRewardsImpl;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Activity;
import protocol.Common;
import protocol.GameplayDB;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

public class MatchArenaOpenManager implements Tickable {

    @Getter
    private static MatchArenaOpenManager instance = new MatchArenaOpenManager();

    private GameConst.ArenaType curArenaType;


    private GameplayDB.DB_GamaPlayMatchArenaInfo.Builder dbBuilder;

    private MatchArenaSeasonConfigObject curSessionConfig;

    private long nextTickTime;

    private long startTime;
    private long endTime;
    private MatchArenaConfigObject matchArenaConfig;

    Map<int[], GameConst.ArenaType> timeScopeArenaTypeMap = new HashMap<>();

    private List<int[]> allTimeScope = new ArrayList<>();

    @Getter
    private volatile boolean open;


    public boolean isOpen() {
        return GlobalTick.getInstance().getCurrentTime() < endTime
                && GlobalTick.getInstance().getCurrentTime() > startTime;
    }

    public boolean init() {
        loadDbBuilder();
        initSeason();
        if (this.curSessionConfig == null && !openNewSeason()) {
            LogUtil.error("MatchArena init Season error");
            return false;
        }
        matchArenaConfig = MatchArenaConfig.getById(GameConst.CONFIG_ID);
        initOpenCfg();
        initNextTickTime();
        return GlobalTick.getInstance().addTick(this);
    }

    private void initNextTickTime() {
        long nextTick;
        if (curArenaType == null || curArenaType == GameConst.ArenaType.Null) {
            nextTick = startTime;
        } else {
            nextTick = endTime;
        }
        if (curSessionConfig != null) {
            this.nextTickTime = Math.min(curSessionConfig.getEndtime(), nextTick);
        }
        this.nextTickTime = nextTick;
    }

    @Override
    public void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (this.curSessionConfig == null && !openNewSeason()) {
            return;
        }
        if (currentTime < nextTickTime) {
            return;
        }
        resetOpenStatus();
        if (currentTime < this.curSessionConfig.getStarttime()) {
            unStartStatue();

        } else if (currentTime < this.curSessionConfig.getEndtime()) {
            openStatus(currentTime);

        } else {
            settleStatus();
        }
        initNextTickTime();

    }


    private void initOpenCfg() {
        initTimeScopeArenaTypeMap();
        resetOpenStatus();

    }

    private void resetOpenStatus() {
        long now = GlobalTick.getInstance().getCurrentTime();
        long todayStart = TimeUtil.getTodayStamp(now);
        long nowMin = (now - TimeUtil.getTodayStamp(now)) / TimeUtil.MS_IN_A_MIN;

        if (initOpenTimeOutOfScope(todayStart, nowMin)) {
            return;
        }
        if (initOpenInTimeScope(todayStart, nowMin)) {
            return;
        }
    }

    private void initTimeScopeArenaTypeMap() {
        int[][] matchTimeScope = matchArenaConfig.getMatchtimescope();
        int[][] timeScope = matchArenaConfig.getTimescope();

        for (int[] ints : matchTimeScope) {
            if (ints.length == 2) {
                timeScopeArenaTypeMap.put(ints, GameConst.ArenaType.Normal);
                allTimeScope.add(ints);
            }
        }
        for (int[] ints : timeScope) {
            if (ints.length == 2) {
                timeScopeArenaTypeMap.put(ints, GameConst.ArenaType.Rank);
                allTimeScope.add(ints);
            }
        }
        allTimeScope.sort(Comparator.comparingInt(o -> o[0]));
        allTimeScope = Collections.unmodifiableList(allTimeScope);
        timeScopeArenaTypeMap = Collections.unmodifiableMap(timeScopeArenaTypeMap);
    }

    private boolean initOpenTimeOutOfScope(long todayStart, long nowMin) {
        int minStart = allTimeScope.get(0)[0];
        int maxEnd = allTimeScope.get(allTimeScope.size() - 1)[1];
        if (nowMin < minStart) {
            startTime = todayStart + TimeUtil.MS_IN_A_MIN * allTimeScope.get(0)[0];
            endTime = todayStart + TimeUtil.MS_IN_A_MIN * allTimeScope.get(0)[1];
            switchArenaType(GameConst.ArenaType.Null);
            return true;
        }
        if (nowMin >= maxEnd) {
            startTime = todayStart + TimeUtil.MS_IN_A_MIN * allTimeScope.get(0)[0] + TimeUtil.MS_IN_A_DAY;
            endTime = todayStart + TimeUtil.MS_IN_A_MIN * allTimeScope.get(0)[1] + TimeUtil.MS_IN_A_DAY;
            switchArenaType(GameConst.ArenaType.Null);
            return true;
        }
        for (int i = 0; i < allTimeScope.size() - 1; i++) {
            if (nowMin >= allTimeScope.get(i)[1] && nowMin < allTimeScope.get(i + 1)[0]) {
                startTime = todayStart + TimeUtil.MS_IN_A_MIN * allTimeScope.get(0)[0];
                endTime = todayStart + TimeUtil.MS_IN_A_MIN * allTimeScope.get(0)[1];
                switchArenaType(GameConst.ArenaType.Null);
                return true;
            }
        }

        return false;

    }

    private void switchArenaType(GameConst.ArenaType arenaType) {
        if (curArenaType == arenaType) {
            return;
        }
        LogUtil.info("switch arena type curType ={}",arenaType);
        if (arenaType == GameConst.ArenaType.Null) {
            onCurArenaClose();
        }
        this.curArenaType = arenaType;
    }

    private void onCurArenaClose() {
        MatchArenaManager.getInstance().cancelAllPlayerMatch();
    }

    private boolean initOpenInTimeScope(long todayStart, long nowMin) {
        for (int[] ints : allTimeScope) {
            if (ints.length != 2) {
                continue;
            }
            if (nowMin >= ints[0] && nowMin < ints[1]) {
                startTime = ints[0] * TimeUtil.MS_IN_A_MIN + todayStart;
                endTime = ints[1] * TimeUtil.MS_IN_A_MIN + todayStart;
                switchArenaType(timeScopeArenaTypeMap.get(ints));
                return true;
            }
        }

        return false;
    }

    public GameConst.ArenaType getCurArenaType() {
        if (curArenaType == GameConst.ArenaType.Rank) {
            return curSessionConfig == null ? GameConst.ArenaType.Null : GameConst.ArenaType.Rank;
        }
        return this.curArenaType;
    }

    private void initSeason() {
        if (this.dbBuilder.getCurOpenSessionId() != 0) {
            curSessionConfig = MatchArenaSeasonConfig.getById(this.getDbBuilder().getCurOpenSessionId());
        }
        LogUtil.info("model.matcharena.MatchArenaManager.initSeason, season config id:" + this.dbBuilder.getCurOpenSessionId());
    }

    private void loadDbBuilder() {
        gameplayEntity entity = gameplayCache.getInstance().getByGamePlayType(GameplayDB.GameplayTypeEnum.GTE_MatchArena);
        if (entity != null && entity.getGameplayinfo() != null) {
            try {
                this.dbBuilder = GameplayDB.DB_GamaPlayMatchArenaInfo.parseFrom(entity.getGameplayinfo()).toBuilder();
            } catch (InvalidProtocolBufferException e) {
                LogUtil.printStackTrace(e);
            }
        }

        if (this.dbBuilder == null) {
            this.dbBuilder = GameplayDB.DB_GamaPlayMatchArenaInfo.newBuilder();
        }
    }

    public GameplayDB.DB_GamaPlayMatchArenaInfo.Builder getDbBuilder() {
        return this.dbBuilder;
    }

    private void updateGamePlayInfo() {
        gameplayEntity entity = gameplayCache.getInstance().getByGamePlayType(GameplayDB.GameplayTypeEnum.GTE_MatchArena);
        entity.setGameplayinfo(this.dbBuilder.build().toByteArray());
        gameplayCache.put(entity);
    }

    private boolean openNewSeason() {
        this.curSessionConfig = MatchArenaSeasonConfig.getCurOpenSeason();
        if (this.curSessionConfig == null) {
            LogUtil.error("model.matcharena.MatchArenaManager.openNewSeason, can not find next open season");
            return false;
        }

        this.dbBuilder.setCurOpenSessionId(this.curSessionConfig.getId());
        updateGamePlayInfo();
        initNextTickTime();
        LogUtil.info("MatchArenaManager.openNewSeason, new season id:" + this.curSessionConfig.getId());
        return true;
    }


    private void unStartStatue() {
        this.open = false;
    }

    private void openStatus(long currentTime) {
        int todayMin = TimeUtil.getMin(currentTime);
        boolean newOpenStatus = false;
        if (this.curSessionConfig.getTimescope() != null) {
            for (int[] ints : this.curSessionConfig.getTimescope()) {
                if (ints.length < 2) {
                    LogUtil.error("MatchArenaManager.openStatus, time open scope length is less than 2, season id:" + this.curSessionConfig.getId());
                    continue;
                }
                if (GameUtil.inScope(ints[0], ints[1], todayMin)) {
                    newOpenStatus = true;
                    break;
                }
            }
        }

        if (this.open != newOpenStatus && !newOpenStatus) {
            MatchArenaManager.getInstance().cancelAllPlayerMatch();
        }
        this.open = newOpenStatus;

        danInheritance();
    }

    private void danInheritance() {
        if (this.dbBuilder.getSettleDanInheritanceIdList().contains(this.curSessionConfig.getId())) {
//            LogUtil.debug("model.matcharena.MatchArenaManager.danInheritance, season id:");
            return;
        }

        //清空排行榜
        RankingManager.getInstance().clearRanking(Activity.EnumRankingType.ERT_MatchArena_Local);
        //跨服排行榜需要做特殊判断
        String seasonIdStr = String.valueOf(this.curSessionConfig.getId());
        if (!jedis.sismember(GameConst.RedisKey.MatchArenaClearSeasonCrossRanking, seasonIdStr)) {
            RankingManager.getInstance().clearRanking(Activity.EnumRankingType.ERT_MatchArena_Cross);
            jedis.sadd(GameConst.RedisKey.MatchArenaClearSeasonCrossRanking, seasonIdStr);
            LogUtil.info("model.matcharena.MatchArenaManager.danInheritance, cross ranking is not clear, clear now, id:" + seasonIdStr);
        }

        Map<Integer, Integer> danNewScoreMap = new HashMap<>();
        Map<Integer, Integer> danNewDanMap = new HashMap<>();
        for (BaseEntity value : matcharenaCache.getInstance().getAll().values()) {
            if (!(value instanceof matcharenaEntity)) {
                continue;
            }

            matcharenaEntity entity = (matcharenaEntity) value;
            int oldDan = entity.getDbBuilder().getRankMatchArenaBuilder().getDan();
            int newScore;
            int newDan;
            if (danNewDanMap.containsKey(oldDan) && danNewDanMap.containsKey(oldDan)) {
                newScore = danNewScoreMap.get(oldDan);
                newDan = danNewDanMap.get(oldDan);
            } else {
                MatchArenaDanInheritanceConfigObject inheritanceConfig = MatchArenaDanInheritanceConfig.getByCurdan(entity.getDbBuilder()
                        .getRankMatchArenaBuilder().getDan());
                if (inheritanceConfig == null) {
                    LogUtil.error("model.matcharena.MatchArenaManager.danInheritance, inheritance cfg is not exist, dan:" + oldDan);
                    continue;
                }
                newScore = inheritanceConfig.getNewscore();
                danNewScoreMap.put(oldDan, newScore);

                newDan = MatchArenaUtil.getScoreDan(newScore);
                danNewDanMap.put(oldDan, newDan);
            }
            SyncExecuteFunction.executeConsumer(entity, e -> {
                entity.getDbBuilder().clear();

                entity.getDbBuilder().getRankMatchArenaBuilder().setScore(newScore);
                entity.getDbBuilder().getRankMatchArenaBuilder().setDan(newDan);
            });

            if (GlobalData.getInstance().checkPlayerOnline(entity.getIdx())) {
                entity.sendInfo();
            }

            //移除Redis数据
            jedis.hdel(GameConst.RedisKey.MatchArenaPlayerInfo.getBytes(StandardCharsets.UTF_8), entity.getIdx().getBytes(StandardCharsets.UTF_8));
            jedis.zrem(GameConst.RedisKey.MatchArenaPlayerScore, entity.getIdx());
        }

        this.dbBuilder.addSettleDanInheritanceId(this.curSessionConfig.getId());
        updateGamePlayInfo();
        LogUtil.info("model.matcharena.MatchArenaManager.danInheritance, finished");
    }

    private void settleStatus() {
        this.open = false;

        MatchArenaManager.getInstance().cancelAllPlayerMatch();

        if (this.dbBuilder.getAlreadySettleSeasonList().contains(this.curSessionConfig.getId())) {
            LogUtil.error("model.matcharena.MatchArenaManager.settleStatus, cur season is already settled, id:" + this.curSessionConfig.getId());
            this.curSessionConfig = null;
            return;
        }

        settleCrossRankingRewards();

        this.dbBuilder.addAlreadySettleSeason(this.curSessionConfig.getId());
        LogUtil.info("model.matcharena.MatchArenaManager.settleStatus, settle season finished, id:" + this.curSessionConfig.getId());
        this.curSessionConfig = null;
    }

    private void settleCrossRankingRewards() {
        int crossRankingTemplate = this.matchArenaConfig.getCrossrankingtemplate();
        MailRankingSettleHandler handler = new MailRankingSettleHandler(Activity.EnumRankingType.ERT_MatchArena_Cross,
                getCrossRankingRewards(), crossRankingTemplate, Common.RewardSourceEnum.RSE_MatchArena);
        handler.settleRanking();
        LogUtil.info("model.matcharena.MatchArenaManager.settleCrossRankingRewards, settle cross server finised");
    }

    private List<RankingRewards> getCrossRankingRewards() {
        return Arrays.stream(this.curSessionConfig.getCrossrankingrewards())
                .mapToObj(e -> {
                    RankRewardRangeConfigObject rankRewardsConfig = RankRewardRangeConfig.getById(e);
                    if (rankRewardsConfig == null) {
                        LogUtil.error("model.matcharena.MatchArenaManager.getRankingRewards, ranking rewards cfg is not exist, id:" + e);
                        return null;
                    }

                    List<Common.Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(rankRewardsConfig.getReward());
                    if (CollectionUtils.isEmpty(rewards)) {
                        LogUtil.error("model.matcharena.MatchArenaManager.getRankingRewards, ranking rewards config error, id:" + e);
                        return null;
                    }

                    return new RankingRewardsImpl(rankRewardsConfig.getRangemin(), rankRewardsConfig.getRangemax(), rewards);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
