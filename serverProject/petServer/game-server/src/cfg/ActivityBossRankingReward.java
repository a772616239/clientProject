/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "ActivityBossRankingReward", methodname = "initConfig")
public class ActivityBossRankingReward extends baseConfig<ActivityBossRankingRewardObject> {


    private static ActivityBossRankingReward instance = null;

    public static ActivityBossRankingReward getInstance() {

        if (instance == null)
            instance = new ActivityBossRankingReward();
        return instance;

    }


    public static Map<Integer, ActivityBossRankingRewardObject> _ix_id = new HashMap<Integer, ActivityBossRankingRewardObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ActivityBossRankingReward) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ActivityBossRankingReward");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ActivityBossRankingRewardObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, ActivityBossRankingRewardObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setStartranking(MapHelper.getInt(e, "startRanking"));

        config.setEndranking(MapHelper.getInt(e, "endRanking"));

        config.setDailyrewards(MapHelper.getInt(e, "dailyRewards"));


        _ix_id.put(config.getId(), config);


    }
}
