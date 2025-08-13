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
import protocol.Battle.BattleSubTypeEnum;
import util.LogUtil;

@annationInit(value = "BattleSubTypeConfig", methodname = "initConfig")
public class BattleSubTypeConfig extends baseConfig<BattleSubTypeConfigObject> {


    private static BattleSubTypeConfig instance = null;

    public static BattleSubTypeConfig getInstance() {

        if (instance == null)
            instance = new BattleSubTypeConfig();
        return instance;

    }


    public static Map<Integer, BattleSubTypeConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (BattleSubTypeConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "BattleSubTypeConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static BattleSubTypeConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, BattleSubTypeConfigObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setHasmonsterbondbuff(MapHelper.getBoolean(e, "HasMonsterBondBuff"));

        config.setSupportedplayback(MapHelper.getBoolean(e, "supportedPlayBack"));


        _ix_id.put(config.getId(), config);


    }

    public static boolean needRecord(BattleSubTypeEnum subTypeEnum) {
        if (subTypeEnum == null) {
            return false;
        }

        BattleSubTypeConfigObject config = getById(subTypeEnum.getNumber());
        if (config == null) {
            LogUtil.error("cfg.BattleSubTypeConfig.needRecord, battle sub type:" + subTypeEnum + ", have no config");
            return false;
        }
        return config.getSupportedplayback();
    }
}
