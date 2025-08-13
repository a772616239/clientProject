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
import util.LogUtil;

@annationInit(value = "FightMake", methodname = "initConfig")
public class FightMake extends baseConfig<FightMakeObject> {


    private static FightMake instance = null;

    public static FightMake getInstance() {

        if (instance == null)
            instance = new FightMake();
        return instance;

    }


    public static Map<Integer, FightMakeObject> _ix_id = new HashMap<Integer, FightMakeObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (FightMake) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "FightMake");

        for (Map e : ret) {
            put(e);
        }

    }

    public static FightMakeObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, FightMakeObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setType(MapHelper.getInt(e, "Type"));

        config.setRewardid(MapHelper.getInt(e, "RewardId"));

        config.setNeedfightpower(MapHelper.getInt(e, "NeedFightPower"));

        config.setEnemydata(MapHelper.getIntArray(e, "EnemyData"));

        config.setMonsterpropertyext(MapHelper.getIntArray(e, "MonsterPropertyExt"));


        _ix_id.put(config.getId(), config);


    }

    public long getNeedFightPowerById(int fightMakeId, int level, int star) {
        FightMakeObject cfg = getById(fightMakeId);
        if (cfg == null) {
            return 0;
        }
        if (cfg.getNeedfightpower() > 0) {
            return cfg.getNeedfightpower();
        } else {
            return cfg.getDynamicFightPower(level, star);
        }
    }

    public boolean afterInit() {
        try {
//            _ix_id.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry->entry.getValue().calcFightPower());
            for (FightMakeObject cfgObj : _ix_id.values()) {
                cfgObj.calcFightPower();
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}
