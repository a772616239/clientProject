package petrobot.system.patrol.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.SC_PatrolIfFinish;

/**
 * @author xiao_FL
 * @date 2019/12/23
 */
@MsgId(msgId = MsgIdEnum.SC_PatrolIfFinish_VALUE)
public class PatrolFinishCheckHandler extends AbstractHandler<SC_PatrolIfFinish> {
    @Override
    protected SC_PatrolIfFinish parse(byte[] bytes) throws Exception {
        return SC_PatrolIfFinish.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PatrolIfFinish scPatrolIfFinish, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gameServerTcpChannel.channel);
        robot.getData().setPatrolFinish(scPatrolIfFinish.getFinish() == 1);
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }
}
