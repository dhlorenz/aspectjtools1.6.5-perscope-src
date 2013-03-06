package org.aspectj.ajdt.internal.compiler.ast.perscope.storage;

import java.lang.reflect.Modifier;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.BodyGenerator;
import org.aspectj.ajdt.internal.compiler.ast.perscope.StaticPerscope;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.HelperInterfaceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
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

public class PerscopeDelegator implements StaticPerscope {

	protected ResolvedMember aspectOfMethod;
	protected ResolvedMember hasAspectMethod;
	protected ResolvedMember bindMethod;
	protected EclipseFactory world;
	protected SourceTypeBinding binding;
	public UnresolvedType aspectsStorageType;
	private String implName;
	
	@Override
	public void instantiateMethodObjects(ReferenceType typeX, ClassScope scope,
			SourceTypeBinding binding) {
		aspectOfMethod = createAspectOfMethod(typeX);
		hasAspectMethod = createHasAspectMethod(typeX);
		bindMethod = createBindMethod(typeX);
				
		world = EclipseFactory.fromScopeLookupEnvironment(scope);
		this.binding = binding;
		// bind the methods
		binding.addMethod(world.makeMethodBinding(aspectOfMethod));
		binding.addMethod(world.makeMethodBinding(hasAspectMethod));
		binding.addMethod(world.makeMethodBinding(bindMethod));
		String type = "org.aspectj.ajdt.internal.compiler.ast.perscope.storage.PertargetAspectsStorage";
		type = 'L' + type;
		type = type + ';';
		aspectsStorageType = UnresolvedType.forSignature(type);
		world.makeTypeBinding(AjcMemberMaker.ASPECT_STORAGE_PROVIDER_TYPE);
	}

