/*CREATED BY TOOL*/

package cfg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value = "MainLineEpisodeConfig", methodname = "initConfig")
public class MainLineEpisodeConfig extends baseConfig<MainLineEpisodeConfigObject> {


    private static MainLineEpisodeConfig instance = null;

    public static MainLineEpisodeConfig getInstance() {

        if (instance == null)
            instance = new MainLineEpisodeConfig();
        return instance;

    }

    private static Map<Integer, Integer> nodeEpisodeIdMap = new HashMap<>();

    public static Map<Integer, MainLineEpisodeConfigObject> _ix_id = new HashMap<>();

    public static int getEpisodeStartByNode(int episodeId) {
        MainLineEpisodeConfigObject cfg = getById(episodeId);
        if (cfg == null) {
            return 0;
        }
        if (cfg.getEpisondenodeid().length <= 0) {
            return 0;
        }

        return cfg.getEpisondenodeid()[0];
    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MainLineEpisodeConfig) o;
        initConfig();
        initCache();
    }

    private void initCache() {
        for (MainLineEpisodeConfigObject cfg : _ix_id.values()) {
            if (cfg.getId() <= 0) {
                continue;
            }
            for (int node : cfg.getEpisondenodeid()) {
                nodeEpisodeIdMap.put(node, cfg.getId());
            }
        }

    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MainLineEpisodeConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static Integer getEpisodeIdByNode(int nodeId) {
        return nodeEpisodeIdMap.get(nodeId);
    }

    public static int getEpisodeNextNode(int nodeId) {
        Integer episodeId = nodeEpisodeIdMap.get(nodeId);
        if (episodeId == null) {
            return -1;
        }
        MainLineEpisodeConfigObject cfg = getById(episodeId);
        if (cfg == null) {
            return -1;
        }
        return getArrayNextElement(nodeId, cfg);
    }

    private static int getArrayNextElement(int nodeId, MainLineEpisodeConfigObject cfg) {
        boolean flag = false;
        for (int i : cfg.getEpisondenodeid()) {
            if (flag) {
                return i;
            }
            if (i == nodeId) {
                flag = true;
            }

        }
        return -1;
    }

    public static MainLineEpisodeConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MainLineEpisodeConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setEpisondenodeid(MapHelper.getInts(e, "EpisondeNodeId"));


        _ix_id.put(config.getId(), config);


    }
}
