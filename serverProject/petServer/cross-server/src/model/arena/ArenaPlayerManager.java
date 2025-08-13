package model.arena;

import cfg.ArenaConfig;
import cfg.ArenaDan;
import cfg.ArenaDanObject;
import cfg.ArenaRobotConfig;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GameConst.RedisKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.arena.entity.ArenaTotalInfo;
import model.arena.util.ArenaUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Arena.ArenaOpponent;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.Arena.ArenaOpponentTotalInfo.Builder;
import protocol.Arena.ArenaPlayerTeamInfo;
import protocol.ArenaDB.DB_ArenaDefinedTeamsInfo;
import protocol.ArenaDB.DB_ArenaPlayerBaseInfo;
import protocol.ArenaDB.DB_ArenaPlayerInfo;
import protocol.ServerTransfer.GS_CS_JoinArena;
import util.JedisUtil;
import static util.JedisUtil.jedis;
import util.LogUtil;

/**
 * 管理竞技场玩家,用于新增和更新玩家信息
 *
 * @author huhan
 * @date 2020/06/15
 */
public class ArenaPlayerManager {
    private static ArenaPlayerManager instance;

    public static ArenaPlayerManager getInstance() {
        if (instance == null) {
            synchronized (ArenaPlayerManager.class) {
                if (instance == null) {
                    instance = new ArenaPlayerManager();
                }
            }
        }
        return instance;
    }

    private ArenaPlayerManager() {
    }

    /**
     * 合并玩家的最新信息到redis
     *
     * @param newInfo
     * @return
     */
    public void syncMergePlayerInfoToRedis(GS_CS_JoinArena newInfo, int newSvrIndex) {
        if (newInfo == null || newSvrIndex <= 0) {
            LogUtil.error("ArenaPlayerManager.updatePlayerInfo, can not update playerInfo, newInfo:" + newInfo + ", newSvrIndex:" + newSvrIndex);
            return;
        }

        JedisUtil.syncExecBooleanSupplier(ArenaUtil.getPlayerLockKey(newInfo.getBaseInfo().getPlayerIdx())
                , () -> {
                    mergePlayerInfoToRedis(newInfo.getBaseInfo(), newSvrIndex);
                    mergePlayerDefinedTeamsInfoToRedis(newInfo.getBaseInfo().getPlayerIdx(), newInfo.getDefinedTeamsList());
                    return true;
                });
    }

    public void mergePlayerInfoToRedis(DB_ArenaPlayerBaseInfo baseInfo, int newSvrIndex) {
        if (baseInfo == null || newSvrIndex <= 0) {
            return;
        }

        DB_ArenaPlayerInfo playerInfo = getPlayerBaseInfo(baseInfo.getPlayerIdx());
        DB_ArenaPlayerInfo.Builder infoBuilder = null;
        if (playerInfo == null) {
            LogUtil.info("player:" + baseInfo.getPlayerIdx() + ", arena base info is not exit, create new DB_ArenaPlayerInfo");
            infoBuilder = createNewPlayerInfoBuilder();
        } else {
            infoBuilder = playerInfo.toBuilder();
        }

        infoBuilder.setBaseInfo(baseInfo);
        infoBuilder.setLastLoginSIndex(newSvrIndex);

        updatePlayerInfoToRedis(infoBuilder.build());
    }

    private DB_ArenaPlayerInfo.Builder createNewPlayerInfoBuilder() {
        DB_ArenaPlayerInfo.Builder result = DB_ArenaPlayerInfo.newBuilder();
        result.setDan(ArenaConfig.getById(GameConst.ConfigId).getStartdan());
        ArenaDanObject danCfg = ArenaDan.getById(result.getDan());
        if (danCfg == null) {
            LogUtil.error("model.arena.ArenaPlayerManager.createNewBaseInfoBuilder, dan cfg is not exist, dan:" + result.getDan());
        } else {
            result.setScore(danCfg.getStartscore());
        }
        return result;
    }

    public void mergePlayerDefinedTeamsInfoToRedis(String playerIdx, Collection<ArenaPlayerTeamInfo> teamsList) {
        if (CollectionUtils.isEmpty(teamsList) || StringUtils.isBlank(playerIdx)) {
            return;
        }

        DB_ArenaDefinedTeamsInfo teamsInfo = getPlayerDefinedTeamsInfo(playerIdx);
        DB_ArenaDefinedTeamsInfo.Builder teamsBuilder = null;
        if (teamsInfo == null) {
            LogUtil.info("player:" + playerIdx + ", arena defined teams info is not exist, create new DB_ArenaPlayerInfo");
            teamsBuilder = DB_ArenaDefinedTeamsInfo.newBuilder().setPlayerIdx(playerIdx);
        } else {
            teamsBuilder = teamsInfo.toBuilder();
        }

        for (ArenaPlayerTeamInfo teamInfo : teamsList) {
            teamsBuilder.putDefinedTeams(teamInfo.getTeanNumValue(), teamInfo);
        }
        updatePlayerDefinedTeamsInfoToRedis(teamsBuilder.build());
    }

