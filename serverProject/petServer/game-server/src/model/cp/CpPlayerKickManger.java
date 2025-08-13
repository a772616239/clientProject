package model.cp;

import common.GameConst;
import common.JedisUtil;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import common.tick.Tickable;

import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.cp.entity.CpCopyMap;
import protocol.CpFunction;
import util.LogUtil;
import util.TimeUtil;

//玩家副本离开久了自动失败,踢出玩家
@Slf4j
public class CpPlayerKickManger implements Tickable {


    @Getter
    private static CpPlayerKickManger instance = new CpPlayerKickManger();

    private static final String lockKey = GameConst.RedisKey.CpTeamPrefix + "CpPlayerKickMangerLock";

    private long nextTick;

    @Override
    public void onTick() {

        if (GlobalTick.getInstance().getCurrentTime() < nextTick) {
            return;
        }

       if (!JedisUtil.lockRedisKey(lockKey, 9000L)){
           return;
       }
        Map<String, String> map = CpTeamCache.getInstance().findAllCopyPlayerLeaveTime();
        long now = GlobalTick.getInstance().getCurrentTime();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            long kickOutTime = Long.parseLong(entry.getValue()) + TimeUtil.MS_IN_A_MIN * 30;
            if (kickOutTime < now) {
                String copyLockKey = CpCopyManger.getInstance().getCopyUpdateRedisKey(entry.getKey());
                JedisUtil.syncExecBooleanSupplier(copyLockKey, () -> {
                    CpCopyMap mapData = CpCopyManger.getInstance().findMapDataByPlayerId(entry.getKey());
                    if (mapData == null) {
                        LogUtil.warn("check cp player copy leave time,not find map data ,playerId:{}", entry.getKey());
                        CpTeamCache.getInstance().removeCopyPlayerLeaveTime(entry.getKey());
                        return false;
                    }
                    mapData.updatePlayerState(entry.getKey(), CpFunction.CpCopyPlayerState.CCPS_Out);
                    LogUtil.info("cp kick out copy,case leave too long ,playerId:{},copyId:{}", entry.getKey(), mapData.getMapId());
                    CpCopyManger.getInstance().playerLeaveCopy(entry.getKey(), mapData, false, 1);
                    CpBroadcastManager.getInstance().broadcastPlayerState(entry.getKey(), mapData.getRealPlayers(), CpFunction.CpCopyPlayerState.CCPS_Out);
                    CpTeamCache.getInstance().saveCopyMap(mapData);
                    return true;
                });
            }
        }
        JedisUtil.unlockRedisKey(lockKey);
        nextTick += TimeUtil.MS_IN_A_S * 10;
    }


    public boolean init() {
        GlobalTick.getInstance().addTick(this);
        return true;
    }
}
