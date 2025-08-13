package petrobot.system.team.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.SC_BuyTeam;

@MsgId(msgId = MsgIdEnum.SC_BuyTeam_VALUE)
public class BuyTeamHandler extends AbstractHandler<SC_BuyTeam> {
    @Override
    protected SC_BuyTeam parse(byte[] bytes) throws Exception {
        return SC_BuyTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_BuyTeam result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
        }
    }
}
