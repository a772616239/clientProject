package petrobot.system.team.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.SC_ClaimTeamsInfo;

@MsgId(msgId = MsgIdEnum.SC_ClaimTeamsInfo_VALUE)
public class ClaimTeamInfoHandler extends AbstractHandler<SC_ClaimTeamsInfo> {
    @Override
    protected SC_ClaimTeamsInfo parse(byte[] bytes) throws Exception {
        return SC_ClaimTeamsInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimTeamsInfo result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        }
    }
}
