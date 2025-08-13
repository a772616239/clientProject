package platform.logs.entity;

import cfg.PetBaseProperties;
import cfg.PetRuneProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.pet.dbCache.petCache;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.PetRuneLog;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Rune;


/**
 * @author xiao_FL
 * @date 2019/12/25
 */
@Getter
@Setter
@NoArgsConstructor
public class PetRuneEquipLog extends AbstractPlayerLog {
    /**
     * 装备符文/卸下符文
     */
    private boolean equip;
    /**
     * 宠物名称
     */
    private String petName;
    /**
     * 宠物唯一id
     */
    private String petId;
    /**
     * 装备符文
     */
    private PetRuneLog equipRune;
    /**
     * 卸下符文
     */
    private PetRuneLog unEquipRune;
    private int equipType;

    public PetRuneEquipLog(String playerIdx, String petId, Rune originRune, Rune nowRune) {
        super(playerIdx);
        this.equip = null != nowRune;
        Pet petById = petCache.getInstance().getPetById(playerIdx, petId);
        if (petById != null) {
            this.petName = PetBaseProperties.getNameById(petById.getPetBookId());
        }
        this.petId = petId;
        if (nowRune != null) {
            this.equipRune = new PetRuneLog(nowRune);
        }
        if (originRune != null) {
            this.unEquipRune = new PetRuneLog(originRune);
        }

        if (null != nowRune) {
            this.equipType = PetRuneProperties.getRuneType(nowRune.getRuneBookId());
        } else if (null != originRune) {
            this.equipType = PetRuneProperties.getRuneType(originRune.getRuneBookId());
        }
    }
}
