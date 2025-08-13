package petrobot.system.mine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.SC_AllFriendHelpList;

@MsgId(msgId = MsgIdEnum.SC_AllFriendHelpList_VALUE)
public class QueryFriendHelpListHandler extends AbstractHandler<SC_AllFriendHelpList> {
    @Override
    protected SC_AllFriendHelpList parse(byte[] bytes) throws Exception {
        return SC_AllFriendHelpList.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_AllFriendHelpList ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
