package petrobot.system.mine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.CS_ResponseMinePvpFight;
import protocol.MineFight.SC_RequestMinePvpFight;

@MsgId(msgId = MsgIdEnum.SC_RequestMinePvpFight_VALUE)
public class ReqMineBattleHandler extends AbstractHandler<SC_RequestMinePvpFight> {
    @Override
    protected SC_RequestMinePvpFight parse(byte[] bytes) throws Exception {
        return SC_RequestMinePvpFight.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RequestMinePvpFight ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        CS_ResponseMinePvpFight.Builder builder = CS_ResponseMinePvpFight.newBuilder();
        builder.setMineId(ret.getMineId());
        builder.setAcceptBattle(false);
        robot.getClient().send(MsgIdEnum.CS_ResponseMinePvpFight_VALUE, builder);
    }
}
