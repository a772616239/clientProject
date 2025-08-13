package server.handler.mail;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mailbox.dbCache.mailboxCache;
import model.mailbox.entity.mailboxEntity;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.MailLog;
import platform.logs.entity.MailLog.MailOperationType;
import protocol.Common.EnumFunction;
import protocol.Mail.CS_DeleteMail;
import protocol.Mail.MailStatusEnum;
import protocol.Mail.SC_DeleteMail;
import protocol.MailDB.DB_MailBox;
import protocol.MailDB.DB_MailInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_DeleteMail_VALUE)
public class DeleteMailHandler extends AbstractBaseHandler<CS_DeleteMail> {
    @Override
    protected CS_DeleteMail parse(byte[] bytes) throws Exception {
        return CS_DeleteMail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_DeleteMail req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        LogUtil.info("playerIdx" + playerIdx + " CS_DeleteMail, channel address" + gsChn.toString());

        SC_DeleteMail.Builder resultBuilder = SC_DeleteMail.newBuilder();
        String mailIdx = req.getMailIdx();
        if (mailIdx == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_DeleteMail_VALUE, resultBuilder);
            return;
        }

        mailboxEntity playerMail = mailboxCache.getInstance().getMailBoxByPlayerIdx(playerIdx);
        if (playerMail == null) {
            LogUtil.error("playerIdx[" + playerIdx + "] mailEntity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DeleteMail_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(playerMail, entity -> {
            DB_MailBox.Builder mailBoxBuilder = playerMail.getDBBuilder();
            if (mailBoxBuilder == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_DeleteMail_VALUE, resultBuilder);
                return;
            }

            DB_MailInfo dbMail = mailBoxBuilder.getMailsMap().get(mailIdx);
            if (dbMail == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Mail_UnExist));
                gsChn.send(MsgIdEnum.SC_DeleteMail_VALUE, resultBuilder);
                return;
            }

            MailStatusEnum mailStatus = dbMail.getMailStatus();
            if (mailStatus == MailStatusEnum.MSE_AttachmentReadUnclaimed) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Mail_AttachmentNoClaim));
                gsChn.send(MsgIdEnum.SC_DeleteMail_VALUE, resultBuilder);
                return;
            }

            if (mailStatus == MailStatusEnum.MSE_NoAttachmentUnread
                    || mailStatus == MailStatusEnum.MSE_AttachmentUnRead) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Mail_UnRead));
                gsChn.send(MsgIdEnum.SC_DeleteMail_VALUE, resultBuilder);
                return;
            }

            //统计：邮件
            LogService.getInstance().submit(new MailLog(playerIdx, mailBoxBuilder.getMailsMap().get(mailIdx),
                    ReasonManager.getInstance().borrowReason(MailOperationType.MOT_DELETE_BY_PLAYER)));

            mailBoxBuilder.removeMails(mailIdx);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_DeleteMail_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Mail;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_DeleteMail_VALUE, SC_DeleteMail.newBuilder().setRetCode(retCode));
    }
}
