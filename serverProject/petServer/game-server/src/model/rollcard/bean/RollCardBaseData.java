package model.rollcard.bean;

import java.util.Map;

/*
*@author Hammer
*2021年11月4日
*/
public abstract class RollCardBaseData {
	public abstract int getType();

	public abstract int getPool();

	public abstract Map<Integer, Integer> getLuckPool();

	public abstract void setLuckPool(Map<Integer, Integer> map);

	public abstract void merge(RollCardBaseData data);

	public abstract void setPool(int pool);

	public abstract void setType(int type);
}
