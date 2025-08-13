/*CREATED BY TOOL*/

package cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value = "PlotAffect", methodname = "initConfig")
public class PlotAffect extends baseConfig<PlotAffectObject> {


    private static PlotAffect instance = null;

    public static PlotAffect getInstance() {

        if (instance == null)
            instance = new PlotAffect();
        return instance;

    }


    public static Map<Integer, PlotAffectObject> _ix_id = new HashMap<>();

    /**
     * <剧情id,<被替换的主线节点,替换的主线节点></>></>
     */
    public static Map<Integer, Map<Integer, Integer>> effectMap = new HashMap<>();

    public Map<Integer, Integer> queryEffectByPlot(int plotId) {
        return effectMap.get(plotId);
    }

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PlotAffect) o;
        initConfig();
        initEffectMap();
    }

    private void initEffectMap() {
        for (PlotAffectObject cfg : _ix_id.values()) {
            Map<Integer, Integer> effect = effectMap.computeIfAbsent(cfg.getPlotid(), a -> new HashMap<>());
            effect.put(cfg.getId(), cfg.getNewnodeid());
        }

    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PlotAffect");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PlotAffectObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PlotAffectObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setPlotid(MapHelper.getInt(e, "plotId"));

        config.setNewnodeid(MapHelper.getInt(e, "NewNodeId"));


        _ix_id.put(config.getId(), config);


    }
}
