package model.cp;

import com.alibaba.fastjson.JSON;
import common.JedisUtil;
import common.load.ServerConfig;
import datatool.StringHelper;
import lombok.Getter;
import model.cp.broadcast.CpCopySettle;
import model.cp.broadcast.CpTeamUpdate;
import model.cp.entity.CpCopyMap;
import model.cp.entity.CpDailyData;
import model.cp.entity.CpTeamMember;
import model.cp.entity.CpTeamPublish;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import util.ObjUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static model.cp.CpRedisKey.*;

public class CpTeamCache {

    @Getter
    public static CpTeamCache instance = new CpTeamCache();

    public static void removeCopyMap(String playerIdx) {
        JedisUtil.jedis.hdel(CpTeamPlayerMapId, playerIdx);
    }

    public void saveApplyJoinToDb(int teamId, String playerIdx) {
        JedisUtil.jedis.sadd(CpTeamApplyJoinPlayer + teamId, playerIdx);
    }

    public boolean existApplyJoinToDb(int teamId, String playerIdx) {
        return JedisUtil.jedis.sismember(CpTeamApplyJoinPlayer + teamId, playerIdx);
    }

    public void clearApplyJoinTeam(int teamId) {
        JedisUtil.jedis.del(CpTeamApplyJoinPlayer + teamId);
    }

    public Set<String> loadApplyJoinTeamPlayers(int teamId) {
        return JedisUtil.jedis.smembers(CpTeamApplyJoinPlayer + teamId);
    }

    public boolean removeApplyJoinTeamPlayer(int teamId, String removePlayerIdx) {
        long result = JedisUtil.jedis.srem(CpTeamApplyJoinPlayer + teamId, removePlayerIdx);
        return result == 1;
    }

    public void clearApplyJoinTeamPlayers(int teamId) {
        JedisUtil.jedis.del(CpTeamApplyJoinPlayer + teamId);
    }

    public void savePlayerInfo(String playerIdx, CpTeamMember playerInfo) {
        JedisUtil.jedis.hset(CpTeamPlayerInfo.getBytes(StandardCharsets.UTF_8),
                playerIdx.getBytes(StandardCharsets.UTF_8), ObjUtil.ObjectToByte(playerInfo));
    }

    public CpTeamMember loadPlayerInfo(String playerIdx) {
        byte[] dbBytes = JedisUtil.jedis.hget(CpTeamPlayerInfo.getBytes(StandardCharsets.UTF_8),
                playerIdx.getBytes(StandardCharsets.UTF_8));
        if (dbBytes == null) {
            return null;
        }
        return (CpTeamMember) ObjUtil.byteToObject(dbBytes);
    }

    public void saveTeamInfo(int teamId, CpTeamPublish teamInfo) {
        JedisUtil.jedis.hset(CpTeamInfo.getBytes(StandardCharsets.UTF_8),
                Integer.valueOf(teamId).toString().getBytes(StandardCharsets.UTF_8), ObjUtil.ObjectToByte(teamInfo));
    }

    public CpTeamPublish loadTeamInfo(int teamId) {
        byte[] dbBytes = JedisUtil.jedis.hget(CpTeamInfo.getBytes(StandardCharsets.UTF_8),
                Integer.valueOf(teamId).toString().getBytes(StandardCharsets.UTF_8));
        if (dbBytes == null) {
            return null;
        }
        return (CpTeamPublish) ObjUtil.byteToObject(dbBytes);
    }

    public Integer getPlayerTeamId(String playerId) {
        String teamId = JedisUtil.jedis.hget(CpTeamPlayerMap, playerId);
        if (teamId != null) {
            return Integer.parseInt(teamId);
        }
        return null;
    }

    public static void savePlayerTeamMap(String playerId, int teamId) {
        JedisUtil.jedis.hset(CpTeamPlayerMap, playerId, String.valueOf(teamId));
    }

    public void removePlayerTeamMap(String playerIdx) {
        JedisUtil.jedis.hdel(CpTeamPlayerMap, playerIdx);
    }

    public void savePlayerAbility(int teamId, long ability) {
        JedisUtil.jedis.zadd(CpTeamAbility, ability, String.valueOf(teamId));
    }

    public void removeTeam(int teamId) {
        JedisUtil.jedis.hdel(CpTeamInfo.getBytes(StandardCharsets.UTF_8), Integer.valueOf(teamId).toString().getBytes(StandardCharsets.UTF_8));
    }

