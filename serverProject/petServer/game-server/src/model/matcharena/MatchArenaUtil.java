package model.matcharena;

import cfg.ArenaConfig;
import cfg.ArenaConfigObject;
import cfg.MatchArenaConfig;
import cfg.MatchArenaConfigObject;
import cfg.MatchArenaDanConfig;
import cfg.MatchArenaDanConfigObject;
import cfg.MatchArenaRobotTeam;
import cfg.PlayerLevelConfig;
import cfg.PlayerLevelConfigObject;
import common.EloScoreCalculator;
import common.GameConst;
import common.GameConst.RedisKey;
import common.GlobalData;

import static common.JedisUtil.jedis;

import common.SyncExecuteFunction;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import model.arena.dbCache.arenaCache;
import model.matcharena.dbCache.matcharenaCache;
import model.matcharena.entity.matcharenaEntity;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import model.recentpassed.RecentPassedUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.team.dbCache.teamCache;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import platform.logs.ReasonManager;
import protocol.Activity.EnumRankingType;
import protocol.Battle;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.SkillBattleDict;
import protocol.Common;
import protocol.Common.Reward;
import protocol.MatchArena.SC_MatchArenaRefreshScore;
import protocol.MatchArenaDB;
import protocol.MatchArenaDB.MatchArenaTeamInfo;
import protocol.MatchArenaDB.RedisMatchArenaPlayerInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RecentPassedOuterClass.RecentPassed;
import protocol.TargetSystem;
import util.EventUtil;
import util.LogUtil;
import util.RandomUtil;

/**
 * @author huhan
 * @date 2021/05/18
 */
public class MatchArenaUtil {

    private static final Object MONITOR = new Object();

    public static final String ROOBOTID = "111";

    private static List<MatchArenaDanConfigObject> sortedDanConfigList;

    /**
     * 获取积分所对应的段位
     *
     * @param score
     * @return
     */
    public static int getScoreDan(int score) {
        if (sortedDanConfigList == null) {
            synchronized (MONITOR) {
                if (sortedDanConfigList == null) {
                    sortedDanConfigList = MatchArenaDanConfig._ix_id.values().stream()
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparingInt(MatchArenaDanConfigObject::getId))
                            .collect(Collectors.toList());
                }
            }
        }

