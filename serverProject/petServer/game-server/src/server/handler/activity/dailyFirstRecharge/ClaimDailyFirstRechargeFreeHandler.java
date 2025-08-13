package server.handler.activity.dailyFirstRecharge;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.activity.ActivityManager;
import model.activity.DailyFirstRechargeManage;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.RetCodeId;
import protocol.Server;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_ClaimDailyFirstRechargeFree_VALUE)
public class ClaimDailyFirstRechargeFreeHandler extends AbstractBaseHandler<Activity.CS_ClaimDailyFirstRechargeFree> {
    @Override
    protected Activity.CS_ClaimDailyFirstRechargeFree parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimDailyFirstRechargeFree.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimDailyFirstRechargeFree req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        LogUtil.info("receive player:{} ClaimDailyFirstRechargeFree ,req:{}", playerIdx, req);

        Activity.SC_ClaimDailyFirstRechargeFree.Builder resultBuilder = Activity.SC_ClaimDailyFirstRechargeFree.newBuilder();

        Server.ServerActivity activity = ActivityManager.getInstance().findOneRecentActivityByType(Activity.ActivityTypeEnum.ATE_DailyFirstRecharge);
        if (activity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimDailyFirstRechargeFree_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimDailyFirstRechargeFree_VALUE, resultBuilder);
            return;
        }
        if (!stateCanClaim(entity)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_MissionCanNotClaim));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimDailyFirstRechargeFree_VALUE, resultBuilder);
            return;
        }
        LogUtil.info("player:{} ClaimDailyFirstRechargeFree cur dailyFirst", playerIdx, req);
        SyncExecuteFunction.executeConsumer(entity, e -> {
            TargetSystemDB.DB_DailyFirstRecharge.Builder dailyFirstRecharge = entity.getDb_Builder().getSpecialInfoBuilder().getDailyFirstRechargeBuilder();
            dailyFirstRecharge.setState(Activity.PayActivityStateEnum.BSE_Finish);
        });
        List<Common.Reward> rewards = DailyFirstRechargeManage.getInstance().queryDailyReward();
        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_DailyFirstPayRecharge);
        RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);
        entity.sendDailyFirstRechargeUpdate();
        LogUtil.info("player:{} ClaimDailyFirstRechargeFree success,req:{}", playerIdx, req);
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MessageId.MsgIdEnum.SC_ClaimDailyFirstRechargeFree_VALUE, resultBuilder);
    }

    private boolean stateCanClaim(targetsystemEntity entity) {
        return Activity.PayActivityStateEnum.PAS_SignOn == entity.getDb_Builder().getSpecialInfo().getDailyFirstRecharge().getState();
    }


    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }
}
