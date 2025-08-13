/*CREATED BY TOOL*/

package cfg;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.Getter;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value = "CrossArenaRobotRule", methodname = "initConfig")
public class CrossArenaRobotRule extends baseConfig<CrossArenaRobotRuleObject> {


    private static CrossArenaRobotRule instance = null;

    public static CrossArenaRobotRule getInstance() {

        if (instance == null)
            instance = new CrossArenaRobotRule();
        return instance;

    }


    public static Map<Integer, CrossArenaRobotRuleObject> _ix_id = new HashMap<>();

    @Getter
    List<CrossArenaRobotRuleObject> winList = Collections.emptyList();
    @Getter
    List<CrossArenaRobotRuleObject> failList = Collections.emptyList();

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CrossArenaRobotRule) o;
        initConfig();
        winList = _ix_id.values().stream().filter(e -> e.getResult() == 1).sorted(Comparator.comparingInt(CrossArenaRobotRuleObject::getPlayerabilityh)).collect(Collectors.toList());
        failList = _ix_id.values().stream().filter(e -> e.getResult() == -1).sorted(Comparator.comparingInt(CrossArenaRobotRuleObject::getPlayerabilityh)).collect(Collectors.toList());

    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CrossArenaRobotRule");

        for (Map e : ret) {
            put(e);
        }

    }

    public static CrossArenaRobotRuleObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, CrossArenaRobotRuleObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setPlayerabilityl(MapHelper.getInt(e, "playerAbilityL"));

        config.setPlayerabilityh(MapHelper.getInt(e, "playerAbilityH"));

        config.setResult(MapHelper.getInt(e, "result"));

        config.setNum(MapHelper.getInt(e, "num"));

        config.setPro(MapHelper.getInt(e, "pro"));

        config.setAilevel(MapHelper.getInt(e, "aiLevel"));


        _ix_id.put(config.getId(), config);


    }
}
