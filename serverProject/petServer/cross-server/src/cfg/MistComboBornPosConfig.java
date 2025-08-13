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

@annationInit(value = "MistComboBornPosConfig", methodname = "initConfig")
public class MistComboBornPosConfig extends baseConfig<MistComboBornPosConfigObject> {


    private static MistComboBornPosConfig instance = null;

    public static MistComboBornPosConfig getInstance() {

        if (instance == null)
            instance = new MistComboBornPosConfig();
        return instance;

    }


    public static Map<Integer, MistComboBornPosConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistComboBornPosConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistComboBornPosConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistComboBornPosConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MistComboBornPosConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setLevel(MapHelper.getInt(e, "level"));

        config.setObjtype(MapHelper.getInt(e, "objType"));

        config.setMasterobjpos(MapHelper.getInts(e, "masterObjPos"));

        config.setSlaveobjposlist(MapHelper.getIntArray(e, "slaveObjPosList"));

        config.setPlayerrebornposlist(MapHelper.getIntArray(e, "playerRebornPosList"));


        _ix_id.put(config.getId(), config);

        List<MistComboBornPosConfigObject> cfgList = levelCfgMap.get(config.getLevel());
        if (null == cfgList) {
            cfgList = new ArrayList<>();
            levelCfgMap.put(config.getLevel(), cfgList);
        }
        cfgList.add(config);
    }

    protected HashMap<Integer, List<MistComboBornPosConfigObject>> levelCfgMap = new HashMap<>();

    public List<MistComboBornPosConfigObject> getCfgListByLevel(int level) {
        return levelCfgMap.get(level);
    }
}