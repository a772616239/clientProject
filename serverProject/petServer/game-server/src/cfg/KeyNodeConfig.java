/*CREATED BY TOOL*/

package cfg;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.Getter;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import org.apache.commons.lang.ObjectUtils;

@annationInit(value = "KeyNodeConfig", methodname = "initConfig")
public class KeyNodeConfig extends baseConfig<KeyNodeConfigObject> {


    private static KeyNodeConfig instance = null;

    public static KeyNodeConfig getInstance() {

        if (instance == null)
            instance = new KeyNodeConfig();
        return instance;

    }


    public static Map<Integer, KeyNodeConfigObject> _ix_id = new HashMap<>();

    private List<Integer> sortKeyNode = Collections.emptyList();

    @Getter
    private  int maxKeyNode ;

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (KeyNodeConfig) o;
        initConfig();
        sortKeyNode = _ix_id.keySet().stream().distinct().sorted(Integer::compareTo).collect(Collectors.toList());
        maxKeyNode = sortKeyNode.get(sortKeyNode.size()-1);
    }

    public int findNextKeyNodeId(int keyNode) {
        if (keyNode==maxKeyNode){
            return keyNode;
        }
        int index = sortKeyNode.indexOf(keyNode);
        if (index < 0) {
            return -1;
        }
        return (int) ObjectUtils.defaultIfNull(sortKeyNode.get(index + 1), -1);
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "KeyNodeConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static KeyNodeConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, KeyNodeConfigObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setMainlinenodeid(MapHelper.getInt(e, "mainlineNodeId"));

        config.setReward(MapHelper.getIntArray(e, "reward"));

        config.setMissionids(MapHelper.getInts(e, "missionIds"));


        _ix_id.put(config.getId(), config);


    }
}
