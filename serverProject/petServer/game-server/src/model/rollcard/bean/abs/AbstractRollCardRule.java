package model.rollcard.bean.abs;

import model.rollcard.bean.RollCardBaseResult;

/*
*@author Hammer
*2021年11月8日
*/
public abstract class AbstractRollCardRule {

	public abstract boolean roll(AbstractRollCardParam params, RollCardBaseResult result);
}
