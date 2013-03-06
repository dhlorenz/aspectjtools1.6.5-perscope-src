/**
 * @author adrian at aspectprogrammer.org
 * Illustrates use of SimpleCaching library aspect to implement a 
 * (client-side) per-client cache.
 */
public aspect IndividualClientExpensiveOperationCaching 
extends SimpleCaching perthis(cachedOperation(Object)) {

	pointcut cachedOperation(Object key) :
		call(int DataProvider.expensiveOperation(int)) 
		&& args(key);
	
}
