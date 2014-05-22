package org.gvt.action;

import org.cbio.causality.data.drug.DrugData;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.ItemSelectionRunnable;
import org.gvt.gui.StringInputDialog;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.model.basicsif.BasicSIFNode;
import org.gvt.model.biopaxl3.BioPAXNode;
import org.gvt.model.sifl3.SIFGraph;

import java.util.*;

/**
 * Action for showing available drugs for the genes shown on the current SIF view.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ShowDruggableAction extends Action
{
	/**
	 * Main application.
	 */
	ChisioMain main;

	private static final String CANCER_DRUG = "Cancer drug";
	private static final String FDA_APPROVED = "FDA approved";
	private static final String NUTRACEUTICAL = "Nutraceutical";
	private static final Color NO_DRUG_BG = new Color(null, 255, 255, 255);
	private static final Color DRUGGABLE_BG = new Color(null, 255, 180, 150);

	/**
	 * Constructor
	 */
	public ShowDruggableAction(ChisioMain main)
	{
		super("Show druggable ...");
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		if (main.getViewer() == null) return;

		final BioPAXGraph graph = main.getPathwayGraph();

		if (!(graph instanceof BasicSIFGraph || graph instanceof SIFGraph ||
			graph instanceof org.gvt.model.sifl2.SIFGraph))
		{
			return;
		}

		Map<NodeModel, Color> colors = getColors(graph);
		Map<NodeModel, String> tooltips = getTooltips(graph);

		List<String> opts = new ArrayList<String>(Arrays.asList(
			CANCER_DRUG, FDA_APPROVED, NUTRACEUTICAL));

		List<String> selected = new ArrayList<String>();

		ItemSelectionRunnable runner = new ItemSelectionRunnable()
		{
			@Override
			public void run(Collection selectedTerms)
			{
				for (Object o : graph.getNodes())
				{
					if (o instanceof NodeModel)
					{
						NodeModel node = (NodeModel) o;

						String name = node.getText();

						Set<String> drugs = new HashSet<String>(DrugData.getDrugs(name));

						Iterator<String> iter = drugs.iterator();
						while (iter.hasNext())
						{
							String drug = iter.next();

							if ((selectedTerms.contains(CANCER_DRUG) && !DrugData.isCancerDrug(drug)) ||
								(selectedTerms.contains(FDA_APPROVED) && !DrugData.isFDAApproved(drug)) ||
								(selectedTerms.contains(NUTRACEUTICAL) && !DrugData.isNutraceutical(drug)))
							{
								iter.remove();
							}
						}

						if (drugs.isEmpty())
						{
							node.setColor(NO_DRUG_BG);
							node.setTooltipText(name);
						}
						else
						{
							node.setColor(DRUGGABLE_BG);
							node.setTooltipText(getTooltip(drugs));
						}
					}
				}
			}
		};
		ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(), 100, "Drugs",
			"Select options", opts, selected, true, false, runner);

		dialog.setUpdateUponSelection(true);
		runner.run(Collections.EMPTY_SET);
		dialog.open();
		restoreColors(graph, colors);
		restoreTooltips(graph, tooltips);
	}

	private String getTooltip(Set<String> drugs)
	{
		List<String> list = new ArrayList<String>(drugs);
		Collections.sort(list);
		StringBuilder s = new StringBuilder();
		for (String drug : list)
		{
			s.append(drug).append("\n");
		}
		return s.toString().trim();
	}

	private Map<NodeModel, Color> getColors(BioPAXGraph graph)
	{
		Map<NodeModel, Color> colors = new HashMap<NodeModel, Color>();
		for (Object o : graph.getNodes())
		{
			if (o instanceof BioPAXNode)
			{
				colors.put((NodeModel) o, ((NodeModel) o).getColor());
			}
		}
		return colors;
	}

	private void restoreColors(BioPAXGraph graph, Map<NodeModel, Color> colors)
	{
		for (Object o : graph.getNodes())
		{
			if (o instanceof BioPAXNode)
			{
				if (colors.containsKey(o))
				{
					((BioPAXNode) o).setColor(colors.get(o));
				}
			}
		}
	}

	private Map<NodeModel, String> getTooltips(BioPAXGraph graph)
	{
		Map<NodeModel, String> tooltips = new HashMap<NodeModel, String>();
		for (Object o : graph.getNodes())
		{
			if (o instanceof BioPAXNode)
			{
				tooltips.put((NodeModel) o, ((NodeModel) o).getTooltipText());
			}
		}
		return tooltips;
	}

	private void restoreTooltips(BioPAXGraph graph, Map<NodeModel, String> tooltips)
	{
		for (Object o : graph.getNodes())
		{
			if (o instanceof BioPAXNode)
			{
				if (tooltips.containsKey(o))
				{
					((BioPAXNode) o).setTooltipText(tooltips.get(o));
				}
			}
		}
	}
}