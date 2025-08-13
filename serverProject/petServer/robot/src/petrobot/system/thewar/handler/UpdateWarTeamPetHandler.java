package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWar.SC_UpdateWarTeamPet;

@MsgId(msgId = MsgIdEnum.SC_UpdateWarTeamPet_VALUE)
public class UpdateWarTeamPetHandler extends AbstractHandler<SC_UpdateWarTeamPet> {
    @Override
    protected SC_UpdateWarTeamPet parse(byte[] bytes) throws Exception {
        return SC_UpdateWarTeamPet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_UpdateWarTeamPet ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
