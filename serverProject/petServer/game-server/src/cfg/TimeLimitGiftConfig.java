/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "TimeLimitGiftConfig", methodname = "initConfig")
public class TimeLimitGiftConfig extends baseConfig<TimeLimitGiftConfigObject> {


    private static TimeLimitGiftConfig instance = null;

    public static TimeLimitGiftConfig getInstance() {

        if (instance == null)
            instance = new TimeLimitGiftConfig();
        return instance;

    }


    public static Map<Integer, TimeLimitGiftConfigObject> _ix_id = new HashMap<Integer, TimeLimitGiftConfigObject>();

    public static TimeLimitGiftConfigObject findMaxIdByTypeAndTarget(int type, int target) {
        return TimeLimitGiftConfig._ix_id.values().stream().filter(config -> type == config.getType()
                && target >= config.getTarget()).max(Comparator.comparingInt(TimeLimitGiftConfigObject::getId)).orElse(null);
    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (TimeLimitGiftConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "TimeLimitGiftConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static TimeLimitGiftConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, TimeLimitGiftConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setType(MapHelper.getInt(e, "type"));

        config.setTarget(MapHelper.getInt(e, "target"));

        config.setMusthave(MapHelper.getBoolean(e, "mustHave"));

        config.setProbability(MapHelper.getInt(e, "probability"));

        config.setExprobability(MapHelper.getInt(e, "exProbability"));

        config.setTriggerlimit(MapHelper.getInt(e, "triggerLimit"));

        config.setPrice(MapHelper.getInts(e, "price"));

        config.setReward(MapHelper.getInt(e, "reward"));

        config.setExpiretime(MapHelper.getInt(e, "expireTime"));

        config.setLevellimit(MapHelper.getInt(e, "levelLimit"));


        _ix_id.put(config.getId(), config);


    }
}
