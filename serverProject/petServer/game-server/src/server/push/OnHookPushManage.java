/*
package server.push;

import common.GlobalData;
import common.tick.GlobalTick;
import model.mainLine.dbCache.mainlineCache;
import model.player.dbCache.playerCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

*/
/**
 * @Description
 * @Author hanx
 * @Date2020/8/6 0006 20:09
 **//*

public class OnHookPushManage extends PushManage {

    public OnHookPushManage() {
        super(PushMsgIdEnum.OnHook);
    }

    private static OnHookPushManage instance = new OnHookPushManage();

    */
/**
     * 当天当次推送与上一次推送的时间差
     *//*

    private long todayLastPushTimeDifference;

    public static OnHookPushManage getInstance() {
        if (instance == null) {
            synchronized (OnHookPushManage.class) {
                if (instance == null) {
                    instance = new OnHookPushManage();
                }
            }
        }
        return instance;
    }


    @Override
    public void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (this.nextTickTime > currentTime) {
            return;
        }
        nextTickTime = calculateNextPushTime(currentTime);
        executePush();
    }

    @Override
    public void executePush() {
        todayLastPushTimeDifference = calculateLastPushTimeDifference();

        Map<String, String> userIdMap = playerCache.getInstance().getUserIdMap();
        Set<String> onlineIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();

        List<String> needPushUid = new ArrayList<>();
        for (Entry<String, String> entry : userIdMap.entrySet()) {
            if (entry.getKey().contains("robot")) {
                continue;
            }
            if (onlineIdx.contains(entry.getValue())) {
                continue;
            }
            long completeTime = mainlineCache.getInstance().OnHookCompleteTime(entry.getValue());
            if (completeTime < 0 || (todayLastPushTimeDifference > 0 && todayLastPushTimeDifference < completeTime)) {
                continue;
            }
            needPushUid.add(entry.getKey());
        }
        sendPushMsgToUserList(getMsgIdEnum(), needPushUid);
    }

    @Override
    protected void init() {
        super.init();
        setPushTimes();
        nextTickTime = calculateNextPushTime(GlobalTick.getInstance().getCurrentTime());
    }
}
*/
