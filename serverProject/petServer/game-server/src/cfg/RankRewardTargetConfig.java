/*CREATED BY TOOL*/

package cfg;

import java.util.*;
import java.util.stream.Collectors;

import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import model.ranking.settle.RankingRewardsImpl;
import model.reward.RewardUtil;
import org.apache.commons.lang.ArrayUtils;
import protocol.Activity;
import util.LogUtil;

@annationInit(value = "RankRewardTargetConfig", methodname = "initConfig")
public class RankRewardTargetConfig extends baseConfig<RankRewardTargetConfigObject> {


    private static RankRewardTargetConfig instance = null;

    public static RankRewardTargetConfig getInstance() {

        if (instance == null)
            instance = new RankRewardTargetConfig();
        return instance;

    }


    public static Map<Integer, RankRewardTargetConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (RankRewardTargetConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "RankRewardTargetConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static RankRewardTargetConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, RankRewardTargetConfigObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setReward(MapHelper.getIntArray(e, "Reward"));

        config.setTargetvalue(MapHelper.getInt(e, "TargetValue"));


        _ix_id.put(config.getId(), config);


    }

    private final Map<Activity.EnumRankingType, List<RankingRewardsImpl>> rangeRankRewardMap = new HashMap<>();

    public List<RankingRewardsImpl> getRangeRewardsByRankType(Activity.EnumRankingType rankingType) {
        return rangeRankRewardMap.get(rankingType);
    }

    public boolean afterAllCfgInit() {
        for (RankConfigObject rankCfg : RankConfig._ix_rankid.values()) {
            if (ArrayUtils.isEmpty(rankCfg.getRankreward_range())|| Arrays.stream(rankCfg.getRankreward_range()).allMatch(e->e==0)) {
                continue;
            }
            if (!initRankingRangRewards(rankCfg)) {
                return false;
            }
        }
        return true;
    }

    private boolean initRankingRangRewards(RankConfigObject rankCfg) {
        Activity.EnumRankingType rankType = Activity.EnumRankingType.forNumber(rankCfg.getRankid());
        if (rankType == null) {
            LogUtil.error("RankRewardTargetConfig initRankingRangRewards error case by rankType is null,rankConfigId:{}", rankCfg.getRankid());
            return false;
        }

        RankConfigObject rankingConfig = RankConfig.getByRankid(rankType.getNumber());
        if (rankingConfig == null) {
            LogUtil.error("RankRewardTargetConfig initRankingRangRewards rank config is not exist,rankConfigId:{}", rankCfg.getRankid());
            return false;
        }
        List<RankingRewardsImpl> rewards = Arrays.stream(rankingConfig.getRankreward_range())
                .mapToObj(e -> {
                            RankRewardRangeConfigObject rankRewardsConfig = RankRewardRangeConfig.getById(e);
                            if (rankRewardsConfig == null) {
                                return null;
                            }
                            return new RankingRewardsImpl(rankRewardsConfig.getRangemin(), rankRewardsConfig.getRangemax(), RewardUtil.parseRewardIntArrayToRewardList(rankRewardsConfig.getReward()));
                        }
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        rangeRankRewardMap.put(rankType, rewards);
        return true;
    }
}
