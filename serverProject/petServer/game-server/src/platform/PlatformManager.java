package platform;

import cfg.Marquee;
import cfg.MarqueeObject;
import cfg.MarqueeTemplate;
import cfg.MarqueeTemplateObject;
import cfg.ServerStringRes;
import common.GameConst;
import common.GlobalData;
import common.IdGenerator;
import common.tick.GlobalTick;
import common.tick.Tickable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.gameplay.GamePlayerUpdate;
import model.gameplay.dbCache.gameplayCache;
import model.gameplay.entity.gameplayEntity;
import model.mailbox.dbCache.mailboxCache;
import model.mailbox.util.MailUtil;
import model.player.dbCache.playerCache;
import model.player.util.PlayerUtil;
import org.apache.commons.lang.StringUtils;
import platform.logs.ReasonManager;
import protocol.Common.LanguageEnum;
import protocol.Common.RewardSourceEnum;
import protocol.Common.SC_Marquee;
import protocol.GameplayDB.DB_MailTemplate;
import protocol.GameplayDB.DB_MailTemplate.Builder;
import protocol.GameplayDB.DB_Marquee;
import protocol.GameplayDB.DB_PlatformInfo;
import protocol.GameplayDB.DailyTimeScope;
import protocol.GameplayDB.EnumMarqueeCycleType;
import protocol.GameplayDB.GameplayTypeEnum;
import protocol.GameplayDB.MarqueeCycle;
import protocol.MailDB.DB_MailInfo;
import protocol.MessageId.MsgIdEnum;
import util.ArrayUtil;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2019/11/22
 * @function 用于管理平台工具消息
 */
public class PlatformManager implements GamePlayerUpdate, Tickable {
    private static PlatformManager instance;

    public static PlatformManager getInstance() {
        if (instance == null) {
            synchronized (PlatformManager.class) {
                if (instance == null) {
                    instance = new PlatformManager();
                }
            }
        }
        return instance;
    }

    private PlatformManager() {
    }

    private static final String platformInfoIdx = String.valueOf(GameplayTypeEnum.GTE_PlatformInfo_VALUE);

    /**
     * <TemplateId, Template.builder>
     */
    private final Map<Long, DB_MailTemplate.Builder> mailTemplateMap = new ConcurrentHashMap<>();
    private final Map<Integer, DB_Marquee.Builder> marqueeMap = new ConcurrentHashMap<>();
    /**
     * 封禁相关提示信息
     **/
    private final Map<Long, String> banMsgMap = new ConcurrentHashMap<>();
    private long nextUpdateTime;

    public boolean init() {
        gameplayEntity byIdx = gameplayCache.getByIdx(platformInfoIdx);
        DB_PlatformInfo.Builder platformInfo;
        if (byIdx == null) {
            platformInfo = DB_PlatformInfo.newBuilder();
        } else {
            try {
                platformInfo = DB_PlatformInfo.parseFrom(byIdx.getGameplayinfo()).toBuilder();
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
                platformInfo = DB_PlatformInfo.newBuilder();
            }
        }

        initMail(platformInfo.getTemplatesList());
        initMarquee(platformInfo.getMarqueesList());
        initLocalMarquee();
        this.banMsgMap.putAll(platformInfo.getBanMsgMap());

        return GlobalTick.getInstance().addTick(this)
                && gameplayCache.getInstance().addToUpdateSet(this);
    }

    private void initLocalMarquee() {
        for (MarqueeObject value : Marquee._ix_id.values()) {
            if (value.getInterval() < GameConst.MARQUEE_MIN_INTERVAL_S) {
                LogUtil.error("marquee local config error, interval time < 60, skip it, marquee id =" + value.getId());
                continue;
            }

            MarqueeTemplateObject templateObject = MarqueeTemplate.getById(value.getMarqueetemplateid());
            if (templateObject == null) {
                LogUtil.error("marquee template is not exist, id =" + value.getMarqueetemplateid());
                continue;
            }

            DB_Marquee.Builder builder = DB_Marquee.newBuilder();
            //设置模板内容
            builder.addAllScenesValue(ArrayUtil.intArrayToList(templateObject.getScene()));
            builder.setCycleTimes(templateObject.getRolltimes());
            builder.setPriorityValue(templateObject.getPriority());
            builder.setDuration(templateObject.getDuration());
            builder.putAllContent(ServerStringRes.buildLanguageContentMap(templateObject.getContent()));

            builder.setNoticeId(value.getId());
            builder.setStartTime(TimeUtil.parseTime(value.getStarttime()));
            builder.setEndTime(TimeUtil.parseTime(value.getEndtime()));
            builder.setInterval(value.getInterval() * TimeUtil.MS_IN_A_S);
            builder.setNextSendTime(builder.getStartTime());
            if (value.getCycletype() != 0) {
                MarqueeCycle.Builder cycleBuilder = MarqueeCycle.newBuilder();
                cycleBuilder.setCycleTypeValue(value.getCycletype());
                Arrays.stream(value.getValidday()).forEach(cycleBuilder::addValidDay);
                int[][] timeScope = value.getTimescope();
                for (int[] ints : timeScope) {
                    if (ints.length < 2) {
                        LogUtil.warn("marquee local config error, time scope length < 2, skip it, marquee id =" + value.getId());
                        continue;
                    }
                    cycleBuilder.addScopes(DailyTimeScope.newBuilder()
                            .setDayOfStartTime(Math.min(ints[0], ints[1]) * TimeUtil.MS_IN_A_MIN)
                            .setDayOfEndTime(Math.max(ints[0], ints[1]) * TimeUtil.MS_IN_A_MIN)
                            .build());
                }
                builder.setCycle(cycleBuilder);
            }

            marqueeMap.put(builder.getNoticeId(), builder);
        }
    }

