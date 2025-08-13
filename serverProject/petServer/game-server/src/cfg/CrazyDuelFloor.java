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

@annationInit(value = "CrazyDuelFloor", methodname = "initConfig")
public class CrazyDuelFloor extends baseConfig<CrazyDuelFloorObject> {


    private static CrazyDuelFloor instance = null;

    public static CrazyDuelFloor getInstance() {

        if (instance == null)
            instance = new CrazyDuelFloor();
        return instance;

    }


    public static Map<Integer, CrazyDuelFloorObject> _ix_floor = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CrazyDuelFloor) o;
        initConfig();
        maxFloor = _ix_floor.keySet().stream().max(Integer::compareTo).orElse(0);
    }


    private int maxFloor;

    public int getMaxFloor() {
        return maxFloor;
    }

    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CrazyDuelFloor");

        for (Map e : ret) {
            put(e);
        }

    }

    public static CrazyDuelFloorObject getByFloor(int floor) {

        return _ix_floor.get(floor);

    }


    public void putToMem(Map e, CrazyDuelFloorObject config) {

        config.setFloor(MapHelper.getInt(e, "floor"));

        config.setFixbuffpool(MapHelper.getInts(e, "fixBuffPool"));

        config.setFixbuffnum(MapHelper.getInt(e, "fixBuffNum"));

        config.setRandombuff(MapHelper.getIntArray(e, "randomBuff"));

        config.setRandumbuffnum(MapHelper.getInt(e, "randumBuffNum"));

        config.setLastbuffposrarity(MapHelper.getInt(e, "lastBuffPosRarity"));

        config.setExbuffposappeare(MapHelper.getInts(e, "exBuffPosAppeare"));

        config.setFightreward(MapHelper.getIntArray(e, "fightReward"));


        _ix_floor.put(config.getFloor(), config);


    }
}
