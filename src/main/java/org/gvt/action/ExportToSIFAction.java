package org.gvt.action;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.pattern.miner.SIFType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.ExportToSIFL3Dialog;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.CompoundModel;
import org.gvt.model.biopaxl3.BioPAXL3Graph;
import org.gvt.util.BioPAXUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ExportToSIFAction extends Action
{
	/**
	 * Main application.
	 */
	private ChisioMain main;

	/**
	 * Rule types to discover and write in SIF in level3.
	 */
	List<SIFType> ruleTypesL3;

	private boolean entireModel;

	public ExportToSIFAction(ChisioMain main, boolean entireModel)
	{
		super("Convert " + (entireModel ? "model" : "view") + " to SIF ...");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/sif.png"));
		setToolTipText(getText());
		this.main = main;
		this.ruleTypesL3 = new ArrayList<SIFType>();
		this.entireModel = entireModel;
	}

	public void run()
	{
		Model model = main.getBioPAXModel();

		if (model == null)
		{
            MessageDialog.openError(main.getShell(), "Error!",
                "Load or query a BioPAX model first.");
			return;
		}
		if (model.getLevel() == BioPAXLevel.L2)
		{
			MessageDialog.openError(main.getShell(), "Error!",
				"This action is not supported at Level 2 BioPAX models. Please use level 3.");
			return;
		}

		if (!entireModel)
		{
			CompoundModel root = main.getPathwayGraph();

			boolean stop = true;

			if (root instanceof BioPAXL3Graph)
			{
				BioPAXL3Graph graph = (BioPAXL3Graph) root;

				if (graph.isMechanistic())
				{
					model = BioPAXUtil.excise(model, graph.getPathway());
					stop = false;
				}
			}

			if (stop)
			{
				MessageDialog.openError(main.getShell(), "Error!",
					"This feature works only for Level 3 mechanistic views.");
				return;
			}
		}

		ExportToSIFL3Dialog dialog = new ExportToSIFL3Dialog(main.getShell(),
			org.gvt.model.sifl3.SIFGraph.getPossibleRuleTypes(), ruleTypesL3);

		boolean okPressed = dialog.open();

		if (okPressed && !ruleTypesL3.isEmpty())
		{
			BioPAXGraph sif = new org.gvt.model.sifl3.SIFGraph(model, ruleTypesL3, main.collectUbiqueIDs());

			int nodenum = sif.getNodes().size();
			int edgenum = sif.getEdges().size();

			System.out.println("created nodenum = " + nodenum + " edgenum = " + edgenum);

			if (nodenum > 0)
			{
				String name = main.getOwlFileName();

				if (name == null)
				{
					name = "SIF View";
				}
				else
				{
					if (name.contains("/"))
					{
						name = name.substring(name.lastIndexOf("/")+1, name.lastIndexOf("."));
					}
					else if (name.contains("\\"))
					{
						name = name.substring(name.lastIndexOf("\\")+ 1, name.lastIndexOf("."));
					}
				}

				sif.setName(name);
				main.createNewTab(sif);
				main.associateGraphWithExperimentData(sif);
				new CoSELayoutAction(main).run();
			}
			else
			{
				MessageDialog.openInformation(main.getShell(), "Empty graph!",
					"Could not create any graph using specified rules.");
			}
		}
	}
}
