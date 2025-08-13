package petrobot.system.targetSystem.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystem.SC_RefreshAchievement;


@MsgId(msgId = MsgIdEnum.SC_RefreshAchievement_VALUE)
public class RefreshAchievementHandler extends AbstractHandler<SC_RefreshAchievement> {
    @Override
    protected SC_RefreshAchievement parse(byte[] bytes) throws Exception {
        return SC_RefreshAchievement.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RefreshAchievement result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
                robotByChannel.getData().replaceAchievement(result.getMissionProList());
            });
        }
    }
}