    /**
     * 更新玩家队伍信息到redis,
     *
     * @return
     */
    public boolean updatePlayerDefinedTeamsInfoToRedis(DB_ArenaDefinedTeamsInfo teamsInfo) {
        if (teamsInfo == null) {
            LogUtil.error("entity.ArenaPlayerManager.updatePlayerDefinedTeamsInfoToRedis, error params, playerInfo is null");
            return false;
        }

        jedis.hset(RedisKey.ARENA_PLAYER_DEFINED_TEAMS_INFO.getBytes(), teamsInfo.getPlayerIdx().getBytes(), teamsInfo.toByteArray());
        return true;
    }

    /**
     * 更新玩家队伍信息到redis,
     *
     * @return
     */
    public boolean syncUpdatePlayerDefinedTeamsInfoToRedis(DB_ArenaDefinedTeamsInfo teamsInfo) {
        if (teamsInfo == null) {
            return false;
        }

        return JedisUtil.syncExecBooleanSupplier(ArenaUtil.getPlayerLockKey(teamsInfo.getPlayerIdx())
                , () -> updatePlayerDefinedTeamsInfoToRedis(teamsInfo));
    }

    /**
     * 更新玩家队伍信息
     */
    public void syncMergePlayerDefinedTeamsInfoToRedis(String playerIdx, Collection<ArenaPlayerTeamInfo> teamsList) {
        if (CollectionUtils.isEmpty(teamsList) || StringUtils.isBlank(playerIdx)) {
            return;
        }

        JedisUtil.syncExecBooleanSupplier(ArenaUtil.getPlayerLockKey(playerIdx), () -> {
            mergePlayerDefinedTeamsInfoToRedis(playerIdx, teamsList);
            return true;
        });
    }

    /**
     * 更新玩家基本信息到redis,
     *
     * @return
     */
    public boolean updatePlayerInfoToRedis(DB_ArenaPlayerInfo playerInfo) {
        if (playerInfo == null) {
            LogUtil.error("entity.ArenaPlayerManager.updatePlayerBaseInfoToRedis, error params, playerInfo is null");
            return false;
        }

        jedis.hset(RedisKey.ARENA_PLAYER_BASE_INFO.getBytes(), playerInfo.getBaseInfo().getPlayerIdx().getBytes(), playerInfo.toByteArray());
        return true;
    }

    /**
     * 更新玩家基本信息到redis,
     *
     * @return
     */
    public boolean syncUpdatePlayerInfoToRedis(DB_ArenaPlayerInfo playerInfo) {
        if (playerInfo == null) {
            LogUtil.error("entity.ArenaPlayerManager.updatePlayerBaseInfoToRedis, error params, playerInfo is null");
            return false;
        }

        return JedisUtil.syncExecBooleanSupplier(ArenaUtil.getPlayerLockKey(playerInfo.getBaseInfo().getPlayerIdx())
                , () -> updatePlayerInfoToRedis(playerInfo));
    }

    /**
     * 更新玩家信息到redis
     *
     * @return 返回更新成功的玩家信息
     */
    public List<ArenaTotalInfo> syncUpdateAllPlayerToRedis(List<ArenaTotalInfo> playerInfoList) {
        if (CollectionUtils.isEmpty(playerInfoList)) {
            LogUtil.warn("entity.ArenaPlayerManager.updatePlayerToRedis, playerInfoList is empty");
            return null;
        }

        List<ArenaTotalInfo> successUpdate = new ArrayList<>();
        for (ArenaTotalInfo playerInfo : playerInfoList) {
            if (syncUpdatePlayerToRedis(playerInfo)) {
                successUpdate.add(playerInfo);
            }
        }
        return successUpdate;
    }

    /**
     * 更新玩家信息到redis
     *
     * @return 返回更新成功的玩家基本信息
     */
    public List<DB_ArenaPlayerInfo> updateAllPlayerToRedis(List<ArenaTotalInfo> playerInfoList) {
        if (CollectionUtils.isEmpty(playerInfoList)) {
            LogUtil.warn("entity.ArenaPlayerManager.updatePlayerToRedis, playerInfoList is empty");
            return null;
        }

        List<DB_ArenaPlayerInfo> successUpdate = new ArrayList<>();
        for (ArenaTotalInfo playerInfo : playerInfoList) {
            if (playerInfo.isEmpty()) {
                continue;
            }
            if (updatePlayerToRedis(playerInfo)) {
                successUpdate.add(playerInfo.getArenaPlayerInfo());
            } else {
                LogUtil.error("ArenaPlayerManager.updatePlayersToRedis, failed update player to redis, playerIdx:"
                        + playerInfo.getArenaPlayerInfo().getBaseInfo().getPlayerIdx());
            }
        }
        return successUpdate;
    }

