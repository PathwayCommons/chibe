package org.gvt.action;

import org.biopax.paxtools.model.Model;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.gvt.ChisioMain;
import org.gvt.GraphAnimation;
import org.gvt.editpart.ChsScalableRootEditPart;
import org.gvt.figure.HighlightLayer;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.GraphObject;
import org.gvt.model.IBioPAXEdge;
import org.gvt.model.IBioPAXNode;
import org.gvt.util.Conf;
import org.gvt.util.PathwayHolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class UpdatePathwayAction extends Action
{
	private ChisioMain main;

	/**
	 * If true, then all open views will be updated. Otherwise only the current view will be
	 * updated.
	 */
	private boolean allOpenPathways;

	/**
	 * This collection is used when user wants to update contents of the pathway.
	 */
	private Collection<GraphObject> withContent;

	private Boolean showCompartments;

	public UpdatePathwayAction(ChisioMain main, boolean allOpenPathways)
	{
		super(allOpenPathways ? "Update All Open Pathways" : "Update Pathway");

		if (allOpenPathways)
		{
			setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/pathway-update-all.png"));
		}

		this.setToolTipText(this.getText());
		this.main = main;
		this.allOpenPathways = allOpenPathways;
	}

	public UpdatePathwayAction(ChisioMain main, Collection<GraphObject> withContent)
	{
		this(main, false);
		this.withContent = withContent;
	}

	public void setShowCompartments(boolean showCompartments)
	{
		this.showCompartments = showCompartments;
	}

	// Replace graph
	private void updateViewerContent(ScrollingGraphicalViewer viewer)
	{
		BioPAXGraph graph = (BioPAXGraph) viewer.getContents().getModel();

		// Updating is supported only for mechanistic views
		if (!graph.isMechanistic()) return;

		// Remember highlighted object's original nodes (members of chisio root graph)

		Map<String, Color> highlightMap = new HashMap<String, Color>();

		for (Object o : graph.getNodes())
		{
			IBioPAXNode node = (IBioPAXNode) o;
			if (node.isHighlighted())
			{
				highlightMap.put(node.getIDHash(), node.getHighlightColor());
			}
		}

		for (Object o : graph.getEdges())
		{
			IBioPAXEdge edge = (IBioPAXEdge) o;
			if (edge.isHighlighted())
			{
				highlightMap.put(edge.getIDHash(), edge.getHighlightColor());
			}
		}

		// Reset highlight
		HighlightLayer hLayer = (HighlightLayer)
			((ChsScalableRootEditPart) viewer.getRootEditPart()).getLayer(
				HighlightLayer.HIGHLIGHT_LAYER);

		hLayer.removeAll();
		hLayer.highlighted.clear();

		viewer.deselectAll();

		// Record layout
		graph.recordLayout();

		// Create updated graph
		PathwayHolder p = graph.getPathway();

		// Update pathway components if update needed

		if (withContent != null)
		{
			p.updateContentWith(withContent);
		}

		// Excise pathway

		BioPAXGraph newGraph = main.createAModelForView(p);
		newGraph.setAsRoot();

		newGraph.setName(graph.getName());

		// Replace the graph
		viewer.setContents(newGraph);

		if (showCompartments == null &&
			newGraph.getEdges().size() > Conf.getNumber(Conf.HIDE_COMPARTMENT_EDGE_THRESHOLD) ||
			(showCompartments != null && !showCompartments))
		{
			new HideCompartmentsAction(main).run();
		}

		// Use same layout
		boolean layedout = newGraph.fetchLayout();

		if (!layedout)
		{
			new CoSELayoutAction(main).run();
		}

		viewer.deselectAll();
		GraphAnimation.run(viewer);

		// Recover highlights

		for (Object o : newGraph.getNodes())
		{
			IBioPAXNode node = (IBioPAXNode) o;
			if (highlightMap.containsKey(node.getIDHash()))
			{
				node.setHighlightColor(highlightMap.get(node.getIDHash()));
				node.setHighlight(true);
			}
		}

		for (Object o : newGraph.getEdges())
		{
			IBioPAXEdge edge = (IBioPAXEdge) o;
			if (highlightMap.containsKey(edge.getIDHash()))
			{
				edge.setHighlightColor(highlightMap.get(edge.getIDHash()));
				edge.setHighlight(true);
			}
		}
	}

	public void run()
	{
		Model model = main.getBioPAXModel();

		if (model == null)
		{
			MessageDialog.openError(main.getShell(), "Error!",
				"Load or query a BioPAX model first!");

			return;
		}

		if (allOpenPathways)
		{
			for (ScrollingGraphicalViewer viewer : main.getTabToViewerMap().values())
			{
				updateViewerContent(viewer);
			}
		}
		else
		{
			updateViewerContent(main.getViewer());
		}
	}
}