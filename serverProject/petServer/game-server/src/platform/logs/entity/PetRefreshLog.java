package platform.logs.entity;

import cfg.PetBaseProperties;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.PetPropertyLog;
import platform.logs.ReasonManager.Reason;
import platform.logs.StatisticsLogUtil;
import protocol.PetMessage.Pet;

@Getter
@Setter
@NoArgsConstructor
public class PetRefreshLog extends AbstractPlayerLog {
    private List<PetPropertyLog> before;
    private List<PetPropertyLog> after;
    private String reason;
    private String petId;
    private String petName;
    private long ability;

    public PetRefreshLog(String playerId, Pet.Builder afterPet, Pet.Builder beforePet, Reason reason) {
        super(playerId);
        this.before = StatisticsLogUtil.buildPetProperty(beforePet);
        this.after = StatisticsLogUtil.buildPetProperty(beforePet);
        this.reason = reason == null ? "" : reason.toString();
        if (afterPet == null) {
            return;
        }

        this.petId = afterPet.getId();

        this.petName = PetBaseProperties.getNameById(afterPet.getPetBookId());
        //战力需要除以1000
        this.ability = afterPet.getAbility();
    }
}
