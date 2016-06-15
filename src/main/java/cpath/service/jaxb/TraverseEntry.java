package cpath.service.jaxb;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TraverseEntry")
public class TraverseEntry implements Serializable {
    @XmlAttribute(required = true)
    private String uri;
    
    private List<String> value;

    public TraverseEntry() {
	}

    public String getUri() {
        return uri;
    }
    public void setUri(String value) {
        this.uri = value;
    }
	
	public List<String> getValue() {
		if(value == null) {
			value = new ArrayList<String>();
		}
		return value;
	}
	public void setValue(List<String> value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return getValue().toString();
	}
	
	//package-private
	@XmlTransient
	boolean isEmpty() {
		return getValue().isEmpty();
	}
}
