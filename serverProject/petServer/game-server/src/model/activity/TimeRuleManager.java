package model.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cfg.TimeRuleCfg;
import cfg.TimeRuleCfgObject;
import common.tick.GlobalTick;
import common.tick.Tickable;
import model.activity.sub.ActivityDataCrossArena;
import model.activity.sub.ActivityDataCrossArenaTop;
import model.activity.sub.ActivityDataUsual;
import model.activity.timerule.TimeRuleType;
import util.timerule.DateUtil;
import util.timerule.SafeTimer;
import util.timerule.TimeRule;
import util.timerule.TimeRuleRange;
import util.timerule.TimeRules;

/**
 * 活动管理类
 */
public class TimeRuleManager implements Tickable {

	private static final Logger logger = LoggerFactory.getLogger(ActivityManager.class);

	/** 所有活动 */
	private Map<Integer, ActivityData> activityMap = new HashMap<Integer, ActivityData>();

	private static TimeRuleManager instance = new TimeRuleManager();

	private TimeRuleManager() {
	}

	public static TimeRuleManager getInstance() {
		return instance;
	}

	private SafeTimer st = new SafeTimer(2000L);
	
	/**
	 * 只允许服务器启动时调用
	 */
	public boolean init() {
		checkAcitivity();
		GlobalTick.getInstance().addTick(this);
		return true;
	}

	/**
	 * 检查活动原始大条件是否满足
	 */
	public void checkAcitivity() {
		// 然后初始化活动
		long nowTime = System.currentTimeMillis();
		for (TimeRuleCfgObject amd : TimeRuleCfg._ix_id.values()) {
			if (amd.getId() <= 0) {
				continue;
			}
			ActivityData adnew = createActivityData(amd);
			if (null == adnew) {
				continue;
			}
			if (!adnew.init()) {
				continue;
			}
			long startTimeBig = adnew.getRuleExe().getBeginTime().getTime();
			long endTimeBig = adnew.getRuleExe().getEndTime().getTime();
			if (nowTime > endTimeBig || nowTime < startTimeBig) {
				if (activityMap.containsKey(amd.getId())) {
					ActivityData ad = activityMap.get(amd.getId());
					if (ad.isOpened) {
						ad.onEnd();
					}
					continue;
				}
			}
			if (activityMap.containsKey(amd.getId())) {
				continue;
			}
			adnew.setBeginTime(startTimeBig);
			adnew.setEndTime(endTimeBig);
			adnew.computeNextTime();
			activityMap.put(adnew.getTimeRuleId(), adnew);
		}
	}

	/**
	 * 重新加载了模板数据后调用
	 */
	public void reloadModelData() {
		checkAcitivity();
	}

	/**
	 * 每小时检测
	 */
	public void everyHour() {
		checkAcitivity();
	}

	/**
	 * 过天刷新
	 */
	public void on24Hour() {
		checkAcitivity();
		for (ActivityData ad : activityMap.values()) {
			ad.computeNextTime();
			ad.computeCloseTime();
		}
	}

	/**
	 * 刷帧
	 */
	public void onTick() {
		long startTime = GlobalTick.getInstance().getCurrentTime();
		if (!st.isIntervalOK(startTime)) {
			return;
		}
		for (ActivityData ad : activityMap.values()) {
			try {
				ad.update(startTime);
				if (System.currentTimeMillis() - startTime > 1000) {
					logger.error(ad.getRuleExe().getTimeRuleId() + "_活动刷帧耗时=" + (System.currentTimeMillis() - startTime));
				}
			} catch (Exception e) {
				logger.error(ad.getRuleExe().getTimeRuleId() + "_活动刷帧异常!", e);
			}
		}
	}

	/**
	 * 根据活动ID获取一个活动数据
	 * 
	 * @param id
	 * @return
	 */
	public ActivityData getActivityData(int id) {
		return activityMap.get(id);
	}

	/**
	 * 获取是否开启
	 * 
	 * @param actId
	 * @return
	 */
	public boolean isOpenTime(int actId) {
		ActivityData ad = getActivityData(actId);
		if (null == ad) {
			return false;
		}
		return ad.isOpened;
	}

	/**
	 * 根据活动ID获取活动大时间段,开始时间
	 * 
	 * @param actId
	 * @return
	 */
	public long getBeginTime(int actId) {
		ActivityData ad = activityMap.get(actId);
		if (null == ad) {
			return 0;
		}
		return ad.getBeginTime();
	}

