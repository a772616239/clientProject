/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import datatool.MapHelper;
import model.base.baseConfig;
import petrobot.util.ServerConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "PetFragmentConfig", methodname = "initConfig")
public class PetFragmentConfig extends baseConfig<PetFragmentConfigObject> {


    private static PetFragmentConfig instance = null;

    public static PetFragmentConfig getInstance() {

        if (instance == null)
            instance = new PetFragmentConfig();
        return instance;

    }


    public static Map<Integer, PetFragmentConfigObject> _ix_id = new HashMap<Integer, PetFragmentConfigObject>();

    public static Map<Integer, PetFragmentConfigObject> _ix_petid = new HashMap<Integer, PetFragmentConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetFragmentConfig) o;
        initConfig();
    }


    public void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetFragmentConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetFragmentConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public static PetFragmentConfigObject getByPetid(int petid) {

        return _ix_petid.get(petid);

    }


    /**
     * key-value:rarity-entityList
     */
    public static Map<Integer, List<PetFragmentConfigObject>> entityMap = new HashMap<>();


    public static List<PetFragmentConfigObject> getProbByRarity(int rarity) {
        return entityMap.get(rarity);
    }


    public void putToMem(Map e, PetFragmentConfigObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setDebrisrarity(MapHelper.getInt(e, "debrisRarity"));

        config.setAmount(MapHelper.getInt(e, "amount"));

        config.setDebristype(MapHelper.getInt(e, "debrisType"));

        config.setPetid(MapHelper.getInt(e, "petId"));

        config.setProbability(MapHelper.getInt(e, "probability"));

        config.setProbabilitybyclass(MapHelper.getInt(e, "probabilityByClass"));

        config.setName(MapHelper.getStr(e, "name"));


        _ix_id.put(config.getId(), config);

        _ix_petid.put(config.getPetid(), config);



        _ix_petid.put(config.getPetid(), config);

        if (config.getPetid() != 0) {
            List<PetFragmentConfigObject> entityList = entityMap.get(config.getDebrisrarity());
            if (entityList == null) {
                List<PetFragmentConfigObject> temp = new ArrayList<>();
                temp.add(config);
                entityMap.put(config.getDebrisrarity(), temp);
            } else {
                entityList.add(config);
            }
        }
    }

    public int getQualityByCfgId(int cfgId) {
        PetFragmentConfigObject byId = getById(cfgId);
        if (byId == null) {
            return 0;
        }
        return byId.getDebrisrarity();
    }

    public static String getNameById(int cfgId) {
        PetFragmentConfigObject byId = getById(cfgId);
        if (byId == null) {
            return String.valueOf(cfgId);
        }
        return byId.getName();
    }
}
