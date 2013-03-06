/**
 * @author adrian at aspectprogrammer.org
 * Illustrates use of the SimpleCaching library aspect to provide
 * a client-side data-provider specific cache.
 * (Use execution in place of call in the cachedOperation pcut definition
 * for a server-side cache).
 */
public aspect DataProviderCaching 
	extends SimpleCaching pertarget(cachedOperation(Object)){

}