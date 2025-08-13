/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.GameConst;
import common.load.ServerConfig;
import datatool.MapHelper;
import lombok.Getter;
import model.base.baseConfig;
import model.consume.ConsumeUtil;
import model.reward.RewardUtil;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Common;
import util.LogUtil;
import util.RandomUtil;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@annationInit(value = "CpTeamCfg", methodname = "initConfig")
public class CpTeamCfg extends baseConfig<CpTeamCfgObject> {


    private static CpTeamCfg instance = null;

    public static CpTeamCfg getInstance() {

        if (instance == null)
            instance = new CpTeamCfg();
        return instance;

    }

    @Getter
    private static Common.Consume buyReviveConsume;

    /**
     * 每周免费次数
     */
    @Getter
    private static int weeklyFreePlayTimes;

    /**
     * 副本有效时长
     */
    @Getter
    private static long copyEffectiveTime;

    @Getter
    private static int teamPlayerNum;

    private static Common.Reward luckStarReward;

    private static final Map<Integer, List<int[]>> eventCfg = new ConcurrentHashMap<>();

    public static Map<Integer, CpTeamCfgObject> _ix_id = new HashMap<>();

    private static final Map<Integer, Integer> diffScore = new HashMap<>();

    public static int randomBuff() {
        int[] randomCfgBy2 = RandomUtil.getRandomCfgBy2(getById(GameConst.CONFIG_ID).getBuffpool());
        return randomCfgBy2[0];
    }

    public static Common.Reward randomTresure() {
        return RewardUtil.parseReward(RandomUtil.getRandomCfgBy4(getById(GameConst.CONFIG_ID).getTreasurerewardpool()));

    }

    public static Common.Reward getLuckyStarReward() {
        return luckStarReward;
    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CpTeamCfg) o;
        initConfig();
        initDiffScore();
        initField();
        luckStarReward = RewardUtil.parseReward(getById(GameConst.CONFIG_ID).getStareventreward());
    }

    private void initField() {
        CpTeamCfgObject cfg = getById(GameConst.CONFIG_ID);
        weeklyFreePlayTimes = cfg.getFreetimes();
        teamPlayerNum = cfg.getTeamplayernum();
        copyEffectiveTime = cfg.getCopytime() * TimeUtil.MS_IN_A_HOUR;
        buyReviveConsume = ConsumeUtil.parseConsume(cfg.getBuyreviceconsume());
    }

    private void initDiffScore() {
        int[][] diffcultScore = getById(GameConst.CONFIG_ID).getDiffcultscore();
        for (int[] ints : diffcultScore) {
            diffScore.put(ints[0], ints[1]);
        }
    }

    public static int queryDiffScore(int difficult) {
        Integer score = diffScore.get(difficult);
        return score == null ? 0 : score;
    }

    public static int randomEvent(int difficult, int pointFloor) {
        CpTeamFloorCfgObject cfg = CpTeamFloorCfg.getById(pointFloor);
        if (cfg == null) {
            LogUtil.error("cpTeamCfg randomEvent error,CpTeamFloorCfg is null by pointFloor:{}", pointFloor);
            return 0;
        }
        List<int[]> configs = getEventCfgByDifficult(difficult, cfg);
        if (configs.size() <= 0) {
            LogUtil.error("cpTeamCfg randomEvent error,CpTeamFloorCfg event difficult is error by floor:{}, difficult:{}", pointFloor, difficult);
            return 0;
        }

        int range = 0;
        for (int[] i : configs) {
            range += i[2];
        }
        int randomRange = RandomUtils.nextInt(range);
        for (int[] i : configs) {
            if (randomRange < i[2]) {
                return i[1];
            } else {
                randomRange -= i[2];
            }
        }

        return -1;
    }

    private static List<int[]> getEventCfgByDifficult(int difficult, CpTeamFloorCfgObject cfg) {
        List<int[]> configs = new ArrayList<>();
        for (int[] ints : cfg.getEventhappen()) {
            if (ints[0] == difficult && ints.length == 3) {
                configs.add(ints);
            }
        }
        return configs;
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CpTeamCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static CpTeamCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, CpTeamCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setEventhappen(MapHelper.getIntArray(e, "eventHappen"));

        config.setStareventreward(MapHelper.getInts(e, "starEventReward"));

        config.setTreasurerewardpool(MapHelper.getIntArray(e, "treasureRewardPool"));

        config.setBuffpool(MapHelper.getIntArray(e, "buffPool"));

        config.setDiffcultscore(MapHelper.getIntArray(e, "diffcultScore"));

        config.setFreetimes(MapHelper.getInt(e, "freeTimes"));

        config.setTeamplayernum(MapHelper.getInt(e, "teamPlayerNum"));

        config.setCopytime(MapHelper.getInt(e, "copyTime"));

        config.setBuyreviceconsume(MapHelper.getInts(e, "buyReviceConsume"));

        config.setBuygameplayconsume(MapHelper.getInts(e, "buyGamePlayConsume"));


        _ix_id.put(config.getId(), config);


    }
}
