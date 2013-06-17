package org.gvt.command;

import java.util.*;

import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.Command;
import org.gvt.model.NodeModel;
import org.gvt.model.EdgeModel;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsCompoundCommand extends CompoundCommand
{
	public ChsCompoundCommand(String s)
	{
		super(s);
	}

	public ChsCompoundCommand()
	{
		super();
	}

	public void execute()
	{
		NodeModel node = null;
		boolean highlight = false;
		List<EdgeModel> highlightedEdges = new ArrayList();

		for (int i = 0; i < getCommands().size(); i++)
		{
			Command cmd = (Command) getCommands().get(i);

			if (cmd instanceof OrphanChildCommand)
			{
				node = ((OrphanChildCommand) cmd).getChild();
				// remove node highlight
				highlight = node.isHighlight();
				node.setHighlight(false);

				// remove edge highlights
				List edges = node.getSourceConnections();
				Iterator<EdgeModel> iter = edges.iterator();

				while (iter.hasNext())
				{
					EdgeModel edge = iter.next();

					if (edge.isHighlight())
					{
						highlightedEdges.add(edge);
						edge.setHighlight(false);
					}
				}
			}

			cmd.execute();
		}

		// restore node highlights
		for (int i = 0; i < getCommands().size(); i++)
		{
			Command cmd = (Command) getCommands().get(i);

			if (cmd instanceof OrphanChildCommand)
			{
				node = ((OrphanChildCommand) cmd).getChild();

				if (node != null && highlight)
				{
					node.setHighlight(true);
				}
			}
		}

		// restore edge highlights
		for (int i = 0; i < highlightedEdges.size(); i++)
		{
			EdgeModel edge = highlightedEdges.get(i);
			edge.setHighlight(true);
		}
	}
}