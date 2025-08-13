/*
package server.push;

import cfg.MailTemplateUsed;
import common.GameConst;
import common.GlobalData;
import common.tick.GlobalTick;
import model.player.dbCache.playerCache;
import platform.logs.ReasonManager;
import protocol.Common.RewardSourceEnum;
import util.EventUtil;
import util.TimeUtil;

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

public class OfflinePushManage extends PushManage {

    public OfflinePushManage() {
        super(PushMsgIdEnum.Offline);
    }

    private static OfflinePushManage instance = new OfflinePushManage();

    private static final long offlineLimit = 2 * TimeUtil.MS_IN_A_DAY;

    */
/**
     * 当天当次推送与上一次推送的时间差
     *//*

    private long todayLastPushTimeDifference;


    private static final long sendGiftLimit = offlineLimit + TimeUtil.MS_IN_A_DAY;

    public static OfflinePushManage getInstance() {
        if (instance == null) {
            synchronized (OfflinePushManage.class) {
                if (instance == null) {
                    instance = new OfflinePushManage();
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
        nextTickTime = calculateNextPushTime(currentTime);
        executePush();
    }

    private boolean alreadyPushToday(long offlineTime) {
        return todayLastPushTimeDifference > 0 && todayLastPushTimeDifference < offlineTime;
    }

    @Override
    public void executePush() {
        todayLastPushTimeDifference = calculateLastPushTimeDifference();

        Map<String, String> userIdMap = playerCache.getInstance().getUserIdMap();
        Set<String> onlineIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
        List<String> needPushUid = new ArrayList<>();

        List<String> needPushMailIds = new ArrayList<>();
        for (Entry<String, String> entry : userIdMap.entrySet()) {
            if (onlineIdx.contains(entry.getValue())) {
                continue;
            }
            long offlineTime = playerCache.getInstance().getOfflineTime(entry.getValue());

            if (alreadyPushToday(offlineTime)) {
                continue;
            }

            if (offlineTime > offlineLimit) {
                needPushUid.add(entry.getKey());
                if (offlineTime < sendGiftLimit) {
                    needPushMailIds.add(entry.getValue());
                }
            }
        }
        //推送通知
        sendPushMsgToUserList(getMsgIdEnum(), needPushUid);

        //发送邮件奖励
        for (String uid : needPushMailIds) {
            EventUtil.triggerAddMailEvent(uid, MailTemplateUsed.getById(GameConst.CONFIG_ID).getPlayerreturn(),
                    null, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PlayerReturn));
        }
    }

    @Override
    protected void init() {
        super.init();
        setPushTimes();
        nextTickTime = calculateNextPushTime(GlobalTick.getInstance().getCurrentTime());
    }

}
*/
