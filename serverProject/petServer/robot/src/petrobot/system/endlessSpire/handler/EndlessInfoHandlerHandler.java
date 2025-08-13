package petrobot.system.endlessSpire.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.EndlessSpire.SC_ClaimEndlessSpireInfo;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_ClaimEndlessSpireInfo_VALUE)
public class EndlessInfoHandlerHandler extends AbstractHandler<SC_ClaimEndlessSpireInfo> {
    @Override
    protected SC_ClaimEndlessSpireInfo parse(byte[] bytes) throws Exception {
        return SC_ClaimEndlessSpireInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimEndlessSpireInfo result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
            robotByChannel.getData().setCurSpireLv(result.getMaxSpireLv());
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
