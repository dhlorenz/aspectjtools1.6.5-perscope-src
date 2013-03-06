package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;

/**
 * Generates the body of some method
 * @author yulia
 *
 */
public interface BodyGenerator {
	void generate(CodeStream codeStream);
}
