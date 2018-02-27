package org.gvt.inspector;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.TableItem;
import org.gvt.ChisioMain;
import org.gvt.model.GraphObject;
import org.gvt.model.NodeModel;

/**
 * This class maintains the node inspector window.
 * 
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class NodeInspector extends Inspector
{
	 NodeModel node;

	/**
	 * Constructor to open the inspector window
	 */
	private NodeInspector(GraphObject model, String title, ChisioMain main)
	{
		super(model, title, main);

		this.node = (NodeModel) model;
	}

	private void prepare()
	{
		TableItem item = addRow(table, "Text");
		item.setText(1, model.getText());

		if (model.getTooltipText() != null)
		{
			item = addRow(table, "Tooltip");
			item.setText(1, model.getTooltipText());
		}

		item = addRow(table, "Text Font");
		Font font = model.getTextFont();
		String fontName = font.getFontData()[0].getName();
		int fontSize = font.getFontData()[0].getHeight();
		int fontStyle = font.getFontData()[0].getStyle();

		if (fontSize > 14)
		{
			fontSize = 14;
		}

		item.setText(1, fontName);
		item.setFont(1,	new Font(null, fontName, fontSize, fontStyle));
		item.setForeground(1, model.getTextColor());

		item = addRow(table, "Color");
		item.setBackground(1, model.getColor());

		item = addRow(table, "Border Color");
		item.setBackground(1, ((NodeModel) model).getBorderColor());

		item = addRow(table, "Shape");
		item.setText(1, ((NodeModel) model).getShape());
	}

	private void prepareForGraphObject()
	{
		org.patika.mada.graph.GraphObject go = (org.patika.mada.graph.GraphObject) model;

		for (String[] property : go.getInspectable())
		{
			addRow(table, property[0]).setText(1, property[1]);
		}
		table.getColumn(1).pack();
		table.pack();
	}

	private void show()
	{
		createContents(shell);

		shell.setLocation(calculateInspectorLocation(main.clickLocation.x, main.clickLocation.y));
		shell.open();
	}

	public static void getInstance(GraphObject model, String title, ChisioMain main)
	{
		if (isSingle(model))
		{
			NodeInspector ndIns = new NodeInspector(model, title, main);
			instances.add(ndIns);

			ndIns.prepare();

			if (model instanceof org.patika.mada.graph.GraphObject)
			{
				ndIns.prepareForGraphObject();
			}

			ndIns.show();
		}
	}
}