package org.gvt.action;

import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.inspector.CompoundInspector;
import org.gvt.inspector.EdgeInspector;
import org.gvt.inspector.GraphInspector;
import org.gvt.inspector.NodeInspector;
import org.gvt.model.CompoundModel;
import org.gvt.model.EdgeModel;
import org.gvt.model.GraphObject;
import org.gvt.model.NodeModel;

/**
 * This class maintains the Action of inspector window. For edges, nodes,
 * compound nodes and graphs, different inspectors are opened.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class InspectorAction extends Action
{
	ChisioMain main;

	/**
	 * Constructor
	 */
	public InspectorAction(ChisioMain main, boolean isGraph)
	{
		setText(isGraph ? "Properties ..." : "Object Properties ...");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/inspector.png"));
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		if (main.getViewer() == null) return;

		// get the object that user wants to open inspector for.
		Object obj = ((IStructuredSelection) main.getViewer().getSelection()).
			getFirstElement();

		if (!(obj instanceof ScalableRootEditPart))
		{
			GraphObject model =
				(GraphObject) ((AbstractEditPart) obj).getModel();

			if (model instanceof EdgeModel)
			{
				EdgeInspector.getInstance(model, "Edge", main);
			}
			else if (model instanceof CompoundModel)
			{
				if (!((CompoundModel)model).isRoot())
				{
					CompoundInspector.getInstance(model, "Compound", main);
				}
			}
			else if (model instanceof NodeModel)
			{
				NodeInspector.getInstance(model, "Node", main);
			}
		}
		else
		{
			ChsRootEditPart edit = (ChsRootEditPart)
				((ScalableRootEditPart) obj).getChildren().get(0);

			GraphInspector.
				getInstance((GraphObject) edit.getModel(), "Graph", main);
		}
	}
}