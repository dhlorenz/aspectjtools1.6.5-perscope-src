import junit.framework.TestCase;

/**
 * @author adrian at aspectprogrammer.org
 * Verify that the cache is doing its job.
 */
public class CachingTest extends TestCase {

	private DataProvider provider;
	
	public void testExpensiveOperationCache() {
		long start100 = System.currentTimeMillis();
		int op100 = provider.expensiveOperation(100);
		long stop100 = System.currentTimeMillis();
		
		long start100v2 = System.currentTimeMillis();
		int op100v2 = provider.expensiveOperation(100);
		long stop100v2 = System.currentTimeMillis();
		
		long expectedSpeedUp = 500; // expect at least 0.5s quicker with cache
		
		assertTrue("caching speeds up return (100)",
				    ((stop100 - start100) - (stop100v2 - start100v2)) 
					>= expectedSpeedUp);
		System.out.println("caching speeds up return (100)");
		assertEquals("cache returns correct value(100)",op100,op100v2);
		System.out.println("cache returns correct value(100)");
	}
	
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		provider = new DataProvider(100);
	}

	public static void main(String[] args) throws Exception {
		CachingTest test = new CachingTest();
		test.setUp();
		test.testExpensiveOperationCache();
	}
}