    public boolean isLocalMarquee(DB_Marquee.Builder builder) {
        if (null == builder) {
            return false;
        }

        return null != Marquee.getById(builder.getNoticeId());
    }

    private void initMail(List<DB_MailTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            return;
        }
        for (DB_MailTemplate template : templates) {
            mailTemplateMap.put(template.getTemplateId(), template.toBuilder());
        }
    }

    private void initMarquee(List<DB_Marquee> marquees) {
        if (marquees == null || marquees.isEmpty()) {
            return;
        }

        for (DB_Marquee marquee : marquees) {
            marqueeMap.put(marquee.getNoticeId(), marquee.toBuilder());
        }
    }

    public void deleteTemplate(long templateId) {
        mailTemplateMap.remove(templateId);
    }

    public boolean addMailTemplate(DB_MailTemplate.Builder newTemplate) {
        if (!checkMail(newTemplate)) {
            LogUtil.debug("add platForm mail is failed, content " + newTemplate.toString());
            return false;
        }
        mailTemplateMap.put(newTemplate.getTemplateId(), newTemplate);
        return true;
    }

    /**
     * 检查过期邮件以及跑马灯, 并持久化到数据库
     */
    @Override
    public void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (currentTime < nextUpdateTime) {
            return;
        }

        mailTemplateTick();
        marqueeTick();

        nextUpdateTime = currentTime + GameConst.MARQUEE_MIN_INTERVAL_MS;
    }

    /**
     * 邮件模板永久保存,因为邮件内容是从模板里面取
     */
    private void mailTemplateTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();

        for (DB_MailTemplate.Builder value : mailTemplateMap.values()) {
            if (value.getType() == TemplateTypeEnum.TTE_NOW_ALL_PLAYER) {
                //已经发送到发放全服
                if (value.getSendToAllPlayer()) {
                    continue;
                }
                addMailToAllPlayer(value, playerCache.getInstance().getAllPlayerIdx());
                value.setSendToAllPlayer(true);

            } else if (value.getType() == TemplateTypeEnum.TTE_NOW_TARGET_PLAYER) {
                //已经发送到发放到指定玩家
                if (value.getSendToAllPlayer()) {
                    continue;
                }
                addMailToAllPlayer(value, value.getTargetPlayerList());
                value.setSendToAllPlayer(true);

            } else if (value.getType() == TemplateTypeEnum.TTE_RANGE_ONLINE_PLAYER) {
                if (GameUtil.outOfScope(value.getStartTime(), value.getExpireTime(), currentTime)) {
                    continue;
                }

                Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
                for (String onlinePlayer : allOnlinePlayerIdx) {
                    if (!value.getSendPlayerList().contains(onlinePlayer)) {
                        addMailToPlayer(onlinePlayer, value);
                        value.addSendPlayer(onlinePlayer);
                    }
                }
            }
        }
    }

    public void addMailToAllPlayer(DB_MailTemplate.Builder template, Collection<String> targetPlayerIdx) {
        if (template == null || GameUtil.collectionIsEmpty(targetPlayerIdx)) {
            return;
        }
        targetPlayerIdx.forEach(value -> addMailToPlayer(value, template));
    }

    private void marqueeTick() {
        List<Integer> removeId = new ArrayList<>();
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        for (DB_Marquee.Builder value : marqueeMap.values()) {
            //过期
            if (currentTime > value.getEndTime()) {
                removeId.add(value.getNoticeId());
                continue;
            }

            //判断是否在有效区间内
            if (!marqueeIsInValidScope(value.getCycle())) {
                continue;
            }

            if (currentTime > value.getNextSendTime()) {
                sendMarqueeToAllPlayer(value);
                value.setNextSendTime(currentTime + value.getInterval());
            }
        }

        if (!removeId.isEmpty()) {
            for (Integer integer : removeId) {
                deleteMarquee(integer);
            }
        }
    }

    /**
     * 判断跑马灯是否在生效时段内
     *
     * @return
     */
    private boolean marqueeIsInValidScope(MarqueeCycle marqueeCycle) {
        if (null == marqueeCycle || EnumMarqueeCycleType.EMCT_NULL == marqueeCycle.getCycleType()
                || marqueeCycle.getScopesCount() <= 0) {
            return true;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        long dayOfTime = currentTime - TimeUtil.getTodayStamp(currentTime);

        if (EnumMarqueeCycleType.EMCT_DAY == marqueeCycle.getCycleType()) {
            //
        } else if (EnumMarqueeCycleType.EMCT_WEEK == marqueeCycle.getCycleType()) {
            int dayOfWeek = TimeUtil.getDayOfWeek(currentTime);
            if (!marqueeCycle.getValidDayList().contains(dayOfWeek)) {
                return false;
            }
        } else if (EnumMarqueeCycleType.EMCT_MONTH == marqueeCycle.getCycleType()) {
            int dayOfMonth = TimeUtil.getDayOfMonth(currentTime);
            if (!marqueeCycle.getValidDayList().contains(dayOfMonth)) {
                return false;
            }
        }

        return marqueeCycle.getScopesList().stream()
                .anyMatch(e -> GameUtil.inScope(e.getDayOfStartTime(), e.getDayOfEndTime(), dayOfTime));

    }

    private void sendMarqueeToAllPlayer(DB_Marquee.Builder value) {
        LogUtil.debug("send marquee to all on line player, marquee id:" + value.getNoticeId());
        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();

        //用于保存相同语言枚举的跑马灯
        Map<LanguageEnum, SC_Marquee.Builder> marquees = new HashMap<>();
        for (String onlinePlayerIdx : allOnlinePlayerIdx) {
            LanguageEnum languageEnum = PlayerUtil.queryPlayerLanguage(onlinePlayerIdx);
            SC_Marquee.Builder builder = marquees.get(languageEnum);
            if (builder == null) {
                builder = SC_Marquee.newBuilder();
                marquees.put(languageEnum, builder);

                builder.setInfo(value.getContentMap().get(languageEnum.getNumber()));
                builder.setCycleCount(value.getCycleTimes());
                builder.addAllScenes(value.getScenesList());
                builder.setPriority(value.getPriority());
                builder.setDuration(value.getDuration());
            }
            GlobalData.getInstance().sendMsg(onlinePlayerIdx, MsgIdEnum.SC_Marquee_VALUE, builder);
        }
    }

    public boolean addMarquee(DB_Marquee.Builder marquee) {
        if (marquee == null) {
            return false;
        }

        if (marqueeMap.containsKey(marquee.getNoticeId())) {
            LogUtil.warn("repeated the same id marquee, marqueeId:" + marquee.getNoticeId());
        }

        marqueeMap.put(marquee.getNoticeId(), marquee);
        return true;
    }

    public synchronized void deleteMarquee(int id) {
        marqueeMap.remove(id);
    }

    private void addMailToPlayer(String playerIdx, DB_MailTemplate.Builder template) {
        DB_MailInfo.Builder builder = builderDBMailInfo(PlayerUtil.queryPlayerLanguage(playerIdx), template);
        if (builder == null) {
            return;
        }

        EventUtil.triggerAddMailEvent(playerIdx, builder, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Platform));
    }

    public static DB_MailInfo.Builder builderDBMailInfo(LanguageEnum languageEnum, DB_MailTemplate.Builder template) {
        if (template == null || languageEnum == null) {
            return null;
        }
        DB_MailInfo.Builder builder = DB_MailInfo.newBuilder();
        builder.setMailIdx(IdGenerator.getInstance().generateId());
        builder.setMailTemplateId(template.getTemplateId());
        builder.setCreateTime(GlobalTick.getInstance().getCurrentTime());
        builder.setExpireTime(GlobalTick.getInstance().getCurrentTime() + MailUtil.defaultValidTime);
        if (template.getParamsCount() > 0) {
            builder.addAllParam(template.getParamsList());

        }
        builder.addAllRewards(template.getRewardsList());
        return builder;
    }

    private boolean checkMail(DB_MailTemplate.Builder newTemplate) {
        if (newTemplate == null) {
            return false;
        }
        if (mailTemplateMap.containsKey(newTemplate.getTemplateId())) {
            LogUtil.error("templateId is already exist, id:" + newTemplate.getTemplateId());
            return false;
        }
        return true;
    }

    public void onPlayerLogIn(String playerIdx) {
        mailOnPlayerLogIn(playerIdx);
    }

    private void mailOnPlayerLogIn(String playerIdx) {
        for (Builder value : mailTemplateMap.values()) {
            if (value.getType() == TemplateTypeEnum.TTE_RANGE_ONLINE_PLAYER
                    && GameUtil.inScope(value.getExpireTime(), value.getStartTime(), GlobalTick.getInstance().getCurrentTime())
                    && !value.getSendPlayerList().contains(playerIdx)) {
                addMailToPlayer(playerIdx, value);
                value.addSendPlayer(playerIdx);
            }
        }
    }

    /**
     * ****************************慎用************************
     * 此方法会遍历所有玩家删除,玩家已经领取或者邮件已经删除 无法撤回, 只对未领取附件的模板有实际作用
     * <p>
     * 撤销邮件模板(
     *
     * @param mailTemplate
     * @return
     */
    public boolean cancelMailTemplate(long mailTemplate) {
        //先尝试从所有玩家移除,再移除模板
        if (!mailboxCache.getInstance().deleteAllPlayerMailByTemplateId(mailTemplate)) {
            return false;
        }
        mailTemplateMap.remove(mailTemplate);
        return true;
    }

    /**
     * @param msg
     * @return 保存的id
     */
    public long addBanMsg(String msg) {
        if (StringUtils.isBlank(msg)) {
            return 0;
        }
        //是否存在相同内容
        for (Entry<Long, String> entry : banMsgMap.entrySet()) {
            if (Objects.equals(entry.getValue(), msg)) {
                return entry.getKey();
            }
        }

        long generateIdNum = IdGenerator.getInstance().generateIdNum();
        banMsgMap.put(generateIdNum, msg);
        return generateIdNum;
    }

    public String getBanMsg(long msgId, LanguageEnum language) {
        String msg = banMsgMap.get(msgId);
        if (msg == null) {
            return "";
        }
        return GameUtil.getLanguageStr(msg, language);
    }

    /**
     * 保存到数据库
     */
    @Override
    public synchronized void update() {
        gameplayEntity byIdx = gameplayCache.getByIdx(platformInfoIdx);
        if (byIdx == null) {
            byIdx = new gameplayEntity();
            byIdx.setIdx(platformInfoIdx);
        }
        DB_PlatformInfo.Builder builder = DB_PlatformInfo.newBuilder();
        for (Builder value : mailTemplateMap.values()) {
            builder.addTemplates(value);
        }
        for (DB_Marquee.Builder value : marqueeMap.values()) {
            //不保存本地配置的跑马灯
            if (isLocalMarquee(value)) {
                continue;
            }
            builder.addMarquees(value);
        }
        builder.putAllBanMsg(banMsgMap);
        byIdx.setGameplayinfo(builder.build().toByteArray());
        byIdx.putToCache();
    }

    public void clearMarquee() {
        marqueeMap.clear();
    }

    public void clearMail() {
        mailTemplateMap.clear();
    }

    public String getSenderByTemplateId(long templateId, LanguageEnum language) {
        return ServerStringRes.getContentByLanguage(10, language);
    }

    public String getTitleByTemplateId(long templateId, LanguageEnum language) {
        Builder builder = mailTemplateMap.get(templateId);
        if (builder != null) {
            String title = builder.getTitleMap().get(language == null ? 0 : language.getNumber());
            if (title != null) {
                return title;
            }
        }
        return "";
    }

    public String getBodyByTemplateId(long templateId, LanguageEnum language, Object... params) {
        Builder builder = mailTemplateMap.get(templateId);
        if (builder != null) {
            String body = builder.getBodyMap().get(language == null ? 0 : language.getNumber());
            if (body != null) {
                return MessageFormat.format(body, params);
            }
        }
        return "";
    }

    public static class TemplateTypeEnum {
        /**
         * 发送到所有玩家
         */
        public static final int TTE_NOW_ALL_PLAYER = 1;

        /**
         * 时间段内发送到在线玩家
         */
        public static final int TTE_RANGE_ONLINE_PLAYER = 2;

        /**
         * 发送到指定列表的玩家
         */
        public static final int TTE_NOW_TARGET_PLAYER = 3;
    }
}
