package petrobot.system.arena.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import protocol.Arena.CS_ClaimArenaInfo;
import protocol.Arena.SC_ArenaDanSettle;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/06/05
 */
@MsgId(msgId = MsgIdEnum.SC_ArenaDanSettle_VALUE)
public class ArenaDanSettleHandler extends AbstractHandler<SC_ArenaDanSettle> {
    @Override
    protected SC_ArenaDanSettle parse(byte[] bytes) throws Exception {
        return SC_ArenaDanSettle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ArenaDanSettle req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }
        robotByChannel.getClient().send(MsgIdEnum.CS_ClaimArenaInfo_VALUE, CS_ClaimArenaInfo.newBuilder());
    }
}
