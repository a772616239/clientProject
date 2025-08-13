package server.handler.activity;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity;
import protocol.Activity.CS_BuyActivityGoods;
import protocol.Activity.SC_BuyActivityGoods;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;
import util.MapUtil;


@MsgId(msgId = MsgIdEnum.CS_BuyActivityGoods_VALUE)
public class BuyActivityGoodsHandler extends AbstractBaseHandler<CS_BuyActivityGoods> {
    @Override
    protected CS_BuyActivityGoods parse(byte[] bytes) throws Exception {
        return CS_BuyActivityGoods.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyActivityGoods req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        long activityId = req.getActivityId();
        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(activityId);
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_BuyActivityGoods.Builder resultBuilder = SC_BuyActivityGoods.newBuilder();
        Server.ServerBuyMission buyInfoCfg;
        if (entity == null
                || req.getBuyTimes() <= 0
                || !ActivityUtil.activityInOpen(activityCfg)
                || (buyInfoCfg = activityCfg.getBuyMissionMap().get(req.getGoodsId())) == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_BuyActivityGoods_VALUE, resultBuilder);
            return;
        }

        if (buyInfoCfg.getEndTimestamp() != -1 && GlobalTick.getInstance().getCurrentTime() > buyInfoCfg.getEndTimestamp()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_MissionOutOfTime));
            gsChn.send(MsgIdEnum.SC_BuyActivityGoods_VALUE, resultBuilder);
            return;
        }

        LogUtil.info("BuyActivityGoodsHandler playerIdx:{} BuyActivityGoods ,req:{}", playerIdx, req);

        RetCodeEnum retCode = SyncExecuteFunction.executeFunction(entity, e -> {

            TargetSystemDB.DB_ActivityBuyInfo.Builder buyInfoInDb = pullBuyInfoFromDb(activityId, entity);

            if (buyCountUseOut(req, buyInfoInDb, buyInfoCfg)) {
                return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
            }

            //todo 新增活动,这里的getReason方法需要新增
            Reason reason = getReason(activityCfg);

            Consume consume = ConsumeUtil.multiConsume(buyInfoCfg.getPrice(), req.getBuyTimes());
            if (!ConsumeManager.getInstance().asyncConsumeMaterial(playerIdx, consume, reason)) {
                return RetCodeEnum.RCE_MatieralNotEnough;
            }

            doReward(req, playerIdx, buyInfoCfg, reason);

            saveBuyRecord(entity, buyInfoInDb, req);

            return RetCodeEnum.RCE_Success;
        });

        if (retCode == RetCodeEnum.RCE_Success) {
            LogUtil.info("playerIdx:{} success buy Activity goods,req:{}", playerIdx, req);
            entity.sendUpdateActivityGoodsInfo(activityId);
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.SC_BuyActivityGoods_VALUE, resultBuilder);
    }

    private void saveBuyRecord(targetsystemEntity entity, TargetSystemDB.DB_ActivityBuyInfo.Builder buyInfoInDb, CS_BuyActivityGoods req) {
        Common.IntMap.Builder buyRecordBuilder = buyInfoInDb.getBuyRecordBuilder();
        MapUtil.incrIntMapValue(buyRecordBuilder, req.getGoodsId(), req.getBuyTimes());
        entity.getDb_Builder().addActivityBuyInfo(buyInfoInDb);
        LogUtil.info("playerIdx:{} buy activityGoods save buy record to cache,record:{}", entity.getLinkplayeridx(), buyInfoInDb);
    }

    private Reason getReason(ServerActivity activity) {
        if (activity.getType() == Activity.ActivityTypeEnum.ATE_RichMan) {
            return ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_RichMan);
        }
        if (activity.getType() == Activity.ActivityTypeEnum.ATE_FestivalBoss) {
            return ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_FestivalBoss, "购买活动boss商品");
        }
        LogUtil.warn("BuyActivityGoodsHandler getReason method need support more detail reason info");
        return ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Activity);
    }

    private void doReward(CS_BuyActivityGoods req, String playerIdx, Server.ServerBuyMission buyInfoCfg, Reason reason) {
        List<Reward> rewards = RewardUtil.multiReward(buyInfoCfg.getRewardsList(), req.getBuyTimes());
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);
    }

    private boolean buyCountUseOut(CS_BuyActivityGoods req, TargetSystemDB.DB_ActivityBuyInfo.Builder buyInfoInDb, Server.ServerBuyMission buyInfoCfg) {
        Integer dbValue = MapUtil.getIntMapValue(buyInfoInDb.getBuyRecord(), req.getGoodsId());
        int alreadyBuyCount = dbValue == null ? 0 : dbValue;
        return alreadyBuyCount + req.getBuyTimes() >= buyInfoCfg.getRewardsCount();
    }

    private TargetSystemDB.DB_ActivityBuyInfo.Builder pullBuyInfoFromDb(long activityId, targetsystemEntity entity) {
        List<TargetSystemDB.DB_ActivityBuyInfo.Builder> InfoList = entity.getDb_Builder().getActivityBuyInfoBuilderList();
        TargetSystemDB.DB_ActivityBuyInfo.Builder builder;
        for (int i = 0; i < InfoList.size(); i++) {
            builder = InfoList.get(i);
            if (builder.getActivityId() == activityId) {
                InfoList.remove(i);
                return builder;
            }
        }
        return TargetSystemDB.DB_ActivityBuyInfo.newBuilder().setActivityId(activityId);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
