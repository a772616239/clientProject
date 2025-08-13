package server.handler.activity.dailyFirstRecharge;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.DailyFirstRechargeManage;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity;
import protocol.Common.EnumFunction;
import util.GameUtil;

@MsgId(msgId = protocol.MessageId.MsgIdEnum.CS_ClaimDailyFirstRechargeInfo_VALUE)
public class ClaimDailyFirstRechargeInfoHandler extends AbstractBaseHandler<Activity.CS_ClaimDailyFirstRechargeInfo> {
    @Override
    protected Activity.CS_ClaimDailyFirstRechargeInfo parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimDailyFirstRechargeInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimDailyFirstRechargeInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Activity.SC_ClaimDailyFirstRechargeInfo.Builder resultBuilder = Activity.SC_ClaimDailyFirstRechargeInfo.newBuilder();

        protocol.Server.ServerActivity activity = ActivityManager.getInstance().findOneRecentActivityByType(Activity.ActivityTypeEnum.ATE_DailyFirstRecharge);
        if (activity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(protocol.RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(protocol.MessageId.MsgIdEnum.SC_ClaimDailyFirstRechargeInfo_VALUE, resultBuilder);
            return;
        }


        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(protocol.RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(protocol.MessageId.MsgIdEnum.SC_ClaimDailyFirstRechargeInfo_VALUE, resultBuilder);
            return;
        }
        protocol.TargetSystemDB.DB_DailyFirstRecharge db_dailyFirstRecharge = entity.getDb_Builder().getSpecialInfo().getDailyFirstRecharge();
        resultBuilder.setRetCode(GameUtil.buildRetCode(protocol.RetCodeId.RetCodeEnum.RCE_Success));
        resultBuilder.setExploreTime(db_dailyFirstRecharge.getExploreTimes());
        resultBuilder.setRechargeDays(db_dailyFirstRecharge.getRechargeDays());
        resultBuilder.addAllEarnedReward(db_dailyFirstRecharge.getEarnedRewardList());
        if (db_dailyFirstRecharge.getState()!= Activity.PayActivityStateEnum.UNRECOGNIZED){
            resultBuilder.setState(db_dailyFirstRecharge.getState());
        }
        resultBuilder.addAllDailyReward(DailyFirstRechargeManage.getInstance().queryDailyReward());
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_ClaimDailyFirstRechargeInfo_VALUE, resultBuilder);
    }


    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
