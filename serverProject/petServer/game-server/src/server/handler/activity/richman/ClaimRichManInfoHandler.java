package server.handler.activity.richman;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_ClaimRichManInfo;
import protocol.Activity.SC_ClaimRichManInfo;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;

/**
 * 获取大富翁玩家部分信息
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimRichManInfo_VALUE)
public class ClaimRichManInfoHandler extends AbstractBaseHandler<CS_ClaimRichManInfo> {
    @Override
    protected CS_ClaimRichManInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimRichManInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimRichManInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        SC_ClaimRichManInfo.Builder resultBuilder = SC_ClaimRichManInfo.newBuilder();
        if (!ActivityUtil.activityInOpen(activityCfg)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_ClaimRichManInfo_VALUE, resultBuilder);
            return;
        }

        if (activityCfg.getType() != ActivityTypeEnum.ATE_RichMan) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimRichManInfo_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimRichManInfo_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            buildClientMsg(resultBuilder, entity, activityCfg);
            gsChn.send(MsgIdEnum.SC_ClaimRichManInfo_VALUE, resultBuilder);
            //doRichManDailyFreeItem(entity);
        });
    }

    private void doRichManDailyFreeItem(targetsystemEntity entity) {
        if (entity.getDb_Builder().getSpecialInfoBuilder().getRichManBuilder().getClaimDailyItem()) {
            return;
        }
        LogUtil.info("player:{} doRichManDailyFreeItem", entity.getLinkplayeridx());
        entity.getDb_Builder().getSpecialInfoBuilder().getRichManBuilder().setClaimDailyItem(true);
        Common.Reward reward = RewardUtil.parseReward(GameConfig.getById(GameConst.CONFIG_ID).getRichmandailyitem());
        if (reward != null) {
            RewardManager.getInstance().doReward(entity.getLinkplayeridx(), reward,
                    ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_RichMan), true);
        }
        entity.sendBeforeRichManInfo();
    }

    private void buildClientMsg(SC_ClaimRichManInfo.Builder resultBuilder, targetsystemEntity entity, ServerActivity activityCfg) {
        TargetSystemDB.DB_RichMan dbRichMan = entity.getDb_Builder().getSpecialInfo().getRichMan();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setCycle(dbRichMan.getCycle());

        resultBuilder.setCurPoint(dbRichMan.getCurPoint());
        resultBuilder.setDischargeRebate(dbRichMan.getDischargeRebate());
        resultBuilder.setDoubleReward(dbRichMan.getDoubleReward());
        for (Server.ServerRichManPoint point : activityCfg.getRichManPointMap().values()) {
            Activity.RichManPoint.Builder pointBuilder = Activity.RichManPoint.newBuilder();
            pointBuilder.setPointId(point.getPointId());
            pointBuilder.setPointType(point.getPointType());
            pointBuilder.addAllRewardList(point.getRewardListList());
            pointBuilder.setRebate(point.getRebate());
            for (Server.ServerBuyMission value : point.getBuyItemList()) {
                pointBuilder.addBuyItem(entity.serverBuyMission2ClientBuyMission(activityCfg, value));
            }
            resultBuilder.addRichManPoints(pointBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