    /**
     * 更新玩家到redis
     *
     * @param totalInfo
     * @return
     */
    public boolean updatePlayerToRedis(ArenaTotalInfo totalInfo) {
        if (totalInfo == null || totalInfo.isEmpty()) {
            return false;
        }
        updatePlayerInfoToRedis(totalInfo.getArenaPlayerInfo());
        updatePlayerDefinedTeamsInfoToRedis(totalInfo.getDefinedTeams());
        return true;
    }

    public boolean syncUpdatePlayerToRedis(ArenaTotalInfo totalInfo) {
        if (totalInfo == null || totalInfo.isEmpty()) {
            return false;
        }

        return JedisUtil.syncExecBooleanSupplier(ArenaUtil.getPlayerLockKey(totalInfo.getArenaPlayerInfo().getBaseInfo().getPlayerIdx())
                , () -> updatePlayerToRedis(totalInfo));
    }


    /**
     * 获取玩家基本信息
     *
     * @param playerIdx
     * @return
     */
    public DB_ArenaPlayerInfo getPlayerBaseInfo(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            LogUtil.error("ArenaPlayerManager.getPlayerInfo, playerIdx is empty");
            return null;
        }

        byte[] result = jedis.hget(RedisKey.ARENA_PLAYER_BASE_INFO.getBytes(), playerIdx.getBytes());
        if (result != null) {
            try {
                return DB_ArenaPlayerInfo.parseFrom(result);
            } catch (InvalidProtocolBufferException e) {
                LogUtil.error("ArenaPlayerManager.getPlayerInfo, parse player info failed, playerIdx:" + playerIdx);
                LogUtil.printStackTrace(e);
            }
        }
        return null;
    }

    /**
     * 获取玩家队伍信息
     *
     * @param playerIdx
     * @return
     */
    public DB_ArenaDefinedTeamsInfo getPlayerDefinedTeamsInfo(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            LogUtil.error("ArenaPlayerManager.getPlayerInfo, playerIdx is empty");
            return null;
        }

        byte[] result = jedis.hget(RedisKey.ARENA_PLAYER_DEFINED_TEAMS_INFO.getBytes(), playerIdx.getBytes());
        if (result != null) {
            try {
                return DB_ArenaDefinedTeamsInfo.parseFrom(result);
            } catch (InvalidProtocolBufferException e) {
                LogUtil.error("ArenaPlayerManager.getPlayerInfo, parse player info failed, playerIdx:" + playerIdx);
                LogUtil.printStackTrace(e);
            }
        }

        return null;
    }

    /**
     * 获取玩家队伍信息
     *
     * @param playerIdx
     * @return
     */
    public DB_ArenaDefinedTeamsInfo.Builder getPlayerDefinedTeamsInfoBuilder(String playerIdx) {
        DB_ArenaDefinedTeamsInfo teamsInfo = getPlayerDefinedTeamsInfo(playerIdx);
        if (teamsInfo == null) {
            return null;
        }
        return teamsInfo.toBuilder();
    }

    /**
     * 获取玩家信息
     *
     * @param playerIdx
     * @return
     */
    public DB_ArenaPlayerInfo.Builder getPlayerBaseInfoBuilder(String playerIdx) {
        DB_ArenaPlayerInfo playerInfo = getPlayerBaseInfo(playerIdx);
        if (playerInfo == null) {
            return null;
        }
        return playerInfo.toBuilder();
    }



    /**
     * <PlayerIdx, ArenaPlayerInfo>
     *
     * @param playerIdxList
     * @return
     */
    public Map<String, DB_ArenaPlayerInfo> getPlayerInfoMap(List<String> playerIdxList) {
        if (CollectionUtils.isEmpty(playerIdxList)) {
            return null;
        }

        byte[][] queryArray = new byte[playerIdxList.size()][];
        for (int i = 0; i < playerIdxList.size(); i++) {
            queryArray[i] = playerIdxList.get(i).getBytes();
        }

        List<byte[]> playerInfoList = jedis.hmget(RedisKey.ARENA_PLAYER_BASE_INFO.getBytes(), queryArray);
        if (CollectionUtils.isEmpty(playerInfoList)) {
            return null;
        }

        Map<String, DB_ArenaPlayerInfo> result = new HashMap<>();
        for (byte[] bytes : playerInfoList) {
            try {
                DB_ArenaPlayerInfo playerInfo = DB_ArenaPlayerInfo.parseFrom(bytes);
                result.put(playerInfo.getBaseInfo().getPlayerIdx(), playerInfo);
            } catch (InvalidProtocolBufferException e) {
                LogUtil.printStackTrace(e);
            }
        }
        return result;
    }

    /**
     * 检查玩家是否存在
     *
     * @param playerIdx
     * @return
     */
    public boolean playerIsExist(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return false;
        }

        Boolean exist = jedis.hexists(RedisKey.ARENA_PLAYER_BASE_INFO.getBytes(), playerIdx.getBytes());
        return exist != null && exist;
    }

    public boolean isRobot(String playerIdx) {
        DB_ArenaPlayerInfo playerBaseInfo = getPlayerBaseInfo(playerIdx);
        return playerBaseInfo == null || playerBaseInfo.getRobotCfgId() > 0;
    }

    /**
     * 构建敌人信息
     *
     * @param playerIdx
     * @param direct
     * @param needTeams 为空或者null时获取所有队伍
     * @return
     */
    public ArenaOpponentTotalInfo buildArenaOpponentTotalInfo(String playerIdx, boolean direct, List<Integer> needTeams) {
        if (StringUtils.isBlank(playerIdx)) {
            return null;
        }

        //玩家存在
        if (!playerIsExist(playerIdx)) {
            return null;
        }


        Builder resultBuilder = ArenaOpponentTotalInfo.newBuilder();
        //先检查队伍是否为空
        Collection<ArenaPlayerTeamInfo> teamsInfo = buildArenaOpponentTeam(playerIdx, needTeams);
        if (teamsInfo != null) {
            resultBuilder.addAllTeamsInfo(teamsInfo);
        }

        ArenaOpponent opponent = ArenaUtil.buildArenaOpponent(getPlayerBaseInfo(playerIdx), direct);
        if (opponent == null) {
            return null;
        }
        resultBuilder.setOpponnentInfo(opponent);

        return resultBuilder.build();
    }

    /**
     * 更具玩家构建指定队伍信息
     *
     * @param playerIdx
     * @param needTeams 所需要的的队伍为空或者null时返回所有队伍
     * @return
     */
    private Collection<ArenaPlayerTeamInfo> buildArenaOpponentTeam(String playerIdx, List<Integer> needTeams) {
        if (StringUtils.isBlank(playerIdx)) {
            return null;
        }

        DB_ArenaDefinedTeamsInfo teamsInfo = getPlayerDefinedTeamsInfo(playerIdx);
        if (teamsInfo != null && teamsInfo.getDefinedTeamsCount() > 0) {
            if (CollectionUtils.isEmpty(needTeams)) {
                return teamsInfo.getDefinedTeamsMap().values();
            } else {
                return needTeams.stream()
                        .map(e -> {
                            //没有队伍需要用空队伍代替
                            ArenaPlayerTeamInfo teamInfo = teamsInfo.getDefinedTeamsMap().get(e);
                            if (teamInfo == null) {
                                teamInfo = ArenaPlayerTeamInfo.newBuilder().setTeanNumValue(e).build();
                            }
                            return teamInfo;
                        })
                        .collect(Collectors.toList());
            }
        }

        //机器人基本信息不存在
        DB_ArenaPlayerInfo.Builder builder = getPlayerBaseInfoBuilder(playerIdx);
        if (builder == null) {
            LogUtil.error("model.arena.ArenaPlayerManager.buildArenaOpponentTeam, playerIdx is not exist, playerIdx:" + playerIdx);
            return null;
        }

        //创建一个新的机器人队伍
        if (ArenaRobotConfig.getById(builder.getRobotCfgId()) != null) {
            Map<Integer, ArenaPlayerTeamInfo> newTeams = ArenaRobotManager.getInstance().createRobotTeamsInfo(builder.getRobotCfgId());
            if (newTeams == null) {
                LogUtil.error("model.arena.ArenaPlayerManager.buildArenaOpponentTeam, create new robot teams failed" +
                        ", robotId:" + playerIdx + "cfg id:" + builder.getRoomId());
                return null;
            }

            //更新队伍信息
            syncMergePlayerDefinedTeamsInfoToRedis(playerIdx, newTeams.values());
            //更新机器人战力
            builder.getBaseInfoBuilder().setFightAbility(ArenaUtil.calculateTotalAbility(newTeams.values()));
            //更新机器人等级
            builder.getBaseInfoBuilder().setLevel(ArenaUtil.getPetMaxLv(newTeams));
            syncUpdatePlayerInfoToRedis(builder.build());
            return newTeams.values();
        }

        return null;
    }
}