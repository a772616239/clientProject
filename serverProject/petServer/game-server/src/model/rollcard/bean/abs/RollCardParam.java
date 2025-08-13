package model.rollcard.bean.abs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
*@author Hammer
*2021年11月9日
*/
public abstract class RollCardParam extends AbstractRollCardParam {

	private List<Object> params = new ArrayList<>();
	@Override
	public void addParam(Object... objs) {
		this.params.addAll(Arrays.asList(objs));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getParam(int index, Class<? extends T> clazz) {
		if (index < 0 || index >= params.size())
			return null;
		Object object = params.get(index);
		if (!clazz.isInstance(object))
			return null;
		return (T) object;
	}
}
