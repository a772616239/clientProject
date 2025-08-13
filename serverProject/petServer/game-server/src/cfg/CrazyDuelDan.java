/*CREATED BY TOOL*/

package cfg;

import common.GameConst;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value = "CrazyDuelDan", methodname = "initConfig")
public class CrazyDuelDan extends baseConfig<CrazyDuelDanObject> {

    private static CrazyDuelDan instance = null;

    public static CrazyDuelDan getInstance() {

        if (instance == null)
            instance = new CrazyDuelDan();
        return instance;

    }


    public static Map<Integer, CrazyDuelDanObject> _ix_dan = new HashMap<>();

    public static int findOpenFloorByDan(int playerSceneId) {
        CrazyDuelDanObject byDan = getByDan(playerSceneId);
        if (byDan==null){
            return 1;
        }
        return byDan.getOpenfloor();

    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CrazyDuelDan) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CrazyDuelDan");

        for (Map e : ret) {
            put(e);
        }

    }

    public static CrazyDuelDanObject getByDan(int dan) {

        return _ix_dan.get(dan);

    }



    public void putToMem(Map e, CrazyDuelDanObject config) {

        config.setDan(MapHelper.getInt(e, "dan"));


        config.setOpenfloor(MapHelper.getInt(e, "openFloor"));

        config.setBuffpool(MapHelper.getInts(e, "buffPool"));

        config.setBuffnum(MapHelper.getInt(e, "buffNum"));

        config.setWinscore(MapHelper.getInt(e, "winScore"));

        config.setFailscore(MapHelper.getInt(e, "failScore"));


        _ix_dan.put(config.getDan(), config);


    }

}
