package org.gvt.model.custom;

import org.gvt.command.AddCommand;
import org.gvt.command.CreateCommand;
import org.gvt.command.OrphanChildCommand;
import org.gvt.command.ReconnectConnectionCommand;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;

import java.util.HashMap;
import java.util.Map;

import static org.gvt.model.custom.CustomGraph.*;

/**
 * @author Ozgun Babur
 */
public class CustomGroup extends CompoundModel
{
	public CustomGroup(CompoundModel root, Map<String, String> props,
		Map<String, NodeModel> nodeMap)
	{
		CreateCommand command = new CreateCommand(root, this);
		command.execute();

		if (props.containsKey(TEXT)) setText(props.get(TEXT));
		else setText("group");

		if (props.containsKey(TOOLTIP)) setTooltipText(props.get(TOOLTIP));
		if (props.containsKey(BGCOLOR)) setColor(textToColor(props.get(BGCOLOR)));
		if (props.containsKey(BORDERCOLOR)) setBorderColor(textToColor(props.get(BORDERCOLOR)));
		if (props.containsKey(TEXTCOLOR)) setTextColor(textToColor(props.get(TEXTCOLOR)));


		Map<NodeModel, NodeModel> old2new = new HashMap<NodeModel, NodeModel>();

		if (props.containsKey(MEMBERS))
		{
			String[] members = props.get(MEMBERS).split(PROPERTY_SEPARATOR);

			for (String mem : members)
			{
				assert nodeMap.containsKey(mem);
				
				NodeModel node = nodeMap.get(mem);
				
				if (node.getParentModel() instanceof CustomGroup)
				{
					NodeModel old = node;
					
					if (old instanceof CustomNode)
					{
						node = new CustomNode(this, (CustomNode) old);
						((CustomNode) old).duplicate = true;
						((CustomNode) node).duplicate = true;
					}
					else if (old instanceof CustomGroup)
					{
						throw new RuntimeException("Missing implementation!");
						// todo implement
					}

					old2new.put(old, node);
				}
				else
				{
					// transfer node

					OrphanChildCommand orphan = new OrphanChildCommand();
					orphan.setParent(node.getParentModel());
					orphan.setChild(node);
					orphan.execute();

					AddCommand add = new AddCommand();
					add.setParent(this);
					add.setChild(node);
					add.execute();
				}
			}

			for (NodeModel old : old2new.keySet())
			{
				for (Object o : old.getSourceConnections())
				{
					CustomEdge edge = (CustomEdge) o;
					
					assert edge.getSource() == old;
					
					NodeModel mate = edge.getTarget();
					
					if (old2new.keySet().contains(mate))
					{
						NodeModel newMate = old2new.get(mate);
						new CustomEdge(old2new.get(old), newMate, edge);
					}
				}

				for (Object o : old.getTargetConnections())
				{
					CustomEdge edge = (CustomEdge) o;
					
					assert edge.getTarget() == old;
					
					NodeModel mate = edge.getSource();
					
					if (old2new.keySet().contains(mate))
					{
						NodeModel newMate = old2new.get(mate);
						new CustomEdge(newMate, old2new.get(old), edge);
					}
				}
			}

			for (Object o : getChildren())
			{
				NodeModel child = (NodeModel) o;

				if (!old2new.values().contains(child))
				{
					for (Object oo : child.getSourceConnections())
					{
						CustomEdge edge = (CustomEdge) oo;
						NodeModel mate = edge.getTarget();
						if (old2new.containsKey(mate))
						{
							ReconnectConnectionCommand com = new ReconnectConnectionCommand();
							com.setConnectionModel(edge);
							com.setNewTarget(old2new.get(mate));
							com.execute();
						}
					}
					for (Object oo : child.getTargetConnections())
					{
						CustomEdge edge = (CustomEdge) oo;
						NodeModel mate = edge.getSource();
						if (old2new.containsKey(mate))
						{
							ReconnectConnectionCommand com = new ReconnectConnectionCommand();
							com.setConnectionModel(edge);
							com.setNewSource(old2new.get(mate));
							com.execute();
						}
					}
				}
			}
		}
	}
}
