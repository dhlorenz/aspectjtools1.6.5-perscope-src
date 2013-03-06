package org.aspectj.ajdt.internal.compiler.ast.perscope;

import java.lang.reflect.Modifier;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.BodyGenerator;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.ExceptionLabel;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerTypeWithin;
import org.aspectj.weaver.patterns.TypePattern;

public class Pertypewithin extends AbstractStaticPerscope {

	private ResolvedMember ptwGetWithinTypeNameMethod;
	private ResolvedMember getInstanceMethod;
	private ResolvedMember createInstanceMethod;
	
	@Override
	public void generateMethodBodies(AspectDeclaration aspectDeclaration,
			ReferenceType typeX) {
		world.createSyntheticFieldBinding(binding, AjcMemberMaker.perTypeWithinWithinTypeField(typeX, typeX));
		world.createSyntheticFieldBinding(binding, AjcMemberMaker.perTypeWithinGetWithinTypeNameMethod(world.fromBinding(binding), 
				world.getWorld().isInJava5Mode()));
		
	}
	@Override
	public void instantiateMethodObjects(ReferenceType typeX, ClassScope scope,
			SourceTypeBinding binding) {
		super.instantiateMethodObjects(typeX, scope, binding);
		
		ptwGetWithinTypeNameMethod = createPerTypeWithinGetWithinTypeNameMethod(typeX);		
		binding.addMethod(world.makeMethodBinding(ptwGetWithinTypeNameMethod));
		getInstanceMethod = createGetInstanceMethod(typeX);		
		binding.addMethod(world.makeMethodBinding(getInstanceMethod));
		createInstanceMethod = createCreateInstanceMethod(typeX);		
		binding.addMethod(world.makeMethodBinding(createInstanceMethod));
	}
	
