package petrobot.system.drawCard.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.DrawCard.SC_FriendShipDrawCard;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/12/25
 */
@MsgId(msgId = MsgIdEnum.SC_FriendShipDrawCard_VALUE)
public class FriendShipDrawCardHandler extends AbstractHandler<SC_FriendShipDrawCard> {
    @Override
    protected SC_FriendShipDrawCard parse(byte[] bytes) throws Exception {
        return SC_FriendShipDrawCard.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_FriendShipDrawCard req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
