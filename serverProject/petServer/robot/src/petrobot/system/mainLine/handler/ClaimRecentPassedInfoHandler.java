package petrobot.system.mainLine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RecentPassedOuterClass.SC_ClaimRecentPassed;

@MsgId(msgId = MsgIdEnum.SC_ClaimRecentPassed_VALUE)
public class ClaimRecentPassedInfoHandler extends AbstractHandler<SC_ClaimRecentPassed> {
    @Override
    protected SC_ClaimRecentPassed parse(byte[] bytes) throws Exception {
        return SC_ClaimRecentPassed.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimRecentPassed result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
