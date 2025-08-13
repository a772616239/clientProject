/**
 * created by tool DAOGenerate
 */
package model.mailbox.entity;

import cfg.GameConfig;
import common.GameConst;
import common.GlobalData;
import common.IdGenerator;
import common.tick.GlobalTick;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.mailbox.dbCache.mailboxCache;
import model.mailbox.util.MailUtil;
import model.obj.BaseObj;
import model.player.util.PlayerUtil;
import model.reward.RewardUtil;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.CancelMailLog;
import platform.logs.entity.MailLog;
import platform.logs.entity.MailLog.MailOperationType;
import protocol.Common.Reward;
import protocol.Common.RewardTypeEnum;
import protocol.Mail;
import protocol.Mail.MailInfo;
import protocol.Mail.MailStatusEnum;
import protocol.Mail.SC_AddNewMail;
import protocol.MailDB.DB_MailBox;
import protocol.MailDB.DB_MailBox.Builder;
import protocol.MailDB.DB_MailInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.LogUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class mailboxEntity extends BaseObj {

    public String getClassType() {
        return "mailboxEntity";
    }

    /**
     *
     */
    private String idx;

    /**
     *
     */
    private String linkplayeridx;

    /**
     *
     */
    private byte[] mailbox;


    @Override
    public void putToCache() {
        mailboxCache.put(this);
    }

    /**
     * 获得
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得
     */
    public String getLinkplayeridx() {
        return linkplayeridx;
    }

    /**
     * 设置
     */
    public void setLinkplayeridx(String linkplayeridx) {
        this.linkplayeridx = linkplayeridx;
    }

    /**
     * 获得
     */
    public byte[] getMailbox() {
        return mailbox;
    }

    /**
     * 设置
     */
    public void setMailbox(byte[] mailbox) {
        this.mailbox = mailbox;
    }


    public String getBaseIdx() {
        // TODO Auto-generated method stub
        return idx;
    }

    /**
     * ================================================================
     */

    private DB_MailBox.Builder db_data;

    private mailboxEntity() {
    }

    public DB_MailBox.Builder getDBBuilder() {
        if (db_data == null) {
            try {
                if (this.mailbox != null) {
                    db_data = DB_MailBox.parseFrom(this.mailbox).toBuilder();
                } else {
                    db_data = DB_MailBox.newBuilder();
                }
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
                return null;
            }
        }
        return db_data;
    }

    @Override
    public void transformDBData() {
        this.mailbox = getDBBuilder().build().toByteArray();
    }

    public mailboxEntity(String playerIdx) {
        this.idx = IdGenerator.getInstance().generateId();
        this.linkplayeridx = playerIdx;
    }

    public int getMailCount() {
        DB_MailBox.Builder dbMailBox = getDBBuilder();
        if (dbMailBox != null) {
            return dbMailBox.getMailsCount();
        }
        return 0;
    }

    public void addMail(DB_MailInfo mail, Reason reason) {
        if (mail == null) {
            return;
        }

        DB_MailBox.Builder builder = getDBBuilder();
        if (builder != null) {
            //如果容量大于配置的值，删除最旧的邮件
            if (getMailCount() >= GameConfig.getById(GameConst.CONFIG_ID).getMaxmailboxcapacity()) {
                DB_MailInfo deleteMail = null;
                long minCreateTime = GlobalTick.getInstance().getCurrentTime();
                for (DB_MailInfo dbMail : builder.getMailsMap().values()) {
                    if (dbMail.getCreateTime() < minCreateTime) {
                        minCreateTime = dbMail.getCreateTime();
                        deleteMail = dbMail;
                    }
                }

                if (deleteMail != null) {
                    builder.removeMails(deleteMail.getMailIdx());
                    LogService.getInstance().submit(new MailLog(getLinkplayeridx(), deleteMail,
                            ReasonManager.getInstance().borrowReason(MailOperationType.MOT_DELETE_FULL)));
                    sendMailRemove(deleteMail.getMailIdx());
                }
            }

            builder.putMails(mail.getMailIdx(), mail);
            sendAddNewMailMsg(mail);
        }

        if (reason != null) {
            reason.addStr(MailOperationType.MOT_ADD_NEW);
        }
        LogService.getInstance().submit(new MailLog(getLinkplayeridx(), mail, reason));
    }

    private void sendMailRemove(String mailIdx) {
        if (!GlobalData.getInstance().checkPlayerOnline(getLinkplayeridx())) {
            return;
        }
        Mail.SC_MailRemove.Builder msg = Mail.SC_MailRemove.newBuilder().setMailId(mailIdx);
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_MailRemove_VALUE, msg);
    }

    public void sendAddNewMailMsg(DB_MailInfo mail) {
        if (mail == null || !GlobalData.getInstance().checkPlayerOnline(getLinkplayeridx())) {
            return;
        }

        MailInfo mailInfo = MailUtil.buildMailInfo(mail, PlayerUtil.queryPlayerLanguage(getLinkplayeridx()));
        if (mailInfo != null && GlobalData.getInstance().checkPlayerOnline(getLinkplayeridx())) {
            SC_AddNewMail.Builder newMail = SC_AddNewMail.newBuilder();
            newMail.addMailinfo(mailInfo);
            GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_AddNewMail_VALUE, newMail);
        }
    }

