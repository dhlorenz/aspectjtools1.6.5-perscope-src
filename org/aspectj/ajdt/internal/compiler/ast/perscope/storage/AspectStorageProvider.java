package org.aspectj.ajdt.internal.compiler.ast.perscope.storage;

public class AspectStorageProvider  {
	private static DynamicPerscope aspectStorage;
	
	private AspectStorageProvider() {
		
	}
	public static DynamicPerscope getAspectsStorageImpl(String implName) {
		if (aspectStorage == null) {
			try {
				Class<?> clazz = Class.forName(implName);
				Object object = clazz.newInstance();
				aspectStorage = (DynamicPerscope) object;
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Failed to instantiate the aspects storage implementation");
			}
		}
		return aspectStorage;
	}
	public static void bindAspectToObject(String implName, Object aspect,Object object) {
		getAspectsStorageImpl(implName).bindAspectToObject(aspect, object);
	}
	public static Object aspectOf(String implName, Class<?> aspectType, Object object) {
		return getAspectsStorageImpl(implName).aspectOf(aspectType, object);
	}
	public static boolean hasAspect(String implName, Class<?> aspectType, Object object)  {
		return getAspectsStorageImpl(implName).hasAspect(aspectType, object);
	}
	
}
