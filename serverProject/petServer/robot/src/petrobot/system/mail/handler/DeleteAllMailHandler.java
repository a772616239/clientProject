package petrobot.system.mail.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Mail.SC_DeleteAllReadMail;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_DeleteAllReadMail_VALUE)
public class DeleteAllMailHandler extends AbstractHandler<SC_DeleteAllReadMail> {
    @Override
    protected SC_DeleteAllReadMail parse(byte[] bytes) throws Exception {
        return SC_DeleteAllReadMail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_DeleteAllReadMail req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
            robotByChannel.getData().getMailInfoMap().clear();
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