//    /**
//     * 检查过期邮件
//     */
//    @Deprecated
//    public void checkMail() {
//        Builder db_data = getDBBuilder();
//
//        //检查过期邮件
//        List<String> removeKey = new ArrayList<>();
//        long curTime = System.currentTimeMillis();
//        for (Entry<String, DB_Mail> entry : db_data.getMailsMap().entrySet()) {
//            DB_Mail value = entry.getValue();
//            if (value == null || entry.getValue().getExpireTime() < curTime) {
//                removeKey.add(entry.getKey());
//            }
//        }
//
//        if (!removeKey.isEmpty()) {
//            for (String idx : removeKey) {
//                LogUtil.info("delete playerIdx[" + getLinkplayeridx() + "] mail id [" + idx + "], expire");
//                db_data.removeMails(idx);
//            }
//        }
//    }

    public DB_MailInfo getMail(String mailIdx) {
        if (mailIdx == null) {
            return null;
        }
        Builder dbBuilder = getDBBuilder();
        if (dbBuilder == null) {
            return null;
        }
        return dbBuilder.getMailsMap().get(mailIdx);
    }

    public boolean putMail(DB_MailInfo mailInfo) {
        if (mailInfo == null) {
            return false;
        }
        Builder dbBuilder = getDBBuilder();
        if (dbBuilder == null) {
            return false;
        }
        dbBuilder.putMails(mailInfo.getMailIdx(), mailInfo);
        return true;
    }

    /**
     * @param mailIdx
     * @param canDoReward 拿到该封邮件可以发放的奖励
     * @return
     */
    public RetCodeEnum getCanDoRewardByIdx(String mailIdx, List<Reward> canDoReward) {
        return getCanDoRewardByMail(getMail(mailIdx), canDoReward);
    }

    /**
     * @param dbMail
     * @param canDoReward 拿到该封邮件可以发放的奖励
     * @return
     */
    public RetCodeEnum getCanDoRewardByMail(DB_MailInfo dbMail, List<Reward> canDoReward) {
        if (dbMail == null || canDoReward == null) {
            return RetCodeEnum.RCE_Mail_UnExist;
        }

        //邮件没有附件
        if (dbMail.getRewardsCount() <= 0) {
            return RetCodeEnum.RCE_Mail_NoAttachment;
        }

        //附件已经被领取
        if (dbMail.getMailStatus() == MailStatusEnum.MSE_AttachmentReadClaimed) {
            return RetCodeEnum.RCE_Mail_AlreadyCliamed;
        }

        List<Reward> remainReward = new ArrayList<>();
        canDoReward.addAll(RewardUtil.getCanDoReward(getLinkplayeridx(), dbMail.getRewardsList(), remainReward, null));
        if (canDoReward.isEmpty()) {
            return fullBag(remainReward);
        }

        DB_MailInfo.Builder mailBuilder = dbMail.toBuilder();
        mailBuilder.clearRewards();
        if (remainReward.isEmpty()) {
            mailBuilder.setMailStatus(MailStatusEnum.MSE_NoAttachmentRead);
        } else {
            mailBuilder.addAllRewards(remainReward);
            mailBuilder.setMailStatus(MailStatusEnum.MSE_AttachmentReadUnclaimed);
        }

        putMail(mailBuilder.build());
        return RetCodeEnum.RCE_Success;
    }

    /**
     * 此方法只作为检查那个背包满,请不要在邮件系统之外调用
     *
     * @param rewards
     * @return
     */
    public RetCodeEnum fullBag(List<Reward> rewards) {
        if (rewards != null) {
            for (Reward reward : rewards) {
                if (reward.getRewardType() == RewardTypeEnum.RTE_Item) {
                    return RetCodeEnum.RCE_Mail_ItemBagIsFull;
                } else if (reward.getRewardType() == RewardTypeEnum.RTE_Pet) {
                    return RetCodeEnum.RCE_Mail_PetBagIsFull;
                } else if (reward.getRewardType() == RewardTypeEnum.RTE_Rune) {
                    return RetCodeEnum.RCE_Mail_RuneBagIsFull;
                } else if (reward.getRewardType() == RewardTypeEnum.RTE_Gem) {
                    return RetCodeEnum.RCE_Mail_GemBagIsFull;
                }
            }
        }

        return RetCodeEnum.RCE_Success;
    }

    /**
     * 通过模板id删除玩家邮件
     *
     * @param template
     */
    public void deleteMailByTemplateId(long template) {
        Map<String, DB_MailInfo> mailMap = getDBBuilder().getMailsMap();
        for (DB_MailInfo value : mailMap.values()) {
            if (value.getMailTemplateId() == template) {
                Builder remove = getDBBuilder().removeMails(value.getMailIdx());
                if (remove != null) {
                    LogService.getInstance().submit(new CancelMailLog(getLinkplayeridx(), value));
                }
                break;
            }
        }
    }
}