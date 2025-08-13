/**
 * created by tool DAOGenerate
 */
package model.matcharena.entity;

import cfg.MatchArenaConfig;
import cfg.MatchArenaConfigObject;
import cfg.MatchArenaDanConfig;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GameConst.RedisKey;
import common.GlobalData;
import static common.JedisUtil.jedis;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import model.matcharena.MatchArenaBattleReward;
import model.matcharena.MatchArenaUtil;
import model.matcharena.dbCache.matcharenaCache;
import model.obj.BaseObj;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import protocol.Activity.EnumRankingType;
import protocol.Common;
import protocol.MatchArena.SC_ClaimMatchArenaInfo;
import protocol.MatchArenaDB;
import protocol.MatchArenaDB.DB_MatchArenaInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class matcharenaEntity extends BaseObj {

    public String getClassType() {
        return "matcharenaEntity";
    }

    /**
     *
     */
    private String idx;

    /**
     *
     */
    private byte[] data;


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
    public byte[] getData() {
        return data;
    }

    /**
     * 设置
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    //<rewardType-id,num>
    @Getter
    private final Map<String, Integer> rewardInfo = new ConcurrentHashMap<>();


    public String getBaseIdx() {
        // TODO Auto-generated method stub
        return idx;
    }

    @Override
    public void putToCache() {
        matcharenaCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.data = this.dbBuilder.build().toByteArray();
    }

    private DB_MatchArenaInfo.Builder dbBuilder;

    public DB_MatchArenaInfo.Builder getDbBuilder() {
        if (this.dbBuilder == null && this.data != null) {
            try {
                this.dbBuilder = DB_MatchArenaInfo.parseFrom(this.data).toBuilder();
                rewardInfo.clear();
                rewardInfo.putAll(dbBuilder.getLimitRewardMap());
            } catch (InvalidProtocolBufferException e) {
                LogUtil.printStackTrace(e);
            }
        }

        if (this.dbBuilder == null) {
            this.dbBuilder = DB_MatchArenaInfo.newBuilder();

            MatchArenaConfigObject matchArenaCfg = MatchArenaConfig.getById(GameConst.CONFIG_ID);
            this.dbBuilder.getRankMatchArenaBuilder().setScore(matchArenaCfg.getDefaultscore());
            this.dbBuilder.getRankMatchArenaBuilder().setDan(MatchArenaUtil.getScoreDan(matchArenaCfg.getDefaultscore()));
            updateScoreToRedis(matchArenaCfg.getDefaultscore());
            LogUtil.info("matcharenaEntity.getDbBuilder, builder is null, create new builder");
        }

        return this.dbBuilder;
    }

    public void updateScoreToRedis() {
        updateScoreToRedis(getDbBuilder().getRankMatchArenaBuilder().getScore());
    }

    public void updateScoreToRedis(int score) {
        jedis.zadd(RedisKey.MatchArenaPlayerScore, score, getIdx());
    }

    public void sendInfo() {
        SC_ClaimMatchArenaInfo.Builder resultBuilder = SC_ClaimMatchArenaInfo.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));

        DB_MatchArenaInfo.Builder dbBuilder = getDbBuilder();
        resultBuilder.setDan(dbBuilder.getRankMatchArenaBuilder().getDan());
        resultBuilder.setScore(dbBuilder.getRankMatchArenaBuilder().getScore());
        resultBuilder.setGainMedalCount(dbBuilder.getRankMatchArenaBuilder().getGainMedalCount());
        resultBuilder.setCurOpenSeasonId(1);

        String localRankingName = RankingUtils.getRankingTypeDefaultName(EnumRankingType.ERT_MatchArena_Local);
        int localRanking = RankingManager.getInstance().queryPlayerRanking(localRankingName, getIdx());
        resultBuilder.setLocalRanking(localRanking);

        String crossRankingName = RankingUtils.getRankingTypeDefaultName(EnumRankingType.ERT_MatchArena_Cross);
        int crossRanking = RankingManager.getInstance().queryPlayerRanking(crossRankingName, getIdx());
        resultBuilder.setCrossRanking(crossRanking);

        GlobalData.getInstance().sendMsg(getIdx(), MsgIdEnum.SC_ClaimMatchArenaInfo_VALUE, resultBuilder);
    }

    public void updateWeeklyData(boolean sendMsg) {
        getDbBuilder().clearLimitReward();
        if (sendMsg) {
            sendInfo();
        }
    }

    public void incrMatchRewardNum(MatchArenaBattleReward reward) {
        if (reward == null) {
            return;
        }
        synchronized (this) {
            Common.Reward var;
            if ((var = reward.getBaseReward()) != null) {
                String key = MatchArenaUtil.getBattleRewardTypeKey(var.getRewardTypeValue(), var.getId());
                Integer integer = rewardInfo.get(key);
                int value = integer == null ? reward.getIncrBaseRewardTimes() : integer + reward.getIncrBaseRewardTimes();
                rewardInfo.put(key, value);
            }
            if ((var = reward.getGiftReward()) != null) {
                String key = MatchArenaUtil.getBattleRewardTypeKey(var.getRewardTypeValue(), var.getId());
                Integer integer = rewardInfo.get(key);
                int value = integer == null ? 1 : integer + 1;
                rewardInfo.put(key, value);
            }

        }
    }

    public void incrRankMatchLosingStreak() {
        MatchArenaDB.DB_RankMatchArena.Builder rankMatchArenaBuilder = getDbBuilder().getRankMatchArenaBuilder();
        rankMatchArenaBuilder.setLosingStreak(rankMatchArenaBuilder.getLosingStreak() + 1);
    }

    public void clearRankMatchLosingStreak() {
        getDbBuilder().getRankMatchArenaBuilder().clearLosingStreak();
    }

    public void incrRankScoreByBattle(int playerBattleResult, int anotherPlayerScore) {
        if (anotherPlayerScore <= 0) {
            anotherPlayerScore = MatchArenaDanConfig.getRobotScore(getDbBuilder().getRankMatchArena().getDan());
        }
        MatchArenaDB.DB_RankMatchArena.Builder rankDbBuilder = getDbBuilder().getRankMatchArenaBuilder();
        int playerScoreChange = MatchArenaUtil.calculateScore(rankDbBuilder.getScore(), anotherPlayerScore, playerBattleResult);

        rankDbBuilder.setScore(rankDbBuilder.getScore() + playerScoreChange);
        rankDbBuilder.setDan(MatchArenaUtil.getScoreDan(rankDbBuilder.getScore()));

        if (playerBattleResult == 1) {
            rankDbBuilder.setWinCount(rankDbBuilder.getWinCount() + 1);
        } else {
            rankDbBuilder.setFailedCount(rankDbBuilder.getFailedCount() + 1);
        }
    }

    public void incrRankScoreByAdd(int score) {
        MatchArenaDB.DB_RankMatchArena.Builder rankDbBuilder = getDbBuilder().getRankMatchArenaBuilder();
        rankDbBuilder.setScore(rankDbBuilder.getScore() + score);
        rankDbBuilder.setDan(MatchArenaUtil.getScoreDan(rankDbBuilder.getScore()));
    }

}