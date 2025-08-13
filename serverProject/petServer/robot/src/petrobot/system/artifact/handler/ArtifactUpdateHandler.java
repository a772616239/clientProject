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

@MsgId(msgId = MessageId.MsgIdEnum.SC_ArtifactUpdate_VALUE)
public class ArtifactUpdateHandler extends AbstractHandler<PlayerInfo.SC_ArtifactUpdate> {

    @Override
    protected PlayerInfo.SC_ArtifactUpdate parse(byte[] bytes) throws Exception {
        return PlayerInfo.SC_ArtifactUpdate.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, PlayerInfo.SC_ArtifactUpdate result, int i) {

    }
}
