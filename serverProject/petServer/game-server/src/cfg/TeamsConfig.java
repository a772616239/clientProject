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

@annationInit(value = "TeamsConfig", methodname = "initConfig")
public class TeamsConfig extends baseConfig<TeamsConfigObject> {


    private static TeamsConfig instance = null;

    public static TeamsConfig getInstance() {

        if (instance == null)
            instance = new TeamsConfig();
        return instance;

    }


    public static Map<Integer, TeamsConfigObject> _ix_teamid = new HashMap<Integer, TeamsConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (TeamsConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "TeamsConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static TeamsConfigObject getByTeamid(int teamid) {

        return _ix_teamid.get(teamid);

    }


    public void putToMem(Map e, TeamsConfigObject config) {

        config.setTeamid(MapHelper.getInt(e, "TeamId"));

        config.setUnlockneedlv(MapHelper.getInt(e, "unlockNeedLv"));

        config.setUnlockneeddiamond(MapHelper.getInt(e, "unlockNeedDiamond"));


        _ix_teamid.put(config.getTeamid(), config);


    }
}
