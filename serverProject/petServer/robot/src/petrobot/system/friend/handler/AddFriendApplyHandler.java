package petrobot.system.friend.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.Friend.FriendBaseInfo;
import protocol.Friend.SC_AddFriendApply;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_AddFriendApply_VALUE)
public class AddFriendApplyHandler extends AbstractHandler<SC_AddFriendApply> {
    @Override
    protected SC_AddFriendApply parse(byte[] bytes) throws Exception {
        return SC_AddFriendApply.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_AddFriendApply result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
                ArrayList<FriendBaseInfo> objects = new ArrayList<>();
                objects.add(result.getAddFriednApply());
                robotByChannel.getData().addApplyFriend(objects);
            });
        }
    }
}
