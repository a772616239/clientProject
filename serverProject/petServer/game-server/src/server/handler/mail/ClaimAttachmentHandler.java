package server.handler.mail;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import model.mailbox.dbCache.mailboxCache;
import model.mailbox.entity.mailboxEntity;
import model.mailbox.util.MailUtil;
import model.reward.RewardManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.MailLog;
import platform.logs.entity.MailLog.MailOperationType;
import protocol.Common.EnumFunction;
import protocol.Common.LanguageEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Mail.CS_ClaimAttachment;
import protocol.Mail.ChangeMailStatus;
import protocol.Mail.ChangeMailStatus.Builder;
import protocol.Mail.SC_ClaimAttachment;
import protocol.MailDB.DB_MailInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimAttachment_VALUE)
public class ClaimAttachmentHandler extends AbstractBaseHandler<CS_ClaimAttachment> {
    @Override
    protected CS_ClaimAttachment parse(byte[] bytes) throws Exception {
        return CS_ClaimAttachment.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimAttachment req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        String mailIdx = req.getMailIdx();

        mailboxEntity playerMail = mailboxCache.getInstance().getMailBoxByPlayerIdx(playerIdx);
        SC_ClaimAttachment.Builder resultBuilder = SC_ClaimAttachment.newBuilder();
        if (mailIdx == null) {
            LogUtil.error("playerIdx[" + playerIdx + "], mailBox entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimAttachment_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(playerMail, entity -> {
            List<Reward> canGet = new ArrayList<>();
            RetCodeEnum retCodeEnum = playerMail.getCanDoRewardByIdx(mailIdx, canGet);
            if (retCodeEnum != RetCodeEnum.RCE_Success) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
                gsChn.send(MsgIdEnum.SC_ClaimAttachment_VALUE, resultBuilder);
                return;
            }

            DB_MailInfo mail = playerMail.getMail(mailIdx);

            //发放奖励
            RewardManager.getInstance().doRewardByList(playerIdx, canGet,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Mail,
                            MailUtil.getTitle(mail.getMailTemplateId(), LanguageEnum.LE_SimpleChinese)), true);

            //正常返回
            Builder builder = ChangeMailStatus.newBuilder();
            builder.setMailIdx(mail.getMailIdx());
            builder.setMailStatus(mail.getMailStatus());
            builder.addAllRemainReward(mail.getRewardsList());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setChangeState(builder);
            gsChn.send(MsgIdEnum.SC_ClaimAttachment_VALUE, resultBuilder);

            LogService.getInstance().submit(new MailLog(playerIdx, mail,
                    ReasonManager.getInstance().borrowReason(MailOperationType.MOT_CLAIM_ANNEX)));

            //返回背包容量提示
            RetCodeEnum needNotice = playerMail.fullBag(mail.getRewardsList());
            if (needNotice != RetCodeEnum.RCE_Success) {
                GlobalData.getInstance().sendRetCodeMsg(playerIdx, needNotice);
            }
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Mail;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimAttachment_VALUE, SC_ClaimAttachment.newBuilder().setRetCode(retCode));
    }
}
