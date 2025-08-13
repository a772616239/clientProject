package petrobot.system.patrol.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.SC_PatrolFinish;

@MsgId(msgId = MsgIdEnum.SC_PatrolFinish_VALUE)
public class PatrolFinishHandler extends AbstractHandler<SC_PatrolFinish> {

    @Override
    protected SC_PatrolFinish parse(byte[] bytes) throws Exception {
        return SC_PatrolFinish.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PatrolFinish scPatrolInit, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gameServerTcpChannel.channel);
        robot.getData().setPatrolFinish(false);
    }
}
