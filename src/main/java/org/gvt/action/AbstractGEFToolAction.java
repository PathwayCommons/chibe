package org.gvt.action;

import org.eclipse.gef.Tool;
import org.eclipse.gef.EditDomain;
import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public abstract class AbstractGEFToolAction extends Action
{
	protected ChisioMain main;

	private Tool tool;

	public AbstractGEFToolAction(String text, ChisioMain main)
	{
		super(text, AS_CHECK_BOX);
		setToolTipText(text);
		this.main = main;
		tool = createTool();
	}

	abstract protected Tool createTool();

	public void run()
	{
		EditDomain editDomain = main.getEditDomain();
		if (editDomain != null)
		{
			editDomain.setActiveTool(tool);
		}
	}
}