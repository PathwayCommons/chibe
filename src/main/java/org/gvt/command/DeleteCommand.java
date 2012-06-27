package org.gvt.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.gvt.model.*;

/**
 * @author Cihan Kucukkececi (modified by)
 */
public class DeleteCommand extends Command
{
	private NodeModel child;

	private CompoundModel parent;

	private List sourceConnections = new ArrayList();

	private List targetConnections = new ArrayList();

	public DeleteCommand()
	{
		super("DeleteCommand_Label");
	}

	private void deleteConnections(NodeModel part)
	{
		if (part instanceof CompoundModel)
		{
			List children = ((CompoundModel) part).getChildren();

			for (int i = 0; i < children.size(); i++)
				deleteConnections((NodeModel) children.get(i));
		}

		sourceConnections.addAll(part.getSourceConnections());

		for (int i = 0; i < sourceConnections.size(); i++)
		{
			EdgeModel wire = (EdgeModel) sourceConnections.get(i);
			wire.setHighlight(false);
			wire.getTarget().removeTargetConnection(wire);
			wire.getSource().removeSourceConnection(wire);
		}

		targetConnections.addAll(part.getTargetConnections());

		for (int i = 0; i < targetConnections.size(); i++)
		{
			EdgeModel wire = (EdgeModel) targetConnections.get(i);
			wire.setHighlight(false);
			wire.getTarget().removeTargetConnection(wire);
			wire.getSource().removeSourceConnection(wire);
		}
	}

	public void execute()
	{
		child.setHighlight(false);

		if (child instanceof CompoundModel)
		{
			removeHighlights((CompoundModel) child);
		}

		primExecute();
		parent.calculateSizeUp();
	}

	public void removeHighlights(CompoundModel mdl)
	{
		for (int i = 0; i < mdl.getChildren().size(); i++)
		{
			NodeModel node = (NodeModel) mdl.getChildren().get(i);
			node.setHighlight(false);

			if (node instanceof CompoundModel)
			{
				removeHighlights((CompoundModel) node);
			}
		}
	}

	protected void primExecute()
	{
		deleteConnections(child);
		parent.removeChild(child);
	}

	public void redo()
	{
		primExecute();
	}

	private void restoreConnections()
	{
		sourceConnections.clear();
		targetConnections.clear();
	}

	public void setChild(NodeModel c)
	{
		child = c;
	}

	public void setParent(CompoundModel p)
	{
		parent = p;
	}

	public void undo()
	{
		parent.addChild(child);
		child.setParentModel(parent);
		restoreConnections();
	}
}