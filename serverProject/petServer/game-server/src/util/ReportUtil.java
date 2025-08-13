package util;


import cfg.MailTemplateConfig;
import cfg.MailTemplateUsed;
import cfg.ReportConfig;
import common.GameConst;
import common.tick.GlobalTick;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Comment.CommentTypeEnum;
import protocol.Comment.EnumReportType;
import protocol.CommentDB.DB_Reporter;
import protocol.Common.RewardSourceEnum;

/**
 * @author huhan
 * @date 2020/07/15
 */
public class ReportUtil {

    /**
     * 判断玩家是达到举报上限
     * @return
     */
    public static boolean canReport(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return false;
        }

        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return false;
        }

        return entity.getDb_data().getTodayReportTimes() < ReportConfig.getById(GameConst.CONFIG_ID).getEachdaycanreporttimes();
    }

    public static DB_Reporter createReporter(String playerIdx, EnumReportType reportType, String reportMsg) {
        DB_Reporter.Builder newReport = DB_Reporter.newBuilder();
        newReport.setPlayerIdx(playerIdx);
        newReport.setReportType(reportType);
        if (newReport.getReportType() == EnumReportType.ERT_Others && StringUtils.isNotBlank(reportMsg)) {
            newReport.setReportMsg(reportMsg);
        }
        newReport.setReportTime(GlobalTick.getInstance().getCurrentTime());
        return newReport.build();
    }

    public static boolean needBan(Collection<DB_Reporter> reporters) {
        if (CollectionUtils.isEmpty(reporters)) {
            return false;
        }
        int sumAutoDeal = 0;
        for (DB_Reporter reporter : reporters) {
            if (isAutoDealReportType(reporter.getReportType())) {
                sumAutoDeal ++;
            }
        }
        return sumAutoDeal >= GameConst.REPORT_AUTO_DEAL_NEED_COUNT;
    }

    public static boolean isAutoDealReportType(EnumReportType reportType) {
        if (reportType == null) {
            return false;
        }
        return ArrayUtil.intArrayContain(ReportConfig.getById(GameConst.CONFIG_ID).getAutodealtype(), reportType.getNumber());
    }

    public static Set<Integer> getAutoDealTypeSet() {
        int[] ints = ReportConfig.getById(GameConst.CONFIG_ID).getAutodealtype();
        Set<Integer> result  = new HashSet<>();
        for (int anInt : ints) {
            result.add(anInt);
        }
        return result;
    }

    private final static Map<EnumReportType, String> REPORT_TYPE_NAME_MAP = new EnumMap<>(EnumReportType.class);
    static {
        REPORT_TYPE_NAME_MAP.put(EnumReportType.ERT_Advertising, "广告");
        REPORT_TYPE_NAME_MAP.put(EnumReportType.ERT_IllegalSpeech, "非法言论");
        REPORT_TYPE_NAME_MAP.put(EnumReportType.ERT_PersonalAttacks, "人身攻击");
        REPORT_TYPE_NAME_MAP.put(EnumReportType.ERT_Others, "其他");
    }

    public static String getReportName(EnumReportType type) {
        if(type == null || type == EnumReportType.ERT_NULL) {
            return "";
        }
        String name = REPORT_TYPE_NAME_MAP.get(type);
        if(StringUtils.isBlank(name)) {
            LogUtil.error("ReportUtil.getReportName, type name is not exist, type:" + type);
        }
        return name;
    }

    public static String getAutoDealTypeName(Collection<DB_Reporter> reporters) {
        if (CollectionUtils.isEmpty(reporters)) {
            return "";
        }

        StringBuilder strBuilder = new StringBuilder();
        Set<Integer> set = getAutoDealTypeSet();
        for (DB_Reporter reporter : reporters) {
            if (set.contains(reporter.getReportTypeValue())) {
                strBuilder.append(getReportName(reporter.getReportType()));
                set.remove(reporter.getReportTypeValue());
            }

            if (set.isEmpty()) {
                break;
            }
        }
        return strBuilder.toString();
    }

    private final static Map<CommentTypeEnum, String> COMMENT_TYPE_NAME_MAP = new EnumMap<>(CommentTypeEnum.class);
    static {
        COMMENT_TYPE_NAME_MAP.put(CommentTypeEnum.CTE_Pet, "魔灵");
        COMMENT_TYPE_NAME_MAP.put(CommentTypeEnum.CTE_MainLine, "主线评论");
        COMMENT_TYPE_NAME_MAP.put(CommentTypeEnum.CTE_EndlessSpire, "无尽尖塔");
        COMMENT_TYPE_NAME_MAP.put(CommentTypeEnum.CTE_BraveChallenge, "勇气试炼");
        COMMENT_TYPE_NAME_MAP.put(CommentTypeEnum.CTE_BossTower, "boss塔");
    }

    public static final String CHAT = "聊天";

    public static String getCommentTypeName(int type) {
        return getCommentTypeName(CommentTypeEnum.forNumber(type));
    }

    public static String getCommentTypeName(CommentTypeEnum type) {
        if(type == null || type == CommentTypeEnum.CTE_Null) {
            return "";
        }
        String name = COMMENT_TYPE_NAME_MAP.get(type);
        if(StringUtils.isBlank(name)) {
            LogUtil.error("ReportUtil.getReportName, type name is not exist, type:" + type);
        }
        return name;
    }

    /**
     * 发送举报成功邮件到被举报人
     */
    public static void sendReportSuccessToReported(Collection<String> playerIdxList) {
        int mailTemp = MailTemplateUsed.getById(GameConst.CONFIG_ID).getSuccessreported();
        sendMailToPlayerIdList(playerIdxList, mailTemp);
    }

    /**
     * 发送举报成功邮件到举报人
     */
    public static void sendReportSuccessToReporter(Collection<String> playerIdxList) {
        int mailTemp = MailTemplateUsed.getById(GameConst.CONFIG_ID).getSuccessreporter();
        sendMailToPlayerIdList(playerIdxList, mailTemp);
    }

    /**
     * 发送举报失败邮件到举报人
     */
    public static void sendReportFailedToReporter(Collection<String> playerIdxList) {
        int mailTemp = MailTemplateUsed.getById(GameConst.CONFIG_ID).getFailedreporter();
        sendMailToPlayerIdList(playerIdxList, mailTemp);
    }

    private static void sendMailToPlayerIdList(Collection<String> playerIdxList, int mailTemplate) {
        if (CollectionUtils.isEmpty(playerIdxList) || MailTemplateConfig.getByTemplateid(mailTemplate) == null) {
            return;
        }
        playerIdxList.forEach(e -> {
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Report);
            EventUtil.triggerAddMailEvent(e, mailTemplate, null, reason);
        });
    }

    public static Set<Integer> getAutoDealTypeSet(Collection<DB_Reporter> reporters) {
        if (CollectionUtils.isEmpty(reporters)) {
            return Collections.emptySet();
        }

        Set<Integer> typeSet = new HashSet<>();
        Set<Integer> set = getAutoDealTypeSet();
        for (DB_Reporter reporter : reporters) {
            if (set.contains(reporter.getReportTypeValue())) {
                typeSet.add(reporter.getReportTypeValue());
                set.remove(reporter.getReportTypeValue());
            }

            if (set.isEmpty()) {
                break;
            }
        }
        return typeSet;
    }

    /**
     * 发送举报结果反馈到玩家
     * @param reported   被举报人
     * @param reporter   举报人
     * @param success    举报成功或者失败
     */
    public static void sendReportMailToPlayer(Collection<String> reported, Collection<String> reporter, boolean success) {
        if (success) {
            sendReportSuccessToReported(reported);
            sendReportSuccessToReporter(reporter);
        } else {
            sendReportFailedToReporter(reporter);
        }
    }

}
