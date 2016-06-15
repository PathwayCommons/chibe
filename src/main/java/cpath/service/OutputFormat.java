package cpath.service;

/**
 * Enumeration of cPath service output formats
 * 
 * @author rodche
 *
 */
public enum OutputFormat {
    BIOPAX("BioPAX RDF/XML Format",".owl","application/vnd.biopax.rdf+xml"),
	BINARY_SIF("Simple Binary Interaction Format",".sif","text/plain"),
    EXTENDED_BINARY_SIF("Extended Simple Binary Interaction Format",".txt","text/plain"),
	GSEA("Gene Set Expression Analysis Format",".gmt","text/plain"),
    SBGN("Systems Biology Graphical Notation Format",".sbgn.xml","application/xml"),
	JSONLD("JSON-LD format", ".json", "application/ld+json")
	;
    
    private final String info;
	private final String ext;
	private final String mediaType;
    
    public String getInfo() {
		return info;
	}

	public String getExt() {
		return ext;
	}

	public String getMediaType() {
		return mediaType;
	}
    
    private OutputFormat(String info, String ext, String mediaType) {
		this.info = info;
		this.ext = ext;
		this.mediaType = mediaType;
	}
}