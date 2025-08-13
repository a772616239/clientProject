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

@annationInit(value = "PetRuneWorth", methodname = "initConfig")
public class PetRuneWorth extends baseConfig<PetRuneWorthObject> {


    private static PetRuneWorth instance = null;

    public static PetRuneWorth getInstance() {

        if (instance == null)
            instance = new PetRuneWorth();
        return instance;

    }


    public static Map<Integer, PetRuneWorthObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetRuneWorth) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetRuneWorth");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetRuneWorthObject getByRarityAndLevel(int rarity, int level) {
        return _ix_id.values().stream().filter(e -> e.getRunelvl() == level && e.getRunerarity() == rarity).
                findFirst().orElse(null);

    }


    public void putToMem(Map e, PetRuneWorthObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setRunerarity(MapHelper.getInt(e, "runeRarity"));

        config.setRunelvl(MapHelper.getInt(e, "runeLvl"));

        config.setRunesale(MapHelper.getIntArray(e, "runeSale"));


        _ix_id.put(config.getId(), config);


    }
}
