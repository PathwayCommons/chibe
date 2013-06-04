package org.gvt;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.widgets.Text;
import org.gvt.model.GraphObject;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsDirectEditManager extends DirectEditManager
{
	private GraphObject model;

	public ChsDirectEditManager(GraphicalEditPart source,
		Class editorType,
		CellEditorLocator locator)
	{
		super(source, editorType, locator);
		model = (GraphObject) source.getModel();
	}

	/* ( Javadoc)
	* @see org.eclipse.gef.tools.DirectEditManager#initCellEditor()
	*/
	protected void initCellEditor()
	{
		getCellEditor().setValue(model.getText());
		Text text = (Text) getCellEditor().getControl();
		text.selectAll();
	}
}
