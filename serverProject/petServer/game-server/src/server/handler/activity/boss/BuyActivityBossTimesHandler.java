package server.handler.activity.boss;

import cfg.ActivityBossConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activityboss.ActivityBossManager;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Activity.SC_BuyActivityBossTimes;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;

/**
 * 购买boss挑战次数
 */
@MsgId(msgId = MsgIdEnum.CS_BuyActivityBossTimes_VALUE)
public class BuyActivityBossTimesHandler extends AbstractBaseHandler<Activity.CS_BuyActivityBossTimes> {

    @Override
    protected Activity.CS_BuyActivityBossTimes parse(byte[] bytes) throws Exception {
        return Activity.CS_BuyActivityBossTimes.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_BuyActivityBossTimes req, int i) {

        Activity.SC_BuyActivityBossTimes.Builder result = Activity.SC_BuyActivityBossTimes.newBuilder();
        String playerId = String.valueOf(gsChn.getPlayerId1());

        LogUtil.info("player:{} buy activity boss time,req:{}",playerId,req);

        if (!ActivityBossManager.getInstance().isOpened()) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ActivityBoss_Closed));
            gsChn.send(MsgIdEnum.SC_BuyActivityBossTimes_VALUE, result);
            return;
        }
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (entity == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BuyActivityBossTimes_VALUE, result);
            return;
        }
        SyncExecuteFunction.executeConsumer(entity, cache -> {
            int limitBuyTimes = ActivityBossConfig.getById(GameConst.CONFIG_ID).getLimitbuytimes();
            TargetSystemDB.DB_ActivityBoss.Builder activityBossBuilder = cache.getDb_Builder().getSpecialInfoBuilder().getActivityBossBuilder();
            //机会用完
            if (activityBossBuilder.getBuyTimes() >= limitBuyTimes) {
                result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ActivityBoss_CanNotBuyMore));
                gsChn.send(MessageId.MsgIdEnum.SC_BuyActivityBossTimes_VALUE, result);
                return;
            }

            Common.Consume consume = ConsumeUtil.parseConsume(ActivityBossConfig.getById(GameConst.CONFIG_ID).getBuyprice());
            //购买复活消耗
            if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ActivityBoss))) {
                result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatieralNotEnough));
                gsChn.send(MessageId.MsgIdEnum.SC_BuyActivityBossTimes_VALUE, result);
                return;
            }
            activityBossBuilder.setBuyTimes(activityBossBuilder.getBuyTimes() + 1).setTimes(activityBossBuilder.getTimes() - 1);
            GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_ActivityBossUpdate_VALUE, Activity.SC_ActivityBossUpdate.newBuilder().setTimes(activityBossBuilder.getTimes())
                    .setRetCode(protocol.RetCodeId.RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success).build()));
            LogUtil.info("player:{} buy activity boss time success ,now can challenge time:{}", playerId, activityBossBuilder.getTimes());

            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_BuyActivityBossTimes_VALUE, result);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ActivityBoss;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyActivityBossTimes_VALUE, SC_BuyActivityBossTimes.newBuilder().setRetCode(retCode));
    }
}
