package org.aspectj.ajdt.internal.compiler.ast.perscope;

import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

public class PointcutParametersParser extends
		AbstractParametersParser {

	@Override
	public PerClause parsePerClause() {
		Pointcut parsePointcut = patternParser.parsePointcut();
		return weaving.getPerClause((AspectWeavingData)parsePointcut);
	
	}

}
