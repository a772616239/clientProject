package petrobot.system.mainLine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.MainLine.SC_RefreashMainLine;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_RefreashMainLine_VALUE)
public class RefreshMainLineHandler extends AbstractHandler<SC_RefreashMainLine> {
    @Override
    protected SC_RefreashMainLine parse(byte[] bytes) throws Exception {
        return SC_RefreashMainLine.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RefreashMainLine result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
                r.getData().setMainLinePro(result.toBuilder().getMainLineProBuilder());
            });

        }

    }
}
