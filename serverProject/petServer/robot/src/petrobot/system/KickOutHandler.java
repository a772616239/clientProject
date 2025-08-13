package petrobot.system;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.LoginProto.SC_KickOut;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_KickOut_VALUE)
public class KickOutHandler extends AbstractHandler<SC_KickOut> {
    @Override
    protected SC_KickOut parse(byte[] bytes) throws Exception {
        return SC_KickOut.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_KickOut result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> robot.setDealResult(DealResultConst.CUR_STEP_FAILED));
            RobotManager.getInstance().removeRobot(robotByChannel.getId());
        }
        LogUtil.error("robot kickOut case by " + result.getRetCode());
    }
}
