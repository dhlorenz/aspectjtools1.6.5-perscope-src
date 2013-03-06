import java.util.Map;

/**
 * @author adrian at aspectprogrammer.org
 * A simple caching aspect with configurable cache implementation.
 * Sub-aspects simply specify the cachedOperation pointcut.
 */
public abstract aspect SimpleCaching {

	private Map cache;
	
	/**
	 * Use the Map interface as a good approximation to a 
	 * cache for this example.
	 * The cache can be provided to the aspect via e.g. dependency
	 * injection.
	 */
	public void setCache(Map cache) { this.cache = cache; }
	
	/**
	 * We don't know what to cache (that's why this is an abstract
	 * aspect), but we know we need a key to index the cached values
	 * by.
	 */
	abstract pointcut cachedOperation(Object key);
	
	/**
	 * This advice implements the actual caching of values and
	 * cache lookups.
	 */
	Object around(Object key) : cachedOperation(key) {
		Object ret;
		if (cache.containsKey(key)) {
			ret = cache.get(key);
		} else {
			ret = proceed(key);
			cache.put(key,ret);
		}
		return ret;
	}
}
