/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import model.player.util.PlayerUtil;
import model.reward.RewardUtil;
import protocol.Activity.ClientSubMission;
import protocol.Common.LanguageEnum;
import protocol.Common.Reward;
import protocol.TargetSystem.TargetTypeEnum;
import util.TimeUtil;

@annationInit(value = "TimeLimitActivityTask", methodname = "initConfig")
public class TimeLimitActivityTask extends baseConfig<TimeLimitActivityTaskObject> {


    private static TimeLimitActivityTask instance = null;

    public static TimeLimitActivityTask getInstance() {

        if (instance == null)
            instance = new TimeLimitActivityTask();
        return instance;

    }


    public static Map<Integer, TimeLimitActivityTaskObject> _ix_id = new HashMap<Integer, TimeLimitActivityTaskObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (TimeLimitActivityTask) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "TimeLimitActivityTask");

        for (Map e : ret) {
            put(e);
        }

    }

    public static TimeLimitActivityTaskObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, TimeLimitActivityTaskObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setMissiontype(MapHelper.getInt(e, "missionType"));

        config.setTargetcount(MapHelper.getInt(e, "targetCount"));

        config.setAddtion(MapHelper.getInt(e, "addtion"));

        config.setMissionname(MapHelper.getInt(e, "missionName"));

        config.setMissiondesc(MapHelper.getInt(e, "missionDesc"));

        config.setReward(MapHelper.getIntArray(e, "reward"));

        config.setEndtime(MapHelper.getInt(e, "endTime"));


        _ix_id.put(config.getId(), config);

        //分类
        CLASSIFY_BY_TYPE.computeIfAbsent(config.getMissiontype(), x -> new ArrayList<>()).add(config);
    }

    /**
     * 将限时活动的
     */
    private static final Map<Integer, List<TimeLimitActivityTaskObject>> CLASSIFY_BY_TYPE = new HashMap<>();

    public static List<TimeLimitActivityTaskObject> getByType(TargetTypeEnum typeEnum) {
        if (typeEnum == null) {
            return null;
        }
        return CLASSIFY_BY_TYPE.get(typeEnum.getNumber());
    }

    public static ClientSubMission.Builder buildClientMission(String playerIdx, int id) {
        return buildClientMission(PlayerUtil.queryPlayerLanguage(playerIdx), id);
    }

    public static ClientSubMission.Builder buildClientMission(LanguageEnum language, int id) {
        return buildClientMission(language, id, 0);
    }

    public static ClientSubMission.Builder buildClientMission(LanguageEnum language, int id, long startTime) {
        TimeLimitActivityTaskObject taskCfg = getById(id);
        if (taskCfg == null) {
            return null;
        }
        ClientSubMission.Builder result = ClientSubMission.newBuilder();
        result.setIndex(taskCfg.getId());
        result.setTarget(taskCfg.getTargetcount());
        result.setDesc(ServerStringRes.getContentByLanguage(taskCfg.getMissiondesc(), language));
        List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(taskCfg.getReward());
        if (rewards != null) {
            result.addAllReward(rewards);
        }
        int endTime = taskCfg.getEndtime();
        result.setEndTimestamp(endTime <= 0 ? -1 : startTime + TimeUtil.MS_IN_A_DAY * endTime);
        return result;
    }
}
