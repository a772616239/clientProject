package util;

import common.tick.GlobalTick;
import timetool.TimeHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {

    public static final long MS_IN_A_DAY = 24 * 60 * 60 * 1000;
    public static final long MS_IN_A_HOUR = 60 * 60 * 1000;
    public static final long MS_IN_A_MIN = 60 * 1000;
    public static final long MS_IN_A_S = 1000;

    public static final String defaultTimeZone = ServerConfig.getInstance().getTimeZone();

    /**
     * @return 1 - 7,周一到周日
     */
    public static int getDayOfWeek(long timeStamp) {
        Calendar calendar = createNewCalender(timeStamp, defaultTimeZone);
        int calenderWeekDay = calendar.get(Calendar.DAY_OF_WEEK);

        calenderWeekDay -= 1;
        if (calenderWeekDay == 0) {
            calenderWeekDay = 7;
        }

        return calenderWeekDay;
    }

    /**
     * 返回下一个周几的时间
     *
     * @param weekday 1-7
     * @return
     */
    public static long getNextDayInWeekTime(long timeStamp, int weekday) {
        if (weekday < 1 || weekday > 7) {
            LogUtil.error("error params, weekday = " + weekday);
            return timeStamp;
        }

        int dayOfWeek = getDayOfWeek(timeStamp);

        int addDay = 0;
        if (dayOfWeek >= weekday) {
            addDay = weekday + 7 - dayOfWeek;
        } else {
            addDay = weekday - dayOfWeek;
        }

        return getTodayStamp(timeStamp) + addDay * MS_IN_A_DAY;
    }

    /**
     * 当日0点时间戳
     *
     * @return
     */
    public static long getTodayStamp(long timeStamp) {
        return getTodayStamp(timeStamp, defaultTimeZone);
    }

    public static long getTodayStamp(long timeStamp, String timeZone) {
        Calendar instance = createNewCalender(timeStamp, timeZone);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTimeInMillis();
    }

    /**
     * 明日0点时间戳
     *
     * @return
     */
    public static long getNextDayStamp(long timeStamp) {
        return getTodayStamp(timeStamp) + MS_IN_A_DAY;
    }

    /**
     * 明日0点时间戳
     *
     * @return
     */
    public static long getNextDayStamp(long timeStamp, String timeZone) {
        return getTodayStamp(timeStamp, timeZone) + MS_IN_A_DAY;
    }


    /**
     * 判断两个时间是不是同一天
     *
     * @param stamp_1
     * @param stamp_2
     * @return
     */
    public static boolean isOneDay(long stamp_1, long stamp_2) {
        return getTodayStamp(stamp_1, defaultTimeZone) == getTodayStamp(stamp_2, defaultTimeZone);
    }

    public static boolean isOneDay(long time_1, long time_2, String timeZone) {
        return getTodayStamp(time_1, timeZone) == getTodayStamp(time_2, timeZone);
    }

    /**
     * 检查传入时间是否超过第二日的刷新时间
     *
     * @param date 传入时间
     * @return 判断结果
     */
    public static boolean ifNeedRefresh(Date date) {
        Calendar nextFreshTime = Calendar.getInstance();
        nextFreshTime.setTime(date);
        nextFreshTime.set(Calendar.DATE, nextFreshTime.get(Calendar.DATE) + 1);
        nextFreshTime.set(Calendar.HOUR_OF_DAY, 20);
        nextFreshTime.set(Calendar.MINUTE, 0);
        nextFreshTime.set(Calendar.SECOND, 0);
        nextFreshTime.set(Calendar.MILLISECOND, 0);
        return GlobalTick.getInstance().getCurrentTime() > nextFreshTime.getTimeInMillis();
    }

    /**
     * 获取配置时区的当前时间
     *
     * @return 时间
     */
    public static Date getTimeWithCfg() {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(GlobalTick.getInstance().getCurrentTime());
        return instance.getTime();
    }

    /**
     * 将时间戳处理成分钟数
     */
    public static int parseStampToMin(long stamp) {
        return (int) Math.min(stamp / MS_IN_A_MIN, Integer.MAX_VALUE);
    }


    /**
     * 获取下次的统一刷新时间
     */
    public static long getNextResetTime(long timeStamp) {
        long todayStamp = getTodayStamp(timeStamp, defaultTimeZone);
        long todayResetTime = todayStamp + 20 * MS_IN_A_HOUR;
        if (timeStamp >= todayResetTime) {
            return todayResetTime + MS_IN_A_DAY;
        }
        return todayResetTime;
    }

    /**
     * 时间格式化转化  String(yyyy-MM-dd hh:mm:ss) -> long
     *
     * @param time
     * @param format
     * @return
     */
    public static long formatTime(String time, String format) {
        Date date = TimeHelper.parseDate(time, format);
        if (date == null) {
            return 0;
        }
        Calendar newCalender = createNewCalender(date, defaultTimeZone);
        return newCalender.getTimeInMillis();
    }

    /**
     * 获得当前周的时间戳
     *
     * @param timeStamp
     * @return
     */
    public static long getToWeekStamp(long timeStamp) {
        int dayOfWeek = getDayOfWeek(timeStamp);
        long nextDayStamp = getNextDayStamp(timeStamp);
        return nextDayStamp - dayOfWeek * MS_IN_A_DAY;
    }

    public static long getNextWeekStamp(long timestamp) {
        return getToWeekStamp(timestamp) + MS_IN_A_DAY * 7;
    }

    public static int getDayOfMonth(long timeStamp) {
        return createNewCalender(timeStamp, null).get(Calendar.DAY_OF_MONTH);
    }

    public static long getCurMonthStamp(long timeStamp) {
        return getNextDayStamp(timeStamp) - getDayOfMonth(timeStamp) * MS_IN_A_DAY;
    }

    public static long getNextMonthStamp(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        int actualMaximum = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return getNextDayStamp(timeStamp) + (actualMaximum - getDayOfMonth(timeStamp)) * MS_IN_A_DAY;
    }

    /**
     * 获取0时区当前时间戳到当日0点的分钟数
     *
     * @param timeStamp
     * @return
     */
    public static int getGMTMin(long timeStamp) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(timeStamp);
        instance.setTimeZone(TimeZone.getTimeZone("GMT"));
        return instance.get(Calendar.HOUR_OF_DAY) * 60 + instance.get(Calendar.MINUTE);
    }

    /**
     * @param stamp
     * @param timeZone null为获取服务器默认时区
     * @return
     */
    public static Calendar createNewCalender(long stamp, String timeZone) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(stamp);
        if (timeZone == null) {
            return instance;
        }
        instance.setTimeZone(TimeZone.getTimeZone(timeZone));
        return instance;
    }

    /**
     * @param date
     * @param timeZone null为使用默认时区默认时区
     * @return
     */
    public static Calendar createNewCalender(Date date, String timeZone) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        if (timeZone == null) {
            return instance;
        }
        instance.setTimeZone(TimeZone.getTimeZone(timeZone));
        return instance;
    }

    /**
     * 获取今年某月份（0时区）的开始时间戳
     *
     * @param month 指定月份
     * @return 开始时间戳
     */
    public static long getBeginTimestampOfMonth(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(defaultTimeZone));
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取今年某月份（0时区）的结束时间戳
     *
     * @param month 指定月份
     * @return 结束时间戳
     */
    public static long getEndTimestampOfMonth(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(defaultTimeZone));
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取时间戳对应的月份（0时区）
     *
     * @param timestamp 时间戳
     * @return 月（0时区）
     */
    public static int getMonth(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.setTimeZone(TimeZone.getTimeZone(defaultTimeZone));
        return calendar.get(Calendar.MONTH) + 1;
    }
}
