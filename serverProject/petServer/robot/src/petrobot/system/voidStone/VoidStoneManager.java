package petrobot.system.voidStone;

import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robotConst.DealResultConst;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_VoidStoneConvert;
import protocol.PetMessage.CS_VoidStoneConvertSave;
import protocol.PetMessage.CS_VoidStoneLvUp;
import protocol.PetMessage.CS_VoidStoneUnLock;
import protocol.PetMessage.Pet;

/**
 * @author xiao_FL
 * @date 2019/12/16
 */
@Controller
public class VoidStoneManager {

   // @Index(IndexConst.PetVoidStone)
    public void petRarityUp(Robot robot) {
        for (Pet pet : robot.getData().getPetBag().getPetList()) {
            //解锁
            if (pet.getVoidStoneId() == 0 && pet.getPetLvl() >= 30) {
                robot.getClient().send(MsgIdEnum.CS_VoidStoneUnLock_VALUE, CS_VoidStoneUnLock.newBuilder().setPetId(pet.getId()));
            }
            if (pet.getVoidStoneId() != 0) {
                //升级
                robot.getClient().send(MsgIdEnum.CS_VoidStoneLvUp_VALUE, CS_VoidStoneLvUp.newBuilder().setPetId(pet.getId()));
                //属性转换
                robot.getClient().send(MsgIdEnum.CS_VoidStoneConvert_VALUE, CS_VoidStoneConvert.newBuilder().setPetId(pet.getId()));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                robot.getClient().send(MsgIdEnum.CS_VoidStoneConvert_VALUE, CS_VoidStoneConvertSave.newBuilder().setPetId(pet.getId()));
            }
        }
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }

}
