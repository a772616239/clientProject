package platform.logs.entity.petGem;

import cfg.PetBaseProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.pet.dbCache.petCache;
import platform.logs.AbstractPlayerLog;
import protocol.PetMessage.Gem;
import protocol.PetMessage.Pet;


/**
 * 宝石穿卸
 */
@Getter
@Setter
@NoArgsConstructor
public class PetGemEquipLog extends AbstractPlayerLog {
    /**
     * 装备宝石/卸下宝石
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
     * 装备宝石
     */
    private GemItemLog equipGem;
    /**
     * 卸下宝石
     */
    private GemItemLog unEquipGem;

    public PetGemEquipLog(String playerIdx, String petId, Gem originGem, Gem nowGem) {
        super(playerIdx);
        this.equip = null != nowGem;
        Pet petById = petCache.getInstance().getPetById(playerIdx, petId);
        if (petById != null) {
            this.petName = PetBaseProperties.getNameById(petById.getPetBookId());
        }
        this.petId = petId;
        if (nowGem != null) {
            this.equipGem = new GemItemLog(nowGem.getId(), nowGem.getGemConfigId());
        }
        if (originGem != null) {
            this.unEquipGem = new GemItemLog(originGem.getId(), originGem.getGemConfigId());
        }

    }
}
