package model.mailbox.util;

import cfg.MailTemplateConfig;
import cfg.MailTemplateConfigObject;
import cfg.ServerStringRes;
import common.IdGenerator;
import common.tick.GlobalTick;
import java.util.List;
import model.reward.RewardUtil;
import platform.PlatformManager;
import protocol.Common.LanguageEnum;
import protocol.Common.Reward;
import protocol.Mail.MailInfo;
import protocol.MailDB.DB_MailInfo;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

public class MailUtil {

    public final static long defaultValidTime = TimeUtil.MS_IN_A_DAY * 30;
    public final static int defaultSenderId = 10;

    /**
     * 构建一封新邮件
     * @param templateId
     * @param rewards     传入附件,如果传入的附件为空,则以模板附件为准,想要附件为空,传入空List
     * @param params
     * @return
     */
    public static DB_MailInfo.Builder fillDBMailByTemplateId(int templateId, List<Reward> rewards, String... params){
        MailTemplateConfigObject mailTemplate = MailTemplateConfig.getByTemplateid(templateId);
        if(mailTemplate == null) {
            LogUtil.error("MailTemplateId not found:id=" + templateId);
            return null;
        }
        DB_MailInfo.Builder mail = DB_MailInfo.newBuilder();
        mail.setMailIdx(IdGenerator.getInstance().generateId());
        mail.setMailTemplateId(templateId);
        if (params != null) {
            mail.addAllParam(GameUtil.parseArrayToList(params));
        }
        if (rewards != null) {
            mail.addAllRewards(rewards);
        } else {
            List<Reward> rewardList = RewardUtil.parseRewardIntArrayToRewardList(mailTemplate.getAttachment());
            if (rewardList != null && !rewardList.isEmpty()) {
                mail.addAllRewards(rewardList);
            }
        }

        long nowTime = GlobalTick.getInstance().getCurrentTime();
        mail.setCreateTime(nowTime);
        mail.setExpireTime(nowTime + (mailTemplate.getExpiretime() * TimeUtil.MS_IN_A_DAY));
        return mail;
    }

    /**
     * 将DB_MailInfo转化为MailInfo
     * @param mail
     * @return
     */
    public static MailInfo buildMailInfo(DB_MailInfo mail, LanguageEnum languageEnum) {
        if (mail == null) {
            return null;
        }
        MailInfo.Builder mailBuilder = MailInfo.newBuilder();
        mailBuilder.setMailIdx(mail.getMailIdx());
        mailBuilder.setSender(getSender(mail.getMailTemplateId(), languageEnum));
        mailBuilder.setMailTitleStr(getTitle(mail.getMailTemplateId(), languageEnum));
        mailBuilder.setMailBodyStr(getBody(mail.getMailTemplateId(), languageEnum, mail.getParamList().toArray()));
        mailBuilder.addAllRewards(mail.getRewardsList());
        mailBuilder.setMailStatus(mail.getMailStatus());
        mailBuilder.setCreateTime(mail.getCreateTime());
        mailBuilder.setExpireTime(mail.getExpireTime());
        return mailBuilder.build();
    }

    /**
     * 先检查配置表模板，后检查配置表模板
     * @param templateId
     * @return
     */
    public static String getSender(long templateId, LanguageEnum language) {
        if (templateId < Integer.MAX_VALUE) {
            MailTemplateConfigObject template = MailTemplateConfig.getByTemplateid((int)templateId);
            if (template != null) {
                return ServerStringRes.getContentByLanguage(template.getSender(), language);
            }
        } else {
            return PlatformManager.getInstance().getSenderByTemplateId(templateId, language);
        }

        return ServerStringRes.getContentByLanguage(defaultSenderId, language);
    }

    public static String getTitle(long templateId, LanguageEnum language) {
        if (templateId < Integer.MAX_VALUE) {
            MailTemplateConfigObject template = MailTemplateConfig.getByTemplateid((int)templateId);
            if (template != null) {
                return ServerStringRes.getContentByLanguage(template.getTitle_tipids(), language);
            }
        } else {
            return PlatformManager.getInstance().getTitleByTemplateId(templateId, language);
        }
        return "";
    }

    public static String getBody(long templateId, LanguageEnum language, Object... params) {
        if (templateId < Integer.MAX_VALUE) {
            MailTemplateConfigObject template = MailTemplateConfig.getByTemplateid((int)templateId);
            if (template != null) {
                return ServerStringRes.getContentByLanguage(template.getBody_tipsid(), language, params);
            }
        } else {
            return PlatformManager.getInstance().getBodyByTemplateId(templateId, language, params);
        }
        return "";
    }
}
