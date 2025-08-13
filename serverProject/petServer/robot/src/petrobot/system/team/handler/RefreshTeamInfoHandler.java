package petrobot.system.team.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.SC_RefreashTeam;

@MsgId(msgId = MsgIdEnum.SC_RefreashTeam_VALUE)
public class RefreshTeamInfoHandler extends AbstractHandler<SC_RefreashTeam> {
    @Override
    protected SC_RefreashTeam parse(byte[] bytes) throws Exception {
        return SC_RefreashTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RefreashTeam result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> robot.getData().refreshTeam(result.getTeamInfo()));
        }
    }
}
