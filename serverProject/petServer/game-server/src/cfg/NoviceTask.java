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

@annationInit(value = "NoviceTask", methodname = "initConfig")
public class NoviceTask extends baseConfig<NoviceTaskObject> {


    private static NoviceTask instance = null;

    public static NoviceTask getInstance() {

        if (instance == null)
            instance = new NoviceTask();
        return instance;

    }


    public static Map<Integer, NoviceTaskObject> _ix_id = new HashMap<Integer, NoviceTaskObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (NoviceTask) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "NoviceTask");

        for (Map e : ret) {
            put(e);
        }

    }

    public static NoviceTaskObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, NoviceTaskObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setOpenday(MapHelper.getInt(e, "openDay"));

        config.setCloseday(MapHelper.getInt(e, "closeDay"));

        config.setEnddisplay(MapHelper.getInt(e, "endDisplay"));

        config.setMissiontype(MapHelper.getInt(e, "missionType"));

        config.setTargetcount(MapHelper.getInt(e, "targetCount"));

        config.setAddtion(MapHelper.getInt(e, "addtion"));

        config.setFinishreward(MapHelper.getIntArray(e, "finishReward"));

        config.setPointreward(MapHelper.getInt(e, "pointReward"));


        _ix_id.put(config.getId(), config);

        refreshTime(config);
    }

    /**
     * ==========================================================================
     */

    /**
     * 活动最长的开启时间
     */
    private int maxOpenTime;
    private int maxDisTime;

    private void refreshTime(NoviceTaskObject config) {
        if (config == null) {
            return;
        }

        int openTime = config.getCloseday();
        if (openTime > maxOpenTime) {
            maxOpenTime = openTime;
        }

        int disTime = config.getEnddisplay();
        if (disTime > maxDisTime) {
            maxDisTime = disTime;
        }
    }

    public int getMaxOpenTime() {
        return maxOpenTime;
    }

    public int getMaxDisTime() {
        return maxDisTime;
    }
}
