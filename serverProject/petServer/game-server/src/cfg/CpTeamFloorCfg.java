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

@annationInit(value = "CpTeamFloorCfg", methodname = "initConfig")
public class CpTeamFloorCfg extends baseConfig<CpTeamFloorCfgObject> {


    private static CpTeamFloorCfg instance = null;

    public static CpTeamFloorCfg getInstance() {

        if (instance == null)
            instance = new CpTeamFloorCfg();
        return instance;

    }


    public static Map<Integer, CpTeamFloorCfgObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CpTeamFloorCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CpTeamFloorCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static CpTeamFloorCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, CpTeamFloorCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setMonstercfg(MapHelper.getIntArray(e, "monsterCfg"));

        config.setEventhappen(MapHelper.getIntArray(e, "eventHappen"));


        _ix_id.put(config.getId(), config);


    }

    public int getMaxFloor() {
        return _ix_id.keySet().stream().max(Integer::compareTo).get();
    }
}
