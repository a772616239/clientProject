/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;

@annationInit(value = "MistIntegralReward", methodname = "initConfig")
public class MistIntegralReward extends baseConfig<MistIntegralRewardObject> {


    private static MistIntegralReward instance = null;

    public static MistIntegralReward getInstance() {

        if (instance == null)
            instance = new MistIntegralReward();
        return instance;

    }


    public static Map<Integer, MistIntegralRewardObject> _ix_index = new HashMap<Integer, MistIntegralRewardObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistIntegralReward) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistIntegralReward");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistIntegralRewardObject getByIndex(int index) {

        return _ix_index.get(index);

    }


    public void putToMem(Map e, MistIntegralRewardObject config) {

        config.setIndex(MapHelper.getInt(e, "Index"));

        config.setRewardintegral(MapHelper.getInt(e, "RewardIntegral"));

        config.setRewardid(MapHelper.getInt(e, "RewardId"));


        _ix_index.put(config.getIndex(), config);

    }

    public int getMistIntegralRewardCount() {
        return _ix_index.size();
    }

}
