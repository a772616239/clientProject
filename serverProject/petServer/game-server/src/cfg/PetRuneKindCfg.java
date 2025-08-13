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

@annationInit(value = "PetRuneKindCfg", methodname = "initConfig")
public class PetRuneKindCfg extends baseConfig<PetRuneKindCfgObject> {


    private static PetRuneKindCfg instance = null;

    public static PetRuneKindCfg getInstance() {

        if (instance == null)
            instance = new PetRuneKindCfg();
        return instance;

    }


    public static Map<Integer, PetRuneKindCfgObject> _ix_runekind = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetRuneKindCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetRuneKindCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetRuneKindCfgObject getByRunekind(int runekind) {

        return _ix_runekind.get(runekind);

    }


    public void putToMem(Map e, PetRuneKindCfgObject config) {

        config.setRunekind(MapHelper.getInt(e, "runeKind"));

        config.setSuitids(MapHelper.getInts(e, "suitIds"));

        _ix_runekind.put(config.getRunekind(), config);

        int[] suitIdArr = config.getSuitids();
        if(suitIdArr != null && suitIdArr.length > 0){
            for (int suitId:suitIdArr) {
                _ix_suitIdKeyMap.put(suitId,config);
            }
            for (int i = 0; i < suitIdArr.length - 1; i++) {
                _ix_nextSuitIdMap.put(suitIdArr[i], suitIdArr[i+1]);
            }
        }

    }

    public static Map<Integer, PetRuneKindCfgObject> _ix_suitIdKeyMap = new HashMap<>();
    public static PetRuneKindCfgObject getByRuneSuitId(int suitId) {
        return _ix_suitIdKeyMap.get(suitId);

    }

    public static Map<Integer, Integer> _ix_nextSuitIdMap = new HashMap<>();
    public static int getNextRuneSuitId(int suitId) {
        Integer nextSuitId = _ix_nextSuitIdMap.get(suitId);
        return nextSuitId > 0? nextSuitId:0;
    }

}
