package petrobot.system.team.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.SC_ChangeTeamName;

@MsgId(msgId = MsgIdEnum.SC_ChangeTeamName_VALUE)
public class ChangeTeamNameHandler extends AbstractHandler<SC_ChangeTeamName> {
    @Override
    protected SC_ChangeTeamName parse(byte[] bytes) throws Exception {
        return SC_ChangeTeamName.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ChangeTeamName result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
