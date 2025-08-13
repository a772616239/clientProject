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

@annationInit(value = "PetEvolveCfg", methodname = "initConfig")
public class PetEvolveCfg extends baseConfig<PetEvolveCfgObject> {


    private static PetEvolveCfg instance = null;

    public static PetEvolveCfg getInstance() {

        if (instance == null)
            instance = new PetEvolveCfg();
        return instance;

    }


    public static Map<Integer, PetEvolveCfgObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetEvolveCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetEvolveCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetEvolveCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetEvolveCfgObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setUpconsume(MapHelper.getIntArray(e, "upConsume"));

        config.setUpreward(MapHelper.getIntArray(e, "upReward"));

        config.setAddability(MapHelper.getInt(e, "addAbility"));


        _ix_id.put(config.getId(), config);


    }

    public PetEvolveCfgObject getByPetIdAndEvolveLv(int petBookId, int evolveLv) {
        return getById(petBookId * 100 + evolveLv);
    }
}
