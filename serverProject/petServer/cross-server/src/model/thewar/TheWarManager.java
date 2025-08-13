package model.thewar;

import cfg.TheWarConstConfig;
import cfg.TheWarSeasonConfig;
import cfg.TheWarSeasonConfigObject;
import common.GameConst;
import common.GameConst.EventType;
import common.GameConst.RedisKey;
import common.GlobalTick;
import common.Tickable;
import common.load.ServerConfig;
import datatool.StringHelper;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import model.mistforest.room.cache.MistRoomCache;
import model.thewar.WarConst.ActivityState;
import model.thewar.warmap.config.TotalWarMapCfgData;
import model.thewar.warroom.dbCache.WarRoomCache;
import org.springframework.util.CollectionUtils;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.JedisUtil;
import static util.JedisUtil.jedis;
import util.LogUtil;
import util.TimeUtil;

public class TheWarManager implements Tickable {
    private static TheWarManager instance;

    private int theWarSate;
    private long updateTime;
    private TheWarSeasonConfigObject warSeasonConfig;

    public static TheWarManager getInstance() {
        if (instance == null) {
            synchronized (TheWarManager.class) {
                if (instance == null) {
                    instance = new TheWarManager();
                }
            }
        }
        return instance;
    }

    public boolean init() {
        if (!initTheWarTime()) {
            return false;
        }
        if (!TotalWarMapCfgData.init()) {
            return false;
        }
        GlobalTick.getInstance().addTick(this);
        GlobalTick.getInstance().addTick(WarRoomCache.getInstance());
        return true;
    }

    public String getMapName() {
        return warSeasonConfig != null ? warSeasonConfig.getOpenmapname() : null;
    }

    public TheWarSeasonConfigObject getWarSeasonConfig() {
        return warSeasonConfig;
    }

    public long getActivityEndTime() {
        if (warSeasonConfig == null) {
            return 0;
        }
        return warSeasonConfig.getEndplaytime();
    }

    protected boolean initTheWarTime() {
        warSeasonConfig = TheWarSeasonConfig.getInstance().getWarOpenConfig();
        return true;
    }

    // 判断自身是否可接收战戈房间
    public boolean canTackUpWarRoom() {
        if (WarRoomCache.getInstance().getWarRoomCount() > 20) {
            LogUtil.debug("takeUpWarRoomFailed for warRoomCount > 20 ");
            return false;
        }
        if (MistRoomCache.getInstance().getMistRoomCount() > 20) {
            LogUtil.debug("takeUpWarRoomFailed for mistRoomCount > 20 ");
            return false;
        }
        if (!isWarOpen()) {
            LogUtil.debug("takeUpWarRoomFailed for WarSeason not open");
            return false;
        }
        return true;
    }

    public void checkSwitchWarRoom() {
        if (!canTackUpWarRoom()) {
            return;
        }
        try {
            Map<String, String> roomAddrMap = jedis.hgetAll(RedisKey.TheWarRoomServerIndex);
            if (CollectionUtils.isEmpty(roomAddrMap)) {
                return;
            }
            long curTime = GlobalTick.getInstance().getCurrentTime();
            Set<String> activeServerSet = jedis.zrangeByScore(RedisKey.CrossServerInfo, curTime, Long.MAX_VALUE);
            if (CollectionUtils.isEmpty(activeServerSet)) {
                LogUtil.debug("takeUpWarRoomFailed for activeRoom is empty");
                return;
            }
            String takeUpRoomIdx = null;
            int svrIndex = ServerConfig.getInstance().getServer();
            String svrIndexStr = StringHelper.IntTostring(svrIndex, "0");
            for (Entry<String, String> entry : roomAddrMap.entrySet()) {
                int tmpSvrIndex = StringHelper.stringToInt(entry.getValue(), 0);
                if (tmpSvrIndex <= 0) {
                    continue;
                }
                if ((svrIndex == tmpSvrIndex && WarRoomCache.getInstance().queryObject(entry.getKey()) == null) || (!activeServerSet.contains(entry.getValue()))) {
                    if (!JedisUtil.lockRedisKey(RedisKey.TheWarUpdateRoomLock, 5000)) {
                        LogUtil.debug("takeUpWarRoomFailed for not get room lock, svrIndex="+entry.getKey());
                        continue;
                    }
                    jedis.hset(RedisKey.TheWarRoomServerIndex, entry.getKey(), svrIndexStr);
                    takeUpRoomIdx = entry.getKey();
                    JedisUtil.unlockRedisKey(RedisKey.TheWarUpdateRoomLock);
                    break;
                }
            }

            if (takeUpRoomIdx != null) {
                LogUtil.debug("takeUpWarRoom try to take up room idx=" + takeUpRoomIdx);
                byte[] roomCacheData = jedis.hget(RedisKey.TheWarRoomData.getBytes(), takeUpRoomIdx.getBytes());
                Map<byte[], byte[]> playerCacheData = jedis.hgetAll((RedisKey.TheWarPlayerData + takeUpRoomIdx).getBytes());
                Map<byte[], byte[]> gridCacheData = jedis.hgetAll((RedisKey.TheWarGridData + takeUpRoomIdx).getBytes());
                if (roomCacheData != null) {
                    Event event = Event.valueOf(EventType.ET_TheWar_TakeUpRoom, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
                    event.pushParam(takeUpRoomIdx, roomCacheData, playerCacheData, gridCacheData);
                    EventManager.getInstance().dispatchEvent(event);
                }
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public boolean isWarOpen() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        return warSeasonConfig != null && warSeasonConfig.getStartplaytime() <= curTime && warSeasonConfig.getEndplaytime() > curTime;
    }

    @Override
    public void onTick() {
        if (warSeasonConfig == null) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (updateTime > curTime) {
            return;
        }
        updateTime = curTime + TimeUtil.MS_IN_A_S;
        switch (theWarSate) {
            case ActivityState.EndState: {
                if (curTime >= warSeasonConfig.getStartplaytime()) {
                    theWarSate = ActivityState.OpenState;
                }
                break;
            }
            case ActivityState.OpenState: {
                if (curTime > warSeasonConfig.getEndplaytime() - TheWarConstConfig.getById(GameConst.ConfigId).getPreendtime() * TimeUtil.MS_IN_A_MIN) {
                    theWarSate = ActivityState.PreEndState;
                } else {
                    checkSwitchWarRoom();
                }
                break;
            }
            case ActivityState.PreEndState: {
                if (curTime > warSeasonConfig.getEndplaytime() - TimeUtil.MS_IN_A_MIN) {
                    theWarSate = ActivityState.EndState;
                    warSeasonConfig = TheWarSeasonConfig.getInstance().getWarOpenConfig();
                } else {
                    checkSwitchWarRoom();
                }
                break;
            }
            default:
                break;
        }
    }
}