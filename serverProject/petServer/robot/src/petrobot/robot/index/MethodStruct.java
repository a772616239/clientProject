package petrobot.robot.index;

import java.lang.reflect.Method;

public class MethodStruct {
	private Method method;
	
	private Object manger;
	
	public MethodStruct(Object manager,Method method) {
		this.manger = manager;
		this.method = method;
	}

	public Method getMethod() {
		return method;
	}

	public Object getManger() {
		return manger;
	}

}
