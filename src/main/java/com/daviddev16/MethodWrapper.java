package com.daviddev16;

import java.lang.reflect.Method;

public class MethodWrapper {

	private Method method;
	private Object instance;
	
	public MethodWrapper(Method method, Object instance) {
		this.method = method;
		this.instance = instance;
	}

	public Method getMethod() {
		return method;
	}
	
	public Object getInstance() {
		return instance;
	}
	
}
