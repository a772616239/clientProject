package model.rollcard.bean;

import java.util.Map;

/*
*@author Hammer
*2021年11月5日
*/
public abstract class RollPoolBaseData {

	public abstract int getTotalWeight();

	public abstract int getWeight(int id);

	public abstract int getPool();

	public abstract int roll();

	public abstract void setWeightMap(Map<Integer, Integer> map);

	public abstract void setPool(int pool);
}
