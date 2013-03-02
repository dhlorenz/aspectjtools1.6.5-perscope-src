package org.aspectj.ajdt.internal.compiler.ast.perscope.storage;

public class Persingleton implements DynamicPerscope {

	private static Object theAspect;
	@Override
	public Object aspectOf(Class<?> aspectType, Object object) {
		return theAspect;
	}

	@Override
	public void bindAspectToObject(Object aspect, Object object) {
		if (theAspect == null) {
			theAspect = aspect;
		}
	}

	@Override
	public boolean hasAspect(Class<?> aspectType, Object object) {
		return theAspect != null;
	}

}
