package server.handler.mail;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import model.mailbox.dbCache.mailboxCache;
import model.mailbox.entity.mailboxEntity;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.MailLog;
import platform.logs.entity.MailLog.MailOperationType;
import protocol.Common.EnumFunction;
import protocol.Mail.CS_DeleteAllReadMail;
import protocol.Mail.MailStatusEnum;
import protocol.Mail.SC_DeleteAllReadMail;
import protocol.MailDB.DB_MailBox;
import protocol.MailDB.DB_MailInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_DeleteAllReadMail_VALUE)
public class DeleteAllReadMailHandler extends AbstractBaseHandler<CS_DeleteAllReadMail> {
    @Override
    protected CS_DeleteAllReadMail parse(byte[] bytes) throws Exception {
        return CS_DeleteAllReadMail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_DeleteAllReadMail req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_DeleteAllReadMail.Builder resultBuilder = SC_DeleteAllReadMail.newBuilder();
        mailboxEntity playerMail = mailboxCache.getInstance().getMailBoxByPlayerIdx(playerIdx);
        if (playerMail == null) {
            LogUtil.error("playerIdx [" + playerIdx + "] mail entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DeleteAllReadMail_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(playerMail, entity -> {
            DB_MailBox.Builder mailBoxBuilder = playerMail.getDBBuilder();
            if (mailBoxBuilder == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_DeleteAllReadMail_VALUE, resultBuilder);
                return;
            }

            Collection<DB_MailInfo> mailCollection = mailBoxBuilder.getMailsMap().values();
            List<String> deleteList = new ArrayList<>();
            for (DB_MailInfo mail : mailCollection) {
                if (mail.getMailStatus() == MailStatusEnum.MSE_AttachmentReadClaimed
                        || mail.getMailStatus() == MailStatusEnum.MSE_NoAttachmentRead) {
                    deleteList.add(mail.getMailIdx());
                }
            }

            if (!deleteList.isEmpty()) {
                for (String mailIdx : deleteList) {
                    LogService.getInstance().submit(new MailLog(playerIdx, mailBoxBuilder.getMailsMap().get(mailIdx),
                            ReasonManager.getInstance().borrowReason(MailOperationType.MOT_DELETE_BY_PLAYER)));
                    mailBoxBuilder.removeMails(mailIdx);
                }
            }

            resultBuilder.addAllSuccessDelete(deleteList);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_DeleteAllReadMail_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Mail;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_DeleteAllReadMail_VALUE, SC_DeleteAllReadMail.newBuilder().setRetCode(retCode));
    }
}
