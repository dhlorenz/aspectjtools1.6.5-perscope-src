/**
 * @author adrian at aspectprogrammer.org
 * Illustrates use of the SimpleCaching library aspect to provide
 * a client-side data-provider specific cache.
 * (Use execution in place of call in the cachedOperation pcut definition
 * for a server-side cache).
 */
public aspect DataProviderCaching 
extends SimpleCaching 
percope(aspectStorageClass=org.aspectj.ajdt.internal.compiler.ast.perscope.storage.Pertarget,cachedOperation(Object)){

	pointcut cachedOperation(Object key) :
		call(int DataProvider.expensiveOperation(int)) 
		&& args(key);
	
	/**
	 * this constructor is here simply to facilitate testing all
	 * of the different cache implementations without having the
	 * test case depend on any one. Normally this dependency would
	 * be set from outside of the aspect (e.g. by Spring).
	 */
	public DataProviderCaching() {
		setCache(new java.util.HashMap());
	}
}