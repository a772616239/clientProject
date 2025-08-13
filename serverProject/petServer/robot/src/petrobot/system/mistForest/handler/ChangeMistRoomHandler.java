package petrobot.system.mistForest.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import protocol.MistForest.SC_ExchangeMistForest;

public class ChangeMistRoomHandler extends AbstractHandler<SC_ExchangeMistForest> {
    @Override
    protected SC_ExchangeMistForest parse(byte[] bytes) throws Exception {
        return SC_ExchangeMistForest.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ExchangeMistForest ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
    }
}
