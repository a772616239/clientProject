/*CREATED BY TOOL*/

package cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import util.ArrayUtil;

@annationInit(value = "PatrolMapLine", methodname = "initConfig")
public class PatrolMapLine extends baseConfig<PatrolMapLineObject> {


    private static PatrolMapLine instance = null;

    public static PatrolMapLine getInstance() {

        if (instance == null)
            instance = new PatrolMapLine();
        return instance;

    }


    public static Map<Integer, PatrolMapLineObject> _ix_lineid = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PatrolMapLine) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PatrolMapLine");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PatrolMapLineObject getByLineid(int lineid) {

        return _ix_lineid.get(lineid);

    }


    public void putToMem(Map e, PatrolMapLineObject config) {

        config.setLineid(MapHelper.getInt(e, "lineId"));

        config.setPointlist(MapHelper.getIntArray(e, "pointList"));


        _ix_lineid.put(config.getLineid(), config);


    }

    public static int[][] getPointList(int id) {
        PatrolMapLineObject lineObject = getByLineid(id);
        if (lineObject == null) {
            return ArrayUtil.emptyIntList2;
        }
        return lineObject.getPointlist();
    }

}
