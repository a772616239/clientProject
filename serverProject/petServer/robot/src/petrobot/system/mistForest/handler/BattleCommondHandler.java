package petrobot.system.mistForest.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.system.mistForest.RobotMistForest;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.SC_BattleCmd;

@MsgId(msgId = MsgIdEnum.SC_BattleCmd_VALUE)
public class BattleCommondHandler extends AbstractHandler<SC_BattleCmd> {
    @Override
    protected SC_BattleCmd parse(byte[] bytes) throws Exception {
        return SC_BattleCmd.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_BattleCmd ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            RobotMistForest mistManager = r.getData().getRobotMistForest();
            if (mistManager == null) {
                return;
            }
            mistManager.HandleMistCmd(ret);
        });
    }
}
