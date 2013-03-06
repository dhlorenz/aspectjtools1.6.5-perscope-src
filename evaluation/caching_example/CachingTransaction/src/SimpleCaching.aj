import java.util.HashMap;
import java.util.Map;

/**
 * @author adrian at aspectprogrammer.org
 * A simple caching aspect with configurable cache implementation.
 * Sub-aspects simply specify the cachedOperation pointcut.
 */
public abstract aspect SimpleCaching {

	private Map<Object, Object> cache = new HashMap<Object, Object>();
	
	pointcut cachedOperation(Object key) :
		call(int DataProvider.expensiveOperation(int)) 
		&& args(key);
	
	/**
	 * This advice implements the actual caching of values and
	 * cache lookups.
	 */
	Object around(Object key) : cachedOperation(key) {
		System.out.println("SimpleCaching key = " + key + " this = " + this);
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
