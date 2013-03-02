package org.aspectj.ajdt.internal.compiler.ast.perscope;

import org.aspectj.weaver.patterns.PerClause;

public interface StaticPerscopeParse extends StaticPerscope {

	/**
	 * Creates the PerClause instance with the specified Pointcut.
	 * Used by the ols AspectJ code. Need it for backward compatability
	 * @param entry
	 * @return
	 */
	PerClause getPerClause(AspectWeavingData data);
	
	ParametersParser getPerClauseAspectParser();
}
