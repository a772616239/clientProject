package model.arena;

import cfg.ArenaConfig;
import cfg.ArenaLeague;
import cfg.ArenaLeagueObject;
import cfg.ArenaRobotConfig;
import cfg.ArenaRobotConfigObject;
import cfg.ServerStringRes;
import common.GameConst;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.battle.BattleUtil;
import model.team.dbCache.teamCache;
import model.team.util.TeamsUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.Arena.ArenaPlayerTeamInfo;
import protocol.Arena.ArenaRankingPlayerInfo;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Common.LanguageEnum;
import protocol.PrepareWar.TeamNumEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020.03.09
 */
public class ArenaUtil {

    /**
     * 此方法不包含羁绊战力
     * @param playerIdx
     * @return
     */
//    public static long getArenaDefinesTotalAbility(String playerIdx) {
//        long totalAbility = 0;
//        totalAbility += teamCache.getInstance().getTeamFightAbility(playerIdx, TeamNumEnum.TNE_Arena_Defense_1);
//        totalAbility += teamCache.getInstance().getTeamFightAbility(playerIdx, TeamNumEnum.TNE_Arena_Defense_2);
//        totalAbility += teamCache.getInstance().getTeamFightAbility(playerIdx, TeamNumEnum.TNE_Arena_Defense_3);
//        return totalAbility;
//    }

    /**
     * 获取竞技场防守队伍战斗战力
     *
     * @param playerIdx
     * @return
     */
    public static long getArenaDefinesBattleTotalAbility(String playerIdx, int dan) {
        long result = 0;
        if (StringUtils.isEmpty(playerIdx)) {
            return result;
        }

        List<Integer> definedTeamNum = getDefinedTeamNum(dan);
        if (CollectionUtils.isEmpty(definedTeamNum)) {
            return result;
        }

        List<BattlePetData> totalPets = new ArrayList<>();
        for (Integer teamNum : definedTeamNum) {
            List<BattlePetData> battlePetDataList = teamCache.getInstance().buildBattlePetData(playerIdx, TeamNumEnum.forNumber(teamNum), BattleSubTypeEnum.BSTE_Arena);
            if (CollectionUtils.isNotEmpty(battlePetDataList)) {
                totalPets.addAll(battlePetDataList);
            }
        }
        return BattleUtil.calculateTotalAbility(totalPets);
    }


//    public static RankingQueryResult queryRanking(int page, int pageSize) {
//        RankingQueryRequest query = new RankingQueryRequest();
//        query.setRank(RankingName.RN_ARENA_LOCAL_RANKING);
//        query.setServerIndex(ServerConfig.getInstance().getServer());
//        query.setPage(page);
//        query.setSize(pageSize);
//        HttpRankingResponse result = HttpRequestUtil.queryRanking(query);
//        if (result == null) {
//            LogUtil.error("query arena ranking result is null");
//            return null;
//        }
//        return result.getData();
//    }
//    /**
//     * 更新排行榜
//     * @param scores 需要更新的记录
//     */
//    public static void updateLocalRanking(RankingScore... scores) {
//        RankingUpdateRequest request = new RankingUpdateRequest(RankingName.RN_ARENA_LOCAL_RANKING);
//        request.setServerIndex(ServerConfig.getInstance().getServer());
//        request.addAllItems(scores);
//        if (!HttpRequestUtil.updateRanking(request)) {
//            LogUtil.info("failed to update arena ranking");
//        }
//    }
//
//    public static ArenaRankingPlayerInfo buildRankingInfo(RankingQuerySingleResult rankingResult) {
//        if (rankingResult == null) {
//            return null;
//        }
//
//        playerEntity player = playerCache.getInstance().getPlayerByUserId(rankingResult.getPrimaryKey());
//        if (player == null) {
//            return null;
//        }
//
//        Builder builder = ArenaRankingPlayerInfo.newBuilder();
//        builder.setPlayerIdx(player.getIdx());
//        builder.setRanking(rankingResult.getRanking());
//        builder.setAvatarId(player.getAvatar());
//        builder.setLv(player.getLevel());
//        builder.setName(player.getName());
//        //竞技场小队战斗力
//        builder.setFightAbility(teamCache.getInstance().getTeamFightAbility(player.getIdx(), TeamNumEnum.TNE_Arena_Attack_1));
//        builder.setScore(rankingResult.getPrimaryScore());
//        return builder.build();
//    }
//
//    public static OpponentSimpleInfo buildOpponentSimpleInfo(String playerIdx) {
//        playerEntity player = playerCache.getByIdx(playerIdx);
//        if (player == null) {
//            return null;
//        }
//
//        OpponentSimpleInfo.Builder builder = OpponentSimpleInfo.newBuilder();
//        builder.setIdx(playerIdx);
//        builder.setAvatar(player.getAvatar());
//        builder.setLv(player.getLevel());
//        builder.setName(player.getName());
//        builder.setScore(player.getDb_data().getArenaData().getScore());
//        builder.setRanking(ArenaManager.getInstance().queryPlayerRanking(playerIdx));
//        builder.setFightAbility(teamCache.getInstance().getTeamFightAbility(playerIdx, TeamNumEnum.TNE_Arena_Attack_1));
//        return builder.build();
//    }


