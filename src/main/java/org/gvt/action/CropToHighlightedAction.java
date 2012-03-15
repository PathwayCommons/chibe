package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.EdgeModel;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.GraphObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Action for removing the highlight from all objects in the graph.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class CropToHighlightedAction extends Action
{
	ChisioMain main;

	/**
	 * Constructor
	 */
	public CropToHighlightedAction(ChisioMain main)
	{
		super("Crop To Highlighted");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/pathway-create-crop.png"));
		this.main = main;
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
}