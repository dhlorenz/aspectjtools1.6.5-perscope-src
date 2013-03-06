/**
 * @author adrian at aspectprogrammer.org
 * Illustrates use of SimpleCaching library aspect to implement a 
 * (client-side) per-client cache.
 */
public aspect IndividualClientCaching 
extends SimpleCaching 
perscope(aspectStorageClass=org.aspectj.ajdt.internal.compiler.ast.perscope.storage.Perthis,cachedOperation(Object)) {

	pointcut cachedOperation(Object key) :
		call(int DataProvider.expensiveOperation(int)) 
		&& args(key);
	
	/**
	 * this constructor is here simply to facilitate testing all
	 * of the different cache implementations without having the
	 * test case depend on any one. Normally this dependency would
	 * be set from outside of the aspect (e.g. by Spring).
	 */
	public IndividualClientCaching() {
		setCache(new java.util.HashMap());
	}
}
