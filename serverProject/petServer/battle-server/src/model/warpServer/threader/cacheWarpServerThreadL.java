/*CREATED BY TOOL*/

package model.warpServer.threader;

import static common.GameConst.RedisKey.CrossArenaBSSid;
import static common.GameConst.RedisKey.SYNCACHE;
import static model.crossarena.CrossArenaManager.TableBase;
import static util.JedisUtil.jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cfg.CrossArenaScene;
import model.crossarena.CrossArenaManager;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.math.NumberUtils;

import annotation.annationInit;
import common.GameConst;
import common.GameConst.RedisKey;
import common.GlobalThread;
import common.GlobalTick;
import common.load.ServerConfig;
import datatool.StringHelper;
import model.player.cache.PlayerCache;
import model.warpServer.WarpServerManager;
import server.event.EventManager;
import server.event.crossarena.CrossArenaTickEvent;
import server.event.crossarena.CrossArenaTopTickEvent;
import timetool.TimeHelper;
import util.JedisUtil;
import util.LogUtil;

@annationInit(value = "cacheWarpServerThreadL", methodname = "Start")
public class cacheWarpServerThreadL implements Runnable {
    private static boolean bRun = true;
    private static cacheWarpServerThreadL _instanse = null;

    private static long lastTickCount;

    /**
     * 擂台需要在本服刷新的业务
     */
    private static List<String> crossArenaTableId = new ArrayList<>();

    public synchronized static void Start(Object o) {
        if (_instanse == null)
            _instanse = (cacheWarpServerThreadL) o;
        Start();
    }

    public static void Start() {
        if (_instanse == null)
            _instanse = new cacheWarpServerThreadL();
        bRun = true;
        GlobalThread.getInstance().getExecutor().execute(_instanse);

    }

    private static void Sleep(long elapsed) {
        if (elapsed <= 0)
            return;
        try {
            Thread.sleep(elapsed);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }


    public void run() {
        LogUtil.info("*************cacheWarpServerThreadL******************* RUN");
        while (bRun) {
            try {
                process();
                Sleep(TimeHelper.SEC * 1);
            } catch (Exception e) {
                if (!bRun)
                    return;
                LogUtil.printStackTrace(e);
                Sleep(TimeHelper.SEC * 1);
            }
        }
    }

    public static void process() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        updateServerInfo(curTime);
    }

