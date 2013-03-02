package org.aspectj.ajdt.internal.compiler.ast.perscope.storage;

public class Persession implements DynamicPerscope {
	private static final String ASPECT_KEY = "aspectKey";
	@Override
	public Object aspectOf(Class<?> aspectType, Object object) {
		Object theAspect = ThreadLocalSession.getSession().getAttribute(ASPECT_KEY);
		return theAspect;
	}

	@Override
	public void bindAspectToObject(Object aspect, Object object) {
		Object theAspect = ThreadLocalSession.getSession().getAttribute(ASPECT_KEY);
		if (theAspect == null) {
			ThreadLocalSession.getSession().setAttribute(ASPECT_KEY, aspect);
		}
	}

	@Override
	public boolean hasAspect(Class<?> aspectType, Object object) {
		return ThreadLocalSession.getSession().getAttribute(ASPECT_KEY) != null;
	}

}
