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

@annationInit(value = "TheWarPersueConfig", methodname = "initConfig")
public class TheWarPersueConfig extends baseConfig<TheWarPersueConfigObject> {


    private static TheWarPersueConfig instance = null;

    public static TheWarPersueConfig getInstance() {

        if (instance == null)
            instance = new TheWarPersueConfig();
        return instance;

    }


    public static Map<Integer, TheWarPersueConfigObject> _ix_roomlevel = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (TheWarPersueConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "TheWarPersueConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static TheWarPersueConfigObject getByRoomlevel(int roomlevel) {

        return _ix_roomlevel.get(roomlevel);

    }


    public void putToMem(Map e, TheWarPersueConfigObject config) {

        config.setRoomlevel(MapHelper.getInt(e, "roomLevel"));

        config.setRewardstamina(MapHelper.getInt(e, "rewardStamina"));

        config.setWarrewrad(MapHelper.getIntArray(e, "warRewrad"));


        _ix_roomlevel.put(config.getRoomlevel(), config);


    }
}
