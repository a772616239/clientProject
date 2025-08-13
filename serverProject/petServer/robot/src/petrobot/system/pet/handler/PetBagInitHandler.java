package petrobot.system.pet.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetBagInit;
import protocol.RetCodeId.RetCodeEnum;

/**
 * @author xiao_FL
 * @date 2019/12/16
 */
@MsgId(msgId = MsgIdEnum.SC_PetBagInit_VALUE)
public class PetBagInitHandler extends AbstractHandler<SC_PetBagInit> {
    @Override
    protected SC_PetBagInit parse(byte[] bytes) throws Exception {
        return SC_PetBagInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PetBagInit scPetBagInit, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gameServerTcpChannel.channel);
        if (robot == null) {
            return;

        }
        //只要前3页的宠物
        if (scPetBagInit.getPageNum() > 3) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, r -> {
            if (scPetBagInit.getResult().getRetCode() == RetCodeEnum.RCE_Success) {
                SC_PetBagInit existPetBag = robot.getData().getPetBag();
                if (existPetBag == null) {
                    r.getData().setPetBag(scPetBagInit);

                } else if (existPetBag.getPageNum() < scPetBagInit.getPageNum()) {
                    SC_PetBagInit newBag = existPetBag.toBuilder().addAllPet(scPetBagInit.getPetList()).setPageNum(scPetBagInit.getPageNum()).build();
                    robot.getData().setPetBag(newBag);
                }
            }
            r.setDealResult(DealResultConst.CUR_STEP_SUCCESS);

        });

    }
}
