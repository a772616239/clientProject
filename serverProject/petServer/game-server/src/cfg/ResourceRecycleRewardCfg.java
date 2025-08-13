/*CREATED BY TOOL*/

package cfg;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import model.reward.RewardUtil;
import protocol.Common;

@annationInit(value = "ResourceRecycleRewardCfg", methodname = "initConfig")
public class ResourceRecycleRewardCfg extends baseConfig<ResourceRecycleRewardCfgObject> {


    private static ResourceRecycleRewardCfg instance = null;

    public static ResourceRecycleRewardCfg getInstance() {

        if (instance == null)
            instance = new ResourceRecycleRewardCfg();
        return instance;

    }


    public static Map<Integer, ResourceRecycleRewardCfgObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ResourceRecycleRewardCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ResourceRecycleRewardCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ResourceRecycleRewardCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, ResourceRecycleRewardCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setFunctionid(MapHelper.getInt(e, "functionId"));

        config.setPointid(MapHelper.getInt(e, "pointId"));

        config.setRewardid(MapHelper.getInt(e, "rewardId"));


        _ix_id.put(config.getId(), config);


    }

    public List<Common.Reward> getByFunctionAndPoint(int function, int point) {
        Optional<ResourceRecycleRewardCfgObject> any = _ix_id.values().stream().filter(e -> e.getFunctionid() == function && e.getPointid() == point).findAny();
        if (any.isPresent()){
            return RewardUtil.getRewardsByRewardId(any.get().getRewardid());
        }
        return Collections.emptyList();

    }
}
