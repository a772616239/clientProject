/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import timetool.TimeHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "MistSeasonConfig", methodname = "initConfig")
public class MistSeasonConfig extends baseConfig<MistSeasonConfigObject> {


    private static MistSeasonConfig instance = null;

    public static MistSeasonConfig getInstance() {

        if (instance == null)
            instance = new MistSeasonConfig();
        return instance;

    }


    public static Map<Integer, MistSeasonConfigObject> _ix_id = new HashMap<Integer, MistSeasonConfigObject>();

    public static MistSeasonConfigObject getCurSeasonConfig(long curTime) {
        MistSeasonConfigObject curSeason = null;
        MistSeasonConfigObject seasonCfg;
        for (Map.Entry<Integer,MistSeasonConfigObject> entry : _ix_id.entrySet()) {
            seasonCfg = entry.getValue();
            if (seasonCfg.getEndtime() <= curTime) {
                continue;
            }
            if (curSeason != null && seasonCfg.getStarttime() >= curSeason.getStarttime()) {
                continue;
            }
            curSeason = seasonCfg;
        }
        return curSeason;
    }

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistSeasonConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistSeasonConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistSeasonConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MistSeasonConfigObject config) {


        config.setId(MapHelper.getInt(e, "ID"));
        long startTime = TimeHelper.parseDate(MapHelper.getStr(e, "StartTime"), "yyyy-MM-dd HH:mm:ss").getTime();
        long endTime = TimeHelper.parseDate(MapHelper.getStr(e, "EndTime"), "yyyy-MM-dd HH:mm:ss").getTime();
        if (endTime <= startTime) {
            throw new RuntimeException("season time error id=" + config.getId());
        }
        config.setStarttime(startTime);

        config.setEndtime(endTime);
        _ix_id.put(config.getId(), config);
    }
}
