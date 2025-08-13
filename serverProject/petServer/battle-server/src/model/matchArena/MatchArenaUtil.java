package model.matchArena;

import cfg.MatchArenaConfig;
import cfg.MatchArenaConfigObject;
import cfg.MatchArenaDanConfig;
import cfg.MatchArenaDanConfigObject;
import common.GameConst;
import common.GlobalTick;
import common.TimeUtil;
import util.LogUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author huhan
 * @date 2021/05/26
 */
public class MatchArenaUtil {

    private static final Object MONITOR = new Object();


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

   /* *//**
     * 最大玩家匹配分差
     *//*
    private static int maxPlayerScoreDiff;

    public static int getMaxPlayerScoreDiff(ArenaRankPlayer matchPlayer) {
        if (maxPlayerScoreDiff == 0) {
            synchronized (MONITOR) {
                if (maxPlayerScoreDiff == 0) {
                    MatchArenaConfigObject matchArenaCfg = MatchArenaConfig.getById(GameConst.ConfgId);
                    for (int[] matchRule : matchArenaCfg.getMatchrules()) {
                        if (matchRule[1] > maxPlayerScoreDiff) {
                            maxPlayerScoreDiff = matchRule[1];
                        }
                    }
                }
            }
        }
        return maxPlayerScoreDiff;
    }*/

    public static int getMaxPlayerScoreDiff(ArenaRankPlayer matchPlayer) {
        MatchArenaDanConfigObject cfg = MatchArenaDanConfig.getById(matchPlayer.getDan());
        if (cfg==null) {
            return 0;
        }
        long matchTime= GlobalTick.getInstance().getCurrentTime()-matchPlayer.getStartMatchTime();
        long diff = matchTime / cfg.getExpandmatchinterval() + cfg.getOnceexpandscore();
        return (int) Math.min(diff,cfg.getMaxscorediff());
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

    /**
     * 最大匹配玩家时长
     */
    private static long maxMatchTime;

    public static long getMaxMatchTime() {
        if (maxMatchTime == 0) {
            synchronized (MONITOR) {
                if (maxMatchTime == 0) {
                    for (int[] matchRule : MatchArenaConfig.getById(GameConst.ConfgId).getMatchrules()) {
                        if (matchRule[0] > maxMatchTime) {
                            maxMatchTime = matchRule[0];
                        }
                    }
                    maxMatchTime = maxMatchTime * TimeUtil.MS_IN_A_S;
                }
            }
        }
        return maxMatchTime;
    }

    /**
     * 玩家匹配Cd
     */
    private static long playerMatchCd;

    public static long getPlayerMatchCd() {
        if (playerMatchCd == 0) {
            synchronized (MONITOR) {
                if (playerMatchCd == 0) {
                    long cdS = MatchArenaConfig.getById(GameConst.ConfgId).getPlayermatchcd();
                    playerMatchCd = cdS * TimeUtil.MS_IN_A_S;
                }
            }
        }
        return playerMatchCd;
    }

}
