package petrobot.system.petmission;

import cfg.PetBaseProperties;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.AcceptedPetMission;
import protocol.PetMessage.CS_PetBagInit;
import protocol.PetMessage.CS_PetMissionAccept;
import protocol.PetMessage.CS_PetMissionAccept.Builder;
import protocol.PetMessage.CS_PetMissionComplete;
import protocol.PetMessage.CS_PetMissionInit;
import protocol.PetMessage.Pet;
import protocol.PetMessage.PetMission;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/12/17
 */
@Controller
public class PetMissionManager {
    @Index(IndexConst.PET_MISSION_INIT)
    public void petMissionInit(Robot robot) {
        robot.getClient().send(MsgIdEnum.CS_PetMissionInit_VALUE, CS_PetMissionInit.newBuilder());
    }

    @Index(IndexConst.PET_MISSION_COMPLETE)
    public void petMissionComplete(Robot robot) {
        List<AcceptedPetMission> acceptedPetMissionList = robot.getData().getAcceptedPetMissionList();
        for (AcceptedPetMission acceptedPetMission : acceptedPetMissionList) {
            robot.getClient().send(MsgIdEnum.CS_PetMissionComplete_VALUE, CS_PetMissionComplete.newBuilder().setMissionId(acceptedPetMission.getMissionId()));
        }
        robot.getClient().send(MsgIdEnum.CS_PetBagInit_VALUE, CS_PetBagInit.newBuilder());
        robot.getClient().send(MsgIdEnum.CS_PetMissionInit_VALUE, CS_PetMissionInit.newBuilder());
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }

    @Index(IndexConst.PET_MISSION_ACCEPT)
    public void petAcceptMission(Robot robot) {
        for (PetMission petMission : robot.getData().getPetMissionList()) {
            int petNeedCount = 0;
            Builder builder = CS_PetMissionAccept.newBuilder();
            if (petMission.getRequiredPetRarity() != 0) {
                petNeedCount++;
                for (Pet pet : robot.getData().getPetBag().getPetList()) {
                    if (pet.getPetMissionStatus() == 0 && PetBaseProperties.getByPetid(pet.getPetBookId()).getStartrarity() > petMission.getRequiredPetRarity()) {
                        builder.addPetId(pet.getId());
                    }
                }
                if (petNeedCount != builder.getPetIdCount()) {
                    break;
                }
            }
            if (petMission.getRequiredPetType() != 0) {
                petNeedCount++;
                for (Pet pet : robot.getData().getPetBag().getPetList()) {
                    if (pet.getPetMissionStatus() == 0 && PetBaseProperties.getByPetid(pet.getPetBookId()).getPettype() > petMission.getRequiredPetType()) {
                        builder.addPetId(pet.getId());
                    }
                }
                if (petNeedCount != builder.getPetIdCount()) {
                    break;
                }
            }
            if (petMission.getRequiredPetRarity() != 0) {
                petNeedCount++;
                for (Pet pet : robot.getData().getPetBag().getPetList()) {
                    if (pet.getPetMissionStatus() == 0 && pet.getPetRarity() > petMission.getRequiredPetRarity()) {
                        builder.addPetId(pet.getId());
                    }
                }
                if (petNeedCount != builder.getPetIdCount()) {
                    break;
                }
            }
            builder.setMissionId(petMission.getMissionId());
            robot.getClient().send(MsgIdEnum.CS_PetMissionAccept_VALUE, builder);
        }
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }
}
