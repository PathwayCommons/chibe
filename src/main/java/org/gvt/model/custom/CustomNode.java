package org.gvt.model.custom;

import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.gvt.command.CreateCommand;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;

import java.util.Map;

import static org.gvt.model.custom.CustomGraph.*;

/**
 * @author Ozgun Babur
 */
public class CustomNode extends NodeModel implements Cloneable
{
	boolean duplicate;

	public CustomNode(CompoundModel root, Map<String, String> map)
	{
		CreateCommand command = new CreateCommand(root, this);
		command.execute();
		
		if (map.containsKey(TEXT)) setText(map.get(TEXT));
		else setText("node");

		if (map.containsKey(TOOLTIP)) setTooltipText(map.get(TOOLTIP));

		if (map.containsKey(BGCOLOR)) setColor(textToColor(map.get(BGCOLOR)));
		else setColor(COLOR_WHITE);

		if (map.containsKey(BORDERCOLOR)) setBorderColor(textToColor(map.get(BORDERCOLOR)));
		else setBorderColor(COLOR_BLACK);

		if (map.containsKey(TEXTCOLOR)) setTextColor(textToColor(map.get(TEXTCOLOR)));

		int width = map.containsKey(WIDTH) ? Integer.parseInt(map.get(WIDTH)) : suggestWidth();
		int height = map.containsKey(HEIGHT) ? Integer.parseInt(map.get(HEIGHT)) : 20;

		setSize(new Dimension(width, height));

		if (map.containsKey(SHAPE)) setShape(map.get(SHAPE));
		else setShape("RoundRect");

	}

	public CustomNode(CompoundModel root, CustomNode copyFrom)
	{
		CreateCommand command = new CreateCommand(root, this);
		command.execute();

		setText(copyFrom.getText());
		setTooltipText(copyFrom.getTooltipText());
		setColor(copyFrom.getColor());
		setBorderColor(copyFrom.getBorderColor());
		setTextColor(copyFrom.getTextColor());
		setSize(copyFrom.getSize());
		setShape(copyFrom.getShape());
	}

	protected int suggestWidth()
	{
		Dimension dim = TextUtilities.INSTANCE.getStringExtents(getText(), getTextFont());
		return Math.max(Math.min(dim.width + 4, 100), 20);
	}

	public boolean isDuplicate()
	{
		return duplicate;
	}
}
