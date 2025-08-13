package petrobot.system.mail.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Mail.SC_DeleteMail;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_DeleteMail_VALUE)
public class DeleteMailHandler extends AbstractHandler<SC_DeleteMail> {
    @Override
    protected SC_DeleteMail parse(byte[] bytes) throws Exception {
        return SC_DeleteMail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_DeleteMail result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robotByChannel, r -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
