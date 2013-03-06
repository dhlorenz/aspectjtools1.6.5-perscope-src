public class ProviderClient {

	DataProvider provider;
	
	public ProviderClient(DataProvider p) {
		this.provider = p;
	}
	
	public Integer[] getExpensiveValues(int x) {
		Integer a = getIt(x);
		Integer b = getIt(x);
		return new Integer[] {a,b};
	}
	
	private Integer getIt(int x) {
		return provider.getExpensiveToComputeValue(x);
	}
}
