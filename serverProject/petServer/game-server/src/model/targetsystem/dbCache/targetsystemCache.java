/*CREATED BY TOOL*/

package model.targetsystem.dbCache;

import annotation.annationInit;
import cfg.GameConfig;
import cfg.PayRewardConfig;
import cfg.PayRewardConfigObject;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import entity.UpdateDailyData;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardUtil;
import model.targetsystem.cache.targetsystemUpdateCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import protocol.Activity.EnumRankingType;
import protocol.Activity.PayActivityBonus;
import protocol.Activity.PayActivityStateEnum;
import protocol.Activity.RechargeType;
import protocol.Activity.SC_GetPayActivityInfo;
import protocol.Common;
import protocol.MessageId;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB;
import protocol.TargetSystemDB.DB_TargetSpecialInfo;
import protocol.TargetSystemDB.DB_TargetSystem.Builder;
import protocol.TargetSystemDB.PayActivityRecord;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@annationInit(value = "targetsystemCache", methodname = "load")
public class targetsystemCache extends baseCache<targetsystemCache> implements IbaseCache, UpdateDailyData {

    /******************* MUST HAVE ********************************/

    private static targetsystemCache instance = null;

    public static targetsystemCache getInstance() {

        if (instance == null) {
            instance = new targetsystemCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "targetsystemDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("targetsystemDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (targetsystemCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(targetsystemEntity v) {
        getInstance().putBaseEntity(v);
    }

    public static targetsystemEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (targetsystemEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return targetsystemUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {
        targetsystemEntity t = (targetsystemEntity) v;
        if (t != null) {
            String linkPlayerIdx = t.getLinkplayeridx();
            if (linkPlayerIdx != null) {
                targetMap.put(linkPlayerIdx, t);
            }
        }
    }

    /*******************MUST HAVE END ********************************/

    private static final Map<String, targetsystemEntity> targetMap = new ConcurrentHashMap<>();
    private static Map<Integer, TargetSystemDB.PayActivityRecord> defaultPayActivityRecord;

    private static Map<Integer, TargetSystemDB.PayActivityRecord> getDefaultPayActivityBonus() {
        if (defaultPayActivityRecord == null) {
            defaultPayActivityRecord = new HashMap<>();
            Collection<PayRewardConfigObject> values = PayRewardConfig._ix_id.values();
            for (PayRewardConfigObject config : values) {
                TargetSystemDB.PayActivityRecord.Builder builder = TargetSystemDB.PayActivityRecord.newBuilder();
                List<PayActivityBonus> defaultPayActivity = new ArrayList<>();
                for (int i = 0; i < config.getReward().length; i++) {
                    List<Common.Reward> rewards = RewardUtil.getRewardsByRewardId(config.getReward()[i]);
                    if (CollectionUtils.isEmpty(rewards)) {
                        throw new RuntimeException("支付活动奖励配置错误");
                    }
                    defaultPayActivity.add(protocol.Activity.PayActivityBonus.newBuilder().addAllBonus(rewards).build());
                }
                builder.addAllBonus(defaultPayActivity);
                defaultPayActivityRecord.put(config.getId(), builder.build());
            }
        }
        return defaultPayActivityRecord;
    }


    public targetsystemEntity getTargetEntityByPlayerIdx(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.warn("targetSystemCache getTargetEntityByPlayerIdx is null ,playerIdx:{}", playerIdx);
            return null;
        }

        targetsystemEntity entity = targetMap.get(playerIdx);
        if (entity == null && PlayerUtil.playerIsExist(playerIdx)) {
            entity = createNewTargetEntity(playerIdx);
        }
        return entity;
    }

    public targetsystemEntity createNewTargetEntity(String playerIdx) {
        if (playerIdx == null) {
            return null;
        }

        targetsystemEntity entity = new targetsystemEntity(playerIdx);
        entity.initFeats();
        entity.putToCache();
        return entity;
    }

    public void onPlayerLogin(String playerIdx) {
        targetsystemEntity entity = getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                entity.onPlayerLogIn();
            });
        }
    }


