package petrobot.system.mail;

import petrobot.robot.anotation.Controller;
import petrobot.robot.PlayerData;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Mail.*;
import protocol.Mail.CS_ReadMail.Builder;
import protocol.MessageId.MsgIdEnum;

import java.util.Map;

@Controller
public class MailManager {

    @Index(value = IndexConst.CLAIM_MAIL_BOX)
    public void claimMailBox(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimMailBoxInfo_VALUE, CS_ClaimMailBoxInfo.newBuilder());
    }

    @Index(value = IndexConst.READ_MAIL)
    public void readMail(Robot robot) {
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, r -> {
            PlayerData data = robot.getData();
            Map<String, MailInfo> mailInfoMap = data.getMailInfoMap();
            if (mailInfoMap.isEmpty()) {
                r.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
                return;
            }

            for (MailInfo value : mailInfoMap.values()) {
                Builder builder = CS_ReadMail.newBuilder();
                builder.setMailIdx(value.getMailIdx());
                robot.getClient().send(MsgIdEnum.CS_ReadMail_VALUE, builder);
                break;
            }
        });
    }

    @Index(value = IndexConst.CLAIM_ATTACHMENT)
    public void claimAttachment(Robot robot) {
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, r -> {
            PlayerData data = robot.getData();
            Map<String, MailInfo> mailInfoMap = data.getMailInfoMap();
            if (mailInfoMap.isEmpty()) {
                r.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
                return;
            }

            for (MailInfo value : mailInfoMap.values()) {
                CS_ClaimAttachment.Builder builder = CS_ClaimAttachment.newBuilder();
                builder.setMailIdx(value.getMailIdx());
                robot.getClient().send(MsgIdEnum.CS_ClaimAttachment_VALUE, builder);
                break;
            }
        });
    }

    @Index(value = IndexConst.CLAIM_ALL_ATTACHMENT)
    public void claimAllAttachment(Robot robot) {
        if (robot == null) {
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_ClaimAllAttachmentMail_VALUE, CS_ClaimAllAttachmentMail.newBuilder());
    }

    @Index(value = IndexConst.DELETE_MAIL)
    public void deleteMail(Robot robot) {
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, r -> {
            PlayerData data = robot.getData();
            Map<String, MailInfo> mailInfoMap = data.getMailInfoMap();
            if (mailInfoMap.isEmpty()) {
                r.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
                return;
            }

            for (MailInfo value : mailInfoMap.values()) {
                CS_DeleteMail.Builder builder = CS_DeleteMail.newBuilder();
                builder.setMailIdx(value.getMailIdx());
                robot.getClient().send(MsgIdEnum.CS_DeleteMail_VALUE, builder);
                break;
            }
        });
    }

    @Index(value = IndexConst.DELETE_ALL_MAIL)
    public void deleteAllMail(Robot robot) {
        if (robot == null) {
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_DeleteAllReadMail_VALUE, CS_DeleteAllReadMail.newBuilder());
    }
}
