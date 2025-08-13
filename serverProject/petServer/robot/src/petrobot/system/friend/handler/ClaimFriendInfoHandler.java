package petrobot.system.friend.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.PlayerData;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Friend.SC_ClaimFriendInfo;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_ClaimFriendInfo_VALUE)
public class ClaimFriendInfoHandler extends AbstractHandler<SC_ClaimFriendInfo> {
    @Override
    protected SC_ClaimFriendInfo parse(byte[] bytes) throws Exception {
        return SC_ClaimFriendInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimFriendInfo req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
                PlayerData data = robotByChannel.getData();
                data.addOwnedFriend(req.getOwnedList());
                data.addApplyFriend(req.getApplyList());
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
