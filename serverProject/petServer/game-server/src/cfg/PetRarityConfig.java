/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "PetRarityConfig", methodname = "initConfig")
public class PetRarityConfig extends baseConfig<PetRarityConfigObject> {


    private static PetRarityConfig instance = null;

    public static PetRarityConfig getInstance() {

        if (instance == null)
            instance = new PetRarityConfig();
        return instance;

    }


    public static Map<Integer, PetRarityConfigObject> _ix_key = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetRarityConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetRarityConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetRarityConfigObject getByKey(int key) {

        return _ix_key.get(key);

    }


    public void putToMem(Map e, PetRarityConfigObject config) {

        config.setKey(MapHelper.getInt(e, "key"));

        config.setRarity(MapHelper.getInt(e, "rarity"));

        config.setPropertymodel(MapHelper.getInt(e, "propertyModel"));

        config.setPetfactor(MapHelper.getInt(e, "petFactor"));

        config.setPetfixproperties(MapHelper.getIntArray(e, "petFixProperties"));

        config.setMaxlvl(MapHelper.getInt(e, "maxLvl"));


        _ix_key.put(config.getKey(), config);

        if (config.getKey() <= 0) {
            return;
        }

        if (maxLv == 0 || config.getMaxlvl() > maxLv) {
            maxLv = config.getMaxlvl();
        }

        if (minRarity == 0 || config.getRarity() < minRarity) {
            minRarity = config.getRarity();
        }
        if (maxRarity < config.getRarity()) {
            maxRarity = config.getRarity();
        }

    }

    public static int maxLv;

    public static int minRarity;

    public static int maxRarity;

    private static List<PetRarityConfigObject> sortedList;

    public static int getRarity(int petCfgId, int lv) {
        return getRarity(PetBaseProperties.getByPetid(petCfgId), lv);
    }

    public static int getRarity(PetBasePropertiesObject petCfg, int lv) {
        if (petCfg == null) {
            return minRarity;
        }

        if (sortedList == null) {
            List<PetRarityConfigObject> list = new ArrayList<>(_ix_key.values());
            list.sort(Comparator.comparingInt(PetRarityConfigObject::getMaxlvl));
            sortedList = list;
        }

        int findRarity = maxRarity;
        for (PetRarityConfigObject object : sortedList) {
            if (lv <= object.getMaxlvl()) {
                findRarity = object.getKey();
                break;
            }
        }

        return Math.max(findRarity, petCfg.getStartrarity());
    }

    public static PetRarityConfigObject getByRarityAndPropertyModel(int rarity, int propertyModel) {
        for (PetRarityConfigObject config : _ix_key.values()) {
            if (config.getRarity() == rarity && config.getPropertymodel() == propertyModel) {
                return config;
            }
        }
        return null;
    }

    /**
     * 根据品质 宠物bookId获取品质固定加成
     *
     * @param petRarity
     * @param petBookId
     * @return
     */
    public static Map<Integer, Integer> getRarityAddition(int petRarity, int petBookId) {
        int petClass = PetBaseProperties.getClass(petBookId);
        for (PetRarityConfigObject config : _ix_key.values()) {
            if (config.getRarity() == petRarity && config.getPropertymodel() == petClass && ArrayUtils.isNotEmpty(config.getPetfixproperties())) {
                Map<Integer, Integer> map = new HashMap<>();
                for (int[] fixProperty : config.getPetfixproperties()) {
                    if (fixProperty.length > 1) {
                        map.put(fixProperty[0], fixProperty[1]);
                    }
                }
                return map;
            }

        }
        return Collections.emptyMap();
    }

    public static Integer getMaxLv(int rarity) {
        for (PetRarityConfigObject config : _ix_key.values()) {
            if (config.getRarity() == rarity) {
                return config.getMaxlvl();
            }
        }
        return null;
    }

}
