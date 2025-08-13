package petrobot.system.petfragment.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.PetFragment;
import protocol.PetMessage.SC_PetFragmetUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author xiao_FL
 * @date 2020/1/6
 */
@MsgId(msgId = MsgIdEnum.SC_PetFragmetUpdate_VALUE)
public class PetFragmentUpdateHandler extends AbstractHandler<SC_PetFragmetUpdate> {
    @Override
    protected SC_PetFragmetUpdate parse(byte[] bytes) throws Exception {
        return SC_PetFragmetUpdate.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_PetFragmetUpdate msg, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            List<PetFragment> updateFragmentList = msg.getFragmentList();

            List<PetFragment> nowFragmentList = new ArrayList<>();
            List<PetFragment> petFragmentList = robot.getData().getPetFragmentList();
            for (PetFragment petFragment : petFragmentList) {
                Optional<PetFragment> exist = updateFragmentList.stream().filter(f -> f.getCfgId() == petFragment.getCfgId()).findFirst();
                if (exist.isPresent()) {
                    nowFragmentList.add(exist.get());
                } else {
                    nowFragmentList.add(petFragment);
                }

            }
            robot.getData().setPetFragmentList(nowFragmentList);

        });

    }
}