    public static void updateServerInfo(long curTime) {
        if (lastTickCount + GameConst.UpdateServerTime > curTime) {
            return;
        }
        lastTickCount = curTime;
        try {
            String ipPort = ServerConfig.getInstance().getIp() + ":" + ServerConfig.getInstance().getPort();
            String serverIndex = StringHelper.IntTostring(ServerConfig.getInstance().getServer(), "");
            LogUtil.info("update info to redis,servreindex=" + serverIndex + ",ip=" + ipPort);

            jedis.hset(RedisKey.BattleServerIndexAddr, serverIndex, ipPort);

            long expireTime = curTime + GameConst.UpdateServerTime + 3000l; // 保活时间
            jedis.zadd(RedisKey.BattleServerInfo, expireTime, serverIndex);
            int size = PlayerCache.getInstance().size();
            if (size < GameConst.MaxPlayerCount - 10) {
                jedis.zadd(RedisKey.BattleOnlineCount, size, serverIndex);
            }
            WarpServerManager.getInstance().printServerInfo(curTime);
            Set<String> activeServerSet = jedis.zrangeByScore(RedisKey.BattleServerInfo, curTime, Long.MAX_VALUE);
            // 监听战场服异常后拉起宕掉的战斗服逻辑处理擂台赛业务
            tickTable(activeServerSet, serverIndex);
            tickTopPlay(activeServerSet, serverIndex);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        for (String tableId : crossArenaTableId) {
            if (tableId.contains(SYNCACHE)) {
                continue;
            }
            CrossArenaTickEvent event = new CrossArenaTickEvent();
            event.setTime(curTime);
            EventManager.getInstance().dealCrossArenaEvent(event, Integer.valueOf(tableId));
        }
    }

    private static void tickTable(Set<String> activeServerSet, String serverIndex) {
        Map<String, String> tableSid = getAllTableAndScene();
        if (MapUtils.isEmpty(tableSid)) {
            return;
        }
        List<String> needFix = new ArrayList<>();
        crossArenaTableId.clear();
        for (Map.Entry<String, String> ent : tableSid.entrySet()) {
            // 当前在线战斗服是否有该服务器
            if (!activeServerSet.contains(ent.getValue())) {
                needFix.add(ent.getKey());
            } else {
                if (Objects.equals(serverIndex, ent.getValue())) {
                    crossArenaTableId.add(ent.getKey());
                    if (ent.getKey().contains(SYNCACHE)) {
                        continue;
                    }
                    CrossArenaTickEvent event = new CrossArenaTickEvent();
                    event.setTime(lastTickCount);
                    EventManager.getInstance().dealCrossArenaEvent(event, NumberUtils.toInt(ent.getKey()));
                }
            }
        }
        if (!needFix.isEmpty()) {
            if (JedisUtil.lockRedisKey(GameConst.RedisKey.CrossArenaTimeLock, 5000l)) {
                // 防止一个服务器揽收全部宕掉逻辑，此处一次只修复一个
                String key = needFix.get(0);
                jedis.hset(getFixBSSIdKey(key),key, serverIndex);
                JedisUtil.unlockRedisKey(GameConst.RedisKey.CrossArenaTimeLock);
            }
        }
    }

    private static String getFixBSSIdKey(String key) {
        if (key.equals(SYNCACHE)) {
            return CrossArenaBSSid;
        }
        int keyInt = Integer.parseInt(key);
        if (keyInt < TableBase) {
            return CrossArenaBSSid;
        }
        return CrossArenaManager.getInstance().getTableServerKey(keyInt);
    }

    public static Map<String, String> getAllTableAndScene() {
        Map<String, String> result = new HashMap<>();
        for (Integer scienceId : CrossArenaScene._ix_id.keySet()) {
            if (scienceId > 0) {
                Map<String, String> all = jedis.hgetAll(CrossArenaBSSid + scienceId);
                if (!MapUtils.isEmpty(all)) {
                    result.putAll(all);
                }
                String hmget = jedis.hget(CrossArenaBSSid, scienceId + "");
                if (hmget != null) {
                    result.put(scienceId + "", hmget);
                }
            }
        }
        result.put(SYNCACHE,jedis.hget(CrossArenaBSSid,SYNCACHE));
        return result;
    }

    private static void tickTopPlay(Set<String> activeServerSet, String serverIndex) {
    	try {
    		// 监听战场服异常后拉起宕掉的战斗服逻辑处理擂台赛业务
            Map<String, String> topSid = jedis.hgetAll(GameConst.RedisKey.TopPlayBSSid);
            if (null == topSid || topSid.isEmpty()) {
            	return;
            }
            List<String> needFix = new ArrayList<String>();
            for (Map.Entry<String, String> ent : topSid.entrySet()) {
                // 当前在线战斗服是否有该服务器
                if (!activeServerSet.contains(ent.getValue())) {
                    needFix.add(ent.getKey());
                } else {
                    if (Objects.equals(serverIndex, ent.getValue())) {
                    	CrossArenaTopTickEvent event = new CrossArenaTopTickEvent();
                    	event.setGroupId(ent.getKey());
                        EventManager.getInstance().dealCrossArenaEvent(event, NumberUtils.toInt(ent.getKey()));
                    }
                }
            }
            if (!needFix.isEmpty()) {
				if (JedisUtil.lockRedisKey(GameConst.RedisKey.TopPlaySidLock, 5000l)) {
					// 防止一个服务器揽收全部宕掉逻辑，此处一次只修复一个
					if (!needFix.isEmpty()) {
						jedis.hset(GameConst.RedisKey.TopPlayBSSid, needFix.get(0), serverIndex);
					}
					JedisUtil.unlockRedisKey(GameConst.RedisKey.TopPlaySidLock);
				}
            }
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
		}
    }

}
