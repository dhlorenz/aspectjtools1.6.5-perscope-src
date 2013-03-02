package org.aspectj.ajdt.internal.compiler.ast.perscope;

import java.lang.reflect.Modifier;

import org.aspectj.ajdt.internal.compiler.ast.AspectClinit;
import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.BodyGenerator;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerSingleton;

public class Persingleton extends AbstractStaticPerscope {

	private FieldBinding initFailureField = null;
	protected ResolvedMember postClinitMethod;
	
	@Override
	public void instantiateMethodObjects(ReferenceType typeX, ClassScope scope,
			SourceTypeBinding binding) {
		super.instantiateMethodObjects(typeX, scope, binding);
		postClinitMethod = createPostClinitMethod(typeX);
		binding.addMethod(world.makeMethodBinding(postClinitMethod));
	}
	@Override
	public void generateMethodBodies(AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		initFailureField = world.createSyntheticFieldBinding(binding, AjcMemberMaker.initFailureCauseField(typeX));
		world.createSyntheticFieldBinding(aspectDeclaration.binding, AjcMemberMaker.perSingletonField(typeX));
		aspectDeclaration.methods[0] = new AspectClinit((Clinit) aspectDeclaration.methods[0], aspectDeclaration.compilationResult, 
				false, true, initFailureField);
	}
	
	
	protected ResolvedMember createPostClinitMethod(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC | Modifier.STATIC, 
				PREFIX + "postClinit", "()V");
	}
	
	
	@Override
	protected ResolvedMember createAspectOfMethod(UnresolvedType declaringType) {
		// in case of the singleton aspect the aspectOf() method has no parameters
		return new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC | Modifier.STATIC, 
				"aspectOf", "()" + declaringType.getSignature());
	}
	
	@Override
	protected ResolvedMember createHasAspectMethod(UnresolvedType declaringType) {
		// in case of the singleton aspect the hasAspect() method has no parameters
		return new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC | Modifier.STATIC, "hasAspect", "()Z");
	}
	
	@Override
	public void generateMethodAttributes(ClassFile classFile,
			AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		// generates the default aspectOf() and hasAspect() methods
		super.generateMethodAttributes(classFile, aspectDeclaration, typeX);
		//  generates the static block
		generateAjcClinitMethod(classFile, aspectDeclaration, typeX);
	}
	private void generateAjcClinitMethod(ClassFile classFile,
			AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		aspectDeclaration.generateMethod(classFile, postClinitMethod, getAjcClinitBodyGenerator(typeX));
	}

	@Override
	protected BodyGenerator getAspectOfBodyGenerator(final ReferenceType typeX) {
		return new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here (see end of each line for what it is doing!)
				FieldBinding fb = world.makeFieldBinding(AjcMemberMaker.perSingletonField(typeX));
				codeStream.getstatic(fb); // GETSTATIC
				BranchLabel isNonNull = new BranchLabel(codeStream);
				codeStream.ifnonnull(isNonNull); // IFNONNULL
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION)); // NEW
				
				
				codeStream.dup(); // DUP
				codeStream.ldc(typeX.getNameAsIdentifier()); // LDC
				codeStream.getstatic(initFailureField); // GETSTATIC
				codeStream.invokespecial(world.makeMethodBindingForCall(AjcMemberMaker.noAspectBoundExceptionInitWithCause())); // INVOKESPECIAL
				
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.MY_CLASS)); // NEW
				codeStream.invokespecial(world.makeMethodBindingForCall(AjcMemberMaker.myClassMember()));
				
				codeStream.athrow(); // ATHROW
				isNonNull.place();
				codeStream.getstatic(fb); // GETSTATIC
				codeStream.areturn(); // ARETURN
				// body ends here
			}
		};

	}

	@Override
	protected BodyGenerator getHasAspectBodyGenerator(final ReferenceType typeX) {
		return new BodyGenerator() {			
			@Override
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.getstatic(world.makeFieldBinding(AjcMemberMaker.perSingletonField(typeX)));
				BranchLabel isNull = new BranchLabel(codeStream);
				codeStream.ifnull(isNull);
				codeStream.iconst_1();
				codeStream.ireturn();
				isNull.place();
				codeStream.iconst_0();
				codeStream.ireturn();
				// body ends here
			}
		};
	}
	
	private BodyGenerator getAjcClinitBodyGenerator(final ReferenceType typeX) {
		return new BodyGenerator() {			
			@Override
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.new_(binding);
				codeStream.dup();
				codeStream.invokespecial(new MethodBinding(0, "<init>".toCharArray(), TypeBinding.VOID, new TypeBinding[0],
						new ReferenceBinding[0], binding));

				codeStream.putstatic(world.makeFieldBinding(AjcMemberMaker.perSingletonField(typeX)));
				codeStream.return_();
				// body ends here
			}
		};
	}


	@Override
	public PerClause getPerClause(AspectWeavingData entry) {
		PerClause perClause = new PerSingleton();
		return perClause;
	}

	@Override
	public ParametersParser getPerClauseAspectParser() {
		PointcutParametersParser aspectParser = new PointcutParametersParser();
		setParser(aspectParser);
		aspectParser.setWeaving(this);
		return aspectParser;
	}
}
