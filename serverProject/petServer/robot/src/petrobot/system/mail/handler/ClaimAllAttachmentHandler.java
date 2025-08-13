package petrobot.system.mail.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Mail.SC_ClaimAllAttachmentMail;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_ClaimAllAttachmentMail_VALUE)
public class ClaimAllAttachmentHandler extends AbstractHandler<SC_ClaimAllAttachmentMail> {
    @Override
    protected SC_ClaimAllAttachmentMail parse(byte[] bytes) throws Exception {
        return SC_ClaimAllAttachmentMail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimAllAttachmentMail req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
