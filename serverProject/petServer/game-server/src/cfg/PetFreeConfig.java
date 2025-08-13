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

@annationInit(value = "PetFreeConfig", methodname = "initConfig")
public class PetFreeConfig extends baseConfig<PetFreeConfigObject> {


    private static PetFreeConfig instance = null;

    public static PetFreeConfig getInstance() {

        if (instance == null)
            instance = new PetFreeConfig();
        return instance;

    }


    public static Map<Integer, PetFreeConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetFreeConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetFreeConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetFreeConfigObject getById(int id) {

        return _ix_id.get(id);

    }

    public static PetFreeConfigObject getByTypeAndLvl(int type, int lvl) {
        for (PetFreeConfigObject value : _ix_id.values()) {
            if (value.getFreetype() == type && value.getLvl() == lvl) {
                return value;
            }
        }
        LogUtil.error("error in PetFreeConfig,method getByRarityAndStar(),type = " + type + ",lvl = " + lvl);
        return null;
    }


    public void putToMem(Map e, PetFreeConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setFreetype(MapHelper.getInt(e, "freeType"));

        config.setLvl(MapHelper.getInt(e, "lvl"));

        config.setRewardbylvl(MapHelper.getIntArray(e, "rewardByLvl"));

        config.setReorderbylvl(MapHelper.getIntArray(e, "reorderByLvl"));

        config.setPetcorereward(MapHelper.getIntArray(e, "petCorereward"));

        config.setPetcorereorder(MapHelper.getIntArray(e, "petCoreReorder"));


        _ix_id.put(config.getId(), config);


    }
}
