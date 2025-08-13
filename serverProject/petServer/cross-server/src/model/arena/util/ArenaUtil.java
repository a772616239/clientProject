package model.arena.util;

import cfg.ArenaDan;
import cfg.ArenaDanObject;
import cfg.ArenaLeague;
import cfg.ArenaLeagueObject;
import cfg.ArenaRobotConfig;
import cfg.ArenaRobotConfigObject;
import com.google.protobuf.GeneratedMessageV3;
import common.GameConst.RankingName;
import common.GameConst.RedisKey;
import common.entity.RankingQuerySingleResult;
import common.entity.RankingScore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import model.arena.ArenaManager;
import model.arena.ArenaPlayerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import protocol.Arena.ArenaOpponent;
import protocol.Arena.ArenaPlayerSimpleInfo;
import protocol.Arena.ArenaPlayerTeamInfo;
import protocol.Arena.ArenaRankingPlayerInfo;
import protocol.Arena.ArenaRankingPlayerInfo.Builder;
import protocol.ArenaDB.DB_ArenaPlayerInfo;
import protocol.Battle.BattlePetData;
import protocol.PrepareWar.TeamNumEnum;
import protocol.ServerTransfer.CS_GS_TransArenaInfo;
import util.GameUtil;
import util.LogUtil;


/**
 * @author huhan
 * @date 2020/05/12
 */
public class ArenaUtil {

    /**
     * 获取一个Redis键的加锁键值
     *
     * @param key
     * @return
     */
    public static String getLockKey(String key) {
        return "Lock-" + key + "-";
    }

    public static String getPlayerLockKey(String playerIdx) {
        return getLockKey(RedisKey.ARENA_PLAYER_BASE_INFO) + playerIdx;
    }

    /**
     * 房间锁
     *
     * @return
     */
    public static String getArenaRoomLockKey(String roomId) {
        return getLockKey(RedisKey.ARENA_ROOM_INFO) + roomId;
    }

    /**
     * 排行榜结算房间锁key
     * @param roomId
     * @return
     */
    public static String getRoomRankingSettleLockKey(String roomId) {
        return getLockKey(RedisKey.ARENA_ROOM_RANKING_SETTLE_LIST) + roomId;
    }

    /**
     * 段位结算房间锁
     * @param roomId
     * @return
     */
    public static String getRoomDanSettleLockKey(String roomId) {
        return getLockKey(RedisKey.ARENA_ROOM_DAN_SETTLE_LIST) + roomId;
    }

    public static String getDanRoomCreateLockKey(int dan) {
        return RedisKey.ARENA_LOCK_DAN_CREATE_ROOM + dan;
    }

    public static String getRoomRankingName(String roomId) {
        return GameUtil.buildTransServerRankName(RankingName.ARENA_ROOM_RANKING) + roomId;
    }

    public static CS_GS_TransArenaInfo.Builder buildCsGsTrans(String playerIdx, int msgIdValue, GeneratedMessageV3.Builder builder) {
        CS_GS_TransArenaInfo.Builder newBuilder = CS_GS_TransArenaInfo.newBuilder();
        newBuilder.setPlayerIdx(playerIdx);
        newBuilder.setMsgId(msgIdValue);
        if (builder != null) {
            newBuilder.setMsgData(builder.build().toByteString());
        }
        return newBuilder;
    }

    /**
     * 将排行榜返回的消息,转化为排行榜更新
     *
     * @param result
     */
    public static ArenaRankingPlayerInfo buildArenaRankingPlayerInfo(RankingQuerySingleResult result) {
        if (result == null) {
            return null;
        }

        DB_ArenaPlayerInfo entity = ArenaPlayerManager.getInstance().getPlayerBaseInfo(result.getPrimaryKey());
        if (entity == null) {
            return null;
        }
        Builder builder = ArenaRankingPlayerInfo.newBuilder();
        builder.setSimpleInfo(buildArenaPlayerSimpleInfo(entity));
        builder.setRanking(result.getRanking());

        //TODO 先使用玩家排行榜分数
        builder.getSimpleInfoBuilder().setScore(result.getIntPrimaryScore());

        return builder.build();
    }

    public static int queryPlayerScore(String playerIdx) {
        DB_ArenaPlayerInfo entity = ArenaPlayerManager.getInstance().getPlayerBaseInfo(playerIdx);
        if (entity == null) {
            return 0;
        }
        return entity.getScore();
    }

    public static List<Integer> getDanUseDefinedTeams(int dan) {
        ArenaLeagueObject leagueObject = getArenaLeagueByDanId(dan);
        if (leagueObject == null) {
            return null;
        }

        List<Integer> result = new ArrayList<>();
        int[][] canUseTeamNumAndPetCount = leagueObject.getCanuseteamnumandpetcount();
        for (int[] ints : canUseTeamNumAndPetCount) {
            if (ints.length < 2) {
                continue;
            }

            if (ints[0] > TeamNumEnum.TNE_Arena_Attack_3_VALUE) {
                result.add(ints[0]);
            }
        }
        return result;
    }

