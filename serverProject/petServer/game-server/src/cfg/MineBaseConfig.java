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

@annationInit(value = "MineBaseConfig", methodname = "initConfig")
public class MineBaseConfig extends baseConfig<MineBaseConfigObject> {


    private static MineBaseConfig instance = null;

    public static MineBaseConfig getInstance() {

        if (instance == null)
            instance = new MineBaseConfig();
        return instance;

    }


    public static Map<Integer, MineBaseConfigObject> _ix_type = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MineBaseConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MineBaseConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MineBaseConfigObject getByType(int type) {

        return _ix_type.get(type);

    }


    public void putToMem(Map e, MineBaseConfigObject config) {

        config.setType(MapHelper.getInt(e, "Type"));

        config.setQuality(MapHelper.getInt(e, "Quality"));

        config.setLevel(MapHelper.getInt(e, "Level"));

        config.setRefreshtime(MapHelper.getInt(e, "RefreshTime"));

        config.setNeedexploittime(MapHelper.getInt(e, "NeedExploitTime"));

        config.setDecexploittime(MapHelper.getInt(e, "DecExploitTime"));

        config.setPvefightmakeid(MapHelper.getInt(e, "PveFightMakeId"));

        config.setPvpfightmakeid(MapHelper.getInt(e, "PvpFightMakeId"));

        config.setDailyminerewardlist(MapHelper.getInts(e, "DailyMineRewardList"));

        config.setNormalrewardlist(MapHelper.getIntArray(e, "NormalRewardList"));

        config.setDefendfailreward(MapHelper.getInts(e, "DefendFailReward"));


        _ix_type.put(config.getType(), config);


    }
}
