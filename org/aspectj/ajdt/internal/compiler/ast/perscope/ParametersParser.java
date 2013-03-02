package org.aspectj.ajdt.internal.compiler.ast.perscope;

import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.PerClause;

public interface ParametersParser {
	PerClause parsePerClause();
	void setPatternParser(PatternParser patternParser);
}
