/**
 * 
 */
package cpath.query;

import cpath.client.CPathClient;
import cpath.client.util.CPathException;
import cpath.service.Cmd;
import cpath.service.CmdArgs;
import cpath.service.OutputFormat;
import cpath.service.jaxb.SearchResponse;
import org.apache.commons.lang.ArrayUtils;
import org.biopax.paxtools.model.BioPAXElement;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Collection;

/**
 * A full-text search query to be executed with {@link CPathClient}
 * on the cpath2 server's biopax database.
 * 
 * @author rodche
 */
public final class CPathSearchQuery extends BaseCPathQuery<SearchResponse> implements CPathQuery<SearchResponse>
{

	// these are for 'search' queries
	private String queryString;
	private String type; // filter by
	private String[] organism; // filter by
	private String[] datasource; // filter by
	private Integer page = 0; // search hits page #
	private boolean multi = false; //if true, and page>0 - all hits on pages 0..page; if true and page<0 - all hits (all pages).
	
	private static final int ALL_PAGES = -13; // a special value <0 for the page field

	protected MultiValueMap<String, String> getRequestParams() {
		MultiValueMap<String, String> request = new LinkedMultiValueMap<String, String>();
		if(queryString == null || queryString.isEmpty())
			throw new IllegalArgumentException("'queryString' parameter is required.");
		request.add(CmdArgs.q.name(), queryString);
		if(type != null)
			request.add(CmdArgs.type.name(), type);
		if(page != null && page > 0)
			request.add(CmdArgs.page.name(), page.toString());
		if(organism != null)
			request.put(CmdArgs.organism.name(), Arrays.asList(organism));
		if(datasource != null)
			request.put(CmdArgs.datasource.name(), Arrays.asList(datasource));
		return request;
	}

	/**
	 * Constructor.
	 * 
	 * @param client instance of the cpath2 client
	 */
	public CPathSearchQuery(CPathClient client) {
		this.client = client;
		this.command = Cmd.SEARCH.toString();
		page = 0;
		multi = false;
	}


	/**
	 * Sets the keyword(s) to match; one can also use Lucene query syntax. 
	 * 
	 * @param queryString
	 * @return
	 */
	public CPathSearchQuery queryString(String queryString) {
		this.queryString = queryString;
		return this;
	}

	/**
	 * Sets the filter by BioPAX class
	 * 
	 * @param type instantiable BioPAX L3 class (e.g., Pathway, ProteinReference, etc.).
	 * @return
	 */
	public CPathSearchQuery typeFilter(Class<? extends BioPAXElement> type) {
		this.type = type.getSimpleName();
		return this;
	}
	
	/**
	 * Sets the filter by BioPAX class
	 * 
	 * @param type name of an instantiable BioPAX L3 type (e.g., Pathway, ProteinReference, etc.).
	 * @return
	 */
	public CPathSearchQuery typeFilter(String type) {
		this.type = type;
		return this;
	}

	/**
	 * The search results page number to retrieve, if there're more than one.
	 * (If a query was not quite specific, there are too many matches, 
	 * which are not returned all at once but pagination is used instead; 
	 * the greater page number, the less relevant hits are out there)
	 * 
	 * @param page number; the default is 0 (top hits)
	 * @return
	 * @throws IllegalArgumentException when page < 0
	 */
	public CPathSearchQuery page(int page) {
		if (page < 0)
			throw new IllegalArgumentException(
					"Negative page number");

		this.page = page;
		this.multi = false;

		return this;
	}

	/**
	 * Set to request all hits (all result pages).
	 * 
	 * @throws IllegalArgumentException when lastPage <= 0
	 * @return query
	 */
	public CPathSearchQuery allPages() {
		multi = true;
		page = ALL_PAGES; //special val.
		if(queryString == null || queryString.isEmpty())
			queryString = "*";
		return this;
	}
	
	/**
	 * Set a number N > 0 to request
	 * top hits on pages 0..N.
	 * 
	 * @param lastPage
	 * @return
	 * @throws IllegalArgumentException when lastPage <= 0
	 */
	public CPathSearchQuery topPages(int lastPage) {
		if (page <= 0)
			throw new IllegalArgumentException(
				"The last page number must be greater than zero");
		multi = true;
		page = lastPage;
		
		return this;
	}

	/**
	 * Sets the filter by organism.
	 * @param organisms a set of organism names/taxonomy or null (no filter)
	 * @return
	 */
	public CPathSearchQuery organismFilter(String[] organisms) {
		this.organism = organisms;
		return this;
	}
	
	/**
	 * Sets the filter by pathway data source.
	 * @param datasources a set of data source names/URIs, or null (no filter)
	 * @return
	 */
	public CPathSearchQuery datasourceFilter(String[] datasources) {
		this.datasource = datasources;
		return this;
	}	
	
	/**
	 * Sets the filter by organism.
	 * @param organisms a set of organism names/taxonomy or null (no filter)
	 * @return
	 */
	public CPathSearchQuery organismFilter(Collection<String> organisms) {
		this.organism = organisms.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
		return this;
	}
	
	/**
	 * Sets the filter by pathway data source.
	 * @param datasources a set of data source names/URIs, or null (no filter)
	 * @return
	 */
	public CPathSearchQuery datasourceFilter(Collection<String> datasources) {
		this.datasource = datasources.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
		return this;
	}	
	
	
	@Override
	public String stringResult(OutputFormat outputFormat) throws CPathException
	{
		throw new UnsupportedOperationException();
	}
	

	@Override
	public SearchResponse result() throws CPathException
	{
		if(!multi) {
			return client.post(command, getRequestParams(), SearchResponse.class);
		} else {
			return multiPageResult();
		}
	}
	
	private SearchResponse multiPageResult() throws CPathException
	{
	 	SearchResponse mulRes = null; //to return
		
		//when 'multi' is true, 'page'>0 means 
		//the last page to grab, and 'page'<0 - get all pages
	 	Integer last = this.page;
	 	//build and save the query parameters map (only once)
		MultiValueMap<String, String> request = getRequestParams();
		
		//btw: should never happen here due to how allPages, topPages(n), and page(n) are implemented
	 	assert multi==true && last!=0 : "bug: multiPageResult is called, whereas page==0, multi==true";
	 	// anyway, a shortcut (for the accidental bug/trivial case)
	 	if(last == 0) 
	 		return client.post(command, request, SearchResponse.class);
		
	 	Integer p = 0; //the first hits page # is always 0
	 	do {
			//update current 'page' # in the request map (but do not touch 'this.page' anymore!)
			request.put(CmdArgs.page.name(), Arrays.asList(p.toString())); //using 'add' instead of 'put' would be a bug
			SearchResponse res = client.post(command, request, SearchResponse.class);
	 		if(res != null && !res.isEmpty()) { //collect hits
	 			if(mulRes == null) {
	 				mulRes = res;
	 				int totPages = res.numPages();
	 				//the last page # auto-correction
	 				if(last == ALL_PAGES || last >= totPages) 
	 					last = totPages-1;
	 			} else {
	 				mulRes.getSearchHit().addAll(res.getSearchHit());
	 			}	
	 		} else //should not happen (cpath2 returns error status when empty result)
	 			break; //there are no hits (no more)
	 		
	 		p++; //next page?
	 		
	 	} while(p <= last);
		
	 	if(mulRes != null)
	 		mulRes.setComment(mulRes.getComment() + 
	 			" This result combines top hits from multiple result pages: from 0 to " + last);
	 	
		return mulRes;
	}
	
}
