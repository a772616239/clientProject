package petrobot.system.petfragment;

import cfg.PetFragmentConfig;
import cfg.PetFragmentConfigObject;
import org.apache.commons.lang.math.RandomUtils;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import protocol.GM.CS_GM;
import protocol.GM.CS_GM.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetFragmentAllUse;
import protocol.PetMessage.CS_PetFragmentInit;
import protocol.PetMessage.CS_PetFragmentUse;
import protocol.PetMessage.PetFragment;

/**
 * @author xiao_FL
 * @date 2019/12/17
 */
@Controller
public class PetFragmentManager {

    @Index(IndexConst.PET_FRAGMENT_INIT)
    public void petFragmentInit(Robot robot) {
        robot.getClient().send(MsgIdEnum.CS_PetFragmentInit_VALUE, CS_PetFragmentInit.newBuilder());
    }

    @Index(IndexConst.PET_FRAGMENT_USE)
    public void useFragment(Robot robot) {
        if (robot.getData().getPetBag().getPetCount() > 80) {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            return;
        }
        if (robot.getData().getPetFragmentList().size() <= 0) {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        }

        int index = RandomUtils.nextInt(robot.getData().getPetFragmentList().size());
        PetFragment petFragment = robot.getData().getPetFragmentList().get(index);
        int cfgId = petFragment.getCfgId();
        PetFragmentConfigObject cfg = PetFragmentConfig.getById(cfgId);
        if (cfg == null) {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            return;
        }

        int petNum = petFragment.getNumber() / cfg.getAmount();
        if (petFragment.getNumber() <= 50) {
            Builder builder = CS_GM.newBuilder();
            builder.setStr("petFragmentSpecify|" + petFragment.getCfgId() + "|50");
            robot.getClient().send(MsgIdEnum.CS_GM_VALUE, builder);
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            petNum += 50 / cfg.getAmount();
        }

        if (petNum > 1) {
            CS_PetFragmentUse.Builder builder = CS_PetFragmentUse.newBuilder().setId(petFragment.getId()).setAmount(petNum);
            robot.getClient().send(MsgIdEnum.CS_PetFragmentUse_VALUE, builder);
        } else {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        }

    }


    @Index(IndexConst.PET_FRAGMENT_USE_ALL)
    public void useAllFragment(Robot robot) {
        if (robot.getData().getPetBag().getPetCount() > 80) {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            return;
        }
        if (robot.getData().getPetFragmentList().size() > 0) {
            CS_PetFragmentAllUse.Builder builder = CS_PetFragmentAllUse.newBuilder();
            robot.getClient().send(MsgIdEnum.CS_PetFragmentAllUse_VALUE, builder);
        } else {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        }
    }

}
