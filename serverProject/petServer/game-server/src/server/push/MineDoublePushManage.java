/*
package server.push;

import cfg.MineDoubleRewardConfig;
import cfg.MineDoubleRewardConfigObject;
import common.tick.GlobalTick;
import org.apache.commons.collections4.CollectionUtils;
import util.LogUtil;
import util.TimeUtil;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

*/
/**
 * @Description
 * @Author hanx
 * @Date2020/8/6 0006 20:09
 **//*

public class MineDoublePushManage extends PushManage {

    public MineDoublePushManage() {
        super(PushMsgIdEnum.MineDouble);
    }

    private static MineDoublePushManage instance = new MineDoublePushManage();

    public static MineDoublePushManage getInstance() {
        if (instance == null) {
            synchronized (MineDoublePushManage.class) {
                if (instance == null) {
                    instance = new MineDoublePushManage();
                }
            }
        }
        return instance;
    }

    @Override
    public void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (nextTickTime > currentTime) {
            return;
        }
        nextTickTime = calculateNextTickTime(currentTime);
        executePush();
    }

    private long calculateNextTickTime(long curTime) {
        Collection<MineDoubleRewardConfigObject> mineDoubleConfig = MineDoubleRewardConfig._ix_id.values();
        if (CollectionUtils.isEmpty(mineDoubleConfig)) {
            LogUtil.error("mineDouble push close,cause by MineDoubleRewardConfig config is empty");
            return Long.MAX_VALUE;
        }

        long todayBeginTime = TimeUtil.getTodayStamp(curTime);

        List<Long> startTimes = mineDoubleConfig.stream().map(config -> todayBeginTime + config.getStartime() * TimeUtil.MS_IN_A_MIN - leadTime).sorted(Long::compareTo).collect(Collectors.toList());

        return startTimes.stream().filter(startTime -> curTime < startTime).findFirst().orElse(TimeUtil.MS_IN_A_DAY + startTimes.get(0));
    }

    @Override
    public void executePush() {
        sendPushMsgToAll(getMsgIdEnum());
    }

    @Override
    protected void init() {
        super.init();
        nextTickTime = calculateNextTickTime(GlobalTick.getInstance().getCurrentTime());
    }

}
*/
