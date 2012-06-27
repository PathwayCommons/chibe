package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;
import org.gvt.gui.StringInputDialog;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;

import java.util.Iterator;

/**
 * Action for highlighting the nodes using a given name.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class HighlightByNameAction extends Action
{
	/**
	 * Main application.
	 */
	ChisioMain main;

	/**
	 * Name to use in highlighting.
	 */
	String name;
	
	/**
	 * Constructor
	 */
	public HighlightByNameAction(ChisioMain main)
	{
		super("Highlight By Name ...");
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		if (main.getViewer() == null) return;

		// Ask user the name if it is null

		if (name == null)
		{
			StringInputDialog dialog = new StringInputDialog(main.getShell(), "Highlight By Name",
				"Enter keyword for searching entities", null);

			name = dialog.open();
		}

		// If we have a name, highlight related

		if (name != null && name.trim().length() > 0)
		{
			// we sill do a case-insensitive comparison

			name = name.toLowerCase().trim();

			// Iterate all nodes in graph
			
			CompoundModel root = (CompoundModel) ((ChsRootEditPart)main.getViewer().
				getRootEditPart().getChildren().get(0)).getModel();

			Iterator<NodeModel> nodeIter = root.getNodes().iterator();

			while (nodeIter.hasNext())
			{
				NodeModel node = nodeIter.next();

				if (node.getText().toLowerCase().indexOf(name) >= 0 ||
					(node.getTooltipText() != null &&
						node.getTooltipText().toLowerCase().indexOf(name) >= 0))
				{
					node.setHighlight(true);
				}
			}
		}
		
		name = null;
	}
}