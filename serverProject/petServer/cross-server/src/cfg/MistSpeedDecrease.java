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

@annationInit(value = "MistSpeedDecrease", methodname = "initConfig")
public class MistSpeedDecrease extends baseConfig<MistSpeedDecreaseObject> {


    private static MistSpeedDecrease instance = null;

    public static MistSpeedDecrease getInstance() {

        if (instance == null)
            instance = new MistSpeedDecrease();
        return instance;

    }


    public static Map<Integer, MistSpeedDecreaseObject> _ix_mistlevel = new HashMap<Integer, MistSpeedDecreaseObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistSpeedDecrease) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistSpeedDecrease");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistSpeedDecreaseObject getByMistlevel(int mistlevel) {

        return _ix_mistlevel.get(mistlevel);

    }


    public void putToMem(Map e, MistSpeedDecreaseObject config) {

        config.setMistlevel(MapHelper.getInt(e, "MistLevel"));

        config.setBaseplayerlevel(MapHelper.getInt(e, "BasePlayerLevel"));

        config.setSpeeddecrease(MapHelper.getIntArray(e, "SpeedDecrease"));


        _ix_mistlevel.put(config.getMistlevel(), config);

    }

    public static long calcSpeedDecreasePercent(int mistLevel, int playerLevel, long baseSpeed) {
        MistSpeedDecreaseObject decreaseObject =  _ix_mistlevel.get(mistLevel);
        if (decreaseObject == null || decreaseObject.getSpeeddecrease() == null) {
            return baseSpeed;
        }
        int deltaLevel = decreaseObject.getBaseplayerlevel() - playerLevel;
        for (int i = 0; i < decreaseObject.getSpeeddecrease().length; i++) {
            int checkLevel = decreaseObject.getSpeeddecrease()[i][0];
            int decreasePercent = decreaseObject.getSpeeddecrease()[i][1];
            if (deltaLevel >= checkLevel) {
                baseSpeed = (100 - decreasePercent) * baseSpeed / 100;
                return baseSpeed;
            }
        }
        return baseSpeed;
    }
}
