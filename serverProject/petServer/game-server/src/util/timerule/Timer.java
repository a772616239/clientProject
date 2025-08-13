package util.timerule;

import util.LogUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 */
public class Timer {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static int getHour(long oldTime, long newTime) {
		long time = (newTime - oldTime) / 1000;
		return (int) (time / 3600);
	}

	/**
	 * 得到1970-01-01 00:00:00
	 * 
	 * @return
	 */
	public static Date getZeroDate() {
		return new Date(0);
	}

	/**
	 * 系统当前时间的修正值，用于动态修改时间方便测试
	 */
	private static long CURRENT_TIME_REVISE = 0;

	/**
	 * 修改修正值,不要随意使用【仅限调试】
	 * 
	 * @param revise
	 */
	public static void setTimeRevise(long revise) {
		CURRENT_TIME_REVISE = revise;
	}

	/**
	 * 修改修正值,不要随意使用【仅限调试】
	 * 
	 * @param revise
	 */
	public static void setTimeRevise(Date revise) {
		long newRevise = revise.getTime() - System.currentTimeMillis();
		if (CURRENT_TIME_REVISE > newRevise) {
			return;
		}
		CURRENT_TIME_REVISE = newRevise;
	}

	/**
	 * 获取系统当前时间毫秒数
	 * 
	 * @return
	 */
	public static long getNowTime() {
		return System.currentTimeMillis() + CURRENT_TIME_REVISE;
	}

	/**
	 * 获取系统当前时间
	 * 
	 * @return
	 */
	public static Date getNowDate() {
		return new Date(getNowTime());
	}
	
	/**
	 * 获取当天开始时间的毫秒数(00:00:00:000)
	 * 
	 * @return
	 */
	public static long get0Time() {
		Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date.getTimeInMillis();
	}

	/***********************************************************/

	public static String getStringDate(Date date) {

		return sdf.format(date);
	}

	public static String getStringDate3(Date date) {

		return sdf3.format(date);
	}

	public static String getNowStringDate() {
		return sdf.format(new Date());
	}

	public static Date getDateByString(String date) {
		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			LogUtil.printStackTrace(e);
			return null;
		}
	}

	public static Date getDateByString3(String date) {
		try {
			return sdf3.parse(date);
		} catch (ParseException e) {
			LogUtil.printStackTrace(e);
			return null;
		}
	}

	/**
	 * 得到几天前的时间
	 * 
	 * @param d
	 * @param day
	 * @return
	 */
	public static Date getDateBefore(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
		return now.getTime();
	}

	/**
	 * 得到几天后的时间
	 * 
	 * @param d
	 * @param day
	 * @return
	 */
	public static Date getDateAfter(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
		return now.getTime();
	}

	public static long getMinute(long oldTime, long newTime) {// 将毫秒数换算成x天x时x分x秒x毫秒
		long l = newTime - oldTime;
		long day = l / (24 * 60 * 60 * 1000);
		long hour = (l / (60 * 60 * 1000) - day * 24);
		long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);

		return min;
	}

	/**
	 * @param strDate
	 *            传入日期返回年龄（yyyy-MM-dd）
	 * 
	 */

	public static String getYear(String strDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		if (strDate.length() > 0) {
			try {
				Date d = bartDateFormat.parse(strDate);
				cal.setTime(d);
			} catch (Exception ec) {
				LogUtil.printStackTrace(ec);
			}
			int year2 = cal.get(Calendar.YEAR);
			int month2 = cal.get(Calendar.MONTH);
			int day2 = cal.get(Calendar.DAY_OF_MONTH);
			int y_c = year - year2;
			int m_c = month - month2;
			int d_c = day - day2;

			if (d_c < 0) {
				m_c -= 1;
			}
			if (m_c < 0) {
				y_c -= 1;
			}

			return "" + y_c;
		} else {
			return "";
		}
	}

	/**
	 * //获取当天时间
	 * 
	 * @param dateformat
	 * @return
	 */
	public static String getNowTime(String dateformat) {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);// 可以方便地修改日期格式
		String hehe = dateFormat.format(now);
		// logger.info(hehe);
		return hehe;
	}

	/**
	 * 两个日期比较
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int getDateBiJiao(String date1, String date2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			Date dt1 = df.parse(date1);
			Date dt2 = df.parse(date2);
			if (dt1.getTime() > dt2.getTime()) {
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			LogUtil.printStackTrace(exception);
		}
		return 0;
	}

	/**
	 * 得到二个日期间的间隔天数
	 */
	public static String getTwoDay(String sj1, String sj2) {
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
		long day = 0;
		try {
			Date date = myFormatter.parse(sj1);
			Date mydate = myFormatter.parse(sj2);
			day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		} catch (Exception e) {
			return "";
		}
		return day + "";
	}

	public static String timeJiSuan(String ksdate, String jsdate) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = df.parse(jsdate);
		Date date = df.parse(ksdate);
		long l = now.getTime() - date.getTime();
		long day = l / (24 * 60 * 60 * 1000);
		long hour = (l / (60 * 60 * 1000) - day * 24);
		long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
		long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		// logger.info(""+day+"天"+hour+"小时"+min+"分"+s+"秒");
		// "上次操作公用"+day+"天"+hour+"小时"+min+"分"+s+"秒"
		return "上次操作公用:" + hour + "小时" + min + "分" + s + "秒";
	}

	public static String timeJiSuanShowFenMiao(String ksdate, String jsdate) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = df.parse(jsdate);
		Date date = df.parse(ksdate);
		long l = now.getTime() - date.getTime();
		long day = l / (24 * 60 * 60 * 1000);
		long hour = (l / (60 * 60 * 1000) - day * 24);
		long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
		long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		// logger.info(""+day+"天"+hour+"小时"+min+"分"+s+"秒");
		// "上次操作公用"+day+"天"+hour+"小时"+min+"分"+s+"秒"
		return "公用:" + min + "分" + s + "秒";
	}
}
