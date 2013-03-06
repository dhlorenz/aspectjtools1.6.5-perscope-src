package org.aspectj.ajdt.internal.compiler.ast.perscope;

import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerObject;
import org.aspectj.weaver.patterns.Pointcut;

public class Perthis extends Perobject {

	@Override
	public PerClause getPerClause(AspectWeavingData entry) {
		PerClause perClause = new PerObject((Pointcut)entry, true);
		return perClause;
	}

}
