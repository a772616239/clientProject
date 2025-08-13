package model.matchArena;

import cfg.MatchArenaConfig;
import cfg.MatchArenaConfigObject;
import cfg.MatchArenaDanConfig;
import cfg.MatchArenaDanConfigObject;
import common.GameConst;
import common.GameConst.RedisKey;
import common.GlobalTick;
import common.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import model.obj.BaseObj;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PlayerBaseInfo;
import protocol.MatchArenaDB.MatchArenaMatchInfo;
import protocol.MatchArenaDB.MatchArenaTeamInfo;
import protocol.MatchArenaDB.RedisMatchArenaMatchInfo;
import protocol.ServerTransfer.PvpBattlePlayerInfo;
import static util.JedisUtil.jedis;
import util.LogUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author huhan
 * @date 2021/05/24
 */
@Getter
@Setter
public class ArenaRankPlayer extends BaseObj {
    private PlayerBaseInfo playerBaseInfo;
    private MatchArenaTeamInfo teamInfo;
    private int score;
    private long startMatchTime;
    private int fromSvrIndex;
    private int losingStreak;
    private int dan;
    /**
     * 匹配机器人时间
     */
    private long matchRobotTime;

    private final List<MatchArenaMatchInfo> matchPlayerInfoList = new ArrayList<>();
    private long nextCanMatchRobotTime;

    public ArenaRankPlayer() {
    }

    public String getPlayerIdx() {
        return this.playerBaseInfo == null ? "" : this.playerBaseInfo.getPlayerId();
    }

    /**
     * 是否与另以玩家满足分值匹配
     *
     * @param player
     * @return
     */
    public boolean matchScore(ArenaRankPlayer player) {
        if (player == null) {
            return false;
        }

        //分值是否满足条件
        int scoreDiff = getScoreDiff();
        if (scoreDiff == -1) {
            return false;
        }
        if (player.getScore() <= getScore() + scoreDiff
                && player.getScore() >= getScore() - scoreDiff) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否能匹配当前玩家
     * 已经战斗的玩家在x分钟内不会匹配,且w场次后可以继续匹配
     *
     * @return
     */
    public boolean matchPlayer(ArenaRankPlayer player) {
        if (player == null) {
            return false;
        }

        //已经战斗的玩家在x分钟内不会匹配,且w场次后可以继续匹配
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        for (MatchArenaMatchInfo matchInfo : this.matchPlayerInfoList) {
            if (Objects.equals(player.getPlayerIdx(), matchInfo.getPlayerIdx())
                    && (currentTime - matchInfo.getLastMatchTime()) <= MatchArenaUtil.getPlayerMatchCd()) {
//                LogUtil.debug("MatchArenaPlayer.matchPlayer, targetPlayer:" + player.getPlayerIdx()
//                        + ", is in playerIdx:" + getPlayerIdx() + ",match cd");
                return false;
            }
        }

        return true;
    }

    /**
     * 获取玩家匹配的分值差
     *
     * @return
     */
    public synchronized int getScoreDiff() {
        MatchArenaConfigObject matchArenaCfg = MatchArenaConfig.getById(GameConst.ConfgId);
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (this.startMatchTime == 0) {
            this.startMatchTime = GlobalTick.getInstance().getCurrentTime();
            return matchArenaCfg.getMatchrules()[0][1];
        }

        long time_s = (currentTime - this.startMatchTime) / TimeUtil.MS_IN_A_S;
        for (int[] matchRule : matchArenaCfg.getMatchrules()) {
            if (time_s > matchRule[0]) {
                continue;
            }

            if (time_s <= matchRule[0]) {
                return matchRule[1];
            }
        }

        LogUtil.info("model.matchArena.MatchArenaPlayer.getScoreDiff failed, player start match:"
                + this.startMatchTime + ", match robot time:" + this.matchRobotTime);
        return -1;
    }

    public void resetMatchTime() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        this.startMatchTime = currentTime;
        //随机延时后,设置匹配机器人的时间
        long delayS = RandomUtils.nextInt(MatchArenaConfig.getById(GameConst.ConfgId).getRobotdelay());
        long robotRandomDelayTime = delayS * TimeUtil.MS_IN_A_S;
        this.matchRobotTime = currentTime + MatchArenaUtil.getMaxMatchTime() + robotRandomDelayTime;
    }

