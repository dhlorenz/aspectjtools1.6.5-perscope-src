package org.aspectj.ajdt.internal.compiler.ast.perscope;

import java.lang.reflect.Modifier;

import org.aspectj.ajdt.internal.compiler.ast.AspectClinit;
import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.BodyGenerator;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.PerCflow;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

public class Percflow extends AbstractStaticPerscope {
	
	
	@Override
	public void generateMethodBodies(AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		world.createSyntheticFieldBinding(binding, AjcMemberMaker.perCflowField(typeX));
		// CUSTARD binding.addField(factory.makeFieldBinding(AjcMemberMaker.perCflowField(typeX)));
		aspectDeclaration.methods[0] = new AspectClinit((Clinit) aspectDeclaration.methods[0], aspectDeclaration.compilationResult, 
				true, false, null);
	}
	
	@Override
	public void generateMethodAttributes(ClassFile classFile,
			AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		// first generate the aspectOf() and hasAspect() methods
		super.generateMethodAttributes(classFile, aspectDeclaration, typeX);
		// generate the method that will push the aspect into the stack
		generatePushMethod(classFile, aspectDeclaration, typeX);
		// generate the method that will create the stack
		generateAjcClinitMethod(classFile, aspectDeclaration, typeX);

	}
	
	
	private void generateAjcClinitMethod(ClassFile classFile,
			AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		MethodBinding ajcClinitMethod = world.makeMethodBinding(AjcMemberMaker.ajcPreClinitMethod(world.fromBinding(binding)));
		aspectDeclaration.generateMethod(classFile, ajcClinitMethod, getAjcClinitBodyGenerator(typeX));
	}


	private BodyGenerator getAjcClinitBodyGenerator(final ReferenceType typeX) {
		return new BodyGenerator() {			
			@Override
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.CFLOW_STACK_TYPE));
				codeStream.dup();
				codeStream.invokespecial(world.makeMethodBindingForCall(AjcMemberMaker.cflowStackInit()));
				codeStream.putstatic(world.makeFieldBinding(AjcMemberMaker.perCflowField(typeX)));
				codeStream.return_();
				// body ends here
			}
		};
	}


	private void generatePushMethod(ClassFile classFile,
			AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		MethodBinding pushMethod = world.makeMethodBinding(AjcMemberMaker.perCflowPush(world.fromBinding(binding)));		
		aspectDeclaration.generateMethod(classFile, pushMethod, getPushMethodBodyGenerator(typeX));
	}


	private BodyGenerator getPushMethodBodyGenerator(final ReferenceType typeX) {
		return new BodyGenerator() {			
			@Override
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.getstatic(world.makeFieldBinding(AjcMemberMaker.perCflowField(typeX)));
				codeStream.new_(binding);
				codeStream.dup();
				codeStream.invokespecial(new MethodBinding(0, "<init>".toCharArray(), TypeBinding.VOID, new TypeBinding[0],
						new ReferenceBinding[0], binding));

				codeStream.invokevirtual(world.makeMethodBindingForCall(AjcMemberMaker.cflowStackPushInstance()));
				codeStream.return_();
				// body ends here

			}
		};
	}


	@Override
	protected ResolvedMember createAspectOfMethod(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC | Modifier.STATIC, 
				"aspectOf", "()" + declaringType.getSignature());
	}
	
	@Override
	protected ResolvedMember createHasAspectMethod(UnresolvedType declaringType) {
		return new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC | Modifier.STATIC, "hasAspect", "()Z");
	}

	@Override
	protected BodyGenerator getAspectOfBodyGenerator(final ReferenceType typeX) {
		return new BodyGenerator() {			
			@Override
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.getstatic(world.makeFieldBinding(AjcMemberMaker.perCflowField(typeX)));
				codeStream.invokevirtual(world.makeMethodBindingForCall(AjcMemberMaker.cflowStackPeekInstance()));
				codeStream.checkcast(binding);
				codeStream.areturn();
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
				codeStream.getstatic(world.makeFieldBinding(AjcMemberMaker.perCflowField(typeX)));
				codeStream.invokevirtual(world.makeMethodBindingForCall(AjcMemberMaker.cflowStackIsValid()));
				codeStream.ireturn();
				// body ends here
			}
		};
	}

	@Override
	public PerClause getPerClause(AspectWeavingData data) {
	//	parser.parsePerClause();
		PerClause perClause = new PerCflow((Pointcut)data, false);
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
