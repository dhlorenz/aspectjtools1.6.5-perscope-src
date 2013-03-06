/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.ast.perscope.StaticPerscopeParse;
import org.aspectj.ajdt.internal.compiler.ast.perscope.StaticPerscope;
import org.aspectj.ajdt.internal.compiler.ast.perscope.Persingleton;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseScope;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseSourceType;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseTypeMunger;
import org.aspectj.ajdt.internal.compiler.lookup.InlineAccessFieldBinding;
import org.aspectj.ajdt.internal.compiler.lookup.PrivilegedHandler;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerFromSuper;
import org.aspectj.weaver.patterns.PerSingleton;
import org.aspectj.weaver.patterns.TypePattern;

// (we used to...) making all aspects member types avoids a nasty hierarchy pain
// switched from MemberTypeDeclaration to TypeDeclaration
public class AspectDeclaration extends TypeDeclaration {
	// public IAjDeclaration[] ajDeclarations;

	
	

	
	
	
	// private AjAttribute.Aspect aspectAttribute;
	public PerClause perClause;
	public ResolvedMember aspectOfMethod;
	public ResolvedMember ptwGetWithinTypeNameMethod;
	public ResolvedMember hasAspectMethod;

	public Map accessForInline = new HashMap();
	public Map superAccessForInline = new HashMap();

	public boolean isPrivileged;
	private int declaredModifiers;

	public EclipseSourceType concreteName;

	public ReferenceType typeX;

	public EclipseFactory factory; // ??? should use this consistently

	public int adviceCounter = 1; // Used as a part of the generated name for advice methods
	public int declareCounter = 1; // Used as a part of the generated name for methods representing declares

	// for better error messages in 1.0 to 1.1 transition
	public TypePattern dominatesPattern;

