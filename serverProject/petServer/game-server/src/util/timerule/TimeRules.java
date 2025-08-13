package util.timerule;

/**
 * 时间表达式
 * 
 */
public class TimeRules {

	/**
	 * 所有活动开启时间格式
	 */
	private TimeRule[] rules;

	// [*][*][*][1:40-2:50]
	// new TimeRule("[*,yyyy,yyyy-yyyy][*,mm,mm-mm][*,dd,dd-dd][*,h1-h2]");
	// 时间表达式格式 [年][月][日][时间段];[年][月][日][时间段];....
	// 用";"隔开并列的表达式, 每个"[]"里都可以用"*"表示不对该日期段进行限制,用","表示并列,用"-"表示从哪儿到哪儿
	// 例[*][5,6,8-9][1,3,5,7][1:10-2:30,3:00-5:00]
	// 表示5月6月,8月至9月,每月的1号3号5号,7号,在这些限定日期内的每日的1:00-2:30,3:00-5:00
	// 例[*][*][1,3,5,7][*];[*][*][2,4,6,8][10-20:30]表示
	static public String getHelp() {
		String t = "时间表达式格式 为  [年][月][日][时间段];[年][月][日][时间段];....\r\n";
		t = t + "用\";\"隔开并列的表达式, 每个\"[]\"里都可以用\"*\"表示不对该日期段进行限制,用\",\"表示并列,用\"-\"表示从哪儿到哪儿\r\n";
		t = t + "例 [*][5,6,8-11][1,3,5,7][01:00-02:30,03:00-5:00]\r\n";
		t = t + "表示每5月,6月,8月至11月,每月的1号3号5号,7号,在这些限定日期内的每日的1:00-2:30,3:00-5:00\r\n";
		t = t + "例[*][*][1,3,5,7][*];[*][*][2,4,6,8][10-20:30]\r\n";
		t = t + "表示1号3号5号7号的任意时间和2号4号6号8号的10:00-20:30\r\n";
		return t;

	}

	public TimeRules(String timeRule) {
		timeRule = timeRule.trim();
		if (timeRule.indexOf("+") != -1) {
			String[] exarray = timeRule.split("\\+");
			TimeRule[] ruleArray = new TimeRule[exarray.length];
			for (int i = 0; i < exarray.length; i++) {
				ruleArray[i] = new TimeRule(i+1, exarray[i]);
				if (i+1 == exarray.length) {
					ruleArray[i].setIdx(99);
				}
			}
			rules = ruleArray;
		} else {
			timeRule = timeRule.trim();
			if (timeRule.endsWith(";")) {
				timeRule = timeRule.substring(0, timeRule.length() - 1);
			}
			String[] exarray = timeRule.split(";");
			TimeRule[] ruleArray = new TimeRule[exarray.length];
			for (int i = 0; i < exarray.length; i++) {
				ruleArray[i] = new TimeRule(0, exarray[i]);
			}
			rules = ruleArray;
		}
	}

	/**
	 * 当前时间是否在活动时间内
	 * 
	 * @param time
	 * @return
	 */
	public boolean isRuleTime(long time) {
		for (TimeRule item : rules) {
			if (item.isAllHour() || item.isValidateTime(time)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取时分时间间隔(秒)
	 * 
	 * @return
	 */
	public int getTimeRuleRange() {
		int seconds = 0;
		if (null == rules) {
			return seconds;
		}
		for (TimeRule expression : rules) {
			if (expression.isContinuity()) {
				for (TimeRuleRange unit : expression.getTimes()) {
					seconds += unit.getContinuedSeconds();
				}
			} else {
				for (TimeRuleRange unit : expression.getTimes()) {
					if (unit.isTime()) {
						seconds = unit.getContinuedSeconds();
						break;
					}
				}
			}
		}
		return seconds;
	}

	public TimeRule[] getRules() {
		return rules;
	}

}
