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

@annationInit(value = "PetRuneRairtyUpCfg", methodname = "initConfig")
public class PetRuneRairtyUpCfg extends baseConfig<PetRuneRairtyUpCfgObject> {


    private static PetRuneRairtyUpCfg instance = null;

    public static PetRuneRairtyUpCfg getInstance() {

        if (instance == null)
            instance = new PetRuneRairtyUpCfg();
        return instance;

    }


    public static Map<Integer, PetRuneRairtyUpCfgObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetRuneRairtyUpCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetRuneRairtyUpCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetRuneRairtyUpCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetRuneRairtyUpCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setRunekind(MapHelper.getInt(e, "runeKind"));

        config.setRairty(MapHelper.getInt(e, "rairty"));

        config.setConsumes(MapHelper.getIntArray(e, "consumes"));


        _ix_id.put(config.getId(), config);

        Map<Integer, PetRuneRairtyUpCfgObject> allRarityMap = _ix_kind_rarity_map.get(config.getRunekind());
        if(allRarityMap == null){
            allRarityMap = new ConcurrentHashMap<>();
        }
        allRarityMap.put(config.getRairty(),config);
        _ix_kind_rarity_map.put(config.getRunekind(),allRarityMap);
    }

    public static Map<Integer, Map<Integer, PetRuneRairtyUpCfgObject>> _ix_kind_rarity_map = new ConcurrentHashMap<>();
    public static PetRuneRairtyUpCfgObject getCfgByKindAndRairty(int kind,int rairty){
        Map<Integer, PetRuneRairtyUpCfgObject> allRarityMap = _ix_kind_rarity_map.get(kind);
        if(allRarityMap == null){
            return null;
        }
        return allRarityMap.get(rairty);
    }

}