    public Set<String> loadPlayerCpInvite(String playerIdx) {
        return JedisUtil.jedis.smembers(CpPlayerInvite + playerIdx);
    }

    public void clearReceiveCpInvite(String playerIdx) {
        JedisUtil.jedis.del(CpPlayerInvite + playerIdx);
    }

    public void addPlayerCpInvite(String playerIdx, String invitePlayer) {
        JedisUtil.jedis.sadd(CpPlayerInvite + invitePlayer, playerIdx);
    }

    public boolean existPlayerCpInvite(String playerIdx, String invitePlayer) {
        return JedisUtil.jedis.sismember(CpPlayerInvite + invitePlayer, playerIdx);
    }

    public boolean removeCpInvite(String playerIdx, String invitePlayerId) {
        long result = JedisUtil.jedis.srem(CpPlayerInvite + playerIdx, invitePlayerId);
        return result == 1;
    }


    public void saveCopyMap(CpCopyMap map) {
        JedisUtil.jedis.hset(CpTeamMapInfo.getBytes(StandardCharsets.UTF_8),
                map.getMapId().getBytes(StandardCharsets.UTF_8), ObjUtil.ObjectToByte(map));
    }

    public void savePlayerMapMap(String playerIdx, String mapId) {
        JedisUtil.jedis.hset(CpTeamPlayerMapId, playerIdx, mapId);
    }


    public String findPlayerCopyMapId(String playerIdx) {
        return JedisUtil.jedis.hget(CpTeamPlayerMapId, playerIdx);
    }

    public CpCopyMap loadCopyMapInfo(String mapId) {
        byte[] dbBytes = JedisUtil.jedis.hget(CpTeamMapInfo.getBytes(StandardCharsets.UTF_8),
                mapId.getBytes(StandardCharsets.UTF_8));
        if (dbBytes == null) {
            return null;
        }
        return (CpCopyMap) ObjUtil.byteToObject(dbBytes);

    }

    public CpCopyMap findPlayerCopyMapInfo(String playerIdx) {
        String mapId = findPlayerCopyMapId(playerIdx);
        if (StringUtils.isEmpty(mapId)) {
            return null;
        }
        return loadCopyMapInfo(mapId);

    }


    public void saveCopySettle(CpCopySettle cpCopySettle) {
        JedisUtil.jedis.hset(CpBattleSettle.getBytes(StandardCharsets.UTF_8),
                cpCopySettle.getMapId().getBytes(StandardCharsets.UTF_8), ObjUtil.ObjectToByte(cpCopySettle));
    }

    public int findPlayerCopyPlayTimes(String playerIdx) {
        String playTimes = JedisUtil.jedis.hget(CpCopyPlayerPlayTimes, playerIdx);
        if (StringUtils.isEmpty(playTimes)) {
            return 0;
        }
        return Integer.parseInt(playTimes);
    }

    public int incrPlayerCopyPlayTimes(String playerIdx) {
        return (int) JedisUtil.jedis.hincrByFloat(CpCopyPlayerPlayTimes, playerIdx, 1);
    }

    public void saveTeamLevel(int teamId, int teamLv) {
        JedisUtil.jedis.sadd(CpTeamLv + teamLv, String.valueOf(teamId));
    }

    public Set<String> findTeamIdByTeamLv(int teamLv) {
        return JedisUtil.jedis.smembers(CpTeamLv + teamLv);
    }

    public void delLevelTeam(int teamId, int teamLv) {
        JedisUtil.jedis.srem(CpTeamLv + teamLv, String.valueOf(teamId));
    }

    public void saveTeamUpdateBroadcast(CpTeamUpdate teamUpdate) {
        JedisUtil.jedis.hset(CpTeamUpdate, teamUpdate.getMsgId(), JSON.toJSONString(teamUpdate));
    }

    public void savePlayerMapExpire(String mapId, long expireTime) {
        JedisUtil.jedis.hset(CpCopyExpireTime, mapId, String.valueOf(expireTime));
    }

    public void removeCopyExpire(String mapId) {
        JedisUtil.jedis.hdel(CpCopyExpireTime, mapId);
    }

    public Map<String, String> loadAllPlayerMapExpire() {
        return JedisUtil.jedis.hgetAll(CpCopyExpireTime);
    }

    public Long loadPlayerMapExpire(String mapId) {
        String hget = JedisUtil.jedis.hget(CpCopyExpireTime, mapId);
        if (StringUtils.isEmpty(hget)) {
            return 0L;
        }
        return Long.parseLong(hget);
    }


