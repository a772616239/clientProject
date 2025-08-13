package server.handler.mail;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mailbox.dbCache.mailboxCache;
import model.mailbox.entity.mailboxEntity;
import protocol.Common.EnumFunction;
import protocol.Mail.CS_ReadMail;
import protocol.Mail.MailStatusEnum;
import protocol.Mail.SC_ReadMail;
import protocol.MailDB.DB_MailBox;
import protocol.MailDB.DB_MailInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ReadMail_VALUE)
public class ReadMailHandler extends AbstractBaseHandler<CS_ReadMail> {
    @Override
    protected CS_ReadMail parse(byte[] bytes) throws Exception {
        return CS_ReadMail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ReadMail req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        String mailIdx = req.getMailIdx();

        SC_ReadMail.Builder resultBuilder = SC_ReadMail.newBuilder();
        mailboxEntity playerMail = mailboxCache.getInstance().getMailBoxByPlayerIdx(playerIdx);
        if (playerMail == null) {
            LogUtil.error("playerIdx" + playerIdx + "] mail  entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ReadMail_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(playerMail, entity -> {
            DB_MailBox.Builder mailBoxBuilder = playerMail.getDBBuilder();
            if (mailBoxBuilder == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ReadMail_VALUE, resultBuilder);
                return;
            }

            DB_MailInfo dbMail = mailBoxBuilder.getMailsMap().get(mailIdx);
            if (dbMail == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Mail_UnExist));
                gsChn.send(MsgIdEnum.SC_ReadMail_VALUE, resultBuilder);
                return;
            }

            DB_MailInfo.Builder mailBuilder = dbMail.toBuilder();
            if (mailBuilder.getRewardsCount() <= 0) {
                mailBuilder.setMailStatus(MailStatusEnum.MSE_NoAttachmentRead);
            } else {
                mailBuilder.setMailStatus(MailStatusEnum.MSE_AttachmentReadUnclaimed);
            }
            mailBoxBuilder.putMails(mailBuilder.getMailIdx(), mailBuilder.build());

            resultBuilder.setNewStatus(mailBuilder.getMailStatus());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ReadMail_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Mail;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ReadMail_VALUE, SC_ReadMail.newBuilder().setRetCode(retCode));
    }
}
