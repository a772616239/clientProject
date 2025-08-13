package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.mailbox.util.MailUtil;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.RewardLog;
import platform.logs.ReasonManager.Reason;
import platform.logs.StatisticsLogUtil;
import protocol.Common.LanguageEnum;
import protocol.MailDB.DB_MailInfo;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MailLog extends AbstractPlayerLog {
    private String mailId;
    private long mailTemplateId;
    private String title;
    private List<RewardLog> rewards;
    private String reason;

    public MailLog(String playerIdx, DB_MailInfo mail, Reason reason) {
        super(playerIdx);
        if (mail != null) {
            this.mailId = mail.getMailIdx();
            this.mailTemplateId = mail.getMailTemplateId();
            this.rewards = StatisticsLogUtil.buildRewardLogList(mail.getRewardsList());
            this.title = MailUtil.getTitle(mailTemplateId, LanguageEnum.LE_SimpleChinese);
        }

        this.reason = reason == null ? "" : reason.toString();
    }

    public static class MailOperationType {
        public static final String MOT_ADD_NEW = "新邮件";
        public static final String MOT_CLAIM_ANNEX = "领取附件";
        public static final String MOT_DELETE_BY_PLAYER = "玩家删除";
        public static final String MOT_DELETE_FULL = "邮箱满删除";
        public static final String MOT_DELETE_EXPIRE = "过期删除";
    }
}
