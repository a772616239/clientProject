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

@annationInit(value = "PollingActivity", methodname = "initConfig")
public class PollingActivity extends baseConfig<PollingActivityObject> {


    private static PollingActivity instance = null;

    public static PollingActivity getInstance() {

        if (instance == null)
            instance = new PollingActivity();
        return instance;

    }


    public static Map<Integer, PollingActivityObject> _ix_id = new HashMap<Integer, PollingActivityObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PollingActivity) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PollingActivity");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PollingActivityObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PollingActivityObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setPollingtype(MapHelper.getInt(e, "pollingType"));

        config.setOpenday(MapHelper.getInts(e, "openDay"));

        config.setOpentime(MapHelper.getInt(e, "openTime"));

        config.setDurationtime(MapHelper.getInt(e, "durationTime"));

        config.setConsumeid(MapHelper.getInt(e, "consumeId"));


        _ix_id.put(config.getId(), config);

    }
}
