package org.gvt.action;

import org.biopax.paxtools.controller.Cloner;
import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.pattern.miner.SIFType;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.ExportToSIFL2Dialog;
import org.gvt.gui.ExportToSIFL3Dialog;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.CompoundModel;
import org.gvt.model.biopaxl3.BioPAXL3Graph;

import java.util.*;

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
	 * Rule types to discover and write in SIF in level 2.
	 */
	List<BinaryInteractionType> ruleTypesL2;

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
		this.ruleTypesL2 = new ArrayList<BinaryInteractionType>();
		this.ruleTypesL3 = new ArrayList<SIFType>();
		this.entireModel = entireModel;
	}

	public void run()
	{
		Model model = main.getOwlModel();

		if (model == null)
		{
            MessageDialog.openError(main.getShell(), "Error!",
                "Load or query a BioPAX model first.");
			return;
		}

		if (!entireModel)
		{
			ScrollingGraphicalViewer viewer = main.getTabToViewerMap().get(main.getSelectedTab());

			CompoundModel root = (CompoundModel) viewer.getContents().getModel();

			boolean stop = true;

			if (root instanceof BioPAXL3Graph)
			{
				BioPAXL3Graph graph = (BioPAXL3Graph) root;

				if (model.getLevel() == BioPAXLevel.L3 && graph.isMechanistic())
				{
					model = excise(model, graph.getPathway().l3p);
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

		boolean l3 = model.getLevel() == BioPAXLevel.L3;

		boolean okPressed;

		if (l3)
		{
			ExportToSIFL3Dialog dialog = new ExportToSIFL3Dialog(main.getShell(),
				org.gvt.model.sifl3.SIFGraph.getPossibleRuleTypes(), ruleTypesL3);

			okPressed = dialog.open();
		}
		else
		{
			ExportToSIFL2Dialog dialog = new ExportToSIFL2Dialog(main.getShell(),
				org.gvt.model.sifl2.SIFGraph.getPossibleRuleTypes(main.getOwlModel().getLevel()),
				ruleTypesL2);

			okPressed = dialog.open();
		}

		if (okPressed && (!ruleTypesL2.isEmpty() || !ruleTypesL3.isEmpty()))
		{
			BioPAXGraph sif = l3 ?
			new org.gvt.model.sifl3.SIFGraph(model, ruleTypesL3, main.collectUbiqueIDs()) :
			new org.gvt.model.sifl2.SIFGraph(model, ruleTypesL2);

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

		ruleTypesL2.clear();
	}

	/**
	 * Editor map to use for excising.
	 */
	static final SimpleEditorMap EM = SimpleEditorMap.L3;

	/**
	 * Excises a model to the given elements.
	 * @param model model to excise
	 * @param p pathway to excise to
	 * @return excised model
	 */
	public static Model excise(Model model, Pathway p)
	{
		Completer c = new Completer(EM);

		Set<BioPAXElement> result = c.complete(new HashSet<BioPAXElement>(Arrays.asList(p)), model);

		Cloner cln = new Cloner(EM, BioPAXLevel.L3.getDefaultFactory());

		return cln.clone(model, result);
	}
}
