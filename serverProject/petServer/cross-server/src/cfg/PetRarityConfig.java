/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.ArrayList;
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

        if (minLv == 0 || config.getMaxlvl() < minLv) {
            minLv = config.getMaxlvl();
        }

        if (minRarity == 0 || config.getKey() < minRarity) {
            minRarity = config.getKey();
        }
    }

    public static int minLv;

    public static int minRarity;

    public static int getRarity(int petCfgId, int lv) {
        return getRarity(PetBaseProperties.getByPetid(petCfgId), lv);
    }

    public static int getRarity(PetBasePropertiesObject petCfg, int lv) {
        if (petCfg == null) {
            return minRarity;
        }

        if (lv <= minLv) {
            return petCfg.getStartrarity();
        }

        List<PetRarityConfigObject> list = new ArrayList<>(_ix_key.values());
        list.sort(Comparator.comparingInt(PetRarityConfigObject::getMaxlvl));

        int findRarity = 0;
        for (PetRarityConfigObject object : list) {
            if (lv <= object.getMaxlvl()) {
                findRarity = object.getRarity();
                break;
            }
        }
        //最低品质
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

}