	private ResolvedMember createPerTypeWithinGetWithinTypeNameMethod(UnresolvedType declaringType) {
		ResolvedMemberImpl rm = new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC,
				UnresolvedType.JAVA_LANG_STRING, // return value
				NameMangler.PERTYPEWITHIN_GETWITHINTYPENAME_METHOD, UnresolvedType.NONE);
		return rm;
	}
	
	private ResolvedMember createGetInstanceMethod(UnresolvedType declaringType) {
		ResolvedMemberImpl rm = new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PRIVATE | Modifier.STATIC, declaringType, // return value
				NameMangler.PERTYPEWITHIN_GETINSTANCE_METHOD, new UnresolvedType[] { UnresolvedType.JAVA_LANG_CLASS });
		return rm;
	}

	private ResolvedMember createCreateInstanceMethod(UnresolvedType declaringType) {
		ResolvedMemberImpl rm = new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC | Modifier.STATIC, declaringType, // return value
				NameMangler.PERTYPEWITHIN_CREATEASPECTINSTANCE_METHOD, new UnresolvedType[] { UnresolvedType
						.forSignature("Ljava/lang/String;") }, new UnresolvedType[] {});
		return rm;
	}
	
	@Override
	protected ResolvedMember createAspectOfMethod(UnresolvedType declaringType) {
		UnresolvedType parameterType = null;
		boolean inJava5Mode = false;
		if (inJava5Mode ) {
			parameterType = UnresolvedType.forRawTypeName("java.lang.Class");
		} else {
			parameterType = UnresolvedType.forSignature("Ljava/lang/Class;");
		}
		return new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC | Modifier.STATIC, declaringType, "aspectOf",
				new UnresolvedType[] { parameterType });
		
	}

	@Override
	protected ResolvedMember createHasAspectMethod(UnresolvedType declaringType) {
		UnresolvedType parameterType = null;
		boolean inJava5Mode = false;
		if (inJava5Mode ) {
			parameterType = UnresolvedType.forRawTypeName("java.lang.Class");
		} else {
			parameterType = UnresolvedType.forSignature("Ljava/lang/Class;");
		}
		return new ResolvedMemberImpl(Member.METHOD, declaringType, Modifier.PUBLIC | Modifier.STATIC, ResolvedType.BOOLEAN, "hasAspect",
				new UnresolvedType[] { parameterType });

	}

	
	@Override
	public void generateMethodAttributes(ClassFile classFile,
			AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		super.generateMethodAttributes(classFile, aspectDeclaration, typeX);
		
		//generatePerTypeWithinAspectOfMethod(classFile); // public static <aspecttype> aspectOf(java.lang.Class)
		generateGetInstanceMethod(classFile, aspectDeclaration, typeX); // private static <aspecttype> ajc$getInstance(Class c) throws
		// Exception
		//generatePerTypeWithinHasAspectMethod(classFile);
		generateCreateAspectInstanceMethod(classFile, aspectDeclaration, typeX); // generate public static X ajc$createAspectInstance(Class
		// forClass) {
		generateGetWithinTypeNameMethod(classFile, aspectDeclaration, typeX);
	}
	
	private void generateGetWithinTypeNameMethod(ClassFile classFile,
			AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		aspectDeclaration.generateMethod(classFile, ptwGetWithinTypeNameMethod, getGetWithinTypeNameBodyGenerator(typeX));
	}

	private BodyGenerator getGetWithinTypeNameBodyGenerator(final ReferenceType typeX) {
		return new BodyGenerator() {
			
			@Override
			public void generate(CodeStream codeStream) {
				ExceptionLabel exc = new ExceptionLabel(codeStream, world.makeTypeBinding(UnresolvedType.JAVA_LANG_EXCEPTION));
				exc.placeStart();
				codeStream.aload_0();
				codeStream.getfield(world.makeFieldBinding(AjcMemberMaker.perTypeWithinWithinTypeField(typeX, typeX)));
				codeStream.areturn();	
			}
		};
	}

	private void generateCreateAspectInstanceMethod(ClassFile classFile,
			AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		aspectDeclaration.generateMethod(classFile, createInstanceMethod, getCreateAspectInstanceBodyGenerator(typeX));
	}

	private BodyGenerator getCreateAspectInstanceBodyGenerator(
			final ReferenceType typeX) {
		return new BodyGenerator() {
			
			@Override
			public void generate(CodeStream codeStream) {
				codeStream.new_(world.makeTypeBinding(typeX));
				codeStream.dup();
				codeStream.invokespecial(new MethodBinding(0, "<init>".toCharArray(), TypeBinding.VOID, new TypeBinding[0],
						new ReferenceBinding[0], binding));
				codeStream.astore_1();
				codeStream.aload_1();
				codeStream.aload_0();
				codeStream.putfield(world.makeFieldBinding(AjcMemberMaker.perTypeWithinWithinTypeField(typeX, typeX)));
				codeStream.aload_1();
				codeStream.areturn();
			}
		};
	}

	private void generateGetInstanceMethod(ClassFile classFile,
			AspectDeclaration aspectDeclaration, ReferenceType typeX) {
		aspectDeclaration.generateMethod(classFile, getInstanceMethod, getGetInstanceBodyGenerator(typeX));
		
	}

	private BodyGenerator getGetInstanceBodyGenerator(final ReferenceType typeX) {
		return new BodyGenerator() {
			
			@Override
			public void generate(CodeStream codeStream) {
				ExceptionLabel exc = new ExceptionLabel(codeStream, world.makeTypeBinding(UnresolvedType.JAVA_LANG_EXCEPTION));
				exc.placeStart();
				codeStream.aload_0();
				codeStream.ldc(NameMangler.perTypeWithinLocalAspectOf(typeX));
				codeStream.aconst_null();
				codeStream.invokevirtual(new MethodBinding(0, "getDeclaredMethod".toCharArray(),
						world.makeTypeBinding(UnresolvedType.forSignature("Ljava/lang/reflect/Method;")), // return type
						new TypeBinding[] { world.makeTypeBinding(UnresolvedType.forSignature("Ljava/lang/String;")),
								world.makeTypeBinding(UnresolvedType.forSignature("[Ljava/lang/Class;")) },
						new ReferenceBinding[0], (ReferenceBinding) world.makeTypeBinding(UnresolvedType.JAVA_LANG_CLASS)));
				codeStream.astore_1();
				codeStream.aload_1();
				codeStream.aconst_null();
				codeStream.aconst_null();
				codeStream
						.invokevirtual(new MethodBinding(0, "invoke".toCharArray(), world.makeTypeBinding(UnresolvedType.OBJECT),
								new TypeBinding[] { world.makeTypeBinding(UnresolvedType.OBJECT),
										world.makeTypeBinding(UnresolvedType.forSignature("[Ljava/lang/Object;")) },
								new ReferenceBinding[0], (ReferenceBinding) world
										.makeTypeBinding(UnresolvedType.JAVA_LANG_REFLECT_METHOD)));
				codeStream.checkcast(world.makeTypeBinding(typeX));
				codeStream.astore_2();
				codeStream.aload_2();
				exc.placeEnd();
				codeStream.areturn();
				exc.place();
				codeStream.astore_1();
				// this just returns null now - the old version used to throw the caught exception!
				codeStream.aconst_null();
				codeStream.areturn();
			}
		};
	}

	@Override
	protected BodyGenerator getAspectOfBodyGenerator(final ReferenceType typeX) {
		return new BodyGenerator() {
			
			@Override
			public void generate(CodeStream codeStream) {
				BranchLabel instanceFound = new BranchLabel(codeStream);

				ExceptionLabel anythingGoesWrong = new ExceptionLabel(codeStream, world
						.makeTypeBinding(UnresolvedType.JAVA_LANG_EXCEPTION));
				anythingGoesWrong.placeStart();
				codeStream.aload_0();
				codeStream.invokestatic(world.makeMethodBindingForCall(AjcMemberMaker.perTypeWithinGetInstance(typeX)));
				codeStream.astore_1();
				codeStream.aload_1();
				codeStream.ifnonnull(instanceFound);
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION));
				codeStream.dup();

				codeStream.ldc(typeX.getName());
				codeStream.aconst_null();

				codeStream.invokespecial(world.makeMethodBindingForCall(AjcMemberMaker.noAspectBoundExceptionInit2()));
				codeStream.athrow();
				instanceFound.place();
				codeStream.aload_1();

				codeStream.areturn();
				anythingGoesWrong.placeEnd();
				anythingGoesWrong.place();

				codeStream.astore_1();
				codeStream.new_(world.makeTypeBinding(AjcMemberMaker.NO_ASPECT_BOUND_EXCEPTION));

				codeStream.dup();

				// Run the simple ctor for NABE
				codeStream.invokespecial(world.makeMethodBindingForCall(AjcMemberMaker.noAspectBoundExceptionInit()));
				codeStream.athrow();

			}
		};
	}

	@Override
	protected BodyGenerator getHasAspectBodyGenerator(final ReferenceType typeX) {
		return new BodyGenerator() {
			
			@Override
			public void generate(CodeStream codeStream) {
				ExceptionLabel goneBang = new ExceptionLabel(codeStream, world.makeTypeBinding(UnresolvedType.JAVA_LANG_EXCEPTION));
				BranchLabel noInstanceExists = new BranchLabel(codeStream);
				BranchLabel leave = new BranchLabel(codeStream);
				goneBang.placeStart();
				codeStream.aload_0();
				codeStream.invokestatic(world.makeMethodBinding(AjcMemberMaker.perTypeWithinGetInstance(typeX)));
				codeStream.ifnull(noInstanceExists);
				codeStream.iconst_1();
				codeStream.goto_(leave);
				noInstanceExists.place();
				codeStream.iconst_0();
				leave.place();
				goneBang.placeEnd();
				codeStream.ireturn();
				goneBang.place();
				codeStream.astore_1();
				codeStream.iconst_0();
				codeStream.ireturn();
			}
		};
	}
	@Override
	public PerClause getPerClause(AspectWeavingData data) {
		return new PerTypeWithin((TypePattern) data);
		
	}
	@Override
	public ParametersParser getPerClauseAspectParser() {
		TypeParametersParser aspectParser = new TypeParametersParser();
		setParser(aspectParser);
		aspectParser.setWeaving(this);
		return aspectParser;
	}

}
