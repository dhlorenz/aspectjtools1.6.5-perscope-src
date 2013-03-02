package org.aspectj.ajdt.internal.compiler.ast.perscope.storage;

public class Perthread implements DynamicPerscope {
	private static ThreadLocalAspect theAspect = new ThreadLocalAspect();
	public Object aspectOf(Class<?> aspectType, Object object) {
		return theAspect.get();
	}
	public void bindAspectToObject(Object aspect, Object object) {
		if (theAspect.get() == null) {
			theAspect.set(aspect);
		}
	}
	public boolean hasAspect(Class<?> aspectType, Object object) {
		return theAspect.get() != null;
	}
	private static class ThreadLocalAspect extends ThreadLocal<Object> {		
	}
}
