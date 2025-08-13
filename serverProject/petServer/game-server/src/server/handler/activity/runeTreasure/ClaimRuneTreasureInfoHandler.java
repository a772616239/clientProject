package server.handler.activity.runeTreasure;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_ClaimRuneTreasureInfo;
import protocol.Activity.SC_ClaimRuneTreasureInfo;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB.DB_RuneTreasureInfo;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/11/26
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimRuneTreasureInfo_VALUE)
public class ClaimRuneTreasureInfoHandler extends AbstractBaseHandler<CS_ClaimRuneTreasureInfo> {
    @Override
    protected CS_ClaimRuneTreasureInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimRuneTreasureInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimRuneTreasureInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        SC_ClaimRuneTreasureInfo.Builder resultBuilder = SC_ClaimRuneTreasureInfo.newBuilder();
        if (!ActivityUtil.activityInOpen(activityCfg)
                || activityCfg.getType() != ActivityTypeEnum.ATE_RuneTreasure) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimRuneTreasureInfo_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimRuneTreasureInfo_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_RuneTreasureInfo.Builder builder = entity.getDbRuneTreasureInfoBuilder(req.getActivityId());

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setDrawTimes(builder.getDrawTimes());
            resultBuilder.addAllClaimedProgress(builder.getClaimedProgressList());
            resultBuilder.addAllDailyMissionPro(builder.getDailyMissionProMap().values());
            gsChn.send(MsgIdEnum.SC_ClaimRuneTreasureInfo_VALUE, resultBuilder);
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
