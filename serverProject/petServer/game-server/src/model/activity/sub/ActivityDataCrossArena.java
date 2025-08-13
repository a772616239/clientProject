package model.activity.sub;

import cfg.TimeRuleCfg;
import cfg.TimeRuleCfgObject;
import model.activity.ActivityData;
import model.activity.TimeRuleManager;
import model.activity.timerule.TimeRuleType;
import model.crossarena.CrossArenaManager;

/**
 * 通用活动
 */
public class ActivityDataCrossArena extends ActivityData {

	public ActivityDataCrossArena(int timeRuleId) {
		super(timeRuleId);
	}

	@Override
	public boolean onStart() {
		TimeRuleCfgObject def = TimeRuleCfg.getById(timeRuleId);
		if (null == def) {
			return false;
		}
		if (def.getFather_id() > 0) {
			if (!TimeRuleManager.getInstance().isOpenTime(def.getFather_id())) {
				return false;
			}
		}
		if (def.getType() == TimeRuleType.TYPE_LT) {
			CrossArenaManager.getInstance().openLT(getEndTime());
		} else if (def.getType() == TimeRuleType.TYPE_LT_10) {
			CrossArenaManager.getInstance().openLT10(getEndTime(), false);
		}
		return true;
	}
	
	@Override
	public boolean onEnd() {
		super.onEnd();
		TimeRuleCfgObject def = TimeRuleCfg.getById(timeRuleId);
		if (null == def) {
			return false;
		}
		switch (def.getType()) {
			case TimeRuleType.TYPE_LT:
				CrossArenaManager.getInstance().closeLT();
				break;
			case TimeRuleType.TYPE_LT_10:
				CrossArenaManager.getInstance().closeLT10();
				break;
		}
		return true;
	}
	
}
