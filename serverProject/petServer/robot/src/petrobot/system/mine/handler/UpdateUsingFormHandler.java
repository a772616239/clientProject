package petrobot.system.mine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.SC_UpdateUsingPetForm;

@MsgId(msgId = MsgIdEnum.SC_UpdateUsingPetForm_VALUE)
public class UpdateUsingFormHandler extends AbstractHandler<SC_UpdateUsingPetForm> {
    @Override
    protected SC_UpdateUsingPetForm parse(byte[] bytes) throws Exception {
        return SC_UpdateUsingPetForm.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_UpdateUsingPetForm ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            r.getData().getMineInfo().getOwnedMineInfo().clear();
            for (int index = 0; index < ret.getUsingPetForms().getKeysCount(); index++) {
                int formIndex = ret.getUsingPetForms().getKeys(index);
                String mineIdx = ret.getUsingPetForms().getValues(index);
                r.getData().getMineInfo().getOwnedMineInfo().put(formIndex, mineIdx);
            }
        });
    }
}
