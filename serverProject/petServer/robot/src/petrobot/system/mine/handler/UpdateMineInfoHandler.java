package petrobot.system.mine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.MineInfo;
import protocol.MineFight.SC_UpdateMineInfo;

@MsgId(msgId = MsgIdEnum.SC_UpdateMineInfo_VALUE)
public class UpdateMineInfoHandler extends AbstractHandler<SC_UpdateMineInfo> {
    @Override
    protected SC_UpdateMineInfo parse(byte[] bytes) throws Exception {
        return SC_UpdateMineInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_UpdateMineInfo ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            for (MineInfo mineInfo : ret.getUpdateMinesList()) {
                r.getData().getMineInfo().getMineInfoMap().put(mineInfo.getId(), mineInfo.toBuilder());
            }
        });

    }
}
