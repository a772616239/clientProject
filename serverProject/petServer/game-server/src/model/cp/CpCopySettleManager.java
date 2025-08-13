package model.cp;

import common.GameConst;
import common.JedisUtil;
import common.tick.GlobalTick;
import common.tick.Tickable;

import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.cp.entity.CpCopyMap;
import org.springframework.util.CollectionUtils;
import server.handler.cp.CpFunctionUtil;
import util.TimeUtil;

@Slf4j
public class CpCopySettleManager implements Tickable {

    @Getter
    private static final CpCopySettleManager instance = new CpCopySettleManager();


    private static final String settleCopyExpireLock = GameConst.RedisKey.CpTeamPrefix + "settleExpireLock";

    public boolean init() {
        return GlobalTick.getInstance().addTick(this);
    }

    private long nextTick;

    private final long interval = TimeUtil.MS_IN_A_S;

    @Override
    public void onTick() {

        if (GlobalTick.getInstance().getCurrentTime() < nextTick) {
            return;
        }
        settleExpireCopy();

        nextTick = GlobalTick.getInstance().getCurrentTime() + interval;
    }

    private void settleExpireCopy() {

        JedisUtil.syncExecBooleanSupplier(settleCopyExpireLock, () -> {

            Map<String, String> data = CpTeamCache.getInstance().loadAllPlayerMapExpire();

            if (CollectionUtils.isEmpty(data)) {
                return true;
            }
            for (Map.Entry<String, String> entry : data.entrySet()) {
                long expireTime = Long.parseLong(entry.getValue());
                if (expireTime > GlobalTick.getInstance().getCurrentTime()) {
                    continue;
                }
                CpCopyMap cpCopyMap = CpTeamCache.getInstance().loadCopyMapInfo(entry.getKey());
                if (cpCopyMap != null) {
                    for (String member : CpFunctionUtil.findPlayerIds(cpCopyMap.getMembers())) {
                        CpCopyManger.getInstance().playerLeaveCopy(member, cpCopyMap, false);
                    }
                    log.info("cp copy expire,ready to settle,mapId:{},players:{}", cpCopyMap.getMapId(), cpCopyMap.getInitRealPlayerIds());
                    CpBroadcastManager.getInstance().broadcastCopySettle(cpCopyMap);
                }
                CpTeamCache.getInstance().removeCopyExpire(entry.getKey());
            }
            return true;

        });


    }

}
