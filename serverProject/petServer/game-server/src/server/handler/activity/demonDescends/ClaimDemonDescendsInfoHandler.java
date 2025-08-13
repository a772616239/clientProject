package server.handler.activity.demonDescends;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_ClaimDemonDescendsInfo;
import protocol.Activity.SC_ClaimDemonDescendsInfo;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.10.08
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimDemonDescendsInfo_VALUE)
public class ClaimDemonDescendsInfoHandler extends AbstractBaseHandler<CS_ClaimDemonDescendsInfo> {
    @Override
    protected CS_ClaimDemonDescendsInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimDemonDescendsInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimDemonDescendsInfo req, int i) {
        ServerActivity activity = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());

        SC_ClaimDemonDescendsInfo.Builder resultBuilder = SC_ClaimDemonDescendsInfo.newBuilder();
        if (activity == null || activity.getType() != ActivityTypeEnum.ATE_DemonDescends) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimDemonDescendsInfo_VALUE, resultBuilder);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimDemonDescendsInfo_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.refreshDemonDescendsActivityInfo(req.getActivityId());
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
