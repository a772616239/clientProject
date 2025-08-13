package petrobot.system.targetSystem.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystem.SC_RefreashDailyMissionPro;

@MsgId(msgId = MsgIdEnum.SC_RefreashDailyMissionPro_VALUE)
public class RefreshDailyProgressHandler extends AbstractHandler<SC_RefreashDailyMissionPro> {
    @Override
    protected SC_RefreashDailyMissionPro parse(byte[] bytes) throws Exception {
        return SC_RefreashDailyMissionPro.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RefreashDailyMissionPro result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
                robotByChannel.getData().replaceDailyMission(result.getMissionProList());
            });
        }
    }
}
