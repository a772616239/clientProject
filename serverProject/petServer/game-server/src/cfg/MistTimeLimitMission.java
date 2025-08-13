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

@annationInit(value = "MistTimeLimitMission", methodname = "initConfig")
public class MistTimeLimitMission extends baseConfig<MistTimeLimitMissionObject> {


    private static MistTimeLimitMission instance = null;

    public static MistTimeLimitMission getInstance() {

        if (instance == null)
            instance = new MistTimeLimitMission();
        return instance;

    }


    public static Map<Integer, MistTimeLimitMissionObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistTimeLimitMission) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistTimeLimitMission");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistTimeLimitMissionObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MistTimeLimitMissionObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setMissionlist(MapHelper.getInts(e, "missionList"));

        config.setOpenlevel(MapHelper.getInts(e, "OpenLevel"));


        _ix_id.put(config.getId(), config);


    }
}
