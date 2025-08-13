/**
 * created by tool DAOGenerate
 */
package model.magicthron.entity;

import cfg.MailTemplateUsed;
import cfg.ShuraArenaBossDamageReward;
import cfg.ShuraArenaBossDamageRewardObject;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GlobalData;
import entity.UpdateDailyData;
import model.magicthron.MagicThronManager;
import model.magicthron.dbcache.magicthronCache;
import model.obj.BaseObj;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.Reward;
import protocol.MagicThron.SC_MagicAreaInfoUpdate;
import protocol.MagicThronDB.DB_MagicThron;
import util.EventUtil;
import util.LogUtil;
import util.MapUtil;

import java.util.Collections;
import java.util.List;

import static protocol.MessageId.MsgIdEnum.SC_MagicAreaInfoUpdate_VALUE;


/**
 * created by tool
 */
@SuppressWarnings("serial")
public class magicthronEntity extends BaseObj implements UpdateDailyData {

    public String getClassType() {
        return "magicthronEntity";
    }

    private String idx;

    private byte[] info;

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public byte[] getInfo() {
        return info;
    }

    public void setInfo(byte[] info) {
        this.info = info;
    }

    public String getBaseIdx() {
        return idx;
    }

    @Override
    public void putToCache() {
        magicthronCache.put(this);
    }

    @Override
    public void transformDBData() {
        info = infoDB.build().toByteArray();
    }

    /*************************** 分割 **********************************/
    private DB_MagicThron.Builder infoDB;


    public void setInfoDB(DB_MagicThron.Builder infoDB) {
        this.infoDB = infoDB;
    }

    public DB_MagicThron.Builder getInfoDB() {
        if (this.infoDB == null) {
            if (getInfo() != null) {
                synchronized (this) {
                    if (this.infoDB != null) {
                        return this.infoDB;
                    }
                    try {
                        this.infoDB = DB_MagicThron.parseFrom(getInfo()).toBuilder();
                    } catch (InvalidProtocolBufferException e) {
                        LogUtil.printStackTrace(e);
                    }
                }
            } else {
                this.infoDB = DB_MagicThron.newBuilder();
            }
        }
        return this.infoDB;
    }

    public void refresh() {
        info = infoDB.build().toByteArray();
    }


    public void updateDailyData() {
        getInfoDB().clearBossTimes();
    }

    public void updateWeeklyData() {
        long cumuDamage = getInfoDB().getCumuDamage();
        List<Reward> rewards = findDamageRewardByDamage(cumuDamage);
        getInfoDB().clearCumuDamage();
        if (CollectionUtils.isEmpty(rewards)) {
            return;
        }
        sendBossDamageRewardByMail(getBaseIdx(), rewards, cumuDamage);
    }


    private List<Reward> findDamageRewardByDamage(long cumuDamage) {
        if (cumuDamage <= 0) {
            return Collections.emptyList();
        }
        for (ShuraArenaBossDamageRewardObject cfg : ShuraArenaBossDamageReward._ix_id.values()) {
            if (cumuDamage >= cfg.getDamagel() && cumuDamage < cfg.getDamageh()) {
                return RewardUtil.parseRewardIntArrayToRewardList(cfg.getReward());
            }
        }
        return Collections.emptyList();

    }


    private void sendBossDamageRewardByMail(String playerId, List<Reward> rewards, long cumuDamage) {
        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_MAGICTHRON, "魔方王座boss伤害奖励");
        EventUtil.triggerAddMailEvent(playerId, MailTemplateUsed.getById(GameConst.CONFIG_ID).getMagicthronbossdamagerankreward(), rewards, reason, String.valueOf(cumuDamage));
        LogUtil.info("player:{} do magicthron boss reward,player damage", playerId, cumuDamage);
    }

    public void sendMagicUpdate() {
        SC_MagicAreaInfoUpdate.Builder msg = SC_MagicAreaInfoUpdate.newBuilder();
        DB_MagicThron.Builder infoDB = getInfoDB();
        msg.setMyRank(MagicThronManager.getInstance().findPlayerRank(getIdx()));
        msg.setMaxDamage(infoDB.getMaxDamageOnce());
        msg.setLastDamage(infoDB.getLastDamage());
        msg.setBossTimes(MapUtil.map2IntMap(infoDB.getBossTimesMap()));
        GlobalData.getInstance().sendMsg(getIdx(),SC_MagicAreaInfoUpdate_VALUE,msg);
    }
}