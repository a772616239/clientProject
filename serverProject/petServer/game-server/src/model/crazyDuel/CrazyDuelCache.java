package model.crazyDuel;

import cfg.CrazyDuelCfg;
import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.JedisUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;

import lombok.Data;
import lombok.Getter;
import model.crazyDuel.dto.CrazyDuelPlayerPageDB;
import model.crazyDuel.entity.CrazyDuelPlayerDB;
import model.crazyDuel.entity.CrazyTeamsDb;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import protocol.CrayzeDuel;
import protocol.CrazyDuelDB.CrazyDuelSettingDB;
import redis.clients.jedis.Tuple;
import util.LogUtil;
import util.ObjUtil;

@Data
public class CrazyDuelCache {

    @Getter
    private static CrazyDuelCache instance = new CrazyDuelCache();

    private String CrazyDuelDataPrefix = GameConst.RedisKey.CrazyDuelPrefix + "data:";

    private String CrazyDuelPlayerSetting = CrazyDuelDataPrefix + "teamSetting:";

    private String CrazyDuelPlayerTeamDetail = CrazyDuelDataPrefix + "playerTeam:";

    private String CrazyDuelPlayerPageInfo = CrazyDuelDataPrefix + "playerPageInfo:";

    private String CrazyDuelPlayerBattleInfo = CrazyDuelDataPrefix + "playerBattleInfo:";

    private String CrazyDuelDataVersion = CrazyDuelDataPrefix + "dataVersion";

    private String CrazyDuelVersionUpdate = CrazyDuelDataPrefix + "versionUpdate";

    private String CrazyDuelBattleRecord = CrazyDuelDataPrefix + "battleRecord";

    private String CrazyDuelPlayerIp = CrazyDuelDataPrefix + "playerIp";

    private String CrazyDuelPlayerTimes = CrazyDuelDataPrefix + "playTimes";

    private String CrazyDuelAbility = CrazyDuelDataPrefix + "playerAbility";

    private String CrazyDuelPlayerScore = CrazyDuelDataPrefix + "playerScore";

    private String CrazyDuelPlayerRefreshTime = CrazyDuelDataPrefix + "refreshTimes";


    public void savePlayerSetting(CrazyDuelSettingDB player) {
        JedisUtil.jedis.hset(CrazyDuelPlayerSetting.getBytes(StandardCharsets.UTF_8), player.getPlayerIdx().getBytes(StandardCharsets.UTF_8), player.toByteArray());
    }

    public Set<String> findAllSettingPlayerIds() {
        return JedisUtil.jedis.hkeys(CrazyDuelPlayerSetting);
    }



    public CrazyDuelSettingDB findPlayerSetting(String playerIdx) {
        byte[] dbBytes = JedisUtil.jedis.hget(CrazyDuelPlayerSetting.getBytes(StandardCharsets.UTF_8),
                playerIdx.getBytes(StandardCharsets.UTF_8));
        if (dbBytes == null) {
            return null;
        }
        try {
            return CrazyDuelSettingDB.parseFrom(dbBytes);
        } catch (InvalidProtocolBufferException e) {
            LogUtil.printStackTrace(e);
        }
        return null;
    }


    public void saveTeamsDb(String playerIdx, CrazyTeamsDb crazyTeamsDb) {
        JedisUtil.jedis.hset((CrazyDuelPlayerTeamDetail + playerIdx).getBytes(StandardCharsets.UTF_8)
                , String.valueOf(crazyTeamsDb.getFloor()).getBytes(StandardCharsets.UTF_8), ObjUtil.ObjectToByte(crazyTeamsDb));
    }


    public List<CrazyTeamsDb> loadTeamsDb(String playerIdx) {
        List<byte[]> hvals = JedisUtil.jedis.hvals((CrazyDuelPlayerTeamDetail + playerIdx).getBytes(StandardCharsets.UTF_8));
        if (CollectionUtils.isEmpty(hvals)) {
            return Collections.emptyList();
        }
        List<CrazyTeamsDb> datas = new ArrayList<>();
        for (byte[] hval : hvals) {
            datas.add((CrazyTeamsDb) ObjUtil.byteToObject(hval));
        }
        return datas;
    }

    public void saveShowPagePlayer(CrazyDuelPlayerPageDB pagePlayer) {
        JedisUtil.jedis.hset(CrazyDuelPlayerPageInfo, pagePlayer.getPlayerId(), JSON.toJSONString(pagePlayer));
    }

    public CrazyDuelPlayerPageDB findPagePlayer(String playerIdx) {
        String hget = JedisUtil.jedis.hget(CrazyDuelPlayerPageInfo, playerIdx);
        return hget == null ? null : JSON.parseObject(hget, CrazyDuelPlayerPageDB.class);
    }


    public List<CrazyDuelPlayerPageDB> findPagePlayers(List<String> players) {
        if (CollectionUtils.isEmpty(players)) {
            return Collections.emptyList();
        }
        List<String> hget = JedisUtil.jedis.hmget(CrazyDuelPlayerPageInfo, players.toArray(new String[0]));
        if (hget == null) {
            return Collections.emptyList();
        }
        List<CrazyDuelPlayerPageDB> result = new ArrayList<>();
        for (String s : hget) {
            result.add(JSON.parseObject(s, CrazyDuelPlayerPageDB.class));
        }
        return result;

    }


