/*CREATED BY TOOL*/

package cfg;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import common.GameConst;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;

@annationInit(value = "Head", methodname = "initConfig")
public class Head extends baseConfig<HeadObject> {


    private static Head instance = null;

    public static Head getInstance() {

        if (instance == null)
            instance = new Head();
        return instance;

    }


    public static Map<Integer, HeadObject> _ix_id = new HashMap<>();

    public static List<Integer> canRandomHeads = new ArrayList<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (Head) o;
        initConfig();
        initCanRandomHeads();
    }

    private void initCanRandomHeads() {
        for (HeadObject cfg : _ix_id.values()) {
            if (cfg.getDisabledrandom()==0){
                canRandomHeads.add(cfg.getId());
            }
        }
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "Head");

        for (Map e : ret) {
            put(e);
        }

    }

    public static HeadObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, HeadObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setDisabledrandom(MapHelper.getInt(e, "disabledRandom"));


        _ix_id.put(config.getId(), config);


    }


    public static String getNameById(int cfgId) {
        return String.valueOf(cfgId);
    }

    public static int randomGetAvatar() {
        if (CollectionUtils.isEmpty(canRandomHeads)) {
            return GameConfig.getById(GameConst.CONFIG_ID).getDefaultavatarid();
        }
        return canRandomHeads.get(RandomUtils.nextInt(canRandomHeads.size()));
    }
}
