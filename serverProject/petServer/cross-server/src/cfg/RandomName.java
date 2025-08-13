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

@annationInit(value = "RandomName", methodname = "initConfig")
public class RandomName extends baseConfig<RandomNameObject> {


    private static RandomName instance = null;

    public static RandomName getInstance() {

        if (instance == null)
            instance = new RandomName();
        return instance;

    }

    public static Map<Integer, RandomNameObject> _ix_pos = new HashMap<>();

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (RandomName) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "RandomName");

        for (Map e : ret) {
            put(e);
        }

    }

    public static RandomNameObject getByPos(int pos) {

        return _ix_pos.get(pos);

    }

    public void putToMem(Map e, RandomNameObject config) {

        config.setPos(MapHelper.getInt(e, "Pos"));

        config.setSvrnamestrid(MapHelper.getInts(e, "SvrNameStrId"));

        _ix_pos.put(config.getPos(), config);
    }

    public List<Integer> getNameList(int pos) {
        RandomNameObject nameList = _ix_pos.get(pos);
        if (nameList == null) {
            return null;
        }
        return nameList != null ? nameList.getSvrnamestrid() : null;
    }
}
