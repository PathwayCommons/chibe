package org.gvt.command;

import java.util.*;

import org.eclipse.gef.commands.Command;
import org.eclipse.draw2d.geometry.Rectangle;
import org.gvt.action.RemoveHighlightFromSelectedAction;
import org.gvt.action.RemoveHighlightsAction;
import org.gvt.model.*;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class RemoveCompoundCommand extends Command
{
	private CompoundModel model;

	private List sourceConnections = new ArrayList();

	private List targetConnections = new ArrayList();

	public RemoveCompoundCommand()
	{
		super("Remove Compound");
	}

	public void execute()
	{
		List children = model.getChildren();

		Set<GraphObject> highlightedSet = new HashSet<GraphObject>();

		for (int size = children.size(); size > 0; size--)
		{
			NodeModel node = (NodeModel) children.get(0);

			collectHighlighted(node, highlightedSet);

			OrphanChildCommand cmd = new OrphanChildCommand();
			cmd.setParent(model);
			cmd.setChild(node);
			cmd.setOnlyRemoveCompound(true);
			cmd.execute();
			Rectangle rect = node.getConstraint().translate(model.getLocation());

			AddCommand add = new AddCommand();
			add.setParent(model.getParentModel());
			add.setChild(node);

			MoveCommand move = new MoveCommand(node, rect);
			move.setParent(model.getParentModel());

			add.chain(move);

			add.execute();

			for (GraphObject graphObject : highlightedSet)
			{
				graphObject.setHighlight(true);
			}
		}

		DeleteCommand cmd = new DeleteCommand();
		cmd.setChild(model);
		cmd.setParent(model.getParentModel());
		cmd.execute();
	}

	public void redo()
	{
		execute();
	}

	private void restoreConnections()
	{
		sourceConnections.clear();
		targetConnections.clear();
	}

	public void undo()
	{
/*		parent.addChild(child);
		child.setParentModel(parent);
		restoreConnections();    */
	}

	public void setCompound(CompoundModel compoundModel)
	{
		model = compoundModel;
	}
	
	protected void collectHighlighted(GraphObject obj, Set<GraphObject> set)
	{
		if (obj.isHighlight()) 
		{
			set.add(obj);
			obj.setHighlight(false);
		}
		
		if (obj instanceof CompoundModel)
		{
			for (Object o : ((CompoundModel) obj).getChildren())
			{
				if (o instanceof GraphObject)
				{
					collectHighlighted((GraphObject) o, set);
				}
			}
		}
	}
}