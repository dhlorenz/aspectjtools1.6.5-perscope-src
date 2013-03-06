package org.aspectj.ajdt.internal.compiler.ast.perscope.storage;

public interface DynamicPerscope {
	void bindAspectToObject(Object aspect,Object object);
	Object aspectOf(Class<?> aspectType, Object object);
	boolean hasAspect(Class<?> aspectType, Object object);
}
