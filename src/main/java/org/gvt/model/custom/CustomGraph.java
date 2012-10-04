package org.gvt.model.custom;

import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;

import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class CustomGraph extends CompoundModel
{
	public CustomGraph(Map<String, String> map)
	{
		if (map != null)
		{
			if (map.containsKey(TEXT)) setText(map.get(TEXT));
		}
	}

	// Parameter names

	public static final String TYPE = "TYPE";
	public static final String TEXT = "TEXT";
	public static final String TOOLTIP = "TOOLTIP";
	public static final String ID = "ID";
	public static final String BGCOLOR = "BGCOLOR";
	public static final String TEXTCOLOR = "TEXTCOLOR";
	public static final String BORDERCOLOR = "BORDERCOLOR";
	public static final String LINECOLOR = "LINECOLOR";
	public static final String SHAPE = "SHAPE";
	public static final String STYLE = "STYLE";
	public static final String ARROW = "ARROW";
	public static final String SOURCE = "SOURCE";
	public static final String TARGET = "TARGET";
	public static final String HEIGHT = "HEIGHT";
	public static final String WIDTH = "WIDTH";
	public static final String MEMBERS = "MEMBERS";
	public static final String X = "X";
	public static final String Y = "Y";

	// Some values

	public static final String NODE = "NODE";
	public static final String COMPOUND = "COMPOUND";
	public static final String EDGE = "EDGE";
	public static final String GRAPH = "GRAPH";

	// Other constants

	public static final String PROPERTY_SEPARATOR = ":";
	public static final String ELEMENT_SEPARATOR = "\t";
	public static final String COMMENT_INDICATOR = "#";
	
	public static final Color COLOR_WHITE = new Color(null, 255, 255, 255);
	public static final Color COLOR_BLACK = new Color(null, 0, 0, 0);

	public static Color textToColor(String text)
	{
		String[] rgb = text.split(" ");

		try
		{
			return new Color(null,
				Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));

		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
			return new Color(null, 0, 0, 0);
		}
	}
}
