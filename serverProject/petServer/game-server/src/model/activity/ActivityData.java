package model.activity;

import model.activity.timerule.TimeRuleExe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.LogUtil;
import util.timerule.SafeTimer;
import util.timerule.TimeRule;
import util.timerule.TimeRuleRange;
import util.timerule.TimeRules;

import javax.sound.midi.SysexMessage;
import java.util.*;
import java.util.Map.Entry;

/**
 * 活动类
 */
public abstract class ActivityData implements IActivity {

	private static final Logger logger = LoggerFactory.getLogger(ActivityData.class);

	protected int timeRuleId = 0;

	protected TimeRuleExe ruleExe = null;
	// 活动是否开启标识
	protected volatile boolean isOpened = false;

	protected int state = 0;
	/**
	 * 活动开始前N分钟标识
	 */
	protected Map<Integer, Boolean> startBeforNMinute = new HashMap<Integer, Boolean>();
	/**
	 * 活动开始后N分钟标识
	 */
	protected Map<Integer, SafeTimer> startAfterNMinute = new HashMap<Integer, SafeTimer>();
	/**
	 * 活动结束前N分钟标识
	 */
	protected Map<Integer, Boolean> endBeforNMinute = new HashMap<Integer, Boolean>();
	/**
	 * 活动结束后N分钟标识
	 */
	protected Map<Integer, SafeTimer> endAfterNMinute = new HashMap<Integer, SafeTimer>();

	/** 活动开启大范围时间 */
	private long beginTime = 0;
	/** 活动结束大范围时间 */
	private long endTime = 0;
	/** 下一次活动周期开启时间 */
	private long nextOpenTime = 0;
	/** 当前活动周期开始时间 */
	private long loopOpenTime = 0;
	/** 当前活动周期关闭时间 */
	private long loopCloseTime = 0;

	public ActivityData(int timeRuleId) {
		this.timeRuleId = timeRuleId;
		addStartBeforNMinute();
	}

	public int getTimeRuleId() {
		return timeRuleId;
	}

	public boolean init() {
		ruleExe = new TimeRuleExe(timeRuleId);
		return ruleExe.init();
	}

	public void setRuleExe(TimeRuleExe ruleExe) {
		this.ruleExe = ruleExe;
	}

	public TimeRuleExe getRuleExe() {
		return ruleExe;
	}

	/**
	 * 该活动是否是开启时间
	 * 
	 * @return
	 */
	public boolean isBetweenOpenTime() {
		long time = System.currentTimeMillis();
		if (time < beginTime || time > endTime) {
			return false;
		}
		return ruleExe.isInOpenTime();
	}

	/**
	 * 是否需要开启
	 * 
	 * @return
	 */
	private boolean isNeedOpen() {
		if (isOpened) {// 已经开启了
			return false;
		}
		return isBetweenOpenTime();
	}

	/**
	 * 是否该关闭了
	 * 
	 * @return
	 */
	private boolean isNeedClose() {
		if (!isOpened) {// 还没有开启就不需要关闭
			return false;
		}
		if (isBetweenOpenTime()) {
			return false;
		}
		return true;
	}

	/**
	 * 结束前n分钟
	 * 
	 * @param beforeMinute 分钟
	 * @return
	 */
	private boolean beforeEnd(int beforeMinute) {
		if (!isOpened || beforeMinute >= 10) {
			// 活动未开和大于10分钟得不处理
			return false;
		}
		// 获取活动开始时间数据
		TimeRules timeRules = ruleExe.getTimeRules();
		// 无数据
		if (timeRules == null) {
			return false;
		}
		boolean flag = false;
		for (TimeRule tr : timeRules.getRules()) {
			if (tr.isContinuity() && !tr.isLast()) {
				// 连续时间段并且不是最后一个活动时间段，肯定执行不了结束前N分钟
				continue;
			}
			if (tr.isValidateDate(System.currentTimeMillis())) {
				for (TimeRuleRange trr : tr.getTimes()) {
					if (trr.isStopBefore(beforeMinute)) {
						flag = true;
						break;
					}
				}
			}
		}
		return flag;
	}

