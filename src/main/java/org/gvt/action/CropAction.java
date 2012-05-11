package org.gvt.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.EdgeModel;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.GraphObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Action for removing the highlight from all objects in the graph.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class CropAction extends Action
{
	ChisioMain main;
	CropTo type;

	/**
	 * Constructor
	 */
	public CropAction(ChisioMain main, CropTo type)
	{
		super("Crop To " + type.toString().substring(0,1) +
			type.toString().substring(1).toLowerCase());

		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(
                ChisioMain.class, "icon/pathway-create-crop.png"));
		this.main = main;
		this.type = type;
	}

	public void run()
	{
		BioPAXGraph graph = main.getPathwayGraph();

		if (graph == null)
		{
			MessageDialog.openError(main.getShell(), "Error!", "No BioPAX pathway.");
			return;
		}

		if (!graph.isMechanistic())
		{
			MessageDialog.openError(main.getShell(), "Not Supported!",
				"Cropping is supported only for mechanistic views.");

			return;
		}

		Set<GraphObject> cropto = new HashSet<GraphObject>();

		if (type == CropTo.HIGHLIGHTED)
		{
			for (Object o : graph.getNodes())
			{
				NodeModel node = (NodeModel) o;
				if (node.isHighlight())
				{
					cropto.add((GraphObject) node);
				}
			}

			for (Object o : graph.getEdges())
			{
				EdgeModel edge = (EdgeModel) o;
				if (edge.isHighlight())
				{
					cropto.add((GraphObject) edge);
				}
			}
		}
		else if (type == CropTo.SELECTED)
		{
			ScrollingGraphicalViewer viewer = main.getViewer();

			if (viewer != null)
			{
				// Iterates selected objects
				Iterator selectedObjects = ((IStructuredSelection) viewer.getSelection()).iterator();

				while (selectedObjects.hasNext())
				{
					Object model = ((EditPart)selectedObjects.next()).getModel();

					if (model instanceof NodeModel)
					{
						cropto.add((GraphObject) model);
					}
					else if (model instanceof EdgeModel)
					{
						cropto.add((GraphObject) model);
					}
				}
			}
		}

		if (cropto.isEmpty())
		{
			MessageDialog.openError(main.getShell(), "Error!",
				"Nothing is highlighted.");
			return;			
		}

		BioPAXGraph excised = graph.excise(cropto, true);
		excised.setName(graph.getName() + " cropped");

		main.createNewTab(excised);
		new CoSELayoutAction(main).run();
		excised.recordLayout();
	}

	public enum CropTo
	{
		HIGHLIGHTED,
		SELECTED
	}
}