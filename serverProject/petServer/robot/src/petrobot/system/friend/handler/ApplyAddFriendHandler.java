package petrobot.system.friend.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Friend.SC_ApplyAddFriend;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_ApplyAddFriend_VALUE)
public class ApplyAddFriendHandler extends AbstractHandler<SC_ApplyAddFriend> {
    @Override
    protected SC_ApplyAddFriend parse(byte[] bytes) throws Exception {
        return SC_ApplyAddFriend.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ApplyAddFriend sc_applyAddFriend, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, t -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
        }
    }
}
