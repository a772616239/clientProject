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

@annationInit(value = "PatrolGreed", methodname = "initConfig")
public class PatrolGreed extends baseConfig<PatrolGreedObject> {


    private static PatrolGreed instance = null;

    public static PatrolGreed getInstance() {

        if (instance == null)
            instance = new PatrolGreed();
        return instance;

    }


    public static Map<Integer, PatrolGreedObject> _ix_greed = new HashMap<Integer, PatrolGreedObject>();
    private static PatrolGreedObject maxGreed;

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PatrolGreed) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PatrolGreed");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PatrolGreedObject getByGreed(int greed) {
        if (greed <= 0) {
            greed = 0;
        }
        // 查询贪婪值，查不到返回最大的配置
        PatrolGreedObject temp = _ix_greed.get(greed);
        if (temp == null) {
            temp = maxGreed;
        }
        return temp;
    }


    public void putToMem(Map e, PatrolGreedObject config) {

        config.setGreed(MapHelper.getInt(e, "greed"));

        config.setEarn(MapHelper.getInt(e, "earn"));

        config.setStrengthen(MapHelper.getInt(e, "strengthen"));


        _ix_greed.put(config.getGreed(), config);

        if (maxGreed == null) {
            maxGreed = config;
            return;
        }
        if (config.getGreed() > maxGreed.getGreed()) {
            maxGreed = config;
        }
    }
}
