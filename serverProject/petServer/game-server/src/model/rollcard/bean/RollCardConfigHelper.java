package model.rollcard.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import model.reward.RewardUtil;
import protocol.Common.Reward;

/*
*@author Hammer
*2021年11月8日
*/
public class RollCardConfigHelper {
	private Map<Integer, RollCardReward> rewardMap = new HashMap<>();

	private int totalWeight = 0;

	private int id = 1;
	public void addHigh(int[] rewardConfig) {
		if (rewardConfig.length < 4) {
			return;
		}
		Reward reward = RewardUtil.parseReward(new int[] { rewardConfig[0], rewardConfig[1], rewardConfig[2] });
		if (reward == null) {
			return;
		}
		int weight = rewardConfig[3];
		totalWeight += weight;

		RollCardReward rewardData = new RollCardReward();
		rewardData.setReward(reward);
		rewardData.setWeight(weight);

		rewardMap.put(id++, rewardData);
	}

	public void addCommon(int[] rewardConfig) {
		if (rewardConfig.length < 3) {
			return;
		}
		Reward reward = RewardUtil.parseReward(rewardConfig);
		if (reward == null) {
			return;
		}
		int weight = 1;
		totalWeight += weight;

		RollCardReward rewardData = new RollCardReward();
		rewardData.setReward(reward);
		rewardData.setWeight(weight);

		rewardMap.put(id++, rewardData);
	}

	public Reward roll() {
		int luck = new Random().nextInt(totalWeight) + 1;
		for (Entry<Integer, RollCardReward> ent : rewardMap.entrySet()) {
			if (ent.getValue().getWeight() >= luck) {
				return ent.getValue().getReward();
			}
			luck -= ent.getValue().getWeight();
		}
		return null;
	}

	private class RollCardReward {

		private int weight;

		private Reward reward;

		public int getWeight() {
			return weight;
		}

		public void setWeight(int weight) {
			this.weight = weight;
		}

		public Reward getReward() {
			return reward;
		}

		public void setReward(Reward reward) {
			this.reward = reward;
		}

	}
}
