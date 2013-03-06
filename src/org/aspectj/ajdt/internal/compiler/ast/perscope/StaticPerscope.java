package org.aspectj.ajdt.internal.compiler.ast.perscope;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.weaver.ReferenceType;

public interface StaticPerscope {

	/**
	 * Instantiates the synthetic methods for the aspect,
	 * like aspectOf, hasAspect etc.
	 * @param typeX
	 * @param scope
	 * @param binding
	 */
	void instantiateMethodObjects(ReferenceType typeX, ClassScope scope, SourceTypeBinding binding);

	/**
	 * Generates the synthetic methods bodies
	*/
	void generateMethodBodies(AspectDeclaration aspectDeclaration, ReferenceType typeX);
	
	/**
	 * Generates the synthetic methods attributes
	 * @param classFile
	 * @param aspectDeclaration
	 * @param typeX
	 */
	void generateMethodAttributes(ClassFile classFile, AspectDeclaration aspectDeclaration, final ReferenceType typeX);
	
	void setImplementationName(String implName);
}
