/**
 * 
 */
package cpath.query;

import cpath.client.CPathClient;
import cpath.client.util.CPathException;
import cpath.service.Cmd;
import cpath.service.CmdArgs;
import cpath.service.OutputFormat;
import org.apache.commons.lang.ArrayUtils;
import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.model.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Collection;

/**
 * A get by ID/URI query to be executed with {@link CPathClient}
 * 
 * @author rodche
 */
public final class CPathGetQuery extends BaseCPathQuery<Model> implements CPathQuery<Model>
{
	
	private boolean mergeEquivalentInteractions = false;
	private String[] source;
	
	
	/**
	 * @return the request
	 */
	protected MultiValueMap<String, String> getRequestParams() {
		MultiValueMap<String, String> request = new LinkedMultiValueMap<String, String>();
		if(source == null || source.length == 0)
			throw new IllegalArgumentException("Required list of URIs/IDs " +
					"cannot be null or empty");
		request.put(CmdArgs.uri.name(), Arrays.asList(source));
		return request;
	}

	/**
	 * Constructor.
	 * @param client cpath2 client instance
	 */
	public CPathGetQuery(CPathClient client) {
		this.client = client;
	}

	/**
	 * A list of URIs (of biopax elements) 
	 * or IDs (e.g., gene symbols).
	 * 
	 * @param sources
	 * @return
	 */
	public CPathGetQuery sources(String[] sources) {
		this.source = sources;
		return this;
	}
	
	/**
	 * A collection of URIs (of biopax elements) 
	 * or IDs (e.g., gene symbols).
	 * 
	 * @param sources
	 * @return
	 */
	public CPathGetQuery sources(Collection<String> sources) {
		this.source = sources.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
		return this;
	}	

	
	/**
	 * Sets the option to merge equivalent interactions in the result model.
	 * @param mergeEquivalentInteractions
	 */
	public void mergeEquivalentInteractions(boolean mergeEquivalentInteractions)
	{
		this.mergeEquivalentInteractions = mergeEquivalentInteractions;
	}

	@Override
	public String stringResult(OutputFormat format) throws CPathException
	{
		MultiValueMap<String, String> request = getRequestParams();
		if (format == null)
			format = OutputFormat.BIOPAX;
		request.add(CmdArgs.format.name(), format.name());
		
		return client.post(Cmd.GET.toString(), request, String.class);
	}

	@Override
	public Model result() throws CPathException
	{
		Model model = client.post(Cmd.GET.toString(), getRequestParams(), Model.class);
		
		if (mergeEquivalentInteractions && model != null)
		{
			ModelUtils.mergeEquivalentInteractions(model);
		}
		
		return model;
	}

}
