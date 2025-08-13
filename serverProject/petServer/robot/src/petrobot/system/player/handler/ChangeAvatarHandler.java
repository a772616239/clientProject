package petrobot.system.player.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.SC_ChangeAvatar;

@MsgId(msgId = MsgIdEnum.SC_ChangeAvatar_VALUE)
public class ChangeAvatarHandler extends AbstractHandler<SC_ChangeAvatar> {
    @Override
    protected SC_ChangeAvatar parse(byte[] bytes) throws Exception {
        return SC_ChangeAvatar.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ChangeAvatar result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robotByChannel, t -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
