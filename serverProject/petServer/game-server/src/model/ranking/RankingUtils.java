package model.ranking;

import common.GameConst.RankingName;
import common.tick.GlobalTick;
import org.apache.commons.lang.StringUtils;
import protocol.Activity.EnumRankingType;
import protocol.Server.ServerActivity;
import util.GameUtil;
import util.LogUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static common.GameConst.MAX_TIME;
import static common.GameConst.RankTimeDivider;

/**
 * @author huhan
 * @date 2020/12/14
 */
public class RankingUtils {
    public static final String ARENA_LOCAL_DAN_RANK_PREFIX = RankingName.RN_ARENA_SCORE_LOCAL_DAN + "-dan-";

    private RankingUtils() {
    }

    /**
     * 用于保存排行榜类型枚举与排行榜名的映射关系
     */
    private static final Map<EnumRankingType, String> RANKING_TYPE_NAME_MAP;

    static {
        Map<EnumRankingType, String> tempMap = new HashMap<>();
        tempMap.put(EnumRankingType.ERT_Ability, RankingName.RN_Statistics_Ability);
        tempMap.put(EnumRankingType.ERT_PetAbility, RankingName.RN_PET_ABILITY);
        tempMap.put(EnumRankingType.ERT_PlayerLevel, RankingName.RN_PLAYER_LV);
        tempMap.put(EnumRankingType.ERT_MainLine, RankingName.RN_MainLinePassed);
        tempMap.put(EnumRankingType.ERT_Spire, RankingName.RN_EndlessSpire);
        tempMap.put(EnumRankingType.ERT_ArenaScoreLocal, RankingName.RN_ARENA_DAN_SCORE);
        tempMap.put(EnumRankingType.ERT_ArenaScoreCross, RankingName.RN_ARENA_DAN_SCORE_CROSS);
        tempMap.put(EnumRankingType.ERT_ArenaGainScore, RankingName.RN_ARENA_GAIN_SCORE);
        tempMap.put(EnumRankingType.ERT_ArenaScoreLocalDan, RankingName.RN_ARENA_SCORE_LOCAL_DAN);
        tempMap.put(EnumRankingType.ERT_MineScore, RankingName.RN_MINE_SCORE);
        tempMap.put(EnumRankingType.ERT_ActivityBoss_Damage, RankingName.RN_ActivityBoss_Damage);
        tempMap.put(EnumRankingType.ERT_DemonDescendsScore, RankingName.RN_DEMON_DESCENDS_SCORE);
        tempMap.put(EnumRankingType.ERT_NewForeignInvasion, RankingName.RN_New_ForInv_Score);
        tempMap.put(EnumRankingType.ERT_TheWar_KillMonster, RankingName.RN_TheWar_KillMonsterCount);
        tempMap.put(EnumRankingType.ERT_Team1Ability, RankingName.RN_Team1Ability);

        tempMap.put(EnumRankingType.ERT_NaturePet, RankingName.RN_NaturePet);
        tempMap.put(EnumRankingType.ERT_WildPet, RankingName.RN_WildPet);
        tempMap.put(EnumRankingType.ERT_AbyssPet, RankingName.RN_AbyssPet);
        tempMap.put(EnumRankingType.ERT_HellPet, RankingName.RN_HellPet);
        tempMap.put(EnumRankingType.ERT_GloryRoad, RankingName.RN_GloryRoad);
        tempMap.put(EnumRankingType.ERT_RichMan, RankingName.RN_RichMan);

        tempMap.put(EnumRankingType.ERT_MatchArena_Local, RankingName.RN_MatchArena_Local);
        tempMap.put(EnumRankingType.ERT_MatchArena_Cross, RankingName.RN_MatchArena_Cross);

        tempMap.put(EnumRankingType.ERT_Lt_Score, RankingName.RN_Lt_Score);
        tempMap.put(EnumRankingType.ERT_Lt_SerialWin, RankingName.RN_Lt_SerialWin);
        tempMap.put(EnumRankingType.ERT_Lt_Duel, RankingName.RN_Lt_Duel);
        tempMap.put(EnumRankingType.ERT_FestivalBoss, RankingName.RN_FestivalBoss);
        tempMap.put(EnumRankingType.ERT_MagicThronDamage, RankingName.RN_MagicThronMaxDamage);
        tempMap.put(EnumRankingType.ERT_PetAvoidance, RankingName.RN_PetAvoidance);
        tempMap.put(EnumRankingType.ERT_ConsumeCoupon, RankingName.RN_ConsumeCoupon);
        tempMap.put(EnumRankingType.ERT_Recharge, RankingName.RN_Recharge);

        RANKING_TYPE_NAME_MAP = Collections.unmodifiableMap(tempMap);
    }


