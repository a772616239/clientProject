package petrobot.system.player.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.SC_AlterName;
import protocol.PlayerInfo.SC_PlayerBaseInfo.Builder;

@MsgId(msgId = MsgIdEnum.SC_AlterName_VALUE)
public class AlterNameHandler extends AbstractHandler<SC_AlterName> {
    @Override
    protected SC_AlterName parse(byte[] bytes) throws Exception {
        return SC_AlterName.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_AlterName result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                Builder baseInfo = robotByChannel.getData().getBaseInfo();
                if (baseInfo != null) {
                    baseInfo.setNextRenameTime(result.getNextRenameTime());
                }
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
