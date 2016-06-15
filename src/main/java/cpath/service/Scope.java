package cpath.service;

/**
 * Predefined large pathway data (sub-)models
 * that are generated and used by the application.
 * (toString method here is to get the part of the 
 * corresponding filename, such as 'All' in '*.All.*.gz').
 * 
 * In addition, by-organism and by-source archives
 * are also created in the batch downloads directory,
 * but those filenames do not require this enum.
 * 
 * @author rodche
 */
public enum Scope {
	ALL, // related to the main biopax model and its derivatives
	DETAILED, //for sub-models based on biopax type datasources only 
	WAREHOUSE //warehouse data archive(s) that contain normalized entity references, etc.
	;
			
	@Override
	public String toString() {
		String ret = super.toString();
		return ret.substring(0, 1).toUpperCase() + ret.substring(1).toLowerCase();
	};
}
