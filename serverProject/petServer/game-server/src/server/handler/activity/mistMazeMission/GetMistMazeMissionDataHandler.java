package server.handler.activity.mistMazeMission;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystem.CS_MistMazeActivityMission;
import protocol.TargetSystem.SC_MistMazeActivityMission;

@MsgId(msgId = MsgIdEnum.CS_MistMazeActivityMission_VALUE)
public class GetMistMazeMissionDataHandler extends AbstractBaseHandler<CS_MistMazeActivityMission> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistMaze;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_MistMazeActivityMission_VALUE, SC_MistMazeActivityMission.newBuilder());
    }

    @Override
    protected CS_MistMazeActivityMission parse(byte[] bytes) throws Exception {
        return CS_MistMazeActivityMission.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MistMazeActivityMission req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        SC_MistMazeActivityMission.Builder retBuilder = SC_MistMazeActivityMission.newBuilder();
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            gsChn.send(MsgIdEnum.SC_MistMazeActivityMission_VALUE, retBuilder);
            return;
        }
        entity.sendMistMazeMission();
    }
}
