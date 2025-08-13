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

@annationInit(value = "PatrolBuffDetail", methodname = "initConfig")
public class PatrolBuffDetail extends baseConfig<PatrolBuffDetailObject> {


    private static PatrolBuffDetail instance = null;

    public static PatrolBuffDetail getInstance() {

        if (instance == null)
            instance = new PatrolBuffDetail();
        return instance;

    }


    public static Map<Integer, PatrolBuffDetailObject> _ix_buffid = new HashMap<Integer, PatrolBuffDetailObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PatrolBuffDetail) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PatrolBuffDetail");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PatrolBuffDetailObject getByBuffid(int buffid) {

        return _ix_buffid.get(buffid);

    }


    public void putToMem(Map e, PatrolBuffDetailObject config) {

        config.setBuffid(MapHelper.getInt(e, "buffId"));

        config.setBuffcamp(MapHelper.getInt(e, "buffCamp"));

        config.setBufftype(MapHelper.getInt(e, "buffType"));

        config.setEffecttype(MapHelper.getInt(e, "effectType"));

        config.setEffectratge(MapHelper.getInt(e, "effectRatge"));

        config.setBuffmaxcount(MapHelper.getInt(e, "buffMaxCount"));


        _ix_buffid.put(config.getBuffid(), config);


    }
}
