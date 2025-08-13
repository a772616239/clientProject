package petrobot.system.mine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.SC_OccupyMine;
import protocol.RetCodeId.RetCodeEnum;

@MsgId(msgId = MsgIdEnum.SC_OccupyMine_VALUE)
public class OccupyMineHandler extends AbstractHandler<SC_OccupyMine> {
    @Override
    protected SC_OccupyMine parse(byte[] bytes) throws Exception {
        return SC_OccupyMine.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_OccupyMine ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
        if (ret.getRetCode().getRetCode() == RetCodeEnum.RCE_Success) {
            LogUtil.info("robot occupy mine success,name=" + robot.getLoginName());
        } else {
            LogUtil.info("robot occupy mine failed,name=" + robot.getLoginName() + ",ret=" + ret.getRetCode().getRetCode());
        }
    }
}
