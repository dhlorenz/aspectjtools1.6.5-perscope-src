package org.aspectj.ajdt.internal.compiler.ast.perscope.storage;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.runtime.internal.CFlowStack;

public class Percflow implements DynamicPerscope {

	private Map<Class<?>, CFlowStack> storage = new HashMap<Class<?>, CFlowStack>();
	
	
	public Object aspectOf(Class<?> aspectType, Object object) {
		//System.out.println("PercflowAspectsStorage aspectOf() aspectType = " + aspectType);
		//System.out.println("PercflowAspectsStorage aspectOf() object = " + object);
		
		CFlowStack cFlowStack = storage.get(aspectType);
		if (cFlowStack != null) {
			Object aspect = cFlowStack.peekInstance();
			//System.out.println("PercflowAspectsStorage aspectOf() going to return aspect " + aspect);
			return aspect;
		}
		return null;
	}

	
	public void bindAspectToObject(Object aspect,
			Object object) {
		Class<?> aspectType = aspect.getClass();
		//System.out.println("PercflowAspectsStorage bind() aspectType = " + aspectType);
		//System.out.println("PercflowAspectsStorage bind() aspect = " + aspect);
		//System.out.println("PercflowAspectsStorage bind() object = " + object);
		CFlowStack cFlowStack = storage.get(aspectType);
		if (cFlowStack == null) {
			//System.out.println("PercflowAspectsStorage bind() no cFlowStack");
			cFlowStack = new CFlowStack();
			storage.put(aspectType, cFlowStack);
		}
		cFlowStack.pushInstance(aspect);
	}

	
	public boolean hasAspect(Class<?> aspectType, Object object) {
		
		//System.out.println("PercflowAspectsStorage hasAspect() aspectType = " + aspectType);
		//System.out.println("PercflowAspectsStorage hasAspect() object = " + object);
		CFlowStack cFlowStack = storage.get(aspectType);
		if (cFlowStack != null) {
			
			boolean containsKey = cFlowStack.isValid();
			//System.out.println("PercflowAspectsStorage hasAspect() containsKey = " + containsKey);
			return containsKey;
		}
		return false;
	}

}
