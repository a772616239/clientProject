/*CREATED BY TOOL*/

package cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import model.reward.RewardUtil;
import protocol.Common;

@annationInit(value = "CpTeamRewardCfg", methodname = "initConfig")
public class CpTeamRewardCfg extends baseConfig<CpTeamRewardCfgObject> {


    private static CpTeamRewardCfg instance = null;

    public static CpTeamRewardCfg getInstance() {

        if (instance == null)
            instance = new CpTeamRewardCfg();
        return instance;

    }


    public static Map<Integer, CpTeamRewardCfgObject> _ix_id = new HashMap<>();


    public static List<Common.Reward> getRewardByScore(int starScore, int scenceId, List<Integer> alreadySettleId) {
        if (alreadySettleId == null) {
            alreadySettleId = Collections.emptyList();
        }

        List<Integer> finalAlreadySettleId = alreadySettleId;
        List<Common.Reward> rewards = new ArrayList<>();

        for (CpTeamRewardCfgObject cfg : _ix_id.values().stream().filter(e ->
                e.getNeedscore() <= starScore && !finalAlreadySettleId.contains(e.getId())
                        && scenceId == e.getScenceid()
        ).collect(Collectors.toList())) {
            rewards.addAll(RewardUtil.parseRewardIntArrayToRewardList(cfg.getRewards()));
        }

        return RewardUtil.mergeReward(rewards);

    }

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CpTeamRewardCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CpTeamRewardCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static CpTeamRewardCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, CpTeamRewardCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setNeedscore(MapHelper.getInt(e, "needScore"));

        config.setRewards(MapHelper.getIntArray(e, "rewards"));

        config.setScenceid(MapHelper.getInt(e, "scenceId"));


        _ix_id.put(config.getId(), config);


    }
}
