package org.aspectj.ajdt.internal.compiler.ast.perscope.storage;

import java.util.HashMap;
import java.util.Map;

public class Perobject implements DynamicPerscope {

	private Map<Class<?>, Map<Object,Object>> storage = new HashMap<Class<?>, Map<Object,Object>>();
	
	
	public Object aspectOf(Class<?> aspectType, Object object) {
		Map<Object, Object> mappingForAspectType = storage.get(aspectType);
		if (mappingForAspectType != null) {
			Object aspect = mappingForAspectType.get(object);
			return aspect;
		}
		return null;
	}

	
	public void bindAspectToObject(Object aspect,
			Object object) {
		Class<?> aspectType = aspect.getClass();
		Map<Object, Object> mappingForAspectType = storage.get(aspectType);
		if (mappingForAspectType == null) {
			mappingForAspectType = new HashMap<Object,Object>();
			storage.put(aspectType, mappingForAspectType);
		}
		if (!mappingForAspectType.containsKey(object)) {
			mappingForAspectType.put(object, aspect);
		}
	}

	
	public boolean hasAspect(Class<?> aspectType, Object object) {
		
	//	System.out.println("PertargetAspectsStorage hasAspect() aspectType = " + aspectType);
//		System.out.println("PertargetAspectsStorage hasAspect() object = " + object);
		Map<Object, Object> mappingForAspectType = storage.get(aspectType);
		if (mappingForAspectType != null) {
			
			boolean containsKey = mappingForAspectType.containsKey(object);
	//		System.out.println("PertargetAspectsStorage hasAspect() containsKey = " + containsKey);
			return containsKey;
		}
		return false;
	}

}