	public AspectDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
		// perClause = new PerSingleton();
	}

	
	
	
	
	
	
	
	
	public boolean isAbstract() {
		return (modifiers & ClassFileConstants.AccAbstract) != 0;
	}

	public void resolve() {
		declaredModifiers = modifiers; // remember our modifiers, we're going to be public in generateCode
		if (binding == null) {
			ignoreFurtherInvestigation = true;
			return;
		}

		super.resolve();
	}

	public void checkSpec(ClassScope scope) {
		if (ignoreFurtherInvestigation)
			return;
		if (dominatesPattern != null) {
			scope.problemReporter().signalError(
					dominatesPattern.getStart(),
					dominatesPattern.getEnd(),
					"dominates has changed for 1.1, use 'declare precedence: " + new String(this.name) + ", "
							+ dominatesPattern.toString() + ";' " + "in the body of the aspect instead");
		}

		if (!isAbstract()) {
			MethodBinding[] methods = binding.methods();
			for (int i = 0, len = methods.length; i < len; i++) {
				MethodBinding m = methods[i];
				if (m.isConstructor()) {
					// this make all constructors in aspects invisible and thus uncallable
					// XXX this only works for aspects that come from source
					methods[i] = new MethodBinding(m, binding) {
						public boolean canBeSeenBy(InvocationSite invocationSite, Scope scope) {
							return false;
						}
					};

					if (m.parameters != null && m.parameters.length != 0) {
						scope.problemReporter().signalError(m.sourceStart(), m.sourceEnd(),
								"only zero-argument constructors allowed in concrete aspect");
					}
				}
			}

			// check the aspect was not declared generic, only abstract aspects can have type params
			if (typeParameters != null && typeParameters.length > 0) {
				scope.problemReporter().signalError(sourceStart(), sourceEnd(), "only abstract aspects can have type parameters");
			}
		}

		if (this.enclosingType != null) {
			if (!Modifier.isStatic(modifiers)) {
				scope.problemReporter().signalError(sourceStart, sourceEnd, "inner aspects must be static");
				ignoreFurtherInvestigation = true;
				return;
			}
		}

		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(scope);
		ResolvedType myType = typeX;
		// if (myType == null) System.err.println("bad myType for: " + this);
		ResolvedType superType = myType.getSuperclass();

		// can't be Serializable/Cloneable unless -XserializableAspects
		if (!world.isXSerializableAspects()) {
			if (world.getWorld().getCoreType(UnresolvedType.SERIALIZABLE).isAssignableFrom(myType)) {
				scope.problemReporter().signalError(sourceStart, sourceEnd, "aspects may not implement Serializable");
				ignoreFurtherInvestigation = true;
				return;
			}
			if (world.getWorld().getCoreType(UnresolvedType.CLONEABLE).isAssignableFrom(myType)) {
				scope.problemReporter().signalError(sourceStart, sourceEnd, "aspects may not implement Cloneable");
				ignoreFurtherInvestigation = true;
				return;
			}

		}

		if (superType.isAspect()) {
			if (!superType.isAbstract()) {
				scope.problemReporter().signalError(sourceStart, sourceEnd, "can not extend a concrete aspect");
				ignoreFurtherInvestigation = true;
				return;
			}

			// if super type is generic, check that we have fully parameterized it
			if (superType.isRawType()) {
				scope.problemReporter().signalError(sourceStart, sourceEnd,
						"a generic super-aspect must be fully parameterized in an extends clause");
				ignoreFurtherInvestigation = true;
				return;
			}
		}
	}

	private FieldBinding initFailureField = null;

	/**
	 * AMC - this method is called by the AtAspectJVisitor during beforeCompilation processing in the AjCompiler adapter. We use
	 * this hook to add in the @AspectJ annotations.
	 */
	public void addAtAspectJAnnotations() {
		Annotation atAspectAnnotation = AtAspectJAnnotationFactory.createAspectAnnotation(perClause.toDeclarationString(),
				declarationSourceStart);
		Annotation privilegedAnnotation = null;
		if (isPrivileged)
			privilegedAnnotation = AtAspectJAnnotationFactory.createPrivilegedAnnotation(declarationSourceStart);
		Annotation[] toAdd = new Annotation[isPrivileged ? 2 : 1];
		toAdd[0] = atAspectAnnotation;
		if (isPrivileged)
			toAdd[1] = privilegedAnnotation;
		if (annotations == null) {
			annotations = toAdd;
		} else {
			Annotation[] old = annotations;
			annotations = new Annotation[annotations.length + toAdd.length];
			System.arraycopy(old, 0, annotations, 0, old.length);
			System.arraycopy(toAdd, 0, annotations, old.length, toAdd.length);
		}
	}

	public void generateCode(ClassFile enclosingClassFile) {
		if (ignoreFurtherInvestigation) {
			if (binding == null)
				return;
			ClassFile.createProblemType(this, scope.referenceCompilationUnit().compilationResult);
			return;
		}

		// make me and my binding public
		this.modifiers = AstUtil.makePublic(this.modifiers);
		this.binding.modifiers = AstUtil.makePublic(this.binding.modifiers);

		if (!isAbstract()) {
			StaticPerscope staticPerscope = perClause.getStaticPerscope();
			staticPerscope.generateMethodBodies(this, typeX);
		}

		if (EclipseFactory.DEBUG)
			System.out.println(toString());

		super.generateCode(enclosingClassFile);
	}

	public boolean needClassInitMethod() {
		return true;
	}

	protected void generateAttributes(ClassFile classFile) {
		if (!isAbstract())
			generatePerSupportMembers(classFile);

		generateInlineAccessMembers(classFile);

		addVersionAttributeIfNecessary(classFile);

		classFile.extraAttributes.add(new EclipseAttributeAdapter(new AjAttribute.Aspect(perClause)));

		if (binding.privilegedHandler != null) {
			ResolvedMember[] members = ((PrivilegedHandler) binding.privilegedHandler).getMembers();
			classFile.extraAttributes.add(new EclipseAttributeAdapter(new AjAttribute.PrivilegedAttribute(members)));
		}

		// XXX need to get this attribute on anyone with a pointcut for good errors
		classFile.extraAttributes.add(new EclipseAttributeAdapter(new AjAttribute.SourceContextAttribute(new String(
				compilationResult().getFileName()), compilationResult().lineSeparatorPositions)));

		super.generateAttributes(classFile);
	}

	/**
	 * A pointcut might have already added the attribute, let's not add it again.
	 */
	private void addVersionAttributeIfNecessary(ClassFile classFile) {
		for (Iterator iter = classFile.extraAttributes.iterator(); iter.hasNext();) {
			EclipseAttributeAdapter element = (EclipseAttributeAdapter) iter.next();
			if (CharOperation.equals(element.getNameChars(), weaverVersionChars))
				return;
		}
		classFile.extraAttributes.add(new EclipseAttributeAdapter(new AjAttribute.WeaverVersionInfo()));
	}

	private static char[] weaverVersionChars = "org.aspectj.weaver.WeaverVersion".toCharArray();
	private String aspectsStorageClassName;
	public UnresolvedType aspectsStorageType;

	private void generateInlineAccessMembers(ClassFile classFile) {
		for (Iterator i = superAccessForInline.values().iterator(); i.hasNext();) {
			AccessForInlineVisitor.SuperAccessMethodPair pair = (AccessForInlineVisitor.SuperAccessMethodPair) i.next();
			generateSuperAccessMethod(classFile, pair.accessMethod, pair.originalMethod);
		}
		for (Iterator i = accessForInline.entrySet().iterator(); i.hasNext();) {
			Map.Entry e = (Map.Entry) i.next();
			generateInlineAccessMethod(classFile, (Binding) e.getValue(), (ResolvedMember) e.getKey());
		}
	}

	private void generatePerSupportMembers(ClassFile classFile) {
		if (isAbstract())
			return;

		// XXX otherwise we need to have this (error handling?)
		if (perClause == null) {
			System.err.println("has null perClause: " + this);
			return;
		}

		// EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);

		StaticPerscope perScopeRelated = perClause.getStaticPerscope();
		perScopeRelated.generateMethodAttributes(classFile, this, typeX);		
	}

	/*private static interface BodyGenerator {
		public void generate(CodeStream codeStream);
	}
*/
	public void generateMethod(ClassFile classFile, ResolvedMember member, BodyGenerator gen) {
		final EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		generateMethod(classFile, world.makeMethodBinding(member), gen);
	}

	public void generateMethod(ClassFile classFile, MethodBinding methodBinding, BodyGenerator gen) {
		generateMethod(classFile, methodBinding, null, gen);
	}

	protected List makeEffectiveSignatureAttribute(ResolvedMember sig, Shadow.Kind kind, boolean weaveBody) {
		List l = new ArrayList(1);
		l.add(new EclipseAttributeAdapter(new AjAttribute.EffectiveSignatureAttribute(sig, kind, weaveBody)));
		return l;
	}

	/*
	 * additionalAttributes allows us to pass some optional attributes we want to attach to the method we generate. Currently this
	 * is used for inline accessor methods that have been generated to allow private field references or private method calls to be
	 * inlined (PR71377). In these cases the optional attribute is an effective signature attribute which means calls to these
	 * methods are able to masquerade as any join point (a field set, field get or method call). The effective signature attribute
	 * is 'unwrapped' in BcelClassWeaver.matchInvokeInstruction()
	 */
	private void generateMethod(ClassFile classFile, MethodBinding methodBinding, List additionalAttributes/*
																											 * ResolvedMember
																											 * realMember
																											 */, BodyGenerator gen) {
		// EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(this.scope);
		classFile.generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = classFile.contentsOffset;

		int attributeNumber;
		if (additionalAttributes != null) { // mini optimization
			List attrs = new ArrayList();
			attrs.addAll(AstUtil.getAjSyntheticAttribute());
			attrs.addAll(additionalAttributes);
			attributeNumber = classFile.generateMethodInfoAttribute(methodBinding, false, attrs);
		} else {
			attributeNumber = classFile.generateMethodInfoAttribute(methodBinding, false, AstUtil.getAjSyntheticAttribute());
		}

		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;

		// Use reset() rather than init()
		// XXX We need a scope to keep reset happy, initializerScope is *not* the right one, but it works !
		// codeStream.init(classFile);
		// codeStream.initializeMaxLocals(methodBinding);
		MethodDeclaration md = AstUtil.makeMethodDeclaration(methodBinding);
		md.scope = initializerScope;
		codeStream.reset(md, classFile);
		// body starts here
		gen.generate(codeStream);
		// body ends here
		if (codeStream.pcToSourceMapSize == 0)
			codeStream.recordPositionsFrom(0, 1);
		boolean b = ((codeStream.generateAttributes & ClassFileConstants.ATTR_VARS) != 0 ? true : false); // pr148693
		if (codeStream.maxLocals == 0) {
			codeStream.generateAttributes &= ~ClassFileConstants.ATTR_VARS;
		}
		classFile.completeCodeAttribute(codeAttributeOffset);
		if (b)
			codeStream.generateAttributes |= ClassFileConstants.ATTR_VARS;

		attributeNumber++;
		classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);
	}


	private void generateSuperAccessMethod(ClassFile classFile, final MethodBinding accessMethod, final ResolvedMember method) {
		generateMethod(classFile, accessMethod, new BodyGenerator() {
			public void generate(CodeStream codeStream) {
				// body starts here
				codeStream.aload_0();
				AstUtil.generateParameterLoads(accessMethod.parameters, codeStream, 1);
				codeStream.invokespecial(factory.makeMethodBinding(method));
				AstUtil.generateReturn(accessMethod.returnType, codeStream);
				// body ends here
			}
		});

	}

	private void generateInlineAccessMethod(ClassFile classFile, final Binding binding, final ResolvedMember member) {
		if (binding instanceof InlineAccessFieldBinding) {
			generateInlineAccessors(classFile, (InlineAccessFieldBinding) binding, member);
		} else {
			generateInlineAccessMethod(classFile, (MethodBinding) binding, member);
		}
	}

	private void generateInlineAccessors(ClassFile classFile, final InlineAccessFieldBinding accessField, final ResolvedMember field) {
		final FieldBinding fieldBinding = factory.makeFieldBinding(field);

		generateMethod(classFile, accessField.reader, makeEffectiveSignatureAttribute(field, Shadow.FieldGet, false),
				new BodyGenerator() {
					public void generate(CodeStream codeStream) {
						// body starts here
						if (field.isStatic()) {
							codeStream.getstatic(fieldBinding);
						} else {
							codeStream.aload_0();
							codeStream.getfield(fieldBinding);
						}

						AstUtil.generateReturn(accessField.reader.returnType, codeStream);
						// body ends here
					}
				});

		generateMethod(classFile, accessField.writer, makeEffectiveSignatureAttribute(field, Shadow.FieldSet, false),
				new BodyGenerator() {
					public void generate(CodeStream codeStream) {
						// body starts here
						if (field.isStatic()) {
							codeStream.load(fieldBinding.type, 0);
							codeStream.putstatic(fieldBinding);
						} else {
							codeStream.aload_0();
							codeStream.load(fieldBinding.type, 1);
							codeStream.putfield(fieldBinding);
						}

						codeStream.return_();
						// body ends here
					}
				});

	}

	private void generateInlineAccessMethod(ClassFile classFile, final MethodBinding accessMethod, final ResolvedMember method) {
		generateMethod(classFile, accessMethod, makeEffectiveSignatureAttribute(method, Shadow.MethodCall, false),
				new BodyGenerator() {
					public void generate(CodeStream codeStream) {
						// body starts here

						AstUtil.generateParameterLoads(accessMethod.parameters, codeStream);

						if (method.isStatic()) {
							codeStream.invokestatic(factory.makeMethodBinding(method));
						} else {
							codeStream.invokevirtual(factory.makeMethodBinding(method));
						}

						AstUtil.generateReturn(accessMethod.returnType, codeStream);
						// body ends here
					}
				});
	}

	private PerClause.Kind lookupPerClauseKind(ReferenceBinding binding) {
		PerClause perClause;
		if (binding instanceof BinaryTypeBinding) {
			ResolvedType superTypeX = factory.fromEclipse(binding);
			perClause = superTypeX.getPerClause();
		} else if (binding instanceof SourceTypeBinding) {
			SourceTypeBinding sourceSc = (SourceTypeBinding) binding;
			if (sourceSc.scope.referenceContext instanceof AspectDeclaration) {
				perClause = ((AspectDeclaration) sourceSc.scope.referenceContext).perClause;
			} else {
				return null;
			}
		} else if (binding instanceof ParameterizedTypeBinding) {
			ParameterizedTypeBinding pBinding = (ParameterizedTypeBinding) binding;
			if (pBinding.type instanceof SourceTypeBinding) {
				SourceTypeBinding sourceSc = (SourceTypeBinding) pBinding.type;
				if (sourceSc.scope != null && sourceSc.scope.referenceContext instanceof AspectDeclaration) {
					perClause = ((AspectDeclaration) sourceSc.scope.referenceContext).perClause;
				} else {
					return null;
				}
			} else {
				perClause = null;
			}
		} else {
			// XXX need to handle this too
			return null;
		}
		if (perClause == null) {
			return lookupPerClauseKind(binding.superclass());
		} else {
			return perClause.getKind();
		}
	}

	private PerClause defaultPerscope() {
		StaticPerscopeParse defaultWeaving = new Persingleton();
		PerClause defaultPerclause = new PerSingleton();
		defaultPerclause.setStaticPerscope(defaultWeaving);
		return defaultPerclause;
	}
	
	private void buildPerClause(ClassScope scope) {
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(scope);

		if (perClause == null) {
			PerClause.Kind kind = lookupPerClauseKind(binding.superclass);
			if (kind == null) {
				perClause = defaultPerscope();
			} else {
				perClause = new PerFromSuper(kind);
			}
		}

		// aspectAttribute = new AjAttribute.Aspect(perClause);

		if (ignoreFurtherInvestigation)
			return; // ???

		if (!isAbstract()) {
			StaticPerscope perScopeRelated = perClause.getStaticPerscope();
			System.out.println("%%%%%% perScopeRelated = " + perScopeRelated);
			perScopeRelated.instantiateMethodObjects(typeX, scope, binding);

			
		}
		resolvePerClause(); // XXX might be too soon for some error checking
	}

	private PerClause resolvePerClause() {
		EclipseScope iscope = new EclipseScope(new FormalBinding[0], scope);
		perClause.resolve(iscope);
		return perClause;
	}

	public void buildInterTypeAndPerClause(ClassScope classScope) {
		factory = EclipseFactory.fromScopeLookupEnvironment(scope);
		if (isPrivileged) {
			binding.privilegedHandler = new PrivilegedHandler(this);
		}

		checkSpec(classScope);
		if (ignoreFurtherInvestigation)
			return;

		buildPerClause(scope);

		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				if (methods[i] instanceof InterTypeDeclaration) {
					EclipseTypeMunger m = ((InterTypeDeclaration) methods[i]).build(classScope);
					if (m != null)
						concreteName.typeMungers.add(m);
				} else if (methods[i] instanceof DeclareDeclaration) {
					Declare d = ((DeclareDeclaration) methods[i]).build(classScope);
					if (d != null)
						concreteName.declares.add(d);
				}
			}
		}

		concreteName.getDeclaredPointcuts();
	}

	// public String toString(int tab) {
	// return tabString(tab) + toStringHeader() + toStringBody(tab);
	// }
	//
	// public String toStringBody(int tab) {
	//
	//		String s = " {"; //$NON-NLS-1$
	//		
	//
	// if (memberTypes != null) {
	// for (int i = 0; i < memberTypes.length; i++) {
	// if (memberTypes[i] != null) {
	//					s += "\n" + memberTypes[i].toString(tab + 1); //$NON-NLS-1$
	// }
	// }
	// }
	// if (fields != null) {
	// for (int fieldI = 0; fieldI < fields.length; fieldI++) {
	// if (fields[fieldI] != null) {
	//					s += "\n" + fields[fieldI].toString(tab + 1); //$NON-NLS-1$
	// if (fields[fieldI].isField())
	//						s += ";"; //$NON-NLS-1$
	// }
	// }
	// }
	// if (methods != null) {
	// for (int i = 0; i < methods.length; i++) {
	// if (methods[i] != null) {
	//					s += "\n" + methods[i].toString(tab + 1); //$NON-NLS-1$
	// }
	// }
	// }
	//		s += "\n" + tabString(tab) + "}"; //$NON-NLS-2$ //$NON-NLS-1$
	// return s;
	// }

	public StringBuffer printHeader(int indent, StringBuffer output) {
		// since all aspects are made public we want to print the
		// modifiers that were supplied in the original source code
		printModifiers(this.declaredModifiers, output);
		output.append("aspect ");
		output.append(name);
		if (superclass != null) {
			output.append(" extends "); //$NON-NLS-1$
			superclass.print(0, output);
		}
		if (superInterfaces != null && superInterfaces.length > 0) {
			output.append((TypeDeclaration.kind(this.modifiers) == TypeDeclaration.INTERFACE_DECL) ? " extends " : " implements ");//$NON-NLS-2$ //$NON-NLS-1$
			for (int i = 0; i < superInterfaces.length; i++) {
				if (i > 0)
					output.append(", "); //$NON-NLS-1$
				superInterfaces[i].print(0, output);
			}
		}
		return output;
		// XXX we should append the per-clause
	}

	/**
	 * All aspects are made public after type checking etc. and before generating code (so that the advice can be called!). This
	 * method returns the modifiers as specified in the original source code declaration so that the structure model sees the right
	 * thing.
	 */
	public int getDeclaredModifiers() {
		return declaredModifiers;
	}
}
