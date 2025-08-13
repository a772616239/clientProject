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

@annationInit(value = "OfferRewardBoss", methodname = "initConfig")
public class OfferRewardBoss extends baseConfig<OfferRewardBossObject> {


    private static OfferRewardBoss instance = null;

    public static OfferRewardBoss getInstance() {

        if (instance == null)
            instance = new OfferRewardBoss();
        return instance;

    }


    public static Map<Integer, OfferRewardBossObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (OfferRewardBoss) o;
        initConfig();
        checkConfig();
    }

    private void checkConfig() {
        for (OfferRewardBossObject cfg : _ix_id.values()) {
            for (int[] ints : cfg.getReward()) {
                if (ints.length < 5) {
                    throw new RuntimeException("OfferRewardBoss config error,length not enough");
                }
                if (ints[2] <= 0) {
                    throw new RuntimeException("OfferRewardBoss config error,reward pro less than zero");
                }
            }
        }

    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "OfferRewardBoss");

        for (Map e : ret) {
            put(e);
        }

    }

    public static OfferRewardBossObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, OfferRewardBossObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setBoss(MapHelper.getInt(e, "boss"));

        config.setStar(MapHelper.getInt(e, "star"));

        config.setBuff(MapHelper.getInts(e, "buff"));

        config.setReward(MapHelper.getIntArray(e, "reward"));

        config.setFight_reward1(MapHelper.getIntArray(e, "fight_reward1"));

        config.setFight_reward2(MapHelper.getIntArray(e, "fight_reward2"));

        config.setFight_reward3(MapHelper.getIntArray(e, "fight_reward3"));

        config.setMaxchooserewardcount(MapHelper.getInt(e, "maxChooseRewardCount"));

        config.setGeneraterewardcount(MapHelper.getInt(e, "generateRewardCount"));


        _ix_id.put(config.getId(), config);


    }
}
