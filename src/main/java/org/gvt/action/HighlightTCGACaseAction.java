package org.gvt.action;

import org.cbio.causality.model.Alteration;
import org.cbio.causality.model.AlterationPack;
import org.eclipse.swt.custom.CTabItem;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.ItemSelectionRunnable;
import org.gvt.model.CompoundModel;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.*;

/**
 * Action for highlighting the nodes using a given name.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class HighlightTCGACaseAction extends TCGASIFAction
{
	/**
	 * Constructor
	 */
	public HighlightTCGACaseAction(ChisioMain main)
	{
		super("Highlight a TCGA Case ...", main);
	}

	public void run()
	{
		if (!okToRun(main)) return;

		CTabItem tab = main.getSelectedTab();

		CompoundModel root = (CompoundModel) main.getTabToViewerMap().get(tab).getContents().
			getModel();

		final Graph graph = (Graph) root;

		final List<String> cases = new ArrayList<String>(Arrays.asList(
			ChisioMain.cBioPortalAccessor.getCurrentCaseList().getCases()));

		Set<GraphObject> gos = getHighlighted(graph);
		graph.removeHighlights();

		ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(), 150, "TCGA Cases",
			"Please select a TCGA case to highlight", cases, null, false, false,
			new ItemSelectionRunnable()
			{
				Set<GraphObject> gos = new HashSet<GraphObject>();

				@Override
				public void run(Collection selectedTerms)
				{
					for (GraphObject go : gos)
					{
						go.setHighlight(false);
					}
					gos.clear();

					if (selectedTerms.isEmpty()) return;
					Object o = selectedTerms.iterator().next();

					if (o.equals(ItemSelectionDialog.NONE)) return;

					String aCase = o.toString();

					// - 1 is there because the list now contains "None" as first element
					int index = cases.indexOf(aCase) - 1;

					for (Node node : graph.getNodes())
					{
						String sym = node.getName();
						AlterationPack altPack = ChisioMain.cBioPortalAccessor.getAlterations(sym);
						if (altPack != null && altPack.get(Alteration.ANY)[index].isAltered())
						{
							node.setHighlight(true);
							gos.add(node);
						}
					}
				}
			});

		dialog.setUpdateUponSelection(true);
		dialog.setDoSort(false);
		dialog.open();

		graph.removeHighlights();
		highlight(gos);
	}
}