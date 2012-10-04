package org.gvt.model.custom;

import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;

import java.util.Map;

import static org.gvt.model.custom.CustomGraph.*;

/**
 * @author Ozgun Babur
 */
public class CustomGroup extends CompoundModel
{
	public CustomGroup(Map<String, String> props, Map<String, NodeModel> nodeMap)
	{
		if (props.containsKey(MEMBERS))
		{
			String[] members = props.get(MEMBERS).split(PROPERTY_SEPARATOR);

			for (String mem : members)
			{
				assert nodeMap.containsKey(mem);

			}

		}
	}
}
