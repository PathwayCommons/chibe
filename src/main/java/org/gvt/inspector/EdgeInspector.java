package org.gvt.inspector;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Font;
import org.gvt.model.*;
import org.gvt.ChisioMain;

/**
 * This class maintains the edge inspector window.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class EdgeInspector extends Inspector
{
	/**
	 * Constructor to open the inspector window
	 */
	private EdgeInspector(GraphObject model, String title, ChisioMain main)
	{
		super(model, title, main);
	}

	private void prepare()
	{
		TableItem item = addRow(table, "Text");
		item.setText(1, model.getText());

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

		item = addRow(table, "Style");
		item.setText(1, ((EdgeModel) model).getStyle());

		item = addRow(table, "Arrow");
		item.setText(1, ((EdgeModel) model).getArrow());

		item = addRow(table, "Width");
		item.setText(1, "" + ((EdgeModel) model).getWidth());
	}

	private void show()
	{
		createContents(shell);

		shell.setLocation(calculateInspectorLocation(main.clickLocation.x,
			main.clickLocation.y));
		shell.open();
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

	public static void getInstance(GraphObject model, String title, ChisioMain main)
	{
		if (isSingle(model))
		{
			EdgeInspector ins = new EdgeInspector(model, title, main);
			instances.add(ins);

			ins.prepare();

			if (model instanceof org.patika.mada.graph.GraphObject)
			{
				ins.prepareForGraphObject();
			}

			ins.show();
		}
	}
}