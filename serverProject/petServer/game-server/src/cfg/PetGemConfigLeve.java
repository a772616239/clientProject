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

@annationInit(value = "PetGemConfigLeve", methodname = "initConfig")
public class PetGemConfigLeve extends baseConfig<PetGemConfigLeveObject> {


    private static PetGemConfigLeve instance = null;

    public static PetGemConfigLeve getInstance() {

        if (instance == null)
            instance = new PetGemConfigLeve();
        return instance;

    }


    public static Map<Integer, PetGemConfigLeveObject> _ix_lv = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetGemConfigLeve) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetGemConfigLeve");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetGemConfigLeveObject getByLv(int lv) {

        return _ix_lv.get(lv);

    }


    public void putToMem(Map e, PetGemConfigLeveObject config) {

        config.setLv(MapHelper.getInt(e, "lv"));

        config.setUplvconsume(MapHelper.getIntArray(e, "upLvConsume"));

        config.setGemsale(MapHelper.getIntArray(e, "gemSale"));


        _ix_lv.put(config.getLv(), config);

    }
}
