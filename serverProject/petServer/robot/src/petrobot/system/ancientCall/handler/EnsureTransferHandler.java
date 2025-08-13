package petrobot.system.ancientCall.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.AncientCall.SC_EnsureTransfer;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_EnsureTransfer_VALUE)
public class EnsureTransferHandler extends AbstractHandler<SC_EnsureTransfer> {
    @Override
    protected SC_EnsureTransfer parse(byte[] bytes) throws Exception {
        return SC_EnsureTransfer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_EnsureTransfer result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r ->
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
