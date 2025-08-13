package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystem.CS_UpdateMistSweepTask;
import protocol.TargetSystem.SC_UpdateMistSweepTask;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_UpdateMistSweepTask_VALUE)
public class UpdateMistSweepTaskHandler extends AbstractBaseHandler<CS_UpdateMistSweepTask> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_UpdateMistSweepTask_VALUE, SC_UpdateMistSweepTask.newBuilder());
    }

    @Override
    protected CS_UpdateMistSweepTask parse(byte[] bytes) throws Exception {
        return CS_UpdateMistSweepTask.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpdateMistSweepTask req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (entity == null) {
            return;
        }
        entity.sendMistSweepTaskData();
    }
}
