package petrobot.system.friend.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Friend.SC_RespondAddFriend;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_RespondAddFriend_VALUE)
public class RespondAddFriendHandler extends AbstractHandler<SC_RespondAddFriend> {
    @Override
    protected SC_RespondAddFriend parse(byte[] bytes) throws Exception {
        return SC_RespondAddFriend.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_RespondAddFriend sc_respondAddFriend, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gameServerTcpChannel.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, t -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
        }
    }
}
