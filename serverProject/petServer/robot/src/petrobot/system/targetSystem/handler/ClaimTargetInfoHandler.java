package petrobot.system.targetSystem.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystem.SC_ClaimTargetInfo;

@MsgId(msgId = MsgIdEnum.SC_ClaimTargetInfo_VALUE)
public class ClaimTargetInfoHandler extends AbstractHandler<SC_ClaimTargetInfo> {
    @Override
    protected SC_ClaimTargetInfo parse(byte[] bytes) throws Exception {
        return SC_ClaimTargetInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimTargetInfo result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
            robotByChannel.getData().addAllDailyMission(result.getMissionProList());
            robotByChannel.getData().addAllAchievementPro(result.getAchievementList());
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
