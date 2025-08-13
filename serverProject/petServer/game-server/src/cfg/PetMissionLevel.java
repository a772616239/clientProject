/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import model.base.baseConfig;

@annationInit(value = "PetMissionLevel", methodname = "initConfig")
public class PetMissionLevel extends baseConfig<PetMissionLevelObject> {


    private static PetMissionLevel instance = null;

    public static PetMissionLevel getInstance() {

        if (instance == null)
            instance = new PetMissionLevel();
        return instance;

    }

    @Getter
    private static int maxMissionLv;


    public static Map<Integer, PetMissionLevelObject> _ix_missionlv = new HashMap<>();

    public static Map<Integer, Map<Integer, Integer>> targetNeed = new HashMap<>();

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetMissionLevel) o;
        initConfig();
        maxMissionLv = _ix_missionlv.values().stream().mapToInt(PetMissionLevelObject::getMissionlv).max().orElse(Integer.MAX_VALUE);

        initTargetNeed();
    }

    public Map<Integer, Integer> getTargetNeed(int missionLv) {
        Map<Integer, Integer> map = targetNeed.get(missionLv);
        return map == null ? Collections.emptyMap() : map;
    }

    private void initTargetNeed() {
        for (PetMissionLevelObject cfg : _ix_missionlv.values()) {
            Map<Integer, Integer> target = new HashMap<>();
            for (int[] ints : cfg.getUptarget()) {
                if (ints.length < 2) {
                    continue;
                }
                target.put(ints[0], ints[1]);
            }
            targetNeed.put(cfg.getMissionlv(), target);
        }
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetMissionLevel");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetMissionLevelObject getByMissionlv(int missionlv) {

        return _ix_missionlv.get(missionlv);

    }


    public void putToMem(Map e, PetMissionLevelObject config) {

        config.setMissionlv(MapHelper.getInt(e, "missionLv"));

        config.setUptarget(MapHelper.getIntArray(e, "upTarget"));

        config.setMissinratio(MapHelper.getIntArray(e, "missinRatio"));

        config.setMissiontypeweight(MapHelper.getIntArray(e, "missionTypeWeight"));


        _ix_missionlv.put(config.getMissionlv(), config);


    }
}
