package server.handler.mail;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.mailbox.dbCache.mailboxCache;
import model.mailbox.entity.mailboxEntity;
import model.mailbox.util.MailUtil;
import model.reward.RewardManager;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.LanguageEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Mail.CS_ClaimAllAttachmentMail;
import protocol.Mail.ChangeMailStatus;
import protocol.Mail.SC_ClaimAllAttachmentMail;
import protocol.MailDB.DB_MailBox;
import protocol.MailDB.DB_MailInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimAllAttachmentMail_VALUE)
public class ClaimAllAttachmentMailHandler extends AbstractBaseHandler<CS_ClaimAllAttachmentMail> {
    @Override
    protected CS_ClaimAllAttachmentMail parse(byte[] bytes) throws Exception {
        return CS_ClaimAllAttachmentMail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimAllAttachmentMail req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimAllAttachmentMail.Builder resultBuilder = SC_ClaimAllAttachmentMail.newBuilder();
        mailboxEntity playerMail = mailboxCache.getInstance().getMailBoxByPlayerIdx(playerIdx);
        if (playerMail == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimAllAttachmentMail_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(playerMail, entity -> {
            DB_MailBox.Builder boxBuilder = playerMail.getDBBuilder();
            if (boxBuilder == null) {
                LogUtil.error("playerIdx[" + playerIdx + "] mailBox is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ClaimAllAttachmentMail_VALUE, resultBuilder);
                return;
            }

            //所有的奖励
            List<Reward> allRewardList = new ArrayList<>();

            RetCodeEnum noticeRetCode = null;

            Map<String, DB_MailInfo> mailsMap = boxBuilder.getMailsMap();
            for (DB_MailInfo value : mailsMap.values()) {
                List<Reward> canGet = new ArrayList<>();
                RetCodeEnum canDoRewardByMail = playerMail.getCanDoRewardByMail(value, canGet);

                DB_MailInfo mail = playerMail.getMail(value.getMailIdx());
                if (mail == null) {
                    continue;
                }
                if (canDoRewardByMail == RetCodeEnum.RCE_Success) {
                    ChangeMailStatus.Builder builder = ChangeMailStatus.newBuilder();
                    builder.setMailIdx(mail.getMailIdx());
                    builder.setMailStatus(mail.getMailStatus());
                    if (mail.getRewardsCount() > 0) {
                        builder.addAllRemainReward(mail.getRewardsList());
                    }
                    resultBuilder.addChangeStatus(builder);

                    RewardManager.getInstance().doRewardByList(playerIdx, canGet,
                            ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Mail,
                                    MailUtil.getTitle(value.getMailTemplateId(), LanguageEnum.LE_SimpleChinese)), false);

                    allRewardList.addAll(canGet);
                } else if (noticeRetCode == null) {
                    RetCodeEnum fullRet = playerMail.fullBag(mail.getRewardsList());
                    if (fullRet != RetCodeEnum.RCE_Success) {
                        noticeRetCode = fullRet;
                    }
                }
            }

            //正常返回
            GlobalData.getInstance().sendDisRewardMsg(playerIdx, allRewardList, RewardSourceEnum.RSE_Mail);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimAllAttachmentMail_VALUE, resultBuilder);

            //背包容量不足提示
            if (noticeRetCode != null) {
                GlobalData.getInstance().sendRetCodeMsg(playerIdx, noticeRetCode);
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
        gsChn.send(MsgIdEnum.SC_ClaimAllAttachmentMail_VALUE, SC_ClaimAllAttachmentMail.newBuilder().setRetCode(retCode));
    }
}
