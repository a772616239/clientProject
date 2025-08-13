package model.rollcard.bean.abs;
/*
*@author Hammer
*2021年11月9日
*/

public abstract class AbstractRollCardParam {
	public abstract <T> T getParam(int index, Class<? extends T> clazz);

	public abstract void addParam(Object... objs);

}
