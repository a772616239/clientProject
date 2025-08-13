/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import datatool.MapHelper;
import model.base.baseConfig;
import util.LogUtil;
import util.TimeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "MatchArenaSeasonConfig", methodname = "initConfig")
public class MatchArenaSeasonConfig extends baseConfig<MatchArenaSeasonConfigObject> {


    private static MatchArenaSeasonConfig instance = null;

    public static MatchArenaSeasonConfig getInstance() {

        if (instance == null)
            instance = new MatchArenaSeasonConfig();
        return instance;

    }


    public static Map<Integer, MatchArenaSeasonConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MatchArenaSeasonConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MatchArenaSeasonConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MatchArenaSeasonConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MatchArenaSeasonConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setStarttime(TimeUtil.parseTimeByDefaultTimeZone(MapHelper.getStr(e, "startTime"), TimeUtil.DEFAULT_TIME_FORMAT));

        config.setEndtime(TimeUtil.parseTimeByDefaultTimeZone(MapHelper.getStr(e, "endTime"), TimeUtil.DEFAULT_TIME_FORMAT));

        config.setTimescope(MapHelper.getIntArray(e, "timeScope"));

        config.setCrossrankingrewards(MapHelper.getInts(e, "crossRankingRewards"));


        _ix_id.put(config.getId(), config);

        if (config.getStarttime() >= config.getEndtime()) {
            LogUtil.error("cfg.MatchArenaSeasonConfig.putToMem, config id:" + config.getId() + ", startTime is less than end time");
            throw new RuntimeException("MatchArenaSeasonConfig 配置出错");
        }
    }

    public static MatchArenaSeasonConfigObject getCurOpenSeason() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        MatchArenaSeasonConfigObject result = null;
        for (MatchArenaSeasonConfigObject seasonConfig : _ix_id.values()) {
            if (currentTime > seasonConfig.getEndtime()) {
                continue;
            }
            if (result == null || seasonConfig.getStarttime() < result.getStarttime()) {
                result = seasonConfig;
            }
        }
        return result;
    }
}
