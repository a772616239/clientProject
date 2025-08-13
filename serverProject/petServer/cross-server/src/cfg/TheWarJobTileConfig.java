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

@annationInit(value = "TheWarJobTileConfig", methodname = "initConfig")
public class TheWarJobTileConfig extends baseConfig<TheWarJobTileConfigObject> {


    private static TheWarJobTileConfig instance = null;

    public static TheWarJobTileConfig getInstance() {

        if (instance == null)
            instance = new TheWarJobTileConfig();
        return instance;

    }


    public static Map<Integer, TheWarJobTileConfigObject> _ix_id = new HashMap<Integer, TheWarJobTileConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (TheWarJobTileConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "TheWarJobTileConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static TheWarJobTileConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, TheWarJobTileConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setMaxpetcount(MapHelper.getInt(e, "maxPetCount"));

        config.setTeammaxpetcount(MapHelper.getInt(e, "teamMaxPetCount"));

        config.setMaxoccupygirdcount(MapHelper.getInt(e, "maxOccupyGirdcount"));

        config.setMaxtechlevel(MapHelper.getInt(e, "maxTechLevel"));

        config.setAchievecondition(MapHelper.getInts(e, "achieveCondition"));

        config.setJobtilereward(MapHelper.getIntArray(e, "JobTileReward"));


        _ix_id.put(config.getId(), config);

        initJobTileLevel = initJobTileLevel == 0 ? config.getId() : Integer.min(initJobTileLevel, config.getId());
        maxJobTileLevel = Integer.max(maxJobTileLevel, config.getId());
    }

    private int initJobTileLevel;
    private int maxJobTileLevel;

    public int getInitJobTileLevel() {
        return initJobTileLevel;
    }

    public int getMaxJobTileLevel() {
        return maxJobTileLevel;
    }
}
