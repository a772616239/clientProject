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

@annationInit(value = "GoldExchange", methodname = "initConfig")
public class GoldExchange extends baseConfig<GoldExchangeObject> {


    private static GoldExchange instance = null;

    public static GoldExchange getInstance() {

        if (instance == null)
            instance = new GoldExchange();
        return instance;

    }


    public static Map<Integer, GoldExchangeObject> _ix_exchangetimes = new HashMap<Integer, GoldExchangeObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (GoldExchange) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "GoldExchange");

        for (Map e : ret) {
            put(e);
        }

    }

    public static GoldExchangeObject getByExchangetimes(int exchangetimes) {

        return _ix_exchangetimes.get(exchangetimes);

    }


    public void putToMem(Map e, GoldExchangeObject config) {

        config.setExchangetimes(MapHelper.getInt(e, "exchangeTimes"));

        config.setExneeddiamond(MapHelper.getInt(e, "exNeedDiamond"));

        config.setTime(MapHelper.getInt(e, "time"));

        config.setVipaddtion(MapHelper.getBoolean(e, "VIPAddtion"));

        config.setMultipleexneeddiamond(MapHelper.getInt(e, "MultipleexNeedDiamond"));

        config.setMultiplenumber(MapHelper.getInt(e, "MultipleNumber"));


        _ix_exchangetimes.put(config.getExchangetimes(), config);

        putCfg(config);
    }

    /**
     * =============================================================================
     */

    public static GoldExchangeObject maxTimesCfg;

    public void putCfg(GoldExchangeObject newCfg) {
        if (newCfg == null) {
            return;
        }

        if (maxTimesCfg == null) {
            maxTimesCfg = newCfg;
            return;
        }

        if (newCfg.getExchangetimes() > maxTimesCfg.getExchangetimes()) {
            maxTimesCfg = newCfg;
        }
    }
}
