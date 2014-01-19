package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;
import org.gvt.gui.DataLegendDialog;
import org.gvt.gui.LegendDialog;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl3.BioPAXNode;
import org.patika.mada.util.AlterationData;
import org.patika.mada.util.ExperimentData;
import org.patika.mada.util.Representable;

/**
 * @author Merve Cakir
 *         <p/>
 *         Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class DataLegendAction extends Action
{
	ChisioMain main;
	ExperimentData data;

	public DataLegendAction(ChisioMain main, ExperimentData data)
	{
		super("Data Legend ...");

		this.main = main;
		this.data = data;
	}

	public void run()
	{
		new DataLegendDialog(main, data).open();
	}

	public static ExperimentData getData(NodeModel node, ChisioMain main)
	{
		if (!(node instanceof BioPAXNode)) return null;

		BioPAXGraph graph = main.getPathwayGraph();

		if (graph == null) return null;
		if (graph.getLastAppliedColoring() == null) return null;

		Representable data = ((BioPAXNode) node).getRepresentableData(
			graph.getLastAppliedColoring());

		if (data != null && data instanceof ExperimentData)
			return (ExperimentData) data;

		return null;
	}
}
