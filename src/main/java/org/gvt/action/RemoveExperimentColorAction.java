package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class RemoveExperimentColorAction extends Action
{
	private ChisioMain main;

	private BioPAXGraph graph;

	public RemoveExperimentColorAction(ChisioMain main)
	{
		super("Remove Data Colors");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/uncolor-experiment.png"));
		setToolTipText(getText());
		this.main = main;
	}

	public RemoveExperimentColorAction(ChisioMain main, BioPAXGraph graph)
	{
		this(main);
		this.graph = graph;
	}

	public void run()
	{
		if (graph == null)
		{
			graph = main.getPathwayGraph();
		}
		if (graph == null)
		{
			return;
		}

		graph.removeRepresentations();
		graph.setLastAppliedColoring(null);

		// Forget the graph
		graph = null;
	}
}