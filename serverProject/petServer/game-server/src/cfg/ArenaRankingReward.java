/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import util.GameUtil;
import util.LogUtil;

@annationInit(value = "ArenaRankingReward", methodname = "initConfig")
public class ArenaRankingReward extends baseConfig<ArenaRankingRewardObject> {


    private static ArenaRankingReward instance = null;

    public static ArenaRankingReward getInstance() {

        if (instance == null)
            instance = new ArenaRankingReward();
        return instance;

    }


    public static Map<Integer, ArenaRankingRewardObject> _ix_id = new HashMap<Integer, ArenaRankingRewardObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ArenaRankingReward) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ArenaRankingReward");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ArenaRankingRewardObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, ArenaRankingRewardObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setDanid(MapHelper.getInt(e, "danId"));

        config.setStartranking(MapHelper.getInt(e, "startRanking"));

        config.setEndranking(MapHelper.getInt(e, "endRanking"));

        config.setDailyrewards(MapHelper.getInt(e, "dailyRewards"));

        config.setWeeklyrewards(MapHelper.getInt(e, "weeklyRewards"));


        _ix_id.put(config.getId(), config);


    }

    /**
     * 根据玩家段位和排名拿到奖励ID
     * @param dan
     * @param ranking
     * @param type 1:日奖励， 2：周奖励
     * @return -1未找到
     */
    public static int getRewardByDanIdAndRanking(int dan, int ranking, int type) {
        if (type != 1 && type != 2) {
            LogUtil.error("ArenaRankingReward.getRewardByPartitionIdAndRanking, error type:" + type);
            return -1;
        }

        ArenaRankingRewardObject findObj = null;
        for (ArenaRankingRewardObject rewardObject : _ix_id.values()) {
            if (dan == rewardObject.getDanid()
                    && GameUtil.inScope(rewardObject.getStartranking(), rewardObject.getEndranking(),ranking)) {
                findObj = rewardObject;
                break;
            }
        }

        if (findObj == null) {
            return -1;
        }

        return type == 1 ? findObj.getDailyrewards() : findObj.getWeeklyrewards();
    }
}
