package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWarDefine.SC_JobTileTaskData;

@MsgId(msgId = MsgIdEnum.SC_JobTileTaskData_VALUE)
public class UpdateJobTileHandler extends AbstractHandler<SC_JobTileTaskData> {
    @Override
    protected SC_JobTileTaskData parse(byte[] bytes) throws Exception {
        return SC_JobTileTaskData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_JobTileTaskData ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
    }
}
