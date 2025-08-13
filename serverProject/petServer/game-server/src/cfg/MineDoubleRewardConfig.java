/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import datatool.MapHelper;
import model.base.baseConfig;
import util.TimeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "MineDoubleRewardConfig", methodname = "initConfig")
public class MineDoubleRewardConfig extends baseConfig<MineDoubleRewardConfigObject> {


    private static MineDoubleRewardConfig instance = null;

    public static MineDoubleRewardConfig getInstance() {

        if (instance == null)
            instance = new MineDoubleRewardConfig();
        return instance;

    }


    public static Map<Integer, MineDoubleRewardConfigObject> _ix_id = new HashMap<Integer, MineDoubleRewardConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MineDoubleRewardConfig) o;
        initConfig();

//        updateRewardTime();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MineDoubleRewardConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MineDoubleRewardConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MineDoubleRewardConfigObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setStartime(MapHelper.getInt(e, "StarTime"));

        config.setEndtime(MapHelper.getInt(e, "EndTime"));

        config.setExtrarewardrate(MapHelper.getInt(e, "ExtraRewardRate"));


        _ix_id.put(config.getId(), config);


    }

// -----------------------------------自定义---------------------------------------------//
    protected boolean updateRewardTime() {
        long tmpTime = 0;
        for (MineDoubleRewardConfigObject cfgObj :_ix_id.values()) {
            if ((tmpTime > 0 && tmpTime >= cfgObj.getStartime()) || cfgObj.getStartime() >= cfgObj.getEndtime()) {
                throw new RuntimeException("MineDoubleReward Config error, CfgId=" + cfgObj.getId());
            }
            if (tmpTime == 0 || tmpTime < cfgObj.getStartime()) {
                tmpTime = cfgObj.getEndtime();
            }
        }
        return true;
    }

    public int getExtraRewardRate() {
        int rate = 0;
        long startTime;
        long endTime;
        long curTime = GlobalTick.getInstance().getCurrentTime();
        long todayBeginTime = TimeUtil.getTodayStamp(curTime);
        for (MineDoubleRewardConfigObject cfgObj : _ix_id.values()) {
            startTime = todayBeginTime + cfgObj.getStartime() * TimeUtil.MS_IN_A_MIN;
            endTime = todayBeginTime + cfgObj.getEndtime() * TimeUtil.MS_IN_A_MIN;
            if (curTime >= startTime && curTime < endTime) {
                rate = cfgObj.getExtrarewardrate();
                break;
            }
        }
        return rate;
    }

    public long getNextStartRewardTime(long curTime) {
        if (_ix_id.isEmpty()) {
            return -1;
        }
        long nextTime = 0;
        long tmpTime = 0;
        long todayBeginTime = TimeUtil.getTodayStamp(curTime);
        for (MineDoubleRewardConfigObject cfgObj : _ix_id.values()) {
            long startTime = todayBeginTime + cfgObj.getStartime() * TimeUtil.MS_IN_A_MIN;
            long endTime = todayBeginTime + cfgObj.getEndtime() * TimeUtil.MS_IN_A_MIN;
            if (curTime >= endTime) {
                continue;
            } else if (curTime >= startTime && curTime < endTime) {
                nextTime = startTime;
                break;
            } else if (curTime < startTime) {
                if (tmpTime == 0 || tmpTime > startTime) {
                    tmpTime = startTime;
                }
            }
        }
        if (nextTime == 0) {
            if (tmpTime > 0) {
                nextTime = tmpTime;
            } else {
                nextTime = todayBeginTime + TimeUtil.MS_IN_A_DAY + _ix_id.get(1).getStartime() * TimeUtil.MS_IN_A_MIN;
            }
        }
        return nextTime;
    }

    public long getNextEndRewardTime(long curTime) {
        if (_ix_id.isEmpty()) {
            return -1;
        }
        long nextTime = 0;
        long tmpTime = 0;
        long todayBeginTime = TimeUtil.getTodayStamp(curTime);
        for (MineDoubleRewardConfigObject cfgObj : _ix_id.values()) {
            long startTime = todayBeginTime + cfgObj.getStartime() * TimeUtil.MS_IN_A_MIN;
            long endTime = todayBeginTime + cfgObj.getEndtime() * TimeUtil.MS_IN_A_MIN;
            if (curTime >= endTime) {
                continue;
            } else if (curTime >= startTime && curTime < endTime) {
                nextTime = endTime;
                break;
            } else if (curTime < startTime) {
                if (tmpTime == 0 || tmpTime > endTime) {
                    tmpTime = endTime;
                }
            }
        }
        if (nextTime == 0) {
            if (tmpTime > 0) {
                nextTime = tmpTime;
            } else {
                nextTime = todayBeginTime + TimeUtil.MS_IN_A_DAY + _ix_id.get(1).getEndtime() * TimeUtil.MS_IN_A_MIN;
            }
        }
        return nextTime;
    }
}
