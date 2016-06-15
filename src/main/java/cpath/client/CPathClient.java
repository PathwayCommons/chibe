package cpath.client;

import cpath.client.util.BioPAXHttpMessageConverter;
import cpath.client.util.CPathException;
import cpath.client.util.ServiceResponseHttpMessageConverter;
import cpath.query.*;
import cpath.service.jaxb.Help;
import org.apache.commons.lang.StringUtils;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

//import cpath.service.jaxb.*;


/**
 * New stateless cPath2 client with create* 
 * methods to conveniently build and run queries.
 *
 * Not quite thread-safe (due to using SimpleIOHandler),
 * especially if (BioPAX) Model is the response type
 * in #post or #get method calls.
 */
public class CPathClient
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CPathClient.class);
	
	// one can set the JVM property: -DcPath2Url="http://some_URL"
	public static final String JVM_PROPERTY_ENDPOINT_URL = "cPath2Url";
	public static final String DEFAULT_ENDPOINT_URL = "http://www.pathwaycommons.org/pc2/";

	private RestTemplate restTemplate;
	private String endPointURL; //official external public cpath2 web service url
	private String name;

	public static enum Direction
    {
		UPSTREAM, DOWNSTREAM, BOTHSTREAM, UNDIRECTED;
    }

	// suppress using constructors in favor of static factories
    private CPathClient() {
    }
    
    
    /**
     * Instantiates the client using the default endpoint URL.
     */
    public static CPathClient newInstance() {
    	return newInstance(null);
    }
    
    
    /**
     * Instantiates the client using the cpath2 endpoint URL.
     * 
     * @param url cpath2 web service endpoint URL or null (to use defaults)
     */
    public static CPathClient newInstance(String url) {
    	CPathClient client = new CPathClient();
		// create a new REST template
		client.restTemplate = new RestTemplate();
     	// add custom cPath2 XML message converter as the first one (accepts 'application/xml' content type)
    	// because one of existing/default msg converters, XML root element based jaxb2, does not work for ServiceResponce types...
    	client.restTemplate.getMessageConverters().add(0, new ServiceResponseHttpMessageConverter());
    	// add BioPAX http message converter
		// (SimpleIOHandler is not thread-safe; so we cannot share the thread-safe restTemplate)
        client.restTemplate.getMessageConverters().add(1, new BioPAXHttpMessageConverter(new SimpleIOHandler()));
        // set the cpath2 server URL (or default one or from the java option)
    	if(url == null || url.isEmpty())
			client.endPointURL = System.getProperty(JVM_PROPERTY_ENDPOINT_URL, DEFAULT_ENDPOINT_URL);
		else
			client.endPointURL = url;
    	
//    	client.updateActualEndPointURL();

    	return client;
    }
    
	
	/**
	 * Sends a HTTP POST (preferred, more reliable with complex queries) 
	 * request to the server.
	 * 
	 * @param requestPath cpath2 web service command path (e.g., search, help/types, etc.)
	 * @param requestParams query parameters object.
	 * @param responseType result class (e.g., String.class, Model)
	 * @return
	 * @throws CPathException
	 */
	public <T> T post(String requestPath, MultiValueMap<String, String> requestParams, Class<T> responseType)
		throws CPathException
	{
		final String url = endPointURL + requestPath;
		
		if(name != null && requestParams != null)
			requestParams.put("client", Collections.singletonList(name));
		
		try {
			return restTemplate.postForObject(url, requestParams, responseType);
//		} catch (UnknownHttpStatusCodeException e) {
//			if (e.getRawStatusCode() == 460) {
//				return null; //empty result
//			} else
//				throw new CPathException(url + " and " + requestParams, e);
		} catch (RestClientException e) {
			throw new CPathException(url + " and " + requestParams, e);
		}
	}
    
	
	/**
	 * Sends a HTTP GET request to the cpath2 server.
	 * 
	 * Note: using {@link #post(String, MultiValueMap, Class)} is the preferred
	 * and more reliable method, especially with complex queries that use URIs or
	 * Lucene syntax.
	 * 
	 * @param requestPath cpath2 web service command path (e.g., search, help/types, etc.)
	 * @param requestParams query parameters map
	 * @param responseType result class (e.g., String.class, Model)
	 * @return
	 * @throws CPathException
	 */
	public <T> T get(String requestPath, MultiValueMap<String, String> requestParams, Class<T> responseType)
		throws CPathException
	{	
		StringBuilder sb = new StringBuilder(endPointURL);
		sb.append(requestPath);
		
		if(requestParams != null) {
			sb.append("?");
			for (Entry<String, List<String>> entry : requestParams.entrySet()) {
				String params = join(entry.getKey() + "=", entry.getValue(), "&");
				sb.append(params).append("&");
			}
			if(name!=null)
				sb.append("client=").append(name);
		}
		
		String url = sb.toString();
		
		try {
			return restTemplate.getForObject(url, responseType);
		} catch (UnknownHttpStatusCodeException e) {
			if (e.getRawStatusCode() == 460) {
				return null; //empty result
			} else
				throw new CPathException(url, e);
		} catch (RestClientException e) {
			throw new CPathException(url, e);
		}
	}

	/**
	 * Retrieves information about available cPath2 commands and their parameters.
	 *
	 * @param hpath relative (ie., as in 'help/[path]') REST query path variable; e.g.: null (all), "datasources", "commands/search", etc.
	 *
	 * @return
	 * @throws CPathException
	 */
	public Help executeHelp(String hpath) throws CPathException {
		return get("help/" + ((hpath != null) ? hpath : ""), null, Help.class);
	}

	/**
     * Joins the collection of strings into one string 
     * using the prefix and delimiter.
     * 
     * @param prefix 
     * @param strings
     * @param delimiter
     * @return
     */
    private String join(String prefix, Collection<String> strings, String delimiter) {
        List<String> prefixed = new ArrayList<String>();

       	for(String s: strings)
       		prefixed.add(prefix + s);

        return StringUtils.join(prefixed, delimiter);
    }
    
 
    /**
     * cPath2 Web Service URL.
     * 
     * @return the cpath2 end point URL as a string
     */
    public String getEndPointURL() {
        return endPointURL;
    }
    
    
    /**
     * Actual cPath2 Web Service URL that is 
     * resolved from the {@link #endPointURL} by
     * following HTTP (302, 301) redirects.
     * 
     * @return the resolved cpath2 end point URL
	 * @deprecated actual url is the same (it won't try to resolve through 30x redirects anymore...)
     */
    public String getActualEndPointURL() {
        return endPointURL;
    }

    
    /**
     * Sets the web service URL and then
     * finds actual resource location (after HTTP redirects)
     * for sending future HTTP requests.
     * 
     * @see #getEndPointURL()
     * @see #getActualEndPointURL()
     * @param url a cPath2 web service URL
	 * @deprecated - will be made private or removed soon; please use #newInstance.
     */
    public void setEndPointUrlAndRedirect(final String url) {
        this.endPointURL = url;
//        updateActualEndPointURL();
    }

