package org.gvt.util;

import org.biopax.paxtools.model.BioPAXElement;

/**
 * For getting ID of a BioPAXElement.
 * @author Ozgun Babur
 */
public class ID
{
	public static String get(BioPAXElement ele)
	{
		return ele.getUri();
	}
}
