package org.gvt.action;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.ExportToSIFDialog;
import org.gvt.model.sif.SIFGraph;

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
	 * Rule types to discover and write in SIF.
	 */
	List<BinaryInteractionType> ruleTypes;

	public ExportToSIFAction(ChisioMain main)
	{
		super("Create SIF View ...");
		setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "../icon/sif.png"));
		setToolTipText(getText());
		this.main = main;
		this.ruleTypes = new ArrayList<BinaryInteractionType>();
	}

	public void run()
	{
		Model model = main.getOwlModel();

		if (model == null)
		{
			return;
		}

		List<BinaryInteractionType> possibleRuleTypes = SIFGraph.getPossibleRuleTypes(
			main.getOwlModel().getLevel());

		ExportToSIFDialog dialog = new ExportToSIFDialog(main.getShell(),
			possibleRuleTypes, ruleTypes);

		if (dialog.open() && !ruleTypes.isEmpty())
		{
			SIFGraph sif = new SIFGraph(model, ruleTypes);

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

		ruleTypes.clear();
	}
}
