package petrobot.system.player.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.GM.CS_GM;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.SC_PlayerBaseInfo;

@MsgId(msgId = MsgIdEnum.SC_PlayerBaseInfo_VALUE)
public class PlayerBaseInfoHandler extends AbstractHandler<SC_PlayerBaseInfo> {
    @Override
    protected SC_PlayerBaseInfo parse(byte[] bytes) throws Exception {
        return SC_PlayerBaseInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_PlayerBaseInfo result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
            robotByChannel.getData().setBaseInfo(result.toBuilder());
            if (robotByChannel.getData().getBaseInfo().getLevel() < 20) {
                robotByChannel.getClient().send(MsgIdEnum.CS_GM_VALUE, CS_GM.newBuilder().setStr("addExp|200000"));
            }
            LogUtil.debug("robot id =" + robotByChannel.getId() + "LogIn success");
        });


    }
}
