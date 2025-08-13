package model.activity.sub;

import cfg.TimeRuleCfg;
import cfg.TimeRuleCfgObject;
import model.activity.ActivityData;
import model.activity.TimeRuleManager;
import model.activity.timerule.TimeRuleType;
import model.crossarena.CrossArenaTopManager;

/**
 * 通用活动
 */
public class ActivityDataCrossArenaTop extends ActivityData {

	public ActivityDataCrossArenaTop(int timeRuleId) {
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
		if (def.getType() == TimeRuleType.TYPE_TOP) {
			CrossArenaTopManager.getInstance().openAndClose(true);
		} else if (def.getType() == TimeRuleType.TYPE_TOP_R) {
			CrossArenaTopManager.getInstance().openInitJion(getCloseTime(), false);
		} else if (def.getType() == TimeRuleType.TYPE_TOP_G) {
			CrossArenaTopManager.getInstance().openGroup(getCloseTime(), false);
		} else if (def.getType() == TimeRuleType.TYPE_TOP_B) {
			CrossArenaTopManager.getInstance().openBattle(getCloseTime(), false);
		} else if (def.getType() == TimeRuleType.TYPE_TOP_V) {
			CrossArenaTopManager.getInstance().openView(getCloseTime(), false);
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
		if (def.getType() == TimeRuleType.TYPE_TOP) {
			CrossArenaTopManager.getInstance().openAndClose( false);
		} else if (def.getType() == TimeRuleType.TYPE_TOP_B) {
			CrossArenaTopManager.getInstance().openSettle(getCloseTime(), false);
		}else if (def.getType() == TimeRuleType.TYPE_TOP_V) {
			CrossArenaTopManager.getInstance().openAndClose(false);
		}
		return true;
	}
	
}
