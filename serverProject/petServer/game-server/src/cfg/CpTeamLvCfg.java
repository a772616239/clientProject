/*CREATED BY TOOL*/

package cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value = "CpTeamLvCfg", methodname = "initConfig")
public class CpTeamLvCfg extends baseConfig<CpTeamLvCfgObject> {


    private static CpTeamLvCfg instance = null;

    public static CpTeamLvCfg getInstance() {

        if (instance == null)
            instance = new CpTeamLvCfg();
        return instance;

    }


    public static Map<Integer, CpTeamLvCfgObject> _ix_id = new HashMap<>();

    @Getter
    private Set<Integer> teamLvs = new HashSet<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CpTeamLvCfg) o;
        initConfig();
        teamLvs = _ix_id.keySet();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CpTeamLvCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static CpTeamLvCfgObject getById(int id) {

        return _ix_id.get(id);

    }

    public static int queryTeamLv(int level) {
        for (CpTeamLvCfgObject cfg : _ix_id.values()) {
            if (cfg.getLevell() <= level && cfg.getLevelh() > level) {
                return cfg.getId();
            }
        }
        return 0;

    }


    public void putToMem(Map e, CpTeamLvCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setLevell(MapHelper.getInt(e, "levelL"));

        config.setLevelh(MapHelper.getInt(e, "levelH"));


        _ix_id.put(config.getId(), config);


    }
}
