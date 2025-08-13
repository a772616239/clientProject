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
import org.springframework.util.CollectionUtils;

@annationInit(value = "PopupMission", methodname = "initConfig")
public class PopupMission extends baseConfig<PopupMissionObject> {


    private static PopupMission instance = null;

    public static PopupMission getInstance() {

        if (instance == null)
            instance = new PopupMission();
        return instance;

    }


    public static Map<Integer, PopupMissionObject> _ix_id = new HashMap<>();

    //<missionType,List<MissionObj>>
    public static Map<Integer, List<PopupMissionObject>> typeTargetMap = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PopupMission) o;
        initConfig();
        groupConfig();
    }

    private void groupConfig() {
        for (PopupMissionObject cfg : _ix_id.values()) {
            if (cfg.getId() <= 0) {
                continue;
            }
            List<PopupMissionObject> targetList = typeTargetMap.computeIfAbsent(cfg.getMissiontype(), a -> new ArrayList<>());
            targetList.add(cfg);
        }
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PopupMission");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PopupMissionObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PopupMissionObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setMissiontype(MapHelper.getInt(e, "missionType"));

        config.setTarget(MapHelper.getInt(e, "target"));

        config.setAddition(MapHelper.getInt(e, "addition"));

        config.setViplv(MapHelper.getInt(e, "vipLv"));

        config.setProductid(MapHelper.getInt(e, "productId"));

        config.setReward(MapHelper.getIntArray(e, "reward"));

        config.setLimittime(MapHelper.getInt(e, "limitTime"));

        config.setLimitbuy(MapHelper.getInt(e, "limitBuy"));

        config.setDailytriggerlimit(MapHelper.getInt(e, "dailyTriggerLimit"));


        _ix_id.put(config.getId(), config);


    }

    public PopupMissionObject findOne(int type, int nowTarget, int param) {
        List<PopupMissionObject> targetList = typeTargetMap.get(type);
        if (CollectionUtils.isEmpty(targetList)) {
            return null;
        }
        return targetList.stream().filter(e -> e.getTarget() == nowTarget && e.getAddition() == param).findFirst().orElse(null);
    }

    public PopupMissionObject getMissionBuyProductId(int productId) {
        return _ix_id.values().stream().filter(e -> e.getProductid() == productId).findFirst().orElse(null);
    }
}
