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

@annationInit(value = "RuneTreasure", methodname = "initConfig")
public class RuneTreasure extends baseConfig<RuneTreasureObject> {


    private static RuneTreasure instance = null;

    public static RuneTreasure getInstance() {

        if (instance == null)
            instance = new RuneTreasure();
        return instance;

    }


    public static Map<Integer, RuneTreasureObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (RuneTreasure) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "RuneTreasure");

        for (Map e : ret) {
            put(e);
        }

    }

    public static RuneTreasureObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, RuneTreasureObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setKeyprice(MapHelper.getInts(e, "keyPrice"));

        config.setDailymission(MapHelper.getInts(e, "dailyMission"));

        config.setDrawprice(MapHelper.getInts(e, "drawPrice"));

        config.setStagerewardsmaxsize(MapHelper.getInt(e, "stageRewardsMaxSize"));


        _ix_id.put(config.getId(), config);


    }

    public static List<MissionObject> getDailyMissionCfgByType(TargetTypeEnum typeEnum) {
        if (typeEnum == null) {
            return null;
        }
        List<MissionObject> result = new ArrayList<>();
        for (int missionId : getById(GameConst.CONFIG_ID).getDailymission()) {
            MissionObject missionCfg = Mission.getById(missionId);
            if (missionCfg != null && missionCfg.getMissiontype() == typeEnum.getNumber()) {
                result.add(missionCfg);
            }
        }
        return result;
    }
}
