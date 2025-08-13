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

@annationInit(value = "PetUpProperties", methodname = "initConfig")
public class PetUpProperties extends baseConfig<PetUpPropertiesObject> {


    private static PetUpProperties instance = null;

    public static PetUpProperties getInstance() {

        if (instance == null)
            instance = new PetUpProperties();
        return instance;

    }


    public static Map<Integer, PetUpPropertiesObject> _ix_id = new HashMap<Integer, PetUpPropertiesObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetUpProperties) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetUpProperties");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetUpPropertiesObject getById(int id) {
        return _ix_id.get(id);
    }

    public static PetUpPropertiesObject getByTypeAndLvl(String type, int lvl) {
        for (PetUpPropertiesObject value : _ix_id.values()) {
            if (value.getType().equals(type) && value.getLvl() == lvl) {
                return value;
            }
        }
        LogUtil.error("error in PetUpProperties,method getByTypeAndLvl(): type = " + type + ",lvl = " + lvl + "\n");
        throw new NullPointerException();
    }

    public static PetUpPropertiesObject getByCfgIdAndType(int petBookId, String type) {
        for (PetUpPropertiesObject value : _ix_id.values()) {
            if (value.getType().equals(type) && value.getPetbookid() == petBookId) {
                return value;
            }
        }
        LogUtil.error("error in PetUpProperties,method getByTypeAndLvl(): type = " + type + ",petBookId = " + petBookId + "\n");
        throw new NullPointerException();
    }


    public void putToMem(Map e, PetUpPropertiesObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setType(MapHelper.getStr(e, "type"));

        config.setLvl(MapHelper.getInt(e, "lvl"));

        config.setPetbookid(MapHelper.getInt(e, "petBookId"));

        config.setProperty(MapHelper.getIntArray(e, "property"));

        config.setAbility(MapHelper.getInt(e, "ability"));


        _ix_id.put(config.getId(), config);


    }
}
