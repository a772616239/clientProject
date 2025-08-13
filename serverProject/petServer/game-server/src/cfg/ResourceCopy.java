/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.*;
import model.base.baseConfig;
import util.LogUtil;

@annationInit(value = "ResourceCopy", methodname = "initConfig")
public class ResourceCopy extends baseConfig<ResourceCopyObject> {


    private static ResourceCopy instance = null;

    public static ResourceCopy getInstance() {

        if (instance == null)
            instance = new ResourceCopy();
        return instance;

    }


    public static Map<Integer, ResourceCopyObject> _ix_id = new HashMap<Integer, ResourceCopyObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ResourceCopy) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ResourceCopy");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ResourceCopyObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, ResourceCopyObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setType(MapHelper.getInt(e, "type"));

        config.setTypepassindex(MapHelper.getInt(e, "typePassIndex"));

        config.setUnlocklv(MapHelper.getInt(e, "unlockLv"));

        config.setFightmakeid(MapHelper.getInt(e, "fightMakeId"));

        config.setAfterid(MapHelper.getInt(e, "afterId"));


        _ix_id.put(config.getId(), config);


        init(config);
    }

    /**
     * ====================================================================
     */

    Map<Integer, Map<Integer, ResourceCopyObject>> resCopyCfg = new HashMap<>();

    public void init(ResourceCopyObject cfgObj) {
        if (cfgObj == null) {
            return;
        }

        Map<Integer, ResourceCopyObject> copyObjectMap = resCopyCfg.get(cfgObj.getType());
        if (copyObjectMap == null) {
            copyObjectMap = new HashMap<>();
            resCopyCfg.put(cfgObj.getType(), copyObjectMap);
        }
        copyObjectMap.put(cfgObj.getTypepassindex(), cfgObj);
    }


    public ResourceCopyObject getCopyCfgByTypeAndIndex(int type, int index) {
        Map<Integer, ResourceCopyObject> resCopyCfgMap = resCopyCfg.get(type);
        if (resCopyCfgMap == null) {
            LogUtil.error("ResourceCopy, type = " + type + ", is null");
            return null;
        }
        return resCopyCfgMap.get(index);
    }

    public Set<Integer> getAllResCopyType() {
        return resCopyCfg.keySet();
    }


    public Collection<ResourceCopyObject> getAllByType(int type) {
        Map<Integer, ResourceCopyObject> cfg = resCopyCfg.get(type);
        if (cfg == null) {
            LogUtil.error("ResourceCopy, type = " + type + ", is null");
            return null;
        }
        return cfg.values();
    }
}
