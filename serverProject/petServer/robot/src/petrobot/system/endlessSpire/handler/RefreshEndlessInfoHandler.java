package petrobot.system.endlessSpire.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.EndlessSpire.SC_RefreashSpireLv;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_RefreashSpireLv_VALUE)
public class RefreshEndlessInfoHandler extends AbstractHandler<SC_RefreashSpireLv> {
    @Override
    protected SC_RefreashSpireLv parse(byte[] bytes) throws Exception {
        return SC_RefreashSpireLv.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RefreashSpireLv result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
            robotByChannel.getData().setCurSpireLv(result.getNewLv());
        });
    }
}
