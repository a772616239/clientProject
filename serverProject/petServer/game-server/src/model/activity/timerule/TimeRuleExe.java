package model.activity.timerule;

import cfg.TimeRuleCfg;
import cfg.TimeRuleCfgObject;
import util.LogUtil;
import util.timerule.TimeRules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeRuleExe {

    public int getTimeRuleId() {
        return timeRuleId;
    }

    private int timeRuleId = 0;

    private Date beginTime;
    private Date endTime;
    private TimeRules timeRules = null;

    public TimeRuleExe(int timeRuleId) {
        this.timeRuleId = timeRuleId;
    }

    public boolean init() {
        TimeRuleCfgObject def = TimeRuleCfg.getById(timeRuleId);
        if (null == def) {
            return false;
        }
        beginTime = new Date(1514736000000l);//默认2018-01-01 00:00:00
        if (null != def.getBegin_time() && !"".equals(def.getBegin_time())) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                beginTime = sdf.parse(def.getBegin_time());
            } catch (ParseException e) {
                LogUtil.error("活动数据异常，开始时间配置异常。活动ID=" + def.getId());
            }
        }
        endTime = new Date(2524579200000l);// 默认2050-01-01 00:00:00
        if (null != def.getEnd_time() && !"".equals(def.getEnd_time())) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                endTime = sdf.parse(def.getEnd_time());
            } catch (ParseException e) {
                LogUtil.error("活动数据异常，结束时间配置异常。活动ID=" + def.getId());
            }
        }
        if (def.getOpen_time() != null && !def.getOpen_time().equals("")) {
            try {
                timeRules = new TimeRules(def.getOpen_time());
            } catch (Exception e) {
                LogUtil.error("活动数据异常，区间时间配置异常。活动ID=" + def.getId(), e);
                e.printStackTrace();
            }
        }
        return true;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public TimeRules getTimeRules() {
        return timeRules;
    }

    /**
     * 是否在活动指定时间区间
     * @return
     */
    public boolean isInOpenTime() {
        return isInOpenTime(System.currentTimeMillis());
    }

    /**
     * 是否在活动指定时间区间
     * @return
     */
    public boolean isInOpenTime(long time) {
        if (timeRules == null)
            return false;
        return timeRules.isRuleTime(time);
    }

}
