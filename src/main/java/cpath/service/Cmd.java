/**
 ** Copyright (c) 2009 Memorial Sloan-Kettering Cancer Center (MSKCC)
 ** and University of Toronto (UofT).
 **
 ** This is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** both UofT and MSKCC have no obligations to provide maintenance, 
 ** support, updates, enhancements or modifications.  In no event shall
 ** UofT or MSKCC be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** UofT or MSKCC have been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this software; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA;
 ** or find it at http://www.fsf.org/ or http://www.gnu.org.
 **/

package cpath.service;

import static cpath.service.CmdArgs.*;


/**
 * cPath2 web service commands.
 * 
 * @author rodche
 *
 */
public enum Cmd {
	SEARCH("Full-text search for BioPAX objects. " +
			"It accepts up to five parameters " +
			"and returns the ordered list of search hits, " +
			"which are simplified description of BioPAX elements " +
			"matching the query and passing all filters. A hit's uri (same as the corresponding BioPAX " +
			"object's RDF ID) can be used with other webservice commands to " +
			"extract the corresponding sub-model to BioPAX or another supported format. ",
			"/search?q=brca*&organism=9606", //full PROVIDER_URL shouldn't be specified here (it depends on the server configuration)!
			"Search Response that lists Search Hits - XML (default) or JSON (when called as '/search.json?')",
			new CmdArgs[]{q, page, type, organism, datasource}),
	GET("Gets a BioPAX element or sub-model " +
        "by ID(s).  This command has two parameters.",
        "/get?uri=http://identifiers.org/uniprot/P38398",
        "BioPAX by default, other formats as specified by the format parameter.  " +
        "See the <a href=\"#valid_output_parameter\">valid values for format parameter</a> below.",
        new CmdArgs[]{uri, format}),
	GRAPH("Executes an advanced graph query on the data within pathway commons. " +
          "Returns a sub-model as the result. This command has up to six parameters.",
          "/graph?kind=neighborhood&source=URI1&source=URI2&...",
          "BioPAX by default, other formats as specified by the format parameter. " +
          "See the <a href=\"#valid_output_parameter\">valid values for format parameter</a> below.",
          new CmdArgs[]{kind, source, target, format, limit, direction, organism, datasource}),
    TOP_PATHWAYS("Gets Top Pathways. This command accepts optional filter by organism and by datasource values", 
    	"/top_pathways",
        "Search Response - XML (JSON, when called as '/top_pathways.json?') contains the list of all top pathways.", 
        new CmdArgs[]{organism, datasource}),
    TRAVERSE("Gets data property values (or elements's URIs) " +
    	"at the end of the property path.  This command has two parameters.",
    	"/traverse?uri=http://identifiers.org/uniprot/P38398&path=ProteinReference/organism/displayName",
    	"Traverse Response - XML (or JSON, when called as '/traverse.json?').", 
    	new CmdArgs[]{path, uri})
          
    ;
	
	private final CmdArgs[] args; //Array is better for use in json/jsp than List/Set
	private final String info;
    private final String example;
    private final String output;
	
	public CmdArgs[] getArgs() {
		return args;
	}
	
	public String getInfo() {
		return info;
	}

    public String getExample() {
        return example;
    }
    
    public String getOutput() {
        return output;
    }
	
	private Cmd(String info, String example, String output, CmdArgs... args) {
		this.info = info;
        this.example = example;
        this.output = output;
		this.args = args;
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
