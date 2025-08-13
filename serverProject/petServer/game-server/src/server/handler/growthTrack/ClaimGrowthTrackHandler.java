package server.handler.growthTrack;

import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.util.PlayerUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_ClaimGrowthTrack;
import protocol.TargetSystem.SC_ClaimGrowthTrack;
import protocol.TargetSystem.SC_ClaimGrowthTrack.Builder;
import protocol.TargetSystemDB.DB_GrowthTrack;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/06/30
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimGrowthTrack_VALUE)
public class ClaimGrowthTrackHandler extends AbstractBaseHandler<CS_ClaimGrowthTrack> {
    @Override
    protected CS_ClaimGrowthTrack parse(byte[] bytes) throws Exception {
        return CS_ClaimGrowthTrack.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimGrowthTrack req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Builder resultBuilder = SC_ClaimGrowthTrack.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.GrowthTrack)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            gsChn.send(MsgIdEnum.SC_ClaimGrowthTrack_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimGrowthTrack_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_GrowthTrack.Builder growthTrackBuilder = entity.getGrowthTrackBuilder();
            resultBuilder.addAllCurMissionGroupIds(growthTrackBuilder.getCurMissionGroupIdsList());
            resultBuilder.addAllMissions(growthTrackBuilder.getMissionsMap().values());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimGrowthTrack_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.GrowthTrack;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimGrowthTrack_VALUE, SC_ClaimGrowthTrack.newBuilder().setRetCode(retCode));
    }
}
