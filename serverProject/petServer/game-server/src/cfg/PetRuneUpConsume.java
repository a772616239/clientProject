/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "PetRuneUpConsume", methodname = "initConfig")
public class PetRuneUpConsume extends baseConfig<PetRuneUpConsumeObject> {


    private static PetRuneUpConsume instance = null;

    public static PetRuneUpConsume getInstance() {

        if (instance == null)
            instance = new PetRuneUpConsume();
        return instance;

    }


    public static Map<Integer, PetRuneUpConsumeObject> _ix_key = new HashMap<Integer, PetRuneUpConsumeObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetRuneUpConsume) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetRuneUpConsume");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetRuneUpConsumeObject getByKey(int key) {
        return _ix_key.get(key);
    }

    public static PetRuneUpConsumeObject getByCfgIdAndLvl(int cfgId, int lvl) {
        for (PetRuneUpConsumeObject value : _ix_key.values()) {
            if (value.getRuneid() == cfgId && value.getRunelvl() == lvl) {
                return value;
            }
        }
        return null;
    }


    public void putToMem(Map e, PetRuneUpConsumeObject config) {

        config.setKey(MapHelper.getInt(e, "key"));

        config.setRuneid(MapHelper.getInt(e, "runeId"));

        config.setRunelvl(MapHelper.getInt(e, "runeLvl"));

        config.setConsume(MapHelper.getInts(e, "consume"));


        _ix_key.put(config.getKey(), config);


    }
}
