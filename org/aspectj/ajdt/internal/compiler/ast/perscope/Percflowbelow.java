package org.aspectj.ajdt.internal.compiler.ast.perscope;

import org.aspectj.weaver.patterns.PerCflow;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

public class Percflowbelow extends Percflow {

	@Override
	public PerClause getPerClause(AspectWeavingData data) {
		parser.parsePerClause();
		PerClause perClause = new PerCflow((Pointcut)data, true);
		return perClause;
	}

}
