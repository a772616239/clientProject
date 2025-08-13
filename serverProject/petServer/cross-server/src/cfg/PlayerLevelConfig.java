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

        config.setRedbagrandomrewards(MapHelper.getIntArray(e, "redBagRandomRewards"));


        _ix_level.put(config.getLevel(), config);


    }
}
