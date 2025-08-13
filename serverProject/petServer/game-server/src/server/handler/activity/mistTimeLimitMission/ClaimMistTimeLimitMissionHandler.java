package server.handler.activity.mistTimeLimitMission;


import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_ClaimMistTimeLimitMission;
import protocol.TargetSystem.SC_ClaimMistTimeLimitMission;
import protocol.TargetSystem.SC_ClaimMistTimeLimitMission.Builder;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/08/06
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimMistTimeLimitMission_VALUE)
public class ClaimMistTimeLimitMissionHandler extends AbstractBaseHandler<CS_ClaimMistTimeLimitMission> {
    @Override
    protected CS_ClaimMistTimeLimitMission parse(byte[] bytes) throws Exception {
        return CS_ClaimMistTimeLimitMission.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimMistTimeLimitMission req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Builder resultBuilder = SC_ClaimMistTimeLimitMission.newBuilder();
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimMistTimeLimitMission_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.sendMistTimeLimitMissionMsg();
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimMistTimeLimitMission_VALUE, SC_ClaimMistTimeLimitMission.newBuilder().setRetCode(retCode));
    }
}
