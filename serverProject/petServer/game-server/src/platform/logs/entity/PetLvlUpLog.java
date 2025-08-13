package platform.logs.entity;

import cfg.PetBaseProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.ConsumeLog;
import protocol.Common.Consume;
import protocol.PetMessage.Pet;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PetLvlUpLog extends AbstractPlayerLog {
    private String petId;
    private String petName;
    private int petLvl;
    private int lastPetLv;
    private List<ConsumeLog> consumeList;

    public PetLvlUpLog(String playerId,int lastPetLv, Pet.Builder pet, List<Consume> consumeList) {
        super(playerId);
        if (pet == null) {
            return;
        }

        this.petId = pet.getId();
        this.lastPetLv=lastPetLv;
        this.petName = PetBaseProperties.getNameById(pet.getPetBookId());
        this.petLvl = pet.getPetLvl();
        List<ConsumeLog> consumeLogList = new ArrayList<>();
        for (Consume consume : consumeList) {
            consumeLogList.add(new ConsumeLog(consume));
        }
        this.consumeList = consumeLogList;
    }
}
