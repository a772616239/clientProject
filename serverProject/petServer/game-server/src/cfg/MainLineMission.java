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

@annationInit(value = "MainLineMission", methodname = "initConfig")
public class MainLineMission extends baseConfig<MainLineMissionObject> {


    private static MainLineMission instance = null;

    public static MainLineMission getInstance() {

        if (instance == null)
            instance = new MainLineMission();
        return instance;

    }


    public static Map<Integer, MainLineMissionObject> _ix_id = new HashMap<Integer, MainLineMissionObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MainLineMission) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MainLineMission");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MainLineMissionObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MainLineMissionObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setRewards(MapHelper.getIntArray(e, "rewards"));


        _ix_id.put(config.getId(), config);


    }
}
