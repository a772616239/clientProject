package petrobot.system.mainLine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MainLine.SC_QuickOnHook;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_QuickOnHook_VALUE)
public class QuickOnHookHandler extends AbstractHandler<SC_QuickOnHook> {
    @Override
    protected SC_QuickOnHook parse(byte[] bytes) throws Exception {
        return SC_QuickOnHook.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_QuickOnHook req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