	/**
	 * 开始前n分钟
	 * 
	 * @param beforeMinute 分钟
	 * @return
	 */
	private boolean beforeStart(int beforeMinute) {
		if (isOpened)
			return false;
		// 获取活动开始时间数据
		TimeRules timeRules = ruleExe.getTimeRules();
		// 无数据
		if (timeRules == null)
			return false;
		boolean flag = false;
		for (TimeRule tr : timeRules.getRules()) {
			if (tr.isContinuity() && !tr.isFirst()) {
				// 连续时间段并且不是第一个活动时间段，肯定执行不了开始前N分钟
				continue;
			}
			if (tr.isValidateDate(System.currentTimeMillis())) {
				for (TimeRuleRange trr : tr.getTimes()) {
					if (trr.isStartBefore(beforeMinute)) {
						flag = true;
						break;
					}
				}
			}
		}
		return flag;
	}

	@Override
	public boolean start() {
		computeNextTime();
		if (setStartFlag()) {
			computeCloseTime();
			boolean state = onStart();
			if (!state) {
				isOpened = false;
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean end() {
		computeNextTime();
		if (setEndFlag()) {
			onEnd();
			return true;
		} else {
			return false;
		}
	}

	public abstract boolean onStart(

	);

	public boolean onEnd() {
		return true;
	}

	/**
	 * 设置活动开启标识
	 * 
	 * @return
	 */
	private boolean setStartFlag() {
		if (isOpened)
			return false;
		logger.error("【"+timeRuleId+"】活动开启!");
		isOpened = true;
		// 活动开启后清除掉活动开启前事件
		startBeforNMinute.clear();
		// 活动开启后清除掉活动结束后事件
		endAfterNMinute.clear();
		// 加载活动开启后事件
		addStartAfterNMinute();
		// 加载活动结束前事件
		addEndBeforNMinute();
		return true;
	}

	/**
	 * 设置活动结束标识
	 * 
	 * @return
	 */
	private boolean setEndFlag() {
		if (!isOpened)
			return false;
		logger.error("【" + timeRuleId + "】活动结束!");
		isOpened = false;
		// 活动结束后清除掉活动开启后事件
		startAfterNMinute.clear();
		// 活动结束后清除活动结束前事件
		endBeforNMinute.clear();
		// 加载活动开始前事件
		addStartBeforNMinute();
		// 加载活动结束后事件
		addEndAfterNMinute();
		return true;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * 活动开始设置开始后N分钟事件
	 * 
	 * @return
	 */
	public void addStartBeforNMinute() {
		startBeforNMinute.clear();
	}

	/**
	 * 活动开始设置开始后N分钟事件
	 * 
	 * @return
	 */
	public void addStartAfterNMinute() {
		startAfterNMinute.clear();
	}

	/**
	 * 活动开始设置结束前N分钟事件
	 * 
	 * @return
	 */
	public void addEndBeforNMinute() {
		endBeforNMinute.clear();
	}

	/**
	 * 活动开始设置结束后N分钟事件
	 * 
	 * @return
	 */
	public void addEndAfterNMinute() {
		endAfterNMinute.clear();
	}

	/**
	 * 开始前N分钟是否已经发生
	 * 
	 * @param minute
	 * @return
	 */
	public boolean isPastStartBeforN(int minute) {
		return startBeforNMinute.getOrDefault(minute, false);
	}

	/**
	 * 活动持续时间(秒)
	 * 
	 * @return
	 */
	public int getActivityTotalTime() {
		if (this.ruleExe.getTimeRules() == null)
			return 0;
		return this.ruleExe.getTimeRules().getTimeRuleRange();
	}

	/**
	 * 缓存下次活动开启时间
	 */
	public void computeNextTime() {
		Calendar cal = getNextActivityCalendar();
		if (null != cal) {
			nextOpenTime = cal.getTimeInMillis();
		}
	}

	/**
	 * 距离下次开启活动的时间戳
	 */
	public Calendar getNextActivityCalendar() {
		Calendar next = TimeRuleManager.getInstance().getNextCalendarByActivity(this);
		return next;
	}

	/**
	 * 获取下一次开启的时间
	 * 
	 * @return
	 */
	public long getNextOpenTime() {
		return nextOpenTime;
	}

	/**
	 * 获取当前活动周期关闭时间
	 * 
	 * @return
	 */
	public long getCloseTime() {
		return loopCloseTime;
	}

	public long getLoopOpenTime() {
		return loopOpenTime;
	}

	/**
	 * 时间段开始的活动,开启后计算活动周期关闭时间点
	 */
	public void computeCloseTime() {
		long oldCloseTime = loopCloseTime;
		loopCloseTime = 0;
		// 首先判断活动是否开启
		if (!isOpened) {
			return;
		}
		// 计算动周期性开启的结束时间
		// 是否是时间段开启
		if (null == ruleExe.getTimeRules()) {
			loopCloseTime = endTime;
			return;
		}
		// 是,则计算时间段内当前的结束时间
		Calendar now = Calendar.getInstance();
		for (TimeRule tr : this.ruleExe.getTimeRules().getRules()) {
			if (tr.isContinuity()) {
				if (tr.isLast()) {
					loopCloseTime = tr.getEndTime();
				}
			} else {
				if (tr.isAllHour()) {
					loopCloseTime = tr.getEndTime();
				} else {
					if (tr.isValidateDate(now.getTimeInMillis())) {
						for (TimeRuleRange trr : tr.getTimes()) {
							if (trr.isTime()) {
								Calendar stop = trr.getStopTimestamp();
								if (now.compareTo(stop) != 1) {
									if (stop.getTimeInMillis() > loopCloseTime) {
										loopCloseTime = stop.getTimeInMillis();
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 距离本次活动结束的时间(秒)
	 */
	public int getFromActivityEndTime() {
		if (this.ruleExe.getTimeRules() == null)
			return 0;
		int total = getActivityTotalTime();
		for (TimeRule tr : this.ruleExe.getTimeRules().getRules()) {
			if (tr.isValidateDate(System.currentTimeMillis()) && (!tr.getTimes().isEmpty())) {
				for (TimeRuleRange trr : tr.getTimes()) {
					if (trr.isTime()) {
						int temp = trr.getRangeOfEnd();
						if (temp != -1 && temp < total) {
							return temp;
						}
					}
				}
			}
		}
		return 0;
	}

	@Override
	public void update(long now) {
		// 检测活动开始前N分钟
		for (Entry<Integer, Boolean> ent : startBeforNMinute.entrySet()) {
			if (!ent.getValue() && beforeStart(ent.getKey())) {
				onStartBeforNMinute(ent.getKey());
			}
		}
		if (isNeedOpen())
			start();
		if (isNeedClose())
			end();
		// 检测活动开始后N分钟
		for (Entry<Integer, SafeTimer> ent : startAfterNMinute.entrySet()) {
			if (null != ent.getValue() && ent.getValue().isFirstOK(now)) {
				onStartAfterNMinute(ent.getKey());
			}
		}
		// 检测活动结束前N分钟
		for (Entry<Integer, Boolean> ent : endBeforNMinute.entrySet()) {
			if (!ent.getValue() && beforeEnd(ent.getKey())) {
				onEndBeforNMinute(ent.getKey());
			}
		}
		// 检测活动结束后N分钟
		for (Entry<Integer, SafeTimer> ent : endAfterNMinute.entrySet()) {
			if (null != ent.getValue() && ent.getValue().isFirstOK(now)) {
				onEndAfterNMinute(ent.getKey());
			}
		}
	}

	public boolean isOpened() {
		return isOpened;
	}

	public boolean isInOpenTime() {
		return ruleExe.isInOpenTime();
	}

	public boolean isInOpenTime(long time) {
		return ruleExe.isInOpenTime(time);
	}

	@Override
	public void onStartBeforNMinute(int minute) {
		startBeforNMinute.put(minute, true);
	}

	@Override
	public void onStartAfterNMinute(int minute) {
		startAfterNMinute.put(minute, null);
	}

	@Override
	public void onEndBeforNMinute(int minute) {
		endBeforNMinute.put(minute, true);
	}

	@Override
	public void onEndAfterNMinute(int minute) {
		endAfterNMinute.put(minute, null);
	}

}
