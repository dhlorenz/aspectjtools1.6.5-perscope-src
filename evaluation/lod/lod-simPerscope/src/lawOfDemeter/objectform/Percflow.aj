package lawOfDemeter.objectform;
import lawOfDemeter.Any;


/**
 * @authors David H. Lorenz and Pengcheng Wu
 * @version 0.8, 12/19/02
 */
/*** THIS MUST BE LINE 9 ***/
aspect Percflow extends ObjectSupplier 
perscope (aspectWeavingClass=org.aspectj.ajdt.internal.compiler.ast.perscope.PerCflowAspectWeaving,Any.Execution()|| Any.Initialization()){
  before(): Any.Execution() {
      addValue(thisJoinPoint.getThis());
      addAll(thisJoinPoint.getArgs());
  }
  after() returning (Object result):
    Any.SelfCall(Object,Object) || Any.StaticCall()
    || Any.ConstructorCall() {
      addValue(result);
  }
}
