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

@annationInit(value = "ArtifactMapExpConfig", methodname = "initConfig")
public class ArtifactMapExpConfig extends baseConfig<ArtifactMapExpConfigObject> {


    private static ArtifactMapExpConfig instance = null;

    public static ArtifactMapExpConfig getInstance() {

        if (instance == null)
            instance = new ArtifactMapExpConfig();
        return instance;

    }


    public static Map<Integer, ArtifactMapExpConfigObject> _ix_id = new HashMap<>();
    public static int getStarUpExp(int lastSkillLv, int nowSkillLv) {
        int exp = 0;
        ArtifactMapExpConfigObject cfg;
        for (int i = lastSkillLv + 1; i <= nowSkillLv; i++) {
            cfg = getById(i);
            if (cfg != null) {
                exp += cfg.getExp();
            }
        }
        return exp;
    }

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ArtifactMapExpConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ArtifactMapExpConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ArtifactMapExpConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, ArtifactMapExpConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setStar(MapHelper.getInt(e, "star"));

        config.setExp(MapHelper.getInt(e, "exp"));


        _ix_id.put(config.getId(), config);


    }
}
