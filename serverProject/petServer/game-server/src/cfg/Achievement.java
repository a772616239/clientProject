/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import protocol.TargetSystem.TargetTypeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "Achievement", methodname = "initConfig")
public class Achievement extends baseConfig<AchievementObject> {


    private static Achievement instance = null;

    public static Achievement getInstance() {

        if (instance == null)
            instance = new Achievement();
        return instance;

    }


    public static Map<Integer, AchievementObject> _ix_id = new HashMap<Integer, AchievementObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (Achievement) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "Achievement");

        for (Map e : ret) {
            put(e);
        }

    }

    public static AchievementObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, AchievementObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setType(MapHelper.getInt(e, "type"));

        config.setAddtiomcondition(MapHelper.getInt(e, "addtiomCondition"));

        config.setTargetcount(MapHelper.getIntArray(e, "targetCount"));


        _ix_id.put(config.getId(), config);

        putByType(config);
    }

    /**
     * ==================================================
     */
    private final Map<TargetTypeEnum, List<AchievementObject>> achievementByType = new HashMap<>();

    private void putByType(AchievementObject config) {
        if (config == null) {
            return;
        }

        int type = config.getType();
        List<AchievementObject> achievementObjects = achievementByType.computeIfAbsent(TargetTypeEnum.forNumber(type), t -> new ArrayList<>());
        achievementByType.put(TargetTypeEnum.forNumber(type), achievementObjects);
        achievementObjects.add(config);
    }

    public List<AchievementObject> getByType(TargetTypeEnum type){
        return achievementByType.get(type);
    }

    public int getMaxTargetCount(int cfgId) {
        AchievementObject byId = getById(cfgId);
        if (byId == null) {
            return 0;
        }

        int[][] targetcount = byId.getTargetcount();
        int max = 0;
        for (int[] ints : targetcount) {
            if (ints[0] > max) {
                max = ints[0];
            }
        }
        return max;
    }
}
