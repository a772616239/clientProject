package model.rollcard.bean.abs;

import model.rollcard.bean.RollCardBaseResult;

/*
*@author Hammer
*2021年11月9日
*/
public class RollCardRule extends AbstractRollCardRule {

	@Override
	public boolean roll(AbstractRollCardParam params, RollCardBaseResult result) {
		return true;
	}

}
