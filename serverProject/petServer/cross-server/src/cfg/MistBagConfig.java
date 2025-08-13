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

@annationInit(value = "MistBagConfig", methodname = "initConfig")
public class MistBagConfig extends baseConfig<MistBagConfigObject> {


    private static MistBagConfig instance = null;

    public static MistBagConfig getInstance() {

        if (instance == null)
            instance = new MistBagConfig();
        return instance;

    }


    public static Map<Integer, MistBagConfigObject> _ix_bagid = new HashMap<Integer, MistBagConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistBagConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistBagConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistBagConfigObject getByBagid(int bagid) {

        return _ix_bagid.get(bagid);

    }


    public void putToMem(Map e, MistBagConfigObject config) {

        config.setBagid(MapHelper.getInt(e, "BagId"));

        config.setRewardtype(MapHelper.getInt(e, "RewardType"));

        config.setRewardcount(MapHelper.getInt(e, "RewardCount"));

        config.setBagprogress(MapHelper.getInt(e, "BagProgress"));


        _ix_bagid.put(config.getBagid(), config);


    }
}
