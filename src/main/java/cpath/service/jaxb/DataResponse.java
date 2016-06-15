package cpath.service.jaxb;

import cpath.service.OutputFormat;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Set;

/**
 * An internal service bean, any-data response type.
 * 
 * This is normally not for XML marshalling/unmarshalling, 
 * but rather for exchanging data between DB, service, and web tiers.
 *
 */
@XmlTransient
public class DataResponse extends ServiceResponse
{

	@XmlTransient
	private Object data; // BioPAX OWL, String, List, or any other data
	@XmlTransient
	private Set<String> providers; //pathway data provider standard names (for logging/stats)
	private OutputFormat format;
	
	public DataResponse() {
	}

	@XmlTransient
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	@Override
	@XmlTransient
	public boolean isEmpty() {
		return  (data == null || data.toString().trim().isEmpty());
	}
	
	@XmlTransient
	public Set<String> getProviders() {
		return providers;
	}
	public void setProviders(Set<String> providers) {
		this.providers = providers;
	}

	@XmlTransient
	public OutputFormat getFormat() {
		return format;
	}
	public void setFormat(OutputFormat format) {
		this.format = format;
	}
}
