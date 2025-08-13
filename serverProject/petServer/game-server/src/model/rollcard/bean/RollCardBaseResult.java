package model.rollcard.bean;

import java.util.ArrayList;
import java.util.List;

import protocol.Common.Reward;

/*
*@author Hammer
*2021年11月8日
*/
public class RollCardBaseResult {

	
	private List<Integer> petIds = new ArrayList<>();

	private List<Reward> rewards = new ArrayList<>();

	public void addPet(int petId) {
		petIds.add(petId);
	}

	public void addReward(Reward reward) {
		rewards.add(reward);
	}

	public List<Integer> getPetIds() {
		return petIds;
	}

	public List<Reward> getRewards() {
		return rewards;
	}
	
	
}
