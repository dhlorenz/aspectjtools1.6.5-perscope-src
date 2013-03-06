import junit.framework.TestCase;


public class CachingTest extends TestCase {

	public void testCaching() {
		ProviderClient c = new ProviderClient(new DataProvider(100));
		Integer[] firstTime = c.getExpensiveValues(4);
		Integer[] secondTime = c.getExpensiveValues(4);
		assertEquals("cached in tx",firstTime[0],firstTime[1]);
		System.out.println("cached in tx");
		assertEquals("cached in tx",secondTime[0],secondTime[1]);
		System.out.println("cached in tx");
		assertTrue("not cached across tx",
				firstTime[0].intValue() != secondTime[0].intValue());
		System.out.println("not cached across tx");
	}
	
	public static void main(String[] args) throws Exception {
		CachingTest test = new CachingTest();
		test.setUp();
		test.testCaching();
	}

}
