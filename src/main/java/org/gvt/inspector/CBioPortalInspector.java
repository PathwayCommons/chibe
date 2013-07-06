package org.gvt.inspector;

import org.gvt.ChisioMain;
import org.gvt.model.GraphObject;
import org.gvt.model.basicsif.BasicSIFNode;
import org.gvt.model.biopaxl3.Actor;
import org.gvt.model.biopaxl3.BioPAXNode;
import org.gvt.model.sifl3.SIFNode;

public class CBioPortalInspector extends Inspector
{
	protected CBioPortalInspector(GraphObject model, String title, ChisioMain main)
	{
		super(model, title, main);

		// Overriding the title because it adds "Properties" at the end otherwise.
		shell.setText(title);
	}

	private void prepareForGraphObject()
	{
		org.patika.mada.graph.GraphObject go = (org.patika.mada.graph.GraphObject) model;
		if (go instanceof Actor || go instanceof BasicSIFNode || go instanceof SIFNode)
		{
			for (String[] property : ((BioPAXNode) go).getCBioDataInspectable(main))
			{
				addRow(table, property[0]).setText(1, property[1]);
			}
			table.getColumn(1).pack();
			table.pack();
		}
	}

	private void show()
	{
		createContents(shell);

		shell.setLocation(calculateInspectorLocation(main.clickLocation.x, main.clickLocation.y));
		shell.open();
	}

	public static void getInstance(GraphObject model, String title, ChisioMain main)
	{
		if (model instanceof org.patika.mada.graph.GraphObject && isSingle(model))
		{
			CBioPortalInspector inspector = new CBioPortalInspector(model, title, main);
			instances.add(inspector);
			inspector.prepareForGraphObject();
			inspector.show();
		}
	}


}
