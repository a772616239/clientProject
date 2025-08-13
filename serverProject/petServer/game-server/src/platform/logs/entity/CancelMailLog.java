package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.StatisticsLogUtil;
import protocol.MailDB.DB_MailInfo;

/**
 * @author huhan
 * @date 2020/07/03
 */
@Getter
@Setter
@NoArgsConstructor
public class CancelMailLog extends AbstractPlayerLog {
    private String mailIdx;
    private long mailTemplateId;
    private String curMailStatus;

    public CancelMailLog(String playerIdx, DB_MailInfo mailInfo) {
        super(playerIdx);
        if (mailInfo == null) {
            return;
        }

        this.mailIdx = mailInfo.getMailIdx();
        this.mailTemplateId = mailInfo.getMailTemplateId();
        this.curMailStatus = StatisticsLogUtil.getMailStateName(mailInfo.getMailStatus());
    }
}
