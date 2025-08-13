package petrobot.system.friend.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Friend.SC_ClaimAllPointAndSend;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_ClaimAllPointAndSend_VALUE)
public class ClaimAllPointAndSendHandler extends AbstractHandler<SC_ClaimAllPointAndSend> {
    @Override
    protected SC_ClaimAllPointAndSend parse(byte[] bytes) throws Exception {
        return SC_ClaimAllPointAndSend.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimAllPointAndSend req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, t -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
        }
    }
}
