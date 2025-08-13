package model.barrage;

import cfg.GameConfig;
import com.alibaba.fastjson.JSON;
import common.GameConst;
import common.GlobalData;
import static common.JedisUtil.jedis;
import common.tick.GlobalTick;
import common.tick.Tickable;
import helper.StringUtils;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import model.wordFilter.WordFilterManager;
import org.springframework.util.CollectionUtils;
import protocol.Barrage;
import protocol.Common;
import static protocol.MessageId.MsgIdEnum.SC_UpdateBarrage_VALUE;
import protocol.RetCodeId;

public class BarrageManager implements Tickable {

    @Getter
    private static final BarrageManager instance = new BarrageManager();

    private static final long tickInterval = 500;

    private static final long playerAddMsgInterval = 500;

    private static final Map<Common.EnumFunction,Map<Integer,String>> redisKeyPool = new ConcurrentHashMap<>();

    /**
     * 观看弹幕的玩家Idx
     */
    private final Map<String, List<String>> watchPlayers = new ConcurrentHashMap<>();

    /**
     * 上次添加弹幕时间
     */
    private final Map<String, Long> addMsgTimes = new ConcurrentHashMap<>();

    private Map<String, List<BarrageDTO>> addMsgMap = new ConcurrentHashMap<>();

    private final Map<String, List<BarrageDTO>> wholeMsgMap = new ConcurrentHashMap<>();


    public boolean init() {
        return GlobalTick.getInstance().addTick(this);
    }

    public boolean joinWatch(String playerIdx, Common.EnumFunction function, int moduleId) {
        leaveWatch(playerIdx);
        String redisKey = getRedisKey(function, moduleId);
        List<String> watchPlayerIdx = watchPlayers.computeIfAbsent(redisKey, a -> new LinkedList<>());
        watchPlayerIdx.add(playerIdx);
        return true;
    }

    public void leaveWatch(String playerIdx, Common.EnumFunction function, int moduleId) {
        String redisKey = getRedisKey(function, moduleId);
        List<String> watchPlayer = watchPlayers.get(redisKey);
        if (CollectionUtils.isEmpty(watchPlayer)) {
            return;
        }
        watchPlayer.remove(playerIdx);
    }

    public void leaveWatch(String playerIdx) {
        for (List<String> watchIdx : watchPlayers.values()) {
            watchIdx.remove(playerIdx);
        }
    }

    private List<BarrageDTO> loadFromRedis(String redisKey) {
        List<String> lrange = jedis.lrange(redisKey, 0, -1);
        if (CollectionUtils.isEmpty(lrange)) {
            return new LimitedQueue<>(GameConfig.getById(GameConst.CONFIG_ID).getMaxbarragesize());
        }
        LimitedQueue<BarrageDTO> msgList =
                new LimitedQueue<>(GameConfig.getById(GameConst.CONFIG_ID).getMaxbarragesize());
        for (String s : lrange) {

            BarrageDTO data = JSON.parseObject(s, BarrageDTO.class);
            msgList.add(data);
        }
        return msgList;
    }

    public RetCodeId.RetCodeEnum playerAddMessage(String playerIdx, Common.EnumFunction function, int moduleId, String msg) {
        if (StringUtils.isBlank(msg)) {
            return RetCodeId.RetCodeEnum.RCE_Barrage_IllegalMsg;
        }
        Long lastAddTimes = addMsgTimes.get(playerIdx);
        if (lastAddTimes != null && GlobalTick.getInstance().getCurrentTime() - lastAddTimes < playerAddMsgInterval) {
            return RetCodeId.RetCodeEnum.RCE_Barrage_MsgIntervalTooShort;
        }
        msg = WordFilterManager.getInstance().replaceBadWord(msg, '*');
        String redisKey = getRedisKey(function, moduleId);
        BarrageDTO value = new BarrageDTO(playerIdx, msg);
        synchronized (this) {
            List<BarrageDTO> addMsg = this.addMsgMap.computeIfAbsent(redisKey, a -> new LimitedQueue<>(GameConfig.getById(GameConst.CONFIG_ID).getMaxbarragesize()));
            addMsg.add(value);

            List<BarrageDTO> wholeMsg = this.wholeMsgMap.computeIfAbsent(redisKey, a -> loadFromRedis(redisKey));
            wholeMsg.add(value);
        }
        saveMsg2Db(redisKey, value);
        addMsgTimes.put(playerIdx, GlobalTick.getInstance().getCurrentTime());
        return RetCodeId.RetCodeEnum.RCE_Success;
    }

    private void saveMsg2Db(String redisKey, BarrageDTO msg) {
        if (jedis.llen(redisKey) >= GameConfig.getById(GameConst.CONFIG_ID).getMaxbarragesize()) {
            jedis.lpop(redisKey);
        }

        jedis.rpush(redisKey, JSON.toJSONString(msg));
    }

    private String getRedisKey(Common.EnumFunction function, int moduleId) {
        Map<Integer, String> keyMap = redisKeyPool.get(function);
        if (keyMap == null) {
            synchronized (this) {
                keyMap = redisKeyPool.get(function);
                if (keyMap == null) {
                    keyMap = new HashMap<>();
                    redisKeyPool.put(function, keyMap);
                }
            }
        }
        String key = keyMap.get(moduleId);
        if (key == null) {
            synchronized (this) {
                key = keyMap.get(moduleId);
                if (StringUtils.isEmpty(key)) {
                    key = GameConst.RedisKey.BarragePrefix + function + ":" + moduleId;
                    keyMap.put(moduleId, key);
                }
            }

        }
        return key;
    }

    private static long nextTickTime;

    @Override
    public void onTick() {
        if (GlobalTick.getInstance().getCurrentTime() < nextTickTime) {
            return;
        }
        sendMsg2WatchPlayer();

        nextTickTime = GlobalTick.getInstance().getCurrentTime() + tickInterval;
    }

    private void sendMsg2WatchPlayer() {
        if (CollectionUtils.isEmpty(addMsgMap)) {
            return;
        }

        if (CollectionUtils.isEmpty(watchPlayers)) {
            addMsgMap.values().forEach(List::clear);
        }

        Map<String, List<BarrageDTO>> temMsgMap = addMsgMap;

        clearAddMsgMap();

        for (Map.Entry<String, List<BarrageDTO>> entry : temMsgMap.entrySet()) {

            List<BarrageDTO> tempAddMsg = entry.getValue();

            if (CollectionUtils.isEmpty(temMsgMap)) {
                return;
            }

            Barrage.SC_UpdateBarrage.Builder msg = buildClientAddMsg(tempAddMsg);

            for (String watchPlayer : watchPlayers.get(entry.getKey())) {
                GlobalData.getInstance().sendMsg(watchPlayer, SC_UpdateBarrage_VALUE, msg);
            }
        }
    }

    private Barrage.SC_UpdateBarrage.Builder buildClientAddMsg(List<BarrageDTO> tempAddMsg) {
        Barrage.SC_UpdateBarrage.Builder msg = Barrage.SC_UpdateBarrage.newBuilder();

        tempAddMsg.forEach(e -> msg.addPlayerIdx(e.getPlayerIdx()).addBarrage(e.getMessage()));
        return msg;
    }

    private void clearAddMsgMap() {
        addMsgMap = new ConcurrentHashMap<>();
    }

    public List<BarrageDTO> getInitMsg(Common.EnumFunction function, int moduleId) {
        String redisKey = getRedisKey(function, moduleId);
        return this.wholeMsgMap.computeIfAbsent(redisKey, a -> loadFromRedis(redisKey));
    }
}
