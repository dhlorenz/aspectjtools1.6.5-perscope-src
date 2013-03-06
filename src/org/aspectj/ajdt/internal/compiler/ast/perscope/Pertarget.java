package org.aspectj.ajdt.internal.compiler.ast.perscope;

import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerObject;
import org.aspectj.weaver.patterns.Pointcut;

public class Pertarget extends Perobject {

	@Override
	public PerClause getPerClause(AspectWeavingData entry) {
		PerClause perClause = new PerObject((Pointcut)entry, false);
		return perClause;
	}

}
