package petrobot.system.player.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.SC_GlobalAddition;

@MsgId(msgId = MsgIdEnum.SC_GlobalAddition_VALUE)
public class GlobalAdditionHandler extends AbstractHandler<SC_GlobalAddition> {
    @Override
    protected SC_GlobalAddition parse(byte[] bytes) throws Exception {
        return SC_GlobalAddition.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_GlobalAddition result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, t -> robotByChannel.getData().setGlobalAddition(result.toBuilder()));
    }
}
