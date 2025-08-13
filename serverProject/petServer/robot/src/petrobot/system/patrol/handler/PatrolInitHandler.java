package petrobot.system.patrol.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.SC_PatrolInit;

/**
 * @author xiao_FL
 * @date 2019/12/23
 */
@MsgId(msgId = MsgIdEnum.SC_PatrolInit_VALUE)
public class PatrolInitHandler extends AbstractHandler<SC_PatrolInit> {

    @Override
    protected SC_PatrolInit parse(byte[] bytes) throws Exception {
        return SC_PatrolInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PatrolInit scPatrolInit, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gameServerTcpChannel.channel);
        robot.getData().setPatrolMap(scPatrolInit.getMap());
        robot.getData().setPatrolStatus(scPatrolInit.getStatus());
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }
}
