package petrobot.system.mainLine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MainLine.MainLineProgress;
import protocol.MainLine.SC_ClaimMainLine;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_ClaimMainLine_VALUE)
public class ClaimMainLineInfoHandler extends AbstractHandler<SC_ClaimMainLine> {
    @Override
    protected SC_ClaimMainLine parse(byte[] bytes) throws Exception {
        return SC_ClaimMainLine.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimMainLine result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
            MainLineProgress mainLinePro = result.getMainLinePro();
            robotByChannel.getData().setMainLinePro(mainLinePro.toBuilder());
            robotByChannel.getData().setStartOnHookTime(result.getStartOnHookTime());
            robotByChannel.getData().setCurOnHookNode(result.getCurOnHookNode());
            robotByChannel.getData().setTodayQuickTimes(result.getTodayQuickTimes());
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
