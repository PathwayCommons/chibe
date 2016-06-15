/**
 * 
 */
package cpath.service.jaxb;

import javax.xml.bind.annotation.*;
import java.util.Set;
import java.util.TreeSet;

/**
 * A bean for the help web service response.
 * 
 * @author rodche
 *
 */
@XmlRootElement
@XmlType(name = "Help")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Help extends ServiceResponse implements Comparable<Help>{

	private String id;
	private String title;
	private String info;
	private String example;
    private String output;
	private Set<Help> members;
	
	
	public Help() {
		members = new TreeSet<Help>();
	}
	
	public Help(String id) {
		this();
		this.id = id;
	}
	
	
	/**
	 * @return the id
	 */
	@XmlElement(required = true)
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}


	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}


	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}
	/**
	 * @param info the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}
	/**
	 * @return the example
	 */
	public String getExample() {
		return example;
	}
	/**
	 * @param example the example to set
	 */
	public void setExample(String example) {
		this.example = example;
	}
	/**
	 * @return the output
	 */
	public String getOutput() {
		return output;
	}
	/**
	 * @param output the output to set
	 */
	public void setOutput(String output) {
		this.output = output;
	}
	/**
	 * @return the members
	 */
	public Help[] getMembers() {
		return members.toArray(new Help[]{});
	}
	
	public void addMember(Help arg) {
		members.add(arg);
	}
	
	/**
	 * @param members the members to set
	 */
	public void setMembers(Help[] members) {
		this.members.clear();
		for(Help h : members) {
			this.members.add(h);
		}
	}

	@Override
	public int compareTo(Help o) {
		return this.id.compareToIgnoreCase(o.id);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
