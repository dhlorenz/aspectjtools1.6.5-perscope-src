/**
 * @author adrian at aspectprogrammer.org
 *
 * A mock data providing class that can be used to illustrate caching 
 * techniques using AspectJ.
 */
public class DataProvider {

	private int multiplicationFactor = 0;
	private int expensiveToComputeValue = 0;
	
	public DataProvider(int seed) { 
		multiplicationFactor = seed; 
		expensiveToComputeValue = seed;
	}
	
	/**
	 * expensiveOperation is a true function (it always
	 * returns the same output value for a given input 
	 * value), but takes a long time to compute the 
	 * answer.
	 */
	public int expensiveOperation(int x) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {}
		return x * multiplicationFactor;
	}

	/**
	 * The expensive to compute value is different each
	 * time you ask for it. It also takes a long time to
	 * compute.
	 */
	public Integer getExpensiveToComputeValue(int x) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {}
		return new Integer(x + expensiveToComputeValue++);	
	}
}
