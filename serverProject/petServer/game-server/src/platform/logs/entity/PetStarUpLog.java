package platform.logs.entity;

import cfg.PetBaseProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.ConsumeLog;
import platform.logs.LogClass.PetLog;
import protocol.Common.Consume;
import protocol.PetMessage.Pet;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PetStarUpLog extends AbstractPlayerLog {
    private int originalRarity;
    private int nowRarity;
    private String petId;
    private String petName;
    List<ConsumeLog> consumeList;
    List<PetLog> consumePetList;

    public PetStarUpLog(String playerId, Pet pet) {
        super(playerId);
        if (pet == null) {
            return;
        }
        this.nowRarity = pet.getPetRarity();
        this.petId = pet.getId();
        this.petName = PetBaseProperties.getNameById(pet.getPetBookId());
    }

    public PetStarUpLog(String playerId, Pet.Builder pet, int originalRarity, List<Consume> consumeList, List<Pet> consumePetList) {
        super(playerId);
        if (pet == null) {
            return;
        }
        this.nowRarity = pet.getPetRarity();
        this.petId = pet.getId();
        this.originalRarity = originalRarity;
        this.petName = PetBaseProperties.getNameById(pet.getPetBookId());
        if (consumeList != null && consumeList.size() > 0) {
            List<ConsumeLog> consumeLogList = new ArrayList<>();
            for (Consume consume : consumeList) {
                consumeLogList.add(new ConsumeLog(consume));
            }
            this.consumeList = consumeLogList;
        }
        if (consumePetList != null && consumePetList.size() > 0) {
            List<PetLog> petLogList = new ArrayList<>();
            for (Pet consumePet : consumePetList) {
                petLogList.add(new PetLog(consumePet));
            }
            this.consumePetList = petLogList;
        }
    }
}
