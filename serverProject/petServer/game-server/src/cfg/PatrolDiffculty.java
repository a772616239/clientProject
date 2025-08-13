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

@annationInit(value = "PatrolDiffculty", methodname = "initConfig")
public class PatrolDiffculty extends baseConfig<PatrolDiffcultyObject> {


    private static PatrolDiffculty instance = null;

    public static PatrolDiffculty getInstance() {

        if (instance == null)
            instance = new PatrolDiffculty();
        return instance;

    }


    public static Map<Integer, PatrolDiffcultyObject> _ix_bastard = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PatrolDiffculty) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PatrolDiffculty");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PatrolDiffcultyObject getByBastard(int bastard) {

        return _ix_bastard.get(bastard);

    }


    public void putToMem(Map e, PatrolDiffcultyObject config) {

        config.setBastard(MapHelper.getInt(e, "bastard"));

        config.setBuffid(MapHelper.getInt(e, "buffId"));


        _ix_bastard.put(config.getBastard(), config);


    }
}
