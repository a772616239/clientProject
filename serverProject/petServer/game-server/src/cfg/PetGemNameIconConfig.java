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

@annationInit(value = "PetGemNameIconConfig", methodname = "initConfig")
public class PetGemNameIconConfig extends baseConfig<PetGemNameIconConfigObject> {


    private static PetGemNameIconConfig instance = null;

    public static PetGemNameIconConfig getInstance() {

        if (instance == null)
            instance = new PetGemNameIconConfig();
        return instance;

    }


    public static Map<String, PetGemNameIconConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetGemNameIconConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetGemNameIconConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetGemNameIconConfigObject getById(String id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetGemNameIconConfigObject config) {

        config.setId(MapHelper.getStr(e, "id"));

        config.setTipname(MapHelper.getStr(e, "tipName"));


        _ix_id.put(config.getId(), config);


    }

    public static String queryName(int cfgId) {
        PetGemConfigObject gemConfig = PetGemConfig.getById(cfgId);
        if (gemConfig == null) {
            return "";
        }
        PetGemNameIconConfigObject nameConfig = getById(getIdByRarityAndType(gemConfig.getRarity(), gemConfig.getGemtype()));

        return nameConfig == null ? "" : nameConfig.getTipname();
    }

    private static String getIdByRarityAndType(int rarity, int type) {
        return rarity * 10 + type + "";
    }
}
