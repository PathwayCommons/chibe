package org.gvt.action;

import org.cbio.causality.data.drug.DrugData;
import org.cbio.causality.idmapping.CancerGeneCensus;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Color;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.ItemSelectionRunnable;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.NodeModel;
import org.gvt.model.basicsif.BasicSIFGraph;
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
public class HighlightCOSMICGenesActions extends Action
{
	/**
	 * Main application.
	 */
	ChisioMain main;

	/**
	 * Constructor
	 */
	public HighlightCOSMICGenesActions(ChisioMain main)
	{
		super("Highlight cancer genes (COSMIC)");
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

		Set<String> cancerGenes = CancerGeneCensus.getAllSymbols();

		for (Object o : graph.getNodes())
		{
			if (o instanceof NodeModel)
			{
				NodeModel node = (NodeModel) o;

				String name = node.getText();

				if (cancerGenes.contains(name))
				{
					node.setHighlightColor(ChisioMain.higlightColor);
					node.setHighlight(true);
				}
			}
		}
	}
}