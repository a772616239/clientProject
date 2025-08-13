/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import model.base.baseConfig;

@annationInit(value = "MistDropItemConfig", methodname = "initConfig")
public class MistDropItemConfig extends baseConfig<MistDropItemConfigObject> {


    private static MistDropItemConfig instance = null;

    public static MistDropItemConfig getInstance() {

        if (instance == null)
            instance = new MistDropItemConfig();
        return instance;

    }


    public static Map<Integer, MistDropItemConfigObject> _ix_dropindex = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistDropItemConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistDropItemConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistDropItemConfigObject getByDropindex(int dropindex) {

        return _ix_dropindex.get(dropindex);

    }


    public void putToMem(Map e, MistDropItemConfigObject config) {

        config.setDropindex(MapHelper.getInt(e, "DropIndex"));

        config.setDropnum(MapHelper.getInt(e, "DropNum"));

        config.setPkdropodds(MapHelper.getInt(e, "PkDropOdds"));

        config.setNormaldropodds(MapHelper.getInt(e, "NormalDropOdds"));

        _ix_dropindex.put(config.getDropindex(), config);

        pkTotalOdds += config.getPkdropodds();

        normalTotalOdds += config.getNormaldropodds();
    }

    protected int pkTotalOdds;

    protected int normalTotalOdds;

    public int calcDropNum(boolean isPkMode) {
        int calcOdds = 0;
        Random random = new Random();
        int randOdds = isPkMode ? random.nextInt(pkTotalOdds) : random.nextInt(normalTotalOdds);
        for (MistDropItemConfigObject config : _ix_dropindex.values()) {
            calcOdds += isPkMode ? config.getPkdropodds() : config.getNormaldropodds();
            if (calcOdds > randOdds) {
                return config.getDropnum();
            }
        }
        return 0;
    }
}
