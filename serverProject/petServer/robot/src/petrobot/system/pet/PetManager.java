package petrobot.system.pet;

import cfg.PetBagConfig;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import org.apache.commons.collections4.CollectionUtils;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import protocol.GM.CS_GM;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetBagEnlarge;
import protocol.PetMessage.CS_PetBagInit;
import protocol.PetMessage.CS_PetDisCharge;
import protocol.PetMessage.CS_PetDisCharge.Builder;
import protocol.PetMessage.CS_PetLvlUp;
import protocol.PetMessage.CS_PetRarityUp;
import protocol.PetMessage.Pet;
import protocol.PetMessage.PetRarityUp;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author xiao_FL
 * @date 2019/12/16
 */
@Controller
public class PetManager {

    @Index(IndexConst.PET_BAG_INIT)
    public void petBagInit(Robot robot) {
        robot.getClient().send(MsgIdEnum.CS_PetBagInit_VALUE, CS_PetBagInit.newBuilder());
    }

    @Index(IndexConst.PET_BAG_ENLARGE)
    public void petBagEnlarge(Robot robot) {
        if (robot.getData().getPetBag().getEnlargeTime() < PetBagConfig._ix_enlargetime.size()) {
            robot.getClient().send(MsgIdEnum.CS_GM_VALUE, CS_GM.newBuilder().setStr("addDiamond|" + PetBagConfig.enlargeWholePetBagDiamond));
            CS_PetBagEnlarge.Builder builder = CS_PetBagEnlarge.newBuilder().setBagType(0);
            robot.getClient().send(MsgIdEnum.CS_PetBagEnlarge_VALUE, builder);
        }
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }

    @Index(IndexConst.PET_DISCHARGE)
    public void petDischarge(Robot robot) {
        if ((robot.getData().getPetBag().getPetList().size() / (double) robot.getData().getPetBag().getCapacity()) > 0.85) {
            Builder builder = CS_PetDisCharge.newBuilder();
            for (Pet pet : robot.getData().getPetBag().getPetList()) {
                int cfgId = pet.getPetBookId();
                if (pet.getPetLvl() == 1) {
                    PetBasePropertiesObject cfg = PetBaseProperties.getByPetid(cfgId);
                    int rarity = cfg.getStartrarity();
                    if (rarity == 3 || rarity == 4) {
                        builder.addId(pet.getId());
                    }
                }
            }
            robot.getClient().send(MsgIdEnum.CS_PetDisCharge_VALUE, builder);
        }
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }

    @Index(IndexConst.PET_LVL_UP)
    public void petLvlUp(Robot robot) {
        int playerLv = robot.getData().getBaseInfo().getLevel();
        for (Pet pet : robot.getData().getPetBag().getPetList()) {
            if (pet.getPetLvl() < playerLv) {
                robot.getClient().send(MsgIdEnum.CS_PetLvlUp_VALUE, CS_PetLvlUp.newBuilder().setPetId(pet.getId()).setUpType(0));
                break;
            }
        }
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }


    @Index(IndexConst.PET_Rarity_Up)
    public void petRarityUp(Robot robot) {
        //一键进阶只支持品质为3的宠物

        List<Pet> petList = robot.getData().getPetBag().getPetList();

        if (CollectionUtils.isEmpty(petList)) {
            return;
        }

        Map<Integer, List<Pet>> collect = petList.stream().filter(pet -> pet.getPetRarity() == 3)
                .collect(Collectors.groupingBy(Pet::getPetBookId));

        CS_PetRarityUp.Builder req = CS_PetRarityUp.newBuilder();

        for (Entry<Integer, List<Pet>> entry : collect.entrySet()) {
            List<Pet> pets = entry.getValue();
            if (pets.size() < 3) {
                continue;
            }

            for (int index = 0; index < pets.size() / 3; index++) {
                if (req.getUpListCount() >= 5) {
                    break;
                }
                PetRarityUp.Builder item = PetRarityUp.newBuilder();
                item.setPetId(pets.get(index * 3).getId());
                item.addMaterialPets(pets.get(index * 3 + 1).getId());
                item.addMaterialPets(pets.get(index * 3 + 2).getId());
                req.addUpList(item.build());
            }

        }

        if (req.getUpListCount() > 0) {
            robot.getClient().send(MsgIdEnum.CS_PetRarityUp_VALUE, req);
        }

        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }
}