	/**
	 * 根据活动ID获取活动大时间段,结束时间
	 * 
	 * @param actId
	 * @return
	 */
	public long getEndTime(int actId) {
		ActivityData ad = activityMap.get(actId);
		if (null == ad) {
			return 0;
		}
		return ad.getEndTime();
	}

	/**
	 * 获取活动的下次开启时间
	 * 
	 * @param actId
	 * @return
	 */
	public long getNextTimeOpen(int actId) {
		ActivityData ad = activityMap.get(actId);
		if (null == ad) {
			return 0;
		}
		Calendar cal = getNextCalendarByActivity(ad);
		if (null == cal) {
			return 0;
		}
		return cal.getTimeInMillis();
	}

	/**
	 * 调用需谨慎null 下次开启活动的时间戳
	 */
	public Calendar getNextCalendarByActivity(ActivityData ad) {
		TimeRules te = ad.getRuleExe().getTimeRules();
		return getNextCalendar(te);
	}

	/**
	 * 调用需谨慎null 下次开启活动的时间戳
	 * 
	 * @param te
	 * @return
	 */
	public Calendar getNextCalendar(TimeRules te) {
		if (null == te) {
			return null;
		}
		Calendar next = getNextActivityCalendar(te);
		if (null != next) {
			return next;
		} else {
			// 说明当天不会在开启活动了，需要计算下一天的开启时间点
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			int i = 0;
			while (i < 32) {
				cal.add(Calendar.DAY_OF_MONTH, 1);
				next = getNextActivityCalendar(cal.getTimeInMillis(), te);
				if (null != next) {
					break;
				}
				i++;
			}
		}
		return next;
	}

	/**
	 * 距离下次开启活动的时间戳（这个方法只能判断当天）
	 */
	private Calendar getNextActivityCalendar(TimeRules te) {
		return getNextActivityCalendar(System.currentTimeMillis(), te);
	}

	private Calendar getNextActivityCalendar(long time, TimeRules te) {
		Calendar start = null;
		if (null == te) {
			return start;
		}
		int seconds = Integer.MAX_VALUE;
		for (TimeRule expression : te.getRules()) {
			if (expression.isValidateDate(time)) {
				if (expression.isLoopWeek()) {
					if (DateUtil.isThatSameWeek(time)) {// 是同一周
						for (TimeRuleRange crtu : expression.getTimes()) {
							int temp = crtu.getRangeOfStart(time);
							if (temp != -1 && temp < seconds) {
								seconds = temp;
								start = crtu.getStartTimestamp(time);
								return start;
							}
						}
					} else {// 不是当天 能进来 就肯定是下次
						for (TimeRuleRange crtu : expression.getTimes()) {
							start = crtu.getStartTimestamp(time);
							break;
						}
						break;
					}
				} else {
					if (DateUtil.isToday(time)) {// 是当天
						for (TimeRuleRange crtu : expression.getTimes()) {
							int temp = crtu.getRangeOfStart();
							if (temp != -1 && temp < seconds) {
								seconds = temp;
								start = crtu.getStartTimestamp();
								return start;
							}
						}
					} else {// 不是当天 能进来 就肯定是下次
						for (TimeRuleRange crtu : expression.getTimes()) {
							start = crtu.getStartTimestamp(time);
							break;
						}
						break;
					}
				}
			}
		}
		return start;
	}

	/**
	 * 创建活动数据
	 *
	 * @return
	 */
	public static ActivityData createActivityData(TimeRuleCfgObject amd) {
		switch (amd.getType()) {
			case TimeRuleType.TYPE_LT:
				return new ActivityDataCrossArena(amd.getId());
			case TimeRuleType.TYPE_LT_10:
				return new ActivityDataCrossArena(amd.getId());
			case TimeRuleType.TYPE_TOP:
				return new ActivityDataCrossArenaTop(amd.getId());
			case TimeRuleType.TYPE_TOP_R:
				return new ActivityDataCrossArenaTop(amd.getId());
			case TimeRuleType.TYPE_TOP_G:
				return new ActivityDataCrossArenaTop(amd.getId());
			case TimeRuleType.TYPE_TOP_B:
				return new ActivityDataCrossArenaTop(amd.getId());
			case TimeRuleType.TYPE_TOP_V:
				return new ActivityDataCrossArenaTop(amd.getId());
			default:
				return new ActivityDataUsual(amd.getId());
		}
	}

}
