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

@annationInit(value = "HadesWorshipConfig", methodname = "initConfig")
public class HadesWorshipConfig extends baseConfig<HadesWorshipConfigObject> {


    private static HadesWorshipConfig instance = null;

    public static HadesWorshipConfig getInstance() {

        if (instance == null)
            instance = new HadesWorshipConfig();
        return instance;

    }


    public static Map<Integer, HadesWorshipConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (HadesWorshipConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "HadesWorshipConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static HadesWorshipConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, HadesWorshipConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setConsume(MapHelper.getInts(e, "consume"));

        config.setReturnrewards(MapHelper.getIntArray(e, "returnRewards"));


        _ix_id.put(config.getId(), config);

        if (config.getId() > 0 && config.getId() > maxTimes) {
            maxTimes = config.getId();
        }

    }

    private static int maxTimes = 0;

    /**
     * 获取下一次供奉的配置
     * @param curTimes
     * @return
     */
    public static HadesWorshipConfigObject getNextWorshipConfig(int curTimes) {
        int nextTimes = curTimes + 1;
        if (nextTimes >= maxTimes) {
            return getById(maxTimes);
        }
        return getById(nextTimes);
    }
}