//	// this (using actualEndPointURL) is required for POST queries to work through proxy/redirects on the way...
//	private void updateActualEndPointURL() {
//		actualEndPointURL = endPointURL; //initially, it's the same
//		// query for the location using previous/initial one until both are the same:
//		int i=0;
//		for(URI loc = URI.create(endPointURL); loc != null && i<5; i++ )
//		{
//			loc = restTemplate.postForLocation(loc, null);
//
//			if(loc != null)
//				actualEndPointURL = loc.toString();
//
//			LOGGER.info("Location: " + loc);
//		}
//	}
	
	/**
	 * Creates a new full-text search query object
	 * (e.g., call as cli.createSearchQuery().queryString("BRCA*")
	 * .typeFilter(Pathway.class).dataSourceFilter("reactome").result();)
	 * 
	 * @return
	 */
	public CPathSearchQuery createSearchQuery() {
		return new CPathSearchQuery(this);
	}
	
	/**
	 * Creates a new biopax graph properties traverse query 
	 * (e.g., call as cli.createTraverseQuery().source(..).propertyPath(..).result();)
	 * 
	 * @return
	 */
	public CPathTraverseQuery createTraverseQuery() {
		return new CPathTraverseQuery(this);
	}
	
	/**
	 * Creates a new advanced biopax graph query 
	 * to calculate and fetch a biopax sub-model from the web service
	 * (e.g., call as cli.createGraphQuery().kind(k).limit(n).source(srcs).result();)
	 * 
	 * @return
	 */
	public CPathGraphQuery createGraphQuery() {
		return new CPathGraphQuery(this);
	}
	
	/**
	 * Creates a new get-by-id (or by URI) query to 
	 * fetch a biopax sub-model from the web service
	 * (e.g., call as model = cli.createGetQuery().ids(..).result();)
	 * 
	 * @return
	 */
	public CPathGetQuery createGetQuery() {
		return new CPathGetQuery(this);
	}
		
	/**
	 * Creates a new "top pathways" query object
	 * (e.g., call as cli.createTopPathwaysQuery()
	 * 					 .dataSourceFilter("reactome")
	 * 					 .result();
	 * )
	 * 
	 * @return
	 */
	public CPathTopPathwaysQuery createTopPathwaysQuery() {
		return new CPathTopPathwaysQuery(this);
	}


	/**
	 * Gives a name to this cpath2 client instance
	 * to be reported to the server with all requests. 
	 * Developers can optionally use this to help the cpath2 
	 * server analyze - in addition to where requests come from (IP address)
	 * and what they are (command, parameters) - also how often it's 
	 * from a particular class of client app (though the server 
	 * can safely ignore this information, it might be also useful 
	 * and practical to report some statistics back to authors.)
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
}
