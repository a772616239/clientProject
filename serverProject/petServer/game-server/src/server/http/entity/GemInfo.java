package server.http.entity;

import cfg.PetBaseProperties;
import cfg.PetGemConfig;
import cfg.PetGemConfigObject;
import cfg.PetGemNameIconConfig;
import lombok.Getter;
import lombok.Setter;
import model.pet.dbCache.petCache;
import platform.logs.StatisticsLogUtil;
import protocol.PetMessage.Gem;
import protocol.PetMessage.Pet;
import platform.logs.LogClass.PetPropertyLog;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author huhan
 * @date 2020.10.22
 */
@Getter
@Setter
public class GemInfo {
    private int configId;
    /**
     * 品质
     */
    private String rarity;

    /**
     * 星级
     */
    private int star;

    /**
     * 精炼等级
     */
    private int level;

    /**
     * id
     */
    private String id;

    /**
     * 宝石名
     */
    private String name;

    /**
     * 宝石装备状态 在装备状态时显示装备的宠物
     */
    private String status;

    /**基础属性**/
    private List<PetPropertyLog> properties = new ArrayList<>();


    public GemInfo(String playerIdx, Gem gem) {
        if (gem == null) {
            return;
        }

        PetGemConfigObject gemCfg = PetGemConfig.getById(gem.getGemConfigId());
        if (gemCfg == null) {
            return;
        }

        Map<Integer, Integer> pro = PetGemConfig.getGemAdditionByGemCfgId(gem.getGemConfigId());
        for (Map.Entry<Integer, Integer> entry : pro.entrySet()) {
            properties.add(new PetPropertyLog(entry.getKey(),entry.getValue()));
        }

        this.id = gem.getId();
        this.configId = gemCfg.getId();
        this.rarity = StatisticsLogUtil.getGemRarityName(gemCfg.getRarity());
        this.star = gemCfg.getStar();
        this.level = gemCfg.getLv();
        this.name = PetGemNameIconConfig.queryName(configId);


        String gemPetId = gem.getGemPet();
        Pet petById = petCache.getInstance().getPetById(playerIdx, gemPetId);
        if (petById != null) {
            this.status = PetBaseProperties.getNameById(petById.getPetBookId());
        } else {
            this.status = "";
        }
    }
}
