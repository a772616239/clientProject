package model.rollcard.bean;

import java.util.HashMap;
import java.util.Map;

import model.rollcard.bean.RollCardBaseData;

/*
*@author Hammer
*2021年11月4日
*/
public class RollCardTemp extends RollCardBaseData {

	private int type = 0;

	private int pool = 0;

	private Map<Integer, Integer> luckPool = new HashMap<>();

	@Override
	public int getType() {
		return type;
	}

	@Override
	public int getPool() {
		return pool;
	}

	@Override
	public Map<Integer, Integer> getLuckPool() {
		return this.luckPool;
	}

	@Override
	public void setLuckPool(Map<Integer, Integer> map) {
		this.luckPool = map;
	}

	@Override
	public void merge(RollCardBaseData data) {

	}

	@Override
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public void setPool(int pool) {
		this.pool = pool;
	}

}
