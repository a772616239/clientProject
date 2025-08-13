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

@annationInit(value = "BraveChallengePoint", methodname = "initConfig")
public class BraveChallengePoint extends baseConfig<BraveChallengePointObject> {


    private static BraveChallengePoint instance = null;

    public static BraveChallengePoint getInstance() {

        if (instance == null)
            instance = new BraveChallengePoint();
        return instance;

    }


    public static Map<Integer, BraveChallengePointObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (BraveChallengePoint) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "BraveChallengePoint");

        for (Map e : ret) {
            put(e);
        }

    }

    public static BraveChallengePointObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, BraveChallengePointObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setPointtype(MapHelper.getInt(e, "pointType"));

        config.setRiviselv(MapHelper.getInt(e, "riviseLv"));

        config.setFightmake(MapHelper.getInt(e, "fightMake"));

        config.setNeedboss(MapHelper.getBoolean(e, "needBoss"));

        config.setExproperty(MapHelper.getInts(e, "exProperTy"));


        _ix_id.put(config.getId(), config);

        if (maxPoint == 0 || config.getId() > maxPoint) {
            maxPoint = config.getId();
        }

    }

    public static int maxPoint;
}
