package petrobot.system.mine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.MineGiftObj;
import protocol.MineFight.SC_GenerateMineGift;

@MsgId(msgId = MsgIdEnum.SC_GenerateMineGift_VALUE)
public class UpdateMineGiftHandler extends AbstractHandler<SC_GenerateMineGift> {
    @Override
    protected SC_GenerateMineGift parse(byte[] bytes) throws Exception {
        return SC_GenerateMineGift.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_GenerateMineGift ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            r.getData().getMineInfo().getMineGiftObjMap().clear();
            for (MineGiftObj mineGift : ret.getMineGiftObjList()) {
                r.getData().getMineInfo().getMineGiftObjMap().put(mineGift.getGiftId(), mineGift);
            }
        });

    }
}
