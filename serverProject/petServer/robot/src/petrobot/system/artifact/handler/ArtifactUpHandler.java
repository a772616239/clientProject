package petrobot.system.artifact.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.LogUtil;
import protocol.MessageId;
import protocol.PlayerInfo;
import protocol.RetCodeId;

@MsgId(msgId = MessageId.MsgIdEnum.SC_ArtifactUp_VALUE)
public class ArtifactUpHandler extends AbstractHandler<PlayerInfo.SC_ArtifactUp> {

    @Override
    protected PlayerInfo.SC_ArtifactUp parse(byte[] bytes) throws Exception {
        return PlayerInfo.SC_ArtifactUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, PlayerInfo.SC_ArtifactUp resutl, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gameServerTcpChannel.channel);
        if (resutl.getRetCode().getRetCode() != RetCodeId.RetCodeEnum.RCE_Success) {
            LogUtil.warn("artifact Up failed :" + resutl.getRetCode().getRetCode());
        }
        if (robot != null) {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        }
    }
}
