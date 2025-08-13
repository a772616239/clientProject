package petrobot.system.player.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.LoginProto.SC_Login;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_Login_VALUE)
public class LoginHandler extends AbstractHandler<SC_Login> {
    @Override
    protected SC_Login parse(byte[] bytes) throws Exception {
        return SC_Login.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_Login req, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            r.addInitRes();
            r.setOnline(true);
        });
        LogUtil.info("robot login success userId=" + robot.getUserId());
    }
}
