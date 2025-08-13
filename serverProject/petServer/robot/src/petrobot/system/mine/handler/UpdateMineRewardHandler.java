package petrobot.system.mine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.system.mine.RobotMine;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.MineRewardInfo;
import protocol.MineFight.SC_UpdateMineRewards;

@MsgId(msgId = MsgIdEnum.SC_UpdateMineRewards_VALUE)
public class UpdateMineRewardHandler extends AbstractHandler<SC_UpdateMineRewards> {
    @Override
    protected SC_UpdateMineRewards parse(byte[] bytes) throws Exception {
        return SC_UpdateMineRewards.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_UpdateMineRewards ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            if (r.getData().getMineInfo()==null){
                r.getData().setMineInfo(new RobotMine(r));
            }
            r.getData().getMineInfo().getMineRewardMap().clear();
            for (MineRewardInfo mineReward : ret.getRewardDataList()) {
                r.getData().getMineInfo().getMineRewardMap().put(mineReward.getFromIndex(), mineReward);
            }
        });
    }
}