    public boolean canMatchRobot() {
        MatchArenaDanConfigObject cfg = MatchArenaDanConfig.getById(getDan());
        if (cfg == null) {
            return true;
        }
        if (cfg.getCanmatchrobottime() == -1) {
            return false;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();
       /* return this.matchRobotTime != 0
                && currentTime > matchRobotTime
                && currentTime > this.nextCanMatchRobotTime;*/
        return (long) cfg.getCanmatchrobottime() * TimeUtil.MS_IN_A_S < currentTime - startMatchTime;
    }

    public PvpBattlePlayerInfo buildPvpPlayerInfo(int camp, int battlePetLimit) {
        if (this.playerBaseInfo == null || this.teamInfo == null) {
            LogUtil.error("model.matchArena.MatchArenaPlayer.buildPvpPlayerInfo, player info is null");
            return null;
        }
        PvpBattlePlayerInfo.Builder resultBuilder = PvpBattlePlayerInfo.newBuilder();
        resultBuilder.setPlayerInfo(this.playerBaseInfo);
        resultBuilder.setFromSvrIndex(getFromSvrIndex());
        resultBuilder.setCamp(camp);
        resultBuilder.addAllPetList(this.teamInfo.getPetListList());
        resultBuilder.addAllPlayerSkillIdList(this.teamInfo.getPlayerSkillIdListList());
        resultBuilder.addExtendProp(ExtendProperty.newBuilder().setCamp(camp).setBattlePetLimit(battlePetLimit).build());
        return resultBuilder.build();
    }

    public void successMatchPlayer(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        int playerMatchBattleTimesCd = MatchArenaConfig.getById(GameConst.ConfgId).getPlayermatchbattletimescd();
        if (this.matchPlayerInfoList.size() > playerMatchBattleTimesCd) {
            this.matchPlayerInfoList.remove(0);
        }
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        MatchArenaMatchInfo newMatchInfo = MatchArenaMatchInfo.newBuilder()
                .setPlayerIdx(playerIdx)
                .setLastMatchTime(currentTime)
                .build();
        this.matchPlayerInfoList.add(newMatchInfo);
        LogUtil.info("model.matchArena.MatchArenaPlayer.successMatchPlayer, playerIdx:" + getPlayerIdx()
                + ", match player:" + playerIdx);
    }

    public void successMatchRobot() {
        long robotMatchCd = ((long) MatchArenaConfig.getById(GameConst.ConfgId).getPlayermatchcd()) * TimeUtil.MS_IN_A_S;
        this.nextCanMatchRobotTime = GlobalTick.getInstance().getCurrentTime() + robotMatchCd;
        LogUtil.info("model.matchArena.MatchArenaPlayer.successMatchRobot, playerIdx:" + getPlayerIdx()
                + ", next can match robot time:" + this.nextCanMatchRobotTime);
    }

    public void updateMatchInfo() {
        RedisMatchArenaMatchInfo.Builder newBuilder = RedisMatchArenaMatchInfo.newBuilder();
        newBuilder.addAllMatchInfo(this.matchPlayerInfoList);
        newBuilder.setNextCanMatchRobotTime(this.nextCanMatchRobotTime);
        jedis.hset(RedisKey.MatchArenaPlayerMatchInfo.getBytes(StandardCharsets.UTF_8),
                this.playerBaseInfo.getPlayerId().getBytes(StandardCharsets.UTF_8), newBuilder.build().toByteArray());
    }

    public void addAllMatchInfo(List<MatchArenaMatchInfo> matchInfoList) {
        if (CollectionUtils.isEmpty(matchInfoList)) {
            return;
        }
        this.matchPlayerInfoList.addAll(matchInfoList);
    }

    @Override
    public String getIdx() {
        return getPlayerIdx();
    }

    @Override
    public void setIdx(String idx) {
//        this.playerIdx = playerIdx;
    }

    @Override
    public String getClassType() {
        return "MatchArenaPlayer";
    }
}

