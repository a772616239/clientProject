package petrobot.system.resCopy.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.ResourceCopy.SC_SweepCopy;

@MsgId(msgId = MsgIdEnum.SC_SweepCopy_VALUE)
public class SweepResCopyHandler extends AbstractHandler<SC_SweepCopy> {
    @Override
    protected SC_SweepCopy parse(byte[] bytes) throws Exception {
        return SC_SweepCopy.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_SweepCopy Result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
