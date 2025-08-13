/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;

@annationInit(value = "GrowthFundConfig", methodname = "initConfig")
public class GrowthFundConfig extends baseConfig<GrowthFundConfigObject> {


    private static GrowthFundConfig instance = null;

    public static GrowthFundConfig getInstance() {

        if (instance == null)
            instance = new GrowthFundConfig();
        return instance;

    }


    public static Map<Integer, GrowthFundConfigObject> _ix_id = new HashMap<Integer, GrowthFundConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (GrowthFundConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "GrowthFundConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static GrowthFundConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, GrowthFundConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setTargetplayerlv(MapHelper.getInt(e, "targetPlayerLv"));

        config.setReward(MapHelper.getInt(e, "reward"));


        _ix_id.put(config.getId(), config);


    }
}
