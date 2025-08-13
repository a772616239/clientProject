/*CREATED BY TOOL*/

package cfg;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import util.LogUtil;

@annationInit(value = "CrossArenaExpCfg", methodname = "initConfig")
public class CrossArenaExpCfg extends baseConfig<CrossArenaExpCfgObject> {


    private static CrossArenaExpCfg instance = null;

    public static CrossArenaExpCfg getInstance() {

        if (instance == null)
            instance = new CrossArenaExpCfg();
        return instance;

    }


    public static Map<Integer, CrossArenaExpCfgObject> _ix_key = new HashMap<>();

    /**
     * 跟分段算水电费有点像
     * @param weekBat
     * @return
     */
    public static int getExpByTotalNum(int weekBat) {
        if (weekBat > 9998) {
            LogUtil.warn("player week battle num gt than 9999,please check player data");
            weekBat = 9998;
        }
        int exp = 0;
        int lastCfgNum = 0;
        for (CrossArenaExpCfgObject cfg : _ix_key.values()) {
            if (cfg.getNum() < weekBat) {
                exp += (cfg.getNum() - lastCfgNum) * cfg.getNumvar();
            } else {
                exp += (weekBat - lastCfgNum) * cfg.getNumvar();
                break;
            }
            lastCfgNum = cfg.getNum();
        }
        return exp;
    }

    public static int getExpByWinRate(int winRate, int weekBat) {
        CrossArenaExpCfgObject cfg = _ix_key.values().stream().filter(expCfg -> expCfg.getWinrate() <= winRate)
                .max(Comparator.comparingInt(CrossArenaExpCfgObject::getWinrate)).orElse(null);

        if (cfg == null) {
            return 0;
        }
        return cfg.getWinratevar() * weekBat;
    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CrossArenaExpCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CrossArenaExpCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static CrossArenaExpCfgObject getByKey(int key) {

        return _ix_key.get(key);

    }


    public void putToMem(Map e, CrossArenaExpCfgObject config) {

        config.setKey(MapHelper.getInt(e, "key"));

        config.setNum(MapHelper.getInt(e, "num"));

        config.setNumvar(MapHelper.getInt(e, "numVar"));

        config.setWinrate(MapHelper.getInt(e, "winRate"));

        config.setWinratevar(MapHelper.getInt(e, "winRateVar"));


        _ix_key.put(config.getKey(), config);


    }
}
