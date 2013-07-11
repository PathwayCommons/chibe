package org.gvt.action;

import org.cbio.causality.data.portal.BroadAccessor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CTabItem;
import org.gvt.ChisioMain;
import org.gvt.model.CompoundModel;
import org.gvt.model.biopaxl3.BioPAXL3Graph;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class TCGASIFAction extends Action
{
	ChisioMain main;

	public TCGASIFAction(String title, ChisioMain main)
	{
		super(title);
		setToolTipText(getText());
		this.main = main;
	}

	protected static boolean okToRun(ChisioMain main)
	{
		return okToRun(main, true);
	}

	public static boolean okToRun(ChisioMain main, boolean displayError)
	{
		assert main != null;

		// Run only if current graph is L3 SIF and portal data is loaded.

		if (main.getSelectedTab() == null)
		{
			if (displayError) MessageDialog.openError(main.getShell(), "No graph",
				"Please load a TCGA scpecific SIF model first. You can get\n" +
					"such a model using the menu item \"SIF --> Load TCGA specific SIF\".");
			return false;
		}

		if (ChisioMain.cBioPortalAccessor == null)
		{
			if (displayError) MessageDialog.openError(main.getShell(), "No alteration data",
				"This feature works only when the graph is overlayed with alteration data.");
			return false;
		}

		if (getGraph(main) == null || getCurrentStudy() == null)
		{
			if (displayError) MessageDialog.openError(main.getShell(), "Cannot show",
				"This feature only works for TCGA scpecific SIF models. You can get\n" +
					"such a model using the menu item \"SIF --> Load TCGA specific SIF\".");
			return false;
		}

		return true;
	}

	protected Set<GraphObject> getHighlighted(Graph graph)
	{
		Set<GraphObject> gos = new HashSet<GraphObject>();
		for (Node node : graph.getNodes())
		{
			if (node.isHighlighted()) gos.add(node);
		}
		for (Edge edge : graph.getEdges())
		{
			if (edge.isHighlighted()) gos.add(edge);
		}
		return gos;
	}

	protected void highlight(Set<GraphObject> gos)
	{
		for (GraphObject go : gos)
		{
			go.setHighlight(true);
		}
	}

	protected static String getCurrentStudy()
	{
		if (ChisioMain.cBioPortalAccessor != null)
		{
			String studyID = ChisioMain.cBioPortalAccessor.getCurrentCancerStudy().getStudyId();
			String code = studyID.substring(0, studyID.indexOf("_"));
			code = code.toUpperCase();
			if (BroadAccessor.getStudyCodes().contains(code))
			{
				return code;
			}
		}
		return null;
	}

	protected static BioPAXL3Graph getGraph(ChisioMain main)
	{
		CTabItem tab = main.getSelectedTab();

		if (tab != null)
		{
			CompoundModel root = (CompoundModel) main.getTabToViewerMap().get(tab).getContents().
				getModel();

			if (root instanceof BioPAXL3Graph)
			{
				return (BioPAXL3Graph) root;
			}
		}
		return null;
	}
}
