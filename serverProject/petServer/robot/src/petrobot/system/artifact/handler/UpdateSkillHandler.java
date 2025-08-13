package petrobot.system.artifact.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import protocol.PlayerInfo;

public class UpdateSkillHandler  extends AbstractHandler<PlayerInfo.SC_ArtifactUpdate> {

    @Override
    protected PlayerInfo.SC_ArtifactUpdate parse(byte[] bytes) throws Exception {
        return PlayerInfo.SC_ArtifactUpdate.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, PlayerInfo.SC_ArtifactUpdate result, int i) {

    }
}
