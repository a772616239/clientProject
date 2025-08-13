/*
package server.push;

import cfg.GameConfig;
import common.GameConst;
import common.tick.GlobalTick;
import org.apache.commons.collections4.CollectionUtils;
import util.LogUtil;
import util.TimeUtil;

import java.util.ArrayList;

*/
/**
 * @Description
 * @Author hanx
 * @Date2020/8/6 0006 20:09
 **//*

public class MistPvPPushManage extends PushManage {

    public MistPvPPushManage() {
        super(PushMsgIdEnum.MistPvP);
    }

    private static MistPvPPushManage instance = new MistPvPPushManage();

    public static MistPvPPushManage getInstance() {
        if (instance == null) {
            synchronized (MistPvPPushManage.class) {
                if (instance == null) {
                    instance = new MistPvPPushManage();
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
        nextTickTime = calculateNextPushTime(nextTickTime);
        executePush();
    }


    @Override
    public void executePush() {
        sendPushMsgToAll(getMsgIdEnum());
    }

    @Override
    protected void init() {
        super.init();
        setTodayPushTime();
        nextTickTime = calculateNextPushTime(nextTickTime);

    }

    private void setTodayPushTime() {
        int[][] startTime = GameConfig.getById(GameConst.CONFIG_ID).getMistforcepvpstarttime();
        if (startTime.length <= 0) {
            LogUtil.error("mist force pvp not open case by gameConfig misForcePvpStartTime not config");
            return;
        }
        for (int[] data : startTime) {
            this.pushTimes = new ArrayList<>();
            if (data.length > 0) {
                pushTimes.add(data[0] * TimeUtil.MS_IN_A_MIN - leadTime);
            }
        }
        if (CollectionUtils.isNotEmpty(pushTimes)) {
            pushTimes.sort(Long::compareTo);
        }
    }
}
*/
