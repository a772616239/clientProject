package petrobot.system.endlessSpire.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.EndlessSpire.SC_ClaimEndlessSpireRanking;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_ClaimEndlessSpireRanking_VALUE)
public class EndlessRankingInfoHandler extends AbstractHandler<SC_ClaimEndlessSpireRanking> {
    @Override
    protected SC_ClaimEndlessSpireRanking parse(byte[] bytes) throws Exception {
        return SC_ClaimEndlessSpireRanking.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimEndlessSpireRanking result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
