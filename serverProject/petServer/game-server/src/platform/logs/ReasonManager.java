package platform.logs;

import common.tick.GlobalTick;
import lombok.Getter;
import lombok.Setter;
import protocol.Common.RewardSourceEnum;
import util.LogUtil;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author huhan
 * @date 2020/1/9
 */
public class ReasonManager {
    private static ReasonManager instance;

    public static ReasonManager getInstance() {
        if (instance == null) {
            synchronized (ReasonManager.class) {
                if (instance == null) {
                    instance = new ReasonManager();
                }
            }
        }
        return instance;
    }

    private ReasonManager() {
    }

    /**
     * 空的Reason列表
     **/
    private final Queue<Reason> reasonList = new ConcurrentLinkedQueue<>();

    private final ConcurrentHashMap<Reason, Long> borrowMap = new ConcurrentHashMap<>();
    /**
     * Reason 借出的超时时间， 通过过期时间后回收
     **/
    private final static long EXPIRE_TIME = TimeUtil.MS_IN_A_S * 10;
    /**
     * 回收检查间隔时间
     **/
    private final static long RECYCLE_INTERVAL = TimeUtil.MS_IN_A_S * 5;
    private long nextRecycleReasonTime;

    public Reason borrowReason() {
        Reason poll = reasonList.poll();
        if (poll == null) {
            poll = new Reason();
        }
        borrowMap.put(poll, GlobalTick.getInstance().getCurrentTime() + EXPIRE_TIME);
        recycleExpireReason();
        return poll;
    }

    public Reason borrowReason(RewardSourceEnum sourceEnum, int count) {
        return borrowReason(sourceEnum, null, count);
    }

    public Reason borrowReason(RewardSourceEnum sourceEnum, String str) {
        return borrowReason(sourceEnum, str, 1);
    }

    public Reason borrowReason(RewardSourceEnum sourceEnum) {
        return borrowReason(sourceEnum, null, 1);
    }

    public Reason borrowReason(String str) {
        return borrowReason(null, str, 1);
    }

    public Reason borrowReason(RewardSourceEnum sourceEnum, String str, int count) {
        Reason reason = borrowReason();
        reason.setSourceEnum(sourceEnum);
        reason.setStr(str);
        reason.setCount(count);
        return reason;
    }

    private boolean returnReason(Reason reason) {
        if (reason == null) {
            return false;
        }

        reason.clear();

        return this.reasonList.add(reason);
    }

    /**
     * 回收过期Reason
     */
    private synchronized void recycleExpireReason() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (nextRecycleReasonTime == 0) {
            nextRecycleReasonTime = currentTime;
        }

        if (currentTime < nextRecycleReasonTime) {
            return;
        }

        LogUtil.debug("ReasonManager.recycleExpireReason, before recycle, reasonList size = " + reasonList.size() + ", borrowMap size = " + borrowMap.size());
        List<Reason> needRecycle = new ArrayList<>();
        for (Entry<Reason, Long> entry : borrowMap.entrySet()) {
            if (currentTime > entry.getValue()) {
                needRecycle.add(entry.getKey());
            }
        }

        if (!needRecycle.isEmpty()) {
            for (Reason reason : needRecycle) {
                borrowMap.remove(reason);
                returnReason(reason);
            }
        }

        nextRecycleReasonTime = currentTime + RECYCLE_INTERVAL;
        LogUtil.debug("ReasonManager.recycleExpireReason, after recycle, reasonList size = " + reasonList.size() + ", borrowMap size = " + borrowMap.size());
    }

    @Getter
    @Setter
    public static class Reason {
        /**
         * reason str 长度限制
         */
        public static final int STR_LENGTH_LIMIT = 20;

        private RewardSourceEnum sourceEnum;
        private String str;
        private int count;

        private Reason() {
        }

        public int getSourceNum() {
            return sourceEnum == null ? 0 : sourceEnum.getNumber();
        }

        public void addStr(String str) {
            if (str == null) {
                return;
            }

            if (str.length() > STR_LENGTH_LIMIT) {
                LogUtil.error("platform.logs.ReasonManager.Reason.addStr, add str length is max than str length limit, str:"
                        + str + ", skip this str add");
                return;
            }

            if (this.str == null) {
                this.str = str;
            } else if (this.str.length() < STR_LENGTH_LIMIT) {
                this.str += str;
            }
        }

        public void clear() {
            sourceEnum = null;
            str = null;
        }

        @Override
        public String toString() {
            return StatisticsLogUtil.getRewardSourceName(sourceEnum) + (str == null ? " " : str);
        }
    }
}