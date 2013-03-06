/**
 * @author adrian at aspectprogrammer.org
 * Illustrates use of SimpleCaching library aspect to give a 
 * cache that returns a constant value for the duration of a 
 * given "transaction" (method execution control flow).
 */
	public aspect PerTransactionCaching 
	extends SimpleCaching percflow(transaction()){
	  
		pointcut transaction() : execution(* ProviderClient.getExpensiveValues(int));
		
		
	}
