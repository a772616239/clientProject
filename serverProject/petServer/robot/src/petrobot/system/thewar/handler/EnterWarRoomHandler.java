package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWar.SC_EnterTheWar;

@MsgId(msgId = MsgIdEnum.SC_EnterTheWar_VALUE)
public class EnterWarRoomHandler extends AbstractHandler<SC_EnterTheWar> {
    @Override
    protected SC_EnterTheWar parse(byte[] bytes) throws Exception {
        return SC_EnterTheWar.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_EnterTheWar ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
