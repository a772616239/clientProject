package server.handler.activity.hades;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_ClaimHadesInfo;
import protocol.Activity.SC_ClaimHadesInfo;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.10.10
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimHadesInfo_VALUE)
public class ClaimHadesInfoHandler extends AbstractBaseHandler<CS_ClaimHadesInfo> {
    @Override
    protected CS_ClaimHadesInfo parse(byte[] bytes) throws Exception {
        return  CS_ClaimHadesInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimHadesInfo req, int i) {
        ServerActivity activity = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        SC_ClaimHadesInfo.Builder resultBuilder = SC_ClaimHadesInfo.newBuilder();
//        if (!ActivityUtil.activityInOpen(activity)) {
//            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
//            gsChn.send(MsgIdEnum.SC_ClaimHadesInfo_VALUE, resultBuilder);
//            return;
//        }

        if (activity == null || activity.getType() != ActivityTypeEnum.ATE_HadesTreasure) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimHadesInfo_VALUE, resultBuilder);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimHadesInfo_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
           entity.sendHadesActivityInfo(req.getActivityId());
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
