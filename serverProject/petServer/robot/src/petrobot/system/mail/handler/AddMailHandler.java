package petrobot.system.mail.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.Mail.SC_AddNewMail;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_AddNewMail_VALUE)
public class AddMailHandler extends AbstractHandler<SC_AddNewMail> {
    @Override
    protected SC_AddNewMail parse(byte[] bytes) throws Exception {
        return SC_AddNewMail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_AddNewMail result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> robotByChannel.getData().addMailList(result.getMailinfoList()));
    }
}
