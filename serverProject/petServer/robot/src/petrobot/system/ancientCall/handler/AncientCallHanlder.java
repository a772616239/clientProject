package petrobot.system.ancientCall.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.AncientCall.SC_CallAncient;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_CallAncient_VALUE)
public class AncientCallHanlder extends AbstractHandler<SC_CallAncient> {
    @Override
    protected SC_CallAncient parse(byte[] bytes) throws Exception {
        return SC_CallAncient.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_CallAncient ret, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
