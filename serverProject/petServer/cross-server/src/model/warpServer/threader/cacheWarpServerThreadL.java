/*CREATED BY TOOL*/

package model.warpServer.threader;

import annotation.annationInit;
import cfg.MistWorldMapConfig;
import cfg.MistWorldMapConfigObject;
import common.GameConst;
import common.GameConst.RedisKey;
import common.GlobalThread;
import common.GlobalTick;
import common.load.ServerConfig;
import datatool.StringHelper;
import java.util.Set;
import model.mistforest.room.cache.MistRoomCache;
import model.mistplayer.cache.MistPlayerCache;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.warpServer.battleServer.BattleServerManager;
import server.PetAPP;
import timetool.TimeHelper;
import util.GameUtil;
import static util.JedisUtil.jedis;
import util.LogUtil;

@annationInit(value = "cacheWarpServerThreadL", methodname = "Start")
public class cacheWarpServerThreadL implements Runnable {
    private static boolean bRun = true;
    private static cacheWarpServerThreadL _instanse = null;

    private static long lastTickCount;

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
        if (!PetAPP.loadFinish) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        updateServerInfo(curTime);
        BattleServerManager.getInstance().onTick(curTime);
    }

    public static void updateServerInfo(long curTime) {
        if (lastTickCount + GameConst.UpdateServerTime > curTime) {
            return;
        }
        lastTickCount = curTime;
        try {
            String ipPort = GameUtil.builderServerAddr();
            String serverIndex = StringHelper.IntTostring(ServerConfig.getInstance().getServer(), "0");
            if (serverIndex == null) {
                LogUtil.error("update redis serverIndex is zero");
                return;
            }

            jedis.hset(RedisKey.CrossServerIndexAddr, serverIndex, ipPort);
            // 保活时间
            long expireTime = curTime + GameConst.UpdateServerTime + 2000L;
            jedis.zadd(RedisKey.CrossServerInfo, expireTime, serverIndex);

            //更新竞技场服务器
            if (ServerConfig.getInstance().isArenaServer()) {
                jedis.zadd(RedisKey.ARENA_SERVER_INFO, calculateArenaLoadValue(), serverIndex);
            }

            //主要跨境服负责清除过期信息
            boolean isMainCrossSvr = ServerConfig.getInstance().isMainCross();
            if (isMainCrossSvr) {
                jedis.zremrangeByScore(RedisKey.CrossServerInfo, 0, curTime);
            }

            Set<String> activeServerSet = jedis.zrange(RedisKey.CrossServerInfo, 0, -1);
            for (MistWorldMapConfigObject cfg : MistWorldMapConfig._ix_mapid.values()) {
                if (cfg.getLevel() >= 1000) {
                    continue;
                }
                long mistKey = GameUtil.mergeIntToLong(cfg.getMaprule(), cfg.getLevel());
                if (isMainCrossSvr && !GameUtil.collectionIsEmpty(activeServerSet)) {
                    // 清理已关闭的服务器列表
                    Set<String> mistServerInfo = jedis.zrange(RedisKey.MistForestInfo + mistKey, 0, -1);
                    for (String mistIndex : mistServerInfo) {
                        if (activeServerSet.contains(mistIndex)) {
                            continue;
                        }
                        jedis.zrem(RedisKey.MistForestInfo + mistKey, mistIndex);
                    }
                }

                long integral = MistRoomCache.getInstance().calcCrossServerInfo(cfg.getMaprule(), cfg.getLevel());
                jedis.zadd(RedisKey.MistForestInfo + mistKey, integral, serverIndex);
            }

            jedis.zadd(RedisKey.TheWarServerLoadInfo, calcTheWarLoadVal(), serverIndex);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    private static final int ARENA_MAX_LOAD_VALUE = 1000;

    /**
     * 计算矿区负载数  最大负载1000  越大当前服务器的人数越多,
     *
     * @return
     */
    private static int calculateArenaLoadValue() {
        int playerCount = MistRoomCache.getInstance().getMistTotalPlayerCount();
        int allowPlayerCount = GameConst.Max_Online_Player_Count - 10;
        if (playerCount >= allowPlayerCount) {
            return ARENA_MAX_LOAD_VALUE;
        }
        return ARENA_MAX_LOAD_VALUE * (playerCount / allowPlayerCount);
    }

    private static int calcTheWarLoadVal() {
        int warRoomCount = WarRoomCache.getInstance().getWarRoomCount();
        int mistRoomCount = MistRoomCache.getInstance().getObjCount();
        int mistPlayerCount = MistPlayerCache.getInstance().getObjCount();
        boolean isArenaServer = ServerConfig.getInstance().isArenaServer();
        return warRoomCount * 2000 + mistRoomCount * 1000 + mistPlayerCount * 10 + (isArenaServer ? 100 : 0);
    }
}