    public void clearUseTime() {
        Set<String> hkeys = JedisUtil.jedis.hkeys(CpCopyPlayerPlayTimes);
        if (CollectionUtils.isEmpty(hkeys)) {
            return;
        }
        String[] strings = hkeys.toArray(new String[0]);
        JedisUtil.jedis.hdel(CpCopyPlayerPlayTimes, strings);
    }

    public void removeCopyMapData(String mapId) {
        JedisUtil.jedis.hdel(CpTeamMapInfo, mapId);
    }


    public void saveOpenTeamInfo(int teamId, int teamLv) {
        JedisUtil.jedis.lpush(CpOpenTeamLv + teamLv, String.valueOf(teamId));
    }

    public List<String> findOpenTeamInfo(int teamLv, int size) {
        return JedisUtil.jedis.lrange(CpOpenTeamLv + teamLv, 0, size - 1);
    }

    public void removeOpenTeamInfo(Integer teamLv, String teamId) {
        JedisUtil.jedis.lrem(CpOpenTeamLv + teamLv, 0, teamId);
    }

    public CpDailyData findPlayerDailyData(String playerIdx) {
        String data = JedisUtil.jedis.hget(CpTeamDailyData, playerIdx);

        return data == null ? new CpDailyData() : JSON.parseObject(data, CpDailyData.class);
    }

    public void savePlayerDailyData(String playerIdx, CpDailyData data) {
        JedisUtil.jedis.hset(CpTeamDailyData, playerIdx, JSON.toJSONString(data));
    }

    public void savePlayerSvrIndex(String playerIdx) {
        int svrIndex = ServerConfig.getInstance().getServer();
        if (svrIndex <= 0) {
            return;
        }
        JedisUtil.jedis.hset(CpPlayerIpPort, playerIdx, StringHelper.IntTostring(svrIndex, "0"));
    }

    public String findPlayerSvrIndex(String playerIdx) {
        return JedisUtil.jedis.hget(CpPlayerIpPort, playerIdx);
    }

    public Map<String, String> findAllTeamExpire() {
        return JedisUtil.jedis.hgetAll(CpTeamExpire);
    }

    public void removeTeamsExpire(List<String> teamIds) {
        JedisUtil.jedis.hdel(CpTeamExpire, teamIds.toArray(new String[0]));
    }

    public String findTeamsExpire(String teamId) {
        return JedisUtil.jedis.hget(CpTeamExpire, teamId);
    }

    public void saveTeamsExpire(int teamId, long expire) {
        JedisUtil.jedis.hset(CpTeamExpire, String.valueOf(teamId), String.valueOf(expire));
    }

    public void removeTeamExpire(int teamId) {
        JedisUtil.jedis.hdel(CpTeamExpire, String.valueOf(teamId));
    }

    public void addPlayerEnterScene(String playerIdx) {
        JedisUtil.jedis.sadd(CpFree, playerIdx);
    }

    public void removePlayerEnterScene(String playerIdx) {
        JedisUtil.jedis.srem(CpFree, playerIdx);
    }

    public Set<String> findAllPlayerEnterScene() {
        return JedisUtil.jedis.smembers(CpFree);
    }

    public int queryPlayerBuyPlayerTimes(String playerIdx) {
        String value = JedisUtil.jedis.hget(CpTeamBuyPlayTimes, playerIdx);
        return value == null ? 0 : Integer.parseInt(value);
    }

    public void savePlayerBuyPlayerTimes(String playerIdx, int times) {
        JedisUtil.jedis.hset(CpTeamBuyPlayTimes, playerIdx, String.valueOf(times));
    }

    public void saveCopyPlayerOfflineTime(String playerIdx, long time) {
        JedisUtil.jedis.hset(CpCopyPlayerOffline, playerIdx, String.valueOf(time));
    }

    public void removeCopyPlayerLeaveTime(String playerIdx) {
        JedisUtil.jedis.hdel(CpCopyPlayerOffline, playerIdx);
    }

    public Map<String, String> findAllCopyPlayerLeaveTime() {
        return JedisUtil.jedis.hgetAll(CpCopyPlayerOffline);
    }


    public void removeAllInCpPlayer() {
        Set<String> smembers = JedisUtil.jedis.smembers(CpFree);
        if (CollectionUtils.isEmpty(smembers)) {
            return;
        }
        JedisUtil.jedis.srem(CpFree, smembers.toArray(new String[0]));
    }
}
