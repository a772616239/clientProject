package model.rollcard.bean.sub;

import cfg.RollCard;
import cfg.RollCardObject;
import model.rollcard.RollCardManager;
import model.rollcard.bean.RollCardBaseResult;
import model.rollcard.bean.RollCardConfigHelper;
import model.rollcard.bean.abs.AbstractRollCardParam;
import model.rollcard.bean.abs.RollCardRule;
import protocol.Common.Reward;

/*
*@author Hammer
*2021年11月9日
*/
public class RollCardGodTemp extends RollCardRule {
	@Override
	public boolean roll(AbstractRollCardParam params, RollCardBaseResult result) {
		Integer id = params.getParam(0, Integer.class);
		if (id == null) {
			return false;
		}
		Integer grade = params.getParam(1, Integer.class);
		if (grade == null) {
			return false;
		}
		RollCardObject cfg = RollCard.getById(id);
		if (cfg == null) {
			return false;
		}
		Reward reward = null;
		switch (grade) {
		case 1:// 绿
		case 3:// 蓝
		case 5:// 紫
		case 7:// 核心
			RollCardConfigHelper configHelper = RollCardManager.getInstance().getConfigHelper(id, grade);
			if (configHelper == null) {
				return false;
			}
			reward = configHelper.roll();
			break;

		default:
			break;
		}
		if (reward == null) {
			return false;
		}
		// TODO 根据奖励数量判断是发宠物还是碎片
		int petId = 0;// TODO 宠物ID
		int countMax = 100;// TODO 需要碎片
		int count = reward.getCount();
		if (count >= countMax) {
			// TODO 发宠物
			result.addPet(petId);
		} else {
			// TODO 发碎片
			result.addReward(reward);
		}
		return super.roll(params, result);
	}
}
