package cpath.service.jaxb;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="traverseResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TraverseResponse")
public class TraverseResponse extends ServiceResponse
{
    @XmlAttribute
    private String propertyPath;

    private List<TraverseEntry> traverseEntry;
    
	public TraverseResponse() {
	}
    
    public String getPropertyPath() {
		return propertyPath;
	}
	public void setPropertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
	}

	public List<TraverseEntry> getTraverseEntry() {
		if(traverseEntry == null) {
			traverseEntry = new ArrayList<TraverseEntry>();
		}
		return traverseEntry;
	}
	public void setTraverseEntry(List<TraverseEntry> traverseEntry) {
		this.traverseEntry = traverseEntry;
	}
	
	@Override
	@XmlTransient
	public boolean isEmpty() {
		if(getTraverseEntry().isEmpty())
			return true;
		
		for(TraverseEntry ent : traverseEntry) {
			if(!ent.isEmpty())
				return false;
		}
		
		return  true; //empty
	} 

}
