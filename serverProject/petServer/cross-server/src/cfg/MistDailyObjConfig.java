/*CREATED BY TOOL*/

package cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value = "MistDailyObjConfig", methodname = "initConfig")
public class MistDailyObjConfig extends baseConfig<MistDailyObjConfigObject> {


    private static MistDailyObjConfig instance = null;

    public static MistDailyObjConfig getInstance() {

        if (instance == null)
            instance = new MistDailyObjConfig();
        return instance;

    }


    public static Map<Integer, MistDailyObjConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistDailyObjConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistDailyObjConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistDailyObjConfigObject getById(int id) {

        return _ix_id.get(id);

    }

    public static List<MistDailyObjConfigObject> getAllConfig(int level) {

        List<MistDailyObjConfigObject> cfgList = null;
        for (MistDailyObjConfigObject cfgObj : _ix_id.values()) {
            if (cfgObj.getMaplevel() == 0 || cfgObj.getMaplevel() == level) {
                if (cfgList == null) {
                    cfgList = new ArrayList<>();
                }
                cfgList.add(cfgObj);
            }
        }
        return cfgList;

    }


    public void putToMem(Map e, MistDailyObjConfigObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setMaprule(MapHelper.getInt(e, "MapRule"));

        config.setMaplevel(MapHelper.getInt(e, "MapLevel"));

        config.setObjtype(MapHelper.getInt(e, "ObjType"));

        config.setCreatetimedata(MapHelper.getIntArray(e, "CreateTimeData"));

        config.setInitprop(MapHelper.getIntArray(e, "InitProp"));

        config.setInitRandProp(MapHelper.getInts(e, "InitRandProp"));


        _ix_id.put(config.getId(), config);


    }
}
