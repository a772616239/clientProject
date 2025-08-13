package util.timerule;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateUtils;

/**
 * 24时制范围时间单元
 *
 */
public class TimeRuleRange {
	/** 开始时间(小时) */
	private int startHour;
	/** 开始时间(分钟) */
	private int startMinute;
	/** 开始时间(秒) */
	private int startSecend;

	/** 结束时间(小时) */
	private int stopHour;
	/** 结束时间(分钟) */
	private int stopMinute;
	/** 结束时间(秒) */
	private int stopSecend;

	/**
	 * 距离下次活动开始时间的剩余秒数
	 *
	 * @return
	 */
	public int getRangeOfStart() {
		Calendar start = getStartTimestamp();
		Calendar now = Calendar.getInstance();
		if (now.compareTo(start) == 1)
			return -1;
		return DateUtil.hasSomeSecond(start.getTimeInMillis() - now.getTimeInMillis());
	}

	/**
	 * 距离下次活动开始时间的剩余秒数
	 *
	 * @return
	 */
	public int getRangeOfStart(long time) {
		Calendar start = getStartTimestamp(time);
		Calendar now = Calendar.getInstance();
		if (now.compareTo(start) == 1)
			return -1;
		return DateUtil.hasSomeSecond(start.getTimeInMillis() - now.getTimeInMillis());
	}

	/**
	 * 距离结束时间剩余N秒
	 *
	 * @return
	 */
	public int getRangeOfEnd() {
		Calendar stop = getStopTimestamp();
		Calendar now = Calendar.getInstance();
		if (now.compareTo(stop) == 1)
			return -1;
		return DateUtil.hasSomeSecond(stop.getTimeInMillis() - now.getTimeInMillis());
	}

	/**
	 * 获取持续的秒数
	 *
	 * @return
	 */
	public int getContinuedSeconds() {
		int seconds = 0;
		seconds += (stopHour - startHour) * DateUtils.MILLIS_PER_HOUR / 1000;
		seconds += (stopMinute - startMinute) * DateUtils.MILLIS_PER_MINUTE / 1000;
		seconds += (stopSecend - startSecend);
		return seconds;
	}

	/**
	 * 获取当天开始时间戳
	 *
	 * @return
	 */
	public Calendar getStartTimestamp(long time) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(new Date(time));
		ca.set(Calendar.HOUR_OF_DAY, startHour);
		ca.set(Calendar.MINUTE, startMinute);
		ca.set(Calendar.SECOND, 0);
		ca.set(Calendar.MILLISECOND, 0);
		return ca;
	}

	/**
	 * 获取当天开始时间戳
	 *
	 * @return
	 */
	public Calendar getStartTimestamp() {
		Calendar ca = Calendar.getInstance();
		ca.set(Calendar.HOUR_OF_DAY, startHour);
		ca.set(Calendar.MINUTE, startMinute);
		ca.set(Calendar.SECOND, startSecend);
		ca.set(Calendar.MILLISECOND, 0);
		return ca;
	}

	/**
	 * 获取当前结束时间戳
	 *
	 * @return
	 */
	public Calendar getStopTimestamp() {
		Calendar ca = Calendar.getInstance();
		ca.set(Calendar.HOUR_OF_DAY, stopHour);
		ca.set(Calendar.MINUTE, stopMinute);
		ca.set(Calendar.SECOND, stopSecend);
		ca.set(Calendar.MILLISECOND, 0);
		return ca;
	}

	/**
	 * 是否是开始前n分钟
	 *
	 * @param minutes
	 * @return
	 */
	public boolean isStartBefore(int minutes) {
		Calendar time = getStartTimestamp();
		time.add(Calendar.MINUTE, (0 - minutes));
		return isSameTime(time);
	}

	/**
	 * 是否是结束前n分钟
	 *
	 * @param minutes
	 * @return
	 */
	public boolean isStopBefore(int minutes) {
		Calendar time = getStopTimestamp();
		time.add(Calendar.MINUTE, (0 - minutes));
		return isSameTime(time);
	}

	/**
	 * 比较两个日期相同(忽略秒和毫秒)
	 *
	 * @param time
	 * @return
	 */
	private boolean isSameTime(Calendar time) {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now.equals(time);
	}

	/**
	 * 当前时间是否在范围内
	 * @return
	 */
	public boolean isTime() {
		Calendar ca = Calendar.getInstance();
		Calendar caStart = getStartTimestamp();
		Calendar caEnd = getStopTimestamp();
		if (ca.getTimeInMillis() >= caStart.getTimeInMillis() && ca.getTimeInMillis() <= caEnd.getTimeInMillis()) {
			return true;
		}
		return false;
	}

	/**
	 * 当前时间是否在范围内
	 * @param checkhourtime
	 * @return
	 */
	public boolean isinhours(int checkhourtime) {
		if (checkhourtime >= getStartMillisecond() && checkhourtime < getStopMillisecond()) {
			return true;
		}
		return false;
	}

	public int getStartMillisecond() {
		return startHour * 60 * 60 * 1000 + startMinute * 60 * 1000 + startSecend * 1000;
	}

	public int getStopMillisecond() {
		return stopHour * 60 * 60 * 1000 + stopMinute * 60 * 1000 + stopSecend * 1000;
	}

	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	public void setStartMinute(int startMinute) {
		this.startMinute = startMinute;
	}

	public void setStartSecend(int startSecend) {
		this.startSecend = startSecend;
	}

	public void setStopHour(int stopHour) {
		this.stopHour = stopHour;
	}

	public void setStopMinute(int stopMinute) {
		this.stopMinute = stopMinute;
	}

	public void setStopSecend(int stopSecend) {
		this.stopSecend = stopSecend;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
