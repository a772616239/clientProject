package util.timerule;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 时间工具类
 */
public class DateUtil {

	/**
	 * 毫秒时间(单位毫秒)；
	 */
	public final static long MILLISECOND = 1L;
	/**
	 * 秒(单位毫秒)；
	 */
	public final static long SECONDETIME = 1000l;
	/**
	 * 分钟(单位毫秒)；
	 */
	public final static long MINUTETIME = 60000l;
	/**
	 * 小时(单位毫秒)；
	 */
	public final static long HOURTIME = 3600000l;
	/**
	 * 天(单位毫秒)
	 */
	public final static long DAYTIME = 86400000l;

	private final static Logger logger = Logger.getLogger(DateUtil.class);

	private DateUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取一年第几周
	 */
	public static int getWeekOfYear() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.WEEK_OF_YEAR);
    }
	
	/**
	 * 获取一年第几天
	 */
	public static int getDayOfYear() {
        Calendar c = Calendar.getInstance();
        c.get(Calendar.HOUR_OF_DAY);
        return c.get(Calendar.DAY_OF_YEAR);
    }

	/**
	 * 查询当前年中的第几天
	 * 
	 * @param time
	 * @return
	 */
	public static int getDayofYear(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar.get(Calendar.DAY_OF_YEAR);
	}
	
	/**
	 * 获取一天中得第几小时
	 */
	public static int getHourOfDay() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY);
    }
	
	public static int getDayOfWeek() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.DAY_OF_WEEK);
	}
	
	public static int getCurrentWeekOfYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		return calendar.get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * 获取上周 是当前年中的第几周
	 */
	public static int getPreWeekOfYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.add(Calendar.WEEK_OF_YEAR, -1);
		return calendar.get(Calendar.WEEK_OF_YEAR);
	}
	
	/**
	 * 格式:yyyy-MM-dd HH:mm:ss
	 */
	private static ThreadLocal<DateFormat> sdf = new ThreadLocal<DateFormat>() {
		protected synchronized DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};
	/**
	 * 格式:yyyy年MM月dd日HH时mm分
	 */
	private static ThreadLocal<DateFormat> sdfcn = new ThreadLocal<DateFormat>() {
		protected synchronized DateFormat initialValue() {
			return new SimpleDateFormat("yyyy年MM月dd日HH时mm分");
		}
	};
	/**
	 * 格式:yyyy-MM-dd
	 */
	private static ThreadLocal<DateFormat> daySDF = new ThreadLocal<DateFormat>() {
		protected synchronized DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};
	/**
	 * 格式:HH:mm:ss
	 */
	private static ThreadLocal<DateFormat> timeSDF = new ThreadLocal<DateFormat>() {
		protected synchronized DateFormat initialValue() {
			return new SimpleDateFormat("HH:mm:ss");
		}
	};
	/**
	 * 格式:HH:mm:ss
	 */
	private static ThreadLocal<DateFormat> hourSDF = new ThreadLocal<DateFormat>() {
		protected synchronized DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH");
		}
	};
	/**
	 * 格式:yyyy年MM月dd日 HH:mm
	 */
	private static ThreadLocal<DateFormat> marriageTimeSDF = new ThreadLocal<DateFormat>() {
		protected synchronized DateFormat initialValue() {
			return new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
		}
	};
	/**
	 * 格式:mm分ss秒
	 */
	private static ThreadLocal<DateFormat> hAndMSDF = new ThreadLocal<DateFormat>() {
		protected synchronized DateFormat initialValue() {
			return new SimpleDateFormat("mm分ss秒");
		}
	};

	/**
	 * 格式:yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static String getStringDate(Date date) {
		return sdf.get().format(date);
	}

	/**
	 * 格式:yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static String getShortDate2String(Date date) {
		return daySDF.get().format(date);
	}
	
	public static String getLong2String(long time) {
		Date date = new Date(time);
		return daySDF.get().format(date);
	}

	/**
	 * 格式:yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static String getShortDateHourString(Date date) {
		return hourSDF.get().format(date);
	}

	/**
	 * 格式:yyyy-MM-dd HH:mm:ss
	 * 
	 * @param time
	 * @return
	 */
	public static String getStringDate(long time) {
		Date date = new Date(time);
		return sdf.get().format(date);
	}

	/**
	 * 格式:yyyy年MM月dd日HH时mm分
	 * 
	 * @param time
	 * @return
	 */
	public static String getStringDateCN(long time) {
		Date date = new Date(time);
		return sdfcn.get().format(date);
	}

	/**
	 * 格式:yyyy年MM月dd日HH时mm分
	 * 
	 * @param date
	 * @return
	 */
	public static String getStringDateCN(Date date) {
		return sdfcn.get().format(date);
	}

	/**
	 * 格式:yyyy-MM-dd HH:mm:ss
	 * 
	 * @return
	 */
	public static String getNowStringDate() {
		return sdf.get().format(new Date());
	}

	/**
	 * 参数格式:yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDateByString(String date) {
		try {
			return sdf.get().parse(date);
		} catch (ParseException e) {
			logger.error(date + "日期格式有误 yyyy-MM-dd HH:mm:ss");
			return null;
		}
	}

	/**
	 * 参数格式:yyyy-MM-dd
	 * 
	 * @param days
	 * @return
	 */
	public static Date getDateByStringOnlyDay(String days) {
		try {
			return daySDF.get().parse(days);
		} catch (ParseException e) {
			logger.error(days + "日期格式有误 yyyy-MM-dd");
			return null;
		}
	}

	/**
	 * 参数格式:HH:mm:ss
	 * 
	 * @param times
	 * @return
	 */
	public static Date getDateByStringOnlyTimes(String times) {
		try {
			return timeSDF.get().parse(times);
		} catch (ParseException e) {
			logger.error(times + "日期格式有误 HH:mm:ss");
			return null;
		}
	}

	/**
	 * 格式:HH:mm:ss
	 * 
	 * @param times
	 * @return
	 */
	public static String getDateForLongToString(long times) {
		try {
			return timeSDF.get().format(times);
		} catch (Exception e) {
			logger.error(times + "日期格式有误 HH:mm:ss");
			return "";
		}
	}

	/**
	 * 格式:yyyy年MM月dd日 HH:mm
	 * 
	 * @param times
	 * @return
	 */
	public static String getMarriageDateForLongToString(long times) {
		try {
			return marriageTimeSDF.get().format(times);
		} catch (Exception e) {
			logger.error(times + "日期格式有误 yyyy年MM月dd日 HH:mm");
			return null;
		}
	}

	/**
	 * 格式:mm分ss秒
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateByLong(Date date) {
		try {
			return hAndMSDF.get().format(date);
		} catch (Exception e) {
			logger.error(date + "日期格式有误 mm:ss");
		}
		return null;
	}

	/**
	 * 取当前时间
	 */
	public static Date getCurrentDate() {
		try {
			return new Date(System.currentTimeMillis());
		} catch (Exception e) {
			logger.error("DateUtil getCurrentDate()");
		}
		return null;
	}

	/**
	 * 判断时间 是不是同一天 true：是今天 false：不是今天
	 */
	public static boolean isToday(long time) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		// 修改时间为数据库记录时间
		c2.setTimeInMillis(time);
		if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
			return false;
		}
		int day1 = c1.get(Calendar.DAY_OF_YEAR);
		int day2 = c2.get(Calendar.DAY_OF_YEAR);
		return day1 == day2;
	}

	/**
	 * 判断两个时间 是不是同一天 true：是 false：不是
	 */
	public static boolean isToday(long time1, long time2) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTimeInMillis(time1);
		c2.setTimeInMillis(time2);
		if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
			return false;
		}
		int day1 = c1.get(Calendar.DAY_OF_YEAR);
		int day2 = c2.get(Calendar.DAY_OF_YEAR);
		return day1 == day2;
	}

	/**
	 * 是不是同一天
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static boolean isTheSameDay(Date d1, Date d2) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(d1);
		c2.setTime(d2);
		return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) && (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH))
				&& (c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * 判断时间 是不是同一个月 true：是同一个月 false：不是同一个月
	 */
	public static boolean isThatSameMonth(long time) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		// 修改时间为数据库记录时间
		c2.setTimeInMillis(time);
		if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
			return false;
		}
		int month1 = c1.get(Calendar.MONTH);
		int month2 = c2.get(Calendar.MONTH);

		return month1 == month2;
	}

	/**
	 * 判断时间 是不是本年的同一周
	 */
	public static boolean isThatSameWeek(long time) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		// 修改时间为数据库记录时间
		c2.setTimeInMillis(time);

		c2.setFirstDayOfWeek(Calendar.MONDAY);
		c1.setFirstDayOfWeek(Calendar.MONDAY);

		if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
			return false;
		}
		int week1 = c1.get(Calendar.WEEK_OF_YEAR);
		int week2 = c2.get(Calendar.WEEK_OF_YEAR);

		return week1 == week2;
	}

	/**
	 * 判断时间 是不是实际周的上一周
	 */
	public static boolean isThatBeforeWeek(long time) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		// 修改时间为数据库记录时间
		c2.setTimeInMillis(time);

		c2.setFirstDayOfWeek(Calendar.MONDAY);
		c1.setFirstDayOfWeek(Calendar.MONDAY);

		if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
			return false;
		}
		int week2 = c2.get(Calendar.WEEK_OF_YEAR);

		return getPreWeekOfYear() == week2;
	}

	/**
	 * 判断两个时间 是不是同一周 true:是；false:不是
	 */
	public static boolean isThatSameWeek(long time1, long time2) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTimeInMillis(time1);
		c2.setTimeInMillis(time2);
		c1.setFirstDayOfWeek(Calendar.MONDAY);
		c2.setFirstDayOfWeek(Calendar.MONDAY);
		if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
			return false;
		}
		int week1 = c1.get(Calendar.WEEK_OF_YEAR);
		int week2 = c2.get(Calendar.WEEK_OF_YEAR);
		return week1 == week2;
	}

	/**
	 * 获取相差天数
	 * @param time
	 * @return
	 */
	public static int differDays(long time) {
		long day1 = LocalDate.now().toEpochDay();
		Instant instant = Instant.ofEpochMilli(time);
		long day2 = instant.atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay();
		return (int) (day1 - day2);
	}

	/**
	 * 获取相差天数
	 * 
	 * @return
	 */
	public static long differDays(long time1, long time2) {
		long day1 = Instant.ofEpochMilli(time1).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay();
		long day2 = Instant.ofEpochMilli(time2).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay();
		return day1 - day2;
	}
	
	/**
	 * 获取相差周数
	 * 
	 * @param time
	 * @return
	 */
	public static int differWeeks(long time) {
		Calendar week1 = Calendar.getInstance();
		week1 = getTimeWeekStart0DayByTime(week1.getTimeInMillis());
		Calendar week2 = getTimeWeekStart0DayByTime(time);
		return (int) ((week1.getTimeInMillis() - week2.getTimeInMillis()) / 604800000l);
	}

	/**
	 * 判断时间 距离当月最后一个24点相差多少 long
	 */
	public static long nowCut24(long time) {
		Calendar c2 = Calendar.getInstance();
		c2.setTimeInMillis(time);
		c2.set(Calendar.DAY_OF_MONTH, c2.getActualMaximum(Calendar.DAY_OF_MONTH));
		c2.set(Calendar.HOUR_OF_DAY, c2.getActualMaximum(Calendar.HOUR_OF_DAY));
		c2.set(Calendar.MINUTE, c2.getActualMaximum(Calendar.MINUTE));
		c2.set(Calendar.SECOND, c2.getActualMaximum(Calendar.SECOND));
		long longTime24 = c2.getTimeInMillis();
		longTime24 = longTime24 - time;

		return longTime24;
	}

	/**
	 * 判断时间距离当月第一个凌晨0点相差多少long
	 */
	public static long nowCut0(long time) {
		Calendar c2 = Calendar.getInstance();
		c2.setTimeInMillis(time);
		c2.set(Calendar.DAY_OF_MONTH, c2.getActualMinimum(Calendar.DAY_OF_MONTH));
		c2.set(Calendar.HOUR_OF_DAY, c2.getActualMinimum(Calendar.HOUR_OF_DAY));
		c2.set(Calendar.MINUTE, c2.getActualMinimum(Calendar.MINUTE));
		c2.set(Calendar.SECOND, c2.getActualMinimum(Calendar.SECOND));
		long longTime0 = c2.getTimeInMillis();
		longTime0 = time - longTime0;

		return longTime0;
	}

	/**
	 * 
	 * 取当前时间的共计多少小时
	 * 
	 * @param time
	 *            (单位毫秒)
	 * @return
	 */
	public static int hasSomeHours(long time) {
		int i = (int) (time / 60 / 60 / 1000);
		return i;
	}

	/**
	 * 
	 * 取当前时间的共计多少分钟
	 * 
	 * @param time
	 *            (单位毫秒)
	 * @return
	 */
	public static int hasSomeMinute(long time) {
		int i = (int) (time / 60 / 1000);
		return i;
	}

	/**
	 * 取当前时间共有多少秒
	 * 
	 * @param time
	 * @return
	 */
	public static int hasSomeSecond(long time) {
		int i = (int) (time / 1000);
		return i;
	}

	/**
	 * 取当前时间不到1小时的毫秒数
	 */
	public static long hasSomeMillisecond(long time) {
		long i = time % (60 * 60 * 1000);
		return i;
	}

	/**
	 * 把格式为HH:mm:ss的字符串转换成HHmmss以int形式返回。
	 * 
	 * @param time
	 *            时间。
	 * @return 转换成HHmmss以int形式返回。
	 */
	public static int timeToInt(String time) {
		if (StringUtils.isNotBlank(time)) {
			try {
				time = time.replaceAll(":", "");
				int result = Integer.parseInt(time, 10);
				return result;
			} catch (NumberFormatException ex) {
				// ignore
				logger.error("", ex);
			}
		}
		return 0;
	}

	/**
	 * 格式:HHmmss 5|6位整数
	 * 
	 * @return
	 */
	public static int timeToInt() {
		return timeToInt(new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}

	/**
	 * 返回:x小时x分钟x秒
	 * 
	 * @param millisecond
	 * @return
	 */
	public static String timeShow(long millisecond) {
		long v = millisecond / 1000;
		long s = v % 60;
		long m = v / 60 % 60;
		long h = v / 3600 % 86400;
		return h + "小时" + m + "分" + s + "秒";
	}

	/**
	 * 返回:x天x小时x分钟x秒
	 * 
	 * @param millisecond
	 * @return
	 */
	public static String timeShow2(long millisecond) {
		long v = millisecond / 1000;
		long s = v % 60;
		long m = v / 60 % 60;
		long h = v / 3600 % 24;
		long d = v / (3600 * 24);
		return d + "天" + h + "小时" + m + "分" + s + "秒";
	}

	/**
	 * 返回:hh:mm:ss
	 * 
	 * @param millisecond
	 * @return
	 */
	public static String timeSho3(long millisecond) {
		long v = millisecond / 1000;
		long s = v % 60;
		String time = "";
		long m = v / 60 % 60;
		long h = v / 3600 % 86400;
		if (h < 10l) {
			time += "0" + h + ":";
		} else {
			time += h + ":";
		}
		if (m < 10l) {
			time += "0" + m + ":";
		} else {
			time += m + ":";
		}
		if (s < 10l) {
			time += "0" + s;
		} else {
			time += s;
		}
		return time;
	}

	/**
	 * 获取日志 名字
	 * 
	 * @return
	 */
	public static String getLoggerName() {
		return (new SimpleDateFormat("yyyyMMdd")).format(System.currentTimeMillis());
	}

	/**
	 * 根据毫秒数 返回 hh:mm:ss格式的时间
	 * 
	 * @param millisecond
	 * @return
	 */
	public static String millisecondFormat(long millisecond) {
		String timeStr = null;
		long hour = 0;
		long minute = 0;
		long second = 0;
		long time = millisecond / 1000;

		if (time < 0) {
			return "00:00:00";
		} else {
			minute = time / 60;
			if (minute < 60) {
				second = time % 60;
				timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
			} else {
				hour = minute / 60;
				if (hour > 99) {
					return "99:59:59";
				}

				minute = minute % 60;
				second = time - hour * 3600 - minute * 60;
				timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
			}
		}
		return timeStr;
	}

	/**
	 * 获取明天6点的时间  祝福值清空 获取清空时间
	 * 
	 * @return
	 */
	public static Calendar getNextDaySixHourseDate() {
		if (getTodaySixHourseDate().getTime().before(Timer.getNowDate())) {
			return getSixHourseDate();
		} else {
			return getTodaySixHourseDate();
		}
	}
	
	/**
	 * 获取之前6点时间
	 * 
	 * @return
	 */
	public static Calendar getBeforeDaySixHourseDate() {
		if (getTodaySixHourseDate().getTime().before(Timer.getNowDate())) {
			return getTodaySixHourseDate();
		} else {
			return getYestDaySixHourseDate();
		}
	}
	
	/**
	 * 获取明天1点的时间  祝福值清空 获取清空时间
	 * 
	 * @return
	 */
	public static Calendar getNextDayOneHourseDate() {
		if (getTodayOneHourseDate().getTime().before(Timer.getNowDate())) {
			return getOneHourseDate();
		} else {
			return getTodayOneHourseDate();
		}
	}
	
	/**
	 * 获取之前1点时间
	 * 
	 * @return
	 */
	public static Calendar getBeforeDayOneHourseDate() {
		if (getTodayOneHourseDate().getTime().before(Timer.getNowDate())) {
			return getTodayOneHourseDate();
		} else {
			return getYestDayOneHourseDate();
		}
	}
	
	/**
	 * 获取当前0点的时间
	 * 
	 * @return
	 */
	public static Calendar getTimeZeroToday() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}
	
	/**
	 * 后去当前时间N天过后得开始时间
	 * 
	 * @return
	 */
	public static Calendar getTimeZeroDayBuNDay(int days) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.DATE, days);
		return c;
	}

	/**
	 * 获取指定时间0点时候的时间
	 * 
	 * @return
	 */
	public static Calendar getTimeZeroDayByTime(long time) {
		Calendar c = DateUtil.dateToCalendar(new Date(time));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}
	
	/**
	 * 获取当前时间24点时候的时间
	 * 
	 * @return
	 */
	public static Calendar getTimeEndToday() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return c;
	}

	/**
	 * 获取指定时间24点时候的时间
	 * 
	 * @return
	 */
	public static Calendar getTimeEndDayByTime(long time) {
		Calendar c = DateUtil.dateToCalendar(new Date(time));
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return c;
	}
	
	/**
	 * 获取当前时间N天过后得结束时间
	 * 
	 * @return
	 */
	public static Calendar getTimeEndDayByNDay(int days) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		c.add(Calendar.DATE, days);
		return c;
	}
	
	/**
	 * 获取当前小时的结束时间
	 * @return
	 */
	public static Calendar getTimeEndHour() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return c;
	}
	
	/**
	 * 获取当天指定小时的结束时间
	 * @param hour
	 * @return
	 */
	public static Calendar getTimeEndHour(int hour) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return c;
	}
	
	/**
	 * 获取指定时间得小时结束时间
	 * @param tmpDate
	 * @return
	 */
	public static Calendar getTimeEndHourByDate(Date tmpDate){
		Calendar c = DateUtil.dateToCalendar(tmpDate);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return c;
	}

	/**
	 * 获取 下周一0点时间
	 * 
	 * @return
	 */
	public static Calendar getTimeNextWeekStart0Day() {
		Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.add(Calendar.WEEK_OF_YEAR, 1);
		calendar.set(Calendar.DAY_OF_WEEK, 2);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
	public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SATURDAY);
        calendar.set(Calendar.DAY_OF_WEEK, 2);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 0);
		System.err.println(calendar.getTimeInMillis());
		System.err.println(getDayOfWeek());
		System.err.println(5/100F);
		System.err.println(1.8/12F);
		int arg1 = 100+123+156+138+105+180;
		int arg2 = 105+156+178+163+142;
		int avg1 = Math.round(arg1*1F/6);
		int avg2 = Math.round(arg2*1F/5);
		int avg3 = Math.round((arg2+arg1)*1F/11);
		int avg4 = Math.round((avg1+avg2)*1F/2);
		System.err.println(avg1);
		System.err.println(avg2);
		System.err.println(avg3);
		System.err.println(avg4);
		List<Integer> a = Arrays.asList(1,2,3,4,5,6,7,8,9);
		System.err.println(a.subList(0, 4));
	}
	/**
	 * 获取指定时间N周后得周一0点时间
	 * 
	 * @return
	 */
	public static Calendar getTimeWeekStart0DayByTime(long time, int nextWeek) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(time));
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.add(Calendar.WEEK_OF_YEAR, nextWeek);
		calendar.set(Calendar.DAY_OF_WEEK, 2);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	/**
	 * 获取指定时间周一0点时间
	 * 
	 * @return
	 */
	public static Calendar getTimeWeekStart0DayByTime(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.set(Calendar.DAY_OF_WEEK, 2);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
	
	/**
	 * 获取当前天剩余时间毫秒
	 * @return
	 */
	public static long getSurplusTimeToday() {
		Date curr = new Date();
		Date next = getTimeEndDayByTime(curr.getTime()).getTime();
		long time = next.getTime() - curr.getTime();
		return time;
	}
	
	public static Calendar getSixHourseDate() {
		Calendar c = DateUtil.dateToCalendar(Timer.getNowDate());
		c.set(Calendar.HOUR_OF_DAY, 6);
		c.add(Calendar.DATE, 1);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}
	
	/**
	 * 后天1点
	 * @return
	 */
	public static Calendar getOneHourseDate() {
		Calendar c = DateUtil.dateToCalendar(Timer.getNowDate());
		c.set(Calendar.HOUR_OF_DAY, 1);
		c.add(Calendar.DATE, 1);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}
	
	/**
	 * 获取当天6点的时间
	 * 
	 * @return
	 */
	public static Calendar getTodaySixHourseDate() {
		Calendar c = DateUtil.dateToCalendar(Timer.getNowDate());
		c.set(Calendar.HOUR_OF_DAY, 6);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}
	
	/**
	 * 获取当天1点的时间
	 * 
	 * @return
	 */
	public static Calendar getTodayOneHourseDate() {
		Calendar c = DateUtil.dateToCalendar(Timer.getNowDate());
		c.set(Calendar.HOUR_OF_DAY, 1);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}

	/**
	 * 获取昨天1点的时间
	 * 
	 * @return
	 */
	public static Calendar getYestDayOneHourseDate() {
		Calendar c = DateUtil.dateToCalendar(Timer.getNowDate());
		c.set(Calendar.HOUR_OF_DAY, 1);
		c.add(Calendar.DATE, -1);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}
	
	/**
	 * 获取昨天6点的时间
	 * 
	 * @return
	 */
	public static Calendar getYestDaySixHourseDate() {
		Calendar c = DateUtil.dateToCalendar(Timer.getNowDate());
		c.set(Calendar.HOUR_OF_DAY, 6);
		c.add(Calendar.DATE, -1);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}

	/**
	 * DATE 转换Calendar
	 * 
	 * @param date
	 * @return
	 */
	public static Calendar dateToCalendar(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}
	
	/**
	 * 把时分秒格式化为两位
	 * 
	 * @param time
	 * @return
	 */
	private static String unitFormat(long time) {
		if (time >= 0L && time < 10L) {
			return "0" + Integer.toString((int) time);
		} else {
			return "" + time;
		}
	}

	// LocalDate LocalDate LocalDate LocalDate LocalDate LocalDate
	/**
	 * 获取第n天的结束时间
	 * @return
	 */
	public static Date getLDEndTime(int day) {
		LocalDateTime todayTime=LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MAX);
		todayTime = todayTime.plusDays(day);
		Instant instant = todayTime.atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	/**
	 * 获取今天的结束时间
	 * @return
	 */
	public static Date getTodayStopTime(){
		LocalDateTime ldt = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
		return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
	}
	/**
	 * 获取下周一的凌晨
	 * @return
	 */
	public static Date getNextMondayZero(){
		LocalDate now =  LocalDate.now();
		LocalDate nextWeek =now.plus(1, ChronoUnit.WEEKS);
		TemporalField fieldISO = WeekFields.of(Locale.CHINA).dayOfWeek();
		LocalDate resultDate =nextWeek.with(fieldISO, 2);
		return  Date.from(resultDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	// 获取周第一天
	public static Date getStartDayOfWeek() {
		LocalDate now =  LocalDate.now();
		return getStartDayOfWeek(now);
	}

	public static Date getStartDayOfWeek(TemporalAccessor date) {
		TemporalField fieldISO = WeekFields.of(Locale.CHINA).dayOfWeek();
		LocalDate localDate = LocalDate.from(date);
		LocalDate localDate1 =localDate.with(fieldISO, 1);
		return Date.from(localDate1.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	// 获取周最后一天
	public static Date getEndDayOfWeek() {
		LocalDate localDate = LocalDate.now();
		return getEndDayOfWeek(localDate);
	}

	public static Date getEndDayOfWeek(TemporalAccessor date) {
		TemporalField fieldISO = WeekFields.of(Locale.CHINA).dayOfWeek();
		LocalDate localDate = LocalDate.from(date);
		LocalDate localDate1 = localDate.with(fieldISO, 7);
		return Date.from(localDate1.atStartOfDay(ZoneId.systemDefault()).plusDays(1L).minusNanos(1L).toInstant());
	}
	
	public static Date localDateTimeToDate(LocalDateTime ldtTime) {
		return  Date.from(ldtTime.atZone(ZoneId.systemDefault()).toInstant());
	}
	public static LocalDateTime dateToLocalDateTime(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();	
	}
	
	/**
	 * 获取指定日期的周末
	 * @return
	 */
	public static Date getWeekEndTime(Date d){
		LocalDateTime localTime= dateToLocalDateTime(d);
		localTime=LocalDateTime.of(localTime.toLocalDate(), LocalTime.MAX);
		localTime = localTime.with(DayOfWeek.SUNDAY);
		Instant instant = localTime.atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}
	
}