	protected ResolvedMember createAspectOfMethod(UnresolvedType declaringType) {
		// In case of the pertartget and perthis the aspectOf gets one parameter, the advised object
		return new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC | Modifier.STATIC, "aspectOf", "(Ljava/lang/Object;)"
				+ declaringType.getSignature());
	}
	
	protected ResolvedMember createHasAspectMethod(UnresolvedType declaringType) {
		// In case of the pertartget and perthis the hasAspect gets one parameter, the advised object
		return new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC | Modifier.STATIC, "hasAspect", "(Ljava/lang/Object;)Z");
	}
	
	protected ResolvedMember createBindMethod(UnresolvedType declaringType) {
		// In case of the pertartget and perthis the aspectOf gets one parameter, the advised object
		return new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC | Modifier.STATIC, "perObjectBind",
		"(Ljava/lang/Object;)V");
	}
	

	@Override
	public void generateMethodBodies(AspectDeclaration aspectDeclaration,
			ReferenceType typeX) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateMethodAttributes(ClassFile classFile,
			AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		// first need to inject the aspec interface
		TypeBinding interfaceType = generatePerObjectInterface(aspectDeclaration, classFile, typeX);
		// generate both aspectOf and hasAspect methods
		// in this case the inherited methods are not fit as here we need the interfaceType
		// that we've just created
		generatePerObjectAspectOfMethod(classFile, aspectDeclaration, typeX, interfaceType);
		generatePerObjectHasAspectMethod(classFile, aspectDeclaration, typeX, interfaceType);
		// generate the binding method - this method will bind the aspect into the advised object
		generatePerObjectBindMethod(classFile, aspectDeclaration, typeX, interfaceType);
	}
	
	
	
	
	private void generatePerObjectBindMethod(ClassFile classFile,
			AspectDeclaration aspectDeclaration, ReferenceType typeX,
			TypeBinding interfaceType) {
		aspectDeclaration.generateMethod(classFile, bindMethod, getPerObjectBindBodyGenerator(typeX, interfaceType));
	}

	private BodyGenerator getPerObjectBindBodyGenerator(final ReferenceType typeX,
			final TypeBinding interfaceType) {
		return new BodyGenerator() {
			
			@Override
			public void generate(CodeStream codeStream) {

				// body starts here
				codeStream.new_(binding);
				codeStream.dup();
				codeStream.invokespecial(new MethodBinding(0, "<init>".toCharArray(), TypeBinding.VOID, new TypeBinding[0],
						new ReferenceBinding[0], binding));
				//codeStream.invokeinterface(world.makeMethodBindingForCall(AjcMemberMaker.perObjectInterfaceSet(typeX)));

// VICTOR
				codeStream.astore_1();
				//codeStream.aload_1();
				///codeStream.aload_0();
				//codeStream.invokestatic(world.makeMethodBinding(AjcMemberMaker.aspectsStorageBindAspect(aspectsStorageType)));
				
				
				
				//codeStream.invokestatic(world.makeMethodBinding(AjcMemberMaker.getInstanceMethod(aspectsStorageType)));
				//codeStream.astore_2();
				//codeStream.aload_2();
				codeStream.ldc(implName);
				codeStream.aload_1();
				codeStream.aload_0();
				//codeStream.invokeinterface(world.makeMethodBinding(AjcMemberMaker.aspectsStorageBindAspect(typeX)));
				codeStream.invokestatic(world.makeMethodBindingForCall(AjcMemberMaker.providerBindMethod(aspectsStorageType)));
				
				
				//wrongType.place();
				codeStream.return_();
				// body ends here
			}
		};
	}

	private void generatePerObjectHasAspectMethod(ClassFile classFile,
			AspectDeclaration aspectDeclaration, ReferenceType typeX,
			TypeBinding interfaceType) {
		aspectDeclaration.generateMethod(classFile, hasAspectMethod, getPerObjectHasAspectBodyGenerator(typeX, interfaceType));
		
	}

	private BodyGenerator getPerObjectHasAspectBodyGenerator(
			final ReferenceType typeX, final TypeBinding interfaceType) {
		return new BodyGenerator() {
			
			@Override
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.ldc(typeX.getBaseName());
				codeStream.invokestatic(world.makeMethodBinding(AjcMemberMaker.classForName(typeX)));
				codeStream.astore_1();
				BranchLabel wrongType = new BranchLabel(codeStream);
				
				codeStream.ldc(implName);
				codeStream.aload_1();
				codeStream.aload_0();
				codeStream.invokestatic(world.makeMethodBinding(AjcMemberMaker.providerHasAspectMethod(aspectsStorageType)));
				
				
				//codeStream.aload_0();
				//codeStream.instance_of(interfaceType);
				codeStream.ifeq(wrongType);
				//codeStream.aload_0();
				//codeStream.checkcast(interfaceType);
				//codeStream.invokeinterface(world.makeMethodBindingForCall(AjcMemberMaker.perObjectInterfaceGet(typeX)));
				//codeStream.ifnull(wrongType);
				
				
				
				codeStream.iconst_1();
				codeStream.ireturn();

				wrongType.place();
				codeStream.iconst_0();
				
				
				
				
				codeStream.ireturn();
				// body ends here
			}
		};
	}

	private void generatePerObjectAspectOfMethod(ClassFile classFile, AspectDeclaration aspectDeclaration,
			ReferenceType typeX, TypeBinding interfaceType) {
		aspectDeclaration.generateMethod(classFile, aspectOfMethod, getPerObjectAspectOfBodyGenerator(typeX, interfaceType));
		
	}

	private BodyGenerator getPerObjectAspectOfBodyGenerator(
			final ReferenceType typeX, final TypeBinding interfaceType) {
		return new BodyGenerator() {
			
			@Override
			public void generate(CodeStream codeStream) {
				// body starts here
				////codeStream.invokestatic(world.makeMethodBinding(AjcMemberMaker.getInstanceMethod(aspectsStorageType)));
				////codeStream.astore_1();
				
				
				BranchLabel popWrongType = new BranchLabel(codeStream);
				//codeStream.aload_0();
				//codeStream.instance_of(interfaceType);
				//codeStream.ifeq(wrongType);
				
				codeStream.ldc(typeX.getBaseName());
				codeStream.invokestatic(world.makeMethodBinding(AjcMemberMaker.classForName(typeX)));
				codeStream.astore_1();
				
				
				//codeStream.aload_0();
				//codeStream.checkcast(interfaceType);
				//codeStream.invokeinterface(world.makeMethodBindingForCall(AjcMemberMaker.perObjectInterfaceGet(typeX)));
				codeStream.ldc(implName);
				codeStream.aload_1();
				codeStream.aload_0();
				codeStream.invokestatic(world.makeMethodBinding(AjcMemberMaker.providerAspectOfMethod(aspectsStorageType)));
				codeStream.checkcast(binding);
				
				codeStream.dup();
				codeStream.ifnull(popWrongType);
				codeStream.areturn();

				popWrongType.place();
				codeStream.pop();

				//wrongType.place();
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION));
				codeStream.dup();
				codeStream.invokespecial(world.makeMethodBindingForCall(AjcMemberMaker.noAspectBoundExceptionInit()));
				codeStream.athrow();
								// body ends here
			}
		};
	}

	private TypeBinding generatePerObjectInterface(AspectDeclaration aspectDeclaration, ClassFile classFile, ReferenceType typeX) {
		UnresolvedType interfaceTypeX = AjcMemberMaker.perObjectInterfaceType(typeX);
		HelperInterfaceBinding interfaceType = new HelperInterfaceBinding(this.binding, interfaceTypeX);
		world.addTypeBindingAndStoreInWorld(interfaceType);
		interfaceType.addMethod(world, AjcMemberMaker.perObjectInterfaceGet(typeX));
		interfaceType.addMethod(world, AjcMemberMaker.perObjectInterfaceSet(typeX));
		interfaceType.generateClass(aspectDeclaration.compilationResult(), classFile);
		return interfaceType;
	}

	@Override
	public void setImplementationName(String implName) {
		this.implName = implName;
	}

	
	
}
