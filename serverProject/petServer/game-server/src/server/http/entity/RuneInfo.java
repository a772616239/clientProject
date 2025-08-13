package server.http.entity;

import cfg.PetBaseProperties;
import cfg.PetRuneProperties;
import lombok.Getter;
import lombok.Setter;
import model.pet.dbCache.petCache;
import platform.logs.LogClass.PetPropertyLog;
import platform.logs.StatisticsLogUtil;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Rune;
import protocol.PetMessage.RunePropertieyEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huhan
 * @date 2020.02.27
 */
@Setter
@Getter
public class RuneInfo {
    private String name;
    private String id;
    private int bookId;
    private int count;
    private String qualityValue;
    private int lv;
    /**
     * 基础属性
     **/
    private List<PetPropertyLog> baseProperties = new ArrayList<>();
    /**
     * 获得符文时的额外属性
     **/
    private List<PetPropertyLog> baseextends = new ArrayList<>();
    /**
     * 升级获得的符文属性
     **/
    private List<PetPropertyLog> lvUpextends = new ArrayList<>();
    private String status;
    /**
     * 装备的宠物名
     **/
    private String equipPetName;

    public RuneInfo(String playerIdx, Rune rune) {
        if (rune == null) {
            return;
        }
        this.id = rune.getId();
        this.bookId = rune.getRuneBookId();
        this.count = 1;
        this.name = PetRuneProperties.getNameById(rune.getRuneBookId());
        this.lv = rune.getRuneLvl();
        this.qualityValue = StatisticsLogUtil.getQualityName(PetRuneProperties.getQualityByCfgId(rune.getRuneBookId()));

        if (rune.getRuneBaseProperty().getPropertyCount() > 0) {
            for (RunePropertieyEntity entity : rune.getRuneBaseProperty().getPropertyList()) {
                baseProperties.add(new PetPropertyLog(entity));
            }
        }

        if (rune.getRuneExProperty().getPropertyCount() > 0) {
            for (RunePropertieyEntity entity : rune.getRuneExProperty().getPropertyList()) {
                baseextends.add(new PetPropertyLog(entity));
            }
        }

//        if (rune.getRuneExPropertyTwo().getPropertyCount() > 0) {
//            for (PetPropertyEntity entity : rune.getRuneExPropertyTwo().getPropertyList()) {
//                lvUpextends.add(new Property(entity));
//            }
//        }

        this.status = getStatusStr(playerIdx, rune);

        Pet petById = petCache.getInstance().getPetById(playerIdx, rune.getRunePet());
        if (petById != null) {
            this.equipPetName = PetBaseProperties.getNameById(petById.getPetBookId());
        }
    }

    private String getStatusStr(String playerIdx, Rune rune) {
        if (rune == null) {
            return "";
        }
        StringBuffer result = new StringBuffer();
        result.append(petCache.getInstance().getPetById(playerIdx, rune.getRunePet()) == null ? "未穿戴" : "穿戴" + "/");
        result.append(1 == rune.getRuneLockStatus() ? "锁定" : "未锁定");
        return result.toString();
    }

}
