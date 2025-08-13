package petrobot.system.resCopy.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.ResourceCopy.SC_BuyTimes;

@MsgId(msgId = MsgIdEnum.SC_BuyTimes_VALUE)
public class BuyTimesHandler extends AbstractHandler<SC_BuyTimes> {
    @Override
    protected SC_BuyTimes parse(byte[] bytes) throws Exception {
        return SC_BuyTimes.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_BuyTimes result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
