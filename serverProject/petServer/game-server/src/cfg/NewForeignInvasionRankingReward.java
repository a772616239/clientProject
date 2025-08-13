/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.base.baseConfig;
import model.ranking.settle.RankingRewards;
import model.ranking.settle.RankingRewardsImpl;
import model.reward.RewardUtil;

@annationInit(value = "NewForeignInvasionRankingReward", methodname = "initConfig")
public class NewForeignInvasionRankingReward extends baseConfig<NewForeignInvasionRankingRewardObject> {


    private static NewForeignInvasionRankingReward instance = null;

    public static NewForeignInvasionRankingReward getInstance() {

        if (instance == null)
            instance = new NewForeignInvasionRankingReward();
        return instance;

    }


    public static Map<Integer, NewForeignInvasionRankingRewardObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (NewForeignInvasionRankingReward) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "NewForeignInvasionRankingReward");

        for (Map e : ret) {
            put(e);
        }

    }

    public static NewForeignInvasionRankingRewardObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, NewForeignInvasionRankingRewardObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setStartranking(MapHelper.getInt(e, "startRanking"));

        config.setEndranking(MapHelper.getInt(e, "endRanking"));

        config.setRewards(MapHelper.getIntArray(e, "rewards"));


        _ix_id.put(config.getId(), config);
    }


    public List<RankingRewards> getRankingRewardsList() {
        return _ix_id.values().stream()
                .filter(e -> e.getId() > 0)
                .map(e -> new RankingRewardsImpl(e.getStartranking(), e.getEndranking(), RewardUtil.parseRewardIntArrayToRewardList(e.getRewards())))
                .collect(Collectors.toList());
    }
}
