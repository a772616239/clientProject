package server.handler.mail;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mailbox.dbCache.mailboxCache;
import model.mailbox.entity.mailboxEntity;
import model.mailbox.util.MailUtil;
import model.player.util.PlayerUtil;
import protocol.Common.EnumFunction;
import protocol.Common.LanguageEnum;
import protocol.Mail.CS_ClaimMailBoxInfo;
import protocol.Mail.SC_ClaimMailBoxInfo;
import protocol.MailDB.DB_MailBox.Builder;
import protocol.MailDB.DB_MailInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimMailBoxInfo_VALUE)
public class ClaimMailBoxInfoHandler extends AbstractBaseHandler<CS_ClaimMailBoxInfo> {
    @Override
    protected CS_ClaimMailBoxInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimMailBoxInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimMailBoxInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        mailboxEntity playerMail = mailboxCache.getInstance().getMailBoxByPlayerIdx(playerIdx);
        SC_ClaimMailBoxInfo.Builder resultBuilder = SC_ClaimMailBoxInfo.newBuilder();
        if (playerMail == null) {
            LogUtil.error("playerIdx[" + playerIdx + "] mailbox is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimMailBoxInfo_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(playerMail, entity -> {
            //取消过期检查
//            playerMail.checkMail();
            Builder dbBuilder = playerMail.getDBBuilder();
            if (dbBuilder == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ClaimMailBoxInfo_VALUE, resultBuilder);
                return;
            }
            LanguageEnum languageEnum = PlayerUtil.queryPlayerLanguage(playerMail.getLinkplayeridx());
            for (DB_MailInfo value : dbBuilder.getMailsMap().values()) {
                resultBuilder.addMails(MailUtil.buildMailInfo(value, languageEnum));
            }
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimMailBoxInfo_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Mail;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimMailBoxInfo_VALUE, SC_ClaimMailBoxInfo.newBuilder().setRetCode(retCode));
    }
}