    public static ArenaLeagueObject getArenaLeagueCfgByDanId(int dan) {
        for (ArenaLeagueObject value : ArenaLeague._ix_id.values()) {
            if (GameUtil.inScope(value.getStartdan(), value.getEnddan(), dan)) {
                return value;
            }
        }

        return null;
    }

    public static int queryPlayerDan(String playerIdx) {
        arenaEntity entity = arenaCache.getInstance().getEntity(playerIdx);
        if (entity == null) {
            return 0;
        }

        return entity.getDbBuilder().getDan();
    }

    /**
     * 获取竞技场进攻队伍对应的防守队伍
     *
     * @return
     */
    public static int getArenaAttackTeamNumLinkDefinedTeamNum(int attackTeamNum) {
        if (!TeamsUtil.isArenaAttack(attackTeamNum)) {
            return -1;
        }

        return attackTeamNum + (TeamNumEnum.TNE_Arena_Defense_1_VALUE - TeamNumEnum.TNE_Arena_Attack_1_VALUE);
    }

    /**
     * 根据段位获取进攻队伍
     * @param dan =0时,使用默认段位队伍配置
     * @return
     */
    public static List<Integer> getAttackTeamNum(int dan) {
        ArenaLeagueObject leagueCfg = getArenaLeagueCfgByDanId(dan);
        //如果未取到联赛的的配置使用默认段位
        if (leagueCfg == null && dan == 0) {
            leagueCfg = getArenaLeagueCfgByDanId(ArenaConfig.getById(GameConst.CONFIG_ID).getStartdan());
        }

        if (leagueCfg == null) {
            LogUtil.error("model.arena.ArenaUtil.getAttackTeamNum, can not get league config by dan:" + dan);
            return null;
        }

        List<Integer> result = new ArrayList<>();
        for (int[] ints : leagueCfg.getCanuseteamnumandpetcount()) {
            if (ints.length < 2) {
                continue;
            }
            if (TeamsUtil.isArenaAttack(ints[0])) {
                result.add(ints[0]);
            }
        }
        return result;
    }

    /**
     * 根据段位获取防御队伍
     *
     * @param dan
     * @return
     */
    public static List<Integer> getDefinedTeamNum(int dan) {
        List<Integer> teamNum = getAttackTeamNum(dan);
        if (CollectionUtils.isEmpty(teamNum)) {
            return Collections.emptyList();
        }
        return teamNum.stream().map(ArenaUtil::getArenaAttackTeamNumLinkDefinedTeamNum).collect(Collectors.toList());
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


    public static String getRobotName(int robotCfgId, LanguageEnum language) {
        ArenaRobotConfigObject robotCfg = ArenaRobotConfig.getById(robotCfgId);
        if (robotCfg == null || language == null) {
            return null;
        }

        return ServerStringRes.getContentByLanguage(robotCfg.getNamestr(), language);
    }

    /**
     * 机器人名字多语言处理
     * @param totalInfo
     * @param language
     * @return
     */
    public static ArenaOpponentTotalInfo dealRobotName(ArenaOpponentTotalInfo totalInfo, LanguageEnum language) {
        if (totalInfo == null) {
            return null;
        }

        if (language == null
                || ArenaRobotConfig.getById(totalInfo.getOpponnentInfo().getSimpleInfo().getRobotCfgId()) == null) {
            return totalInfo;
        }

        String newName = ArenaUtil.getRobotName(totalInfo.getOpponnentInfo().getSimpleInfo().getRobotCfgId(), language);
        if (org.apache.commons.lang.StringUtils.isNotBlank(newName)) {
            ArenaOpponentTotalInfo.Builder builder = totalInfo.toBuilder();
            builder.getOpponnentInfoBuilder().getSimpleInfoBuilder().setName(newName);
            return builder.build();
        }
        return totalInfo;
    }
}
