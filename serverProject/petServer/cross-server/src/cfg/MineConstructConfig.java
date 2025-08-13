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

@annationInit(value = "MineConstructConfig", methodname = "initConfig")
public class MineConstructConfig extends baseConfig<MineConstructConfigObject> {


    private static MineConstructConfig instance = null;

    public static MineConstructConfig getInstance() {

        if (instance == null)
            instance = new MineConstructConfig();
        return instance;

    }

    public static Map<Integer, MineConstructConfigObject> _ix_id = new HashMap<Integer, MineConstructConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MineConstructConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MineConstructConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MineConstructConfigObject getById(int id) {

        return _ix_id.get(id);

    }

    public static Map<Integer, MineConstructConfigObject> getAllMineConfig() {
        return _ix_id;
    }


    public void putToMem(Map e, MineConstructConfigObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setMinetype(MapHelper.getIntArray(e, "MineType"));


        _ix_id.put(config.getId(), config);


    }
}
