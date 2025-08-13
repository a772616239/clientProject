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

@annationInit(value = "MineGiftBaseCfg", methodname = "initConfig")
public class MineGiftBaseCfg extends baseConfig<MineGiftBaseCfgObject> {


    private static MineGiftBaseCfg instance = null;

    public static MineGiftBaseCfg getInstance() {

        if (instance == null)
            instance = new MineGiftBaseCfg();
        return instance;

    }


    public static Map<Integer, MineGiftBaseCfgObject> _ix_cfgid = new HashMap<Integer, MineGiftBaseCfgObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MineGiftBaseCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MineGiftBaseCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MineGiftBaseCfgObject getByCfgid(int cfgid) {

        return _ix_cfgid.get(cfgid);

    }


    public void putToMem(Map e, MineGiftBaseCfgObject config) {

        config.setCfgid(MapHelper.getInt(e, "CfgId"));

        config.setExisttime(MapHelper.getInt(e, "ExistTime"));

        config.setEffecttime(MapHelper.getInt(e, "EffectTime"));

        config.setEffecttype(MapHelper.getInt(e, "EffectType"));

        config.setEffectvalue(MapHelper.getInt(e, "EffectValue"));


        _ix_cfgid.put(config.getCfgid(), config);


    }
}
