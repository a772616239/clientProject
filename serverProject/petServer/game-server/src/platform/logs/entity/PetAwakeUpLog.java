package platform.logs.entity;

import cfg.PetBaseProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass;
import protocol.Common.Consume;
import protocol.PetMessage.Pet;

@Getter
@Setter
@NoArgsConstructor
public class PetAwakeUpLog extends AbstractPlayerLog {
    private String petId;
    private String petName;
    private List<LogClass.ConsumeRemainLog> consumeList;
    private int originalTotalAwakeLv;
    private int nowTotalAwakeLv;
    private int awakeType;
    private int originalLv;
    private int nowLv;
    private long abilityUp;

    public PetAwakeUpLog(String playerId, Pet pet, int type, int originalTotalAwakeLv, int nowTotalAwakeLv
            , int originalLv, int nowLv, List<Consume> consumes, long abilityUp) {
        super(playerId);
        if (pet == null) {
            return;
        }
        this.awakeType = type;
        this.originalTotalAwakeLv = originalTotalAwakeLv;
        this.nowTotalAwakeLv = nowTotalAwakeLv;
        this.petId = pet.getId();
        this.petName = PetBaseProperties.getNameById(pet.getPetBookId());
        this.originalLv = originalLv;
        this.nowLv = nowLv;
        List<LogClass.ConsumeRemainLog> consumeLogList = new ArrayList<>();
        for (Consume consume : consumes) {
            consumeLogList.add(new LogClass.ConsumeRemainLog(playerId, consume));
        }
        this.consumeList = consumeLogList;
        this.abilityUp = abilityUp;
    }

}
