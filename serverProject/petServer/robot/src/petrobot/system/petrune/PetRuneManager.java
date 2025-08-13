package petrobot.system.petrune;

import cfg.PetRuneProperties;
import cfg.PetRunePropertiesObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import petrobot.util.LogUtil;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiao_FL
 * @date 2019/12/17
 */
@Controller
public class PetRuneManager {

    @Index(IndexConst.PET_RUNE_BAG_INIT)
    public void petRuneBagInit(Robot robot) {
        robot.getClient().send(MsgIdEnum.CS_PetRuneBagInit_VALUE, CS_PetRuneBagInit.newBuilder());
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }

    @Index(IndexConst.PET_RUNE_UN_EQUIP)
    public void petRuneUnEquip(Robot robot) {
        List<Rune> petRuneList = robot.getData().getPetRuneList();

        Set<String> runeIds = new HashSet<>();

        for (Rune rune : petRuneList) {
            if (StringUtils.isNotEmpty(rune.getRunePet())) {
                runeIds.add(rune.getId());
            }
            if (runeIds.size() > 4) {
                break;
            }

        }
        robot.getClient().send(MsgIdEnum.CS_PetRuneUnEquip_VALUE, CS_PetRuneUnEquip.newBuilder().addAllRuneId(runeIds));
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }

    @Index(IndexConst.PET_RUNE_LVL_UP)
    public void petRuneLvlUp(Robot robot) {
        List<Rune> petRuneList = robot.getData().getPetRuneList();
        sortRuneList(petRuneList);
        CS_PetRuneLvlUp.Builder builder = CS_PetRuneLvlUp.newBuilder();
        int maxRarity = 0;
        for (Rune rune : petRuneList) {
            if (rune.getRuneLvl() < 15 && StringUtils.isNotBlank(builder.getUpRuneId())) {
                builder.setUpRuneId(rune.getId());
                maxRarity = PetRuneProperties.getByRuneid(rune.getRuneBookId()).getRunerarity();
                continue;
            }
            if (builder.getMaterialRuneCount() > 5) {
                break;
            }
            if (PetRuneProperties.getByRuneid(rune.getRuneBookId()).getRunerarity() < maxRarity) {
                builder.addMaterialRune(rune.getId());
            }
        }
        if (builder.getMaterialRuneCount() <= 0) {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_PetRuneLvlUp_VALUE, builder);
        robot.getClient().send(MsgIdEnum.CS_PetRuneBagInit_VALUE, CS_PetRuneBagInit.newBuilder());
    }

    @Index(IndexConst.PET_RUNE_EQUIP)
    public void petRuneEquip(Robot robot) {
        List<Pet> petList = new ArrayList<>(robot.getData().getPetBag().getPetList());
        List<Rune> petRuneList = robot.getData().getPetRuneList().stream().filter(rune -> StringUtils.isEmpty(rune.getRunePet())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(petRuneList) || CollectionUtils.isEmpty(petList)) {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            return;
        }
        sortPetList(petList);
        sortRuneList(petRuneList);

        Random random = new Random();
        int operateIndex = random.nextInt(Math.min(10, petList.size()));
        Pet pet = petList.get(operateIndex);


        List<Integer> equipRuneType = robot.getData().getPetRuneList().stream().filter(rune -> rune.getRunePet().equals(pet.getId()))
                .map(rune -> PetRuneProperties.getByRuneid(rune.getRuneBookId()).getRunetype()).collect(Collectors.toList());

        List<String> runeIds = new ArrayList<>();
        for (Rune rune : petRuneList) {
            if (equipRuneType.size() >= 4) {
                break;
            }
            int runeType = PetRuneProperties.getByRuneid(rune.getRuneBookId()).getRunetype();
            if (!equipRuneType.contains(runeType)) {
                runeIds.add(rune.getId());
                equipRuneType.add(runeType);
            }
        }
        if (CollectionUtils.isEmpty(runeIds)) {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_PetRuneEquip_VALUE, CS_PetRuneEquip.newBuilder().setPetId(pet.getId()).addAllRuneId(runeIds));

    }


    private void sortRuneList(List<Rune> runes) {
        runes.sort((o1, o2) -> {
            int compare = getRuneRarity(o2.getRuneBookId()) - getRuneRarity(o1.getRuneBookId());
            return compare != 0 ? compare : o2.getRuneLvl() - o1.getRuneLvl();

        });
    }

    public int getRuneRarity(int bookId) {
        PetRunePropertiesObject thisRune = PetRuneProperties.getByRuneid(bookId);
        if (thisRune == null) {
            LogUtil.error("getRuneRarity() cant`t find PetRuneProperties by rune book id:" + bookId);
            return 0;
        }
        return thisRune.getRunerarity();
    }

    private void sortPetList(List<Pet> pets) {
        pets.sort((pet1, pet2) -> {
            int compare = pet2.getPetRarity() - pet1.getPetRarity();
            return compare == 0 ? pet2.getPetRarity() - pet1.getPetRarity() : compare;
        });
    }
}
