package petrobot.system;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.GM.SC_GM;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_GM_VALUE)
public class GMHandler extends AbstractHandler<SC_GM> {
    @Override
    protected SC_GM parse(byte[] bytes) throws Exception {
        return SC_GM.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_GM sc_gm, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
