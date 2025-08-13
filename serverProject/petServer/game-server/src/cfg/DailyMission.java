/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import protocol.TargetSystem.TargetTypeEnum;

@annationInit(value = "DailyMission", methodname = "initConfig")
public class DailyMission extends baseConfig<DailyMissionObject> {


    private static DailyMission instance = null;

    public static DailyMission getInstance() {

        if (instance == null)
            instance = new DailyMission();
        return instance;

    }


    public static Map<Integer, DailyMissionObject> _ix_id = new HashMap<Integer, DailyMissionObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (DailyMission) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "DailyMission");

        for (Map e : ret) {
            put(e);
        }

    }

    public static DailyMissionObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, DailyMissionObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setMissiontype(MapHelper.getInt(e, "missionType"));

        config.setAddtion(MapHelper.getInt(e, "addtion"));

        config.setTargetcount(MapHelper.getInt(e, "targetCount"));

        config.setFinishreward(MapHelper.getIntArray(e, "finishReward"));


        _ix_id.put(config.getId(), config);

        putByType(config);
    }


    /**
     * =========================================================================
     */

    private Map<Integer, List<DailyMissionObject>> dailyMissionCfgMap = new HashMap<>();

    private void putByType(DailyMissionObject config) {
        if (config == null) {
            return;
        }

        int missionType = config.getMissiontype();
        List<DailyMissionObject> dailyMissionObjects = dailyMissionCfgMap.get(missionType);
        if (dailyMissionObjects == null) {
            dailyMissionObjects = new ArrayList<>();
            dailyMissionObjects.add(config);
            dailyMissionCfgMap.put(missionType, dailyMissionObjects);
        } else {
            dailyMissionObjects.add(config);
        }
    }

    public List<DailyMissionObject> getDailyMissionCfgListByType(TargetTypeEnum type) {
        return dailyMissionCfgMap.get(type.getNumber());
    }


}
