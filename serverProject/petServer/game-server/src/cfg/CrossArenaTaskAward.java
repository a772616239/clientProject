/*CREATED BY TOOL*/

package cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value = "CrossArenaTaskAward", methodname = "initConfig")
public class CrossArenaTaskAward extends baseConfig<CrossArenaTaskAwardObject> {


    private static CrossArenaTaskAward instance = null;

    public static CrossArenaTaskAward getInstance() {

        if (instance == null)
            instance = new CrossArenaTaskAward();
        return instance;

    }


    public static Map<Integer, CrossArenaTaskAwardObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CrossArenaTaskAward) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CrossArenaTaskAward");

        for (Map e : ret) {
            put(e);
        }

        for (CrossArenaTaskAwardObject catao : _ix_id.values()) {
            String key = createKey(catao.getMissionid(), catao.getSceneid(), catao.getPlan());
            keyMap.put(key, catao);
        }

    }

    public static CrossArenaTaskAwardObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, CrossArenaTaskAwardObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setMissionid(MapHelper.getInt(e, "missionId"));

        config.setSceneid(MapHelper.getInt(e, "sceneId"));

        config.setPlan(MapHelper.getInt(e, "plan"));

        config.setAward(MapHelper.getIntArray(e, "award"));


        _ix_id.put(config.getId(), config);
    }

    public static Map<String, CrossArenaTaskAwardObject> keyMap = new HashMap<>();

    public CrossArenaTaskAwardObject getAward(int taskId, int sceneId, int plan) {
        return keyMap.get(createKey(taskId, sceneId, plan));
    }

    public String createKey(int taskId, int sceneId, int plan) {
        return taskId + "_" + sceneId + "_" + plan;
    }

}
