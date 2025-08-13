package server.handler.targetSystem;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystem.CS_RefreashMistSeasonMissionPro;
import protocol.TargetSystem.SC_RefreashMistSeasonMissionPro;

@MsgId(msgId = MsgIdEnum.CS_RefreashMistSeasonMissionPro_VALUE)
public class RequestMistSeasonTaskHandler extends AbstractBaseHandler<CS_RefreashMistSeasonMissionPro> {
    @Override
    protected CS_RefreashMistSeasonMissionPro parse(byte[] bytes) throws Exception {
        return CS_RefreashMistSeasonMissionPro.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RefreashMistSeasonMissionPro req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (targetEntity == null) {
            return;
        }
        SC_RefreashMistSeasonMissionPro.Builder builder = SC_RefreashMistSeasonMissionPro.newBuilder();
        builder.addAllMistSeasonTask(targetEntity.getDb_Builder().getMistSeasonTaskMap().values());
        gsChn.send(MsgIdEnum.SC_RefreashMistSeasonMissionPro_VALUE, builder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_RefreashMistSeasonMissionPro_VALUE, SC_RefreashMistSeasonMissionPro.newBuilder());
    }
}
