/*CREATED BY TOOL*/

package cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value = "PetLvlGroupUpConfig", methodname = "initConfig")
public class PetLvlGroupUpConfig extends baseConfig<PetLvlGroupUpConfigObject> {


    private static PetLvlGroupUpConfig instance = null;

    public static PetLvlGroupUpConfig getInstance() {

        if (instance == null)
            instance = new PetLvlGroupUpConfig();
        return instance;

    }


    public static Map<Integer, PetLvlGroupUpConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetLvlGroupUpConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetLvlGroupUpConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetLvlGroupUpConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetLvlGroupUpConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setLvl(MapHelper.getInt(e, "lvl"));

        config.setPropertymodel(MapHelper.getInt(e, "propertyModel"));

        config.setFactor(MapHelper.getIntArray(e, "factor"));

        config.setMonsterfactor(MapHelper.getIntArray(e, "monsterfactor"));

        config.setOtherfactors(MapHelper.getIntArray(e, "otherFactors"));

        config.setMonsterotherfactors(MapHelper.getIntArray(e, "monsterOtherFactors"));


        _ix_id.put(config.getId(), config);


    }

    public static PetLvlGroupUpConfigObject findByPetPropertyModelAndLv(int petModel, int petLvl) {

        for (PetLvlGroupUpConfigObject cfg : _ix_id.values()) {
            if (cfg.getPropertymodel() == petModel && cfg.getLvl() == petLvl) {
                return cfg;
            }
        }
        return null;
    }
}