    public void saveCrazyDuelDB(CrazyDuelPlayerDB crazyDuelDB) {
        JedisUtil.jedis.hset(CrazyDuelPlayerBattleInfo, crazyDuelDB.getPlayerIdx(), JSON.toJSONString(crazyDuelDB));
    }

    public Set<String> findCrazyDuelDbPlayerIds() {
        return JedisUtil.jedis.hkeys(CrazyDuelPlayerBattleInfo);
    }


    public CrazyDuelPlayerDB findCrazyDuelDB(String playerIdx) {
        String hget = JedisUtil.jedis.hget(CrazyDuelPlayerBattleInfo, playerIdx);
        if (StringUtils.isEmpty(hget)) {
            return null;
        }
        return JSON.parseObject(hget, CrazyDuelPlayerDB.class);
    }


    public CrazyTeamsDb loadTeamsDbByFloor(String playerIdx, int floor) {
        byte[] hget = JedisUtil.jedis.hget((CrazyDuelPlayerTeamDetail + playerIdx).getBytes(StandardCharsets.UTF_8), String.valueOf(floor).getBytes(StandardCharsets.UTF_8));
        if (ArrayUtils.isEmpty(hget)) {
            return null;
        }
        return (CrazyTeamsDb) ObjUtil.byteToObject(hget);

    }

    public long incrVersion() {
        return JedisUtil.jedis.incrBy(CrazyDuelDataVersion, 1);
    }



    public void recordVersionUpdate(long version, Set<String> versionUpdatePlayers) {
        JedisUtil.jedis.hset(CrazyDuelVersionUpdate, String.valueOf(version), JSON.toJSONString(versionUpdatePlayers));
    }


    public int incrPlayerScore(String playerIdx, int incr) {

        return (int) Math.max(0, JedisUtil.jedis.zincrby(CrazyDuelPlayerScore, incr, playerIdx));

    }

    public void saveBattleRecord(String playerIdx, CrayzeDuel.CrazyBattleRecord playerRecord) {
        JedisUtil.jedis.lpush((CrazyDuelBattleRecord + ":" + playerIdx).getBytes(StandardCharsets.UTF_8), playerRecord.toByteArray());
    }

    public List<CrayzeDuel.CrazyBattleRecord> findPlayerRecord(String playerIdx) {
        List<byte[]> values = JedisUtil.jedis.lrange((CrazyDuelBattleRecord + ":" + playerIdx).getBytes(StandardCharsets.UTF_8), 0, 20);
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        List<CrayzeDuel.CrazyBattleRecord> result = new ArrayList<>();
        for (byte[] value : values) {
            try {
                result.add(CrayzeDuel.CrazyBattleRecord.parseFrom(value));
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String findPlayerFromSvrIndex(String playerIdx) {
        return JedisUtil.jedis.hget(CrazyDuelPlayerIp, playerIdx);
    }

    public void savePlayerFromSvrIndex(String playerIdx, int fromSvrIndex) {
        JedisUtil.jedis.hset(CrazyDuelPlayerIp, playerIdx, String.valueOf(fromSvrIndex));
    }

    public void savePlayerAbility(Map<String, Long> robotAbility) {
        if (CollectionUtils.isEmpty(robotAbility)) {
            return;
        }
        for (Map.Entry<String, Long> entry : robotAbility.entrySet()) {
            JedisUtil.jedis.zadd(CrazyDuelAbility, entry.getValue(), entry.getKey());
        }
    }

    public Set<Tuple> findAllPlayerAbility() {
        return JedisUtil.jedis.zrangeByScoreWithScores(CrazyDuelAbility, 0, Long.MAX_VALUE);
    }


    public int findPlayerScore(String playerId) {
        double zscore = JedisUtil.jedis.zscore(CrazyDuelPlayerScore, playerId);
        if (zscore == -1.0) {
            savePlayerScore(playerId, CrazyDuelCfg.getById(GameConst.CONFIG_ID).getPlayerinitscore());
            return CrazyDuelCfg.getById(GameConst.CONFIG_ID).getPlayerinitscore();
        }
        return (int) Math.max(0, zscore);
    }

    public void savePlayerScore(String playerIdx, int score) {
        JedisUtil.jedis.zadd(CrazyDuelPlayerScore, score, playerIdx);
    }

    public long getPagePlayerSize() {
        return JedisUtil.jedis.hlen(CrazyDuelPlayerPageInfo);
    }


    public String findPlayerRefreshTime(String playerIdx) {
        return JedisUtil.jedis.hget(CrazyDuelPlayerRefreshTime, playerIdx);
    }

    public void incrPlayerRefreshTime(String playerIdx) {
        JedisUtil.jedis.hincrByFloat(CrazyDuelPlayerRefreshTime, playerIdx, 1);
    }

    public void clearAllPlayerScore(Set<String> crazyDuelDbPlayerIds) {
        JedisUtil.jedis.zrem(CrazyDuelPlayerScore,crazyDuelDbPlayerIds.toArray(new String[0]));
    }

    public void clearPlayerRefreshTimes(Set<String> crazyDuelDbPlayerIds) {
        JedisUtil.jedis.hdel(CrazyDuelPlayerRefreshTime, crazyDuelDbPlayerIds.toArray(new String[0]));
    }

    public void clearAllPlayerDataDb(Set<String> crazyDuelDbPlayerIds) {
        JedisUtil.jedis.hdel(CrazyDuelPlayerBattleInfo, crazyDuelDbPlayerIds.toArray(new String[0]));
    }
}