    public static ArenaLeagueObject getArenaLeagueByDanId(int dan) {
        for (ArenaLeagueObject value : ArenaLeague._ix_id.values()) {
            if (GameUtil.inScope(value.getStartdan(), value.getEnddan(), dan)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 获取段位的起始积分
     *
     * @return
     */
    public static int getDanStartScore(int dan) {
        ArenaDanObject danCfg = ArenaDan.getById(dan);
        if (danCfg == null) {
            return 0;
        }
        return danCfg.getStartscore();
    }

    public static int randomInScope(int[] scope) {
        if (scope == null || scope.length < 2) {
            return 0;
        }
        return randomInScope(scope[0], scope[1]);
    }

    /**
     * 包含上界
     *
     * @param border_1
     * @param border_2
     * @return
     */
    public static int randomInScope(int border_1, int border_2) {
        int max = Math.max(border_1, border_2);
        int min = Math.min(border_1, border_2);
        if (max <= min) {
            return max;
        }

        return min + new Random().nextInt(max - min + 1);
    }

    public static int getDanMaxRobotSize(int dan) {
        List<ArenaRobotConfigObject> robotCfgList = getRobotCfgByDanId(dan);
        if (CollectionUtils.isEmpty(robotCfgList)) {
            return -1;
        }
        int robotCount = 0;
        for (ArenaRobotConfigObject robotCfg : robotCfgList) {
            robotCount += robotCfg.getNeedcount();
        }
        return robotCount;
    }

    public static List<ArenaRobotConfigObject> getRobotCfgByDanId(int danId) {
        List<ArenaRobotConfigObject> result = new ArrayList<>();
        for (ArenaRobotConfigObject value : ArenaRobotConfig._ix_id.values()) {
            if (value.getDan() == danId) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * -1 未找到
     *
     * @param dan
     * @return
     */
    public static int getDanRobotMinScore(int dan) {
        List<ArenaRobotConfigObject> robotCfgList = getRobotCfgByDanId(dan);
        if (CollectionUtils.isEmpty(robotCfgList)) {
            return -1;
        }

        int minScore = Integer.MAX_VALUE;
        for (ArenaRobotConfigObject robotCfg : robotCfgList) {
            int curMin = Math.min(robotCfg.getStartscore(), robotCfg.getEndscore());
            if (curMin < minScore) {
                minScore = curMin;
            }
        }
        return minScore;
    }

    /**
     * -1 未找到
     *
     * @param dan
     * @return
     */
    public static int getDanRobotMaxScore(int dan) {
        List<ArenaRobotConfigObject> robotCfgList = getRobotCfgByDanId(dan);
        if (CollectionUtils.isEmpty(robotCfgList)) {
            return -1;
        }

        int maxScore = Integer.MIN_VALUE;
        for (ArenaRobotConfigObject robotCfg : robotCfgList) {
            int curMax = Math.max(robotCfg.getStartscore(), robotCfg.getEndscore());
            if (curMax > maxScore) {
                maxScore = curMax;
            }
        }
        return maxScore;
    }


    public static ArenaRobotConfigObject getRobotCfgByDanAndScore(int dan, int score) {
        List<ArenaRobotConfigObject> robotCfgList = getRobotCfgByDanId(dan);
        if (CollectionUtils.isEmpty(robotCfgList)) {
            return null;
        }

        for (ArenaRobotConfigObject robotCfg : robotCfgList) {
            if (GameUtil.inScope(robotCfg.getStartscore(), robotCfg.getEndscore(), score)) {
                return robotCfg;
            }
        }
        return null;
    }

    /**
     * 获取段位房间的房间人数
     *
     * @param dan
     * @return
     */
    public static int getRoomMaxSize(int dan) {
        ArenaDanObject danObject = ArenaDan.getById(dan);
        if (danObject == null) {
            LogUtil.error("model.arenaplayer.ArenaUtil.getDanMaxSize, dan cfg is not exist, dan:" + dan);
            return 0;
        }
        return danObject.getRoommaxsize();
    }

    /**
     * 构建对手信息
     * @param baseInfo
     * @param directUp
     * @return
     */
    public static ArenaOpponent buildArenaOpponent(DB_ArenaPlayerInfo baseInfo, boolean directUp) {
        if (baseInfo == null) {
            return null;
        }

        ArenaPlayerSimpleInfo arenaOpponent = buildArenaPlayerSimpleInfo(baseInfo);
        if (arenaOpponent == null) {
            return null;
        }

        ArenaOpponent.Builder resultBuilder = ArenaOpponent.newBuilder();
        resultBuilder.setSimpleInfo(arenaOpponent);
        resultBuilder.setDerectUp(directUp);
        int ranking = ArenaManager.getInstance().queryPlayerRanking(baseInfo.getRoomId(), baseInfo.getBaseInfo().getPlayerIdx());
        resultBuilder.setRanking(ranking);

        if (resultBuilder.getSimpleInfo().getTitleId() == 0) {
            resultBuilder.getSimpleInfoBuilder().setTitleId(ArenaUtil.getDanLinkTitleId(resultBuilder.getSimpleInfo().getDan()));
        }
        return resultBuilder.build();
    }


    public static ArenaPlayerSimpleInfo buildArenaPlayerSimpleInfo(DB_ArenaPlayerInfo playerInfo) {
        if (playerInfo == null) {
            return null;
        }
        ArenaPlayerSimpleInfo.Builder builder = ArenaPlayerSimpleInfo.newBuilder();
        builder.setPlayerIdx(playerInfo.getBaseInfo().getPlayerIdx());
        builder.setAvatar(playerInfo.getBaseInfo().getAvatar());
        if (playerInfo.getBaseInfo().getName() != null) {
            builder.setName(playerInfo.getBaseInfo().getName());
        }
        builder.setLevel(playerInfo.getBaseInfo().getLevel());
        builder.setServerIndex(playerInfo.getBaseInfo().getServerIndex());
        builder.setDan(playerInfo.getDan());
        builder.setScore(playerInfo.getScore());
        builder.setFightAbility(playerInfo.getBaseInfo().getFightAbility());
        builder.setVipLv(playerInfo.getBaseInfo().getVipLv());
        builder.setShowPetId(playerInfo.getBaseInfo().getShowPetId());
        builder.setAvatarBorder(playerInfo.getBaseInfo().getAvatarBorder());
        builder.setAvatarBorderRank(playerInfo.getBaseInfo().getAvatarBorderRank());
        builder.setRobotCfgId(playerInfo.getRobotCfgId());
        builder.setTitleId(playerInfo.getBaseInfo().getTitleId());
        return builder.build();
    }

    /**
     * 判断一个玩家是否是机器人CS_GS_EnterMistPvpBattle
     *
     * @param playerInfo
     * @return
     */
    public static boolean isRobot(DB_ArenaPlayerInfo playerInfo) {
        if (playerInfo == null) {
            return true;
        }
        return ArenaRobotConfig.getById(playerInfo.getRobotCfgId()) != null;
    }

    /**
     * 计算总战力
     */
    public static long calculateTotalAbility(Collection<ArenaPlayerTeamInfo> teamsColl) {
        long totalAbility = 0;
        if (CollectionUtils.isEmpty(teamsColl)) {
            return 0;
        }

        for (ArenaPlayerTeamInfo teamInfo : teamsColl) {
            for (BattlePetData battlePetData : teamInfo.getPetsList()) {
                totalAbility += battlePetData.getAbility();
            }
        }
        return totalAbility;
    }


    public static int getPetMaxLv(Map<Integer, ArenaPlayerTeamInfo> newTeams) {
        int result = 1;
        if (MapUtils.isEmpty(newTeams)) {
            return result;
        }

        for (ArenaPlayerTeamInfo value : newTeams.values()) {
            for (BattlePetData battlePetData : value.getPetsList()) {
                if (battlePetData.getPetLevel() > result) {
                    result = battlePetData.getPetLevel();
                }
            }
        }
        return result;
    }

    public static List<ArenaRankingPlayerInfo> buildArenaRankingPlayerInfoByList(List<RankingQuerySingleResult> needBuildPlayerInfo) {
        if (CollectionUtils.isEmpty(needBuildPlayerInfo)) {
            return Collections.emptyList();
        }

        List<String> idxList = needBuildPlayerInfo.stream()
                .map(RankingScore::getPrimaryKey)
                .collect(Collectors.toList());

        Map<String, DB_ArenaPlayerInfo> infoMap = ArenaPlayerManager.getInstance().getPlayerInfoMap(idxList);
        if (MapUtils.isEmpty(infoMap)) {
            return Collections.emptyList();
        }

        List<ArenaRankingPlayerInfo> result = new ArrayList<>();
        for (RankingQuerySingleResult rankingResult : needBuildPlayerInfo) {
            DB_ArenaPlayerInfo playerInfo = infoMap.get(rankingResult.getPrimaryKey());
            if (playerInfo == null) {
                LogUtil.error("model.arena.util.ArenaUtil.buildArenaRankingPlayerInfoByList, arena db info is not exist, playerIdx:" + rankingResult.getPrimaryKey());
                continue;
            }
            Builder builder = ArenaRankingPlayerInfo.newBuilder();
            builder.setSimpleInfo(buildArenaPlayerSimpleInfo(playerInfo));
            builder.setRanking(rankingResult.getRanking());

            //TODO 先使用玩家排行榜分数
            builder.getSimpleInfoBuilder().setScore(rankingResult.getIntPrimaryScore());

            result.add(builder.build());
        }
        return result;
    }

    public static int getDanLinkTitleId(int dan) {
        ArenaDanObject cfg = ArenaDan.getById(dan);
        return cfg == null ? 0 : cfg.getTitleid();
    }
}
