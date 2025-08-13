package petrobot.system.team.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.SC_ChangeUsedTeam;

@MsgId(msgId = MsgIdEnum.SC_ChangeUsedTeam_VALUE)
public class ChangeUsedTeamHandler extends AbstractHandler<SC_ChangeUsedTeam> {
    @Override
    protected SC_ChangeUsedTeam parse(byte[] bytes) throws Exception {
        return SC_ChangeUsedTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ChangeUsedTeam result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
