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

/**
 * cPath2 web service command arguments.
 * 
 * @author rodche
 *
 */
public enum CmdArgs {
	uri("an identifier, usually a BioPAX element URI (default); multiple '&id=' arguments per query are supported"),
    q("query string (full-text search)."),
    page("search results page number (>=0)."),
	type("a BioPAX class - see the <a href=\"#valid_biopax_parameter\">valid values for type parameter</a> below"),
	kind("graph query type - see the <a href=\"#valid_graph_parameter\">valid values for kind parameter</a> below"),
	format("output format - see the <a href=\"#valid_output_parameter\">valid values for format parameter</a> below"),
	organism("filter by organism (multiple 'organism=' are allowed) - see the <a href=\"#valid_organism_parameter\">valid values for organism parameter</a> below"),
	datasource("filter by data sources (multiple 'datasource=' are allowed) - see the <a href=\"#valid_datasource_parameter\">valid values for datasource parameter</a> below"),
	source("graph query source URI (multiple 'source=' are allowed)"),
	target("graph query destination URI (multiple 'target=' are allowed)"),
    limit("graph query search distance limit"),
    biopax("a BioPAX OWL to convert"),
    path("a BioPAX property path expression (like xPath)"),
    direction("graph query parameter 'direction'"),
    //TODO alg("a user-defined BioPAX data analysis (code) to run."),
	;
	
	private final String info;
	
	public String getInfo() {
		return info;
	}

	private CmdArgs(String info) {
		this.info = info;
	}
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}	
}
