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

@annationInit(value = "PetUpConsume", methodname = "initConfig")
public class PetUpConsume extends baseConfig<PetUpConsumeObject> {


    private static PetUpConsume instance = null;

    public static PetUpConsume getInstance() {

        if (instance == null)
            instance = new PetUpConsume();
        return instance;

    }


    public static Map<Integer, PetUpConsumeObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetUpConsume) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetUpConsume");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetUpConsumeObject getById(int id) {

        return _ix_id.get(id);

    }

    public static PetUpConsumeObject getByTypeAndLvl(int type, int lvl) {
        for (PetUpConsumeObject value : _ix_id.values()) {
            if (value.getUptype() == type && value.getUplvl() == lvl) {
                return value;
            }
        }
        LogUtil.error("error in PetUpProperties,method getByTypeAndLvl():type = " + type + ",lvl = " + lvl + "\n");
        return null;
    }

    public static PetUpConsumeObject getByTypeAndLvl(int type, int lvl, int petBookId) {
        for (PetUpConsumeObject value : _ix_id.values()) {
            if (value.getUptype()==type&& value.getUplvl() == lvl && value.getPetcfgid() == petBookId) {
                return value;
            }
        }
        LogUtil.error("error in PetUpProperties,method getByTypeAndLvl():type = " + type + ",lvl = " + lvl + ",petBookId = " + petBookId + "\n");
        return null;
    }

    public void putToMem(Map e, PetUpConsumeObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setUptype(MapHelper.getInt(e, "upType"));

        config.setNeedexp(MapHelper.getInt(e, "needExp"));

        config.setNeedpetlv(MapHelper.getInt(e, "needPetLv"));

        config.setUplvl(MapHelper.getInt(e, "upLvl"));

        config.setPetcfgid(MapHelper.getInt(e, "petCfgId"));

        config.setUpconsume(MapHelper.getIntArray(e, "upConsume"));

        config.setUpsucrate(MapHelper.getInt(e, "upSucRate"));

        config.setExtraconsume(MapHelper.getIntArray(e, "extraConsume"));


        _ix_id.put(config.getId(), config);


    }
}
