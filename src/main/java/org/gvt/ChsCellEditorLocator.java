package org.gvt;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Text;
import org.gvt.model.CompoundModel;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsCellEditorLocator implements CellEditorLocator
{
	private IFigure figure;

	public ChsCellEditorLocator(IFigure f)
	{
		figure = f;
	}

	public void relocate(CellEditor celleditor)
	{
		Text text = (Text) celleditor.getControl();
		Rectangle rect = figure.getBounds().getCopy();
		figure.translateToAbsolute(rect);
		text.setBounds(rect.x,
			rect.y + rect.height - CompoundModel.LABEL_HEIGHT,
			rect.width, CompoundModel.LABEL_HEIGHT);
	}
}
