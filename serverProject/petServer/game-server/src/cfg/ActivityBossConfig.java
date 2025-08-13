/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "ActivityBossConfig", methodname = "initConfig")
public class ActivityBossConfig extends baseConfig<ActivityBossConfigObject> {


    private static ActivityBossConfig instance = null;

    public static ActivityBossConfig getInstance() {

        if (instance == null)
            instance = new ActivityBossConfig();
        return instance;

    }


    public static Map<Integer, ActivityBossConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ActivityBossConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ActivityBossConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ActivityBossConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, ActivityBossConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setCfgid(MapHelper.getInt(e, "cfgId"));

        config.setBuffid(MapHelper.getInt(e, "buffId"));

        config.setFightmakeid(MapHelper.getInt(e, "fightMakeId"));

        config.setOpencecyle(MapHelper.getInt(e, "openCecyle"));

        config.setOpendays(MapHelper.getInt(e, "openDays"));

        config.setDisplayaheadtime(MapHelper.getInt(e, "displayAheadTime"));

        config.setDisplaylagtime(MapHelper.getInt(e, "displayLagTime"));

        config.setBegibtime(MapHelper.getStr(e, "begibTime"));

        config.setEndtime(MapHelper.getStr(e, "endTime"));

        config.setTimes(MapHelper.getInt(e, "times"));

        config.setLimitbuytimes(MapHelper.getInt(e, "limitBuyTimes"));

        config.setBuyprice(MapHelper.getInts(e, "buyPrice"));

        config.setHelp(MapHelper.getInt(e, "help"));

        config.setUnlocklevel(MapHelper.getInt(e, "unLockLevel"));


        _ix_id.put(config.getId(), config);


    }
}
