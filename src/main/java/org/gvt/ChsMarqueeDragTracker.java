package org.gvt;

import org.eclipse.gef.tools.MarqueeDragTracker;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.swt.SWT;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsMarqueeDragTracker extends MarqueeDragTracker
{
	public ChsMarqueeDragTracker()
	{
		super();
		setMarqueeBehavior(
			MarqueeSelectionTool.BEHAVIOR_NODES_AND_CONNECTIONS);
	}

	protected boolean handleDragInProgress()
	{
		if (!getCurrentInput().isShiftKeyDown() &&
			!getCurrentInput().isModKeyDown(SWT.CTRL))
		{
			getCurrentViewer().setSelection(new StructuredSelection());
		}

		super.handleDragInProgress();

		return true;
	}
}
