package model.reward;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.StringUtils;
import platform.logs.ReasonManager.Reason;

/**
 * 统计资源获取来源次数
 * 批量的地方需要特殊处理
 *
 * @author huhan
 * @date 2021/3/12
 */
public class RewardSourceMonitorManager {

    private static RewardSourceMonitorManager instance;

    public static RewardSourceMonitorManager getInstance() {
        if (instance == null) {
            synchronized (RewardSourceMonitorManager.class) {
                if (instance == null) {
                    instance = new RewardSourceMonitorManager();
                }
            }
        }
        return instance;
    }

    private RewardSourceMonitorManager() {
    }

    private final Map<String, ResourceMonitor> monitorMap = new ConcurrentHashMap<>();

    public void recordRewards(String playerIdx, Reason reason) {
        if (reason == null) {
            return;
        }

        ResourceMonitor monitor = getResourceMonitorOrCreate(playerIdx);
        if (monitor == null) {
            return;
        }
        monitor.recordReason(reason);
    }

    public synchronized ResourceMonitor getResourceMonitorOrCreate(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        ResourceMonitor monitor = this.monitorMap.get(playerIdx);
        if (monitor == null) {
            monitor = new ResourceMonitor(playerIdx);
            this.monitorMap.put(playerIdx, monitor);
        }
        return monitor;
    }
}