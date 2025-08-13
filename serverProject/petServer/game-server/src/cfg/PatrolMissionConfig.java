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
import org.apache.commons.lang.math.RandomUtils;

@annationInit(value = "PatrolMissionConfig", methodname = "initConfig")
public class PatrolMissionConfig extends baseConfig<PatrolMissionConfigObject> {


    private static PatrolMissionConfig instance = null;

    public static PatrolMissionConfig getInstance() {

        if (instance == null)
            instance = new PatrolMissionConfig();
        return instance;

    }


    public static Map<Integer, PatrolMissionConfigObject> _ix_missionid = new HashMap<>();

    public static int totalWeight;


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PatrolMissionConfig) o;
        initConfig();
        totalWeight = _ix_missionid.values().stream().mapToInt(PatrolMissionConfigObject::getWeight).sum();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PatrolMissionConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PatrolMissionConfigObject getByMissionid(int missionid) {

        return _ix_missionid.get(missionid);

    }


    public void putToMem(Map e, PatrolMissionConfigObject config) {

        config.setMissionid(MapHelper.getInt(e, "missionId"));

        config.setWeight(MapHelper.getInt(e, "weight"));

        config.setLimittime(MapHelper.getInt(e, "limitTime"));

        config.setRewarduprate(MapHelper.getInt(e, "rewardUpRate"));


        _ix_missionid.put(config.getMissionid(), config);


    }

    public static PatrolMissionConfigObject randomOneMission() {
        int random = RandomUtils.nextInt(totalWeight);
        for (PatrolMissionConfigObject value : _ix_missionid.values()) {
            if (random < value.getWeight()) {
                return value;
            }
            random -= value.getWeight();
        }
        return null;
    }
}
