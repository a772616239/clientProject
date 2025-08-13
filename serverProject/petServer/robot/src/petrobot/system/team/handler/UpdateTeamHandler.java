package petrobot.system.team.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.SC_UpdateTeam;

@MsgId(msgId = MsgIdEnum.SC_UpdateTeam_VALUE)
public class UpdateTeamHandler extends AbstractHandler<SC_UpdateTeam> {
    @Override
    protected SC_UpdateTeam parse(byte[] bytes) throws Exception {
        return SC_UpdateTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_UpdateTeam result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS));

            LogUtil.debug("robot[" + robotByChannel.getId() + "]UpdateTeam result=" + result.getRetCode().getRetCode());
        }
    }
}
