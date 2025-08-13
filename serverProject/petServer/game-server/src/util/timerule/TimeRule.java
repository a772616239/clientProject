package util.timerule;

import util.StrObjUtil;
import util.Symbol;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeRule {
	
	private int idx = 0;
	public String rule;
	
	private List<Integer> yearsList = new LinkedList<Integer>();
	private List<Integer> monthsList = new LinkedList<Integer>();
	private boolean isWeek = false;//false是天true是周
	private List<Integer> daysList = new LinkedList<Integer>();
	
	private boolean isAllHour = false;
	private List<TimeRuleRange> times = new LinkedList<TimeRuleRange>();
	
	/**
	 * 活动循环类型0普通1天2周3月4年
	 */
	private int loopType = 1;
	
	public TimeRule(int idx, String rule) {// [*][*][w4][20:30-21:00]
		this.idx = idx;
		rule = rule.substring(1, rule.length() - 1);// *][*][w4][20:30-21:00
		String[] t = rule.split("\\]\\[");// *,*,w4,20:30-21:00
		if (t.length != 4) {
			throw new IllegalStateException();
		}
		// 处理年
		initYear(t[0]);
		// 处理月
		initMonths(t[1]);
		// 处理周
		initWeek(t[2]);
		// 处理时间
		initTime(t[3].split(","));

		if (yearsList.isEmpty() || monthsList.isEmpty() || daysList.isEmpty() || times.isEmpty()) {
			throw new IllegalStateException();
		}
		// 初始化活动时间规则循环模式
		loopTypeInit();
	}
	
	/**
	 * 处理年时间格式
	 * @param str
	 */
	private void initYear(String str) {
		List<Integer> li = new ArrayList<Integer>();
		// 处理年
		if (Symbol.XINGHAO.equals(str)) {
			li.add(0);
		} else if (str.indexOf("-") != -1) {
			String[] seg = str.split("-");
			int min = Integer.parseInt(seg[0]);
			int max = Integer.parseInt(seg[1]);
			for (int i=min;i<=max;i++) {
				li.add(i);
			}
		} else {
			List<Integer> temp = StrObjUtil.toListInt(str);
			if (temp.size() > 0) {
				li.addAll(temp);
			}
		}
		yearsList.clear();
		yearsList.addAll(li);
		// 开始排序
		Collections.sort(yearsList, new Comparator<Integer>() {
			@Override
			public int compare(Integer one, Integer two) {
				if (one > two) {
					return 1;
				} else if (one < two) {
					return -1;
				} else {
					return 0;
				}
			}
		});
	}
	
	/**
	 * 处理月时间格式 
	 * @param str
	 */
	private void initMonths(String str) {
		List<Integer> li = new ArrayList<Integer>();
		if (Symbol.XINGHAO.equals(str)) {
			li.add(0);
		} else if (str.indexOf("-") != -1) {
			String[] seg = str.split("-");
			int min = Integer.parseInt(seg[0]);
			int max = Integer.parseInt(seg[1]);
			min = min < 1 ? 1 : min;
			max = max > 12 ? 12 : max;
			for (int i=min;i<=max;i++) {
				li.add(i);
			}
		} else {
			List<Integer> temp = StrObjUtil.toListInt(str);
			if (temp.size() > 0) {
				li.addAll(temp);
			}
		}
		monthsList.clear();
		monthsList.addAll(li);
		if (monthsList.size() >= 12) {
			monthsList.clear();
			monthsList.add(0);
		}
		// 开始排序
		Collections.sort(monthsList, new Comparator<Integer>() {
			@Override
			public int compare(Integer one, Integer two) {
				if (one > two) {
					return 1;
				} else if (one < two) {
					return -1;
				} else {
					return 0;
				}
			}
		});
	}
	
	/**
	 * 处理天时间格式 
	 * @param str
	 */
	private void initWeek(String str) {
		List<Integer> li = new ArrayList<Integer>();
		if (str.startsWith("w") || str.startsWith("W")) {
			isWeek = true;
			str = str.substring(1, str.length());
		} else {
			isWeek = false;
		}
		if (Symbol.XINGHAO.equals(str)) {
			isWeek = false;
			li.add(0);
		} else if (str.indexOf("-") != -1) {
			String[] seg = str.split("-");
			int min = Integer.parseInt(seg[0]);
			int max = Integer.parseInt(seg[1]);
			if (isWeek) {
				min = min < 1 ? 1 : min;
				max = max > 7 ? 7 : max;
			} else {
				min = min < 1 ? 1 : min;
				max = max > 31 ? 31 : max;
			}
			for (int i=min;i<=max;i++) {
				li.add(i);
			}
		} else {
			List<Integer> temp = StrObjUtil.toListInt(str);
			if (temp.size() > 0) {
				li.addAll(temp);
			}
		}
		daysList.clear();
		daysList.addAll(li);
		if (isWeek) {
			if (daysList.size() >= 7) {
				daysList.clear();
				daysList.add(0);
			}
		} else {
			if (daysList.size() >= 31) {
				daysList.clear();
				daysList.add(0);
			}
		}
		// 开始排序
		Collections.sort(daysList, new Comparator<Integer>() {
			@Override
			public int compare(Integer one, Integer two) {
				if (one > two) {
					return 1;
				} else if (one < two) {
					return -1;
				} else {
					return 0;
				}
			}
		});
	}
	
	/**
	 * 处理时间
	 * @param hours
	 */
	private void initTime(String[] hours) {
		for (String str : hours) {
			if (str.equals("*")) {
				// 处理为全天24小时
				TimeRuleRange unit = new TimeRuleRange();
				unit.setStartHour(0);
				unit.setStartMinute(0);
				unit.setStartSecend(0);
				unit.setStopHour(23);
				unit.setStopMinute(59);
				unit.setStopSecend(59);
				times.add(unit);
				isAllHour = true;
			} else if (str.indexOf("-") != -1) {
				try {
					String[] seg = str.split("-");
					String[] start = seg[0].split(":");
					String[] end = seg[1].split(":");
					TimeRuleRange unit = new TimeRuleRange();
					unit.setStartHour(Integer.parseInt(start[0]));
					unit.setStartMinute(Integer.parseInt(start[1]));
					unit.setStartSecend(Integer.parseInt(start[2]));
					unit.setStopHour(Integer.parseInt(end[0]));
					unit.setStopMinute(Integer.parseInt(end[1]));
					unit.setStopSecend(Integer.parseInt(end[2]));
					times.add(unit);
				} catch (Exception e) {
					throw new IllegalStateException();
				}
			}
		}
	}
	
	/**
	 * 检测是否有指定格式
	 * @param str
	 * @return
	 */
	public static String checkMinMax(String str) {
		String pattern = "[0-9]+\\-[0-9]+";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.group();
		} else {
			return "";
		}
	}
	
	public void loopTypeInit() {
		loopType = 0;
		if (isWeek) {
			loopType = 2;
		} else {
			if (yearsList.contains(0) && monthsList.contains(0) && daysList.contains(0)) {
				loopType = 1;
			}
		}
	}
	
	/**
	 * 是否在活动时间内
	 * @param time
	 * @return
	 */
	public boolean isValidateTime(long time) {
		if (isValidateDate(time)) {
			Calendar ca = new GregorianCalendar();
			ca.setTimeInMillis(time);
			int hour = ca.get(Calendar.HOUR_OF_DAY);
			int minute = ca.get(Calendar.MINUTE);
			int secend = ca.get(Calendar.SECOND);
			int millsecend = ca.get(Calendar.MILLISECOND);
			int hourtime = hour * 60 * 60 * 1000 + minute * 60 * 1000 + secend * 1000 + millsecend;
			if (isinhours(hourtime)) {
				return true;
			}
			return false;
		} else {
			return false;
		}
	}

	/**
	 * 指定参数是否在指定数组内
	 * 
	 * @param list
	 * @param value
	 * @return
	 */
	private boolean isinarray(List<Integer> list, int value) {
		if (list.contains(0)) {
			return true;
		}
		return list.contains(value);
	}

	// 当前日期内是否执行
	public boolean isValidateDate(long time) {
		Calendar ca = new GregorianCalendar();
		ca.setTimeInMillis(time);
		int year = ca.get(Calendar.YEAR);
		if (!isinarray(yearsList, year)) {
			return false;
		}
		int month = ca.get(Calendar.MONTH) + 1;
		if (!isinarray(monthsList, month)) {
			return false;
		}
		int day = ca.get(Calendar.DAY_OF_MONTH);
		if (isWeek) {
			day = ca.get(Calendar.DAY_OF_WEEK) - 1;
			if (day == 0) {
				day = 7;
			}
		}
		if (!isinarray(daysList, day)) {
			return false;
		}
		return true;
	}

	private boolean isinhours(int checkhourtime) {
		for (TimeRuleRange item : times) {
			if (item.isinhours(checkhourtime)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {多段时间配置，且连续配置得活动才调用该方法}获取活动结束时间
	 * @return
	 */
	public long getEndTime() {
		Calendar stop = Calendar.getInstance();
		for (TimeRuleRange trr : times) {
			stop = trr.getStopTimestamp();
		}
		if (isWeek) {
			//获取到当前天数
			int weekSrc = stop.get(Calendar.DAY_OF_WEEK) - 1;
			weekSrc = weekSrc == 0 ? 7 : weekSrc;
			if (!daysList.contains(0)) {
				if (daysList.size() <= 1) {
					int weekMax = daysList.get(daysList.size() - 1);
					if (weekMax >= weekSrc) {
						stop.add(Calendar.DAY_OF_WEEK, weekMax - weekSrc);
					}
				} else {
					boolean isLX = true;
					int min = daysList.get(0);
					for (int i=1; i< daysList.size(); i++) {
						int next = min + i;
						if (!daysList.contains(next)) {
							isLX = false;
						}
					}
					if (isLX) {
						int weekMax = daysList.get(daysList.size() - 1);
						if (weekMax >= weekSrc) {
							stop.add(Calendar.DAY_OF_WEEK, weekMax - weekSrc);
						}
					} else {
						for (int xh : daysList) {
							if (xh >= weekSrc) {
								stop.add(Calendar.DAY_OF_WEEK, xh - weekSrc);
								break;
							}
						}
					}
				}
			}
		} else {
			int day = stop.get(Calendar.DAY_OF_MONTH);
			if (!daysList.contains(0)) {
				int daysMax = daysList.get(daysList.size() - 1);
				if (daysMax > day) {
					day = daysMax;
					stop.set(Calendar.DAY_OF_MONTH, day);
				}
			}
		}
		return stop.getTimeInMillis();
	}
	
	public boolean isAllHour() {
		return isAllHour;
	}

	/**
	 * 当天是否会发生（不发生不展示在活动页面）
	 * 
	 * @param time
	 * @return
	 */
	public boolean isTodayOpen(long time) {
		return isValidateDate(time);
	}

	/**
	 * 活动时间是否是天循环
	 * @return
	 */
	public boolean isLoopWeek() {
		return loopType == 2;
	}
	public boolean isLoopDay() {
		return loopType == 1;
	}
	
	/**
	 * 是否是连续时间规则
	 * @return
	 */
	public boolean isContinuity() {
		return idx > 0;
	}
	
	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public boolean isFirst() {
		return idx == 1;
	}
	
	public boolean isLast() {
		return idx == 99;
	}
	
	public List<TimeRuleRange> getTimes() {
		return times;
	}

}
