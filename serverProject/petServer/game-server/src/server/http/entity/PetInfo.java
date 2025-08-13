package server.http.entity;

import cfg.PetBaseProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import model.petgem.dbCache.petgemCache;
import model.petrune.dbCache.petruneCache;
import platform.logs.StatisticsLogUtil;
import protocol.PetMessage.Gem;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Rune;

/**
 * @author huhan
 * @date 2020.02.27
 */
@Getter
@Setter
public class PetInfo {
    private String name;
    private String uniqueId;
    private int id;
    private int level;
    private int rarity;
    private int awakeLv;
    private String state;
    private List<RuneInfo> rune = new ArrayList<>();
    private String type;
    private long ability;
    private String quality;
    private GemInfo gemInfo;

    /**
     * @param playerIdx
     * @param pet
     * @param abilityAddition 加成战力
     */
    public PetInfo(String playerIdx, Pet pet, long abilityAddition) {
        if (pet == null) {
            return;
        }
        this.id = pet.getPetBookId();
        this.uniqueId = pet.getId();
        this.name = PetBaseProperties.getNameById(id);
        this.level = pet.getPetLvl();
        this.rarity = pet.getPetRarity();
        this.awakeLv = pet.getPetUpLvl();
        this.state = getStateStr(pet);
        List<Rune> petRuneList = petruneCache.getInstance().getPetRune(playerIdx, pet.getId());
        if (petRuneList != null) {
            for (Rune petRune : petRuneList) {
                this.rune.add(new RuneInfo(playerIdx, petRune));
            }
        }
        this.type = PetBaseProperties.getTypeNameById(id);
        this.ability = pet.getAbility() + abilityAddition;
        this.quality = StatisticsLogUtil.getQualityName(pet.getPetRarity());

        //宝石佩戴
        Gem gem = petgemCache.getInstance().getGemByGemIdx(playerIdx, pet.getGemId());
        if (gem != null) {
            this.gemInfo = new GemInfo(playerIdx, gem);
        }
    }

    private String getStateStr(Pet pet) {
        StringBuffer stringBuffer = new StringBuffer();
        if (pet.getPetLockStatus() == 1) {
            stringBuffer.append("锁定/");
        }
        if (pet.getPetMineStatus() == 1) {
            stringBuffer.append("矿区中/");
        }
        if (pet.getPetTeamStatus() >= 1) {
            stringBuffer.append("编队中/");
        }
        if (pet.getPetChangeStatus() == 1) {
            stringBuffer.append("转换中/");
        }
        return stringBuffer.length() > 0 ? stringBuffer.toString() : "空闲";
    }
}
