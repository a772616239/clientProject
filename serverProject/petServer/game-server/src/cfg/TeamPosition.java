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

@annationInit(value = "TeamPosition", methodname = "initConfig")
public class TeamPosition extends baseConfig<TeamPositionObject> {


    public static Map<Integer, TeamPositionObject> _ix_positionid = new HashMap<Integer, TeamPositionObject>();
    private static TeamPosition instance = null;

    public static TeamPosition getInstance() {

        if (instance == null)
            instance = new TeamPosition();
        return instance;

    }

    public static TeamPositionObject getByPositionid(int positionid) {

        return _ix_positionid.get(positionid);

    }

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (TeamPosition) o;
        initConfig();
    }

    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "TeamPosition");

        for (Map e : ret) {
            put(e);
        }

    }


    public void putToMem(Map e, TeamPositionObject config) {

        config.setPositionid(MapHelper.getInt(e, "positionId"));

        config.setUnlocklv(MapHelper.getInt(e, "unlocklv"));


        _ix_positionid.put(config.getPositionid(), config);

        setConfig(config);
    }



    public Map<Integer, TeamPositionObject> getAllConfig(){
        return _ix_positionid;
    }

    /**
     * =================================================================
     * @return
     */
    private void setConfig(TeamPositionObject config) {
        if (config == null) {
            return;
        }

        if (config.getPositionid() > totalPosition) {
            totalPosition = config.getPositionid();
        }
    }

    public static int totalPosition;
}
