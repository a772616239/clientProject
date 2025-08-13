package model.reward;

import common.tick.GlobalTick;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Getter;
import model.player.util.PlayerUtil;
import org.apache.commons.lang.StringUtils;
import platform.logs.ReasonManager.Reason;
import protocol.Common.RewardSourceEnum;
import util.LogUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2021/3/12
 */
public class ResourceMonitor {

    /**
     * 检查周期
     */
    public static final long CHECK_INTERVAL = TimeUtil.MS_IN_A_MIN * 5;

    /**
     * 当一个检查周期来源获取次数超过此值需要警告
     */
    public static final int EACH_CHECK_RECORD_COUNT_LIMIT = 50;

    /**
     * 当连续的奖励来源来自同一个来源的次数累计超过此值会警告
     */
    public static final int SAME_SOURCE_COUNT_LIMIT = 20;

    @Getter
    private String playerIdx;
    /**
     * 统计资源来源次数
     */
    private Map<RewardSourceEnum, Integer> sourceCountMap;

    /**
     * 上次清空次数计数时间
     */
    private long nextCheckTime;

    /**
     * 上次获取奖励的reason
     */
    private RewardSourceEnum lastReason;
    /**
     * 已经累计来自相同来源的次数
     */
    private int lastCount;

    public ResourceMonitor(String playerIdx) {
        this.playerIdx = playerIdx;
        this.sourceCountMap = new HashMap<>();
    }

    public synchronized void recordReason(Reason reason) {
        if (needNotRecord(reason)) {
            return;
        }

        if (reason.getSourceEnum() != null) {
            Integer oldVal = this.sourceCountMap.get(reason.getSourceEnum());
            int newValue = oldVal == null ? reason.getCount() : reason.getCount() + oldVal;
            this.sourceCountMap.put(reason.getSourceEnum(), newValue);

            //设置上次获取的来源
            if (this.lastReason == reason.getSourceEnum()) {
                this.lastCount += reason.getCount();
            } else {
                this.lastReason = reason.getSourceEnum();
                this.lastCount = reason.getCount();
            }
            checkLast();
        }

        if (needCheck()) {
            check();
            clearMap();
        }
    }

    private void checkLast() {
        if (this.lastCount >= SAME_SOURCE_COUNT_LIMIT) {
            logAlarm("player continuous gain reward from same source:" + this.lastReason + ", count:" + this.lastCount);
//            this.lastCount = 0;
        }
    }

    private void check() {
        StringBuilder builder = new StringBuilder();
        for (Entry<RewardSourceEnum, Integer> entry : sourceCountMap.entrySet()) {
            if (entry.getValue() >= EACH_CHECK_RECORD_COUNT_LIMIT) {
                builder.append(",source:");
                builder.append(entry.getKey());
                builder.append(",count:");
                builder.append(entry.getValue());
                builder.append("\n");
            }
        }
        if (builder.length() > 0) {
            builder.insert(0, "abnormal condition:");
        }
        logAlarm(builder.toString());
    }

    private void clearMap() {
        this.sourceCountMap.clear();
    }

    private Entry<RewardSourceEnum, Integer> getMaxSourceCount() {
        return this.sourceCountMap.entrySet().stream()
                .max(Comparator.comparingInt(Entry::getValue)).orElse(null);
    }

    private List<Entry<RewardSourceEnum, Integer>> getMoreThanSourceCount(int baseCount) {
        return this.sourceCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() > baseCount)
                .collect(Collectors.toList());
    }

    /**
     * 判断是否需要检查
     */
    private boolean needCheck() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (this.nextCheckTime == 0) {
            this.nextCheckTime = currentTime + CHECK_INTERVAL;
            return false;
        }

        if (currentTime > this.nextCheckTime) {
            this.nextCheckTime = currentTime + CHECK_INTERVAL;
            return true;
        }

        return false;
    }

    private boolean needNotRecord(Reason reason) {
        if (reason == null
                || reason.getSourceEnum() == RewardSourceEnum.RSE_GM) {
            return true;
        }
        return false;
    }

    private void logAlarm(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("========================RewardsGainAlarm  start=================================");
        builder.append("\n");
        builder.append("======================playerBaseInfo: playerIdx:" + playerIdx
                + ", playerName:" + PlayerUtil.queryPlayerName(playerIdx));
        builder.append("\n");
        builder.append("======================" + msg);
        builder.append("\n");
        builder.append("==============================================RewardsGainAlarm  end===================================");
        LogUtil.warn(builder.toString());
    }
}
