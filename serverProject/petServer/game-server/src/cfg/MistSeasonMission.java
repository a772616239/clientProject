/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import protocol.TargetSystem.TargetTypeEnum;

@annationInit(value = "MistSeasonMission", methodname = "initConfig")
public class MistSeasonMission extends baseConfig<MistSeasonMissionObject> {


    private static MistSeasonMission instance = null;

    public static MistSeasonMission getInstance() {

        if (instance == null) instance = new MistSeasonMission();
        return instance;

    }


    public static Map<Integer, MistSeasonMissionObject> _ix_id = new HashMap<Integer, MistSeasonMissionObject>();


    public void initConfig(baseConfig o) {
        if (instance == null) instance = (MistSeasonMission) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistSeasonMission");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistSeasonMissionObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MistSeasonMissionObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setMissiontype(MapHelper.getInt(e, "missionType"));

        config.setAddtion(MapHelper.getInt(e, "addtion"));

        config.setTargetcount(MapHelper.getInt(e, "targetCount"));

        config.setFinishreward(MapHelper.getIntArray(e, "finishReward"));


        _ix_id.put(config.getId(), config);

        putByType(config);

    }

    /**
     * ------------------------------分割线-------------------------------------
     */

    private Map<Integer, List<MistSeasonMissionObject>> mistSeasonMissionMap = new HashMap<>();

    private void putByType(MistSeasonMissionObject config) {
        if (config == null) {
            return;
        }

        int missionType = config.getMissiontype();
        List<MistSeasonMissionObject> mistSeasonMissionObjs = mistSeasonMissionMap.get(missionType);
        if (mistSeasonMissionObjs == null) {
            mistSeasonMissionObjs = new ArrayList<>();
            mistSeasonMissionObjs.add(config);
            mistSeasonMissionMap.put(missionType, mistSeasonMissionObjs);
        } else {
            mistSeasonMissionObjs.add(config);
        }
    }

    public List<MistSeasonMissionObject> getMistSeasonMissionCfgListByType(TargetTypeEnum type) {
        return mistSeasonMissionMap.get(type.getNumber());
    }
}
