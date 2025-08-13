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

@annationInit(value = "ArtifactStarConfig", methodname = "initConfig")
public class ArtifactStarConfig extends baseConfig<ArtifactStarConfigObject> {


    private static ArtifactStarConfig instance = null;

    public static ArtifactStarConfig getInstance() {

        if (instance == null)
            instance = new ArtifactStarConfig();
        return instance;

    }


    public static Map<Integer, ArtifactStarConfigObject> _ix_key = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ArtifactStarConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ArtifactStarConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ArtifactStarConfigObject getByKey(int key) {

        return _ix_key.get(key);

    }


    public void putToMem(Map e, ArtifactStarConfigObject config) {

        config.setKey(MapHelper.getInt(e, "key"));

        config.setUpconsume(MapHelper.getInts(e, "upConsume"));

        config.setIncreaseproperty(MapHelper.getIntArray(e, "increaseProperty"));

        config.setCumuproperty(MapHelper.getIntArray(e, "cumuProperty"));


        _ix_key.put(config.getKey(), config);


    }

    public static ArtifactStarConfigObject getByArtifactIdAndStar(int artifactId, int star) {

        ArtifactConfigObject artifactConfig = ArtifactConfig.getByKey(artifactId);
        if (artifactConfig == null) {
            return null;
        }
        if (artifactConfig.getStarconfgid().length < star) {
            return null;
        }
        int configKey = artifactConfig.getStarconfgid()[star];
        return getByKey(configKey);
    }

}
