package org.gvt.model.custom;

import org.gvt.command.CreateConnectionCommand;
import org.gvt.model.EdgeModel;
import org.gvt.model.NodeModel;

import java.util.Map;

import static org.gvt.model.custom.CustomGraph.*;

/**
 * @author Ozgun Babur
 */
public class CustomEdge extends EdgeModel
{
	public CustomEdge(NodeModel source, NodeModel target, Map<String, String> map)
	{
		assert source != null;
		assert target != null;

		CreateConnectionCommand ccc = new CreateConnectionCommand();
		ccc.setSource(source);
		ccc.setTarget(target);
		ccc.setConnection(this);
		ccc.execute();
		
		if (map.containsKey(STYLE)) setStyle(map.get(STYLE));
		if (map.containsKey(ARROW)) setArrow(map.get(ARROW));
		if (map.containsKey(LINECOLOR)) setColor(textToColor(map.get(LINECOLOR)));

		if (map.containsKey(TEXT)) setText(map.get(TEXT));
		if (map.containsKey(TOOLTIP)) setTooltipText(map.get(TOOLTIP));

	}
}
