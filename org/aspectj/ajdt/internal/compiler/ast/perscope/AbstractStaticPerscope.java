package org.aspectj.ajdt.internal.compiler.ast.perscope;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.BodyGenerator;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.UnresolvedType;

/**
 * Base class for the PerClauseAspectWeaving implementations
 * It holds the EclipseFactory reference, as it is needed almost always
 * for actual methods creation and geneartion
 * @author yulia
 *
 */
public abstract class AbstractStaticPerscope implements
		StaticPerscopeParse {
	public static final String PREFIX = "ajc$";
	// All the implementation will use these methods: aspectOf() and hasAspect()
	protected ResolvedMember aspectOfMethod;
	protected ResolvedMember hasAspectMethod;
	protected EclipseFactory world;
	protected SourceTypeBinding binding;
	protected ParametersParser parser;
	
	public void setParser(ParametersParser parser) {
		this.parser = parser;
	}

	/**
	 * Will be called by the AspectDeclaration class, which is the main AspectJ class that deals with the aspect
	 */
	public void generateMethodBodies(AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		
	}
	
	/**
	 * By default the aspectOf() and hasAspect() will be created
	 */
	@Override
		// create the methods
	public void instantiateMethodObjects(ReferenceType typeX,ClassScope scope,SourceTypeBinding binding) {
		aspectOfMethod = createAspectOfMethod(typeX);
		hasAspectMethod = createHasAspectMethod(typeX);
		
		world = EclipseFactory.fromScopeLookupEnvironment(scope);
		this.binding = binding;
		// bind the methods
		binding.addMethod(world.makeMethodBinding(aspectOfMethod));
		binding.addMethod(world.makeMethodBinding(hasAspectMethod));
	}

	/**
	 * By default the aspectOf() and hasAspect() will be generated
	 */
	public void generateMethodAttributes(ClassFile classFile, AspectDeclaration aspectDeclaration, final ReferenceType typeX) {
		aspectDeclaration.generateMethod(classFile, aspectOfMethod, getAspectOfBodyGenerator(typeX));
		aspectDeclaration.generateMethod(classFile, hasAspectMethod, getHasAspectBodyGenerator(typeX));
	}
	
	protected abstract BodyGenerator getAspectOfBodyGenerator(final ReferenceType typeX);
	protected abstract BodyGenerator getHasAspectBodyGenerator(final ReferenceType typeX);

	protected abstract ResolvedMember createAspectOfMethod(UnresolvedType declaringType);
	protected abstract ResolvedMember createHasAspectMethod(UnresolvedType declaringType);
	
	@Override
	public void setImplementationName(String implName) {
	}
}
