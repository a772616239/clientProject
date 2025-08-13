package petrobot.system.bravechallenge.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import protocol.BraveChallenge.SC_BraveChallengeInit;
import protocol.MessageId.MsgIdEnum;

/**
 * @author xiao_FL
 * @date 2019/12/23
 */
@MsgId(msgId = MsgIdEnum.SC_BraveChallengeInit_VALUE)
public class BraveChallengeInitHandler extends AbstractHandler<SC_BraveChallengeInit> {

    @Override
    protected SC_BraveChallengeInit parse(byte[] bytes) throws Exception {
        return SC_BraveChallengeInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_BraveChallengeInit scBraveChallengeInit, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gameServerTcpChannel.channel);
        robot.getData().setBraveChallengeProgress(scBraveChallengeInit.getProgressMsg());
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }
}
