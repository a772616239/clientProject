/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;

@annationInit(value = "TheWarSeasonConfig", methodname = "initConfig")
public class TheWarSeasonConfig extends baseConfig<TheWarSeasonConfigObject> {


    private static TheWarSeasonConfig instance = null;

    public static TheWarSeasonConfig getInstance() {

        if (instance == null)
            instance = new TheWarSeasonConfig();
        return instance;

    }


    public static Map<Integer, TheWarSeasonConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (TheWarSeasonConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "TheWarSeasonConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static TheWarSeasonConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, TheWarSeasonConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setOpenmapname(MapHelper.getStr(e, "openMapName"));

        config.setStartplaytime(MapHelper.getStr(e, "startPlayTime"));

        config.setEndplaytime(MapHelper.getStr(e, "endPlayTime"));

        config.setMissions(MapHelper.getInts(e, "missions"));

        config.setSeasoncamprankreward(MapHelper.getIntArray(e, "seasonCampRankReward"));


        _ix_id.put(config.getId(), config);
    }

    public TheWarSeasonConfigObject getWarOpenConfig() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        TheWarSeasonConfigObject config = null;
        for (TheWarSeasonConfigObject loopCfg : _ix_id.values()) {
            if (loopCfg.getEndplaytime() < curTime) {
                continue;
            }
            if (config == null || config.getStartplaytime() > loopCfg.getStartplaytime()) {
                config = loopCfg;
            }
        }
        return config;
    }
}
