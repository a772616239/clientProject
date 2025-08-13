package model.warpServer.crossServer.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_MistTargetMissionData;

@MsgId(msgId = MsgIdEnum.CS_GS_MistTargetMissionData_VALUE)
public class MistTargetMissionDataHandler extends AbstractHandler<CS_GS_MistTargetMissionData> {
    @Override
    protected CS_GS_MistTargetMissionData parse(byte[] bytes) throws Exception {
        return CS_GS_MistTargetMissionData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_MistTargetMissionData ret, int i) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(ret.getPlayerIdx());
        if (target == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(target, entity -> entity.doTargetPro(ret.getTargetType(), ret.getAddProg(), ret.getParam()));
    }
}
