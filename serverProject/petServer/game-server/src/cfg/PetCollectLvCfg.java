/*CREATED BY TOOL*/

package cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import util.MapUtil;

@annationInit(value = "PetCollectLvCfg", methodname = "initConfig")
public class PetCollectLvCfg extends baseConfig<PetCollectLvCfgObject> {


    private static PetCollectLvCfg instance = null;

    public static PetCollectLvCfg getInstance() {

        if (instance == null)
            instance = new PetCollectLvCfg();
        return instance;

    }

    @Getter
    private int maxLv ;

    public static Map<Integer, PetCollectLvCfgObject> _ix_lv = new HashMap<>();

    //<图鉴等级,玩家属性加成>
    public static Map<Integer, Map<Integer,Integer>> lvAdditionMap = new HashMap<>();

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetCollectLvCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetCollectLvCfg");

        for (Map e : ret) {
            put(e);
        }
        maxLv = _ix_lv.values().stream().mapToInt(PetCollectLvCfgObject::getLv).max().orElse(0);
        initLvAdditionMap();
    }

    private void initLvAdditionMap() {
        for (PetCollectLvCfgObject cfg : _ix_lv.values()) {
            if (cfg.getLv()>0){
                Map<Integer, Integer> map = new HashMap<>();
                MapUtil.add2IntMapValue(map,cfg.getOfferproperty());
                lvAdditionMap.put(cfg.getLv(),map);
            }

        }
    }

    public static PetCollectLvCfgObject getByLv(int lv) {

        return _ix_lv.get(lv);

    }


    public void putToMem(Map e, PetCollectLvCfgObject config) {

        config.setLv(MapHelper.getInt(e, "lv"));

        config.setUpexp(MapHelper.getInt(e, "upExp"));

        config.setOfferproperty(MapHelper.getIntArray(e, "offerProperty"));


        _ix_lv.put(config.getLv(), config);


    }

    public int getUpLvExp(int lv) {
        PetCollectLvCfgObject cfg = getByLv(lv);
        if (lv==maxLv||cfg == null) {
            return Integer.MAX_VALUE;
        }
        return cfg.getUpexp();
    }

    public Map<Integer,Integer> getAdditionMap(int collectionLv){
        return lvAdditionMap.get(collectionLv);
    }

}
