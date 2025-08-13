package model.stoneRift;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import common.JedisUtil;
import common.tick.GlobalTick;
import common.tick.Tickable;
import db.entity.BaseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.player.util.PlayerUtil;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbStoneRift;
import model.stoneRift.entity.StoneRiftMsg;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.core.util.JsonUtils;
import protocol.StoneRift;
import util.TimeUtil;

import static common.GameConst.RedisKey.StoneRiftPrefix;

@Slf4j
public class StoneRiftManager implements Tickable {

    private static final String msgKey = StoneRiftPrefix + "msg:";

    @Getter
    private static final StoneRiftManager instance = new StoneRiftManager();

    private final Map<String, Long> settleTimeMap = new ConcurrentHashMap<>();

    private long nextTick;

    public boolean init() {
        StoneRiftCfgManager.getInstance().init();
        for (BaseEntity entity : stoneriftCache.getInstance()._ix_id.values()) {
            settleTimeMap.put(entity.getBaseIdx(), ((stoneriftEntity) entity).getDB_Builder().getNextSettleTime());

        }
        clacuteNextTick();
        GlobalTick.getInstance().addTick(this);
        return true;
    }

    public void addStoneEntity(String idx, DbStoneRift dbStoneRift) {
        this.settleTimeMap.put(idx, dbStoneRift.getNextSettleTime());
    }

    public void updateNextSettleTime(String entityId, long nextSettleTime) {
        if (nextSettleTime < GlobalTick.getInstance().getCurrentTime()) {
            log.error("update StoneRift settleTime error,nextSettleTime:{} less than time,entityId:{}", nextSettleTime, entityId);
        }
        this.settleTimeMap.put(entityId, nextSettleTime);
    }


    @Override
    public void onTick() {
        long now = GlobalTick.getInstance().getCurrentTime();
        if (nextTick < now) {
            return;
        }
        settleTimeMap.forEach((k, time) -> {
            if (time < now) {
                stoneriftEntity entity = stoneriftCache.getByIdx(k);
                if (entity != null) {
                    entity.settleReward();
                }
            }
        });

        clacuteNextTick();

    }

    private void clacuteNextTick() {
      /*  if (MapUtils.isEmpty(settleTimeMap)) {
            this.nextTick = GlobalTick.getInstance().getCurrentTime() + TimeUtil.MS_IN_A_MIN;
        }
        nextTick = settleTimeMap.values().stream().min(Long::compareTo).orElse(GlobalTick.getInstance().getCurrentTime() + TimeUtil.MS_IN_A_MIN);*/
        this.nextTick = GlobalTick.getInstance().getCurrentTime() + TimeUtil.MS_IN_A_MIN;

    }

    /**
     * 拉取留言
     * @param playerId
     * @param page
     * @return
     */
    public List<StoneRiftMsg> claimStoneRiftMsg(String playerId, int page) {
        List<String> lrange = JedisUtil.jedis.lrange(msgKey + playerId, page * 20L, (page + 1) * 20L);
        if (CollectionUtils.isEmpty(lrange)) {
            return Collections.emptyList();
        }
        return lrange.stream().map(e -> JSON.parseObject(e, StoneRiftMsg.class)).collect(Collectors.toList());
    }

    public void addPlayerMsg(String playerId, StoneRiftMsg msg) {
        JedisUtil.jedis.lpush(msgKey + playerId, JSON.toJSONString(msg));
    }

}
