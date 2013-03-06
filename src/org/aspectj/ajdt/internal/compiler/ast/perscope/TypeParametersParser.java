package org.aspectj.ajdt.internal.compiler.ast.perscope;

import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.TypePattern;

public class TypeParametersParser extends
		AbstractParametersParser {

	@Override
	public PerClause parsePerClause() {
		TypePattern data = patternParser.parseTypePattern();
		return weaving.getPerClause((AspectWeavingData)data);
	}

}
