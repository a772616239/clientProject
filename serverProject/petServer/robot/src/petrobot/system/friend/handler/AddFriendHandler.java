package petrobot.system.friend.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Friend.FriendInfo;
import protocol.Friend.SC_AddFriend;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_AddFriend_VALUE)
public class AddFriendHandler extends AbstractHandler<SC_AddFriend> {
    @Override
    protected SC_AddFriend parse(byte[] bytes) throws Exception {
        return SC_AddFriend.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_AddFriend req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
                FriendInfo friend = req.getFriend();
                robotByChannel.getData().addOwnedFriend(friend);
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
