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
import model.reward.RewardUtil;
import protocol.Common.Reward;

@annationInit(value = "ScratchLotteryReward", methodname = "initConfig")
public class ScratchLotteryReward extends baseConfig<ScratchLotteryRewardObject> {


    private static ScratchLotteryReward instance = null;

    public static ScratchLotteryReward getInstance() {

        if (instance == null)
            instance = new ScratchLotteryReward();
        return instance;

    }


    public static Map<Integer, ScratchLotteryRewardObject> _ix_id = new HashMap<Integer, ScratchLotteryRewardObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ScratchLotteryReward) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ScratchLotteryReward");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ScratchLotteryRewardObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, ScratchLotteryRewardObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setPetavatar(MapHelper.getInt(e, "petAvatar"));

        config.setLinkcount(MapHelper.getInt(e, "linkCount"));

        config.setOdds(MapHelper.getInt(e, "odds"));

        config.setRewardsid(MapHelper.getInt(e, "rewardsId"));


        _ix_id.put(config.getId(), config);
    }

    public static List<Reward> getRewardsByPetIdAndCount(int petId, int linkCount) {
        for (ScratchLotteryRewardObject value : _ix_id.values()) {
            if (value.getOdds() <= 0) {
                continue;
            }
            if (value.getPetavatar() == petId && value.getLinkcount() == linkCount) {
                return RewardUtil.getRewardsByRewardId(value.getRewardsid());
            }

        }
        return null;
    }
}
