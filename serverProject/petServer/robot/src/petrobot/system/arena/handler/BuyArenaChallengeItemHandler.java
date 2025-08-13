package petrobot.system.arena.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Arena.SC_BuyArenaChallengeItem;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/06/05
 */
@MsgId(msgId = MsgIdEnum.SC_BuyArenaChallengeItem_VALUE)
public class BuyArenaChallengeItemHandler extends AbstractHandler<SC_BuyArenaChallengeItem> {
    @Override
    protected SC_BuyArenaChallengeItem parse(byte[] bytes) throws Exception {
        return SC_BuyArenaChallengeItem.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_BuyArenaChallengeItem req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