        int result = 0;
        for (MatchArenaDanConfigObject configObj : sortedDanConfigList) {
            if (score >= configObj.getNeedscore()) {
                result = configObj.getId();
            } else {
                break;
            }
        }
        return Math.max(result, 1);
    }

    private static EloScoreCalculator calculator;

    private static void initCalculator() {
        if (calculator == null) {
            synchronized (MONITOR) {
                if (calculator == null) {
                    MatchArenaConfigObject matchArenaCfg = MatchArenaConfig.getById(GameConst.CONFIG_ID);
                    double saWin = (matchArenaCfg.getSawin() * 1.0) / 100;
                    double saFailed = (matchArenaCfg.getSafailed() * 1.0) / 100;
                    double saDraw = (matchArenaCfg.getSadraw() * 1.0) / 100;
                    calculator = new EloScoreCalculator(saWin, saFailed, saDraw, matchArenaCfg.getConstnum(), matchArenaCfg.getK());
                }
            }
        }
    }

    //    battleResult  -1平局， 1 胜利， 2失败
    public static int calculateScore(int playerScore, int opponentScore, int battleResult) {
        initCalculator();
        return calculator.calculateScore(playerScore, opponentScore, battleResult);
    }

    public static void savePlayerRecentBattleRecord(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }

        RecentPassed recentPassed = RecentPassedUtil.buildRecentPassedInfo(playerIdx, TeamTypeEnum.TTE_MatchArenaRank);
        if (recentPassed == null) {
            LogUtil.error("MatchArenaUtil.savePlayerRecentBattleRecord, build match arena recent passed info failed, playerIdx:" + playerIdx);
            return;
        }

        jedis.hset(RedisKey.MatchArenaRecentBattle.getBytes(StandardCharsets.UTF_8),
                playerIdx.getBytes(StandardCharsets.UTF_8), recentPassed.toByteArray());
    }

    public static RedisMatchArenaPlayerInfo buildRedisPlayerInfo(String playerIdx, GameConst.ArenaType arenaType) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("model.matcharena.MatchArenaUtil.buildPlayerInfo, can not find playerIdx:" + playerIdx);
            return null;
        }

        boolean rank = arenaType == GameConst.ArenaType.Rank;

        RedisMatchArenaPlayerInfo.Builder resultBuilder = RedisMatchArenaPlayerInfo.newBuilder();
        resultBuilder.setPlayerBaseInfo(player.getBattleBaseData());

        TeamNumEnum useTeamNum = rank ? TeamNumEnum.TNE_MatchArenaRank_1 : TeamNumEnum.TNE_MatchArenaNormal_1;
        MatchArenaTeamInfo.Builder teamBuilder = MatchArenaTeamInfo.newBuilder().setTeamNum(useTeamNum);
        BattleSubTypeEnum subTypeEnum = rank ? BattleSubTypeEnum.BSTE_MatchArenaRanking : BattleSubTypeEnum.BSTE_ArenaMatchNormal;
        List<BattlePetData> petDataList = teamCache.getInstance().buildBattlePetData(playerIdx,
                useTeamNum, subTypeEnum);
        if (CollectionUtils.isNotEmpty(petDataList)) {
            teamBuilder.addAllPetList(petDataList);
        }

        List<Integer> skillList = teamCache.getInstance().getPlayerTeamSkillList(playerIdx, useTeamNum);
        if (CollectionUtils.isNotEmpty(skillList)) {
            skillList.forEach(skillId -> {
                SkillBattleDict skillDict = SkillBattleDict.newBuilder()
                        .setSkillId(skillId)
                        .setSkillLv(player.getSkillLv(skillId))
                        .build();
                teamBuilder.addPlayerSkillIdList(skillDict);
            });
        }

        resultBuilder.setTeamInfo(teamBuilder);
        matcharenaEntity entity = matcharenaCache.getInstance().getEntity(playerIdx);
        if (entity != null) {
            MatchArenaDB.DB_RankMatchArena.Builder rankMatchArenaBuilder = entity.getDbBuilder().getRankMatchArenaBuilder();
            resultBuilder.setLosingStreak(rankMatchArenaBuilder.getLosingStreak());
            resultBuilder.setDan(rankMatchArenaBuilder.getDan());
        }
        return resultBuilder.build();
    }

    public static List<Reward> getBattleRewards(boolean win, int dan, int curGainMedalCount) {
        MatchArenaDanConfigObject danConfig = MatchArenaDanConfig.getById(dan);
        if (danConfig == null) {
            LogUtil.error("model.matcharena.MatchArenaUtil.getBattleRewards, dan cfg is not exist, dan:" + dan);
            return Collections.emptyList();
        }
        MatchArenaConfigObject matchArenaCfg = MatchArenaConfig.getById(GameConst.CONFIG_ID);
        List<Reward> battleRewards = win ? RewardUtil.parseRewardIntArrayToRewardList(matchArenaCfg.getBattlewinrewards())
                : RewardUtil.parseRewardIntArrayToRewardList(matchArenaCfg.getBattlefailedrewards());
        if (CollectionUtils.isNotEmpty(battleRewards) && danConfig.getMedalgainrate() != 100) {
            battleRewards = battleRewards.stream()
                    .map(e -> {
                        if (isMedalReward(e)) {
                            int tempNewCount = (e.getCount() / 100) * danConfig.getMedalgainrate();
                            int newCount = Math.min(tempNewCount, danConfig.getMedallimit() - curGainMedalCount);
                            if (newCount <= 0) {
                                return null;
                            }
                            return e.toBuilder().setCount(newCount).build();
                        }
                        return e;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return battleRewards;
    }

    public static boolean isMedalReward(Reward reward) {
        if (reward == null) {
            return false;
        }
        int[] medalConfig = MatchArenaConfig.getById(GameConst.CONFIG_ID).getMedalconfig();
        return reward.getRewardTypeValue() == medalConfig[0] && reward.getId() == medalConfig[1];
    }

    public static int getMedalCount(List<Reward> rewardList) {
        if (CollectionUtils.isEmpty(rewardList)) {
            return 0;
        }
        return rewardList.stream()
                .map(e -> isMedalReward(e) ? e.getCount() : 0)
                .reduce(Integer::sum)
                .orElse(0);
    }

    public static void updateRanking(String playerIdx, int newScore) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        int playerDan = arenaCache.getInstance().getPlayerDan(playerIdx);
        RankingManager.getInstance().updatePlayerRankingScore(playerIdx, EnumRankingType.ERT_MatchArena_Local, playerDan, newScore);
        RankingManager.getInstance().updatePlayerRankingScore(playerIdx, EnumRankingType.ERT_MatchArena_Cross, newScore);
    }

    /**
     * 获取可同时上阵的数量
     *
     * @param scores
     * @return
     */
    public static int getBattlePetLimit(int... scores) {
        int maxScore = 0;
        for (int score : scores) {
            if (score > maxScore) {
                maxScore = score;
            }
        }

        int maxScoreDan = getScoreDan(maxScore);
        MatchArenaDanConfigObject danCfg = MatchArenaDanConfig.getById(maxScoreDan);
        if (danCfg == null) {
            LogUtil.error("model.matchArena.MatchArenaUtil.getMaxPetCountAtSameTime, dan cfg is not exist, dan:" + maxScore);
            return 0;
        }
        return danCfg.getBattlepetlimit();
    }

    public static void refreshClientScore(String playerIdx, boolean win, int oldScore, int newScore) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }

        SC_MatchArenaRefreshScore.Builder builder = SC_MatchArenaRefreshScore.newBuilder()
                .setWin(win)
                .setOldScore(oldScore)
                .setNewScore(newScore);

        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_MatchArenaRefreshScore_VALUE, builder);
    }

    public static void sendNormalMatchArenaBattleFinish(String playerIdx, boolean win) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }

        SC_MatchArenaRefreshScore.Builder builder = SC_MatchArenaRefreshScore.newBuilder().setWin(win);

        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_MatchArenaRefreshScore_VALUE, builder);
    }

    public static MatchArenaBattleReward randomMatchReward(boolean win, matcharenaEntity entity) {
        MatchArenaBattleReward reward = new MatchArenaBattleReward();
        if (entity == null) {
            return null;
        }

        PlayerLevelConfigObject lvCfg = PlayerLevelConfig.getByLevel(PlayerUtil.queryPlayerLv(entity.getIdx()));

        if (lvCfg == null) {
            return null;
        }
        //惊喜奖励
        Reward gift = randomMatchArenaGiftReward(lvCfg, entity);
        reward.setGiftReward(gift);

        //随机奖励类型
        DefaultKeyValue<Reward, Integer> baseReward = randomMatchBaseReward(win, lvCfg, entity);
        if (baseReward != null) {
            reward.setBaseReward(baseReward.getKey());
            reward.setIncrBaseRewardTimes(baseReward.getValue());
        }
        return reward;
    }

    private static DefaultKeyValue<Reward, Integer> randomMatchBaseReward(boolean win, PlayerLevelConfigObject lvCfg, matcharenaEntity entity) {
        String rewardType = randomMatchRewardType(entity.getRewardInfo(), lvCfg.getMatcharenamatchfullreward());

        if (rewardType == null) {
            return null;
        }

        DefaultKeyValue<Reward, Integer> result = new DefaultKeyValue<>();

        int[][] rewardCfg = win ? lvCfg.getMatcharenamatchwinreward()
                : lvCfg.getMatcharenamatchfailreward();

        for (int[] ints : rewardCfg) {
            if (ints.length != 4) {
                continue;
            }
            if (rewardType.equals(getBattleRewardTypeKey(ints[0], ints[1]))) {
                result.setKey(RewardUtil.parseReward(ints));
                result.setValue(ints[3]);
            }
        }
        return result;

    }

    private static Reward randomMatchArenaGiftReward(PlayerLevelConfigObject lvCfg, matcharenaEntity entity) {
        if (RandomUtils.nextInt(100) >= MatchArenaConfig.getById(GameConst.CONFIG_ID).getGaingiftpro()) {
            return null;
        }
        String giftRewardType = randomMatchRewardType(entity.getRewardInfo(), lvCfg.getMatcharenamatchfullreward());
        if (giftRewardType == null) {
            return null;
        }
        for (int[] arr : lvCfg.getMatcharenagift()) {
            if (giftRewardType.equals(getBattleRewardTypeKey(arr[0], arr[1]))) {
                return RewardUtil.parseReward(arr);
            }
        }
        return null;
    }

    private static String randomMatchRewardType(Map<String, Integer> rewardInfo, int[][] limitCfg) {
        String key;
        List<Integer> random = new ArrayList<>();
        for (int i = 0; i < limitCfg.length; i++) {
            random.add(i);
        }
        Collections.shuffle(random);
        int[] ints;
        int remainRewardTimes;
        for (Integer index : random) {
            ints = limitCfg[index];
            if (ints.length != 3) {
                continue;
            }
            key = getBattleRewardTypeKey(ints[0], ints[1]);
            Integer alreadyRewardTimes = rewardInfo.get(key);
            remainRewardTimes = alreadyRewardTimes == null ? ints[2] : ints[2] - alreadyRewardTimes;
            if (remainRewardTimes > 0) {
                return key;
            }
        }
        return null;
    }

    public static String getBattleRewardTypeKey(int type, int id) {
        return type + "-" + id;
    }

    public static List<Reward> doMatchRewards(String playerIdx, boolean win) {
        matcharenaEntity entity = matcharenaCache.getInstance().getEntity(playerIdx);
        if (entity == null) {
            return Collections.emptyList();
        }

        MatchArenaBattleReward reward = MatchArenaUtil.randomMatchReward(win, entity);
        if (reward == null || (reward.getBaseReward() == null && reward.getGiftReward() == null)) {
            return Collections.emptyList();
        }

        List<Reward> realRewards = new ArrayList<>();

        if (reward.getBaseReward() != null) {
            realRewards.add(reward.getBaseReward());
        }

        if (reward.getGiftReward() != null) {
            realRewards.add(reward.getGiftReward());
        }

        SyncExecuteFunction.executeConsumer(entity, ex -> entity.incrMatchRewardNum(reward));

        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_MatchArena);

        RewardManager.getInstance().doRewardByList(playerIdx, realRewards, reason, false);

        return realRewards;

    }


    public static List<PetMessage.Pet> copyTeamProperty(List<PetMessage.Pet> petList) {
        int[] team = MatchArenaRobotTeam.randomTeam();
        List<PetMessage.Pet> result = new ArrayList<>();
        ArenaConfigObject cfg = ArenaConfig.getById(GameConst.CONFIG_ID);
        if (cfg == null) {
            return Collections.emptyList();
        }
        int[] robotPropertyScope = cfg.getRankrobotpropertyscope();

        PetMessage.Pet newPet;

        int petIndex;
        for (int i = 0; i < team.length; i++) {
            petIndex = i % petList.size();
            newPet = buildArenaRankRobot(petList, team[i], petIndex, robotPropertyScope);
            result.add(newPet);
        }
        return result;
    }

    public static PetMessage.Pet buildArenaRankRobot(List<PetMessage.Pet> petList,
                                                     int petBookId, int index, int[] robotPropertyScope) {
        PetMessage.Pet pet = petList.get(index);
        int level;
        int rarity;
        level = Math.max(1, pet.getPetLvl() * RandomUtil.randomInScope(robotPropertyScope[0], robotPropertyScope[1]) / 100);
        rarity = Math.max(2, pet.getPetRarity() * RandomUtil.randomInScope(robotPropertyScope[0], robotPropertyScope[1]) / 100);
        return petCache.getInstance().buildPet(petBookId, rarity, level);
    }

    public static int randomRobotPlayerLv(int lv) {
        if (lv <= 0) {
            return lv;
        }
        int add = RandomUtils.nextInt(20) - 10;
        return Math.max(1, lv + add);
    }

    public static void tailMatchArenaRankBattle(String playerIdx, int anotherPlayerScore, int camp, Battle.CS_BattleResult realResult) {
        matcharenaEntity entity = matcharenaCache.getInstance().getEntity(playerIdx);
        if (entity == null) {
            return;
        }
        int oldScore = entity.getDbBuilder().getRankMatchArenaBuilder().getScore();
        SyncExecuteFunction.executeConsumer(entity, e -> {
            if (realResult.getWinnerCamp() == camp) {
                settleRankBattleWin(entity);
            }
            if (realResult.getWinnerCamp() != camp) {
                settleRankBattleFailed(entity);
            }

            int playerBattleResult = realResult.getWinnerCamp() == camp ? 1 : realResult.getWinnerCamp() == -1 ? -1 : realResult.getWinnerCamp();

            entity.incrRankScoreByBattle(playerBattleResult, anotherPlayerScore);
        });

        int newScore = entity.getDbBuilder().getRankMatchArenaBuilder().getScore();
        int playerScoreChange = newScore - oldScore;

        LogUtil.info("tailMatchArenaRankBattle, playerIdx:" + playerIdx + ", scoreChange:"
                + playerScoreChange + ", afterChange:" + entity.getDbBuilder().getRankMatchArenaBuilder().getScore());

        entity.updateScoreToRedis();

        MatchArenaUtil.updateRanking(playerIdx, newScore);

        MatchArenaUtil.refreshClientScore(playerIdx, realResult.getWinnerCamp() == camp, oldScore, newScore);

        entity.sendInfo();
        MatchArenaUtil.savePlayerRecentBattleRecord(playerIdx);

        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_MatchArenaRank_Win, 1, 0);
        if (realResult.getWinnerCamp() == camp) {
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_MatchArenaRank_Jion, 1, 0);
            if (entity.getDbBuilder().getRankMatchArenaBuilder().getDefWinNum() == 3) {
                EventUtil.triggerUpdateTargetProgress(playerIdx, TargetSystem.TargetTypeEnum.TTE_MatchArenaRank_3Win, 1, 0);
            }
        }

    }

    private static void settleRankBattleFailed(matcharenaEntity entity) {
        MatchArenaDB.DB_RankMatchArena.Builder rankMatchArenaBuilder = entity.getDbBuilder().getRankMatchArenaBuilder();
        rankMatchArenaBuilder.clearDefWinNum();
        entity.incrRankMatchLosingStreak();
    }

    private static void settleRankBattleWin(matcharenaEntity entity) {
        entity.clearRankMatchLosingStreak();
        MatchArenaDB.DB_RankMatchArena.Builder rankMatchArenaBuilder = entity.getDbBuilder().getRankMatchArenaBuilder();
        rankMatchArenaBuilder.setDefWinNum(rankMatchArenaBuilder.getDefWinNum() + 1);
    }
}
