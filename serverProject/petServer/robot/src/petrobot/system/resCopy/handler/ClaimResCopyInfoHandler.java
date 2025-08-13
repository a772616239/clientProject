package petrobot.system.resCopy.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.ResourceCopy.SC_ClaimResCopy;

import java.util.ArrayList;

@MsgId(msgId = MsgIdEnum.SC_ClaimResCopy_VALUE)
public class ClaimResCopyInfoHandler extends AbstractHandler<SC_ClaimResCopy> {
    @Override
    protected SC_ClaimResCopy parse(byte[] bytes) throws Exception {
        return SC_ClaimResCopy.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimResCopy result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
            robotByChannel.getData().setResCopies(new ArrayList<>(result.getResCopyDataList()));
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
