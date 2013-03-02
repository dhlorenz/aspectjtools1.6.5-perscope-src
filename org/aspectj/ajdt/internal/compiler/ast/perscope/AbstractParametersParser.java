package org.aspectj.ajdt.internal.compiler.ast.perscope;

import org.aspectj.weaver.patterns.PatternParser;

public abstract class AbstractParametersParser implements
		ParametersParser {

	protected PatternParser patternParser;
	protected StaticPerscopeParse weaving;
	
	public void setWeaving(StaticPerscopeParse weaving) {
		this.weaving = weaving;
	}

	public void setPatternParser(PatternParser patternParser) {
		this.patternParser = patternParser;
	}

	
}
