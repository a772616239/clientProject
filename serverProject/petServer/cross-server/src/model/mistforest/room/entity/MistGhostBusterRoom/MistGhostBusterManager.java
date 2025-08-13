package model.mistforest.room.entity.MistGhostBusterRoom;

import cfg.CrossConstConfig;
import cfg.MistWorldMapConfig;
import cfg.MistWorldMapConfigObject;
import common.GameConst;
import common.GameConst.EventType;
import common.GameConst.RedisKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import protocol.MistForest.EnumMistRuleKind;
import protocol.TransServerCommon.MistGhostBusterSyncData;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.JedisUtil;
import static util.JedisUtil.jedis;
import util.LogUtil;
import util.TimeUtil;

public class MistGhostBusterManager {
    protected Map<String, MistGhostBusterSyncData> matchPlayerMap = new HashMap<>();
    protected long updateTime;

    protected void dealSyncMatchData(long curTime) {
        MistWorldMapConfigObject mapCfg = MistWorldMapConfig.getInstance().getByRuleAndLevel(EnumMistRuleKind.EMRK_GhostBuster_VALUE, 1);
        if (mapCfg == null || mapCfg.getMaxplayercount() <= 0) {
            return;
        }
        try {
            Map<byte[], byte[]> data = jedis.hgetAll(RedisKey.MistGhostBusterMatchData.getBytes());
            if (MapUtils.isEmpty(data)) {
                return;
            }
            for (Entry<byte[], byte[]> entry : data.entrySet()) {
                String playerIdx = new String(entry.getKey(), "utf-8");
                MistGhostBusterSyncData matchData = MistGhostBusterSyncData.parseFrom(entry.getValue());
                matchPlayerMap.put(playerIdx, matchData);
            }
            List<Entry<String, MistGhostBusterSyncData>> playerDataList = matchPlayerMap.entrySet().stream().sorted(
                    (o1, o2) -> (int) (o1.getValue().getStartMatchTime() - o2.getValue().getStartMatchTime())).limit(mapCfg.getMaxplayercount()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(playerDataList)) {
                return;
            }
            if (!JedisUtil.lockRedisKey(RedisKey.MistGhostBusterMatchLock, 5000L)) {
                return;
            }
            long maxMatchTime = CrossConstConfig.getById(GameConst.ConfigId).getGhostbustermaxmatchtime();
            if (playerDataList.size() == mapCfg.getMaxplayercount() || curTime - playerDataList.get(0).getValue().getStartMatchTime() > maxMatchTime * TimeUtil.MS_IN_A_S) {
                Map<String, MistGhostBusterSyncData> playerDataMap = playerDataList.stream().collect(Collectors.toMap(e->e.getKey(), e->e.getValue()));
                Event event = Event.valueOf(EventType.ET_CreateGhostBusterRoom, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
                event.pushParam(playerDataMap);
                EventManager.getInstance().dispatchEvent(event);

                for (Entry<String, MistGhostBusterSyncData> entry : playerDataMap.entrySet()) {
                    jedis.hdel(RedisKey.MistGhostBusterMatchData.getBytes(), entry.getKey().getBytes());
                }
            }
            JedisUtil.unlockRedisKey(RedisKey.MistGhostBusterMatchLock);
            matchPlayerMap.clear();
            StringBuilder stBldr = new StringBuilder("GhostBuster deal match finish, matach player:");
            playerDataList.forEach(e->{
                stBldr.append("["+e.getKey() +",");
                stBldr.append(e.getValue().getFromSvrIndex()+",");
                stBldr.append(e.getValue().getStartMatchTime()+"],");
            });
            LogUtil.info(stBldr.toString());
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public void onTick(long curTime) {
        if (updateTime > curTime) {
            return;
        }
        updateTime = curTime + TimeUtil.MS_IN_A_S;
        dealSyncMatchData(curTime);
    }
}
