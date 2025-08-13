package petrobot.system.activity.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Activity.SC_SignIn;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_SignIn_VALUE)
public class SignInHandler extends AbstractHandler<SC_SignIn> {
    @Override
    protected SC_SignIn parse(byte[] bytes) throws Exception {
        return SC_SignIn.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_SignIn result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
