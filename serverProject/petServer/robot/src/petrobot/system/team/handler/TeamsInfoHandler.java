package petrobot.system.team.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.SC_TeamsInfo;

@MsgId(msgId = MsgIdEnum.SC_TeamsInfo_VALUE)
public class TeamsInfoHandler extends AbstractHandler<SC_TeamsInfo> {
    @Override
    protected SC_TeamsInfo parse(byte[] bytes) throws Exception {
        return SC_TeamsInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_TeamsInfo result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                robotByChannel.getData().setTeamsInfo(result.toBuilder());
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
