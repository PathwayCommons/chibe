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
import org.gvt.util.CancerGenes;

import java.util.*;

/**
 * Action for showing available drugs for the genes shown on the current SIF view.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class HighlightCancerGenesActions extends Action
{
	/**
	 * Main application.
	 */
	ChisioMain main;

	/**
	 * Constructor
	 */
	public HighlightCancerGenesActions(ChisioMain main)
	{
		super("Highlight cancer genes (COSMIC + OncoKB)");
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		if (main.getViewer() == null) return;

		final BioPAXGraph graph = main.getPathwayGraph();

		if (!(graph instanceof BasicSIFGraph || graph instanceof SIFGraph))
		{
			return;
		}

		int cnt = 0;
		for (Object o : graph.getNodes())
		{
			if (o instanceof NodeModel)
			{
				NodeModel node = (NodeModel) o;

				String name = node.getText();

				if (CancerGenes.isCancerGene(name))
				{
//					node.setHighlightColor(ChisioMain.higlightColor);
//					node.setHighlight(true);
					node.setColor(new Color(null, 255, 255, 155));
					cnt++;
				}
			}
		}
		System.out.println("highlighted = " + cnt);
	}
}