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

@annationInit(value = "PetMission", methodname = "initConfig")
public class PetMission extends baseConfig<PetMissionObject> {


    private static PetMission instance = null;

    public static PetMission getInstance() {

        if (instance == null)
            instance = new PetMission();
        return instance;

    }


    public static Map<Integer, PetMissionObject> _ix_missionlvl = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetMission) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetMission");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetMissionObject getByMissionlvl(int missionlvl) {

        return _ix_missionlvl.get(missionlvl);

    }


    public void putToMem(Map e, PetMissionObject config) {

        config.setMissionlvl(MapHelper.getInt(e, "missionLvl"));

        config.setPetrarity(MapHelper.getInts(e, "petRarity"));

        config.setPettype(MapHelper.getInts(e, "petType"));

        config.setRequire(MapHelper.getInt(e, "require"));

        config.setTime(MapHelper.getInt(e, "time"));

        config.setFragemntnum(MapHelper.getInt(e, "fragemntNum"));

        config.setLimitmissionrarity(MapHelper.getInt(e, "limitMissionRarity"));


        _ix_missionlvl.put(config.getMissionlvl(), config);


    }

    public static int queryLimitMissionRewardCount(int missionLvl) {
        PetMissionObject cfg = getByMissionlvl(missionLvl);
        if (cfg == null) {
            return 0;
        }
        return cfg.getFragemntnum();
    }

}
