/*CREATED BY TOOL*/

package cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value = "MatchArenaRobotPropertyCfg", methodname = "initConfig")
public class MatchArenaRobotPropertyCfg extends baseConfig<MatchArenaRobotPropertyCfgObject> {


    private static MatchArenaRobotPropertyCfg instance = null;

    public static MatchArenaRobotPropertyCfg getInstance() {

        if (instance == null)
            instance = new MatchArenaRobotPropertyCfg();
        return instance;

    }


    public static Map<Integer, MatchArenaRobotPropertyCfgObject> _ix_id = new HashMap<>();

    private static List<MatchArenaRobotPropertyCfgObject> cfgObjects = new ArrayList<>();

    public static MatchArenaRobotPropertyCfgObject randomCfg() {
        return cfgObjects.get(cfgObjects.size() - 1);
    }

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MatchArenaRobotPropertyCfg) o;
        initConfig();
        cfgObjects = new ArrayList<>(_ix_id.values());
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MatchArenaRobotPropertyCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MatchArenaRobotPropertyCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MatchArenaRobotPropertyCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setLevel(MapHelper.getInts(e, "level"));

        config.setRarity(MapHelper.getInt(e, "rarity"));

        config.setExproperty(MapHelper.getIntArray(e, "exProperty"));


        _ix_id.put(config.getId(), config);


    }
}
