/*CREATED BY TOOL*/

package cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value = "PlayerLevelConfig", methodname = "initConfig")
public class PlayerLevelConfig extends baseConfig<PlayerLevelConfigObject> {


    private static PlayerLevelConfig instance = null;

    public static PlayerLevelConfig getInstance() {

        if (instance == null)
            instance = new PlayerLevelConfig();
        return instance;

    }


    public static Map<Integer, PlayerLevelConfigObject> _ix_level = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PlayerLevelConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PlayerLevelConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PlayerLevelConfigObject getByLevel(int level) {

        return _ix_level.get(level);

    }


    public void putToMem(Map e, PlayerLevelConfigObject config) {

        config.setLevel(MapHelper.getInt(e, "Level"));

        config.setExperience(MapHelper.getInt(e, "Experience"));

        config.setPetmissioncfg(MapHelper.getIntArray(e, "petMissionCfg"));

        config.setArenafightmake(MapHelper.getInt(e, "arenaFightMake"));

        config.setArenabattlerewardfactor(MapHelper.getInt(e, "arenaBattleRewardFactor"));

        config.setMistdailyrewardlimit(MapHelper.getIntArray(e, "mistDailyRewardLimit"));

        config.setMistbattlerewarditemid(MapHelper.getInt(e, "mistBattleRewardItemId"));

        config.setMistbattlerewarcount(MapHelper.getInt(e, "mistBattleRewarCount"));

        config.setMistrbagrewarcount(MapHelper.getInt(e, "mistrBagRewarCount"));

        config.setBravefightreward(MapHelper.getIntArray(e, "braveFightReward"));

        config.setGloryroadquizconsume(MapHelper.getInts(e, "gloryRoadQuizConsume"));

        config.setMatcharenagift(MapHelper.getIntArray(e, "matchArenaGift"));

        config.setMatcharenafullgift(MapHelper.getIntArray(e, "matchArenaFullGift"));

        config.setMatcharenamatchwinreward(MapHelper.getIntArray(e, "matcharenamatchwinreward"));

        config.setMatcharenamatchfailreward(MapHelper.getIntArray(e, "matcharenamatchfailreward"));

        config.setMatcharenamatchfullreward(MapHelper.getIntArray(e, "matcharenamatchfullreward"));

        config.setRedbagrandomrewards(MapHelper.getIntArray(e, "redBagRandomRewards"));


        _ix_level.put(config.getLevel(), config);

        if (config.getLevel() > maxLevel) {


            maxLevel = config.getLevel();


        }
    }

    /**
     * ================================================
     */

    public static int maxLevel = 1;

    public int getLvUpExp(int level) {
        PlayerLevelConfigObject config = getByLevel(level);
        if (config == null || level == maxLevel) {
            return Integer.MAX_VALUE;
        }
        return config.getExperience();
    }
}
