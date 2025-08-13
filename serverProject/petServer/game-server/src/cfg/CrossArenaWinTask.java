/*CREATED BY TOOL*/

package cfg;

import java.util.*;
import java.util.stream.Collectors;

import lombok.Getter;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

import static common.GameConst.LtDailyWinTask;
import static common.GameConst.LtWeeklyWinTask;

@annationInit(value = "CrossArenaWinTask", methodname = "initConfig")
public class CrossArenaWinTask extends baseConfig<CrossArenaWinTaskObject> {


    private static CrossArenaWinTask instance = null;

    public static CrossArenaWinTask getInstance() {

        if (instance == null)
            instance = new CrossArenaWinTask();
        return instance;

    }


    public static Map<Integer, CrossArenaWinTaskObject> _ix_id = new HashMap<>();

    @Getter
    private List<Integer> dailyTaskIds = Collections.emptyList();
    @Getter
    private List<Integer> weekTaskIds = Collections.emptyList();

    private  Map<Integer, List<CrossArenaWinTaskObject>> dailyScienceIdTaskMap = Collections.EMPTY_MAP;

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CrossArenaWinTask) o;
        initConfig();
        dailyTaskIds = groupIdByType(LtDailyWinTask);
        weekTaskIds = groupIdByType(LtWeeklyWinTask);
        dailyScienceIdTaskMap = _ix_id.values().stream().filter(e->e.getType()==1).collect(Collectors.groupingBy(CrossArenaWinTaskObject::getSceneid));
    }

    public List<CrossArenaWinTaskObject> getDailyTaskByScienceId(int scienceId) {
        return dailyScienceIdTaskMap.getOrDefault(scienceId, Collections.emptyList());
    }

    private List<Integer> groupIdByType(int type) {
        return _ix_id.values().stream().filter(e -> e.getType() == type).map(CrossArenaWinTaskObject::getId).collect(Collectors.toList());
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CrossArenaWinTask");

        for (Map e : ret) {
            put(e);
        }

    }

    public static CrossArenaWinTaskObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, CrossArenaWinTaskObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setWinning(MapHelper.getInt(e, "winning"));

        config.setSceneid(MapHelper.getInt(e, "sceneId"));

        config.setType(MapHelper.getInt(e, "type"));

        config.setReward(MapHelper.getInt(e, "reward"));

        config.setRandom_reward(MapHelper.getInt(e, "random_reward"));

        config.setRandom(MapHelper.getInt(e, "random"));


        _ix_id.put(config.getId(), config);


    }
}
