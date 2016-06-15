package cpath.query;


import cpath.client.CPathClient;
import org.springframework.util.MultiValueMap;

/**
 * A query to be executed with {@link CPathClient}
 * 
 * @author rodche
 */
abstract class BaseCPathQuery<T> {

	protected CPathClient client;
	protected String command;

	/**
	 * Builds the query parameters object.
	 * 
	 * @return
	 */
	protected abstract MultiValueMap<String, String> getRequestParams();
	
	/**
	 * @return the web service command (after the endpoint base URL before parameters)
	 */
	public String getCommand() {
		return command;
	}

	public void setClient(CPathClient client) {
		this.client = client;
	}

}
