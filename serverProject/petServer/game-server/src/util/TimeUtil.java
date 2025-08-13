package util;

import cfg.GameConfig;
import cfg.GameConfigObject;
import common.GameConst;
import common.load.ServerConfig;
import org.junit.Test;
import org.springframework.util.StringUtils;
import timetool.TimeHelper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class TimeUtil {

    public static final long MS_IN_A_S = 1000;
    public static final long MS_IN_A_MIN = MS_IN_A_S * 60;
    public static final long MS_IN_A_HOUR = MS_IN_A_MIN * 60;
    public static final long MS_IN_A_DAY = MS_IN_A_HOUR * 24;
    public static final long MS_IN_A_WEEK = MS_IN_A_DAY * 7;

    public static final long MIN_IN_A_HOUR = 60;
    public static final long MIN_IN_A_DAY = MIN_IN_A_HOUR * 24;

    public static final String defaultTimeZone = ServerConfig.getInstance().getTimeZone();

    public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * @return 1 - 7,周一到周日
     */
    public static int getDayOfWeek(long timeStamp) {
        ZonedDateTime newZonedDateTime = createNewZonedDateTimeByZoneName(timeStamp, defaultTimeZone);
        return newZonedDateTime.get(ChronoField.DAY_OF_WEEK);
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
        return getToWeekStamp(timestamp) + MS_IN_A_WEEK;
    }

    /**
     * 获取以统一重置时间为准的周时间
     */
    public static long getToWeekResetStamp(long timeStamp) {
        int dayOfWeek = getDayOfWeek(timeStamp);
        long nextResetStamp = getNextDayResetTime(timeStamp);
        return nextResetStamp - dayOfWeek * MS_IN_A_DAY;
    }

    public static long getNextWeekResetStamp(long timestamp) {
        return getToWeekResetStamp(timestamp) + MS_IN_A_WEEK;
    }

    /**
     * 获取当前时间的日期
     *
     * @param timeStamp
     * @return
     */
    public static int getDayOfMonth(long timeStamp) {
        return createNewZonedDateTime(timeStamp).get(ChronoField.DAY_OF_MONTH);
    }

    /**
     * 获取当前月天数
     *
     * @param timeStamp
     * @return
     */
    public static int getTotalDaysOfMonth(long timeStamp) {
        return createNewZonedDateTime(timeStamp).getMonth().maxLength();

    }

    /**
     * 获取时间戳对应的月份（0时区）
     *
     * @param timestamp 时间戳
     * @return 月（0时区）
     */
    public static int getMonth(long timestamp) {
        return createNewZonedDateTime(timestamp).get(ChronoField.MONTH_OF_YEAR);
    }


    public static long getCurMonthStamp(long timeStamp) {
        ZonedDateTime curMonth = createNewZonedDateTime(timeStamp)
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .with(ChronoField.MILLI_OF_SECOND, 0);
        return curMonth.toInstant().toEpochMilli();
    }

    public static long getCurMonthResetTime(long stamp) {
        return getNextDayResetTime(getCurMonthStamp(stamp));
    }

    public static long getNextMonthStamp(long timeStamp) {
        ZonedDateTime nextMonth = createNewZonedDateTime(timeStamp).plus(1, ChronoUnit.MONTHS);
        return getCurMonthStamp(nextMonth.toInstant().toEpochMilli());
    }

    public static long getNextMonthResetTime(long timeStamp) {
        return getNextDayResetTime(getNextMonthStamp(timeStamp));
    }

    /**
     * 返回下一个周几的时间
     *
     * @param weekday 1-7
     * @return
     */
    public static long getNextDayInWeekTime(long timeStamp, int weekday) {
        if (GameUtil.outOfScope(1, 7, weekday)) {
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
     * 获取一个新的带时区的实例
     *
     * @param stamp
     * @return
     */
    public static ZonedDateTime createNewZonedDateTime(long stamp) {
        return createNewZonedDateTimeByZoneName(stamp, defaultTimeZone);
    }

    public static ZonedDateTime createNewZonedDateTimeByZoneName(long stamp, String zoneName) {
        ZoneId zoneId = zoneName == null ? ZoneId.systemDefault() : ZoneId.of(zoneName);
        return createNewZonedDateTimeByZoneId(stamp, zoneId);
    }

    public static ZonedDateTime createNewZonedDateTimeByZoneId(long stamp, ZoneId zoneId) {
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
        return Instant.ofEpochMilli(stamp).atZone(zoneId);
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
        ZonedDateTime instant = createNewZonedDateTimeByZoneName(timeStamp, timeZone);
        ZonedDateTime newDataTime = instant.withHour(0)
                .withMinute(0)
                .withSecond(0)
                .with(ChronoField.MILLI_OF_SECOND, 0);
        return newDataTime.toInstant().toEpochMilli();
    }

    public static void main(String[] args) {
        System.out.println(getTotalDaysOfMonth(System.currentTimeMillis()));
    }

    /**
     * n天后0点时间戳
     *
     * @return
     */
    public static long getNextDaysStamp(long timeStamp, int days) {
        return getTodayStamp(timeStamp, defaultTimeZone) + MS_IN_A_DAY * days;
    }

    /**
     * 明日0点时间戳
     *
     * @return
     */
    public static long getNextDayStamp(long timeStamp) {
        return getNextDayStamp(timeStamp, defaultTimeZone);
    }

    public static long getNextDayStamp(long timeStamp, String timeZone) {
        return getTodayStamp(timeStamp, timeZone) + MS_IN_A_DAY;
    }

    /**
     * 获取当前时间指定某个月的第一个刷新时间
     *
     * @param currentTime
     * @param month
     * @return
     */
    public static long getMonthFirstResetTime(long currentTime, int month) {
        if (GameUtil.outOfScope(1, 12, month)) {
            LogUtil.warn("util.TimeUtil.getMonthFirstResetTime, error param, month is out of scope");
            return currentTime;
        }
        ZonedDateTime newZonedDateTime = createNewZonedDateTime(currentTime);
        int curMonth = newZonedDateTime.get(ChronoField.MONTH_OF_YEAR);
        long newMonthStamp = newZonedDateTime.plus(curMonth >= month ? 1 : 0, ChronoUnit.YEARS)
                .with(ChronoField.MONTH_OF_YEAR, month)
                .toInstant().toEpochMilli();

        return getCurMonthResetTime(newMonthStamp);
    }

    public static String formatStamp(long stamp) {
        return formatStampByZoneName(stamp, defaultTimeZone);
    }

    public static String formatStampByZoneName(long stamp, String zoneName) {
        return formatStampByZoneId(stamp, ZoneId.of(zoneName));
    }

    public static String formatStampByZoneId(long stamp, ZoneId zoneId) {
        ZonedDateTime byZoneName = createNewZonedDateTimeByZoneId(stamp, zoneId);
        return DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT).format(byZoneName);
    }

    /**
     * 判断两个时间是不是同一天
     *
     * @param stamp_1
     * @param stamp_2
     * @return
     */
    public static boolean isOneDay(long stamp_1, long stamp_2) {
        return isOneDay(stamp_1, stamp_2, defaultTimeZone);
    }

    public static boolean isOneDay(long time_1, long time_2, String timeZone) {
        return getTodayStamp(time_1, timeZone) == getTodayStamp(time_2, timeZone);
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
    public static long getNextDayResetTime(long timeStamp) {
        GameConfigObject gameCfg = GameConfig.getById(GameConst.CONFIG_ID);
        if (gameCfg == null) {
            LogUtil.error("gameCfg is null");
            return getNextDayStamp(timeStamp);
        }

        long todayResetTime = getTodayStamp(timeStamp, defaultTimeZone) + gameCfg.getResettime() * MS_IN_A_HOUR;
        if (timeStamp >= todayResetTime) {
            todayResetTime += MS_IN_A_DAY;
        }
        return todayResetTime;
    }

    /**
     * 获取下次的统一刷新时间
     */
    public static long getNextDaysResetTime(long timeStamp, int days) {
        return getNextDayResetTime(timeStamp) + (days - 1) * MS_IN_A_DAY;
    }


    /**
     * 时间格式化转化  String(yyyy-MM-dd hh:mm:ss) -> long
     *
     * @param time
     * @param format
     * @return
     */
    public static long parseTime(String time, String format) {
        Date date = TimeHelper.parseDate(time, format);
        if (date == null) {
            return 0;
        }
        return createNewZonedDateTimeByZoneName(date.getTime(), defaultTimeZone).toInstant().toEpochMilli();
    }

    public static long parseTimeByDefaultTimeZone(String time, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        ZonedDateTime zonedDateTime = LocalDateTime.parse(time, formatter).atZone(ZoneId.of(defaultTimeZone));
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public static long parseTime(String time) {
        return parseTime(time, DEFAULT_TIME_FORMAT);
    }

    /**
     * @param time
     * @return -1不限时
     */
    public static long parseActivityTime(String time) {
        //没填 空值等情况表示不限时
        if (StringUtils.isEmpty(time) || time.equals("null") || time.equals("0")) {
            return -1;
        }
        return parseTime(time);
    }

    /**
     * 获取0时区当前时间戳到当日0点的分钟数
     *
     * @param timeStamp
     * @return
     */
    public static int getMin(long timeStamp) {
        return (int) ((timeStamp - getTodayStamp(timeStamp)) / MS_IN_A_MIN);
    }


    /**
     * 0 4 8 12 16 20 24
     */
    public final static int testReset = 8;


    private static String formatTimeUnit(long value) {
        if (value < 10) {
            return "0" + value;
        }
        return value + "";
    }

    /**
     * 计算活动结束时间 endDay 为-1 返回-1表不结束
     * @param startTime
     * @param endDay
     * @return
     */
    public static long calculateActivityEndTime(long startTime, int endDay) {
        return endDay == -1 ? endDay : startTime + TimeUtil.MS_IN_A_DAY * endDay;
    }


    @Test
    public void test1() {
        long curTime = Instant.now().toEpochMilli();
        System.out.println("======================today stamp=========================");
        System.out.println(formatStampByZoneId(getTodayStamp(curTime), ZoneId.systemDefault()));

        System.out.println("======================next day reset stamp=========================");
        System.out.println(formatStampByZoneId(getNextDayResetTime(curTime), ZoneId.systemDefault()));

        System.out.println("======================day of week=========================");
        System.out.println(getDayOfWeek(curTime));

        System.out.println("======================month of year=========================");
        System.out.println(getMonth(curTime));

        System.out.println("======================cur month stamp time=========================");
        System.out.println(formatStampByZoneId(getCurMonthStamp(curTime), ZoneId.systemDefault()));

        System.out.println("======================cur month reset time=========================");
        System.out.println(formatStampByZoneId(getCurMonthResetTime(curTime), ZoneId.systemDefault()));

        System.out.println("======================next month stamp=========================");
        System.out.println(formatStampByZoneId(getNextMonthStamp(curTime), ZoneId.systemDefault()));

        System.out.println("======================next month reset time=========================");
        System.out.println(formatStampByZoneId(getNextMonthResetTime(curTime), ZoneId.systemDefault()));

        System.out.println("====================== month first reset time=========================");
        System.out.println("month 3:" + formatStampByZoneId(getMonthFirstResetTime(curTime, 3), ZoneId.systemDefault()));
        System.out.println("month 4:" + formatStampByZoneId(getMonthFirstResetTime(curTime, 4), ZoneId.systemDefault()));
        System.out.println("month 5:" + formatStampByZoneId(getMonthFirstResetTime(curTime, 5), ZoneId.systemDefault()));

        System.out.println("====================== is one day =========================");
        System.out.println("GMT - 8 :" + isOneDay(curTime, curTime - TimeUtil.MS_IN_A_HOUR * 8));
        System.out.println("GMT + 8 :" + isOneDay(curTime, curTime + TimeUtil.MS_IN_A_HOUR * 8));
        System.out.println("local + 8:" + isOneDay(curTime, curTime + TimeUtil.MS_IN_A_HOUR * 8, ZoneId.systemDefault().toString()));
        System.out.println("local - 8 :" + isOneDay(curTime, curTime - TimeUtil.MS_IN_A_HOUR * 8, ZoneId.systemDefault().toString()));

        System.out.println("====================== getToWeekResetStamp =========================");
        System.out.println("toWeek timeStamp:" + formatStampByZoneName(getToWeekStamp(curTime), ZoneId.systemDefault().toString()));
        System.out.println("toWeek reset:" + formatStampByZoneName(getToWeekResetStamp(curTime), ZoneId.systemDefault().toString()));
        System.out.println("nextWeek timeStamp:" + formatStampByZoneName(getNextWeekStamp(curTime), ZoneId.systemDefault().toString()));
        System.out.println("nextWeek reset:" + formatStampByZoneName(getNextWeekResetStamp(curTime), ZoneId.systemDefault().toString()));

        System.out.println("====================== getNextDayInWeekTime =========================");
        System.out.println(formatStampByZoneName(getNextDayInWeekTime(curTime, 3), ZoneId.systemDefault().toString()));

    }
}
