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

@annationInit(value = "TheWarTechConfig", methodname = "initConfig")
public class TheWarTechConfig extends baseConfig<TheWarTechConfigObject> {


    private static TheWarTechConfig instance = null;

    public static TheWarTechConfig getInstance() {

        if (instance == null)
            instance = new TheWarTechConfig();
        return instance;

    }


    public static Map<Integer, TheWarTechConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (TheWarTechConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "TheWarTechConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static TheWarTechConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, TheWarTechConfigObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setRace(MapHelper.getInt(e, "race"));

        config.setLevel(MapHelper.getInt(e, "level"));

        config.setBasebuffid(MapHelper.getInts(e, "BaseBuffId"));

        config.setSkilllist(MapHelper.getIntArray(e, "SkillList"));

        config.setNeedquality(MapHelper.getInt(e, "NeedQuality"));

        _ix_id.put(config.getId(), config);

        if (raceConfigMap == null) {
            raceConfigMap = new HashMap<>();
        }
        List<TheWarTechConfigObject> raceCfgList = raceConfigMap.get(config.getRace());
        if (raceCfgList == null) {
            raceCfgList = new ArrayList<>();
            raceConfigMap.put(config.getRace(), raceCfgList);
        }
        raceCfgList.add(config);
    }

    private Map<Integer, List<TheWarTechConfigObject>> raceConfigMap;

    public Map<Integer, List<TheWarTechConfigObject>> getRaceConfigMap() {
        return raceConfigMap;
    }

    public int getRaceMaxLevel(int race) {
        List<TheWarTechConfigObject> configList = raceConfigMap.get(race);
        return configList != null ? configList.size() : 0;
    }

    public TheWarTechConfigObject getTechCfgByRaceAndLevel(int race, int level) {
        List<TheWarTechConfigObject> configList = raceConfigMap.get(race);
        return configList.stream().filter(config -> config.getLevel() == level).findFirst().orElseGet(null);
    }
}
