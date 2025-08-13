package petrobot.system.friend.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.PlayerData;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Friend.SC_FriendRecommend;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_FriendRecommend_VALUE)
public class RecommendFriendHandler extends AbstractHandler<SC_FriendRecommend> {
    @Override
    protected SC_FriendRecommend parse(byte[] bytes) throws Exception {
        return SC_FriendRecommend.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_FriendRecommend req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
                PlayerData data = robotByChannel.getData();
                data.addRecommend(req.getRecommandList());
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
