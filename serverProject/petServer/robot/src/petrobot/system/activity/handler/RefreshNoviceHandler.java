package petrobot.system.activity.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.Activity.SC_RefreshNovicePro;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_RefreshNovicePro_VALUE)
public class RefreshNoviceHandler extends AbstractHandler<SC_RefreshNovicePro> {
    @Override
    protected SC_RefreshNovicePro parse(byte[] bytes) throws Exception {
        return SC_RefreshNovicePro.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RefreshNovicePro result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                robot.getData().updateNoviceMissionPro(result.getNewProList());
            });
        }
    }
}
