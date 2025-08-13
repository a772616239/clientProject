package petrobot.system.mistForest.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_JoinMistForest;

@MsgId(msgId = MsgIdEnum.SC_JoinMistForest_VALUE)
public class JoinMistForestHandler extends AbstractHandler<SC_JoinMistForest> {
    @Override
    protected SC_JoinMistForest parse(byte[] bytes) throws Exception {
        return SC_JoinMistForest.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_JoinMistForest ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
        if (ret.getRetCode() == MistRetCode.MRC_Success) {
            LogUtil.info("robot join mistforest suceess,name=" + robot.getLoginName());
        } else {
            LogUtil.info("robot join mistforest failed,name=" + robot.getLoginName() + ",reason=" + ret.getRetCode());
        }
    }
}
