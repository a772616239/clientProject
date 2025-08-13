package petrobot.system.activity.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.Activity.RefreshActivity;
import protocol.Activity.SC_RefreshActivity;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_RefreshActivity_VALUE)
public class RefreshActivityHandler extends AbstractHandler<SC_RefreshActivity> {
    @Override
    protected SC_RefreshActivity parse(byte[] bytes) throws Exception {
        return SC_RefreshActivity.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RefreshActivity ret, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                for (RefreshActivity activity : ret.getRefreshList()) {
                    robotByChannel.getData().refreshActivity(activity);
                }
            });
        }
    }
}
