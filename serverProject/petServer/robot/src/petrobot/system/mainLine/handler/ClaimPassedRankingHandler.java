package petrobot.system.mainLine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MainLine.SC_ClaimPassedRanking;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_ClaimPassedRanking_VALUE)
public class ClaimPassedRankingHandler extends AbstractHandler<SC_ClaimPassedRanking> {
    @Override
    protected SC_ClaimPassedRanking parse(byte[] bytes) throws Exception {
        return SC_ClaimPassedRanking.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimPassedRanking result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
