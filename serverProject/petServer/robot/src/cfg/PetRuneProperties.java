/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import datatool.MapHelper;
import model.base.baseConfig;
import petrobot.util.ServerConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "PetRuneProperties", methodname = "initConfig")
public class PetRuneProperties extends baseConfig<PetRunePropertiesObject> {


    private static PetRuneProperties instance = null;

    public static PetRuneProperties getInstance() {

        if (instance == null)
            instance = new PetRuneProperties();
        return instance;

    }


    public static Map<Integer, PetRunePropertiesObject> _ix_runeid = new HashMap<Integer, PetRunePropertiesObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetRuneProperties) o;
        initConfig();
    }


    public void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetRuneProperties");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetRunePropertiesObject getByRuneid(int runeid) {

        return _ix_runeid.get(runeid);

    }


    public void putToMem(Map e, PetRunePropertiesObject config) {

        config.setRuneid(MapHelper.getInt(e, "runeId"));

        config.setRunename(MapHelper.getInt(e, "runeName"));

        config.setSevername(MapHelper.getStr(e, "severName"));

        config.setRunerarity(MapHelper.getInt(e, "runeRarity"));

        config.setRunetype(MapHelper.getInt(e, "runeType"));

        config.setBaseproperties(MapHelper.getIntArray(e, "baseProperties"));

        config.setExpropertiesrange(MapHelper.getInts(e, "exPropertiesRange"));

        config.setExproperties(MapHelper.getIntArray(e, "exProperties"));

        config.setRunesuit(MapHelper.getInt(e, "runeSuit"));


        _ix_runeid.put(config.getRuneid(), config);


    }

    public static String getNameById(int cfgId) {
        PetRunePropertiesObject byRuneid = getByRuneid(cfgId);
        if (byRuneid == null) {
            return String.valueOf(cfgId);
        }
        return byRuneid.getSevername();
    }
}
