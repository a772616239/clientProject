package petrobot.system.player.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.SC_AddAvatar;
import protocol.PlayerInfo.SC_PlayerBaseInfo.Builder;

@MsgId(msgId = MsgIdEnum.SC_AddAvatar_VALUE)
public class AddAvatarHandler extends AbstractHandler<SC_AddAvatar> {
    @Override
    protected SC_AddAvatar parse(byte[] bytes) throws Exception {
        return SC_AddAvatar.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_AddAvatar result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
            Builder baseInfo = robotByChannel.getData().getBaseInfo();
            if (baseInfo == null ) {
                return;
            }
            for (Integer integer : result.getAvatarIdList()) {
                if (!baseInfo.getOwnedAvatarList().contains(integer)) {
                    baseInfo.addOwnedAvatar(integer);
                }
            }
        });
    }
}
