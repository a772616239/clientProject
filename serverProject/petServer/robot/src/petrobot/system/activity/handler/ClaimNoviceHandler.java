package petrobot.system.activity.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Activity.SC_ClaimNovice;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_ClaimNovice_VALUE)
public class ClaimNoviceHandler extends AbstractHandler<SC_ClaimNovice> {
    @Override
    protected SC_ClaimNovice parse(byte[] bytes) throws Exception {
        return SC_ClaimNovice.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimNovice result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                robot.getData().setNovice(result.toBuilder());
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
