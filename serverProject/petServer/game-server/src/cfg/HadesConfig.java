/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.GameConst;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import protocol.TargetSystem.TargetTypeEnum;
import util.LogUtil;

@annationInit(value = "HadesConfig", methodname = "initConfig")
public class HadesConfig extends baseConfig<HadesConfigObject> {


    private static HadesConfig instance = null;

    public static HadesConfig getInstance() {

        if (instance == null)
            instance = new HadesConfig();
        return instance;

    }


    public static Map<Integer, HadesConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (HadesConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "HadesConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static HadesConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, HadesConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setDefaulttimes(MapHelper.getInt(e, "defaultTimes"));

        config.setDailymission(MapHelper.getInts(e, "dailyMission"));


        _ix_id.put(config.getId(), config);


    }

    public static List<MissionObject> getDailyMissionCfgByType(TargetTypeEnum type) {
        if (type == null) {
            return null;
        }

        List<MissionObject> result = new ArrayList<>();
        for (int missionId : getById(GameConst.CONFIG_ID).getDailymission()) {
            MissionObject missionCfg = Mission.getById(missionId);
            if (missionCfg == null) {
                LogUtil.error("cfg.HadesConfig.getDailyMissionCfgByType, daily mission config is not exist, mission id:" + missionId);
                continue;
            }

            if (missionCfg.getMissiontype() == type.getNumber()) {
                result.add(missionCfg);
            }
        }
        return result;
    }
}
