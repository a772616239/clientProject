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

@annationInit(value = "ArtifactEnhancePointConfig", methodname = "initConfig")
public class ArtifactEnhancePointConfig extends baseConfig<ArtifactEnhancePointConfigObject> {


    private static ArtifactEnhancePointConfig instance = null;

    public static ArtifactEnhancePointConfig getInstance() {

        if (instance == null)
            instance = new ArtifactEnhancePointConfig();
        return instance;

    }


    public static Map<Integer, ArtifactEnhancePointConfigObject> _ix_key = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ArtifactEnhancePointConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ArtifactEnhancePointConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ArtifactEnhancePointConfigObject getByKey(int key) {

        return _ix_key.get(key);

    }


    public void putToMem(Map e, ArtifactEnhancePointConfigObject config) {

        config.setKey(MapHelper.getInt(e, "key"));

        config.setId(MapHelper.getInt(e, "Id"));

        config.setLevel(MapHelper.getInt(e, "Level"));

        config.setUpconsume(MapHelper.getInts(e, "upConsume"));

        config.setIncreaseproperty(MapHelper.getIntArray(e, "increaseProperty"));

        config.setNeedplayerlv(MapHelper.getInt(e, "NeedPlayerLv"));

        config.setCumuproperty(MapHelper.getIntArray(e, "cumuProperty"));


        _ix_key.put(config.getKey(), config);


    }
    public static ArtifactEnhancePointConfigObject getByPointAndLv(int pointId, int pointLevel) {
        for (ArtifactEnhancePointConfigObject config : _ix_key.values()) {
            if (config.getId() == pointId && config.getLevel() == pointLevel) {
                return config;
            }
        }
        return null;
    }
}
