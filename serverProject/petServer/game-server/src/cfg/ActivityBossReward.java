/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import model.reward.RewardUtil;
import protocol.Common.Reward;
import util.LogUtil;
import util.RandomUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static common.GameConst.ActivityBossMaxRandomRewardTime;

@annationInit(value = "ActivityBossReward", methodname = "initConfig")
public class ActivityBossReward extends baseConfig<ActivityBossRewardObject> {


    private static ActivityBossReward instance = null;

    public static ActivityBossReward getInstance() {

        if (instance == null)
            instance = new ActivityBossReward();
        return instance;

    }


    public static Map<Integer, ActivityBossRewardObject> _ix_times = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ActivityBossReward) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ActivityBossReward");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ActivityBossRewardObject getByTimes(int times) {

        return _ix_times.get(times);

    }


    public void putToMem(Map e, ActivityBossRewardObject config) {

        config.setTimes(MapHelper.getInt(e, "times"));

        config.setPlayerlv(MapHelper.getInt(e, "playerLv"));

        config.setMustreward(MapHelper.getIntArray(e, "mustReward"));

        config.setDamagebase(MapHelper.getLong(e, "damageBase"));

        config.setRandomreward(MapHelper.getIntArray(e, "randomReward"));


        _ix_times.put(config.getTimes(), config);
    }


    private int getRewardTimesByDamage(ActivityBossRewardObject cfg, long damage) {
        if (cfg.getDamagebase() == 0) {
            LogUtil.error("getRewardTimesByDamage cfg damageBase is zero by cfgId:{}", cfg.getTimes());
            return 1;
        }
        return (int) Math.ceil(damage * 2.0 / cfg.getDamagebase());
    }

    public List<Reward> getRewardListByDamage(int playerLv, long damage) {

        ActivityBossRewardObject cfg = findConfigByPlayerLv(playerLv);
        if (cfg == null) {
            LogUtil.error("getRewardListByDamage error ,find ActivityBossRewardObject by playerLv:{} is null", playerLv);
            return Collections.emptyList();
        }

        //必得奖励
        List<Reward> result = RewardUtil.parseRewardIntArrayToRewardList(cfg.getMustreward());
        if (result == null) {
            LogUtil.error("ActivityBossRewardObject must reward cfg error by cfgTimes:{}", cfg.getTimes());
            return Collections.emptyList();
        }

        int rewardTimes = Math.min(ActivityBossMaxRandomRewardTime, getRewardTimesByDamage(cfg, damage));
        if (rewardTimes <= 0) {
            return result;
        }

        //随机奖励
        int[][] randomReward = cfg.getRandomreward();
        for (int i = 0; i < rewardTimes; i++) {
            result.add(RewardUtil.parseReward(RandomUtil.getRandomCfgBy4(randomReward)));

        }
        return result;
    }

    private ActivityBossRewardObject findConfigByPlayerLv(int playerLv) {
        ActivityBossRewardObject lastCfg = null;
        for (ActivityBossRewardObject cfg : _ix_times.values()) {
            if (cfg.getTimes() <= 0) {
                continue;
            }
            if (cfg.getPlayerlv() > playerLv) {
                return lastCfg == null ? cfg : lastCfg;
            }
            lastCfg = cfg;
        }
        return lastCfg;
    }
}