    public static  String getMagicRankName(int areaId) {
        return RankingName.RN_MagicThronMaxDamage + "-areaId-" + areaId;
    }

    /**
     * 获取竞技场本服本段位排行榜的RankName
     * @param dan 段位
     * @return
     */
    public static String getArenaScoreLocalDanRankName(int dan) {
        return ARENA_LOCAL_DAN_RANK_PREFIX + dan;
    }

    public static boolean isArenaScoreLocalDanRank(String rankName) {
        return rankName.startsWith(ARENA_LOCAL_DAN_RANK_PREFIX);
    }

    public static int getArenaScoreLocalDanRankDan(String rankName) {
        return Integer.parseInt(rankName.replace(ARENA_LOCAL_DAN_RANK_PREFIX, ""));
    }

    /**
     * 返回当前积分带上一个小数，（当前时间越大小数越小）
     * @param score
     * @return
     */
    public  static  double getRankScoreWithTimeDesc(int score){
        return score +((MAX_TIME- GlobalTick.getInstance().getCurrentTime()) /RankTimeDivider);
    }


    public static String getRankingTypeDefaultName(EnumRankingType rankingType) {
        if (rankingType == null) {
            return "";
        }
        String rankingName = RANKING_TYPE_NAME_MAP.get(rankingType);
        if (StringUtils.isEmpty(rankingName)) {
            LogUtil.error("RankingUtils.getRankingTypeDefaultName, can not find ranking name, ranking type:" + rankingType);
            rankingName = "";
        }
        return rankingName;
    }

    /**
     * 需要加上活动id的排行榜活动,用去区分不同的活动,单独存储
     */
    private static final Set<EnumRankingType> SEPARATE_STORAGE_RANKING_TYPE;

    static {
        Set<EnumRankingType> temp = new HashSet<>();
        temp.add(EnumRankingType.ERT_ArenaGainScore);
        temp.add(EnumRankingType.ERT_MineScore);
//        temp.add(EnumRankingType.ERT_ActivityBoss_Damage);
        temp.add(EnumRankingType.ERT_DemonDescendsScore);
        temp.add(EnumRankingType.ERT_RichMan);
        temp.add(EnumRankingType.ERT_FestivalBoss);
        temp.add(EnumRankingType.ERT_Recharge);
        temp.add(EnumRankingType.ERT_ConsumeCoupon);
        temp.add(EnumRankingType.ERT_PetAvoidance);

        SEPARATE_STORAGE_RANKING_TYPE = Collections.unmodifiableSet(temp);
    }

    public static final String ACTIVITY_RANKING_SPLIT = "_";

    public static boolean isSeparateStorageActivityRanking(EnumRankingType rankingType) {
        if (rankingType == null) {
            return false;
        }
        return SEPARATE_STORAGE_RANKING_TYPE.contains(rankingType);
    }

    public static String getActivityRankingName(ServerActivity activity) {
        if (activity == null
                || activity.getRankingType() == null
                || activity.getRankingType() == EnumRankingType.ERT_Null) {
            return null;
        }

        String rankingName = getRankingTypeDefaultName(activity.getRankingType());
        if (SEPARATE_STORAGE_RANKING_TYPE.contains(activity.getRankingType())) {
            rankingName += (ACTIVITY_RANKING_SPLIT + activity.getActivityId());
        }
        return rankingName;
    }

    public static long getActivityIdByRankingName(String rankingName) {
        if (StringUtils.isEmpty(rankingName)) {
            return 0L;
        }

        String[] split = rankingName.split(ACTIVITY_RANKING_SPLIT);
        if (split.length < 2) {
            return 0L;
        }
        return GameUtil.stringToLong(split[1], 0L);
    }

    /**
     * 只需要清空活动排行榜
     *
     * @return
     */
    public static boolean needClearRanking(String rankingName) {
        return getActivityIdByRankingName(rankingName) != 0;
    }

    /**
     *
     * @return
     */
    public static String getLtSerialWinRankName(int sceneId) {
        return RankingName.RN_Lt_SerialWin + "-" + sceneId;
    }
}
