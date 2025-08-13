package common;

import util.LogUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {

    public static final int MS_IN_A_DAY = 24 * 60 * 60 * 1000;
    public static final int MS_IN_A_HOUR = 60 * 60 * 1000;
    public static final int MS_IN_A_MIN = 60 * 1000;
    public static final int MS_IN_A_S = 1000;

    public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * @return  1 - 7,周一到周日
     */
    public static int getDayOfWeek(long timeStamp){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        calendar.setTime(new Date(timeStamp));
        int calenderWeekDay = calendar.get(Calendar.DAY_OF_WEEK);

        calenderWeekDay -= 1;
        if(calenderWeekDay == -1){
            calenderWeekDay = 7;
        }

        return calenderWeekDay;
    }
    /**
     *返回下一个周几的时间
     * @param weekday  1-7
     * @return
     */
    public static long getNextDayInWeekTime(long timeStamp, int weekday){
        if(weekday < 1 || weekday > 7){
            LogUtil.error("error params, weekday = " + weekday);
            return 0;
        }

        int dayOfWeek = getDayOfWeek(timeStamp);

        int addDay = 0;
        if(dayOfWeek >= weekday){
            addDay = weekday  + 7 - dayOfWeek;
        }else{
            addDay = weekday - dayOfWeek;
        }

        return getTodayStamp(timeStamp) + addDay * MS_IN_A_DAY;
    }

    /**
     * 当日0点时间戳  时区GMT+8
     * @return
     */
    public static long getTodayStamp(long timeStamp){
        return timeStamp - timeStamp % MS_IN_A_DAY - MS_IN_A_HOUR * 8;
    }

    /**
     * 明日0点时间戳
     * @return
     */
    public static long getNextDayStamp(long timeStamp){
        return getTodayStamp(timeStamp) + MS_IN_A_DAY;
    }

    /**
     *计算几小时后的时间
     * @param startTime
     * @param addTime    hh:mm
     * @return
     */
    public static long sumTheTime(long startTime, String addTime){
        if(addTime == null){
            return 0;
        }
        String[] split = addTime.split(":");
        if(split.length == 2){
            return startTime + Integer.valueOf(split[0]) * MS_IN_A_HOUR + Integer.valueOf(split[1]) * MS_IN_A_MIN;
        }
        return 0;
    }


    /**
     *
     * @param startTime
     * @param addTime     min
     * @return
     */
    public static long sumTheTime(long startTime, int addTime){
        return startTime + addTime * MS_IN_A_MIN;
    }
}
