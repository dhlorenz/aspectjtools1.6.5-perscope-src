/**
 * @author adrian at aspectprogrammer.org
 * Illustrates use of SimpleCaching library aspect to give a 
 * cache that returns a constant value for the duration of a 
 * given "transaction" (method execution control flow).
 */
public aspect PerTransactionCaching 
extends SimpleCaching 
perscope (aspectWeavingClass=org.aspectj.ajdt.internal.compiler.ast.perscope.Percflow,transaction()){
  
	pointcut transaction() : execution(* ProviderClient.getExpensiveValues(int));
	
	pointcut cachedOperation(Object key) :
		call(Integer DataProvider.getExpensiveToComputeValue(int)) 
		&& args(key);
	
	/**
	 * this constructor is here simply to facilitate testing all
	 * of the different cache implementations without having the
	 * test case depend on any one. Normally this dependency would
	 * be set from outside of the aspect (e.g. by Spring).
	 */
	public PerTransactionCaching() {
		setCache(new java.util.HashMap());
	}
}
