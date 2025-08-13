package model.activity.sub;

import model.activity.ActivityData;

/**
 * 通用活动
 */
public class ActivityDataUsual extends ActivityData {

	public ActivityDataUsual(int timeRuleId) {
		super(timeRuleId);
	}

	@Override
	public boolean onStart() {
		return true;
	}
	
	@Override
	public boolean onEnd() {
		super.onEnd();
		return true;
	}
	
}
