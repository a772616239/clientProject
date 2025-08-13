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

@annationInit(value = "MistObjDefaultProp", methodname = "initConfig")
public class MistObjDefaultProp extends baseConfig<MistObjDefaultPropObject> {


    private static MistObjDefaultProp instance = null;

    public static MistObjDefaultProp getInstance() {

        if (instance == null)
            instance = new MistObjDefaultProp();
        return instance;

    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistObjDefaultProp) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistObjDefaultProp");

        for (Map e : ret) {
            put(e);
        }

    }


    public void putToMem(Map e, MistObjDefaultPropObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setObjtype(MapHelper.getInt(e, "ObjType"));

        config.setInitpos(MapHelper.getInts(e, "InitPos"));

        config.setInittoward(MapHelper.getInts(e, "InitToward"));

        config.setDefaultprop(MapHelper.getIntArray(e, "DefaultProp"));

        typeMap.put(config.getObjtype(), config);
    }

    /**************************分割线*********************************/
    public Map<Integer, MistObjDefaultPropObject> typeMap = new HashMap<>();

    public MistObjDefaultPropObject getByType(int type) {
        return typeMap.get(type);
    }
}
