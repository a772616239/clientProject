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

@annationInit(value = "PatrolMap", methodname = "initConfig")
public class PatrolMap extends baseConfig<PatrolMapObject> {


    private static PatrolMap instance = null;

    public static PatrolMap getInstance() {

        if (instance == null)
            instance = new PatrolMap();
        return instance;

    }


    public static Map<Integer, PatrolMapObject> _ix_mapid = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PatrolMap) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PatrolMap");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PatrolMapObject getByMapid(int mapid) {

        return _ix_mapid.get(mapid);

    }


    public void putToMem(Map e, PatrolMapObject config) {

        config.setMapid(MapHelper.getInt(e, "mapId"));

        config.setMain(MapHelper.getInt(e, "main"));

        config.setBranch(MapHelper.getInts(e, "branch"));

        config.setPath(MapHelper.getInt(e, "path"));


        if (config.getMapid()<=0){
            return;
        }
        _ix_mapid.put(config.getMapid(), config);


    }
}
