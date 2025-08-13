package platform.logs.statistics;

import common.tick.GlobalTick;
import db.entity.BaseEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import util.MapUtil;
import util.TimeUtil;

/**
 * @Description
 * @Author hanx
 * @Date2020/10/14 0014 14:51
 **/
public class EndlessSpireStatistics extends AbstractStatistics {

    private static final EndlessSpireStatistics instance = new EndlessSpireStatistics();

    public static EndlessSpireStatistics getInstance() {
        return instance;
    }

    private EndlessSpireStatistics() {
    }

    private static long nextStatisticsTime;
    private static long refreshInterval = TimeUtil.MS_IN_A_MIN * 10;

    /**
     * 《层数，停留人数》
     */
    private final Map<Integer, Integer> stayMap = new ConcurrentHashMap<>();

    public Map<Integer, Integer> getStayMap() {
        if (GlobalTick.getInstance().getCurrentTime() < nextStatisticsTime) {
            statistics();
        }
        return stayMap;
    }

    @Override
    public void init() {
        statistics();
    }

    private void statistics() {
        for (BaseEntity value : playerCache.getInstance()._ix_id.values()) {
            playerEntity player = (playerEntity) value;
            int maxSpireLv = player.getDb_data().getEndlessSpireInfo().getMaxSpireLv();
            MapUtil.add2IntMapValue(stayMap, maxSpireLv + 1, 1);
        }
        nextStatisticsTime += refreshInterval;
    }

}
