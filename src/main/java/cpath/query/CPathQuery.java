/**
 * 
 */
package cpath.query;

import cpath.client.util.CPathException;
import cpath.service.OutputFormat;

/**
 * Basic interface to develop specific 
 * and convenient query classes to be  
 * executed with the cPath2 client. 
 * 
 * @author rodche
 */
public interface CPathQuery<T> {
	
	/**
	 * Sends the query to the service and receives the result as string.
	 * @return
	 * @throws CPathException 
	 */
	String stringResult(OutputFormat outputFormat) throws CPathException;

	
	/**
	 * Sends the query to the service and receives the result.
	 * @return
	 */
	T result() throws CPathException;
}
