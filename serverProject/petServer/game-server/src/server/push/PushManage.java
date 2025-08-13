/*
package server.push;

import cfg.PushConfig;
import cfg.PushConfigObject;
import cfg.ServerStringRes;
import common.GameConst.PushTargetType;
import common.GlobalData;
import common.HttpRequestUtil;
import common.entity.PushNotificationData;
import common.tick.GlobalTick;
import common.tick.Tickable;
import lombok.Data;
import model.player.dbCache.playerCache;
import model.player.util.PlayerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Common.LanguageEnum;
import util.LogUtil;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public abstract class PushManage implements Tickable {

    */
/**
     * @see PushMsgIdEnum
     *//*

    protected PushMsgIdEnum msgIdEnum;
    */
/**
     * 对应推送配置
     *//*

    protected PushConfigObject pushConfig;

    */
/**
     * 每天推送的时间点 目前(挂机奖励满推送,玩家回归,迷雾森林pvp推送使用)
     *//*

    protected List<Long> pushTimes;

    */
/**
     * 推送提前时间
     *//*

    protected Long leadTime;

    */
/**
     * 单次推送最大推送条数
     *//*

    protected static final int maxDataSize = 500;


    */
/**
     * 下次tick时间戳
     *//*

    protected long nextTickTime;


    public PushManage(PushMsgIdEnum msgIdEnum) {
        this.msgIdEnum = msgIdEnum;
        init();
    }

    public abstract void executePush();


    */
/**
     * 从todayPushTime中计算下次推送时间
     *
     * @param curPushTime
     * @return
     *//*

    public long calculateNextPushTime(long curPushTime) {

        if (curPushTime <= 0) {
            curPushTime = GlobalTick.getInstance().getCurrentTime();
        }

        long todayStamp = TimeUtil.getTodayStamp(curPushTime);
        long diff = curPushTime - todayStamp;

        if (CollectionUtils.isEmpty(getPushTimes())) {
            LogUtil.error("no push task  pushConfig ConfigId: " + msgIdEnum.getMsgId());
            return Long.MAX_VALUE;
        }
        Optional<Long> nextTime = getPushTimes().stream().filter(time -> diff < time).min(Long::compareTo);
        if (nextTime.isPresent()) {
            return todayStamp + nextTime.get();
        } else {
            return TimeUtil.getNextDaysStamp(curPushTime, 1) + pushTimes.get(0);
        }
    }

    protected void setPushTimes() {
        if (pushConfig == null || pushConfig.getPushtime().length <= 0) {
            LogUtil.error("no push task,pushConfig ConfigId: " + msgIdEnum.getMsgId());
            return;
        }
        String[] pushTime = pushConfig.getPushtime();
        pushTimes = new ArrayList<>();
        for (String str : pushTime) {
            try {
                String[] split = str.split(":");
                pushTimes.add(Integer.parseInt(split[0].trim()) * TimeUtil.MS_IN_A_HOUR + Integer.parseInt(split[1].trim()) * TimeUtil.MS_IN_A_MIN);
            } catch (Exception e) {
                LogUtil.error("pushConfig pushTime config error by pushTime,configId:{}", msgIdEnum.getMsgId());
            }
        }
        pushTimes.sort(Long::compareTo);
    }


    protected void init() {
        pushConfig = PushConfig.getById(msgIdEnum.getMsgId());
        leadTime = pushConfig.getLeadtime() * TimeUtil.MS_IN_A_MIN;
    }


    */
/**
     * 全服推送消息(离线玩家)
     *
     * @param msgIdEnum
     *//*

    public static void sendPushMsgToAll(PushMsgIdEnum msgIdEnum) {
        sendPushMsgToUserList(msgIdEnum, playerCache.getInstance().getUserIdMap().keySet());
    }

    */
/**
     * 推送给指定离线玩家
     *
     * @param msgIdEnum
     * @param userIdList userId
     *//*

    public static void sendPushMsgToUserList(PushMsgIdEnum msgIdEnum, Collection<String> userIdList) {
        LogUtil.info("push msg,msgEnum:{}", msgIdEnum);
        if (CollectionUtils.isEmpty(userIdList)) {
            LogUtil.debug("sendPushMsgToUserList ,userIdxList is empty");
            return;
        }
        int msgId = msgIdEnum.getMsgId();
        PushConfigObject pushConfig = PushConfig.getById(msgId);
        if (pushConfig == null) {
            LogUtil.error("pushConfig is null by msgId:{}", msgId);
            return;
        }
        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
        Map<String, String> userIdMap = playerCache.getInstance().getUserIdMap();
        Map<LanguageEnum, List<String>> collect = userIdList.parallelStream()
                .filter(userId -> canPush(allOnlinePlayerIdx, userIdMap, userId)).collect(Collectors.groupingBy(PlayerUtil::queryLanguageByUid));


        for (Entry<LanguageEnum, List<String>> entry : collect.entrySet()) {
            List<String> uIds = entry.getValue();
            List<String> pageUIds = new ArrayList<>(maxDataSize);
            for (String uId : uIds) {
                if (uId.contains("robot")) {
                    continue;
                }
                if (pageUIds.size() >= maxDataSize) {
                    PushNotificationData pushData = buildPushData(pushConfig, entry.getKey(), pageUIds);
                    HttpRequestUtil.pushNotify(pushData);
                    pageUIds.clear();
                } else {
                    pageUIds.add(uId);
                }

            }
            if (CollectionUtils.isNotEmpty(pageUIds)) {
                PushNotificationData pushData = buildPushData(pushConfig, entry.getKey(), pageUIds);
                HttpRequestUtil.pushNotify(pushData);
            }
        }

    }

    private static boolean canPush(Set<String> allOnlinePlayerIdx, Map<String, String> userIdMap, String userId) {
        String userIdx = userIdMap.get(userId);
        return userIdx != null && !allOnlinePlayerIdx.contains(userIdMap.get(userId));
    }

    */
/**
     * 构建推送实体
     *
     * @param pushConfig
     * @param languageEnum
     * @param pushUIds
     * @return
     *//*

    private static PushNotificationData buildPushData(PushConfigObject pushConfig, LanguageEnum languageEnum, List<String> pushUIds) {
        PushNotificationData pushData = new PushNotificationData();
        String title = ServerStringRes.getContentByLanguage(pushConfig.getTitle(), languageEnum);
        String content = ServerStringRes.getContentByLanguage(pushConfig.getContent(), languageEnum);
        pushData.setTitle(title);
        pushData.setBody(content);
        pushData.setTargetType(PushTargetType.USERID.value());
        pushData.setTargetValue(StringUtils.join(pushUIds, ","));
        return pushData;
    }

    protected boolean todayFirstPush(long curTickTime) {
        if (PushMsgIdEnum.Offline == getMsgIdEnum() || PushMsgIdEnum.OnHook == getMsgIdEnum()) {
            return pushTimes.indexOf(curTickTime) == 0;
        }
        LogUtil.error("todayFirstPush unSupport PushMsgIdEnum" + getMsgIdEnum());
        return false;
    }

    protected long calculateLastPushTimeDifference() {
        long diff = nextTickTime - TimeUtil.getTodayStamp(nextTickTime);
        return todayFirstPush(diff) ? 0 : diff - pushTimes.get(pushTimes.indexOf(diff) - 1);
    }

}
*/
