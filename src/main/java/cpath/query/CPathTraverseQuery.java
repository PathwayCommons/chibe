package cpath.query;

import cpath.client.CPathClient;
import cpath.client.util.CPathException;
import cpath.service.Cmd;
import cpath.service.CmdArgs;
import cpath.service.OutputFormat;
import cpath.service.jaxb.TraverseResponse;
import org.apache.commons.lang.ArrayUtils;
import org.biopax.paxtools.controller.PathAccessor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Collection;

/**
 * A property 'traverse' query 
 * to be executed with a {@link CPathClient}
 * on the cpath2 server's biopax db.
 * 
 * @author rodche
 */
public final class CPathTraverseQuery extends BaseCPathQuery<TraverseResponse> implements CPathQuery<TraverseResponse>
{

	private String path;
	private String[] source;

	/**
	 * @return the request
	 */
	protected MultiValueMap<String, String> getRequestParams() {
		MultiValueMap<String, String> request = new LinkedMultiValueMap<String, String>();
		if(source == null || source.length == 0)
			throw new IllegalArgumentException("Required 'source' (URIs) parameter cannot be null or empty.");
		request.put(CmdArgs.uri.name(), Arrays.asList(source));
		if(path == null || path.isEmpty())
			throw new IllegalArgumentException("Property 'path' is null or empty.");
		request.add(CmdArgs.path.name(), path);
		return request;
	}

	/**
	 * Constructor.
	 * @param client instance of the cpath2 client
	 */
	public CPathTraverseQuery(CPathClient client) {
		this.client = client;
	}

	/**
	 * URIs of biopax elements to start with
	 * (i.e., to apply the path to each one and get property values).
	 * @param source
	 * @return
	 */
	public CPathTraverseQuery sources(String[] source) {
		this.source = source;
		return this;
	}
	
	/**
	 * URIs of biopax elements to start with
	 * (i.e., to apply the path to each one and get property values).
	 * @param source
	 * @return
	 */
	public CPathTraverseQuery sources(Collection<String> sources) {
		this.source = sources.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
		return this;
	}	

	/**
	 * A biopax properties path to follow
	 * in order to get values.
	 * 
	 * @see PathAccessor
	 * 
	 * @param path
	 * @return
	 */
	public CPathTraverseQuery propertyPath(String path) {
		this.path = path;
		return this;
	}

	@Override
	public String stringResult(OutputFormat outputFormat) throws CPathException
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public TraverseResponse result() throws CPathException
	{
		return client.post(Cmd.TRAVERSE.toString(), getRequestParams(), TraverseResponse.class);
	}
}
