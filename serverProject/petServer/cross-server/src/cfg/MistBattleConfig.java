/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import org.apache.commons.lang.math.RandomUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "MistBattleConfig", methodname = "initConfig")
public class MistBattleConfig extends baseConfig<MistBattleConfigObject> {


    private static MistBattleConfig instance = null;

    public static MistBattleConfig getInstance() {

        if (instance == null)
            instance = new MistBattleConfig();
        return instance;

    }


    public static Map<Integer, MistBattleConfigObject> _ix_id = new HashMap<Integer, MistBattleConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistBattleConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistBattleConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistBattleConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MistBattleConfigObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setLevel(MapHelper.getInt(e, "Level"));

        config.setBattletype(MapHelper.getInt(e, "BattleType"));

        config.setFightmakeid(MapHelper.getInts(e, "FightMakeId"));


        _ix_id.put(config.getId(), config);

    }

    public static int getFightMakeId(int mistLevel, int battleType) {
        for (MistBattleConfigObject battleCfg : _ix_id.values()) {
            if (battleCfg.getLevel() == mistLevel && battleCfg.getBattletype() == battleType) {
                int[] fightMakeList = battleCfg.getFightmakeid();
                if (fightMakeList != null && fightMakeList.length > 0) {
                    int rand = RandomUtils.nextInt(fightMakeList.length);
                    return fightMakeList[rand];
                }
                break;
            }
        }
        return 0;
    }
}
