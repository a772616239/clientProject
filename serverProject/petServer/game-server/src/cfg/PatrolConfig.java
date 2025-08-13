/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.GameConst;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import model.reward.RewardUtil;
import protocol.Common;
import util.RandomUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "PatrolConfig", methodname = "initConfig")
public class PatrolConfig extends baseConfig<PatrolConfigObject> {


    private static PatrolConfig instance = null;

    public static PatrolConfig getInstance() {

        if (instance == null)
            instance = new PatrolConfig();
        return instance;

    }


    public static Map<Integer, PatrolConfigObject> _ix_id = new HashMap<>();

    private static int treasureExRewardProbability = 0;

    private static int treasureExRewardId = 0;

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PatrolConfig) o;
        initConfig();
        initField();
    }

    private void initField() {
        PatrolConfigObject patrolConfig = PatrolConfig.getById(GameConst.CONFIG_ID);
        treasureExRewardProbability = patrolConfig.getTreasurexrewardprobability();
        treasureExRewardId = patrolConfig.getTreasurexrewardid();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PatrolConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PatrolConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PatrolConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setWeight(MapHelper.getInt(e, "weight"));

        config.setTreasurexrewardprobability(MapHelper.getInt(e, "treasurExRewardProbability "));

        config.setTreasurexrewardid(MapHelper.getInt(e, "treasurExRewardId"));

        config.setPatrolconsume(MapHelper.getInts(e, "patrolConsume"));

        config.setIrritategreed(MapHelper.getInt(e, "irritateGreed"));

        config.setExplore1greed(MapHelper.getInt(e, "explore1Greed"));

        config.setExplore2greed(MapHelper.getInt(e, "explore2Greed"));

        config.setExplore3greed(MapHelper.getInt(e, "explore3Greed"));

        config.setChallengeagain(MapHelper.getInts(e, "challengeAgain"));

        config.setTaskprobability(MapHelper.getInt(e, "taskProbability"));

        config.setTimeroll(MapHelper.getInts(e, "timeRoll"));

        config.setBranchtreasurnum(MapHelper.getInt(e, "branchTreasurNum"));

        config.setTreasureprob(MapHelper.getInt(e, "treasureProb"));

        config.setExploreprob(MapHelper.getInt(e, "exploreProb"));

        config.setChamberprob(MapHelper.getInt(e, "chamberProb"));

        config.setSpaceprob(MapHelper.getInt(e, "spaceProb"));

        config.setBattlepoint(MapHelper.getInts(e, "battlePoint"));

        config.setExpolre1(MapHelper.getIntArray(e, "expolre1"));

        config.setExpolre2(MapHelper.getIntArray(e, "expolre2"));

        config.setExpolre3(MapHelper.getIntArray(e, "expolre3"));

        config.setReborntime(MapHelper.getInt(e, "rebornTime"));

        config.setMingreed(MapHelper.getInt(e, "minGreed"));

        config.setMaxgreed(MapHelper.getInt(e, "maxGreed"));

        config.setRandombastardreward(MapHelper.getIntArray(e, "randomBastardReward"));

        config.setRandombossreward(MapHelper.getIntArray(e, "randomBossReward"));

        config.setRandomtreasurereward(MapHelper.getIntArray(e, "randomTreasureReward"));

        config.setFixedtreasurereward(MapHelper.getIntArray(e, "fixedTreasureReward"));

        config.setRandomdroprate(MapHelper.getInt(e, "randomDropRate"));

        config.setRandomtimes(MapHelper.getInt(e, "randomTimes"));

        config.setChamberrandompets(MapHelper.getInts(e, "chamberRandomPets"));


        if (config.getId() <= 0) {
            return;
        }
        _ix_id.put(config.getId(), config);


    }

    public List<Common.Reward> randomExTreasureReward() {
        if (RandomUtil.getRandom1000() >= treasureExRewardProbability) {
            return Collections.emptyList();
        }
        return RewardUtil.getRewardsByRewardId(treasureExRewardId);

    }
}
