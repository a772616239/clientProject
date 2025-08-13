package model.rollcard.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/*
*@author Hammer
*2021年11月5日
*/
public class RollPoolTemp extends RollPoolBaseData {

	private Map<Integer, Integer> weightMap = new HashMap<>();

	private int totalWeight;

	private Random random = new Random();

	private int pool;

	@Override
	public int getTotalWeight() {
		return this.totalWeight;
	}

	@Override
	public int getWeight(int id) {
		return this.weightMap.getOrDefault(id, 0);
	}

	@Override
	public int getPool() {
		return this.pool;
	}

	@Override
	public void setPool(int pool) {
		this.pool = pool;
	}

	@Override
	public int roll() {
		int luck = random.nextInt(totalWeight) + 1;
		for (Entry<Integer, Integer> ent : weightMap.entrySet()) {
			if (ent.getValue() >= luck) {
				return ent.getKey();
			}
			luck -= ent.getValue();
		}
		return 0;
	}

	@Override
	public void setWeightMap(Map<Integer, Integer> map) {
		int totalWeight = 0;
		for (Entry<Integer, Integer> ent : map.entrySet()) {
			totalWeight += ent.getValue();
		}
		this.weightMap = map;
		this.totalWeight = totalWeight;
	}

}
