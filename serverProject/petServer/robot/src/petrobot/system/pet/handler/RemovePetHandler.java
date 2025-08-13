package petrobot.system.pet.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.Pet;
import protocol.PetMessage.SC_PetBagInit.Builder;
import protocol.PetMessage.SC_PetRemove;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author hanx
 * @Date2020/10/23 0023 17:29
 **/
@MsgId(msgId = MsgIdEnum.SC_PetRemove_VALUE)
public class RemovePetHandler extends AbstractHandler<SC_PetRemove> {
    @Override
    protected SC_PetRemove parse(byte[] bytes) throws Exception {
        return SC_PetRemove.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_PetRemove result, int ii) {

        if (result.getIdCount() <= 0) {
            return;
        }

        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, r -> {
            if (robot.getData().getPetBag() == null) {
                return;
            }
            Builder petBag = robot.getData().getPetBag().toBuilder();
            List<Pet> collect = petBag.getPetList().stream().filter(pet -> !result.getIdList().contains(pet.getId())).collect(Collectors.toList());
            petBag.clearPet().addAllPet(collect);
            robot.getData().setPetBag(petBag.build());
        });
    }

}
