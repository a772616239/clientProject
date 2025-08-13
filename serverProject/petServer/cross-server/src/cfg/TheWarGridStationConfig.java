/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;

@annationInit(value = "TheWarGridStationConfig", methodname = "initConfig")
public class TheWarGridStationConfig extends baseConfig<TheWarGridStationConfigObject> {


    private static TheWarGridStationConfig instance = null;

    public static TheWarGridStationConfig getInstance() {

        if (instance == null)
            instance = new TheWarGridStationConfig();
        return instance;

    }


    public static Map<Integer, TheWarGridStationConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (TheWarGridStationConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "TheWarGridStationConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static TheWarGridStationConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, TheWarGridStationConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setGroup(MapHelper.getInt(e, "group"));

        config.setQuality(MapHelper.getInt(e, "Quality"));

        config.setWargoldplus(MapHelper.getInt(e, "WarGoldPlus"));

        config.setHolywaterplus(MapHelper.getInt(e, "HolyWaterPlus"));

        config.setDpplus(MapHelper.getInt(e, "DPPlus"));

        config.setCommonbuffid(MapHelper.getInt(e, "CommonBuffID"));

        config.setRacebuffid(MapHelper.getIntArray(e, "RaceBuffID"));


        _ix_id.put(config.getId(), config);


    }

    public TheWarGridStationConfigObject getTroopsBuffConfigByGroupAndRace(int groupId, int quality) {
        for (TheWarGridStationConfigObject cfg : _ix_id.values()) {
            if (cfg.getGroup() == groupId && cfg.getQuality() == quality) {
                return cfg;
            }
        }
        return null;
    }

    public List<Integer> getTroopsBuffListByPetInfo(int groupId, int quality, int petCfgId) {
        TheWarGridStationConfigObject cfg = getTroopsBuffConfigByGroupAndRace(groupId, quality);
        if (cfg == null) {
            return null;
        }
        List<Integer> buffList = new ArrayList<>();
        if (cfg.getCommonbuffid() > 0) {
            buffList.add(cfg.getCommonbuffid());
        }

        PetBasePropertiesObject petBaseCfg = PetBaseProperties.getByPetid(petCfgId);
        if (petBaseCfg != null) {
            for (int i = 0; i < cfg.getRacebuffid().length; i++) {
                if (cfg.getRacebuffid()[i] == null || cfg.getRacebuffid()[i].length < 2 || cfg.getRacebuffid()[i][0] != petBaseCfg.getPettype() || cfg.getRacebuffid()[i][1] <= 0) {
                    continue;
                }
                buffList.add(cfg.getRacebuffid()[i][1]);
            }
        }
        return buffList;
    }
}
