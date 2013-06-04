package org.gvt.inspector;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Point;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.GraphObject;
import org.gvt.model.CompoundModel;
import org.gvt.ChisioMain;

/**
 * This class maintains the graph inspector window.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class GraphInspector extends Inspector
{
	/**
	 * Constructor to open the inspector window
	 */
	private GraphInspector(GraphObject model, String title, ChisioMain main)
	{
		super(model, title, main);
	}

	private void show()
	{
		createContents(shell);

		// Display it in the middle
		Point loc = shell.getParent().getShell().getLocation();
		Point size = shell.getParent().getShell().getSize();
		Point s = shell.getSize();
		shell.setLocation(size.x/2 + loc.x - s.x/2, size.y/2 + loc.y -s.y/2);
		shell.open();
	}

	private void prepare()
	{
		TableItem item = addRow(table, "Margin");
		item.setText(1, String.valueOf(CompoundModel.MARGIN_SIZE));

		item = addRow(table, "Highlight Color");
		item.setBackground(1, ChisioMain.higlightColor);
	}

	private void prepareForBioPAXGraph()
	{
		BioPAXGraph graph = (BioPAXGraph) model;

		for (String[] property : graph.getInspectable())
		{
			addRow(table, property[0]).setText(1, property[1]);
		}
		table.getColumn(1).pack();
		table.pack();
	}


	public void setAsDefault()
	{
		// Not enabled for this inspector
	}

	public static void getInstance(GraphObject model,
		String title,
		ChisioMain main)
	{
		if (isSingle(model))
		{
			GraphInspector ins = new GraphInspector(model, title, main);
			instances.add(ins);

			ins.prepare();

			if (model instanceof BioPAXGraph)
			{
				ins.prepareForBioPAXGraph();
			}

			ins.show();
		}
	}
}