    public boolean firstRechargeNotActive(String playerId) {
        targetsystemEntity entity = getTargetEntityByPlayerIdx(playerId);
        if (entity != null) {
            PayActivityRecord payRewardRecord = getPayRewardRecord(playerId, RechargeType.RT_FirstPay_VALUE);
            return payRewardRecord != null && payRewardRecord.getState() == PayActivityStateEnum.PAS_NotActive_VALUE;
        }
        return true;
    }

    public boolean cumuRechargeActive(String playerId) {
        return PayActivityStateEnum.PAS_NotActive_VALUE !=
                getPayRewardRecord(playerId, protocol.Activity.RechargeType.RT_SignlePay_VALUE).getState();
    }

    @Override
    public void updateDailyData() {
        Map<String, BaseEntity> all = getAll();
        for (BaseEntity value : all.values()) {
            if (value instanceof targetsystemEntity) {
                targetsystemEntity entity = (targetsystemEntity) value;
                try {
                    SyncExecuteFunction.executeConsumer(entity,
                            e -> entity.updateDailyData(GlobalData.getInstance().checkPlayerOnline(entity.getLinkplayeridx())));
                } catch (Exception ex) {
                    LogUtil.error("targetSystemCache.updateDailyData error by playerId:[{}]", entity.getLinkplayeridx());
                    LogUtil.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * 该方法用于清除指定活动id的数据
     */
    public void clearAllPlayerActivitiesData(ServerActivity activity) {
        LogUtil.info("clear all player activityData, clear id:" + activity.getActivityId());
        for (targetsystemEntity value : targetMap.values()) {
            SyncExecuteFunction.executeConsumer(value, v -> {
                value.clearActivitiesData(activity);
            });
        }
    }

    public void sendNewActivityToAllOnlinePlayer(List<ServerActivity> serverActivities) {
        if (GameUtil.collectionIsEmpty(serverActivities)) {
            return;
        }

        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
        if (GameUtil.collectionIsEmpty(allOnlinePlayerIdx)) {
            return;
        }

        for (String onlinePlayerIdx : allOnlinePlayerIdx) {
            targetsystemEntity entity = getTargetEntityByPlayerIdx(onlinePlayerIdx);
            if (entity != null) {
                entity.sendNewActivity(serverActivities);
            }
        }
    }


    /**
     * 推送充值活动
     *
     * @param playerId
     */
    public void sendRechargeActivityShow(String playerId) {
        if (StringUtils.isEmpty(playerId)) {
            return;
        }
        SC_GetPayActivityInfo.Builder result = getPayActivityBuilder(playerId);

        //推送消息
        GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_GetPayActivityInfo_VALUE, result);
    }

    private protocol.Activity.SC_GetPayActivityInfo.Builder getPayActivityBuilder(String playerId) {
        SC_GetPayActivityInfo.Builder result = SC_GetPayActivityInfo.newBuilder();
        targetsystemEntity entity = getTargetEntityByPlayerIdx(playerId);
        if (entity == null) {
            return result;
        }
        Builder db_builder = entity.getDb_Builder();
        //首充状态
        result.setFirstPayEnable(!firstRechargeNotActive(playerId));
        result.setCumuPayStep(db_builder.getCumuRechargeCoupon());
        //首充奖励
        TargetSystemDB.PayActivityRecord payRewardRecord = getPayRewardRecord(playerId, 0);
        if (payRewardRecord != null) {
            result.addAllFirstPayBonusList(payRewardRecord.getBonusList());
        }
        //累充奖励
        TargetSystemDB.PayActivityRecord payRewardRecord2 = getPayRewardRecord(playerId, 1);
        if (payRewardRecord2 != null) {
            result.addAllCumuPayBonusList(payRewardRecord2.getBonusList());
        }
        return result;
    }

    public TargetSystemDB.PayActivityRecord getPayRewardRecord(String playerIdx, int index) {
        Map<Integer, TargetSystemDB.PayActivityRecord> payActivityRecordMap = getTargetEntityByPlayerIdx(playerIdx).getDb_Builder().getPayActivityRecordMap();
        if (CollectionUtils.isEmpty(payActivityRecordMap) || payActivityRecordMap.get(index) == null) {
            return getDefaultPayActivityBonus().get(index);
        }
        return payActivityRecordMap.get(index);
    }

    public void initRechargeRecord(String targetIdx, int index) {
        PayRewardConfigObject config = PayRewardConfig.getById(index);
        targetsystemEntity target = targetsystemCache.getByIdx(targetIdx);
        if (config == null || target == null) {
            return;
        }
        Map<Integer, TargetSystemDB.PayActivityRecord> payActivityRecordMap = target.getDb_Builder().getPayActivityRecordMap();
        if (CollectionUtils.isEmpty(payActivityRecordMap) || payActivityRecordMap.get(index) == null) {
            TargetSystemDB.PayActivityRecord.Builder builder = TargetSystemDB.PayActivityRecord.newBuilder();
            for (int i = 0; i < config.getReward().length; i++) {
                List<Common.Reward> rewards = RewardUtil.getRewardsByRewardId(config.getReward()[i]);
                protocol.Activity.PayActivityBonus.Builder builder1 = protocol.Activity.PayActivityBonus.newBuilder().addAllBonus(rewards);
                if (i == 0) {
                    builder1.setBonusState(protocol.Activity.BonusStateEnum.BSE_WaitSignOn_VALUE).setClaimTimestamp(GlobalTick.getInstance().getCurrentTime());
                }
                builder.setState(protocol.Activity.PayActivityStateEnum.PAS_SignOn_VALUE).addBonus(builder1.build());
            }
            target.getDb_Builder().putPayActivityRecord(index, builder.build());
        }
    }

    /**
     * 清空迷雾深林限时任务进度
     */
    public void clearAllPlayerMistTimeLimitMissionProgress() {
        for (BaseEntity value : getAll().values()) {
            if (!(value instanceof targetsystemEntity)) {
                continue;
            }
            targetsystemEntity entity = (targetsystemEntity) value;
            SyncExecuteFunction.executeConsumer(entity
                    , e -> {
                        entity.getDb_Builder().getSpecialInfoBuilder().clearMistTimeLimitMission();
                        entity.sendMistTimeLimitMissionMsg();
                    });
        }
    }

    public int queryRankingActivityScore(String playerIdx, long activityId, EnumRankingType rankingType) {
        if (StringUtils.isBlank(playerIdx)) {
            return 0;
        }
        targetsystemEntity entity = getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return 0;
        }
        return entity.getRankingActivityScore(activityId, rankingType);
    }

    public boolean canRecordMistKillPlayerTarget(String playerIdx, String targetPlayerIdx) {
        if (StringUtils.isEmpty(playerIdx) || StringUtils.isEmpty(targetPlayerIdx)) {
            return false;
        }

        targetsystemEntity entity = getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return false;
        }

        return SyncExecuteFunction.executeFunction(entity, e -> {
            DB_TargetSpecialInfo.Builder infoBuilder = entity.getDb_Builder().getTargetSpecialInfoBuilder();
            Long aLong = infoBuilder.getMistKillPlayerRecordMap().get(targetPlayerIdx);
            long currentTime = GlobalTick.getInstance().getCurrentTime();
            if (aLong == null
                    || (currentTime - aLong) >= TimeUtil.MS_IN_A_S * GameConfig.getById(GameConst.CONFIG_ID).getMistkillplayerupdateinterval()) {
                LogUtil.debug("targetsystemCache.canRecordMistKillPlayerTarget, playerIdx:"
                        + entity.getLinkplayeridx() + ", kill target playerIdx:" + targetPlayerIdx
                        + ", last update time:" + TimeUtil.formatStamp(aLong == null ? 0 : aLong));
                infoBuilder.putMistKillPlayerRecord(targetPlayerIdx, currentTime);
                return true;
            }
            return false;
        });
    }

    public void clearAllPlayerTheWarSeasonMissionPro() {
        for (BaseEntity value : _ix_id.values()) {
            if (!(value instanceof targetsystemEntity)) {
                return;
            }
            targetsystemEntity entity = (targetsystemEntity) value;
            SyncExecuteFunction.executeConsumer(entity, e -> e.getDb_Builder().getSpecialInfoBuilder().clearTheWarSeasonMission());
        }
    }

    public void clearAllPlayerMistSessionTaskData() {
        for (BaseEntity value : _ix_id.values()) {
            if (!(value instanceof targetsystemEntity)) {
                continue;
            }
            targetsystemEntity entity = (targetsystemEntity) value;
            SyncExecuteFunction.executeConsumer(entity, e -> entity.clearMistSeasonTaskData());
        }
    }
}
